package server.websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.GameState;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.*;
import sharedexception.UnauthorizedException;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.commands.*;
import websocket.messages.*;

import java.io.IOException;
import java.util.Objects;

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

        } catch (UnauthorizedException ex) {
            // Send error to the user without adding to the connection manager
            try {
                session.getRemote().sendString(new ErrorMessage(ex.getMessage()).toString());
            } catch (IOException ignore) {
                // Log this
            }
        } catch (InvalidMoveException ex) {
            connections.broadcast(gameID, username, new ErrorMessage(
                    ex.getMessage()), ConnectionManager.SendType.ONE);
        } catch (DataAccessException ex) {
            // Make the message simpler in production
            connections.broadcast(gameID, username, new ErrorMessage(
                    ex.getMessage()), ConnectionManager.SendType.ONE);
        } catch (Exception ex) {
            ex.printStackTrace();
            connections.broadcast(gameID, username, new ErrorMessage(
                    "Error: " + ex.getMessage()), ConnectionManager.SendType.ONE);
        }
    }

    private String getUsername(String authToken) throws DataAccessException, UnauthorizedException {
        // if the authToken is missing, throws an exception
        AuthData authData = authDAO.getAuth(authToken);
        // return the username
        return authData.username();
    }

    private enum PlayerType {
        OBSERVER,
        PLAYER
    }

    private record PlayerInfo (PlayerType playerType, ChessGame.TeamColor color) {}

    private PlayerInfo getPlayerInfo(String username, Integer gameID) throws DataAccessException {
        // Get the game and determine if the user is playing + their color or observing
        GameData gameData = gameDAO.getGame(gameID);
        ChessGame.TeamColor color = null;
        PlayerType playerType = null;
        if (Objects.equals(gameData.whiteUsername(), username)) {
            color = ChessGame.TeamColor.WHITE;
            playerType = PlayerType.PLAYER;
        } else if (Objects.equals(gameData.blackUsername(), username)) {
            color = ChessGame.TeamColor.BLACK;
            playerType = PlayerType.PLAYER;
        } else {
            playerType = PlayerType.OBSERVER;
        }

        return new PlayerInfo(playerType, color);
    }

    private String createConnectMessage(String username, PlayerInfo playerInfo) {
        ChessGame.TeamColor color = playerInfo.color();
        PlayerType playerType = playerInfo.playerType();
        switch (playerType) {
            case OBSERVER -> {
                return username + " is observing";
            }
            case PLAYER -> {
                if (color == ChessGame.TeamColor.WHITE) {
                    return username + " joined as white";
                } else {
                    return username + " joined as black";
                }
            }
            default -> {
                return "Issue connecting to game";
            }
        }
    }

    private void connect(Session session, String username, UserGameCommand command) throws DataAccessException {
        // Get gameID
        Integer gameID = command.getGameID();
        UserGameCommand.CommandType commandType = command.getCommandType();
        // Add user to connections
        connections.add(gameID, username, session);

        // Now get the game and determine if the user is playing + their color or observing
        PlayerInfo playerInfo = getPlayerInfo(username, gameID);

        // Send a load_game message back to user
        GameData gameData = gameDAO.getGame(gameID);
        ChessGame game = gameData.game();
        connections.broadcast(gameID, username,
                new LoadGameMessage(game), ConnectionManager.SendType.ONE);

        // Send notification to other users
        connections.broadcast(gameID, username,
                new NotificationMessage(createConnectMessage(username, playerInfo)),
                ConnectionManager.SendType.EXCLUDE_ONE);
    }

    private void makeMove(Session session, String username, MakeMoveCommand command)
            throws DataAccessException, InvalidMoveException {
        // Get gameID and chess move
        Integer gameID = command.getGameID();
        ChessMove move = command.getMove();

        // Get the game and determine if the user is playing (+ their color) or observing
        PlayerInfo playerInfo = getPlayerInfo(username, gameID);
        ChessGame.TeamColor color = playerInfo.color();
        GameData gameData = gameDAO.getGame(gameID);
        ChessGame game = gameData.game();

        // Only continue if the user is a player
        if (playerInfo.playerType == PlayerType.OBSERVER) {
            throw new DataAccessException("Error: observers cannot make a move");
        }
        // Only continue if it is player's turn
        if (game.getTeamTurn() != color) {
            throw new InvalidMoveException("Error: it is not your turn!");
        }

        // Update game
        game.makeMove(move);
        gameDAO.makeMove(gameID, game);

        // Send load_game message to everybody
        connections.broadcast(gameID, username,
                new LoadGameMessage(game), ConnectionManager.SendType.ALL);
        // send notification telling what move was made
        connections.broadcast(gameID, username,
                new NotificationMessage(username + " made move " + move.toString()),
                ConnectionManager.SendType.EXCLUDE_ONE);
        // Send notification if in check, or game is over for checkmate or stalemate
        sendGameOverMessages(gameID, color, username, game);
    }

    private void sendGameOverMessages(Integer gameID, ChessGame.TeamColor playerColor, String username,
                                      ChessGame game) {

        // Check if game is over
        if (!game.gameIsOver()) {
            // check for... check
            if (game.isInCheck(ChessGame.TeamColor.WHITE)) {
                connections.broadcast(gameID, username,
                        new NotificationMessage("White is in check"),
                        ConnectionManager.SendType.ALL);
            } else if (game.isInCheck(ChessGame.TeamColor.BLACK)) {
                connections.broadcast(gameID, username,
                        new NotificationMessage("Black is in check"),
                        ConnectionManager.SendType.ALL);
            }
            return;
        }

        GameState.Winner winner = game.getWinner();
        if (winner == GameState.Winner.DRAW) {
            connections.broadcast(gameID, username,
                    new NotificationMessage("Game ended in stalemate"),
                    ConnectionManager.SendType.ALL);
        } else if (winner == GameState.Winner.BLACK) {
            connections.broadcast(gameID, username,
                    new NotificationMessage("Black wins!"),
                    ConnectionManager.SendType.ALL);
        } else if (winner == GameState.Winner.WHITE) {
            connections.broadcast(gameID, username,
                    new NotificationMessage("White wins!"),
                    ConnectionManager.SendType.ALL);
        }
    }

    private void leaveGame(Session session, String username, UserGameCommand command)
            throws DataAccessException{
        // Get gameID
        Integer gameID = command.getGameID();

        // Leave game
        gameDAO.leaveGame(username, gameID);

        // Leave from connections manager
        connections.remove(gameID, username);

        ///////
        // Should users only be notified if a PLAYER is leaving?????

        // Notify remaining players and observers
        connections.broadcast(gameID, username,
                new NotificationMessage(username + " left"),
                ConnectionManager.SendType.ALL);
    }


    private void resign(Session session, String username, UserGameCommand command)
            throws DataAccessException, InvalidMoveException {
        // Get gameID
        Integer gameID = command.getGameID();

        // Get the game and determine if the user is playing (+ their color) or observing
        PlayerInfo playerInfo = getPlayerInfo(username, gameID);
        ChessGame.TeamColor color = playerInfo.color();
        GameData gameData = gameDAO.getGame(gameID);


        // Only continue if the user is a player
        if (playerInfo.playerType == PlayerType.OBSERVER) {
            throw new DataAccessException("Error: observers cannot resign, try LEAVE instead");
        }

        // Update game by resigning
        ChessGame game = gameData.game();
        game.resign(playerInfo.color());
        gameDAO.makeMove(gameID, game);

        // Inform all clients that the player resigned
        connections.broadcast(gameID, username,
                new NotificationMessage(username + " resigned"),
                ConnectionManager.SendType.ALL);
    }
}
