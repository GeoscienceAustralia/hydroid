package au.gov.ga.hydroid.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.WriteOutContentHandler;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by u24529 on 17/02/2016.
 */
public class IOUtils {

   private IOUtils() {
   }

   public static byte[] fromInputStreamToByteArray(InputStream input) {
      byte[] output = null;
      try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
         int bytesRead = 0;
         byte[] buffer = new byte[4096];
         while ((bytesRead = input.read(buffer)) > 0) {
            baos.write(buffer, 0, bytesRead);
         }
         output = baos.toByteArray();
      } catch (Exception e) {
         throw new HydroidException(e);
      }
      return output;
   }

   public static String parseStream(InputStream stream) {
      return parseStream(stream, new Metadata());
   }

   public static String parseStream(InputStream stream, Metadata metadata) {
      return parseStream(stream, metadata, new AutoDetectParser());
   }

   public static String parseStream(InputStream stream, Metadata metadata, Parser parser) {
      if (metadata == null) {
         throw new HydroidException("parseStream - the metadata parameter cannot be null");
      }
      WriteOutContentHandler handler = new WriteOutContentHandler(-1);
      ParseContext context = new ParseContext();
      try {
         parser.parse(stream, handler, metadata, context);
      } catch (Exception e) {
         throw new HydroidException(e);
      }
      return handler.toString();
   }

   public static void sendResponseError(HttpServletResponse response, int errorCode) {
      try {
         response.sendError(errorCode);
      } catch (Exception e) {
         throw new HydroidException(e);
      }
   }

   public static InputStream getUrlContent(String url) {
      try {
         URL obj = new URL(url);
         HttpURLConnection con = (HttpURLConnection) obj.openConnection();
         int responseCode = con.getResponseCode();
         if (responseCode < 300) {
            return con.getInputStream();
         }
      } catch (Exception e) {
         throw new HydroidException(e);
      }
      return null;
   }

   public static String getSha1Hash(byte[] input) {
      try {
         return DigestUtils.sha1Hex(input);
      } catch (Exception e) {
         throw new HydroidException(e);
      }
   }

}
