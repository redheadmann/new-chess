package calculators;

import java.util.ArrayList;
import java.util.Collection;
import chess.*;

public class PawnMovesCalculator implements PieceMovesCalculator {

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ArrayList<ChessMove> moves = new ArrayList<>();

        int currentRow = myPosition.getRow();
        int currentCol = myPosition.getColumn();
        ChessPiece pawn = board.getPiece(myPosition);
        ChessGame.TeamColor teamColor = pawn.getTeamColor();

        // use this multiplier to move forward/backward in the following code
        int direction = switch (teamColor) {
            case WHITE -> 1;
            case BLACK -> -1;
        };

        /*
            These next movements are all in one column. We must be sure no piece is in the way
         */
        int nextRow = currentRow + direction;
        ChessPosition nextPosition = new ChessPosition(nextRow, currentCol);

        addForwardMoves(board, myPosition, nextPosition, nextRow, moves, direction, currentCol, teamColor, currentRow);

        /*
            Check for pieces diagonal to pawn
         */
        addDiagonalMoves(board, myPosition, currentCol, currentRow, direction, teamColor, moves);

        return moves;
    }

    private static void addDiagonalMoves(ChessBoard board, ChessPosition myPosition, int currentCol,
                                         int currentRow, int direction, ChessGame.TeamColor teamColor,
                                         ArrayList<ChessMove> moves) {
        int nextRow;
        ChessPosition nextPosition;
        for (int colMovement: new int[]{-1, 1}) {
            int nextCol = currentCol + colMovement;
            nextRow = currentRow + direction;

            if (nextCol >= 1 && nextCol <= 8) { // col on board
                // Check for piece on diagonal
                nextPosition = new ChessPosition(nextRow, nextCol);
                if (board.getPiece(nextPosition) != null) { // if piece is diagonal to pawn
                    addNormalCaptures(board, myPosition, teamColor, moves, nextPosition, nextRow);
                } else { // piece is not diagonal to pawn, so en passant is possible
                    addEnPassant(board, myPosition, currentRow, nextCol, nextPosition, moves);
                }
            }
        }
    }

    private static void addNormalCaptures(ChessBoard board, ChessPosition myPosition,
                                          ChessGame.TeamColor teamColor, ArrayList<ChessMove> moves,
                                          ChessPosition nextPosition, int nextRow) {
        if (board.getPiece(nextPosition).getTeamColor() != teamColor) { // and is on opposing team
            // then you can capture it

            // capture with promotion
            if (isLastRow(nextRow)) {
                addPromotionMoves(myPosition, nextPosition, moves);
            } else {
                // normal capture
                moves.add(new ChessMove(myPosition, nextPosition, null));
            }
        }
    }

    private static void addEnPassant(ChessBoard board, ChessPosition myPosition, int currentRow,
                                     int nextCol, ChessPosition nextPosition,
                                     ArrayList<ChessMove> moves) {
        // Check for en passant: check the last move for a pawn which moved 2 spaces right next to us
        GameLog log = board.getGameLog();
        GameLog.LogEntry lastLogEntry = log.getLastMove();
        if (lastLogEntry != null) { // make sure we aren't checking an empty log
            ChessPosition lastEndPosition = lastLogEntry.move().getEndPosition();
            int lastRow = lastEndPosition.getRow();
            int lastCol = lastEndPosition.getColumn();
            if (lastRow == currentRow && lastCol == nextCol) {// piece is directly left or right of us
                ChessPiece.PieceType lastPieceType = lastLogEntry.piece().getPieceType();
                if (lastPieceType == ChessPiece.PieceType.PAWN) { // piece is a pawn
                    ChessPosition lastStartPosition = lastLogEntry.move().getStartPosition();
                    int lastStartRow = lastStartPosition.getRow();
                    if (Math.abs(lastRow - lastStartRow) == 2) {
                        ChessMove move = new ChessMove(myPosition, nextPosition, null);
                        moves.add(move);
                    }
                }
            }
        }
    }

    private static void addForwardMoves(ChessBoard board, ChessPosition myPosition,
                                        ChessPosition nextPosition, int nextRow,
                                        ArrayList<ChessMove> moves, int direction, int currentCol,
                                        ChessGame.TeamColor teamColor, int currentRow) {
        if (board.getPiece(nextPosition) == null) { // next space must be empty
            // check if we can move forward, but not to final row
            if (nextRow > 1 && nextRow < 8) { // pawn stays in middle of the board
                moves.add(new ChessMove(myPosition, nextPosition, null));
            } else if (isLastRow(nextRow)) { //we are near the final row
                addPromotionMoves(myPosition, nextPosition, moves);
            }


            // we can move twice if we are in starting position and space 2 ahead is free
            int rowTwoAhead = nextRow + direction;
            if (rowTwoAhead <= 8 && rowTwoAhead >= 1) {
                ChessPosition nextnextPosition = new ChessPosition(rowTwoAhead, currentCol);
                if (board.getPiece(nextnextPosition) == null) {
                    if (teamColor == ChessGame.TeamColor.WHITE && currentRow == 2) {
                        moves.add(new ChessMove(myPosition, nextnextPosition, null));
                    } else if (teamColor == ChessGame.TeamColor.BLACK && currentRow == 7) {
                        moves.add(new ChessMove(myPosition, nextnextPosition, null));
                    }
                }
            }
        }
    }

    private static void addPromotionMoves(ChessPosition myPosition, ChessPosition nextPosition, ArrayList<ChessMove> moves) {
        // we need a separate move for every possible promotion piece
        for (ChessPiece.PieceType promotionPiece : ChessPiece.PieceType.values()) {
            if (promotionPiece != ChessPiece.PieceType.PAWN && promotionPiece != ChessPiece.PieceType.KING) { // cannot promote to pawn
                moves.add(new ChessMove(myPosition, nextPosition, promotionPiece));
            }
        }
    }

    private static boolean isLastRow(int nextRow) {
        return nextRow == 1 || nextRow == 8;
    }

    public Boolean moveIsEnPassant(ChessBoard board, ChessMove move) {
        ChessPosition startPosition = move.getStartPosition();
        ChessPosition endPosition = move.getEndPosition();
        ChessPiece.PieceType pieceType = board.getPiece(startPosition).getPieceType();

        if (pieceType == ChessPiece.PieceType.PAWN) { // Double check that the piece is a pawn,
            int startCol = startPosition.getColumn();
            int endCol = endPosition.getColumn();
            if (startCol != endCol) { // pawn moves diagonally,
                if (board.getPiece(endPosition) == null) { // but there is no piece diagonal to the pawn
                    return Boolean.TRUE;
                }
            }
        }

        return Boolean.FALSE;
    }


    public ChessPosition getEnPassantCapturePosition(ChessMove move) {
        ChessPosition startPosition = move.getStartPosition();
        ChessPosition endPosition = move.getEndPosition();
        int startRow = startPosition.getRow();
        int endCol = endPosition.getColumn();
        return new ChessPosition(startRow, endCol);
    }

}



