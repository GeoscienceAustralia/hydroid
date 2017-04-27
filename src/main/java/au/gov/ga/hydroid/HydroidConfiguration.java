package au.gov.ga.hydroid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Created by u24529 on 9/02/2016.
 */
@Configuration
public class HydroidConfiguration {

   @Value("${proxy.host}")
   private String proxyHost;

   @Value("${proxy.port}")
   private int proxyPort;

   @Value("${stanbol.chain}")
   private String stanbolChain;

   @Value("${stanbol.url}")
   private String stanbolUrl;

   @Value("${solr.collection}")
   private String solrCollection;

   @Value("${solr.url}")
   private String solrUrl;

   @Value("${s3.bucket}")
   private String s3Bucket;

   @Value("${s3.enhancer.input}")
   private String s3EnhancerInput;

   @Value("${s3.enhancer.output}")
   private String s3EnhancerOutput;

   @Value("${s3.enhancer.output.images}")
   private String s3EnhancerOutputImages;

   @Value("${fuseki.url}")
   private String fusekiUrl;

   @Value("${google.vision.apiKey}")
   private String googleVisionApiKey;

   @Value("${store.ga.vocabs.only}")
   private boolean storeGAVocabsOnly;

   @Value("${s3.output.bucket}")
   private String s3OutputBucket;

   @Value("${s3.output.url}")
   private String s3OutputUrl;

   @Value("${hydroid.pid.path}")
   private String hydroidPidPath;

   @Value ("${cmi.base.url}")
   private String cmiBaseUrl;

   @Value("${cmi.summary.endpoint}")
   private String cmiSummaryEndpoint;

   @Value("${cmi.node.endpoint}")
   private String cmiNodeEndpoint;

   public String getProxyHost() {
      return proxyHost;
   }

   public int getProxyPort() {
      return proxyPort;
   }

   public String getStanbolChain() {
      return stanbolChain;
   }

   public String getStanbolUrl() {
      return stanbolUrl;
   }

   public String getSolrCollection() {
      return solrCollection;
   }

   public String getSolrUrl() {
      return solrUrl;
   }

   public String getS3Bucket() {
      return s3Bucket;
   }

   public String getS3EnhancerInput() {
      return s3EnhancerInput;
   }

   public String getS3EnhancerOutput() {
      return s3EnhancerOutput;
   }

   public String getS3EnhancerOutputImages() {
      return s3EnhancerOutputImages;
   }

   public String getFusekiUrl() { return fusekiUrl; }

   public String getGoogleVisionApiKey() {return googleVisionApiKey;}

   public boolean isStoreGAVocabsOnly() {
      return storeGAVocabsOnly;
   }

   public String getS3OutputBucket() {
      return s3OutputBucket;
   }

   public String getS3OutputUrl() {
      return s3OutputUrl;
   }

   public String getHydroidPidPath() {
      return hydroidPidPath;
   }

   public String getCmiBaseUrl() { return cmiBaseUrl; }

   public String getCmiSummaryEndpoint() { return cmiSummaryEndpoint; }

   public String getCmiNodeEndpoint() { return cmiNodeEndpoint; }
}
