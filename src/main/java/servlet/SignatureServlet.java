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
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        Gson gson = new Gson();

        try {
            Map<String, Object> requestBody = gson.fromJson(req.getReader(), Map.class);
            Map<String, Object> petitionData = (Map<String, Object>) requestBody.get("petition");
            String userId = (String) requestBody.get("userId");

            if (petitionData== null || userId == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(gson.toJson("Missing petitionId or userId."));
                return;
            }

            Key petitionKey = KeyFactory.createKey("Petition", Long.parseLong(petitionData.get("id").toString()));
            DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
            Entity petitionEntity;
            try {
                petitionEntity = datastore.get(petitionKey);
            } catch (EntityNotFoundException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(gson.toJson("Petition not exists in datastore"));
                return;
            }
            // Vérification de l'existence du signataire dans le Datastore
            Key signataireKey = KeyFactory.createKey("Signataire", userId);
            Entity signataireEntity;
            try {
                signataireEntity = datastore.get(signataireKey);
            } catch (EntityNotFoundException e) {
                signataireEntity = new Entity("Signataire", userId);
                signataireEntity.setProperty("signataires", new ArrayList<String>());
                signataireEntity.setProperty("free", true);
                signataireEntity.setProperty("nbSignatures", 0);
            }

            List<String> signataires = (List<String>) signataireEntity.getProperty("signataires");
            boolean free = (Boolean) signataireEntity.getProperty("free");
            int nbSignatures = (int) signataireEntity.getProperty("nbSignatures"); 

            // Vérification si la liste des signataires est pleine
            if (signataires != null && signataires.size() >= 40000) {
                free = false; // Mettre à jour free à false si la liste est pleine
            } else {
                // Ajout de l'userId aux signataires si non présent
                if (!signataires.contains(userId)) {
                    signataires.add(userId);
                    nbSignatures++; // Incrément de nbSignatures
                } else {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write(gson.toJson("Already signed"));
                    return;
                }
            }

            // Mise à jour de l'entité Signataire
            signataireEntity.setProperty("signataires", signataires);
            signataireEntity.setProperty("free", free);
            signataireEntity.setProperty("nbSignatures", nbSignatures);

            petitionEntity.setProperty("nbSignatures", nbSignatures);

            // Transaction pour enregistrer les modifications dans le Datastore
            Transaction txn = datastore.beginTransaction();
            datastore.put(signataireEntity);
            datastore.put(petitionEntity);
            txn.commit();

            // Réponse de succès
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(gson.toJson("Signature added successfully."));
            
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(gson.toJson("Error processing request: " + e.getMessage()));
        }

    }
}
