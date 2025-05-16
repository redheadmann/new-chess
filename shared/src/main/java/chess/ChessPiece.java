package chess;

import calculators.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type; //might need to be mutable

    public ChessPiece(ChessGame.TeamColor pieceColor, PieceType type) {
        this.pieceColor = pieceColor; //should be one of the enum ChessGame.TeamColor
        this.type = type;
    }

    public ChessPiece(ChessPiece original) {
        this.pieceColor = original.pieceColor;
        this.type = original.type;
    }


    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {return true;}
        if (o == null || getClass() != o.getClass()) {return false;}
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    @Override
    public String toString() {
        HashMap<PieceType, String> map = new HashMap<>();
        map.put(PieceType.KING, "K");
        map.put(PieceType.QUEEN, "Q");
        map.put(PieceType.ROOK, "R");
        map.put(PieceType.KNIGHT, "N");
        map.put(PieceType.BISHOP, "B");
        map.put(PieceType.PAWN, "P");

        ChessGame.TeamColor color = this.getTeamColor();
        PieceType type = this.getPieceType();

        if (color == ChessGame.TeamColor.BLACK) {
            return map.get(type).toLowerCase();
        } else {
            return map.get(type);
        }


    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        PieceType type = this.type;
        PieceMovesCalculator movesCalculator = switch (type) {
            case KING -> new KingMovesCalculator();
            case QUEEN -> new QueenMovesCalculator();
            case BISHOP -> new BishopMovesCalculator();
            case KNIGHT -> new KnightMovesCalculator();
            case ROOK -> new RookMovesCalculator();
            case PAWN -> new PawnMovesCalculator();
        };

        return movesCalculator.pieceMoves(board, myPosition);
    }
}

