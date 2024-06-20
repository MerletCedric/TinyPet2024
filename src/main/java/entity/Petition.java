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

   public Object getAutorName() {
      return this.autorName;
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
}
