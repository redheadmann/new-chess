package ui;

import chess.ChessPiece;
import sharedexception.ResponseException;
import serverfacade.ServerFacade;

import java.util.Arrays;
import java.util.HashMap;

import static ui.EscapeSequences.*;
import static ui.EscapeSequences.SET_TEXT_COLOR_WHITE;

public class GameplayClient implements Client {


    // These determine what game a user is accessing
    private HashMap<Integer, Integer> gameMap = new HashMap<>();

    private final ServerFacade server;
    private String authToken = null;


    HashMap<ChessPiece.PieceType, String> whiteMap = new HashMap<>();
    HashMap<ChessPiece.PieceType, String> blackMap = new HashMap<>();


    public GameplayClient(String serverUrl) {
        server = new ServerFacade(serverUrl);

        createPieceMap(whiteMap, blackMap);
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
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
                case "redraw" -> createGame(params);
                case "leave" -> listGames();
                case "move" -> joinGame(params);
                case "resign" -> observe(params);
                case "highlight" -> logout(params);
                default -> help();
            };
        } catch (ResponseException ex) {
            return ex.getMessage();
        }
    }

    public String createGame(String... params) throws ResponseException{
        return "Not implemented";
    }

    public String listGames(String... params) throws ResponseException{
        return "Not implemented";
    }

    public String joinGame(String... params) throws ResponseException{
        return "Not implemented";
    }

    public String observe(String... params) throws ResponseException{
        return "Not implemented";
    }


    public String logout(String... params) throws ResponseException{
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
}
