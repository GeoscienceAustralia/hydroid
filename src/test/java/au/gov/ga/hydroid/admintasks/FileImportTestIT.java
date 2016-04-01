package au.gov.ga.hydroid.admintasks;

import au.gov.ga.hydroid.HydroidApplication;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(HydroidApplication.class)
@IntegrationTest
public class FileImportTestIT {

   @Test
   public void testParseFile() {
      try (Stream<Path> fileStream = Files.walk(Paths.get("C:\\Data\\hydroid\\testing\\Articles\\Articles"))) {
         fileStream.forEach(filePath -> {
            try {
               if (Files.isRegularFile(filePath) && (Files.size(filePath) < (1024 * 1024))) {
                  Integer response = postFile("http://hydroid-dev-web-lb-1763223935.ap-southeast-2.elb.amazonaws.com/api/index-file", filePath.toFile());
                  if (response != 200)
                     System.out.print("File '" + filePath.toFile().getName() + "' failed with " + response.toString());
                  else
                     System.out.print("File '" + filePath.toFile().getName() + "' succeeded.");
               }
            } catch (Exception e) {
               e.printStackTrace();
            }
         });
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   private Integer postFile(String uri,File file) throws IOException {
      HttpClient httpClient = HttpClientBuilder.create().setProxy(new HttpHost("localhost",3128)).build();
      HttpPost httppost = new HttpPost(uri);
      ContentBody cbFile = new FileBody(file);
      MultipartEntityBuilder builder = MultipartEntityBuilder.create();
      builder.addPart("file", cbFile);
      builder.addPart("name",new StringBody(file.getName(), ContentType.TEXT_PLAIN));

      httppost.setEntity(builder.build());
      System.out.println("executing request " + httppost.getRequestLine());
      HttpResponse response = httpClient.execute(httppost);
      HttpEntity resEntity = response.getEntity();

      return response.getStatusLine().getStatusCode();
   }
}
