@Setter 
@Getter
@NoArgsConstructor
public class User {
    private int ID;
    private String fullName;
    private String firstName;
    private String lastName;
    private String email;

    public User(String fullName, String email, String ID) {
        this.fullName = fullName;
        this.email = email;
        this.ID = ID;
    }
}