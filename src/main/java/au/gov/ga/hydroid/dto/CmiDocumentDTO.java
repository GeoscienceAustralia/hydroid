package au.gov.ga.hydroid.dto;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Representation of the CMI Json output. The variable name should match the keys in the JSON.
 * Look at toDocumentDTO() method in this class for the corresponding DocumentDTO variables.
 */
public class CmiDocumentDTO {
   // raw json properties
   private List<CmiSimpleJsonObject> title; // #TODO Dee should this be "field_alternate_title"
   private List<CmiSimpleJsonObject> nid;
   private List<CmiSimpleJsonObject> field_principal_contributors;
   private List<CmiSimpleJsonObject> created;

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

    public List<CmiSimpleJsonObject> getPrincipalContributor() {
        return field_principal_contributors;
    }

    public void setPrincipalContributor(List<CmiSimpleJsonObject> author) {
        this.field_principal_contributors = author;
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

    private String parseAuthor() {
        String author = null;
        author = this.getCmiJsonObjectsAsString(this.getPrincipalContributor()); // TODO Dee parse author name from node endpoint
        return author;
    }

    public DocumentDTO toDocumentDTO() {
        DocumentDTO documentDTO = new DocumentDTO();
        documentDTO.setTitle(this.getCmiJsonObjectsAsString(this.getTitle()));
        documentDTO.setDateCreated(this.getObjectsAsDate(this.getDateCreated()));
        documentDTO.setAuthor(this.parseAuthor());
        return documentDTO;
    }

    @Override
    public String toString() {
        return "CmiDocumentDTO{" +
                "title='" + title + '\'' +
                ", nid='" + nid + '\'' +
                ", author='" + field_principal_contributors + '\'' +
                ", created=" + created +
                '}';
    }

}
