package servlet;

import entity.UserToken;
import endPoint.utility.GoogleAuthVerifier;
import entity.Petition;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.repackaged.com.google.gson.Gson;

@WebServlet(name = "PostPetitionServlet", urlPatterns = {"/postPetition"})
public class PostPetitionServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        Gson gson = new Gson();

        try {
            Map<String, Object> requestBody = gson.fromJson(req.getReader(), Map.class);
            Map<String, Object> petitionData = (Map<String, Object>) requestBody.get("petition");

            if (petitionData == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(gson.toJson("Invalid petition data."));
                return;
            }

            UserToken userToken = new UserToken();
            userToken.setToken((String) requestBody.get("userToken"));
            userToken.setUserId((String) petitionData.get("autorId"));

            Petition petition = new Petition();
            petition.setAutorName((String) petitionData.get("autorName"));
            petition.setAutorId((String) petitionData.get("autorId"));
            petition.setTitle((String) petitionData.get("title"));
            petition.setDescription((String) petitionData.get("description"));
            petition.setDate(new Date());
            petition.setNbSignatures(0);

            List<Object> result = GoogleAuthVerifier.verifyToken(userToken.getToken(), userToken.getUserId());
            boolean isVerified = (Boolean) result.get(0);
            if (!isVerified) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.getWriter().write(gson.toJson("Invalid user ID or token."));
                return;
            }

            Entity e = new Entity("Petition");
            e.setProperty("autorName", petition.getAutorName());
            e.setProperty("autorId", petition.getAutorId());
            e.setProperty("title", petition.getTitle().toLowerCase());
            e.setProperty("description", petition.getDescription());
            e.setProperty("nbSignatures", petition.getNbSignatures());
            e.setProperty("date",petition.getDate());

            DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
            Transaction txn = datastore.beginTransaction();
            datastore.put(e);
            txn.commit();

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(gson.toJson(e));
        } catch (Exception e) {
            e.printStackTrace();  // Log the full stack trace for debugging
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(gson.toJson("Error processing request: " + e.getMessage()));
        }
    }
}

