package handler;

import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.UserDAO;
import service.UserService;
import spark.Request;
import spark.Response;


public class RegisterHandler extends Handler {
    public final UserDAO userDAO;
    public final AuthDAO authDAO;

    public RegisterHandler(AuthDAO authDAO, UserDAO userDAO) {
        super(authDAO);
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    @Override
    public Object handleRequest(Request req, Response res) {
        Gson serializer = new Gson();
        try {
            // make object from request
            UserService.RegisterRequest request = serializer.fromJson(req.body(), UserService.RegisterRequest.class);

            // register user
            UserService service = new UserService(authDAO, userDAO);
            UserService.RegisterResult result = service.register(request);

            // Set the status code
            setStatusCode(res, result);

            // Return the body of the response
            res.type("application/json");
            return serializer.toJson(result);
        } catch (Exception e) {
            res.status(500);
            String errorMessage = "Internal server error: " + e.getMessage();
            UserService.RegisterResult result = new UserService.RegisterResult(null, null, errorMessage);
            return serializer.toJson(result);
        }
    }

}
