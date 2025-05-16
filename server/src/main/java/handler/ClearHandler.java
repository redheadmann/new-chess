package handler;

import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import service.ClearService;
import spark.Request;
import spark.Response;

public class ClearHandler extends Handler {
    public final GameDAO gameDAO;
    public final UserDAO userDAO;
    public final AuthDAO authDAO;

    public ClearHandler(AuthDAO authDAO, UserDAO userDAO, GameDAO gameDAO) {
        super(authDAO);
        this.gameDAO = gameDAO;
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    @Override
    public Object handleRequest(Request req, Response res) {
        Gson serializer = new Gson();

        ClearService service = new ClearService(gameDAO, userDAO, authDAO);
        ClearService.ClearResult result = service.clear();

        if (result.message() != null) {
            res.status(500);
        }

        res.type("application/json");
        return serializer.toJson(result);
    }
}
