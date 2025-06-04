package serverfacade;

import com.google.gson.Gson;
import exception.ResponseException;
import model.GameData;
import service.UserService;
import service.GameService;

import java.io.*;
import java.net.*;
import java.util.List;

public class ServerFacade {

    private final String serverUrl;

    public ServerFacade(String url) {
        serverUrl = url;
    }


    public void clear() throws ResponseException {
        var path = "/db";
        this.makeRequest("DELETE", path, null, null, null);
    }


    public String[] registerUser(String username, String password, String email) throws ResponseException {
        // Register request object
        UserService.RegisterRequest request = new UserService.RegisterRequest(username, password, email);

        var path = "/user";
        UserService.RegisterResult result = this.makeRequest("POST", path,
                request, UserService.RegisterResult.class, null);
        return new String[]{result.username(), result.authToken()};
    }


    public String[] loginUser(String username, String password) throws ResponseException {
        // Login request object
        UserService.LoginRequest request = new UserService.LoginRequest(username, password);

        var path = "/session";
        UserService.LoginResult result = this.makeRequest("POST", path,
                request, UserService.LoginResult.class, null);
        return new String[]{result.username(), result.authToken()};
    }


    public void logoutUser(String authToken) throws ResponseException {
        // Logout request object
        UserService.LogoutRequest request = new UserService.LogoutRequest(authToken);

        var path = "/session";
        this.makeRequest("DELETE", path,
                request, UserService.LogoutResult.class, authToken);

    }


    public List<GameService.ReducedGameData> listGames(String authToken) throws ResponseException {
        // No request object exists
        var path = "/game";
        GameService.ListResult result = this.makeRequest("GET", path,
                null, GameService.ListResult.class, authToken);

        // Might need to change this to actually include the games. Not sure why it didn't from the beginning.
        return result.games();
    }


    public int createGame(String authToken, String gameName) throws ResponseException {
        GameService.CreateRequest request = new GameService.CreateRequest(gameName);

        var path = "/game";
        GameService.CreateResult result = this.makeRequest("POST", path,
                request, GameService.CreateResult.class, authToken);

        // Returns the game ID
    }


    public String joinGame(String authToken, String gameName) throws ResponseException {
        GameService.JoinRequest request = new GameService.JoinRequest(null, null);

        var path = "/game";
        GameService.JoinResult result = this.makeRequest("POST", path,
                request, GameService.JoinResult.class, authToken);
    }

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

    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass, String authToken) throws ResponseException {
        try {
            URL url = (new URI(serverUrl + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);

            writeHeader(authToken, http);
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


    private static void writeHeader(String authToken, HttpURLConnection http) throws IOException {
        if (authToken != null) {
            http.addRequestProperty("authorization", authToken);
            try (OutputStream reqBody = http.getOutputStream()) {
                reqBody.write(authToken.getBytes());
            }
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
