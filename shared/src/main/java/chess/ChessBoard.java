package chess;

import calculators.PawnMovesCalculator;

import java.util.*;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {
    private ChessPiece[][] squares = new ChessPiece[8][8];
    private GameLog gameLog = new GameLog();

    public ChessBoard() {
    }



    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        squares[position.getRow()-1][position.getColumn()-1] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return squares[position.getRow()-1][position.getColumn()-1]; // we call the first row 'row 1'
    }

    /**
     * Moves a single chess piece
     *
     * @param move to implement, should know beforehand it is valid
     */
    public void movePiece(ChessMove move) {
        ChessPosition startPosition = move.getStartPosition();
        ChessPosition endPosition = move.getEndPosition();

        // In case our piece captures an opposing piece, remove the board's reference to it
        // if the piece is a pawn, check for en passant
        ChessPiece.PieceType pieceType = this.getPiece(startPosition).getPieceType();
        ChessPosition opposingPosition;
        if (pieceType == ChessPiece.PieceType.PAWN) {
            PawnMovesCalculator pawnCalculator = new PawnMovesCalculator();
            if (pawnCalculator.moveIsEnPassant(this, move)) {
                opposingPosition = pawnCalculator.getEnPassantCapturePosition(move);
            } else {
                opposingPosition = endPosition;
            }
        } else {
            opposingPosition = endPosition;
        }
        // Remove the potential opposing piece
        ChessPiece possiblePiece = this.getPiece(opposingPosition); // could be an opposing piece
        if (possiblePiece != null) {
            this.addPiece(opposingPosition, null);
        }


        // Move the piece
        ChessPiece movingPiece = getPiece(startPosition); // get piece which is moving
        ChessPiece.PieceType promotionPieceType = move.getPromotionPiece(); // promotion type
        if (promotionPieceType != null) { // if promoting the piece, create a new chess piece
            ChessGame.TeamColor pieceColor = movingPiece.getTeamColor();
            addPiece(startPosition, null);
            addPiece(endPosition, new ChessPiece(pieceColor, promotionPieceType));
        } else {
            addPiece(startPosition, null); // remove reference to moving piece at its starting position
            addPiece(endPosition, movingPiece);
        }

    }



    /** Find the king of a specific team
     *
     * @param teamColor the color of the king to find
     * @return the position of the correct king
     */
    public ChessPosition findKing(ChessGame.TeamColor teamColor) {
        ChessPosition king = null;

        // for each piece on the team, check if it is the king
        for (BoardIterator<ChessPosition> iterator = this.iterator(teamColor); iterator.hasNext();) {
            ChessPosition position = iterator.next();
            ChessPiece piece = this.getPiece(position);

            if (piece.getPieceType() == ChessPiece.PieceType.KING) { // if this is the king
                king = position;
                break;
            }
        }

        return king;
    }


    /** Creates a special board iterator, which gives the positions of all the pieces on
     * one team
     *
     * @param teamColor the color of piece whose positions will be stored in the iterator
     * @return special board iterator, which gives positions of the pieces on one team
     */
    public BoardIterator<ChessPosition> iterator(ChessGame.TeamColor teamColor) {
        return new BoardIterator<>(teamColor);
    }

    public GameLog getGameLog() {
        return gameLog;
    }


    /** Iterates over each piece of the given color
     *
     * @param <T> T should always be ChessPosition
     */
    public class BoardIterator<T> implements Iterator<ChessPosition> {
        private final Queue<ChessPosition> placements;
        /**
         * Initialize the Iterator with a queue of chess pieces on the board
         */
        public BoardIterator(ChessGame.TeamColor teamColor) {
            placements = new LinkedList<>();
            // Check each square on the board
            for (int row = 1; row <= 8; row++) {
                for (int col = 1; col <= 8; col++) {
                    ChessPosition position = new ChessPosition(row, col);
                    ChessPiece chessPiece = getPiece(position);
                    addCorrectPiece(teamColor, chessPiece, position);
                }
            }
        }

        private void addCorrectPiece(ChessGame.TeamColor teamColor, ChessPiece chessPiece,
                                     ChessPosition position) {
            if (chessPiece != null) { // If there is a piece
                if (chessPiece.getTeamColor() == teamColor) {  // and piece is of the correct color
                    placements.add(position); // add the position to the queue
                }
            }
        }

        @Override
        public boolean hasNext() {
            return !placements.isEmpty();
        }

        @Override
        public ChessPosition next() {
            if (!hasNext()) {
                throw new NoSuchElementException("Chess Board Iterator has no more pieces");
            }
            return placements.remove();
        }
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        this.squares = new ChessPiece[8][8];

        HashMap<Integer, ChessPiece.PieceType> colToPiece = new HashMap<>();
        colToPiece.put(1, ChessPiece.PieceType.ROOK);
        colToPiece.put(2, ChessPiece.PieceType.KNIGHT);
        colToPiece.put(3, ChessPiece.PieceType.BISHOP);
        colToPiece.put(4, ChessPiece.PieceType.QUEEN);
        colToPiece.put(5, ChessPiece.PieceType.KING);
        colToPiece.put(6, ChessPiece.PieceType.BISHOP);
        colToPiece.put(7, ChessPiece.PieceType.KNIGHT);
        colToPiece.put(8, ChessPiece.PieceType.ROOK);


        // put in rows of pawns
        for (int col = 1; col <= 8; col++) {
            ChessPosition whitePawn = new ChessPosition(2, col);
            this.addPiece(whitePawn, new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN));
            ChessPosition whiteSpecial = new ChessPosition(1, col);
            this.addPiece(whiteSpecial, new ChessPiece(ChessGame.TeamColor.WHITE, colToPiece.get(col)));


            ChessPosition blackPawn = new ChessPosition(7, col);
            this.addPiece(blackPawn, new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN));
            ChessPosition blackSpecial = new ChessPosition(8, col);
            this.addPiece(blackSpecial, new ChessPiece(ChessGame.TeamColor.BLACK, colToPiece.get(col)));
        }

        // reset game log
        this.gameLog = new GameLog();
    }




    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                if (this.getPiece(new ChessPosition(row, col)) == null) {
                    str.append("| ");
                } else {
                    str.append("|").append(this.getPiece(new ChessPosition(row, col)).toString());
                }
            }
            str.append("|/n");
        }
        return str.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessBoard that = (ChessBoard) o;
        return Objects.deepEquals(squares, that.squares);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(squares);
    }
}
