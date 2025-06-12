package server.websocket;

import chess.ChessMove;
import com.google.gson.Gson;
import dataaccess.*;
import exception.ResponseException;
import model.AuthData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import spark.serialization.Serializer;
import websocket.commands.*;
import websocket.messages.*;

@WebSocket
public class WebSocketHandler {

    private final ConnectionManager connections = new ConnectionManager();
    private final GameDAO gameDAO;
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public WebSocketHandler(GameDAO gameDAO, UserDAO userDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        Integer gameID = null;
        String username = null;
        try {
            Gson serializer = CommandSerializer.createSerializer();
            UserGameCommand command = serializer.fromJson(message, UserGameCommand.class);

            // Get the chess move if we have a MakeMoveCommand
            ChessMove move;
            if (command instanceof MakeMoveCommand) {
                move = ((MakeMoveCommand) command).getMove();
            }

            // Find the username. Throws unauthorized exception
            username = getUsername(command.getAuthToken());

            // Save the session in connection manager
            gameID = command.getGameID();
            connections.add(gameID, username, session);

            switch (command.getCommandType()) {
                case CONNECT -> connect(session, username, command);
                case MAKE_MOVE -> makeMove(session, username, (MakeMoveCommand) command);
                case LEAVE -> leaveGame(session, username, command);
                case RESIGN -> resign(session, username, command);
            }

        } catch (DataAccessException ex) {
            connections.broadcast(gameID, username, new ErrorMessage(
                    ServerMessage.ServerMessageType.ERROR, "Error: unauthorized"));
        } catch (Exception ex) {
            ex.printStackTrace();
            connections.broadcast(gameID, username, new ErrorMessage(
                    ServerMessage.ServerMessageType.ERROR, "Error: " + ex.getMessage()));
        }
    }


    private String getUsername(String authToken) throws DataAccessException {
        // if the authToken is missing, throws an exception
        AuthData authData = authDAO.getAuth(authToken);
        // return the username
        return authData.username();
    }

    private void connect(Session session, String username, UserGameCommand command) {
        // Get gameID
        Integer gameID = command.getGameID();
        // Add user to connections
        connections.add(gameID, username, session);
    }

    private void makeMove(Session session, String username, MakeMoveCommand command) {
        // Get gameID and chess move
        Integer gameID = command.getGameID();
        ChessMove move = command.getMove();
    }

    private void leaveGame(Session session, String username, UserGameCommand command) {
        // Get gameID
        Integer gameID = command.getGameID();
    }

    private void resign(Session session, String username, UserGameCommand command) {
        // Get gameID
        Integer gameID = command.getGameID();
    }
}
