package servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.repackaged.com.google.gson.Gson;

// Exemple simplifié pour inclure l'auteur dans la réponse JSON

@WebServlet(name = "GetPetitionsServlet", urlPatterns = {"/getPetitions"})
public class GetPetitionsServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        Gson gson = new Gson();

        String selection = req.getParameter("selection");
        String userId = req.getParameter("userId");

        if ("topHundred".equals(selection)) {
            try {
                Query q = new Query("Petition")
                    .addSort("date", Query.SortDirection.DESCENDING)
                    .addSort("nbSignatures",  Query.SortDirection.DESCENDING);
                DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
                PreparedQuery pq = datastore.prepare(q);
                List<Entity> result = pq.asList(FetchOptions.Builder.withLimit(100));
    
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write(gson.toJson(result));
            } catch (Exception e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write(gson.toJson("Error fetching petitions from Datastore: " + e.getMessage()));
            }
        } else {
            try {
                Filter autorIdFilter = new FilterPredicate("autorId", FilterOperator.EQUAL, userId);
                Query getSignataireQuery = new Query("Petition").setFilter(autorIdFilter).addSort("date",  Query.SortDirection.DESCENDING);
                DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
                PreparedQuery pq = datastore.prepare(getSignataireQuery);
                List<Entity> result = pq.asList(FetchOptions.Builder.withLimit(100));
    
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write(gson.toJson(result));
            } catch (Exception e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write(gson.toJson("Error fetching petitions from Datastore: " + e.getMessage()));
            }
        }
    }
}