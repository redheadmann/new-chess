package ui;

import chess.ChessPiece;
import serverfacade.websocket.ServerMessageObserver;
import serverfacade.websocket.WebSocketFacade;
import sharedexception.ResponseException;
import serverfacade.ServerFacade;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.util.Arrays;
import java.util.HashMap;

import static ui.EscapeSequences.*;
import static ui.EscapeSequences.SET_TEXT_COLOR_WHITE;

public class GameplayClient implements Client, ServerMessageObserver {


    // These determine what game a user is accessing
    private HashMap<Integer, Integer> gameMap = new HashMap<>();

    private final WebSocketFacade server;
    private String authToken = null;


    HashMap<ChessPiece.PieceType, String> whiteMap = new HashMap<>();
    HashMap<ChessPiece.PieceType, String> blackMap = new HashMap<>();


    public GameplayClient(String serverUrl) throws ResponseException {
        server = new WebSocketFacade(serverUrl, this);

        createPieceMap(whiteMap, blackMap);
    }

    public String eval(String input) {
        try {
            var tokens = input.split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            if (authToken == null) {
                return "";
            }
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "redraw" -> redraw(params);
                case "leave" -> leaveGame();
                case "move" -> movePiece(params);
                case "resign" -> resign(params);
                case "highlight" -> highlightMoves(params);
                default -> help();
            };
        } catch (ResponseException ex) {
            return ex.getMessage();
        }
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public String redraw(String... params) throws ResponseException{
        return "Not implemented";
    }

    public String leaveGame(String... params) throws ResponseException{
        return "Not implemented";
    }

    public String movePiece(String... params) throws ResponseException{
        return "Not implemented";
    }

    public String resign(String... params) throws ResponseException{
        return "Not implemented";
    }


    public String highlightMoves(String... params) throws ResponseException{
        return "Not implemented";
    }


    // Version of help message for post login
    public String help() {
        return SET_TEXT_COLOR_BLUE + "redraw" +
                SET_TEXT_COLOR_WHITE + " - the board\n" +
                SET_TEXT_COLOR_BLUE + "leave" +
                SET_TEXT_COLOR_WHITE + " - the game\n" +
                SET_TEXT_COLOR_BLUE + "move " +
                SET_TEXT_COLOR_WHITE + " - a piece\n" +
                SET_TEXT_COLOR_BLUE + "resign" +
                SET_TEXT_COLOR_WHITE + " - from the game\n" +
                SET_TEXT_COLOR_BLUE + "highlight " +
                SET_TEXT_COLOR_WHITE + " - possible moves\n" +
                SET_TEXT_COLOR_BLUE + "help" +
                SET_TEXT_COLOR_WHITE + " - with possible commands\n";
    }

    @Override
    public void notify(ServerMessage message) {

    }
}
