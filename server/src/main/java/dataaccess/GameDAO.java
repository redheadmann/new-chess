package dataaccess;

import chess.ChessGame;
import chess.InvalidMoveException;
import model.GameData;

import java.util.List;

public interface GameDAO {
    GameData createGame(String gameName) throws DataAccessException;
    GameData getGame(int gameID) throws DataAccessException;
    List<GameData> listGames() throws DataAccessException;
    void updateGame(String username, ChessGame.TeamColor playerColor, int gameID) throws DataAccessException;
    void clear() throws DataAccessException;
    boolean makeMove(int gameID, ChessGame game) throws InvalidMoveException, DataAccessException;


    default String[] calculateUsernames(String username, ChessGame.TeamColor playerColor,
                                        GameData oldGame) throws DataAccessException{
        // update correct username based on player color, ensuring name is not taken
        String newWhiteUsername;
        String newBlackUsername;
        if (playerColor == ChessGame.TeamColor.WHITE) {
            if (oldGame.whiteUsername() != null) {
                throw new DataAccessException("Error: already taken");
            }
            newWhiteUsername = username;
            newBlackUsername = oldGame.blackUsername();
        } else {
            if (oldGame.blackUsername() != null) {
                throw new DataAccessException("Error: already taken");
            }
            newWhiteUsername = oldGame.whiteUsername();
            newBlackUsername = username;
        }

        return new String[]{newWhiteUsername, newBlackUsername};
    }
}
