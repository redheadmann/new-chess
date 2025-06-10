package ui;

import exception.ResponseException;

public interface Client {

    public String help() throws ResponseException;
    public String eval(String input) throws ResponseException;

    public enum Color {
        WHITE,
        BLACK
    }
}
