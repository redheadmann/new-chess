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
        try {
            Gson serializer = CommandSerializer.createSerializer();
            UserGameCommand command = serializer.fromJson(message, UserGameCommand.class);

            // Get the chess move if we have a MakeMoveCommand
            ChessMove move;
            if (command instanceof MakeMoveCommand) {
                move = ((MakeMoveCommand) command).getMove();
            }

            // Find the username. Throws unauthorized exception
            String authToken = getUsername(command.getAuthToken());


        } catch (ResponseException ex) {
            sendMessage(session.getRemote(), new ErrorMessage("Error: unauthorized"));
        } catch (DataAccessException ex) {
            connections.broadcast(1234, null, null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private String getUsername(String authToken) throws DataAccessException{
        AuthData authData = authDAO.getAuth(authToken);
        return "Not implemented";
    }

}
