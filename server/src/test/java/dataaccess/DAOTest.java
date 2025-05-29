package dataaccess;

import org.junit.jupiter.api.BeforeEach;
import service.ClearService;
import service.GameService;
import service.UserService;


public abstract class DAOTest {
    GameDAO gameDAO;
    UserDAO userDAO;
    AuthDAO authDAO;
    GameService gameService;
    UserService userService;
    ClearService clearService;

    @BeforeEach
    public void setUp() {
        gameDAO = new MemoryGameDAO();
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();

        gameService = new GameService(gameDAO);
        userService = new UserService(authDAO, userDAO);
        clearService = new ClearService(gameDAO, userDAO, authDAO);
    }
}
