package entity;

import java.util.List;

public class Signataire {
    private String petId;
    private List<String> signataires;
    private boolean free;
    
    public String getPetId() {
        return petId;
    }
    public String getUserId(String userId) {
        return signataires.stream()
            .filter(signataire -> signataire.equals(userId))
            .findFirst()
            .orElse(null);
    }
}
