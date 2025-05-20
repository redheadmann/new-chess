package dataaccess;

import model.AuthData;

import com.google.gson.Gson;
import java.sql.SQLException;
import java.util.UUID;

import static java.sql.Types.NULL;

public class SqlAuthDAO implements AuthDAO {
    public SqlAuthDAO() throws DataAccessException {
        configureDatabase();
    }

    @Override
    public AuthData createAuth(String username) {
        // Create data, including random authToken
        String authToken = UUID.randomUUID().toString();
        AuthData authData = new AuthData(username, authToken);
        // Serialize the authData for data tables
        String serializedData = new Gson().toJson(authData);

        // Update the tables
        String statement = "INSERT INTO auth (authToken, authData) VALUES (?, ?)";

        try {
            executeUpdate(statement, authToken, serializedData);
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }

        return authData;
        }


    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        String statement = "SELECT authData FROM auth WHERE authToken=?";
        AuthData authData;
        // Connect to the database and  extract a match for the given token
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement)) {
                // Set the authToken in our table query statement
                ps.setString(1, authToken);
                try (var rs = ps.executeQuery()) {
                    // If result is not null, process it. Otherwise, throw an exception
                    if (rs.next()) {
                        // Deserialize the authData object
                        String serializedData = rs.getString("authData");
                        if (serializedData == null) {
                            throw new DataAccessException("authData is NULL in the database");
                        }
                        authData = new Gson().fromJson(serializedData, AuthData.class);
                    } else {
                        throw new DataAccessException("Cannot getAuth: authToken is not in database");
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Database error: " + e.getMessage(), e);
        }

        return authData;
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        String statement = "DELETE FROM auth WHERE authToken=?";
        // Delete
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, authToken);
                int rowsAffected = ps.executeUpdate();
                if (!(rowsAffected == 1)) {
                    throw new DataAccessException("Cannot getAuth: authToken is not in database");
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("unable to update database: %s, %s", statement, e.getMessage()));
        }
    }

    @Override
    public void clear() {
        // Clear the entire table with TRUNCATE
        String statement = "TRUNCATE TABLE auth";
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
            CREATE TABLE IF NOT EXISTS auth (
              `authToken` varchar(256) NOT NULL,
              `authData` longtext DEFAULT NULL,
              PRIMARY KEY (`authToken`),
              INDEX(authToken)
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
