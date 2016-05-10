package au.gov.ga.hydroid.service.impl;

import au.gov.ga.hydroid.HydroidConfiguration;
import au.gov.ga.hydroid.dto.ImageAnnotation;
import au.gov.ga.hydroid.dto.ImageMetadata;
import au.gov.ga.hydroid.service.ImageService;
import au.gov.ga.hydroid.utils.HydroidException;
import com.google.api.client.googleapis.GoogleUtils;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.VisionScopes;
import com.google.api.services.vision.v1.model.*;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.security.GeneralSecurityException;
import java.util.ArrayList;

@Service
public class GoogleVisionImageService implements ImageService {

   private Logger logger = LoggerFactory.getLogger(getClass());

   @Autowired
   private HydroidConfiguration configuration;

   private float round(float value, int newScale) {
      BigDecimal bd = BigDecimal.valueOf(value);
      bd = bd.setScale(newScale, BigDecimal.ROUND_HALF_UP);
      return bd.floatValue();
   }

   @Override
   public ImageMetadata getImageMetadata(InputStream is) {
      if(is == null) {
         logger.debug("GoogleVisionImageService:getImageMetadata - input stream is null.");
         throw new HydroidException("input stream is null");
      }
      try {
         JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
         Vision.Builder builder;
         GoogleCredential credential = GoogleCredential.getApplicationDefault().createScoped(VisionScopes.all());

         builder = new Vision.Builder(newProxyTransport(configuration), jsonFactory, credential);

         String apiKey = configuration.getGoogleVisionApiKey();
         if(apiKey == null || apiKey.length() == 0) {
            throw new HydroidException("Google Vision API key missing from configuration. Check ${google.vision.apiKey}.");
         }
         builder.setVisionRequestInitializer(new
                 VisionRequestInitializer());
         Vision vision = builder.build();

         BatchAnnotateImagesRequest batchAnnotateImagesRequest =
                 new BatchAnnotateImagesRequest();
         batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
            AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

            Image base64EncodedImage = new Image();

            byte[] bytes = IOUtils.toByteArray(is);
            base64EncodedImage.encodeContent(bytes);
            annotateImageRequest.setImage(base64EncodedImage);

            // add the features we want
            annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
               Feature labelDetection = new Feature();
               labelDetection.setType("LABEL_DETECTION");
               labelDetection.setMaxResults(10);
               add(labelDetection);
            }});

            // Add the list of one thing to the request
            add(annotateImageRequest);
         }});

         Vision.Images.Annotate annotateRequest = vision.images().annotate(batchAnnotateImagesRequest);

         // Due to a bug: requests to Vision API containing large images fail when GZipped.
         annotateRequest.setDisableGZipContent(true);

         BatchAnnotateImagesResponse response;

         response = annotateRequest.execute();

         ImageMetadata result = new ImageMetadata();
         for (AnnotateImageResponse imgRes : response.getResponses()) {
            if (imgRes != null && imgRes.getLabelAnnotations() != null) {
               for (EntityAnnotation entityAnnotation : imgRes.getLabelAnnotations()) {
                  result.getImageLabels().add(
                        new ImageAnnotation(entityAnnotation.getDescription(), round(entityAnnotation.getScore(), 2))
                  );
               }
            }
         }
         return result;
      }
      catch (Exception e) {
         throw new HydroidException(e);
      }
   }

   @Override
   public ImageMetadata describeImage(InputStream is) {
      return null;
   }

   static HttpTransport newProxyTransport(HydroidConfiguration configuration) throws GeneralSecurityException, IOException {
      NetHttpTransport.Builder builder = new NetHttpTransport.Builder();
      builder.trustCertificates(GoogleUtils.getCertificateTrustStore());
      if(configuration.getProxyPort() > 0) {
         builder.setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(configuration.getProxyHost(), configuration.getProxyPort())));
      }
      return builder.build();
   }
}
