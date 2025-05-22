package handler;

import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import service.GameService;
import spark.Request;
import spark.Response;

public class ListHandler extends Handler {
    public final AuthDAO authDAO;
    public final GameDAO gameDAO;

    public ListHandler(AuthDAO authDAO, GameDAO gameDAO) {
        super(authDAO);
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    @Override
    public Object handleRequest(Request req, Response res) {
        Gson serializer = new Gson();
        try {
            // validate authToken
            String authToken = req.headers("authorization");
            Boolean valid = this.validateAuthToken(authToken);
            if (!valid) { // Ensure authToken is valid
                res.status(401);
                GameService.ListResult result = new GameService.ListResult(null, "Error: unauthorized");
                return serializer.toJson(result);
            }

            // list games
            GameService service = new GameService(gameDAO);
            GameService.ListResult result = service.list();

            // Return the body of the response
            res.type("application/json");
            return serializer.toJson(result);
        } catch (Exception e) {
            res.status(500);
            GameService.ListResult result = new GameService.ListResult(null, "Error: " + e.getMessage());
            return serializer.toJson(result);
        }
    }


}
