package ui;

import static ui.EscapeSequences.*;

public class PreLoginClient {

    public PreLoginClient(String serverUrl) {
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
