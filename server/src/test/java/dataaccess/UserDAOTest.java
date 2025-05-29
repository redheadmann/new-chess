package dataaccess;

import model.UserData;
import org.junit.jupiter.api.*;
import org.mindrot.jbcrypt.BCrypt;

class UserDAOTest {

    SqlUserDAO userDAO;


    @BeforeEach
    void setUp() {
        try {
            userDAO = new SqlUserDAO();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try {
            userDAO.clear();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void compareUsers (UserData expected, UserData actual) {
        Assertions.assertEquals(expected.username(), actual.username());
        Assertions.assertEquals(expected.email(), actual.email());
        Assertions.assertTrue(BCrypt.checkpw(expected.password(), actual.password()));
    }

    @DisplayName("Create User")
    @Test
    void createUser() {
        try {
            // Add users
            UserData expected = new UserData("hkf", "1234", "mymail@gmail.com");
            userDAO.createUser(expected);
            // Check database for correct user
            UserData actual;
            actual = userDAO.getUser("hkf");

            compareUsers(expected, actual);

            // CHeck for null response
            expected = null;
            actual = userDAO.getUser("bogus");

            Assertions.assertEquals(expected, actual);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @DisplayName("Try to take username")
    @Test
    void failCreateUser() {
        try {
            UserData user = new UserData("hkf", "1234", "mail");
            userDAO.createUser(user);

            Assertions.assertThrows(DataAccessException.class, () -> {
                userDAO.createUser(user);
            });
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void getUser() {
        try {
            UserData expected = new UserData("person", "pass", "mail");
            userDAO.createUser(expected);
            UserData user2 = new UserData("2", "1234", "gmail");
            userDAO.createUser(user2);

            UserData actual;
            actual = userDAO.getUser("person");

            compareUsers(expected, actual);
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void failGetUser() {
        try {
            UserData expected = new UserData("person", "pass", "mail");
            userDAO.createUser(expected);
            UserData user2 = new UserData("2", "1234", "gmail");
            userDAO.createUser(user2);

            Assertions.assertNull(userDAO.getUser("Persons"));

        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }


    private void passwordSetup () {
        try {
            UserData user = new UserData("2", "1234", "gmail");
            userDAO.createUser(user);
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void verifyPassword() {
        try {
            passwordSetup();

            Assertions.assertTrue(userDAO.verifyPassword("2", "1234"));

        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void failVerifyPassword() {
        try {
            passwordSetup();

            Assertions.assertFalse(userDAO.verifyPassword("2", "12345"));

        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void clear() {
        try {
            UserData user = new UserData("hkf", "1234", "mail");
            userDAO.createUser(user);
            userDAO.clear();
            Assertions.assertNull(userDAO.getUser("hkf"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}