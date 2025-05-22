package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import dataaccess.UserDAO;

public class ClearService {
    private final GameDAO gameDAO;
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public ClearService(GameDAO gameDAO, UserDAO userDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public record ClearResult(String message) implements Result {}

    public ClearResult clear() {
        try {
            gameDAO.clear();
            userDAO.clear();
            authDAO.clear();
            return new ClearResult(null);
        } catch (Error | DataAccessException e) {
            return new ClearResult("Error: " + e.getMessage());
        }
    }


}
