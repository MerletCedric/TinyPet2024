package entity;

import java.util.List;

public class Signataire {
    private Petition petition;
    private List<String> signataires;
    private boolean free;
    private int nbSignatures;
    
    public Petition getPetition() {
        return this.petition;
    }

    public void setPetition(Petition petition) {
        this.petition = petition;
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

    public List<String> getSignataires() {
        return signataires;
    }

    public void setSignataires(List<String> signataires) {
        this.signataires = signataires;
    }

    public void addSignataires(Object user) {
    }
}
