package au.gov.ga.hydroid.dto;

import au.gov.ga.hydroid.model.DocumentType;

import java.util.Date;

/**
 * Created by u24529 on 8/02/2016.
 */
public class DocumentDTO {

   private String title;
   private String content;
   private String docType = DocumentType.DOCUMENT.name();
   private String origin;
   private String author;
   private Date dateCreated;
   private String sha1Hash;

   public String getTitle() {
      return title;
   }

   public void setTitle(String title) {
      this.title = title;
   }

   public String getContent() {
      return content;
   }

   public void setContent(String content) {
      this.content = content;
   }

   public String getDocType() {
      return docType;
   }

   public void setDocType(String docType) {
      this.docType = docType;
   }

   public String getOrigin() {
      return origin;
   }

   public void setOrigin(String origin) {
      this.origin = origin;
   }

   public String getAuthor() {
      return author;
   }

   public void setAuthor(String author) {
      this.author = author;
   }

   public Date getDateCreated() {
      return dateCreated;
   }

   public void setDateCreated(Date dateCreated) {
      this.dateCreated = dateCreated;
   }

   public String getSha1Hash() {
      return sha1Hash;
   }

   public void setSha1Hash(String sha1Hash) {
      this.sha1Hash = sha1Hash;
   }

    @Override
    public String toString() {
        return "DocumentDTO {" +
                "title ='" + title + '\'' +
                ", content ='" + content + '\'' +
                ", docType ='" + docType + '\'' +
                ", origin ='" + origin + '\'' +
                ", author ='" + author + '\'' +
                ", dateCreated =" + dateCreated +
                ", sha1Hash ='" + sha1Hash + '\'' +
                '}';
    }
}
