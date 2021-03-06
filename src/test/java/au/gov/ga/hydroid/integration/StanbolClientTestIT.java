package au.gov.ga.hydroid.integration;

import au.gov.ga.hydroid.HydroidApplication;
import au.gov.ga.hydroid.service.StanbolClient;
import au.gov.ga.hydroid.utils.StanbolMediaTypes;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Enumeration;
import java.util.Properties;


/**
 * Created by u24529 on 3/02/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(HydroidApplication.class)
@IntegrationTest
public class StanbolClientTestIT {

   @Autowired
   private StanbolClient stanbolClient;

   @Test
   public void testEnhance() {
      stanbolClient.enhance("default", "Bob Barley is cool", StanbolMediaTypes.RDFXML);
   }

   @Test
   public void testFindAllPredicates() {
      String enhancedText = stanbolClient.enhance("default", "Bob Barley is cool", StanbolMediaTypes.RDFXML);
      Properties allPredicates = stanbolClient.findAllPredicates(enhancedText);
      Enumeration<String> predicateNames = (Enumeration<String>) allPredicates.propertyNames();
      while (predicateNames.hasMoreElements()) {
         String predicate = predicateNames.nextElement();
         System.out.println(predicate + ": " + allPredicates.getProperty(predicate));
      }
   }

}
