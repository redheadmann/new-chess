package chess;

import java.util.Collection;

import static chess.ChessGame.getOtherColor;
import calculators.*;

public class ChessRuleBook {

    /**
     *
     * @param board the chess board
     * @param myPosition the position of the piece we consider valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     *      * startPosition
     */
    public Collection<ChessMove> makeMoves(ChessBoard board, ChessPosition myPosition) {
        // Find the type of the piece and its color
        ChessPiece chessPiece = board.getPiece(myPosition);
        if (chessPiece == null) {return null;} // null check
        ChessPiece.PieceType type = chessPiece.getPieceType();

        // Use the appropriate moves calculator
        PieceMovesCalculator movesCalculator = switch (type) {
            case KING -> new KingMovesCalculator();
            case QUEEN -> new QueenMovesCalculator();
            case BISHOP -> new BishopMovesCalculator();
            case KNIGHT -> new KnightMovesCalculator();
            case ROOK -> new RookMovesCalculator();
            case PAWN -> new PawnMovesCalculator();
        };

        // Find all potential moves
        return movesCalculator.pieceMoves(board, myPosition);
    }


    /**
     * Filters out moves which leave the king in check
     *
     * @param board the chess board
     * @param myPosition the position of the piece we consider valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     *      * startPosition
     */
    public Collection<ChessMove> validMoves(ChessBoard board, ChessPosition myPosition) {
        // Get all potential moves
        Collection<ChessMove> moves = makeMoves(board, myPosition);
        if (moves == null) {return null;} // null check

        // Get team color and remove moves which leave our king in check
        ChessPiece chessPiece = board.getPiece(myPosition);
        ChessGame.TeamColor teamColor = chessPiece.getTeamColor();
        moves.removeIf(move -> movePlacesInCheck(board, move, teamColor));
        return moves;
    }


    public Boolean isInCheck(ChessBoard board,  ChessGame.TeamColor teamColor) {
        // Find the king
        ChessPosition ourKingPosition = board.findKing(teamColor);

        /* Check the moves of each piece on the opposing team and see if they can attack the king's cell
            The endPosition of their move will be the same position as our king
         */
        ChessGame.TeamColor otherTeamColor = getOtherColor(teamColor);
        for (ChessBoard.BoardIterator<ChessPosition> iterator = board.iterator(otherTeamColor); iterator.hasNext();) {
            ChessPosition opposingPiecePosition = iterator.next();
            for (ChessMove move: makeMoves(board, opposingPiecePosition)) {
                ChessPosition endPosition = move.getEndPosition();
                if (endPosition.equals(ourKingPosition)) { // if the opposing piece can attack the king, return true
                    return Boolean.TRUE;
                }
            }
        }

        return Boolean.FALSE; // no opposing piece can attack our king
    }


    /**
     * Checks if a king is in checkmate
     *
     * @param board the chess board
     * @param teamColor the color of the king
     * @return boolean of whether the king is in checkmate
     */
    public Boolean isInCheckmate(ChessBoard board, ChessGame.TeamColor teamColor) {
        /*
        We can move out of checkmate by
            1) moving the king out of check
            2) blocking the attacking piece
            3) capture the attacking piece
        These amount to
            1) moving the king to each possible position and checking if we are in check
            and 2) moving each other piece to all possible positions and checking if we are in check
         */

        if (!isInCheck(board, teamColor)) {return Boolean.FALSE;} // If we aren't even in check, we are not in checkmate

        // Our king is in check. For each position held by this team,
        for (ChessBoard.BoardIterator<ChessPosition> positionIterator = board.iterator(teamColor); positionIterator.hasNext();) {
            ChessPosition startPosition = positionIterator.next();
            // consider all moves the piece at that position could make
            for (ChessMove move : makeMoves(board, startPosition)) {
                if (!movePlacesInCheck(board, move, teamColor)) { // If the move does not keep us in check, this is not checkmate
                    return Boolean.FALSE;
                }
            }
        }

        // If every possible move results in check, return true
        return Boolean.TRUE;
    }


    public Boolean isInStalemate(ChessBoard board, ChessGame.TeamColor teamColor) {

        /*
            We first check if we are actually in checkmate
            Then we check every single piece with teamColor, and if at least one has a move, return False
            otherwise, return True
        */

        if (isInCheckmate(board, teamColor)) {return Boolean.FALSE;}

        // For each position held by this team,
        for (ChessBoard.BoardIterator<ChessPosition> positionIterator = board.iterator(teamColor); positionIterator.hasNext();) {
            ChessPosition myPosition = positionIterator.next();
            // if a piece can make at least one move, we are not in stalemate
            if (!validMoves(board, myPosition).isEmpty()) {
                return Boolean.FALSE;
            }
        }

        // This team cannot make any moves
        return Boolean.TRUE;
    }


    public Boolean movePlacesInCheck(ChessBoard board, ChessMove move, ChessGame.TeamColor teamColor) {
        ChessPosition startPosition = move.getStartPosition();
        ChessPosition endPosition = move.getEndPosition(); // position to which our piece could move


        // In case our piece captures an opposing piece, temporarily store it before moving our piece
        // If the piece is a pawn, check if the move is en passant
        ChessPiece.PieceType pieceType = board.getPiece(startPosition).getPieceType();
        ChessPiece possiblePiece;
        ChessPosition opposingPosition;
        if (pieceType == ChessPiece.PieceType.PAWN) {
            PawnMovesCalculator pawnCalculator = new PawnMovesCalculator();
            if (pawnCalculator.moveIsEnPassant(board, move)) {
                opposingPosition = pawnCalculator.getEnPassantCapturePosition(move);
            } else {
                opposingPosition = endPosition;
            }
        } else {
            opposingPosition = endPosition;
        }
        possiblePiece = board.getPiece(opposingPosition); // could be an opposing piece

        // Move our piece, but track our old piece type in case this is a pawn being promoted
        ChessPiece.PieceType oldType = board.getPiece(startPosition).getPieceType();
        board.movePiece(move);
        // Find if we are in check now
        Boolean inCheck;
        if (isInCheck(board, teamColor)) {
            inCheck = Boolean.TRUE;
        } else {
            inCheck = Boolean.FALSE;
        }
        // Reverse the move
        ChessMove reverseMove = move.reverseMove(oldType);
        board.movePiece(reverseMove); // move the piece back where it was
        board.addPiece(opposingPosition, possiblePiece); // put the piece back which may have been captured

        return inCheck;
    }
}
