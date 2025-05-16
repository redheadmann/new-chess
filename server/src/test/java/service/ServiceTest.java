package service;

import dataaccess.*;
import org.junit.jupiter.api.BeforeEach;

public abstract class ServiceTest {
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
