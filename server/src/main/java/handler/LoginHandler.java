package handler;

import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.UserDAO;
import service.UserService;
import spark.Request;
import spark.Response;

public class LoginHandler extends Handler {
    public final AuthDAO authDAO;
    public final UserDAO userDAO;

    public LoginHandler(AuthDAO authDAO, UserDAO userDAO) {
        super(authDAO);
        this.authDAO = authDAO;
        this.userDAO = userDAO;
    }

    @Override
    public Object handleRequest(Request req, Response res) {
        Gson serializer = new Gson();
        try {
            // make object from request
            UserService.LoginRequest request = serializer.fromJson(req.body(), UserService.LoginRequest.class);

            // Check for missing username or password


            // login
            UserService service = new UserService(authDAO, userDAO);
            UserService.LoginResult result = service.login(request);

            // Set the status code
            setStatusCode(res, result);

            // Return the body of the response
            res.type("application/json");
            return serializer.toJson(result);
        } catch (Exception e) {
            res.status(500);
            String errorMessage = "Error: " + e.getMessage();
            UserService.LoginResult result = new UserService.LoginResult(null, null, errorMessage);
            return serializer.toJson(result);
        }
    }


}
