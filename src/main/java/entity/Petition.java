package entity;

import java.util.Date;

public class Petition {
   private String title;
   private String autorId;
   private String autorName;
   private String description;
   private Date date; 
   private int nbSignatures;
   
   public String getTitle() {
      return this.title;
   }
   
   public String getDescription() {
    return this.description;
   }

   public String getAutorName() {
      return this.autorName;
   }

   public String getAutorId() {
      return this.autorId;
   }

   public void setAutorId(String autorId) {
      this.autorId = autorId;
   }

   public void setTitle(String title) {
      this.title = title;
   }

   public void setDescription(String description) {
      this.description = description;
   }

   public void setAutorName(String autorName) {
      this.autorName = autorName;
   }

   public Date getDate() {
      return this.date;
   }

   public void setDate(Date date) {
      this.date = date;
   }

   public int getNbSignatures() {
      return this.nbSignatures;
   }

   public void setNbSignatures(int nbSignatures) {
      this.nbSignatures = nbSignatures;
   }
}
