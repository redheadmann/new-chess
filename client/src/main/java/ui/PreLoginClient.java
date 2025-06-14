package ui;

import repl.Repl;
import repl.State;
import serverfacade.websocket.ServerMessageObserver;
import sharedexception.ResponseException;
import records.UserRecords;
import serverfacade.ServerFacade;
import websocket.messages.ServerMessage;

import java.util.Arrays;

import static ui.EscapeSequences.*;

public class PreLoginClient implements Client, ServerMessageObserver {

    private final ServerFacade server;
    private final Repl repl;

    public PreLoginClient(Repl repl) {
        this.repl = repl;
        server = repl.getServer();
    }

    public record ReturnValue(String value, String authToken) {}

    public String eval(String input) {
        try {
            var tokens = input.split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "register" -> register(params);
                case "login" -> login(params);
                case "quit" -> "quit";
                default -> help();
            };
        } catch (ResponseException ex) {
            return ex.getMessage();
        }
    }

    public String register(String... params) throws ResponseException{
        // Should I ALLOW ONLY 2 PARAMETERS?
        if (params.length == 3) {
            String username = params[0];
            String password = params[1];
            String email = params[2];
            UserRecords.RegisterResult result = server.registerUser(username, password, email);

            // Store the authToken in repl and update the repl state
            repl.setState(State.SIGNED_IN);
            repl.setAuthToken(result.authToken());
            return "registered successfully!";
        } else {
            throw new ResponseException(400, "Expected: register <USERNAME> <PASSWORD> <EMAIL>");
        }
    }


    public String login(String... params) throws ResponseException{
        if (params.length == 2) {
            String username = params[0];
            String password = params[1];
            UserRecords.LoginResult result = server.loginUser(username, password);

            // return the authToken, which the Repl class will use to
            //  instantiate the PostLoginClient
            repl.setState(State.SIGNED_IN);
            repl.setAuthToken(result.authToken());
            return "login successful";
        } else {
            throw new ResponseException(400, "Expected: login <USERNAME> <PASSWORD>");
        }
    }

    // Version of help message for pre login
    public String help() {
        return SET_TEXT_COLOR_BLUE + "register <USERNAME> <PASSWORD> <EMAIL>" +
                SET_TEXT_COLOR_WHITE + " - to create an account\n" +
                SET_TEXT_COLOR_BLUE + "login <USERNAME> <PASSWORD>" +
                SET_TEXT_COLOR_WHITE + " - to play chess\n" +
                SET_TEXT_COLOR_BLUE + "quit" +
                SET_TEXT_COLOR_WHITE + " - playing chess\n" +
                SET_TEXT_COLOR_BLUE + "help" +
                SET_TEXT_COLOR_WHITE + " - with possible commands\n";
    }

    @Override
    public void notify(ServerMessage message) {
        repl.notify(message);
    }

}
