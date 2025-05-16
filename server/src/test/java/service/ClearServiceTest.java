package service;

import org.junit.jupiter.api.*;

class ClearServiceTest extends ServiceTest {
    @Test
    @DisplayName("Successfully clear everything")
    void clear() {
        // Populate Game, Auth, and User databases
        // Register user
        String username = "user";
        String password = "1234";
        userService.register(new UserService.RegisterRequest(username,
                password, "null"));

        // Create two games
        gameService.createGame(new GameService.CreateRequest("Game One"));
        GameService.CreateResult createResult = gameService.createGame(new GameService.CreateRequest("Game Two"));
        Integer gameID = createResult.gameID();

        // Join game
        gameService.join(new GameService.JoinRequest("WHITE", gameID), username);

        // Assert that games exist and user exists
        Assertions.assertFalse(gameService.list().games().isEmpty());
        Assertions.assertEquals(userService.login(new UserService.LoginRequest(username, password)).username(), username);


        // Clear everything
        clearService.clear();

        // Ensure everything is empty
        Assertions.assertEquals(gameService.list().games().size(), 0);
        Assertions.assertNull(userService.login(new UserService.LoginRequest(username, password)).username());
    }
}