package entity;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import scorex.util.ArrayList;

public class Petition {
   private String name;
   private String date;
   private List<User> users;
   private Boolean isSigned;
   private int countSign = 0;

   public Petition (String name) {
    this.name = name;
    this.users = new ArrayList<User>();
    this.date = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
   }

   public String getName() {
      return this.name;
   }

   public String getDate() {
      return this.date;
   }

   public List<User> getUsers() {
      return this.users;
   }

   public Boolean getIsSigned() {
      return this.isSigned;
   }

   public int getCountSign () {
      return this.countSign;
   }

   public void setIsSigned(Boolean signed) {
      this.isSigned = signed;
   }
}
