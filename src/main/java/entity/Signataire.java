package entity;

import java.util.List;

public class Signataire {
    private String petId;
    private List<String> signataires;
    private boolean free;
    private Long nbSignatures;
    
    public String getPetId() {
        return this.petId;
    }

    public void setPetId(String petitionId) {
        this.petId = petitionId;
    }

    public void addToSignataire(String userId) {
        this.signataires.add(userId);
    }

    public Long getNbSignatures() {
        return this.nbSignatures;
    }

    public void setNbSignatures(Long nbSignatures) {
        this.nbSignatures = nbSignatures;
    }

    public boolean getFree() {
        return this.free;
    }

    public void setFree(boolean free) {
        this.free = free;
    }

    public List<String> getSignataires() {
        return signataires;
    }

    public void setSignataires(List<String> signataires) {
        this.signataires = signataires;
    }

    public void addSignataires(Object user) {
    }
}
