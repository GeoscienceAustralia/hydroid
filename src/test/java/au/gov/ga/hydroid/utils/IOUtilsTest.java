package au.gov.ga.hydroid.utils;

import org.apache.tika.metadata.Metadata;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.ConnectException;

/**
 * Created by u24529 on 15/04/2016.
 */
public class IOUtilsTest {

   @Test
   public void testFromInputStreamToByteArray() {
      byte[] testBytes = "test".getBytes();
      InputStream is = new ByteArrayInputStream(testBytes);
      Assert.assertEquals(new String(testBytes), new String(IOUtils.fromInputStreamToByteArray(is)));
   }

   @Test
   public void testFromInputStreamToByteArrayError() {
      try {
         Assert.assertEquals("test", new String(IOUtils.fromInputStreamToByteArray(null)));
      } catch (HydroidException e) {
         Assert.assertTrue(e.getCause() instanceof NullPointerException);
      }
   }

   @Test
   public void testParseStreamNullMetadata() {
      try {
         IOUtils.parseStream(null, null, null);
      } catch (HydroidException e) {
         Assert.assertEquals("parseStream - the metadata parameter cannot be null", e.getMessage());
      }
   }

   @Test
   public void testParseStreamNullInputStream() {
      try {
         IOUtils.parseStream(null, new Metadata(), null);
      } catch (HydroidException e) {
         Assert.assertTrue(e.getCause() instanceof NullPointerException);
      }
   }

   @Test
   public void testSendResponseError() {
      IOUtils.sendResponseError(new MockHttpServletResponse(), HttpStatus.OK.value());
   }

   @Test
   public void testSendResponseErrorError() {
      try {
         IOUtils.sendResponseError(null, HttpStatus.OK.value());
      } catch (HydroidException e) {
         Assert.assertTrue(e.getCause() instanceof NullPointerException);
      }
   }

   @Test
   public void testGetUrlContent() {
      Assert.assertNotNull(IOUtils.getUrlContent("http://ga.gov.au"));
   }

   @Test
   public void testGetUrlWrongAddress() {
      try {
         Assert.assertNull("Content: ", IOUtils.getUrlContent("http://wrong-test-url"));
      } catch (HydroidException e) {
         Assert.assertNotNull(e.getCause());
      }
   }

   @Test
   public void testGetUrlConnectionRefused() {
      try {
         Assert.assertNotNull(IOUtils.getUrlContent("http://localhost"));
      } catch (HydroidException e) {
         Assert.assertTrue(e.getCause() instanceof ConnectException);
      }
   }

   @Test
   public void testGetSha1Hash() {
      String documentPath = "/testfiles/36_4_1175-1197_Buss_and_Clote.pdf";
      String sha1Hash = IOUtils.getSha1Hash(IOUtils.fromInputStreamToByteArray(this.getClass().getResourceAsStream(documentPath)));
      Assert.assertNotNull("SHA1 is null: ", sha1Hash);
      Assert.assertEquals("SHA1 is not equal: ", "b5d5b085d117cbb2625c6177b3617cbfcdeaaaf7", sha1Hash);
   }

}
