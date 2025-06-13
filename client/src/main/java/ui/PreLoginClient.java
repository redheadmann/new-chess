package ui;

import sharedexception.ResponseException;
import records.UserRecords;
import serverfacade.ServerFacade;

import java.util.Arrays;

import static ui.EscapeSequences.*;

public class PreLoginClient {

    private final ServerFacade server;

    public PreLoginClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
    }

    public record ReturnValue(String value, String authToken) {}

    public ReturnValue eval(String input) {
        try {
            var tokens = input.split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "register" -> register(params);
                case "login" -> login(params);
                case "quit" -> new ReturnValue("quit", null);
                default -> help();
            };
        } catch (ResponseException ex) {
            return new ReturnValue(ex.getMessage(), null);
        }
    }

    public ReturnValue register(String... params) throws ResponseException{
        // Should I ALLOW ONLY 2 PARAMETERS?
        if (params.length == 3) {
            String username = params[0];
            String password = params[1];
            String email = params[2];
            UserRecords.RegisterResult result = server.registerUser(username, password, email);

            // return the authToken, which the Repl class will use to
            //  instantiate the PostLoginClient
            return new ReturnValue(" ", result.authToken());
        } else {
            throw new ResponseException(400, "Expected: register <USERNAME> <PASSWORD> <EMAIL>");
        }
    }


    public ReturnValue login(String... params) throws ResponseException{
        if (params.length == 2) {
            String username = params[0];
            String password = params[1];
            UserRecords.LoginResult result = server.loginUser(username, password);

            // return the authToken, which the Repl class will use to
            //  instantiate the PostLoginClient
            return new ReturnValue(" ", result.authToken());
        } else {
            throw new ResponseException(400, "Expected: login <USERNAME> <PASSWORD>");
        }
    }

    // Version of help message for pre login
    public ReturnValue help() {
        String string = SET_TEXT_COLOR_BLUE + "register <USERNAME> <PASSWORD> <EMAIL>" +
                SET_TEXT_COLOR_WHITE + " - to create an account\n" +
                SET_TEXT_COLOR_BLUE + "login <USERNAME> <PASSWORD>" +
                SET_TEXT_COLOR_WHITE + " - to play chess\n" +
                SET_TEXT_COLOR_BLUE + "quit" +
                SET_TEXT_COLOR_WHITE + " - playing chess\n" +
                SET_TEXT_COLOR_BLUE + "help" +
                SET_TEXT_COLOR_WHITE + " - with possible commands\n";
        return new ReturnValue(string,null);
    }
}
