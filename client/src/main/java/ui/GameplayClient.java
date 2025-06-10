package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import exception.ResponseException;
import records.GameRecords;
import serverfacade.ServerFacade;

import java.util.Arrays;
import java.util.HashMap;

import static ui.EscapeSequences.*;
import static ui.EscapeSequences.SET_TEXT_COLOR_WHITE;
import static ui.Repl.LOGOUT_MESSAGE;

public class GameplayClient {


    // These determine what game a user is accessing
    private HashMap<Integer, Integer> gameMap = new HashMap<>();

    private final ServerFacade server;
    private String authToken = null;


    HashMap<ChessPiece.PieceType, String> whiteMap = new HashMap<>();
    HashMap<ChessPiece.PieceType, String> blackMap = new HashMap<>();


    public PostLoginClient(String serverUrl) {
        server = new ServerFacade(serverUrl);

        whiteMap.put(ChessPiece.PieceType.KING, WHITE_KING);
        whiteMap.put(ChessPiece.PieceType.QUEEN, WHITE_QUEEN);
        whiteMap.put(ChessPiece.PieceType.ROOK, WHITE_ROOK);
        whiteMap.put(ChessPiece.PieceType.KNIGHT, WHITE_KNIGHT);
        whiteMap.put(ChessPiece.PieceType.BISHOP, WHITE_BISHOP);
        whiteMap.put(ChessPiece.PieceType.PAWN, WHITE_PAWN);

        blackMap.put(ChessPiece.PieceType.KING, BLACK_KING);
        blackMap.put(ChessPiece.PieceType.QUEEN, BLACK_QUEEN);
        blackMap.put(ChessPiece.PieceType.ROOK, BLACK_ROOK);
        blackMap.put(ChessPiece.PieceType.KNIGHT, BLACK_KNIGHT);
        blackMap.put(ChessPiece.PieceType.BISHOP, BLACK_BISHOP);
        blackMap.put(ChessPiece.PieceType.PAWN, BLACK_PAWN);
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

    private enum Color {
        WHITE,
        BLACK
    }


    private PostLoginClient.Color updateColor(PostLoginClient.Color color) {
        return color == PostLoginClient.Color.WHITE ? PostLoginClient.Color.BLACK : PostLoginClient.Color.WHITE;
    }

    private void setDarkSquare(StringBuilder str) {
        str.append(SET_BG_COLOR_DARK_GREEN);
    }

    private void setLightSquare(StringBuilder str) {
        str.append(SET_BG_COLOR_WHITE);
    }

    private void drawPieceOrNull(StringBuilder str, ChessBoard board, int row, int col) {
        ChessPiece piece = board.getPiece(new ChessPosition(row, col));
        if (piece == null) {
            str.append(EMPTY);
        } else {
            if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                str.append(whiteMap.get(piece.getPieceType()));
            } else {
                str.append(blackMap.get(piece.getPieceType()));
            }
        }
    }

    private String drawRow(ChessBoard board, Integer row, Integer direction, PostLoginClient.Color leftColor) {
        // 0 for white perspective, 1 for black
        StringBuilder str = new StringBuilder();

        // Label
        str.append(String.format(" %d ", row));

        // Switch the color each iteration
        PostLoginClient.Color color = PostLoginClient.Color.WHITE;
        if (direction == 0 && leftColor == PostLoginClient.Color.BLACK) {
            color = PostLoginClient.Color.BLACK;
        } else if (direction == 1 && leftColor == PostLoginClient.Color.WHITE) {
            color = PostLoginClient.Color.BLACK;
        }

        // Go through row
        if (direction == 0) {
            for (int col = 1; col <= 8; col++) {
                if (color == PostLoginClient.Color.WHITE) {
                    setLightSquare(str);
                } else {
                    setDarkSquare(str);
                }
                drawPieceOrNull(str, board, row, col);
                color = updateColor(color);
            }
        } else {
            for (int col = 8; col >= 1; col--) {
                if (color == PostLoginClient.Color.WHITE) {
                    setLightSquare(str);
                } else {
                    setDarkSquare(str);
                }
                drawPieceOrNull(str, board, row, col);
                color = updateColor(color);
            }

        }
        str.append(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLACK).append(String.format(" %d ", row));

        return str.toString();
    }

    private String drawBoard(ChessBoard board, Integer direction) {
        StringBuilder str = new StringBuilder();

        // 0 for white perspective, 1 for black
        if (direction == 0) {
            str.append(WHITE_HEADER);
            for (int row = 8; row >= 1; row--) {
                // Even rows have white on far left
                if (row % 2 == 0) {
                    str.append(drawRow(board, row, 0, PostLoginClient.Color.WHITE));
                } else {
                    str.append(drawRow(board, row, 0, PostLoginClient.Color.BLACK));
                }
                str.append("\n");
            }
            str.append(WHITE_HEADER);
        } else {
            str.append(BLACK_HEADER);
            for (int row = 1; row <= 8; row++) {
                // Even rows have white on far left
                if (row % 2 == 0) {
                    str.append(drawRow(board, row, 1, PostLoginClient.Color.WHITE));
                } else {
                    str.append(drawRow(board, row, 1, PostLoginClient.Color.BLACK));
                }
                str.append("\n");
            }
            str.append(BLACK_HEADER);
        }

        return str.toString();
    }

    private String drawStartingBoard(PostLoginClient.Color color) {
        // use a board object
        ChessBoard board = new ChessBoard();
        board.resetBoard();

        if (color == PostLoginClient.Color.WHITE) {
            return drawBoard(board, 0);
        } else {
            return drawBoard(board, 1);
        }
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
                PostLoginClient.Color safeColor = color.equals("WHITE") ? PostLoginClient.Color.WHITE : PostLoginClient.Color.BLACK;

                Integer gameID = gameMap.get(gameNum);
                if (gameID == null) {
                    throw new ResponseException(400, "Invalid ID");
                }
                server.joinGame(authToken, color, gameID);


                return drawStartingBoard(safeColor);
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

                return drawStartingBoard(PostLoginClient.Color.WHITE);
            } catch (NumberFormatException ignored) {
            }
        }

        throw new ResponseException(400, "Expected: join <ID> [WHITE|BLACK]");
    }


    public String logout(String... params) throws ResponseException{
        if (params.length == 0) {
            try {
                server.logoutUser(authToken);

                return LOGOUT_MESSAGE;
            } catch (NumberFormatException ignored) {
            }
        }
        throw new ResponseException(400, "Expected: logout");
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
