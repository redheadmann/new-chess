package ui;

import chess.ChessPiece;
import sharedexception.ResponseException;

import java.util.HashMap;

import static ui.EscapeSequences.*;
import static ui.EscapeSequences.BLACK_PAWN;

public interface Client {

    public String help() throws ResponseException;
    public String eval(String input) throws ResponseException;

    public enum Color {
        WHITE,
        BLACK
    }

    default void createPieceMap(HashMap<ChessPiece.PieceType, String> whiteMap,
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
}
