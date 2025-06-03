package ui;

import exception.ResponseException;
import serverfacade.ServerFacade;

import java.util.Arrays;

import static ui.EscapeSequences.*;

public class PreLoginClient {

    private final ServerFacade server;

    public PreLoginClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
    }

    public String eval(String input) {
        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "register" -> register(params);
                case "login" -> login();
                default -> help();
            };
        } catch (ResponseException ex) {
            return ex.getMessage();
        }
    }

    public String register(String... params) throws ResponseException{
        return "Not implemented";
    }

    public String login(String... params) throws ResponseException{
        return "Not implemented";
    }

    // Version of help message for pre login
    public String help() {
        return SET_TEXT_COLOR_BLUE + "register <USERNAME> <PASSWORD> <EMAIL>" +
                SET_TEXT_COLOR_DARK_GREY + " - to create an account\n" +
                SET_TEXT_COLOR_BLUE + "login <USERNAME> <PASSWORD>" +
                SET_TEXT_COLOR_DARK_GREY + " - to play chess\n" +
                SET_TEXT_COLOR_BLUE + "quit" +
                SET_TEXT_COLOR_DARK_GREY + " - playing chess" +
                SET_TEXT_COLOR_BLUE + "help" +
                SET_TEXT_COLOR_DARK_GREY + " - with possible commands\n";
    }
}
