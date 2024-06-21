package servlet;

import entity.Signataire;

import java.io.IOException;
import java.util.Date;

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
        
        Filter userFilter = new FilterPredicate("idUser", FilterOperator.EQUAL, signataire.getUserId(userId));
        Filter petitionFilter = new FilterPredicate("idPetition", FilterOperator.EQUAL, signataire.getPetId());
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
            
            Long nbSignatures = (Long) petition.getProperty("nbSignature");
            petition.setProperty("nbSignature", (nbSignatures == null ? 0 : nbSignatures) + 1);
            
            Entity signatureEntity = new Entity("Signature");
            signatureEntity.setProperty("idPetition", signataire.getPetId());
            signatureEntity.setProperty("idUser", userId);
            signatureEntity.setProperty("date", new Date());
            
            datastore.put(petition);
            datastore.put(signatureEntity);
            
            txn.commit();
            
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(gson.toJson(signatureEntity));
        } catch (EntityNotFoundException e) {
            txn.rollback();
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(gson.toJson("Petition not found"));
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }
}
