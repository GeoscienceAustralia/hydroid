package au.gov.ga.hydroid.job;

import au.gov.ga.hydroid.service.EnhancerService;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.util.Date;

/**
 * Created by u24529 on 1/03/2016.
 */
@Component
@DisallowConcurrentExecution
public class EnhancerJob implements Job {

   private static final Logger logger = LoggerFactory.getLogger(EnhancerJob.class);

   @Autowired
   private EnhancerService enhancerService;

   @Override
   public void execute(JobExecutionContext jobExecutionContext)  {
      Instant started = Instant.now();
      logger.info("execute started at..: " + started.toString());
      enhancerService.enhanceDocuments();
      enhancerService.enhanceDatasets();
      enhancerService.enhanceModels();
      enhancerService.enhanceImages();
       enhancerService.enhanceCMINodes();
      Instant finished = Instant.now();
      logger.info("execute finished at.: " + finished);
      logger.info("execute elapsed time: " + Duration.between(started, finished));
   }

}
