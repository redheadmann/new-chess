package dataaccess;

import chess.ChessGame;
import model.GameData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class GameDAOTest {

    SqlGameDAO gameDAO;

    @BeforeEach
    public void setup() {
        try {
            gameDAO = new SqlGameDAO();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void createGame() {
        try {
            GameData gameData;
            gameData = gameDAO.createGame("game");

            Assertions.assertTrue(gameData.gameID() >= 1000);
            Assertions.assertTrue(gameData.gameID() <= 9999);

        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void failCreateGame() {
        try {
            GameData game1;
            GameData game2;
            game1 = gameDAO.createGame("game");
            game2 = gameDAO.createGame("game");


            Assertions.assertNotEquals(game1, game2);

        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private ArrayList<GameData> setupGames() {
        GameData game1;
        GameData game2;
        GameData game3;
        try {
            game1 = gameDAO.createGame("game1");
            game2 = gameDAO.createGame("game2");
            game3 = gameDAO.createGame("game3");
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }

        return new ArrayList<>(Arrays.asList(game1, game2, game3));
    }

    @Test
    void getGame() {
        try {
            ArrayList<GameData> games;
            games = setupGames();

            GameData expected = games.get(1);
            GameData actual;
            actual = gameDAO.getGame(games.get(1).gameID());

            Assertions.assertEquals(expected, actual);
        } catch (DataAccessException e) {
            Assertions.assertThrows(DataAccessException.class, () -> {
                gameDAO.createGame(null);
            });
        }
    }

    @Test
    void failGetGame() {
        ArrayList<GameData> games;
        games = setupGames();

        Assertions.assertThrows(DataAccessException.class, () -> {
            gameDAO.getGame(123); // Invalid gameID
        });
    }

    @Test
    void listGames() {
        try {
            ArrayList<GameData> expectedGames;
            expectedGames = setupGames();

            List<GameData> actualGames;
            actualGames = gameDAO.listGames();

            for (int i: new int[]{0,1,2}) {
                Assertions.assertTrue(actualGames.contains(expectedGames.get(i)));
            }

        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void failListGames() {
        try {
            setupGames();

            gameDAO.clear();

            List<GameData> actualGames;
            actualGames = gameDAO.listGames();

            Assertions.assertTrue(actualGames.isEmpty());

        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void updateGame() {
        try {
            ArrayList<GameData> expectedGames;
            expectedGames = setupGames();

            // Get game 1
            GameData game1 = expectedGames.getFirst();
            GameData expected = new GameData(game1.gameID(), "New Name", game1.blackUsername(),
                    game1.gameName(), game1.game());

            // Actual game
            GameData actual;
            gameDAO.updateGame("New Name", ChessGame.TeamColor.WHITE, game1.gameID(), null);
            actual = gameDAO.getGame(game1.gameID());

            Assertions.assertEquals(expected, actual);
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void failUpdateGame() {
        setupGames();

        Assertions.assertThrows(DataAccessException.class, () -> {
            gameDAO.updateGame("New Name", ChessGame.TeamColor.WHITE,123, null); // Invalid gameID
        });
    }

    @Test
    void clear() {
        try {
            // Setup
            ArrayList<GameData> expectedGames;
            expectedGames = setupGames();
            // Get game 1
            GameData game1 = expectedGames.getFirst();

            gameDAO.clear();

            Assertions.assertThrows(DataAccessException.class, () -> {
                gameDAO.getGame(game1.gameID()); // Invalid gameID
            });
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }
}