package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;

import java.sql.SQLException;
import java.util.List;
import java.util.Random;

import static java.sql.Types.NULL;

public class SqlGameDAO implements GameDAO {
    public SqlGameDAO() throws DataAccessException {
        configureDatabase();
    }

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
                throw new DataAccessException(String.format("unable to update database: %s, %s", statement, e.getMessage()));
            }
            // Update gameID
            gameID = 1000 + random.nextInt(9000);
        }

        // Create new game GameData object
        GameData game = new GameData(gameID, null, null, gameName, new ChessGame());
        // Insert into database
        String statement = "INSERT INTO game (gameID, gameData) VALUES (?,?)";
        executeUpdate(statement, gameID, game);
        // Return the game object
        return game;
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        // Check for gameID in the database
        String statement = "SELECT gameID FROM game WHERE gameID=?";
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement)) {
                ps.setInt(1, gameID);
                var rs = ps.executeQuery();
                // If the query result is empty, break from the loop
                if (rs.next()) {
                    String json = rs.getString("gameData");
                    GameData gameData = (GameData) new Gson.fromJson(json, GameData.class);
                    return gameData;
                } else {
                    // The game does not exist
                    throw new DataAccessException("Error: bad request");
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("unable to update database: %s, %s", statement, e.getMessage()));
        }
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
        // Clear the entire table with TRUNCATE
        String statement = "TRUNCATE TABLE game";
        try {
            executeUpdate(statement);
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void executeUpdate(String statement, Object... params) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement)) {
                for (var i = 0; i < params.length; i++) {
                    var param = params[i];
                    if (param instanceof String p) {
                        ps.setString(i + 1, p);
                    }
                    else if (param instanceof Integer p) {
                        ps.setInt(i + 1, p);
                    }
                    else if (param == null) {
                        ps.setNull(i + 1, NULL);
                    }
                }
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("unable to update database: %s, %s", statement, e.getMessage()));
        }
    }

    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS game (
              `gameID` INT NOT NULL,
              `gameData` longtext DEFAULT NULL,
              PRIMARY KEY (`gameID`),
              INDEX(gameID)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """
    };

    private void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase(); // create database if it doesn't exist
        try (var conn = DatabaseManager.getConnection()) {
            for (var statement : createStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException(String.format("Unable to configure database: %s", ex.getMessage()));
        }
    }
}
