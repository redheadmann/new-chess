package client;

import chess.ChessGame;
import exception.ResponseException;
import org.junit.jupiter.api.*;
import passoff.model.*;
import server.Server;
import serverfacade.ServerFacade;
import records.GameRecords;
import records.UserRecords;

import java.net.HttpURLConnection;
import java.util.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServerFacadeTests {

    private static TestUser existingUser;
    private static TestUser newUser;
    private static ServerFacade serverFacade;
    private static Server server;
    private String existingAuth;
    private final String gameName = "game1";

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);


        serverFacade = new ServerFacade("http://localhost:" + port);
        existingUser = new TestUser("ExistingUser", "existingUserPassword", "eu@mail.com");
        newUser = new TestUser("NewUser", "newUserPassword", "nu@mail.com");
    }

    @BeforeEach
    public void setup() {
        try {
            serverFacade.clear();
        } catch (ResponseException e) {
            throw new RuntimeException(e);
        }

        //log one user in to start with
        UserRecords.RegisterResult regResult = null;
        try {
            regResult = serverFacade.registerUser(existingUser.getUsername(),
                    existingUser.getPassword(), existingUser.getEmail());
            existingAuth = regResult.authToken();
        } catch (ResponseException e) {
            throw new RuntimeException(e);
        }

    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    // ### SERVER-LEVEL API TESTS ###
    @Test
    @Order(2)
    @DisplayName("Normal User Login")
    public void loginSuccess() {
        UserRecords.LoginResult loginResult = null;
        try {
            loginResult = serverFacade.loginUser(existingUser.getUsername(), existingUser.getPassword());
        } catch (ResponseException e) {
            throw new RuntimeException(e);
        }

        Assertions.assertEquals(existingUser.getUsername(), loginResult.username(),
                "Response did not give the same username as user");
        Assertions.assertNotNull(loginResult.authToken(), "Response did not return authentication String");
    }

    @Test
    @Order(3)
    @DisplayName("Login Bad Request")
    public void loginBadRequest() {
        TestUser[] incompleteLoginRequests = {
                new TestUser(null, existingUser.getPassword(), existingUser.getEmail()),
                new TestUser(existingUser.getUsername(), null, existingUser.getEmail()),
        };

        for (TestUser incompleteLoginRequest : incompleteLoginRequests) {
            Assertions.assertThrows(ResponseException.class, () -> {
                serverFacade.loginUser(incompleteLoginRequest.getUsername(),
                        incompleteLoginRequest.getPassword());
            });
        }
    }

    @Test
    @Order(3)
    @DisplayName("Login Unauthorized (Multiple Forms)")
    public void loginUnauthorized() {
        TestUser[] unauthorizedLoginRequests = {newUser, new TestUser(existingUser.getUsername(), "BAD!PASSWORD")};

        for (TestUser unauthorizedLoginRequest : unauthorizedLoginRequests) {
            Assertions.assertThrows(ResponseException.class, () -> {
                serverFacade.loginUser(unauthorizedLoginRequest.getUsername(),
                        unauthorizedLoginRequest.getPassword());
            });
        }
    }

    @Test
    @Order(4)
    @DisplayName("Normal User Registration")
    public void registerSuccess() {
        //submit register request
        UserRecords.RegisterResult registerResult = null;
        try {
            registerResult = serverFacade.registerUser(newUser.getUsername(), newUser.getPassword(),
                    newUser.getEmail());
        } catch (ResponseException e) {
            throw new RuntimeException(e);
        }

        Assertions.assertEquals(newUser.getUsername(), registerResult.username(),
                "Response did not have the same username as was registered");
        Assertions.assertNotNull(registerResult.authToken(), "Response did not contain an authentication string");
    }

    @Test
    @Order(5)
    @DisplayName("Re-Register User")
    public void registerTwice() {
        //submit register request trying to register existing user

        Assertions.assertThrows(ResponseException.class, () -> {
            serverFacade.registerUser(existingUser.getUsername(),
                    existingUser.getPassword(), existingUser.getEmail());
        });
    }

    @Test
    @Order(5)
    @DisplayName("Register Bad Request")
    public void registerBadRequest() {
        //attempt to register a user without a password
        Assertions.assertThrows(ResponseException.class, () -> {
            serverFacade.registerUser(newUser.getUsername(),
                    null, newUser.getEmail());
        });
    }

    @Test
    @Order(6)
    @DisplayName("Normal Logout")
    public void logoutSuccess() {
        //log out existing user
        try {
            serverFacade.logoutUser(existingAuth);
        } catch (ResponseException e) {
            Assertions.assertTrue(Boolean.FALSE, "Failed to logout user for test");
        }
    }

    @Test
    @Order(7)
    @DisplayName("Invalid Auth Logout")
    public void logoutTwice() {
        //log out user twice
        //second logout should fail
        try {
            serverFacade.logoutUser(existingAuth);
        } catch (ResponseException e) {
            Assertions.assertTrue(Boolean.FALSE, "Failed to logout user for test");
        }

        Assertions.assertThrows(ResponseException.class, () -> {
            serverFacade.logoutUser(existingAuth);
        });
    }


    private GameRecords.CreateResult createGame(String name) {
        //create game
        GameRecords.CreateResult createResult = null;
        try {
            if (name == null) {
                createResult = serverFacade.createGame(existingAuth, gameName);
            } else {
                createResult = serverFacade.createGame(existingAuth, name);
            }

        } catch (ResponseException e) {
            Assertions.assertTrue(Boolean.FALSE, "Failed to create game");
        }
        return createResult;
    }


    @Test
    @Order(8)
    @DisplayName("Valid Creation")
    public void createGameSuccess() {
        GameRecords.CreateResult createResult = createGame(null);

        Assertions.assertNotNull(createResult.gameID(), "Result did not return a game ID");
        Assertions.assertTrue(createResult.gameID() > 0, "Result returned invalid game ID");
    }

    @Test
    @Order(9)
    @DisplayName("Create with Bad Authentication")
    public void createGameUnauthorized() {
        //log out user so auth is invalid
        try {
            serverFacade.logoutUser(existingAuth);
        } catch (ResponseException e) {
            Assertions.assertTrue(Boolean.FALSE, "Failed to logout user for test");
        }

        Assertions.assertThrows(ResponseException.class, () -> {
            serverFacade.createGame(existingAuth, gameName);
        }, "Game created without authorization");
    }

    @Test
    @Order(9)
    @DisplayName("Create Bad Request")
    public void createGameBadRequest() {
        Assertions.assertThrows(ResponseException.class, () -> {
            serverFacade.createGame(existingAuth, null);
        }, "Game created without name");
    }

    @Test
    @Order(10)
    @DisplayName("Join Created Game")
    public void joinGameSuccess() {
        //create game
        GameRecords.CreateResult createResult = createGame(null);

        //join as white
        try {
            serverFacade.joinGame(existingAuth, "WHITE",
                    createResult.gameID());
        } catch (ResponseException e) {
            Assertions.assertTrue(Boolean.FALSE, "Failed to join game");
        }

        GameRecords.ListResult listResult = null;
        try {
            listResult = serverFacade.listGames(existingAuth);
        } catch (ResponseException e) {
            throw new RuntimeException(e);
        }

        Assertions.assertNotNull(listResult.games(), "List result did not contain games");
        Assertions.assertEquals(1, listResult.games().size(), "List result is incorrect size");
        Assertions.assertEquals(existingUser.getUsername(), listResult.games().getFirst().whiteUsername(),
                "Username of joined player not present in list result");
        Assertions.assertNull(listResult.games().getFirst().blackUsername(), "Username present on non-joined color");
    }


    @Test
    @Order(11)
    @DisplayName("Join Bad Authentication")
    public void joinGameUnauthorized() {
        //create game
        GameRecords.CreateResult createResult = createGame(null);

        Assertions.assertThrows(ResponseException.class, ()-> {
            serverFacade.joinGame(existingAuth + "bad stuff", "WHITE",
                    createResult.gameID());
        }, "Game created with bad authentication");
    }


    @Test
    @Order(11)
    @DisplayName("Join Bad Team Color")
    public void joinGameBadColor() {
        // Create game
        GameRecords.CreateResult createResult = createGame(null);
        int gameID = createResult.gameID();

        // Try bad colors
        for(String color : new String[]{null, "", "GREEN"}) {
            Assertions.assertThrows(ResponseException.class, () -> {
                serverFacade.joinGame(existingAuth, color, gameID);
            });
        }
    }


    @Test
    @Order(12)
    @DisplayName("List No Games")
    public void listGamesEmpty() {
        GameRecords.ListResult result = null;
        try {
            result = serverFacade.listGames(existingAuth);
        } catch (ResponseException e) {
            Assertions.assertTrue(Boolean.FALSE, "Failed to list games");
        }

        Assertions.assertNotNull(result.games(), "List result did not contain an empty game list");
        Assertions.assertEquals(0, result.games().size(), "Found games when none should be there");
    }

    @Test
    @Order(12)
    @DisplayName("List Multiple Games")
    public void listGamesSuccess() {
        //register a few users to create games
        TestUser userA = new TestUser("a", "A", "a.A");
        TestUser userB = new TestUser("b", "B", "b.B");
        TestUser userC = new TestUser("c", "C", "c.C");
        String authA = null;
        String authB = null;
        String authC = null;
        try {
            authB = serverFacade.registerUser(userB.getUsername(), userB.getPassword(),
                    userB.getEmail()).authToken();
            authC = serverFacade.registerUser(userC.getUsername(), userC.getPassword(),
                    userC.getEmail()).authToken();
            authA = serverFacade.registerUser(userA.getUsername(), userA.getPassword(),
                    userA.getEmail()).authToken();
        } catch (ResponseException e) {
            Assertions.assertTrue(Boolean.FALSE, "Failed to register users");
        }

        //create games
        ArrayList<GameRecords.ReducedGameData> expectedList = new ArrayList<>();

        try {
            //1 as black from A
            String game1Name = "I'm numbah one!";
            GameRecords.CreateResult game1 = serverFacade.createGame(authA, game1Name);
            serverFacade.joinGame(authA, "BLACK", game1.gameID());
            expectedList.add(new GameRecords.ReducedGameData(game1.gameID(),
                    null, "a", game1Name));

            //1 as white from B
            String game2Name = "Lonely";
            GameRecords.CreateResult game2 = serverFacade.createGame(authB, game2Name);
            serverFacade.joinGame(authB, "WHITE", game2.gameID());
            expectedList.add(new GameRecords.ReducedGameData(game2.gameID(),
                    "b", null, game2Name));

            //1 of each from C
            String game3Name = "GG";
            GameRecords.CreateResult game3 = serverFacade.createGame(authC, game3Name);
            serverFacade.joinGame(authC, "WHITE", game3.gameID());
            serverFacade.joinGame(authA, "BLACK", game3.gameID());
            expectedList.add(new GameRecords.ReducedGameData(game3.gameID(),
                    "c", "a", game3Name));

            //C play self
            String game4Name = "All by myself";
            GameRecords.CreateResult game4 = serverFacade.createGame(authC, game4Name);
            serverFacade.joinGame(authC, "WHITE", game4.gameID());
            serverFacade.joinGame(authC, "BLACK", game4.gameID());
            expectedList.add(new GameRecords.ReducedGameData(game4.gameID(),
                    "c", "c", game4Name));
        } catch (ResponseException e) {
            Assertions.assertTrue(Boolean.FALSE, "Failed to create multiple games");
        }

        //list games
        GameRecords.ListResult listResult = null;
        try {
            listResult = serverFacade.listGames(existingAuth);
        } catch (ResponseException e) {
            Assertions.assertTrue(Boolean.FALSE, "Failed to list games");
        }
        List<GameRecords.ReducedGameData> returnedList = listResult.games();
        Assertions.assertNotNull(returnedList, "List result did not contain a list of games");
        Comparator<GameRecords.ReducedGameData> gameIdComparator =
                Comparator.comparingInt(GameRecords.ReducedGameData::gameID);
        expectedList.sort(gameIdComparator);
        returnedList.sort(gameIdComparator);

        //check
        Assertions.assertEquals(expectedList, returnedList, "Returned Games list was incorrect");
    }


/*
    @Test
    @Order(14)
    @DisplayName("Clear Test")
    public void clearData() {
        //create filler games
        serverFacade.createGame(new TestCreateRequest("Mediocre game"), existingAuth);
        serverFacade.createGame(new TestCreateRequest("Awesome game"), existingAuth);

        //log in new user
        TestUser user = new TestUser("ClearMe", "cleared", "clear@mail.com");
        TestAuthResult registerResult = serverFacade.register(user);

        //create and join game for new user
        TestCreateResult createResult = serverFacade.createGame(new TestCreateRequest("Clear game"),
                registerResult.getAuthToken());

        TestJoinRequest joinRequest = new TestJoinRequest(ChessGame.TeamColor.WHITE, createResult.getGameID());
        serverFacade.joinPlayer(joinRequest, registerResult.getAuthToken());

        //do clear
        TestResult clearResult = serverFacade.clear();

        //test clear successful
        assertHttpOk(clearResult);

        //make sure neither user can log in
        //first user
        TestAuthResult loginResult = serverFacade.login(existingUser);
        assertHttpUnauthorized(loginResult);

        //second user
        loginResult = serverFacade.login(user);
        assertHttpUnauthorized(loginResult);

        //try to use old auth token to list games
        TestListResult listResult = serverFacade.listGames(existingAuth);
        assertHttpUnauthorized(listResult);

        //log in new user and check that list is empty
        registerResult = serverFacade.register(user);
        assertHttpOk(registerResult);
        listResult = serverFacade.listGames(registerResult.getAuthToken());
        assertHttpOk(listResult);

        //check listResult
        Assertions.assertNotNull(listResult.getGames(), "List result did not contain an empty list of games");
        Assertions.assertEquals(0, listResult.getGames().length, "list result did not return 0 games after clear");
    }
*/
}
