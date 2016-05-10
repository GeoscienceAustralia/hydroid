package au.gov.ga.hydroid.integration;

import au.gov.ga.hydroid.HydroidApplication;
import au.gov.ga.hydroid.HydroidConfiguration;
import au.gov.ga.hydroid.dto.DocumentDTO;
import au.gov.ga.hydroid.model.DocumentType;
import au.gov.ga.hydroid.service.EnhancerService;
import au.gov.ga.hydroid.service.S3Client;
import au.gov.ga.hydroid.utils.IOUtils;
import org.apache.http.client.utils.DateUtils;
import org.apache.tika.metadata.Metadata;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;

/**
 * Created by u24529 on 3/02/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(HydroidApplication.class)
@IntegrationTest
public class EnhancerServiceTestIT {

   @Autowired
   private HydroidConfiguration configuration;

   @Autowired
   private EnhancerService enhancerService;

   @Autowired
   @Qualifier("s3ClientImpl")
   private S3Client s3Client;

   @Test
   public void testEnhance() {
      String origin = "/testfiles/36_4_1175-1197_Buss_and_Clote.pdf";
      Metadata metadata = new Metadata();
      DocumentDTO document = new DocumentDTO();
      document.setDocType(DocumentType.DOCUMENT.name());
      document.setContent(IOUtils.parseStream(this.getClass().getResourceAsStream(origin)));
      document.setTitle(metadata.get("title") == null ? "36_4_1175-1197_Buss_and_Clote.pdf" : metadata.get("title"));
      document.setAuthor(metadata.get("author") == null ? metadata.get("Author") : metadata.get("author"));
      if (metadata.get("Creation-Date") != null) {
         document.setDateCreated(DateUtils.parseDate(metadata.get("Creation-Date"), new String[]{"yyyy-MM-dd'T'HH:mm:ss'Z'"}));
      }
      enhancerService.enhance(document);
   }

   @Test
   public void testEnhanceFromS3() {
      byte[] s3FileContent = s3Client.getFileAsByteArray(configuration.getS3Bucket(),
            "enhancer/input/documents/13658816.2011_IJGIS_HUANGetal.pdf");
      String origin = configuration.getS3Bucket() + ":enhancer/input/documents/13658816.2011_IJGIS_HUANGetal.pdf";
      String sha1Hash = IOUtils.getSha1Hash(s3FileContent);

      Metadata metadata = new Metadata();
      DocumentDTO document = new DocumentDTO();
      document.setDocType(DocumentType.DOCUMENT.name());
      document.setContent(IOUtils.parseStream(new ByteArrayInputStream(s3FileContent), metadata));
      document.setOrigin(origin);
      document.setSha1Hash(sha1Hash);

      String fallBackTitle = "13658816.2011_IJGIS_HUANGetal.pdf";
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

      Assert.assertTrue(enhancerService.enhance(document));
   }



   @Test
   public void testEnhanceDocuments() {
      enhancerService.enhanceDocuments();
   }

   @Test
   public void testEnhanceImages() {
      enhancerService.enhanceImages();
   }

   @Test
   public void testMatchedGAVocabs() {
      ReflectionTestUtils.setField(configuration, "stanbolChain", "hydroid");
      ReflectionTestUtils.setField(configuration, "storeGAVocabsOnly", true);
      DocumentDTO document = new DocumentDTO();
      document.setTitle("Document Title Created: " + System.currentTimeMillis());
      document.setDocType(DocumentType.DOCUMENT.name());
      document.setOrigin("Pasted Content");
      document.setContent("This enhancement should find Corals, Terrace and Bob Marley. But Bob Marley should be discarded.");
      Assert.assertTrue(enhancerService.enhance(document));
   }

   @Test
   public void testNotMatchedGAVocabs() {
      ReflectionTestUtils.setField(configuration, "stanbolChain", "default");
      ReflectionTestUtils.setField(configuration, "storeGAVocabsOnly", true);
      DocumentDTO document = new DocumentDTO();
      document.setTitle("Document Title Created: " + System.currentTimeMillis());
      document.setDocType(DocumentType.DOCUMENT.name());
      document.setOrigin("Pasted Content");
      document.setContent("This enhancement should find Corals, Terrace and Bob Marley. But Bob Marley should be discarded.");
      Assert.assertFalse(enhancerService.enhance(document));
   }

   @Test
   public void testEnhanceImage() {
      ReflectionTestUtils.setField(configuration, "stanbolChain", "hydroid");
      ReflectionTestUtils.setField(configuration, "storeGAVocabsOnly", true);
      DocumentDTO document = new DocumentDTO();
      document.setTitle("2.3_shark Whitetip Reef Shark_0.jpg");
      document.setDocType(DocumentType.IMAGE.name());
      document.setOrigin("hydroid:enhancer/input/images/20160429/2.3_shark Whitetip Reef Shark_0.jpg");
      document.setContent("The labels found for 2.3_shark Whitetip Reef Shark_0.jpg are fish (0.94), marine biology (0.92), coral reef (0.89), underwater (0.87), biology (0.87), coral reef fish (0.84), reef (0.83), sea (0.73), coral (0.62), invertebrate (0.59)");
      Assert.assertFalse(enhancerService.enhance(document));
   }

   @Test
   public void testSameContentHash() {
      byte[] file1 = s3Client.getFileAsByteArray(configuration.getS3Bucket(), "enhancer/input/documents/13658816.2011_IJGIS_HUANGetal.pdf");
      byte[] file2 = s3Client.getFileAsByteArray(configuration.getS3Bucket(), "enhancer/input/documents/20160408/05_fsr06_wtbf.pdf");

      String hashFile1 = IOUtils.getSha1Hash(file1);
      String hashFile2 = IOUtils.getSha1Hash(file2);

      Assert.assertEquals(hashFile1, hashFile2);
   }

}
