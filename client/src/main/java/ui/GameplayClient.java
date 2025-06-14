package ui;

import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import repl.Repl;
import repl.State;
import serverfacade.ServerFacade;
import serverfacade.websocket.ServerMessageObserver;
import serverfacade.websocket.WebSocketFacade;
import sharedexception.ResponseException;
import websocket.messages.ServerMessage;

import java.util.Arrays;
import java.util.HashMap;

import static ui.BoardDrawer.createFileMap;
import static ui.EscapeSequences.*;
import static ui.EscapeSequences.SET_TEXT_COLOR_WHITE;

public class GameplayClient implements Client, ServerMessageObserver {

    private ServerFacade server;
    private WebSocketFacade ws;
    private String authToken;
    private Integer gameID;
    private final Repl repl;


    public GameplayClient(Repl repl) {

        this.repl = repl;
    }

    public String eval(String input) {
        server = repl.getServer();
        ws = repl.getWs();
        authToken = repl.getAuthToken();
        gameID = repl.getGameID();
        try {
            var tokens = input.split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "redraw" -> redraw();
                case "leave" -> leaveGame();
                case "move" -> movePiece(params);
                case "resign" -> resign();
                case "highlight" -> highlightMoves(params);
                default -> help();
            };
        } catch (ResponseException ex) {
            return ex.getMessage();
        }
    }

    public String redraw() {
        return "Not implemented";
    }

    public String leaveGame() throws ResponseException {
        // delete websocket connection and update repl state and game ID
        repl.setState(State.SIGNED_IN);
        repl.setGameID(null);
        // disconnects websocket session
        ws.leave(authToken, gameID);
        repl.setWs(null);
        return "You left the game";
    }

    private boolean isValidChessPosition(String position) {
        // With my knowledge of regex, we will be okay
        return position != null && position.matches("^[a-h][1-8]$");
    }

    private ChessPosition parsePosition(String input) {
        // Create map from letters to numbers
        HashMap<Character, Integer> map = createFileMap();

        char[] chars = input.toCharArray();
        int col = map.get(chars[0]);
        int row = Integer.parseInt(String.valueOf(chars[1]));
        return new ChessPosition(row, col);
    }

    private final String moveResponseString = "Expected: move <START_POSITION> <END_POSITION> | <PROMOTION_TYPE> " +
            "(examples: e4 e5, h7 h8 QUEEN) - specify castling with the king's move";

    public String movePiece(String... params) throws ResponseException{
        if (params.length == 2 || params.length == 3) {
            try {
                ChessMove move = createChessMove(params, moveResponseString);

                ws.makeMove(authToken, gameID, move);
                return "";
            } catch (IllegalArgumentException ignored) {
            }
        }
        throw new ResponseException(500, moveResponseString);
    }

    private ChessMove createChessMove(String[] params, String moveResponseString) throws ResponseException {
        // Check each input
        if (!isValidChessPosition(params[0]) || !isValidChessPosition(params[1])) {
            throw new ResponseException(500, moveResponseString);
        }
        ChessPiece.PieceType promotionType= null;
        if (params.length == 3) {
            // throws IllegalArgumentException
            promotionType = ChessPiece.PieceType.valueOf(params[2].toUpperCase());
        }
        // return true;
        // Create the move
        ChessPosition start = parsePosition(params[0]);
        ChessPosition end = parsePosition(params[1]);
        return new ChessMove(start, end, promotionType);
    }

    public String resign() throws ResponseException {
        ws.resign(authToken, gameID);

        return "";
    }


    public String highlightMoves(String... params) throws ResponseException{

        return "Not implemented";
    }


    // Version of help message for post login
    public String help() {
        return SET_TEXT_COLOR_BLUE + "\nredraw" +
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
        repl.notify(message);
    }
}
