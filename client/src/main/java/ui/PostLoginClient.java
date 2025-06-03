package ui;

import exception.ResponseException;
import serverfacade.ServerFacade;

import java.util.Arrays;

import static ui.EscapeSequences.SET_TEXT_COLOR_BLUE;
import static ui.EscapeSequences.SET_TEXT_COLOR_DARK_GREY;

public class PostLoginClient {

    private final ServerFacade server;

    public PostLoginClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
    }

    public String eval(String input) {
        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "create" -> createGame(params);
                case "list" -> listGames();
                case "join" -> joinGame(params);
                case "observe" -> observe(params);
                case "logout" -> logout(params);
                default -> help();
            };
        } catch (ResponseException ex) {
            return ex.getMessage();
        }
    }

    public String createGame(String... params) throws ResponseException{
        return "Not implemented";
    }

    public String listGames(String... params) throws ResponseException{
        return "Not implemented";
    }

    public String joinGame(String... params) throws ResponseException{
        return "Not implemented";
    }

    public String observe(String... params) throws ResponseException{
        return "Not implemented";
    }


    public String logout(String... params) throws ResponseException{
        return "Not implemented";
    }

/*
    public String adoptPet(String... params) throws ResponseException {
        if (params.length == 1) {
            try {
                var id = Integer.parseInt(params[0]);
                var pet = getPet(id);
                if (pet != null) {
                    server.deletePet(id);
                    return String.format("%s says %s", pet.name(), pet.sound());
                }
            } catch (NumberFormatException ignored) {
            }
        }
        throw new ResponseException(400, "Expected: <pet id>");
    }
 */


    // Version of help message for post login
    public String help() {
        return SET_TEXT_COLOR_BLUE + "create <NAME>" +
                SET_TEXT_COLOR_DARK_GREY + " - game\n" +
                SET_TEXT_COLOR_BLUE + "list" +
                SET_TEXT_COLOR_DARK_GREY + " - games\n" +
                SET_TEXT_COLOR_BLUE + "join <ID> [WHITE|BLACK]" +
                SET_TEXT_COLOR_DARK_GREY + " - a game" +
                SET_TEXT_COLOR_BLUE + "observe <ID>" +
                SET_TEXT_COLOR_DARK_GREY + " - a game\n" +
                SET_TEXT_COLOR_BLUE + "logout" +
                SET_TEXT_COLOR_DARK_GREY + " - when you are done\n" +
                SET_TEXT_COLOR_BLUE + "quit" +
                SET_TEXT_COLOR_DARK_GREY + " - playing chess\n" +
                SET_TEXT_COLOR_BLUE + "help" +
                SET_TEXT_COLOR_DARK_GREY + " - with possible commands\n";
    }
}
