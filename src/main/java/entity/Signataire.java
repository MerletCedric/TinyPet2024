package entity;

import java.util.List;

public class Signataire {
    private String petId;
    private List<String> signataires;
    private boolean free;
    private int nbSignatures;
    
    public String getPetId() {
        return petId;
    }

    public String getUserId(String userId) {
        return signataires.stream()
            .filter(signataire -> signataire.equals(userId))
            .findFirst()
            .orElse(null);
    }

    public void addToSignataire(String userId) {
        this.signataires.add(userId);
    }

    public int getNbSignatures() {
        return this.nbSignatures;
    }

    public void setNbSignatures(int nbSignatures) {
        this.nbSignatures = nbSignatures;
    }

    public boolean getFree() {
        return this.free;
    }

    public void setFree(boolean free) {
        this.free = free;
    }
}
