package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.util.*;

public class MemoryGameDAO implements  GameDAO {

    private final HashMap<Integer, GameData> data = new HashMap<>();

    @Override
    public GameData createGame(String gameName) throws DataAccessException {
        // Create a unique id
        Random random = new Random();
        // try a maximum of 100 times to generate a new gameID
        int gameID = 1234;
        for (int i=0; i < 101; i++) {
            if (i == 100) {
                throw new DataAccessException("Cannot create game, too many games on server");
            }
            if (!data.containsKey(gameID)) { // break from loop if new gameID is unique
                break;
            }
            gameID = 1000 + random.nextInt(9000);
        }

        GameData game = new GameData(gameID, null, null, gameName, new ChessGame());
        data.put(gameID, game);
        return game;
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        GameData gameData = data.get(gameID);
        if (gameData == null) {
            throw new DataAccessException("Error: bad request");
        }
        return gameData;
    }

    @Override
    public List<GameData> listGames() throws DataAccessException {
        return new ArrayList<>(data.values());
    }

    @Override
    public void updateGame(String username, ChessGame.TeamColor playerColor, int gameID, ChessGame game) throws DataAccessException {
        // copy old game data
        GameData oldGame = this.getGame(gameID);

        // Find new usernames
        String[] names = calculateUsernames(username, playerColor, oldGame);
        String newWhiteUsername = names[0];
        String newBlackUsername = names[1];

        // create new GameData model and insert in old position
        GameData newGame = new GameData(oldGame.gameID(), newWhiteUsername, newBlackUsername,
                oldGame.gameName(), oldGame.game());

        data.put(gameID, newGame);
    }

    @Override
    public void clear() throws DataAccessException {
        data.clear();
    }
}
