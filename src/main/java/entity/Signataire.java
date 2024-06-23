package entity;

import java.util.ArrayList;
import java.util.List;

public class Signataire {
    private String petId;
    private List<String> signataires;
    private boolean free;
    private Long nbSignatures;

    public Signataire() {
        this.signataires = new ArrayList<String>();
    }
    
    public String getPetId() {
        return this.petId;
    }

    public void setPetId(String petitionId) {
        this.petId = petitionId;
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
        return this.signataires;
    }

    public void setSignataires(List<String> signataires) {
        this.signataires = signataires;
    }

    public void addSignataires(String userId) {
        this.signataires.add(userId);
    }
}
