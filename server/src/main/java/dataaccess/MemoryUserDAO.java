package dataaccess;

import model.UserData;

import java.util.HashMap;

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
    public UserData getUser(String username) {
        return data.get(username);
    }

    @Override
    public void clear() {
        data.clear();
    }

    @Override
    public boolean verifyPassword(String username, String clearTextPassword) {
        // Get user and extract password
        String storedPassword = this.getUser(username).password();
        boolean res = storedPassword == clearTextPassword;
        return res;
    }
}
