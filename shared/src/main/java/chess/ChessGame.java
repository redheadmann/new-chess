package chess;

import java.util.Collection;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private ChessBoard board; // the board on which to play
    private final GameState gameState;
    private final ChessRuleBook ruleBook = new ChessRuleBook();

    public ChessGame() {
        board = new ChessBoard();
        board.resetBoard(); // start off with the default board
        gameState = new GameState(); // always keep track of the game state
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return gameState.teamTurn();
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        gameState.setTeamTurn(team);
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Get the other team's color
     *
     * @param teamColor the color of the team we know
     * @return the color of the opposing team
     */
    public static TeamColor getOtherColor(TeamColor teamColor) {
        if (teamColor == TeamColor.WHITE) {return TeamColor.BLACK;}
        return TeamColor.WHITE;
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        return ruleBook.validMoves(this.board, startPosition);
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        // Check if game is over
        if (gameState.isGameOver()) {
            throw new InvalidMoveException("Game is over");
        }

        // Check for piece
        ChessPosition moveStartPosition = move.getStartPosition();
        Collection<ChessMove> validMoves = validMoves(moveStartPosition);

        // If validMoves is null, there was no piece at the given position
        if (validMoves == null) {throw new InvalidMoveException("Move given was invalid");}
        // If there is a piece at the position, but it is not its turn, throw an exception
        if (!gameState.moveIsInTurn(move, board)) {
            throw new InvalidMoveException("It is not this color's turn");
        }

        // If move is valid, put it in place and update whose turn it is
        if (validMoves.contains(move)) {
            gameState.updateTurn();
            ChessPiece oldPiece = board.getPiece(moveStartPosition);
            board.movePiece(move);
            // add the move to the game log
            board.getGameLog().addMove(move, oldPiece);
        } else {
            throw new InvalidMoveException("Move given was invalid");
        }

        // If move went through, check if the game is over
        checkForGameOver();
    }

    private void checkForGameOver() {
        // Find whose turn it is
        TeamColor movingColor = gameState.teamTurn();
        TeamColor otherColor;
        if (movingColor == TeamColor.WHITE) {
            otherColor = TeamColor.BLACK;
        } else {
            otherColor = TeamColor.WHITE;
        }


        // Check for stalemate
        if (isInStalemate(movingColor)) {
            gameState.setGameIsOver(Boolean.TRUE);
            gameState.setWinner(GameState.Winner.DRAW);
        }
        // Check for checkmate
        else if (isInCheckmate(movingColor)) {
            gameState.setGameIsOver(Boolean.TRUE);
            if (otherColor == TeamColor.WHITE) {
                gameState.setWinner(GameState.Winner.BLACK);
            } else {
                gameState.setWinner(GameState.Winner.WHITE);
            }
        }
    }

    public boolean gameIsOver() {
        return gameState.isGameOver();
    }

    public GameState.Winner getWinner() {
        return gameState.getWinner();
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        return ruleBook.isInCheck(this.getBoard(), teamColor);
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        return ruleBook.isInCheckmate(this.getBoard(), teamColor);
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        return ruleBook.isInStalemate(this.getBoard(), teamColor);
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return this.board;
    }


    public void resign(TeamColor color) {
        // Set game is over flag and determine the winner
        gameState.setGameIsOver(Boolean.TRUE);
        if (color == TeamColor.WHITE) {
            gameState.setWinner(GameState.Winner.BLACK);
        } else {
            gameState.setWinner(GameState.Winner.WHITE);
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return Objects.equals(board, chessGame.board) && Objects.equals(gameState, chessGame.gameState);
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, gameState);
    }
}
