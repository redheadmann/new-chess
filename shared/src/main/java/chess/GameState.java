package chess;

import java.util.Objects;

public class GameState {
    private ChessGame.TeamColor teamTurn;

    public GameState () {
        this.teamTurn = ChessGame.TeamColor.WHITE; // game starts off with white to move
    }


    /*
        Getters and Setters
     */
    public ChessGame.TeamColor teamTurn() {
        return teamTurn;
    }

    /**
     *
     * @param teamColor the team color to give the turn
     */
    public void setTeamTurn(ChessGame.TeamColor teamColor) {
        teamTurn = teamColor;
    }

    private void updateColor() {
        if (this.teamTurn == ChessGame.TeamColor.WHITE) {
            this.teamTurn = ChessGame.TeamColor.BLACK;
        } else {
            this.teamTurn = ChessGame.TeamColor.WHITE;
        }
    }

    public void updateTurn() {
        this.updateColor();
    }

    /**
     * Check if the piece moving is on the team whose turn it is
     *
     * @param move the potential move
     * @return whether this piece can move right now
     */
    public Boolean moveIsInTurn(ChessMove move, ChessBoard board) {
        ChessPiece piece = board.getPiece(move.getStartPosition());
        ChessGame.TeamColor teamColor = piece.getTeamColor();
        if (teamColor == this.teamTurn) {return Boolean.TRUE;}
        else {return Boolean.FALSE;}
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GameState gameState = (GameState) o;
        return teamTurn == gameState.teamTurn;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(teamTurn);
    }
}
