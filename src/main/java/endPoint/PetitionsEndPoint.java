package endPoint;

import entity.UserToken;
import endPoint.utility.GoogleAuthVerifier;
import entity.Petition;
import entity.Signataire;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.response.InternalServerErrorException;
import com.google.api.server.spi.response.UnauthorizedException;
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
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.users.User;

@Api(name = "myApi",
     version = "v1",
     audiences = "113524796838-sn3sk232hsqa0p04r1opnpbb1htkbqrj.apps.googleusercontent.com",
  	 clientIds = {"113524796838-sn3sk232hsqa0p04r1opnpbb1htkbqrj.apps.googleusercontent.com"},
     namespace =
     @ApiNamespace(
		   ownerDomain = "helloworld.example.com",
		   ownerName = "helloworld.example.com",
		   packagePath = "")
     )
public class PetitionsEndPoint {
	@ApiMethod(name = "postPetition", httpMethod = HttpMethod.POST)
	public Entity postPetition(Map<String, Object> requestBody) throws UnauthorizedException {
		UserToken userToken = (UserToken) requestBody.get("userToken");
		Petition petition = new Petition();
		petition.setAutorId((String) requestBody.get("autorId"));
		petition.setTitle((String) requestBody.get("title"));
		petition.setDescription((String) requestBody.get("description"));

        List<Object> result = GoogleAuthVerifier.verifyToken(userToken.getToken(), userToken.getUserId());
        
        boolean isVerified = (Boolean) result.get(0);
        if (!isVerified) {
            throw new UnauthorizedException("Invalid user ID or token.");
        }

		Entity e = new Entity("Petition");
		e.setProperty("autor", petition.getAutorName());
		e.setProperty("title", petition.getTitle().toLowerCase());
		e.setProperty("description", petition.getDescription());
		e.setProperty("nbSignature", 0);
		e.setProperty("date", new Date());

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Transaction txn = datastore.beginTransaction();
		datastore.put(e);
		txn.commit();
		return e;
	}

    @ApiMethod(name = "getPetitions", httpMethod = HttpMethod.GET)
    public List<Entity> getPetitions() throws InternalServerErrorException {
		try {
			Query q = new Query("Petition").addSort("date", SortDirection.DESCENDING);
			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
			PreparedQuery pq = datastore.prepare(q);
			List<Entity> result = pq.asList(FetchOptions.Builder.withLimit(100));
			return result;
		} catch (Exception e) {
			// Log the exception for debugging purposes
			e.printStackTrace();
			throw new InternalServerErrorException("Error fetching petitions from Datastore");
		}
    }

	@ApiMethod(name = "signature", httpMethod = HttpMethod.POST)
	public Entity signature(String userId, Signataire signataire) throws Exception {
		System.out.print("test" + signataire.getPetId());
		if (userId == null || signataire.getPetId() == null) {
			throw new IllegalArgumentException("User ID and Petition ID are required");
		}
	
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Key petitionKey = KeyFactory.createKey("Petition", signataire.getPetId());
	
		// Vérifier si l'utilisateur a déjà signé la pétition
		Filter userFilter = new FilterPredicate("idUser", FilterOperator.EQUAL, signataire.getUserId(userId));
		Filter petitionFilter = new FilterPredicate("idPetition", FilterOperator.EQUAL, signataire.getPetId());
		Filter signatureFilter = CompositeFilterOperator.and(petitionFilter, userFilter);
	
		Query query = new Query("Signature").setFilter(signatureFilter);
		PreparedQuery pq = datastore.prepare(query);
		if (pq.countEntities(FetchOptions.Builder.withLimit(1)) > 0) {
			throw new IllegalStateException("User has already signed this petition");
		}
	
		TransactionOptions options = TransactionOptions.Builder.withXG(true);
		Transaction txn = datastore.beginTransaction(options);
	
		try {
			Entity petition = datastore.get(petitionKey);
	
			// Incrémenter le nombre de signatures
			Long nbSignatures = (Long) petition.getProperty("nbSignature");
			petition.setProperty("nbSignature", (nbSignatures == null ? 0 : nbSignatures) + 1);
	
			// Créer une nouvelle entité pour la signature
			Entity signatureEntity = new Entity("Signature");
			signatureEntity.setProperty("idPetition", signataire.getPetId());
			signatureEntity.setProperty("idUser", userId);
			signatureEntity.setProperty("date", new Date());
	
			// Sauvegarder les deux entités
			datastore.put(petition);
			datastore.put(signatureEntity);
	
			txn.commit();
	
			return signatureEntity;
	
		} catch (EntityNotFoundException e) {
			txn.rollback();
			throw new IllegalArgumentException("Petition not found");
		} finally {
			if (txn.isActive()) {
				txn.rollback();
			}
		}
	}
}
