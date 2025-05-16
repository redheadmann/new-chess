package handler;

import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.UserDAO;
import service.UserService;
import spark.Request;
import spark.Response;

public class LogoutHandler extends Handler {
    public final AuthDAO authDAO;
    public final UserDAO userDAO;

    public LogoutHandler(AuthDAO authDAO, UserDAO userDAO) {
        super(authDAO);
        this.authDAO = authDAO;
        this.userDAO = userDAO;
    }

    @Override
    public Object handleRequest(Request req, Response res) {
        Gson serializer = new Gson();
        try {
            // make object from request
            String authToken = req.headers("authorization");
            UserService.LogoutRequest request = new UserService.LogoutRequest(authToken);

            // logout
            UserService service = new UserService(authDAO, userDAO);
            UserService.LogoutResult result = service.logout(request);

            // Set the status code
            setStatusCode(res, result);

            // Return the body of the response
            res.type("application/json");
            return serializer.toJson(result);
        } catch (Exception e) {
            res.status(500);
            String errorMessage = "Error: " + e.getMessage();
            UserService.LogoutResult result = new UserService.LogoutResult(errorMessage);
            return serializer.toJson(result);
        }
    }

}
