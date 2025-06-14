package repl;


import chess.ChessGame;
import serverfacade.ServerFacade;
import serverfacade.websocket.ServerMessageObserver;
import serverfacade.websocket.WebSocketFacade;
import ui.GameplayClient;
import ui.PostLoginClient;
import ui.PreLoginClient;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.util.Scanner;

import static ui.BoardDrawer.drawBoard;
import static ui.EscapeSequences.*;

public class Repl implements ServerMessageObserver {
    private final PostLoginClient postLoginClient;
    private final PreLoginClient preLoginClient;
    private final GameplayClient gameplayClient;

    private State state = State.SIGNED_OUT;

    private Integer gameID = null;
    private String authToken = null;
    private ChessGame.TeamColor playerColor = ChessGame.TeamColor.WHITE;
    private ChessGame currentGame;

    private ServerFacade server;
    private WebSocketFacade ws;


    public Repl(String serverUrl) {
        this.server = new ServerFacade(serverUrl);

        preLoginClient = new PreLoginClient(this);
        postLoginClient = new PostLoginClient(this);
        gameplayClient = new GameplayClient(this);
    }

    public void setState(State state) {
        this.state = state;
    }


    public void run() {
        System.out.println(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_WHITE + "Welcome to the chess client! Type \"help\" for a list of commands.");
        System.out.print(preLoginClient.help());

        Scanner scanner = new Scanner(System.in);
        String result = "";
        while (!"quit".equals(result)) {
            printPrompt();
            String line = scanner.nextLine();

            try {
                switch (state) {
                    case SIGNED_OUT -> {
                        result = preLoginClient.eval(line);
                        System.out.print(SET_TEXT_COLOR_BLUE + result);
                        }
                    case SIGNED_IN -> {
                        result = postLoginClient.eval(line);
                        System.out.print(SET_TEXT_COLOR_BLUE + result);
                    }
                    case IN_GAME -> {
                        result = gameplayClient.eval(line);
                        System.out.print(SET_TEXT_COLOR_BLUE + result);
                    }
                }
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(msg);
            }
        }
        // Implement proper steps for quiting, including logging out if logged in
        //
        System.out.println();
    }

    private void printPrompt() {
        String stateString = switch (state) {
            case SIGNED_IN -> "LOGGED_IN";
            case SIGNED_OUT -> "LOGGED_OUT";
            case IN_GAME -> "IN_GAME";
        };
        System.out.print("\n" + SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_WHITE + "[" + stateString + "] >>> " + SET_TEXT_COLOR_GREEN);
    }

    public void notify(ServerMessage message) {
        System.out.print("\n");
        switch (message.getServerMessageType()) {
            case LOAD_GAME -> loadGame(((LoadGameMessage) message).getGame());
            case ERROR -> displayError(((ErrorMessage) message).getErrorMessage());
            case NOTIFICATION -> displayNotification(((NotificationMessage) message).getMessage());
        }
        printPrompt();
    }

    public void displayError(String message) {
        System.out.print(message);
    }

    public void loadGame(ChessGame game) {
        // Update the game
        this.currentGame = game;
        // Print the game
        String board = drawBoard(game.getBoard(), playerColor);
        System.out.print(board + SET_BG_COLOR_DARK_GREY);
    }

    public void displayNotification(String message) {
        System.out.print(message);
    }

    public Integer getGameID() {
        return gameID;
    }

    public void setGameID(Integer gameID) {
        this.gameID = gameID;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public WebSocketFacade getWs() {
        return ws;
    }

    public void setWs(WebSocketFacade ws) {
        this.ws = ws;
    }

    public ServerFacade getServer() {
        return server;
    }

    public void setPlayerColor(ChessGame.TeamColor playerColor) {
        this.playerColor = playerColor;
    }

    public ChessGame getCurrentGame() {
        return currentGame;
    }
}
