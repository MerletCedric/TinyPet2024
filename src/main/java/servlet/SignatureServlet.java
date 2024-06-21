package servlet;

import entity.Signataire;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

        String userId = req.getParameter("userId");
        Signataire signataire = gson.fromJson(req.getReader(), Signataire.class);

        if (userId == null || signataire.getPetId() == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(gson.toJson("User ID and Petition ID are required"));
            return;
        }

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Key petitionKey = KeyFactory.createKey("Petition", signataire.getPetId());

        Filter userFilter = new FilterPredicate("userId", FilterOperator.EQUAL, userId);
        Filter petitionFilter = new FilterPredicate("petitionId", FilterOperator.EQUAL, signataire.getPetId());
        Filter signatureFilter = CompositeFilterOperator.and(petitionFilter, userFilter);

        Query query = new Query("Signature").setFilter(signatureFilter);
        PreparedQuery pq = datastore.prepare(query);
        if (pq.countEntities(FetchOptions.Builder.withLimit(1)) > 0) {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            resp.getWriter().write(gson.toJson("User has already signed this petition"));
            return;
        }

        TransactionOptions options = TransactionOptions.Builder.withXG(true);
        Transaction txn = datastore.beginTransaction(options);

        try {
            Entity petition = datastore.get(petitionKey);
            long nbSignatures = (long) petition.getProperty("nbSignatures");
            nbSignatures += 1;
            petition.setProperty("nbSignatures", nbSignatures);

            Query signataireQuery = new Query("Signataire")
                    .setFilter(new FilterPredicate("petitionId", FilterOperator.EQUAL, signataire.getPetId()))
                    .addSort("index", Query.SortDirection.DESCENDING);
            PreparedQuery signatairePQ = datastore.prepare(signataireQuery);
            Entity lastSignataireEntity = signatairePQ.asSingleEntity();

            List<String> signataires = null;
            if (lastSignataireEntity != null) {
                Object signatairesObj = lastSignataireEntity.getProperty("signataires");
                if (signatairesObj instanceof List<?>) {
                    signataires = (List<String>) signatairesObj;
                }
            }

            if (signataires == null || signataires.size() >= 40000) {
                Entity newSignataireEntity = new Entity("Signataire");
                newSignataireEntity.setProperty("petitionId", signataire.getPetId());
                newSignataireEntity.setProperty("signataires", new ArrayList<>(Arrays.asList(userId)));
                newSignataireEntity.setProperty("nbSignatures", 1);
                newSignataireEntity.setProperty("index", lastSignataireEntity == null ? 1 : (long) lastSignataireEntity.getProperty("index") + 1);
                newSignataireEntity.setProperty("free", signataires == null || signataires.size() < 40000);
                datastore.put(newSignataireEntity);
            } else {
                signataires.add(userId);
                lastSignataireEntity.setProperty("signataires", signataires);
                lastSignataireEntity.setProperty("nbSignatures", signataires.size());
                lastSignataireEntity.setProperty("free", signataires.size() < 40000);
                datastore.put(lastSignataireEntity);
            }

            datastore.put(petition);

            txn.commit();

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(gson.toJson("Petition signed successfully"));
        } catch (EntityNotFoundException e) {
            txn.rollback();
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(gson.toJson("Petition not found"));
        } catch (Exception e) {
            txn.rollback();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(gson.toJson("Error processing request: " + e.getMessage()));
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }
}
