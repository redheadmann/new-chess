package serverfacade;

import com.google.gson.Gson;
import exception.ResponseException;
import model.GameData;
import service.UserService;

import java.io.*;
import java.net.*;

public class ServerFacade {

    private final String serverUrl;

    public ServerFacade(String url) {
        serverUrl = url;
    }


    public void clear() throws ResponseException {
        var path = "/db";
        this.makeRequest("DELETE", path, null, null);
    }

    public String[] registerUser(String username, String password, String email) throws ResponseException {
        // Create special register request object
        UserService.RegisterRequest request = new UserService.RegisterRequest(username, password, email);

        var path = "/user";
        UserService.RegisterResult result = this.makeRequest("POST", path,
                request, UserService.RegisterResult.class);
        return new String[]{result.username(), result.authToken()};
    }

    public String[] loginUser(String username, String password) throws ResponseException {
        // Special login request object
        UserService.LoginRequest request = new UserService.LoginRequest(username, password);

        var path = "/session";
        UserService.LoginResult result = this.makeRequest("POST", path,
                request, UserService.LoginResult.class);
    }
        Spark.delete("/session", (req, res) ->
            (new LogoutHandler(authDAO, userDAO)).handleRequest(req,
                                                                res)); // logout
        Spark.get("/game", (req, res) ->
            (new ListHandler(authDAO, gameDAO)).handleRequest(req,
                                                              res)); // list
        Spark.post("/game", (req, res) ->
            (new CreateHandler(authDAO, gameDAO)).handleRequest(req,
                                                                res)); // create game
        Spark.put("/game", (req, res) ->
            (new JoinHandler(authDAO, gameDAO)).handleRequest(req,
                                                              res)); // join game

    public GameData addGame(GameData gameData) throws ResponseException {
        var path = "/pet";
        return this.makeRequest("POST", path, null, null);
    }

    public void deletePet(int id) throws ResponseException {
        var path = String.format("/pet/%s", id);
        this.makeRequest("DELETE", path, null, null);
    }

    public void deleteAllPets() throws ResponseException {
        var path = "/pet";
        this.makeRequest("DELETE", path, null, null);
    }

    public GameData[] listPets() throws ResponseException {
        var path = "/pet";
        record listPetResponse(GameData[] pet) {
        }
        var response = this.makeRequest("GET", path, null, listPetResponse.class);
        return response.pet();
    }

    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass) throws ResponseException {
        try {
            URL url = (new URI(serverUrl + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);

            writeBody(request, http);
            http.connect();
            throwIfNotSuccessful(http);
            return readBody(http, responseClass);
        } catch (ResponseException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }


    private static void writeBody(Object request, HttpURLConnection http) throws IOException {
        if (request != null) {
            http.addRequestProperty("Content-Type", "application/json");
            String reqData = new Gson().toJson(request);
            try (OutputStream reqBody = http.getOutputStream()) {
                reqBody.write(reqData.getBytes());
            }
        }
    }

    private void throwIfNotSuccessful(HttpURLConnection http) throws IOException, ResponseException {
        var status = http.getResponseCode();
        if (!isSuccessful(status)) {
            try (InputStream respErr = http.getErrorStream()) {
                if (respErr != null) {
                    throw ResponseException.fromJson(respErr);
                }
            }

            throw new ResponseException(status, "other failure: " + status);
        }
    }

    private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        T response = null;
        if (http.getContentLength() < 0) {
            try (InputStream respBody = http.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(respBody);
                if (responseClass != null) {
                    response = new Gson().fromJson(reader, responseClass);
                }
            }
        }
        return response;
    }


    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }
}
