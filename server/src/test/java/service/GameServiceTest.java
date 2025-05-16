package service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


class GameServiceTest extends ServiceTest {

    @Test
    @DisplayName("List Games")
    void goodList() {
        String blackUsername = "Jeff";
        String whiteUsername = "Bob";

        // create two games
        gameService.createGame(new GameService.CreateRequest("Game One"));
        GameService.CreateResult createResult = gameService.createGame(new GameService.CreateRequest("Game Two"));
        Integer gameID = createResult.gameID();

        // join game
        gameService.join(new GameService.JoinRequest("WHITE", gameID), whiteUsername);
        gameService.join(new GameService.JoinRequest("BLACK", gameID), blackUsername);

        // try listing games
        GameService.ListResult listResult = gameService.list();
        Assertions.assertEquals(listResult.games().size(), 2);
    }

    @Test
    @DisplayName("List no games")
    void emptyList() {
        // create game
        String gameName = "name";
        gameService.createGame(new GameService.CreateRequest(gameName));

        // delete game
        clearService.clear();

        // try list
        Assertions.assertEquals(gameService.list().games().size(), 0);
    }

    @Test
    @DisplayName("createGame")
    void createGame() {
        // create game
        String gameName = "gameName";
        GameService.CreateResult createResult = gameService.createGame(new GameService.CreateRequest(gameName));

        // check result
        Assertions.assertNull(createResult.message());
        Assertions.assertTrue(createResult.gameID() > 0);
    }

    @Test
    @DisplayName("Create game with no name")
    void failCreateGame() {
        // create game with null name
        GameService.CreateResult createResult = gameService.createGame(new GameService.CreateRequest(null));

        // check result
        Assertions.assertEquals(createResult.message(), "Error: bad request");
        Assertions.assertNull(createResult.gameID());
    }

    @Test
    @DisplayName("join game")
    void join() {
        // create game
        String gameName = "gameName";
        GameService.CreateResult createResult = gameService.createGame(new GameService.CreateRequest(gameName));
        Integer gameID = createResult.gameID();

        // join
        String blackUsername = "Jeff";
        String whiteUsername = "Bob";
        gameService.join(new GameService.JoinRequest("WHITE", gameID), whiteUsername);
        gameService.join(new GameService.JoinRequest("BLACK", gameID), blackUsername);

        // list games and check usernames
        GameService.ListResult listResult = gameService.list();
        GameService.ReducedGameData game = listResult.games().getFirst();
        Assertions.assertEquals(game.gameName(), gameName);
        Assertions.assertEquals(game.whiteUsername(), whiteUsername);
        Assertions.assertEquals(game.blackUsername(), blackUsername);
        Assertions.assertEquals(game.gameID(), gameID);
    }

    @Test
    @DisplayName("Try to steal color in game")
    void stealJoin() {
        Integer gameID = createOneGame();

        // join
        String blackUsername = "Jeff";
        String whiteUsername = "Bob";
        gameService.join(new GameService.JoinRequest("WHITE", gameID), whiteUsername);
        GameService.JoinResult joinResult = gameService.join(new GameService.JoinRequest("WHITE", gameID), blackUsername);

        // assert that steal failed
        Assertions.assertEquals("Error: already taken", joinResult.message());
    }

    private Integer createOneGame() {
        // create game
        String gameName = "gameName";
        GameService.CreateResult createResult = gameService.createGame(new GameService.CreateRequest(gameName));
        return createResult.gameID();
    }
}