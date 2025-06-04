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


    public UserService.RegisterResult registerUser(String username, String password, String email) throws ResponseException {
        // Register request object
        UserService.RegisterRequest request = new UserService.RegisterRequest(username, password, email);

        var path = "/user";
        return this.makeRequest("POST", path,
                request, UserService.RegisterResult.class, null);
    }


    public UserService.LoginResult loginUser(String username, String password) throws ResponseException {
        // Login request object
        UserService.LoginRequest request = new UserService.LoginRequest(username, password);

        var path = "/session";
        return this.makeRequest("POST", path,
                request, UserService.LoginResult.class, null);
    }


    public void logoutUser(String authToken) throws ResponseException {
        // Logout request object

        var path = "/session";
        this.makeRequest("DELETE", path,
                null, UserService.LogoutResult.class, authToken);

    }


    public GameService.ListResult listGames(String authToken) throws ResponseException {
        // No request object exists
        var path = "/game";
        return this.makeRequest("GET", path,
                null, GameService.ListResult.class, authToken);

        // Why does that not actually include the games?
    }


    public GameService.CreateResult createGame(String authToken, String gameName) throws ResponseException {
        GameService.CreateRequest request = new GameService.CreateRequest(gameName);

        var path = "/game";
        return this.makeRequest("POST", path,
                request, GameService.CreateResult.class, authToken);
    }


    public void joinGame(String authToken, String playerColor, int gameID) throws ResponseException {
        GameService.JoinRequest request = new GameService.JoinRequest(playerColor, gameID);

        var path = "/game";
        GameService.JoinResult result = this.makeRequest("PUT", path,
                request, GameService.JoinResult.class, authToken);
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
