package calculators;

import java.util.ArrayList;
import java.util.Collection;
import chess.*;

public class BishopMovesCalculator implements PieceMovesCalculator{

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ArrayList<ChessMove> moves = new ArrayList<>();

        /* We go out in one direction at a time and stop at the edge of a board
          or when we encounter another piece
        */
        for (int rowDirection: new int[]{-1,1}) { // iterate at most 7 times
            for  (int colDirection: new int[]{-1,1}) {
                moveUntilBlocked(board, myPosition, rowDirection, colDirection, moves);
            }
        }

        return moves;
    }

}
