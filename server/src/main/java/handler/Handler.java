package handler;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import service.Result;
import spark.*;

import java.util.Objects;

abstract class Handler {
    private final AuthDAO authDAO;

    public Handler(AuthDAO authDAO) {
        this.authDAO = authDAO;
    }


    public abstract Object handleRequest(Request req, Response res);


    public Boolean validateAuthToken(String authToken) throws DataAccessException {
        try {
            authDAO.getAuth(authToken);
            return Boolean.TRUE;
        } catch (DataAccessException e) {
            if (Objects.equals(e.getMessage(), "Error: unauthorized")) {
                return Boolean.FALSE;
            }
            throw e;
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

