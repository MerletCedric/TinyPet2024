package servlet;

import entity.Petition;
import entity.Signataire;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.repackaged.com.google.gson.Gson;

@WebServlet(name = "SignatureServlet", urlPatterns = {"/signature"})
public class SignatureServlet extends HttpServlet {

    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Gson gson = new Gson();
        Signataire signataire = gson.fromJson(req.getReader(), Signataire.class);
        String userId = req.getParameter("userId");

        if (userId == null || signataire.getPetition() == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("User ID and Petition are required");
            return;
        }

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Key petitionKey = KeyFactory.createKey("Petition", signataire.getPetition().getTitle());

        TransactionOptions options = TransactionOptions.Builder.withXG(true);
        Transaction txn = datastore.beginTransaction(options);

        try {
            Entity petitionEntity = datastore.get(petitionKey);
            Signataire existingSignataire = entityToSignataire(petitionEntity);

            // Vérifier si l'utilisateur a déjà signé la pétition
            if (existingSignataire.getUserId(userId) != null) {
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
                resp.getWriter().write("User has already signed this petition");
                return;
            }

            // Ajouter l'utilisateur aux signataires
            existingSignataire.addToSignataire(userId);

            // Incrémenter le nombre de signatures
            int nbSignatures = existingSignataire.getNbSignatures();
            existingSignataire.setNbSignatures(nbSignatures + 1);

            // Sauvegarder les changements
            Entity updatedPetitionEntity = signataireToEntity(petitionKey, existingSignataire);
            datastore.put(updatedPetitionEntity);

            txn.commit();

            resp.setContentType("application/json");
            resp.getWriter().write(gson.toJson(existingSignataire));

        } catch (EntityNotFoundException e) {
            txn.rollback();
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Petition not found");
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    private Signataire entityToSignataire(Entity entity) {
        Signataire signataire = new Signataire();
        Petition petition = new Petition();

        petition.setTitle((String) entity.getProperty("title"));
        petition.setAutorId((String) entity.getProperty("autorId"));
        petition.setAutorName((String) entity.getProperty("autorName"));
        petition.setDescription((String) entity.getProperty("description"));
        petition.setDate((Date) entity.getProperty("date"));
        petition.setNbSignatures(((Long) entity.getProperty("nbSignatures")).intValue());

        signataire.setPetition(petition);
        signataire.setNbSignatures(((Long) entity.getProperty("nbSignatures")).intValue());
        signataire.setFree((Boolean) entity.getProperty("free"));
        signataire.setSignataires((List<String>) entity.getProperty("signataires"));

        return signataire;
    }

    private Entity signataireToEntity(Key key, Signataire signataire) {
        Entity entity = new Entity(key);
        Petition petition = signataire.getPetition();

        entity.setProperty("title", petition.getTitle());
        entity.setProperty("autorId", petition.getAutorId());
        entity.setProperty("autorName", petition.getAutorName());
        entity.setProperty("description", petition.getDescription());
        entity.setProperty("date", petition.getDate());
        entity.setProperty("nbSignatures", signataire.getNbSignatures());
        entity.setProperty("free", signataire.getFree());
        entity.setProperty("signataires", new ArrayList<>(signataire.getSignataires()));

        return entity;
    }
}