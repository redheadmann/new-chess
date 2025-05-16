package server;

import dataaccess.*;
import handler.*;
import spark.*;

public class Server {

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Instantiate Data Access Objects here so all service methods act on the same database
        GameDAO gameDAO = new MemoryGameDAO();
        UserDAO userDAO = new MemoryUserDAO();
        AuthDAO authDAO = new MemoryAuthDAO();

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
