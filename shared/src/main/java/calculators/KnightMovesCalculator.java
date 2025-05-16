package calculators;

import java.util.ArrayList;
import java.util.Collection;
import chess.*;

public class KnightMovesCalculator implements PieceMovesCalculator{


    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ArrayList<ChessMove> moves = new ArrayList<>();
        int currentRow = myPosition.getRow();
        int currentCol = myPosition.getColumn();

        ChessPiece knight = board.getPiece(myPosition);
        ChessGame.TeamColor teamColor = knight.getTeamColor();

        // move to all 8 spaces
        for (int rowMovement: new int[]{-2,-1,1,2}) {
            int[] colMovements = new int[]{-(3-Math.abs(rowMovement)), 3-Math.abs(rowMovement)};
            for (int colMovement: colMovements) {
                //calculate next position based off of movement
                int nextCol = currentCol + colMovement;
                int nextRow = currentRow + rowMovement;
                ChessPosition nextPosition = new ChessPosition(nextRow, nextCol);

                addPossibleMoves(board, myPosition, nextRow, nextCol, nextPosition, teamColor, moves);

            }
        }


        return moves;
    }

    private static void addPossibleMoves(ChessBoard board, ChessPosition myPosition,
                                         int nextRow, int nextCol, ChessPosition nextPosition,
                                         ChessGame.TeamColor teamColor, ArrayList<ChessMove> moves) {
        if (nextRow >=1 && nextRow <=8 && nextCol >=1 && nextCol <=8) { // still on the board
            ChessPiece pieceInWay = board.getPiece(nextPosition);
            if (pieceInWay != null) {
                if (pieceInWay.getTeamColor() != teamColor) {
                    moves.add(new ChessMove(myPosition, nextPosition, null));
                }
            } else {
                moves.add(new ChessMove(myPosition, nextPosition, null));
            }
        }
    }
}
