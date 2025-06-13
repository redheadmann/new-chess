package dataaccess;

import chess.ChessGame;
import chess.InvalidMoveException;
import sharedexception.UnauthorizedException;
import model.GameData;

import java.util.List;
import java.util.Objects;

public interface GameDAO {
    GameData createGame(String gameName) throws DataAccessException;
    GameData getGame(int gameID) throws DataAccessException;
    List<GameData> listGames() throws DataAccessException;
    void updateGame(String username, ChessGame.TeamColor playerColor, int gameID) throws DataAccessException;
    void clear() throws DataAccessException;
    void makeMove(int gameID, ChessGame game) throws InvalidMoveException, DataAccessException;
    void leaveGame(String username, int gameID) throws DataAccessException, UnauthorizedException;

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

    default GameData getNewGameData(String username, GameData oldGame) {
        // Find usernames to make null
        String whiteUsername = oldGame.whiteUsername();
        String blackUsername = oldGame.blackUsername();
        GameData newGameData = oldGame;
        if (Objects.equals(whiteUsername, username) && Objects.equals(blackUsername, username)) {
            newGameData = new GameData(oldGame.gameID(), null, null,
                    oldGame.gameName(), oldGame.game());
        } else if (Objects.equals(whiteUsername, username)) {
            newGameData = new GameData(oldGame.gameID(), null, blackUsername,
                    oldGame.gameName(), oldGame.game());
        } else if (Objects.equals(blackUsername, username)) {
            newGameData = new GameData(oldGame.gameID(), whiteUsername, null,
                    oldGame.gameName(), oldGame.game());
        }
        return newGameData;
    }
}
