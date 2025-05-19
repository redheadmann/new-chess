package dataaccess;

import model.UserData;

public class SqlUserDAO implements UserDAO {
    @Override
    public void createUser(UserData userData) throws DataAccessException {
        
    }

    @Override
    public UserData getUser(String username) {
        return null;
    }

    @Override
    public void clear() {

    }
}
