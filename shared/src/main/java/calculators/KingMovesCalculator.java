package calculators;

import java.util.ArrayList;
import java.util.Collection;
import chess.*;

public class KingMovesCalculator implements PieceMovesCalculator{

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ArrayList<ChessMove> moves = new ArrayList<>();
        int row = myPosition.getRow();
        int col = myPosition.getColumn();

        ChessPiece king = board.getPiece(myPosition);
        ChessGame.TeamColor teamColor = king.getTeamColor();

        /* We go out in one direction at a time and stop at the edge of a board
          or when we encounter another piece
        */
        for (int rowDirection: new int[]{-1,0,1}) { // iterate at most 7 times
            for  (int colDirection: new int[]{-1,0,1}) {
                //make sure direction is not (0, 0)
                if (!(rowDirection == 0 && colDirection == 0)) {
                    int possibleRow = row + rowDirection;
                    int possibleCol = col + colDirection;
                    ChessPosition nextPosition = new ChessPosition(possibleRow, possibleCol);

                    // if endPosition is still on the board
                    addPossibleMove(board, myPosition, possibleRow, possibleCol, teamColor, moves, nextPosition);
                }
            }
        }


        return moves;
    }

    private static void addPossibleMove(ChessBoard board, ChessPosition myPosition,
                                        int possibleRow, int possibleCol,
                                        ChessGame.TeamColor teamColor, ArrayList<ChessMove> moves,
                                        ChessPosition nextPosition) {
        if (possibleRow >= 1 && possibleRow <= 8 && possibleCol >= 1 && possibleCol <= 8) {
            // check for a piece already at the end position
            ChessPiece pieceOnPosition = board.getPiece(new ChessPosition(possibleRow, possibleCol));
            if (pieceOnPosition != null) {
                // check if piece on end position is of the same color
                if (pieceOnPosition.getTeamColor() != teamColor) { // the piece is on the opposing team and we can capture it
                    moves.add(new ChessMove(myPosition, nextPosition, null));
                }
            } else { // we can move to any empty space
                    moves.add(new ChessMove(myPosition, nextPosition, null));
            }
        }
    }


}
