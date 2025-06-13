package dataaccess;

import sharedexception.UnauthorizedException;
import model.AuthData;

import com.google.gson.Gson;

import java.sql.SQLException;
import java.util.UUID;

public class SqlAuthDAO extends SqlDAO implements AuthDAO {

    public SqlAuthDAO() throws DataAccessException {
        String[] createStatements = {
                """
            CREATE TABLE IF NOT EXISTS auth (
              `authToken` varchar(256) NOT NULL,
              `authData` longtext DEFAULT NULL,
              PRIMARY KEY (`authToken`),
              INDEX(authToken)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """
        };
        configureDatabase(createStatements);
    }

    @Override
    public AuthData createAuth(String username) throws DataAccessException {
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
            throw new DataAccessException("Error creating authorization: ",e);
        }

        return authData;
        }


    @Override
    public AuthData getAuth(String authToken) throws DataAccessException, UnauthorizedException {
        String statement = "SELECT authData FROM auth WHERE authToken=?";
        AuthData authData;
        // Connect to the database and  extract a match for the given token
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(statement)) {
            // Set the authToken in our table query statement
            ps.setString(1, authToken);
            try (var rs = ps.executeQuery()) {
                // If we don't find a matching authToken, throw unauthorized error
                if (!rs.next()) {
                    throw new UnauthorizedException("Error: unauthorized");
                }
                // Otherwise, deserialize the data, make sure it exists, and return
                String serializedData = rs.getString("authData");
                if (serializedData == null) {
                    throw new DataAccessException("Error: authData is NULL in the database");
                }
                return new Gson().fromJson(serializedData, AuthData.class);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Database error: " + e.getMessage(), e);
        }
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
                    throw new DataAccessException("Error: unauthorized");
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("Error: unable to update database: %s, %s", statement, e.getMessage()));
        }
    }

    @Override
    public void clear() throws DataAccessException {
        // Clear the entire table with TRUNCATE
        String statement = "TRUNCATE TABLE auth";
        try {
            executeUpdate(statement);
        } catch (DataAccessException e) {
            throw new DataAccessException("Error clearing database: ",e);
        }
    }

}
