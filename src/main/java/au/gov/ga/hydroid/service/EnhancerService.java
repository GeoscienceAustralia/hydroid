package au.gov.ga.hydroid.service;

import au.gov.ga.hydroid.dto.DocumentDTO;

/**
 * Created by u24529 on 3/02/2016.
 */
public interface EnhancerService {

   public boolean enhance(DocumentDTO documentDTO);

   public void enhanceDocuments();
   public void enhanceDatasets();
   public void enhanceModels();
   public void enhanceImages();

    /**
     * enhances documents from cmi
     */
    public void enhanceCMINodes();

}
