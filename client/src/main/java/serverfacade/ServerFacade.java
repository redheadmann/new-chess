package serverfacade;

import com.google.gson.Gson;
import exception.ResponseException;
import records.GameRecords;
import records.UserRecords;

import java.io.*;
import java.net.*;

public class ServerFacade {

    private final String serverUrl;

    public ServerFacade(String url) {
        serverUrl = url;
    }


    public void clear() throws ResponseException {
        var path = "/db";
        this.makeRequest("DELETE", path, null, null, null);
    }


    public UserRecords.RegisterResult registerUser(String username, String password, String email) throws ResponseException {
        // Register request object
        UserRecords.RegisterRequest request = new UserRecords.RegisterRequest(username, password, email);

        var path = "/user";
        return this.makeRequest("POST", path,
                request, UserRecords.RegisterResult.class, null);
    }


    public UserRecords.LoginResult loginUser(String username, String password) throws ResponseException {
        // Login request object
        UserRecords.LoginRequest request = new UserRecords.LoginRequest(username, password);

        var path = "/session";
        return this.makeRequest("POST", path,
                request, UserRecords.LoginResult.class, null);
    }


    public void logoutUser(String authToken) throws ResponseException {
        // Logout request object

        var path = "/session";
        this.makeRequest("DELETE", path,
                null, UserRecords.LogoutResult.class, authToken);

    }


    public GameRecords.ListResult listGames(String authToken) throws ResponseException {
        // No request object exists
        var path = "/game";
        return this.makeRequest("GET", path,
                null, GameRecords.ListResult.class, authToken);

        // Why does that not actually include the games?
    }


    public GameRecords.CreateResult createGame(String authToken, String gameName) throws ResponseException {
        GameRecords.CreateRequest request = new GameRecords.CreateRequest(gameName);

        var path = "/game";
        return this.makeRequest("POST", path,
                request, GameRecords.CreateResult.class, authToken);
    }


    public void joinGame(String authToken, String playerColor, int gameID) throws ResponseException {
        GameRecords.JoinRequest request = new GameRecords.JoinRequest(playerColor, gameID);

        var path = "/game";
        GameRecords.JoinResult result = this.makeRequest("PUT", path,
                request, GameRecords.JoinResult.class, authToken);
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
