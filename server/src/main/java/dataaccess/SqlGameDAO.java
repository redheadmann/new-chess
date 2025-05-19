package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.util.List;

public class SqlGameDAO implements GameDAO {
    @Override
    public GameData createGame(String gameName) throws DataAccessException {
        return null;
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        return null;
    }

    @Override
    public List<GameData> listGames() {
        return List.of();
    }

    @Override
    public void updateGame(String username, ChessGame.TeamColor playerColor, int gameID) throws DataAccessException {

    }

    @Override
    public void clear() {

    }
}
