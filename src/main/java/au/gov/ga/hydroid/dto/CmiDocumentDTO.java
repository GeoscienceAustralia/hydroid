package au.gov.ga.hydroid.dto;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Representation of the CMI Json output. The variable name should match the keys in the JSON.
 * Look at toDocumentDTO() method in this class for the corresponding DocumentDTO variables.
 */
public class CmiDocumentDTO {
   private List<CmiSimpleJsonObject> title; // #TODO Dee should this be alternate title from json? "field_alternate_title"
   private String content;
   private List<CmiSimpleJsonObject> nid;
   private List<CmiSimpleJsonObject> field_principal_contributors;
   private List<CmiSimpleJsonObject> created;

    public List<CmiSimpleJsonObject> getTitle() {
        return this.title;
    }

    public void setTitle(List<CmiSimpleJsonObject> titles) {
        this.title = titles;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<CmiSimpleJsonObject> getNid() {
        return nid;
    }

    public void setNid(List<CmiSimpleJsonObject> nid) {
        this.nid = nid;
    }

    public List<CmiSimpleJsonObject> getAuthor() {
        return field_principal_contributors;
    }

    public void setAuthor(List<CmiSimpleJsonObject> author) {
        this.field_principal_contributors = author;
    }

    public List<CmiSimpleJsonObject> getDateCreated() {
        return created;
    }

    public void setDateCreated(List<CmiSimpleJsonObject> dateCreated) {
        this.created = dateCreated;
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

    public DocumentDTO toDocumentDTO() {
        DocumentDTO documentDTO = new DocumentDTO();
        documentDTO.setTitle(this.getCmiJsonObjectsAsString(this.getTitle()));
        documentDTO.setDateCreated(this.getObjectsAsDate(this.getDateCreated()));
        documentDTO.setAuthor(this.getCmiJsonObjectsAsString(this.getAuthor()));
        documentDTO.setOrigin("http://13.55.186.172/node/" + this.getCmiJsonObjectsAsString(this.getNid())); // #TODO Dee from config file - maybe cmi url in config and /node as const
        return documentDTO;
    }

    @Override
    public String toString() {
        return "CmiDocumentDTO{" +
                "title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", nid='" + nid + '\'' +
                ", author='" + field_principal_contributors + '\'' +
                ", created=" + created +
                '}';
    }

}
