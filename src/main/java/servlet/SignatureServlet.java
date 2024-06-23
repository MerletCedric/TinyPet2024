package servlet;

import entity.Petition;
import entity.Signataire;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Type;
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
import com.google.appengine.repackaged.com.google.gson.*;
import com.google.common.reflect.TypeToken;

@WebServlet(name = "SignatureServlet", urlPatterns = {"/signature"})
public class SignatureServlet extends HttpServlet {

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(new TypeToken<Map<String, Object>>() {}.getType(), new JsonDeserializer<Map<String, Object>>() {
                @Override
                public Map<String, Object> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                    // Force all values to be treated as strings
                    JsonObject jsonObject = json.getAsJsonObject();
                    for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                        if (entry.getValue().isJsonPrimitive() && entry.getValue().getAsJsonPrimitive().isNumber()) {
                            entry.setValue(new JsonPrimitive(entry.getValue().getAsString()));
                        }
                    }
                    return new Gson().fromJson(jsonObject, typeOfT);
                }
            })
            .create();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Read JSON from request body
        BufferedReader reader = req.getReader();
        StringBuilder jsonBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            jsonBuilder.append(line);
        }
        String jsonString = jsonBuilder.toString();

        // Use TypeToken to specify the generic types
        Type type = new TypeToken<Map<String, Object>>() {}.getType();
        Map<String, Object> requestBody = gson.fromJson(jsonString, type);

        // Extract petitionId and userId as Strings
        String petitionId = (String) requestBody.get("petitionId");
        String userId = (String) requestBody.get("userId");

        if (petitionId == null || userId == null) {
            resp.getWriter().write("Missing parameters");
            return;
        }
        
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        try {
            Filter petitionFilter = new FilterPredicate("petId", FilterOperator.EQUAL, petitionId);
            Query query = new Query("Signataire").setFilter(petitionFilter);
            PreparedQuery pq = datastore.prepare(query);

            // List<Entity> testEntities = pq.asList(FetchOptions.Builder.withLimit(1));
            // if(!testEntities.isEmpty()) {
            //     resp.setStatus(HttpServletResponse.SC_OK);
            //     resp.getWriter().write(gson.toJson("Trouvé !"));
            //     return;
            // }

            boolean userIdFound = false;
            for (Entity entity : pq.asIterable()) {
                List<String> signataires = (List<String>) entity.getProperty("signataires");
                if (signataires != null && signataires.contains(userId)) {
                    userIdFound = true;
                    break;
                }
            }

            if (userIdFound) {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write(gson.toJson("Pétition déjà signée"));
                return;
            } else {
                TransactionOptions options = TransactionOptions.Builder.withXG(true);
		        Transaction txn = datastore.beginTransaction(options);
                try {
                    Entity signataireEntity;
                    Entity petitionEntity;
                    Signataire signataire;
                    Long nbSignatures;

                    Key petitionKey = KeyFactory.createKey("Petition", Long.parseLong(petitionId));
                    petitionEntity = datastore.get(petitionKey);

                    Filter freeFilter = new FilterPredicate("free", FilterOperator.EQUAL, true);
                    Filter isSignatairesListFree = CompositeFilterOperator.and(petitionFilter, freeFilter);
                    Query getSignataireQuery = new Query("Signataire").setFilter(isSignatairesListFree);
                    pq = datastore.prepare(getSignataireQuery);
                    List<Entity> signataireEntities = pq.asList(FetchOptions.Builder.withLimit(1));
                    if(!signataireEntities.isEmpty()) {
                        signataireEntity = signataireEntities.get(0);
                        signataire = new Signataire();
                        signataire.setPetId((String) signataireEntity.getProperty("petitionKey"));
                        signataire.setSignataires((List<String>) signataireEntity.getProperty("signataires"));
                        signataire.setFree((boolean) signataireEntity.getProperty("free"));
                        signataire.setNbSignatures((Long) signataireEntity.getProperty("nbSignatures"));

                        nbSignatures = (Long) petitionEntity.getProperty("nbSignature");
                        petitionEntity.setProperty("nbSignature", (nbSignatures == null ? 0 : nbSignatures) + 1);
                        signataireEntity.setProperty("nbSignature", (nbSignatures == null ? 0 : nbSignatures) + 1);
                        
                        signataire.addSignataires(userId);
                        signataireEntity.setProperty("signataires", signataire.getSignataires());
                        if (signataire.getSignataires().size() >= 40000) {
                            signataireEntity.setProperty("free", false);
                        }
                    } else {
                        // Récupérer l'entité Petition pour la mettre à jour
                        nbSignatures = (Long) petitionEntity.getProperty("nbSignatures");

                        // Si aucune entité Signataire n'est trouvée, en créer une nouvelle (arrive la première fois)
                        signataireEntity = new Entity("Signataire");
                        signataire = new Signataire();
                        signataire.setPetId(petitionId);
                        signataire.addSignataires(userId);
                        signataire.setFree(true);

                        // Configurer l'entité Signataire avec les propriétés appropriées
                        signataireEntity.setProperty("petId", petitionId);
                        signataireEntity.setProperty("signataires", signataire.getSignataires());
                        signataireEntity.setProperty("free", signataire.getFree());
                        signataireEntity.setProperty("nbSignatures", (nbSignatures == null ? 0 : nbSignatures) + 1);

                        petitionEntity.setProperty("nbSignatures", (nbSignatures == null ? 0 : nbSignatures) + 1);
                    }
                    // Sauvegarder les deux entités
                    datastore.put(petitionEntity);
                    datastore.put(signataireEntity);
            
                    txn.commit();
            
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getWriter().write(gson.toJson(signataireEntity));
                } catch (Exception e) {
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    resp.getWriter().write(gson.toJson("Error fetching signataires from Datastore: " + e.getMessage()));
                } finally {
                    if (txn.isActive()) {
                        txn.rollback();
                    }
                }
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(gson.toJson("Error fetching - is already sign - Signataire from Datastore: " + e.getMessage()));
        }
    }
}
