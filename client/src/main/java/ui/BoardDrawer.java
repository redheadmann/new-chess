package ui;

import chess.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import static ui.EscapeSequences.*;
import static ui.EscapeSequences.BLACK_HEADER;

public abstract class BoardDrawer {

    static HashMap<ChessPiece.PieceType, String> whiteMap = new HashMap<>();
    static HashMap<ChessPiece.PieceType, String> blackMap = new HashMap<>();

    static {
        createPieceMap(whiteMap, blackMap);
    }

    public static HashMap<Character, Integer> createFileMap() {
        HashMap<Character, Integer> fileMap = new HashMap<>();
        fileMap.put('a', 1);
        fileMap.put('b', 2);
        fileMap.put('c', 3);
        fileMap.put('d', 4);
        fileMap.put('e', 5);
        fileMap.put('f', 6);
        fileMap.put('g', 7);
        fileMap.put('h', 8);
        return fileMap;
    }

    static void createPieceMap(HashMap<ChessPiece.PieceType, String> whiteMap,
                               HashMap<ChessPiece.PieceType, String> blackMap) {
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

    public enum Color {
        WHITE,
        BLACK
    }

    public static Color updateColor(Color color) {
        return color == Color.WHITE ? Color.BLACK : Color.WHITE;
    }

    public static void setDarkSquare(StringBuilder str, ArrayList<ChessPosition> endPositions, ChessPosition myPosition, ChessPosition currentPosition) {
        if (currentPosition == myPosition) {
            str.append(SET_BG_COLOR_YELLOW);
        } else if (endPositions.contains(currentPosition)) {
            str.append(SET_BG_COLOR_DARK_GREEN);
        } else {
            str.append(SET_BG_COLOR_BLUE);
        }
    }

    public static void setLightSquare(StringBuilder str, ArrayList<ChessPosition> endPositions, ChessPosition myPosition, ChessPosition currentPosition) {
        if (currentPosition == myPosition) {
            str.append(SET_BG_COLOR_YELLOW);
        } else if (endPositions.contains(currentPosition)) {
            str.append(SET_BG_COLOR_GREEN);
        } else {
            str.append(SET_BG_COLOR_WHITE);
        }
    }

    public static void drawPieceOrNull(StringBuilder str, ChessBoard board, int row, int col) {
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

    public static String drawRow(ChessBoard board, Integer row, Integer direction, Color leftColor, ArrayList<ChessPosition> endPositions, ChessPosition myPosition) {
        // 0 for white perspective, 1 for black
        StringBuilder str = new StringBuilder();

        // Label
        str.append(String.format(" %d ", row));

        // Switch the color each iteration
        Color color = Color.WHITE;
        if (direction == 0 && leftColor == Color.BLACK) {
            color = Color.BLACK;
        } else if (direction == 1 && leftColor == Color.WHITE) {
            color = Color.BLACK;
        }

        ChessPosition currentPosition;
        // Go through row
        if (direction == 0) {
            for (int col = 1; col <= 8; col++) {
                currentPosition = new ChessPosition(row, col);
                if (color == Color.WHITE) {
                    setLightSquare(str, endPositions, myPosition, currentPosition);
                } else {
                    setDarkSquare(str, endPositions, myPosition, currentPosition);
                }
                drawPieceOrNull(str, board, row, col);
                color = updateColor(color);
            }
        } else {
            for (int col = 8; col >= 1; col--) {
                currentPosition = new ChessPosition(row, col);
                if (color == Color.WHITE) {
                    setLightSquare(str, endPositions, myPosition, currentPosition);
                } else {
                    setDarkSquare(str, endPositions, myPosition, currentPosition);
                }
                drawPieceOrNull(str, board, row, col);
                color = updateColor(color);
            }

        }
        str.append(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLACK).append(String.format(" %d ", row));

        return str.toString();
    }


    public static String drawBoard(ChessBoard board, ChessGame.TeamColor color, Collection<ChessMove> moves, ChessPosition myPosition) {
        // Set direction
        int direction = setDirection(color);

        // If moves is not null
        ArrayList<ChessPosition> endPositions = null;
        if (moves != null) {
            for (ChessMove move : moves) {
                endPositions.add(move.getEndPosition());
            }
        }

        StringBuilder str = new StringBuilder();

        // 0 for white perspective, 1 for black
        if (direction == 0) {
            str.append(WHITE_HEADER);
            for (int row = 8; row >= 1; row--) {
                // Even rows have white on far left
                if (row % 2 == 0) {
                    str.append(drawRow(board, row, 0, Color.WHITE, endPositions, myPosition));
                } else {
                    str.append(drawRow(board, row, 0, Color.BLACK, endPositions, myPosition));
                }
                str.append("\n");
            }
            str.append(WHITE_HEADER);
        } else {
            str.append(BLACK_HEADER);
            for (int row = 1; row <= 8; row++) {
                // Even rows have white on far left
                if (row % 2 == 0) {
                    str.append(drawRow(board, row, 1, Color.WHITE, endPositions, myPosition));
                } else {
                    str.append(drawRow(board, row, 1, Color.BLACK, endPositions, myPosition));
                }
                str.append("\n");
            }
            str.append(BLACK_HEADER);
        }

        return str.toString();
    }

    public static int setDirection(ChessGame.TeamColor color) {
        // use a board object
        ChessBoard board = new ChessBoard();
        board.resetBoard();

        if (color == ChessGame.TeamColor.WHITE) {
            return 0;
        } else {
            return 1;
        }
    }
}
