package au.gov.ga.hydroid.model;

import au.gov.ga.hydroid.HydroidConfiguration;
import au.gov.ga.hydroid.dto.DocumentDTO;
import org.apache.jena.rdf.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by u24529 on 27/04/2016.
 */
@Component
public class HydroidSolrMapper {

   private static final String EXTRACTED_FROM = "extracted-from";
   private static final String ENTITY_REFERENCE = "entity-reference";
   private static final String ENTITY_LABEL = "entity-label";
   private static final String SELECTION_CONTEXT = "selection-context";
   private static final String GA_PUBLIC_VOCABS = "GAPublicVocabsSandbox";
   private static final String SOLR_DOCUMENT_KEY = "about";

   private static final List<String> VALID_PREDICATES = Arrays.asList(EXTRACTED_FROM, ENTITY_REFERENCE,
         ENTITY_LABEL, SELECTION_CONTEXT);

   @Autowired
   private HydroidConfiguration configuration;

   private Properties initProperties(DocumentDTO document) {
      Properties properties = new Properties();
      properties.put("content", document.getContent());
      properties.put("title", document.getTitle());
      properties.put("docType", document.getDocType());
      properties.put("docOrigin", document.getOrigin());
      if (document.getAuthor() != null) {
         properties.put("creator", document.getAuthor());
      }
      if (document.getDateCreated() != null) {
         properties.put("created", document.getDateCreated());
      }
      return properties;
   }

   private void addStatementToRDF(List<Statement> rdfDocument, Resource subject, String propertyName, String value) {
      Property property = ResourceFactory.createProperty(propertyName);
      RDFNode object = ResourceFactory.createPlainLiteral(value);
      rdfDocument.add(ResourceFactory.createStatement(subject, property, object));
   }

   private String addStatementsToRDF(List<Statement> rdfDocument, String about, DocumentDTO document) {
      String documentUrl = configuration.getS3OutputUrl()
            + (document.getDocType().equals(DocumentType.IMAGE.name()) ? "/images/" : "/rdfs/")
            + about;

      Resource subject = ResourceFactory.createResource(about);

      // Add property:type to rdf (DOCUMENT, DATASET, MODEL or IMAGE)
      addStatementToRDF(rdfDocument, subject, "https://www.w3.org/TR/rdf-schema/#ch_type", document.getDocType());

      // Add property:label to the rdf (the document title)
      addStatementToRDF(rdfDocument, subject, "https://www.w3.org/TR/rdf-schema/#ch_label", document.getTitle());

      // Added property:image to the RDF document
      if (document.getDocType().equals(DocumentType.IMAGE.name())) {
         addStatementToRDF(rdfDocument, subject, "http://purl.org/dc/dcmitype/Image", documentUrl);
      }

      return documentUrl;
   }

   // Add a new subject to the GA Vocab Subjects list
   private void addGAVocabSubject(Map<String,String> gaVocabSubjects, Statement statement) {
      String subject = statement.getSubject().getLocalName().toLowerCase();
      String predicate = statement.getPredicate().getLocalName().toLowerCase();
      String objectValue = statement.getObject().isLiteral() ? statement.getObject().asLiteral().getString()
            : statement.getObject().asResource().getURI();
      if ("entity-reference".equalsIgnoreCase(predicate) && objectValue.contains(GA_PUBLIC_VOCABS)
            && gaVocabSubjects.get(subject) == null) {
         gaVocabSubjects.put(subject, objectValue);
      }
   }

   // add new value if not there yet
   private void addMultiValuedField(Map<String,List<String>> multiValuedFields, String predicate, String objectValue) {
      List<String> multiValuedField = multiValuedFields.get(predicate);
      if (!multiValuedField.contains(objectValue)) {
         multiValuedField.add(objectValue);
      }
   }

   private boolean processStatement(Statement statement, Properties properties, Map<String,List<String>> multiValuedFields) {
      String predicate = statement.getPredicate().getLocalName().toLowerCase();
      String objectValue = statement.getObject().isLiteral() ? statement.getObject().asLiteral().getString()
            : statement.getObject().asResource().getURI();

      // Discard if the predicate is not included in the list we are after
      if (!VALID_PREDICATES.contains(predicate)) {
         return false;
      }

      if (EXTRACTED_FROM.equalsIgnoreCase(predicate) && !properties.containsKey(SOLR_DOCUMENT_KEY)) {
         properties.put(SOLR_DOCUMENT_KEY, objectValue);

      } else if (multiValuedFields.get(predicate) != null) {
         addMultiValuedField(multiValuedFields, predicate, objectValue);

      } else {
         properties.put(predicate, objectValue);
      }

      return true;
   }

   public Properties generateDocument(List<Statement> rdfDocument, DocumentDTO document) {
      Map<String,String> gaVocabSubjects = new HashMap<>();
      Map<String,List<String>> multiValuedFields = new HashMap<>();
      multiValuedFields.put(ENTITY_REFERENCE, new ArrayList<>());
      multiValuedFields.put(ENTITY_LABEL, new ArrayList<>());
      multiValuedFields.put(SELECTION_CONTEXT, new ArrayList<>());

      Properties properties = initProperties(document);

      for (Statement statement : rdfDocument) {
         if (processStatement(statement, properties, multiValuedFields)) {
            addGAVocabSubject(gaVocabSubjects, statement);
         }
      }

      // GAPublicVocabs is required but none was found
      boolean isGAVocabsNotValid = configuration.isStoreGAVocabsOnly() && gaVocabSubjects.isEmpty();

      // No labels or concepts were found so we discard the process by clearing all the properties
      if (isGAVocabsNotValid || (multiValuedFields.get(ENTITY_LABEL).isEmpty()
            && multiValuedFields.get(ENTITY_REFERENCE).isEmpty())) {
         properties.clear();

      } else {
         // Add additional statements to RDF and generate documentURL
         String documentUrl = addStatementsToRDF(rdfDocument, properties.getProperty(SOLR_DOCUMENT_KEY), document);

         properties.put("label", multiValuedFields.get(ENTITY_LABEL));
         properties.put("concept", multiValuedFields.get(ENTITY_REFERENCE));
         properties.put("docUrl", documentUrl);
         properties.put("selectionContext", multiValuedFields.get(SELECTION_CONTEXT));
      }

      return properties;
   }

}
