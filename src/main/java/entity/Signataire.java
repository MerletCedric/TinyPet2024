package entity;

import java.util.List;

public class Signataire {
    private String petId;
    private List<Long> signataires;
    private boolean free;
    private int nbSignatures;
    
    public String getPetId() {
        return this.petId;
    }

    public void setPetId(String petitionId) {
        this.petId = petitionId;
    }

    public void addToSignataire(Long userId) {
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

    public List<Long> getSignataires() {
        return signataires;
    }

    public void setSignataires(List<Long> signataires) {
        this.signataires = signataires;
    }

    public void addSignataires(Object user) {
    }
}
