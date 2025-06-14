package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import repl.Repl;
import repl.State;
import serverfacade.websocket.ServerMessageObserver;
import serverfacade.websocket.WebSocketFacade;
import sharedexception.ResponseException;
import serverfacade.ServerFacade;
import records.GameRecords;
import websocket.messages.ServerMessage;

import java.util.Arrays;
import java.util.HashMap;

import static ui.EscapeSequences.*;

public class PostLoginClient implements Client, ServerMessageObserver {
    // This determines what game a user is accessing
    private HashMap<Integer, Integer> gameMap = new HashMap<>();

    private final ServerFacade server;
    private String authToken = null;
    private final Repl repl;

    public PostLoginClient(Repl repl) {
        server = repl.getServer();
        authToken = repl.getAuthToken();
        this.repl = repl;
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
                case "create" -> createGame(params);
                case "list" -> listGames();
                case "join" -> joinGame(params);
                case "observe" -> observe(params);
                case "logout" -> logout(params);
                default -> help();
            };
        } catch (ResponseException ex) {
            return ex.getMessage();
        }
    }

    public String createGame(String... params) throws ResponseException{
        if (params.length == 1) {
            try {
                String gameName = params[0];
                GameRecords.CreateResult result = server.createGame(authToken, gameName);

                return "success";
            } catch (NumberFormatException ignored) {
            }
        }
        throw new ResponseException(400, "Expected: create <NAME>");

    }

    private String createHashMapReturnString(GameRecords.ListResult result) {
        StringBuilder str = new StringBuilder();

        int gameNumber = 0;
        for (GameRecords.ReducedGameData gameData : result.games()) {
            gameNumber ++;
            gameMap.put(gameNumber, gameData.gameID());
            str.append( String.format("%d: %s whiteUsername:%s blackUsername:%s\n",
                    gameNumber, gameData.gameName(), gameData.whiteUsername(), gameData.blackUsername()) );
        }

        return str.toString();
    }

    public String listGames(String... params) throws ResponseException{
        if (params.length == 0) {
            try {
                GameRecords.ListResult result = server.listGames(authToken);


                return createHashMapReturnString(result);
            } catch (NumberFormatException ignored) {
            }
        }
        throw new ResponseException(400, "Expected: create <NAME>");

    }


    public String joinGame(String... params) throws ResponseException{
        if (params.length == 2) {
            try {
                // Check input
                Integer gameNum = Integer.parseInt(params[0]);
                String color = params[1];
                if (!color.equals("WHITE") && !color.equals("BLACK")) {
                    throw new ResponseException(400, "Expected: join <ID> [WHITE|BLACK]");
                }
                ChessGame.TeamColor playerColor = color.equals("WHITE") ?
                        ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;

                // Make sure gameID is not null and join the game
                Integer gameID = gameMap.get(gameNum);
                if (gameID == null) {
                    throw new ResponseException(400, "Invalid ID");
                }
                server.joinGame(authToken, color, gameID);

                // Send connect command via websocket
                WebSocketFacade ws = new WebSocketFacade(server.getUrl(), repl);
                repl.setWs(ws);
                // TODO:: send the command, which will automatically draw the board
                // upon receiving a notification in return

                // Update repl state and gameID
                repl.setState(State.IN_GAME);
                repl.setGameID(gameID);
                return null;
            } catch (NumberFormatException ignored) {
            }
        }

        throw new ResponseException(400, "Expected: join <ID> [WHITE|BLACK]");
    }

    public String observe(String... params) throws ResponseException{
        if (params.length == 1) {
            try {
                // Check input
                Integer gameNum = Integer.parseInt(params[0]);
                // Check that gameID is valid
                Integer gameID = gameMap.get(gameNum);
                if (gameID == null) {
                    throw new ResponseException(400, "Invalid ID");
                }

                // I do not yet have an observe function

                return drawBoard(Color.WHITE);
            } catch (NumberFormatException ignored) {
            }
        }

        throw new ResponseException(400, "Expected: join <ID> [WHITE|BLACK]");
    }


    public String logout(String... params) throws ResponseException{
        if (params.length == 0) {
            try {
                server.logoutUser(authToken);

                // update repl state and delete authToken from repl
                repl.setState(State.SIGNED_OUT);
                repl.setAuthToken(null);
            } catch (NumberFormatException ignored) {
            }
        }
        throw new ResponseException(400, "Expected: logout");
    }


    // Version of help message for post login
    public String help() {
        return SET_TEXT_COLOR_BLUE + "create <NAME>" +
                SET_TEXT_COLOR_WHITE + " - game\n" +
                SET_TEXT_COLOR_BLUE + "list" +
                SET_TEXT_COLOR_WHITE + " - games\n" +
                SET_TEXT_COLOR_BLUE + "join <ID> [WHITE|BLACK]" +
                SET_TEXT_COLOR_WHITE + " - a game\n" +
                SET_TEXT_COLOR_BLUE + "observe <ID>" +
                SET_TEXT_COLOR_WHITE + " - a game\n" +
                SET_TEXT_COLOR_BLUE + "logout" +
                SET_TEXT_COLOR_WHITE + " - when you are done\n" +
                SET_TEXT_COLOR_BLUE + "help" +
                SET_TEXT_COLOR_WHITE + " - with possible commands\n";
    }

    @Override
    public void notify(ServerMessage message) {
        repl.notify(message);
    }
}
