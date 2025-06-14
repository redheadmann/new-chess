package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

import java.util.HashMap;

import static ui.EscapeSequences.*;
import static ui.EscapeSequences.BLACK_HEADER;

abstract class BoardDrawer {

    HashMap<ChessPiece.PieceType, String> whiteMap = new HashMap<>();
    HashMap<ChessPiece.PieceType, String> blackMap = new HashMap<>();

    public BoardDrawer() {
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

    public void createPieceMap(HashMap<ChessPiece.PieceType, String> whiteMap,
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

    public Color updateColor(Color color) {
        return color == Color.WHITE ? Color.BLACK : Color.WHITE;
    }

    public void setDarkSquare(StringBuilder str) {
        str.append(SET_BG_COLOR_DARK_GREEN);
    }

    public void setLightSquare(StringBuilder str) {
        str.append(SET_BG_COLOR_WHITE);
    }

    public void drawPieceOrNull(StringBuilder str, ChessBoard board, int row, int col) {
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

    public String drawRow(ChessBoard board, Integer row, Integer direction, Color leftColor) {
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

        // Go through row
        if (direction == 0) {
            for (int col = 1; col <= 8; col++) {
                if (color == Color.WHITE) {
                    setLightSquare(str);
                } else {
                    setDarkSquare(str);
                }
                drawPieceOrNull(str, board, row, col);
                color = updateColor(color);
            }
        } else {
            for (int col = 8; col >= 1; col--) {
                if (color == Color.WHITE) {
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

    public String drawBoard(ChessBoard board, ChessGame.TeamColor color) {
        // Set direction
        int direction = setDirection(color);

        StringBuilder str = new StringBuilder();

        // 0 for white perspective, 1 for black
        if (direction == 0) {
            str.append(WHITE_HEADER);
            for (int row = 8; row >= 1; row--) {
                // Even rows have white on far left
                if (row % 2 == 0) {
                    str.append(drawRow(board, row, 0, Color.WHITE));
                } else {
                    str.append(drawRow(board, row, 0, Color.BLACK));
                }
                str.append("\n");
            }
            str.append(WHITE_HEADER);
        } else {
            str.append(BLACK_HEADER);
            for (int row = 1; row <= 8; row++) {
                // Even rows have white on far left
                if (row % 2 == 0) {
                    str.append(drawRow(board, row, 1, Color.WHITE));
                } else {
                    str.append(drawRow(board, row, 1, Color.BLACK));
                }
                str.append("\n");
            }
            str.append(BLACK_HEADER);
        }

        return str.toString();
    }

    public int setDirection(ChessGame.TeamColor color) {
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
