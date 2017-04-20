package au.gov.ga.hydroid.service;

import au.gov.ga.hydroid.HydroidApplication;
import au.gov.ga.hydroid.HydroidConfiguration;
import au.gov.ga.hydroid.dto.DocumentDTO;
import au.gov.ga.hydroid.mock.CustomMockJenaService;
import au.gov.ga.hydroid.mock.CustomMockStanbolClient;
import au.gov.ga.hydroid.model.DocumentType;
import au.gov.ga.hydroid.model.HydroidSolrMapper;
import au.gov.ga.hydroid.service.impl.EnhancerServiceImpl;
import au.gov.ga.hydroid.service.impl.FileSystemClientImpl;
import au.gov.ga.hydroid.service.impl.ImageServiceImpl;
import au.gov.ga.hydroid.utils.HydroidException;
import au.gov.ga.hydroid.utils.IOUtils;
import org.apache.http.client.utils.DateUtils;
import org.apache.tika.metadata.Metadata;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.ReflectionUtils;

/**
 * Created by u24529 on 7/04/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(HydroidApplication.class)
public class EnhancerServiceTest {

   @Autowired
   private HydroidConfiguration wiredConfiguration;

   @Autowired
   private ApplicationContext applicationContext;

   @Autowired
   private HydroidSolrMapper hydroidSolrMapper;

   @Mock
   private SolrClient solrClient;

   @Mock
   private DocumentService documentService;

   private EnhancerService enhancerService;

   private HydroidConfiguration configuration;

   @Before
   public void setup() {
      MockitoAnnotations.initMocks(this);
      enhancerService = new EnhancerServiceImpl();
      // Create a new instance of configuration and copy values from the auto wired
      // one to prevent temporary changes to its properties to affect the result of
      // other tests
      try {
         configuration = wiredConfiguration.getClass().newInstance();
      } catch (Exception e) {
         throw new HydroidException(e);
      }
      System.setProperty("s3.use.file.system.path", "src/test/resources/testfiles/");
      ReflectionUtils.shallowCopyFieldState(wiredConfiguration, configuration);
      ReflectionTestUtils.setField(enhancerService, "configuration", configuration);
      ReflectionTestUtils.setField(enhancerService, "stanbolClient", new CustomMockStanbolClient());
      ReflectionTestUtils.setField(enhancerService, "solrClient", solrClient);
      ReflectionTestUtils.setField(enhancerService, "s3Client", new FileSystemClientImpl());
      ReflectionTestUtils.setField(enhancerService, "jenaService", new CustomMockJenaService());
      ReflectionTestUtils.setField(enhancerService, "documentService", documentService);
      ReflectionTestUtils.setField(enhancerService, "imageService", new ImageServiceImpl());
      ReflectionTestUtils.setField(enhancerService, "applicationContext", applicationContext);
      ReflectionTestUtils.setField(hydroidSolrMapper, "configuration", configuration);
      ReflectionTestUtils.setField(enhancerService, "hydroidSolrMapper", hydroidSolrMapper);
   }

   @Test
   public void testEnhance() {
      String origin = "/testfiles/36_4_1175-1197_Buss_and_Clote.pdf";
      Metadata metadata = new Metadata();
      DocumentDTO document = new DocumentDTO();
      document.setDocType(DocumentType.DOCUMENT.name());
      document.setContent(IOUtils.parseStream(this.getClass().getResourceAsStream(origin), metadata));
      document.setTitle(metadata.get("title"));
      document.setAuthor(metadata.get("author") == null ? metadata.get("Author") : metadata.get("author"));
      document.setDateCreated(DateUtils.parseDate(metadata.get("Creation-Date"), new String[]{"yyyy-MM-dd'T'HH:mm:ss'Z'"}));
      Assert.assertTrue(enhancerService.enhance(document));
   }

   @Test
   public void testEnhanceDocuments() {
      enhancerService.enhanceDocuments();
   }

   @Test
   public void testEnhanceDatasets() {
      enhancerService.enhanceDatasets();
   }

   @Test
   public void testEnhanceModels() {
      enhancerService.enhanceModels();
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

}
