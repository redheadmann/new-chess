package ui;

//import client.websocket.NotificationHandler;
//import webSocketMessages.Notification;

import java.util.Scanner;

import static ui.EscapeSequences.*;

public class Repl {
    private final PostLoginClient postLoginClient;
    private final PreLoginClient preLoginClient;
    private State state = State.SIGNED_OUT;

    public Repl(String serverUrl) {
        postLoginClient = new PostLoginClient(serverUrl);
        preLoginClient = new PreLoginClient(serverUrl);
    }

    public void run() {
        System.out.println(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_WHITE + "Welcome to the chess client! Type \"help\" for a list of commands.");
        System.out.print(postLoginClient.help());

        Scanner scanner = new Scanner(System.in);
        String result = "";
        while (!result.equals("quit")) {
            printPrompt();
            String line = scanner.nextLine();

            try {
                switch (state) {
                    case SIGNED_IN -> {
                        result = postLoginClient.eval(line);
                        System.out.print(SET_TEXT_COLOR_BLUE + result);
                    }
                    case SIGNED_OUT -> {
                        result = preLoginClient.eval(line);
                        System.out.print(SET_TEXT_COLOR_BLUE + result);
                    }
                    case IN_GAME -> {
                        System.out.print("Not yet implemented");
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

    /*
    public void notify(Notification notification) {
        System.out.println(RED + notification.message());
        printPrompt();
    }
     */

    private void printPrompt() {
        String stateString = switch (state) {
            case SIGNED_IN -> "LOGGED_IN";
            case SIGNED_OUT -> "LOGGED_OUT";
            case IN_GAME -> "IN_GAME";
        };
        System.out.print("\n" + SET_TEXT_COLOR_BLACK + RESET_BG_COLOR + "[" + stateString + "] >>> " + SET_TEXT_COLOR_GREEN);
    }

}
