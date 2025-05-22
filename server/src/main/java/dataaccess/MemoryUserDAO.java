package dataaccess;

import model.UserData;

import javax.xml.crypto.Data;
import java.util.HashMap;
import java.util.Objects;

public class MemoryUserDAO implements UserDAO {

    // while we have no actual database, use linked lists in memory
    final private HashMap<String, UserData> data = new HashMap<>();

    @Override
    public void createUser(UserData userData) throws DataAccessException {
        String username = userData.username();
        if (this.getUser(username) != null ) {
            throw new DataAccessException("username already taken");
        }
        data.put(username, userData);
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        return data.get(username);
    }

    @Override
    public void clear() throws DataAccessException {
        data.clear();
    }

    @Override
    public boolean verifyPassword(String username, String clearTextPassword) throws DataAccessException {
        // Get user and extract password
        String storedPassword = this.getUser(username).password();
        return Objects.equals(storedPassword, clearTextPassword);
    }
}
