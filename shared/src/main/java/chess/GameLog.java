package chess;

import java.util.LinkedList;
import java.util.Objects;

public class GameLog {
    public record LogEntry(ChessMove move, ChessPiece piece) { }
    LinkedList<LogEntry> moves = new LinkedList<>();


    public void addMove(ChessMove move, ChessPiece piece) {
        moves.add(new LogEntry(move, piece));
    }

    public LogEntry getLastMove() {
        return moves.peekLast(); // returns null if there are no moves in the log
    }

    public LinkedList<LogEntry> getAllMoves() {
        return moves;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameLog gameLog = (GameLog) o;
        return Objects.equals(moves, gameLog.moves);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(moves);
    }
}

