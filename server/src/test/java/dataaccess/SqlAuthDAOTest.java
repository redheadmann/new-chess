package dataaccess;

import sharedexception.UnauthorizedException;
import model.AuthData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SqlAuthDAOTest {

    SqlAuthDAO authDAO;

    @BeforeEach
    public void setup() {
        try {
            authDAO = new SqlAuthDAO();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void createAuth() {
        try {
            AuthData expected;
            expected = authDAO.createAuth("user");

            AuthData actual;
            actual = authDAO.getAuth(expected.authToken());
            Assertions.assertEquals(expected, actual);
        } catch (UnauthorizedException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void failCreateAuth() {
        try {
            // All potential errors are handles at a higher level, so this test is not useful
            AuthData auth1 = authDAO.createAuth("user");
            AuthData auth2 = authDAO.createAuth("user");
            Assertions.assertNotEquals(auth1, auth2);
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private AuthData addAuth() throws DataAccessException {
        return authDAO.createAuth("henry");
    }

    @Test
    void getAuth() {
        try {
            AuthData userOne = addAuth();

            AuthData userTwo = authDAO.createAuth("new guy");

            AuthData expected = authDAO.getAuth(userTwo.authToken());
            Assertions.assertEquals(expected, userTwo);
            expected = authDAO.getAuth(userOne.authToken());
            Assertions.assertEquals(expected, userOne);
        } catch (DataAccessException | UnauthorizedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void failGetAuth() {
        try {
            addAuth();
            Assertions.assertThrows(DataAccessException.class, () -> {
                authDAO.getAuth("failure");
            });
        } catch (DataAccessException | UnauthorizedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void deleteAuth() {
        try {
            // add user
            AuthData user = addAuth();

           // check successful insertion
            AuthData expected = authDAO.getAuth(user.authToken());
            Assertions.assertEquals(expected, user);

            // remove and check for user again
            authDAO.deleteAuth(user.authToken());
            Assertions.assertThrows(DataAccessException.class, () -> {
                authDAO.getAuth(user.authToken());
            });
        } catch (DataAccessException | UnauthorizedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void failDeleteAuth() {
        try {
            // add user
            AuthData user = addAuth();



            // remove twice
            authDAO.deleteAuth(user.authToken());
            Assertions.assertThrows(DataAccessException.class, () -> {
                authDAO.deleteAuth(user.authToken());
            });
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    void clear() {
        try {
            // Setup
            AuthData auth1 = authDAO.createAuth("user1");
            authDAO.createAuth("user2");


            authDAO.clear();

            Assertions.assertThrows(DataAccessException.class, () -> {
                authDAO.getAuth(auth1.authToken()); // Invalid gameID
            });
        } catch (DataAccessException | UnauthorizedException e) {
            throw new RuntimeException(e);
        }
    }
}