package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.sql.Types.NULL;

public class SqlGameDAO extends SqlDAO implements GameDAO {
    public SqlGameDAO() throws DataAccessException {
        String[] createStatements = {
                """
            CREATE TABLE IF NOT EXISTS game (
              `gameID` INT NOT NULL,
              `gameData` longtext DEFAULT NULL,
              PRIMARY KEY (`gameID`),
              INDEX(gameID)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """
        };
        configureDatabase(createStatements);
    }

    @Override
    public GameData createGame(String gameName) throws DataAccessException {
        // Create a unique id
        Random random = new Random();
        // try a maximum of 100 times to generate a new gameID
        int gameID = 1234;
        for (int i=0; i < 101; i++) {
            if (i == 100) {
                throw new DataAccessException("Error: cannot create game, too many games on server");
            }

            // Check for gameID in the database. End the loop when our ID is unique
            String statement = "SELECT gameID FROM game WHERE gameID=?";
            try (var conn = DatabaseManager.getConnection()) {
                try (var ps = conn.prepareStatement(statement)) {
                    ps.setInt(1, gameID);
                    var rs = ps.executeQuery();
                    // If the query result is empty, break from the loop
                    if (!rs.next()) {
                        break;
                    }
                }
            } catch (SQLException e) {
                throw new DataAccessException(String.format("Error: unable to update database: %s, %s", statement, e.getMessage()));
            }
            // Update gameID
            gameID = 1000 + random.nextInt(9000);
        }

        // Create new game GameData object
        GameData game = new GameData(gameID, null, null, gameName, new ChessGame());
        String gameJson = new Gson().toJson(game);
        // Insert into database
        String statement = "INSERT INTO game (gameID, gameData) VALUES (?,?)";
        executeUpdate(statement, gameID, gameJson);
        // Return the game object
        return game;
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        // Check for gameID in the database
        String statement = "SELECT gameData FROM game WHERE gameID=?";
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement)) {
                ps.setInt(1, gameID);
                var rs = ps.executeQuery();
                // If the query result is empty, break from the loop
                if (rs.next()) {
                    String json = rs.getString("gameData");
                    return (GameData) new Gson().fromJson(json, GameData.class);
                } else {
                    // The game does not exist
                    throw new DataAccessException("Error: bad request");
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("Error: unable to update database: %s, %s", statement, e.getMessage()));
        }
    }

    @Override
    public List<GameData> listGames() throws DataAccessException {
        //Build a list
        ArrayList<GameData> gameList = new ArrayList<>();
        // Add GameData objects from the table
        String statement = "SELECT gameData FROM game";
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement)) {
                var rs = ps.executeQuery();
                // If the query result is empty, break from the loop
                while (rs.next()) {
                    String json = rs.getString("gameData");
                    GameData gameData = new Gson().fromJson(json, GameData.class);
                    gameList.add(gameData);
                }
            }
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException(String.format("Error: unable to update database: %s, %s", statement, e.getMessage()));
        }
        return gameList;
    }

    @Override
    public void updateGame(String username, ChessGame.TeamColor playerColor, int gameID) throws DataAccessException {
        // Copy old game data
        GameData oldGame = this.getGame(gameID);

        // Find new usernames
        String[] names = calculateUsernames(username, playerColor, oldGame);
        String newWhiteUsername = names[0];
        String newBlackUsername = names[1];

        // create new GameData model and serialize it
        GameData newGame = new GameData(oldGame.gameID(), newWhiteUsername, newBlackUsername,
                oldGame.gameName(), oldGame.game());
        String gameJson = new Gson().toJson(newGame);
        // Insert into database
        String statement = "UPDATE game SET gameData=? WHERE gameID=?";
        executeUpdate(statement, gameJson, gameID);
    }

    @Override
    public void clear() throws DataAccessException {
        // Clear the entire table with TRUNCATE
        String statement = "TRUNCATE TABLE game";
        try {
            executeUpdate(statement);
        } catch (DataAccessException e) {
            throw new DataAccessException("Error clearing database: ", e);
        }
    }

}
