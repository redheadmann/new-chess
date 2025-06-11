package server;

import dataaccess.*;
import handler.*;
import spark.*;
import server.websocket.WebSocketHandler;


import java.net.http.WebSocket;

public class Server {
    private final GameDAO gameDAO;
    private final UserDAO userDAO;
    private final AuthDAO authDAO;
    private final WebSocketHandler webSocketHandler;

    public Server() {
        // Instantiate Data Access Objects here so all service methods act on the same database
        try {
            gameDAO = new SqlGameDAO();
            userDAO = new SqlUserDAO();
            authDAO = new SqlAuthDAO();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
        webSocketHandler = new WebSocketHandler(gameDAO, userDAO, authDAO);
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");


        // Websocket segment
        Spark.webSocket("/ws", webSocketHandler);

        // Register your endpoints and handle exceptions here.
        Spark.delete("/db", (req, res) ->
                (new ClearHandler(authDAO, userDAO, gameDAO)).handleRequest(req,
                        res)); // clear
        Spark.post("/user", (req, res) ->
        (new RegisterHandler(authDAO, userDAO)).handleRequest(req,
                res)); // register
        Spark.post("/session", (req, res) ->
                (new LoginHandler(authDAO, userDAO)).handleRequest(req,
                        res)); // login
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

        //This line initializes the server and can be removed once you have a functioning endpoint 
        //Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

}
