package au.gov.ga.hydroid.dto;

import au.gov.ga.hydroid.model.DocumentType;
import au.gov.ga.hydroid.utils.IOUtils;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Representation of the CMI Json output for a particular node. The variable name matches the keys in JSON.
 * The toDocumentDTO() method converts it to corresponding DocumentDTO.
 */
public class CmiDocumentDTO {
   // raw json properties
   private List<CmiSimpleJsonObject> title;
   private List<CmiSimpleJsonObject> nid;
   private List<CmiAuthorJsonObject> field_data_sources;
   private List<CmiSimpleJsonObject> created;
   private String author = "";

   // property as needed by the application
   private int nodeId;

    public List<CmiSimpleJsonObject> getTitle() {
        return this.title;
    }

    public void setTitle(List<CmiSimpleJsonObject> title) {
        this.title = title;
    }

    public List<CmiSimpleJsonObject> getNid() {
        return nid;
    }

    public void setNid(List<CmiSimpleJsonObject> nid) {
        this.nid = nid;
    }

    public List<CmiSimpleJsonObject> getDateCreated() {
        return created;
    }

    public void setDateCreated(List<CmiSimpleJsonObject> dateCreated) {
        this.created = dateCreated;
    }

    public int getNodeId() {
        if (nodeId == 0 && nid != null) {
            nodeId = Integer.parseInt(getCmiJsonObjectsAsString(nid));
        }
        return nodeId;
    }

    private String getCmiJsonObjectsAsString(List<CmiSimpleJsonObject> objects) {
        StringBuffer result = new StringBuffer();
        if (objects != null && !objects.isEmpty()) {
            Iterator<CmiSimpleJsonObject> objIterator = objects.iterator();
            while (objIterator.hasNext()) {
                result.append(((CmiSimpleJsonObject)objIterator.next()).getValue());
                if (objIterator.hasNext()) {
                    result.append(",");
                }
            }
        }

        return result.toString();
    }

    /*
    *  Assuming there will be only on date in the array, break on the first date.
    */
    private Date getObjectsAsDate(List<CmiSimpleJsonObject> objects) {
        Date dateCreated = null;
        if (objects != null && !objects.isEmpty()) {
            Iterator<CmiSimpleJsonObject> objIterator = objects.iterator();
            while (objIterator.hasNext()) {
                dateCreated = new Date(new Long(((CmiSimpleJsonObject)objIterator.next()).getValue()));
                break;
            }
        }

        return dateCreated;
    }

    /* JSON path to parse author - field_data_sources[0].field_principal_contributors */
    private String parseAuthor() {
        if (this.field_data_sources != null && this.field_data_sources.size() > 0) {
            author = this.getCmiJsonObjectsAsString(((CmiAuthorJsonObject) field_data_sources.get(0)).getPrincipalContributors());
        }
        return author;
    }

    public DocumentDTO toDocumentDTO(String origin, String content) {
        DocumentDTO documentDTO = new DocumentDTO();
        documentDTO.setTitle(this.getCmiJsonObjectsAsString(this.getTitle()));
        documentDTO.setDateCreated(this.getObjectsAsDate(this.getDateCreated()));
        documentDTO.setAuthor(this.parseAuthor());

        documentDTO.setDocType(DocumentType.DOCUMENT.name());
        documentDTO.setOrigin(origin);
        documentDTO.setContent(content);
        documentDTO.setSha1Hash(IOUtils.getSha1Hash(content));

        return documentDTO;
    }

    @Override
    public String toString() {
        return "CmiDocumentDTO{" +
                "title='" + title + '\'' +
                ", nid='" + nid + '\'' +
                ", author='" + author + '\'' +
                ", created=" + created +
                '}';
    }

}
