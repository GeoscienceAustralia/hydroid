package au.gov.ga.hydroid.service.impl;

import au.gov.ga.hydroid.HydroidConfiguration;
import au.gov.ga.hydroid.dto.DocumentDTO;
import au.gov.ga.hydroid.dto.ImageAnnotation;
import au.gov.ga.hydroid.dto.ImageMetadata;
import au.gov.ga.hydroid.model.Document;
import au.gov.ga.hydroid.model.DocumentType;
import au.gov.ga.hydroid.model.EnhancementStatus;
import au.gov.ga.hydroid.model.HydroidSolrMapper;
import au.gov.ga.hydroid.service.*;
import au.gov.ga.hydroid.utils.HydroidException;
import au.gov.ga.hydroid.utils.IOUtils;
import au.gov.ga.hydroid.utils.StanbolMediaTypes;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.utils.DateUtils;
import org.apache.http.entity.ContentType;
import org.apache.jena.rdf.model.Statement;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AbstractParser;
import org.imgscalr.Scalr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by u24529 on 3/02/2016.
 */
@Service
public class EnhancerServiceImpl implements EnhancerService {

   private static final Logger logger = LoggerFactory.getLogger(EnhancerServiceImpl.class);

   @Autowired
   private HydroidConfiguration configuration;

   @Autowired
   private StanbolClient stanbolClient;

   @Autowired
   private SolrClient solrClient;

   @Autowired
   private JenaService jenaService;

   @Autowired
   @Value("#{systemProperties['s3.use.file.system'] != null ? s3FileSystem : s3ClientImpl}")
   private S3Client s3Client;

   @Autowired
   private DocumentService documentService;

   @Autowired
   @Value("#{systemProperties['use.local.image.service'] != null ? localImageService : googleVisionImageService}")
   private ImageService imageService;

   @Autowired
   private ApplicationContext applicationContext;

   @Autowired
   private HydroidSolrMapper hydroidSolrMapper;

   private String getFileNameFromS3ObjectSummary(DataObjectSummary objectSummary) {
      return objectSummary.getKey().substring(objectSummary.getKey().lastIndexOf("/") + 1);
   }

   private String getImageThumb(BufferedImage image, String urn) {
      try {
         BufferedImage resized = Scalr.resize(image, 200);
         ByteArrayOutputStream os = new ByteArrayOutputStream();
         ImageIO.write(resized,"png", os);
         InputStream byteArrayInputStream = new ByteArrayInputStream(os.toByteArray());
         s3Client.storeFile(
               configuration.getS3OutputBucket(),
               configuration.getS3EnhancerOutputImages() + urn + "_thumb",
               byteArrayInputStream,
               "image/png");
         return configuration.getS3OutputUrl() + "/images/" + urn + "_thumb";
      } catch (Exception e) {
         throw new HydroidException(e);
      }
   }

   private void processFailure(DocumentDTO document, String urn, String reason) {
      logger.info("processFailure - saving document in the database");
      saveOrUpdateDocument(document, urn, EnhancementStatus.FAILURE, reason);
      logger.info("processFailure - document saved in the database");

      // Also store original image metadata
      if (document.getDocType().equals(DocumentType.IMAGE.name())) {
         logger.info("processFailure - saving image metadata in the database");
         saveOrUpdateImageMetadata(document.getOrigin(), document.getContent());
         logger.info("processFailure - image metadata saved");
      }
   }

   private void saveImageDetails(String urn, DocumentDTO document, Properties properties) throws IOException {
      logger.info("saveImageDetails - saving image in S3 and its metadata in the database");
      int bucketEndPosition = document.getOrigin().indexOf(":") + 1;
      s3Client.copyObject(configuration.getS3Bucket(), document.getOrigin().substring(bucketEndPosition),
            configuration.getS3OutputBucket(), configuration.getS3EnhancerOutputImages() + urn);
      saveOrUpdateImageMetadata(document.getOrigin(), document.getContent());
      logger.info("saveImageDetails - original image content and metadata saved");
      InputStream origImage = s3Client.getFile(configuration.getS3Bucket(),
            document.getOrigin().substring(bucketEndPosition));
      BufferedImage image = ImageIO.read(origImage);
      properties.put("imgThumb", getImageThumb(image, urn));
   }

   @Override
   public boolean enhance(DocumentDTO document) {

      String urn = null;

      try {

         // Send content to Stanbol for enhancement
         logger.info("enhance - about to post to stanbol server");
         String enhancedText = stanbolClient.enhance(configuration.getStanbolChain(), document.getContent(),
               StanbolMediaTypes.RDFXML);
         logger.info("enhance - received results from stanbol server");
         enhancedText = StringUtils.replace(enhancedText, ":content-item-sha1-", ":content-item-sha1:");
         logger.info("enhance - changed urn pattern, still contain old: " + enhancedText.contains(":content-item-sha1-"));

         // Parse enhancedText into an rdf document
         List<Statement> rdfDocument = jenaService.parseRdf(enhancedText, "");
         if (rdfDocument == null) {
            return false;
         }

         // Generate dictionary with properties we are interested in
         Properties properties = hydroidSolrMapper.generateDocument(rdfDocument, document);
         urn = properties.getProperty("about");

         // Content has NOT been tagged with our vocabularies
         if (properties.isEmpty()) {
            processFailure(document, urn, "No matches were found in the vocabularies used by the chain: "
                  + configuration.getStanbolChain());
            return false;
         }

         // Store full enhanced doc (rdf) in S3
         s3Client.storeFile(configuration.getS3OutputBucket(), configuration.getS3EnhancerOutput() + urn,
               enhancedText, ContentType.APPLICATION_XML.getMimeType());

         // Also store original image in S3
         if (document.getDocType().equals(DocumentType.IMAGE.name())) {
            saveImageDetails(urn, document, properties);
         }

         // Add enhanced document to Solr
         logger.info("enhance - about to add document to solr");
         solrClient.addDocument(configuration.getSolrCollection(), properties);
         logger.info("enhance - document added to solr");

         // Store full document in DB
         logger.info("enhance - saving document in the database");
         saveOrUpdateDocument(document, urn, EnhancementStatus.SUCCESS, null);
         logger.info("enhance - document saved in the database");

         // Store full enhanced doc (rdf) in Jena
         logger.info("enhance - about to store RDF in Jena");
         jenaService.storeRdfDefault(enhancedText, "");
         logger.info("enhance - RDF stored in Jena");

         return true;

      } catch (Exception e) {
         logger.error("enhance - Exception: ", e);

         processFailure(document, urn, e.getMessage());

         // if there was any error in the process we remove the documents stored under the URN if created
         rollbackEnhancement(urn);

         return false;
      }
   }

   private void saveOrUpdateDocument(DocumentDTO documentDTO, String urn, EnhancementStatus status, String statusReason) {
      Document document = documentService.findByOrigin(documentDTO.getOrigin());
      if (document == null) {
         document = new Document();
      }
      document.setOrigin(documentDTO.getOrigin());
      document.setUrn(urn);
      document.setTitle(documentDTO.getTitle());
      document.setType(DocumentType.valueOf(documentDTO.getDocType()));
      document.setStatus(status);
      document.setStatusReason(statusReason);
      document.setSha1Hash(documentDTO.getSha1Hash());
      if (document.getId() == 0) {
         documentService.create(document);
      } else {
         documentService.update(document);
      }
   }

   private void saveOrUpdateImageMetadata(String origin, String metadata) {
      if (documentService.readImageMetadata(origin) == null) {
         documentService.createImageMetadata(origin, metadata);
      } else {
         documentService.updateImageMetadata(origin, metadata);
      }
   }

   private List<DataObjectSummary> getDocumentsForEnhancement(List<DataObjectSummary> input) {
      List<DataObjectSummary> output = new ArrayList<>();
      if (input.isEmpty()) {
         return output;
      }
      String origin;
      Document document;
      for (DataObjectSummary object : input) {
         // Ignore folders
         if (object.getKey().endsWith("/")) {
            continue;
         }
         origin = object.getBucketName() + ":" + object.getKey();
         document = documentService.findByOrigin(origin);
         // Document was not enhanced or previous enhancement failed
         if (document == null || document.getStatus() == EnhancementStatus.FAILURE) {
               output.add(object);
            }
         }
      return output;
   }

   private void copyMetadataToDocument(Metadata metadata, DocumentDTO document, String fallBackTitle) {
      if (metadata.get("title") != null) {
         document.setTitle(metadata.get("title"));
      } else if (metadata.get("dc:title") != null) {
         document.setTitle(metadata.get("dc:title"));
      } else {
         document.setTitle(fallBackTitle);
      }
      document.setAuthor(metadata.get("author") == null ? metadata.get("Author") : metadata.get("author"));
      document.setDateCreated(metadata.get("Creation-Date") == null ? null :
            DateUtils.parseDate(metadata.get("Creation-Date"), new String[]{"yyyy-MM-dd'T'HH:mm:ss'Z'"}));
   }

   private boolean isDuplicate(String origin, String sha1Hash, DocumentType documentType) {
      Document existingDocument = documentService.findBySha1Hash(sha1Hash);

      // Same document found at a different source location skip and set status as duplicate
      if (existingDocument != null && !existingDocument.getOrigin().equals(origin)) {
         Document duplicate = new Document();
         duplicate.setOrigin(origin);
         duplicate.setTitle(existingDocument.getTitle());
         duplicate.setType(documentType);
         duplicate.setStatus(EnhancementStatus.DUPLICATE);
         duplicate.setStatusReason("Document already exists at " + existingDocument.getOrigin());
         documentService.create(duplicate);
         return true;
      }

      return false;
   }

   private void enhanceCollection(DocumentType documentType) {
      Metadata metadata;
      DocumentDTO document;
      byte[] s3FileContent;
      String key = configuration.getS3EnhancerInput() + documentType.name().toLowerCase() + "s";
      List<DataObjectSummary> objects = s3Client.listObjects(configuration.getS3Bucket(), key);
      objects = getDocumentsForEnhancement(objects);
      logger.info("enhanceCollection - there are " + objects.size() + " " + documentType.name().toLowerCase() + "s to be enhanced");
      for (DataObjectSummary object : objects) {
         document = new DocumentDTO();
         try {
            s3FileContent = s3Client.getFileAsByteArray(object.getBucketName(), object.getKey());
            String origin = object.getBucketName() + ":" + object.getKey();
            String sha1Hash = IOUtils.getSha1Hash(s3FileContent);

            if (isDuplicate(origin, sha1Hash, documentType)) {
               continue;
            }

            metadata = new Metadata();
            document.setContent(IOUtils.parseStream(new ByteArrayInputStream(s3FileContent), metadata));
            document.setOrigin(origin);
            document.setSha1Hash(sha1Hash);
            copyMetadataToDocument(metadata, document, getFileNameFromS3ObjectSummary(object));

            enhance(document);
         } catch (Exception e) {
            logger.error("enhanceCollection - error processing file key: " + object.getKey(), e);
            processFailure(document, null, e.getMessage());
         }
      }
   }

   private String getImageMetadataAsString(InputStream s3FileContent) {
      StringBuilder result = new StringBuilder();
      ImageMetadata imageMetadata = imageService.getImageMetadata(s3FileContent);
      if (!imageMetadata.getImageLabels().isEmpty()) {
         for (ImageAnnotation imageLabel : imageMetadata.getImageLabels()) {
            result.append(imageLabel.getDescription()).append(" (").append(imageLabel.getScore()).append("), ");
         }
         result.setLength(result.length() - 2);
      }
      return result.toString();
   }

   private void enhancePendingDocuments() {
      Metadata metadata;
      DocumentDTO document;

      List<Document> documents = documentService.findByStatus(EnhancementStatus.PENDING);
      logger.info("enhancePendingDocuments - there are " + documents.size() + " pending documents to be enhanced");
      for (Document dbDocument : documents) {
         document = new DocumentDTO();
         try {
            metadata = new Metadata();
            InputStream inputStream = IOUtils.getUrlContent(dbDocument.getOrigin());

            // User custom parser
            if (dbDocument.getParserName() != null) {
               AbstractParser parser = (AbstractParser) applicationContext.getBean(dbDocument.getParserName());
               document.setContent(IOUtils.parseStream(inputStream, metadata, parser));
            // Use default parser
            } else {
               document.setContent(IOUtils.parseStream(inputStream, metadata));
            }

            document.setOrigin(dbDocument.getOrigin());
            document.setSha1Hash(IOUtils.getSha1Hash(IOUtils.fromInputStreamToByteArray(inputStream)));
            copyMetadataToDocument(metadata, document, dbDocument.getOrigin());

            enhance(document);
         } catch (Exception e) {
            logger.error("enhancePendingDocuments - error processing URL: " + dbDocument.getOrigin(), e);
            processFailure(document, null, e.getMessage());
         }
      }
   }

   @Override
   public void enhanceDocuments() {
      enhanceCollection(DocumentType.DOCUMENT);
      enhancePendingDocuments();
   }

   @Override
   public void enhanceDatasets() {
      enhanceCollection(DocumentType.DATASET);
   }

   @Override
   public void enhanceModels() {
      enhanceCollection(DocumentType.MODEL);
   }

   @Override
   public void enhanceImages() {
      DocumentDTO document;
      byte[] s3FileContent;
      String key = configuration.getS3EnhancerInput() + DocumentType.IMAGE.name().toLowerCase() + "s";
      List<DataObjectSummary> objectsForEnhancement = getDocumentsForEnhancement(s3Client.listObjects(configuration.getS3Bucket(), key));
      logger.info("enhanceImages - there are " + objectsForEnhancement.size() + " images to be enhanced");
      for (DataObjectSummary s3ObjectSummary : objectsForEnhancement) {
         document = new DocumentDTO();
         try {
            s3FileContent = s3Client.getFileAsByteArray(s3ObjectSummary.getBucketName(), s3ObjectSummary.getKey());
            String origin = s3ObjectSummary.getBucketName() + ":" + s3ObjectSummary.getKey();
            String sha1Hash = IOUtils.getSha1Hash(s3FileContent);

            if (isDuplicate(origin, sha1Hash, DocumentType.IMAGE)) {
               continue;
            }

            document.setDocType(DocumentType.IMAGE.name());
            document.setTitle(getFileNameFromS3ObjectSummary(s3ObjectSummary));
            document.setOrigin(origin);

            // The cached imaged metadata will be used for enhancement (if exists)
            document.setContent(documentService.readImageMetadata(document.getOrigin()));

            // The image metadata will be extracted and used for enhancement
            if (document.getContent() == null) {
               document.setContent("The labels found for " + document.getTitle() + " are " +
                     getImageMetadataAsString(new ByteArrayInputStream(s3FileContent)));
               document.setSha1Hash(sha1Hash);
            }

            enhance(document);
         } catch (Exception e) {
            logger.error("enhanceImages - error processing file key: " + s3ObjectSummary.getKey(), e);
            processFailure(document, null, e.getMessage());
         }
      }
   }

   private void rollbackEnhancement(String urn) {
      if (urn == null) {
         return;
      }

      // Delete document from S3
      s3Client.deleteFile(configuration.getS3Bucket(), configuration.getS3EnhancerOutput() + urn);

      // Delete document from Solr
      solrClient.deleteDocument(configuration.getSolrCollection(), urn);
   }

}
