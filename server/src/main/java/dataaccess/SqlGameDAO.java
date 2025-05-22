package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;

import java.sql.SQLException;
import java.util.ArrayList;
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
        // Insert into database
        String statement = "INSERT INTO game (gameID, gameData) VALUES (?,?)";
        executeUpdate(statement, gameID, game);
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

        // create new GameData model and insert in old position
        GameData newGame = new GameData(oldGame.gameID(), newWhiteUsername, newBlackUsername,
                oldGame.gameName(), oldGame.game());
        // Insert into database
        String statement = "UPDATE game SET gameData=? WHERE gameID=?";
        executeUpdate(statement, newGame, gameID);
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
                    else if (param instanceof GameData p) {
                        String json = new Gson().toJson(p);
                        ps.setString(i + 1, json);
                    }
                    else if (param == null) {
                        ps.setNull(i + 1, NULL);
                    }
                }
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("Error: unable to update database: %s, %s", statement, e.getMessage()));
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
            throw new DataAccessException(String.format("Error: unable to configure database: %s", ex.getMessage()));
        }
    }
}
