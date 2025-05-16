package handler;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import service.Result;
import spark.*;

abstract class Handler {
    private final AuthDAO authDAO;

    public Handler(AuthDAO authDAO) {
        this.authDAO = authDAO;
    }


    public abstract Object handleRequest(Request req, Response res);


    public Boolean validateAuthToken(String authToken) {
        try {
            authDAO.getAuth(authToken);
            return Boolean.TRUE;
        }
        catch (DataAccessException e) {
            return Boolean.FALSE;
        }
    }

    static void setStatusCode(Response res, Result result) {
        if (result.message() != null) {
            switch (result.message()) {
                case "Error: bad request" -> res.status(400);
                case "Error: unauthorized" -> res.status(401);
                case "Error: already taken" -> res.status(403);
                default -> res.status(500);
            }
        }
    }
}

