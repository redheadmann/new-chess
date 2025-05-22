package dataaccess;

import com.google.gson.Gson;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import javax.xml.crypto.Data;
import java.sql.SQLException;

import static java.sql.Types.NULL;

public class SqlUserDAO implements UserDAO {
    public SqlUserDAO() throws DataAccessException {
        configureDatabase();
    }

    @Override
    public void createUser(UserData userData) throws DataAccessException {
        //Hash password in advance
        String clearTextPassword = userData.password();
        String hashedPassword = BCrypt.hashpw(clearTextPassword, BCrypt.gensalt());
        UserData newUserData = new UserData(userData.username(), hashedPassword, userData.email());

        // Check if username is taken
        String username = newUserData.username();
        String statement = "SELECT userData FROM user WHERE username=?";
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, username);
                var rs = ps.executeQuery();
                // If we have a match, then throw an error
                if (rs.next()) {
                    throw new DataAccessException("Error: already taken");
                } else {
                    // Otherwise, we can add a new username to our table
                    statement = "INSERT INTO user (username, userData) VALUES (?,?)";
                    String userDataString = new Gson().toJson(newUserData);
                    executeUpdate(statement, username, userDataString);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("Error: unable to update database: %s, %s", statement, e.getMessage()));
        }
    }


    @Override
    public UserData getUser(String username) throws DataAccessException {
        String statement = "SELECT userData FROM user WHERE username=?";
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, username);
                var rs = ps.executeQuery();
                // If we have a match, then return it. Otherwise, return null
                if (rs.next()) {
                    String json = rs.getString("userData");
                    return new Gson().fromJson(json, UserData.class);
                } else {
                    return null;
                }
            }
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException(String.format("Error: unable to update database: %s, %s", statement, e.getMessage()));
        }
    }

    public boolean verifyPassword(String username, String clearTextPassword) throws DataAccessException {
        // Read the hashed password from the database
        String hashedPassword;
        String statement = "SELECT userData FROM user WHERE username=?";
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, username);
                var rs = ps.executeQuery();
                // If we have a match, then return it. Otherwise, return null
                if (rs.next()) {
                    String json = rs.getString("userData");
                    UserData userData = new Gson().fromJson(json, UserData.class);
                    hashedPassword = userData.password();
                } else {
                    throw new RuntimeException("Error: verifyPassword was called for a user which doesn't " +
                            "exist in the database");
                }
            }
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException(String.format("Error: unable to update database: %s, %s", statement, e.getMessage()));
        }
        // Check the password
        return BCrypt.checkpw(clearTextPassword, hashedPassword);
    }


    @Override
    public void clear() throws DataAccessException {
        // Clear the entire table with TRUNCATE
        String statement = "TRUNCATE TABLE user";
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
            throw new DataAccessException(String.format("Error: unable to update database: %s, %s", statement, e.getMessage()));
        }
    }

    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS user (
              `username` varchar(256) NOT NULL,
              `userData` longtext DEFAULT NULL,
              PRIMARY KEY (`username`),
              INDEX(username)
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
