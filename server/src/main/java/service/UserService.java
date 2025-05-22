package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;

public class UserService {
    public AuthDAO authDAO;
    public UserDAO userDAO;

    public UserService(AuthDAO authDAO, UserDAO userDAO) {
        this.authDAO = authDAO;
        this.userDAO = userDAO;
    }

    public record RegisterRequest(String username, String password, String email) {}
    public record RegisterResult(String username, String authToken, String message) implements Result {}

    public RegisterResult register(RegisterRequest registerRequest) {
        try {
            // ensure the request includes a username and a password
            if (registerRequest.username() == null || registerRequest.password() == null) {
                String errorMessage = "Error: bad request";
                return new RegisterResult(null, null, errorMessage);
            }

            // 1. get user
            String username = registerRequest.username();
            UserData userData = userDAO.getUser(username);
            // 2. if no user create new user
            if (userData == null) {
                userData = new UserData(registerRequest.username(),
                        registerRequest.password(),
                        registerRequest.email());
                userDAO.createUser(userData);
            } else {
                String errorMessage = "Error: already taken";
                return new RegisterResult(null, null, errorMessage);
            }
            // 3. create auth token
            AuthData authData = authDAO.createAuth(username);

            return new RegisterResult(authData.username(), authData.authToken(), null);
        } catch (DataAccessException e) {
            return new RegisterResult(null, null, e.getMessage());
        }
    }


    public record LoginRequest(String username, String password) {}
    public record LoginResult(String username, String authToken, String message) implements Result {}

    public LoginResult login(LoginRequest request) {
        String username = request.username();
        String password = request.password();

        // Check for null usernames or passwords
        if (username == null || password == null) {
            return new LoginResult(null, null, "Error: bad request");
        }

        try {
            // 1. getUser
            UserData userData = userDAO.getUser(username);
            if (userData == null) { // user not in database
                return new LoginResult(null, null, "Error: unauthorized");
            }
            // 2. verify password
            if (!userDAO.verifyPassword(username, password)) { // wrong password
                return new LoginResult(null, null, "Error: unauthorized");
            }
            // 3. create auth
            AuthData authData = authDAO.createAuth(username);
            return new LoginResult(authData.username(), authData.authToken(), null);
        } catch (DataAccessException e) {
            return new LoginResult(null, null, e.getMessage());
        }
    }

    public record LogoutRequest(String authToken) {}
    public record LogoutResult(String message) implements Result {}

    public LogoutResult logout(LogoutRequest request) {
        String authToken = request.authToken();

        // 1. delete Auth
        try {
            authDAO.deleteAuth(authToken);
            return new LogoutResult(null);
        }
        catch (DataAccessException e) {
            return new LogoutResult(e.getMessage());
        }
    }

}
