package calculators;

import java.util.ArrayList;
import java.util.Collection;
import chess.*;

public interface PieceMovesCalculator {
    Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition);


    default void moveUntilBlocked(ChessBoard board, ChessPosition myPosition, int rowDirection,
                                  int colDirection, ArrayList<ChessMove> moves) {
        // find current information
        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        ChessGame.TeamColor teamColor = board.getPiece(myPosition).getTeamColor();

        for (int i = 1; i <= 8; i++) { // at most 8 spaces to  move
            //find next space to check
            int possibleRow = row + i*rowDirection;
            int possibleCol = col + i*colDirection;
            ChessPosition nextPosition = new ChessPosition(possibleRow, possibleCol);

            // if endPosition is still on the board
            if (possibleRow >= 1 && possibleRow <= 8 && possibleCol >= 1 && possibleCol <= 8) {
                // check for a piece already at the end position
                ChessPiece pieceOnPosition = board.getPiece(new ChessPosition(possibleRow, possibleCol));
                if (pieceOnPosition != null) {
                    // check if piece on end position is of the same color
                    if (pieceOnPosition.getTeamColor() != teamColor) { // the piece is on the opposing team and we can capture it
                        moves.add(new ChessMove(myPosition, nextPosition, null));
                    }
                    break;
                } else { // we can move to any empty space
                    moves.add(new ChessMove(myPosition, nextPosition, null));
                }
            } else {
                break; //we have left the board
            }
        }
    }
}
