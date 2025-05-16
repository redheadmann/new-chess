package handler;

import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import service.GameService;
import spark.Request;
import spark.Response;

public class CreateHandler extends Handler {
    public final AuthDAO authDAO;
    public final GameDAO gameDAO;

    public CreateHandler(AuthDAO authDAO, GameDAO gameDAO) {
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
                GameService.CreateResult result = new GameService.CreateResult(null, "Error: unauthorized");
                return serializer.toJson(result);
            }

            // make object from request
            GameService.CreateRequest request = serializer.fromJson(req.body(), GameService.CreateRequest.class);

            // create game
            GameService service = new GameService(gameDAO);
            GameService.CreateResult result = service.createGame(request);

            // Set the status code
            setStatusCode(res, result);

            // Return the body of the response
            res.type("application/json");
            return serializer.toJson(result);
        } catch (Exception e) {
            res.status(500);
            String errorMessage = "Error: " + e.getMessage();
            GameService.CreateResult result = new GameService.CreateResult(null, errorMessage);
            return serializer.toJson(result);
        }
    }
}
