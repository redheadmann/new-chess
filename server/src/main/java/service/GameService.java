package service;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.GameData;

import java.util.ArrayList;
import java.util.List;

public class GameService {
    public final GameDAO gameDAO;

    public GameService(GameDAO gameDAO) {
        this.gameDAO = gameDAO;
    }

    public record ReducedGameData(int gameID, String whiteUsername, String blackUsername, String gameName) {}
    public record ListResult(List<ReducedGameData> games, String message) implements Result {}

    public ListResult list() {
        // 1. list all games
        List<GameData> games = gameDAO.listGames();
        // 2. convert list to ListResult
        List<ReducedGameData> resultList = new ArrayList<>();
        for (GameData game: games) {
            ReducedGameData reducedGame = new ReducedGameData(game.gameID(), game.whiteUsername(),
                    game.blackUsername(), game.gameName());
            resultList.add(reducedGame);
        }
        // 3. return list
        return new ListResult(resultList, null);
    }

    public record CreateRequest(String gameName) {}
    public record CreateResult(Integer gameID, String message) implements Result {}

    public CreateResult createGame(CreateRequest request) {
        String gameName = request.gameName();

        if (gameName == null) { // ensure game name is not null
            String message = "Error: bad request";
            return new CreateResult(null, message);
        }

        GameData newGame;
        // overkill, but catch the exception for if too many games already exist
        try {
            newGame = gameDAO.createGame(gameName); // return a failed result instead
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }


        return new CreateResult(newGame.gameID(), null);
    }

    public record JoinRequest(String playerColor, Integer gameID) {}
    public record JoinResult(String message) implements Result {}

    public JoinResult join(JoinRequest request, String username) {
        Integer gameID = request.gameID();
        String playerColor = request.playerColor();


        // check that playerColor is valid and gameID is not null
        if (playerColor == null || gameID == null) {
            return new JoinResult("Error: bad request");
        } else if (!playerColor.equals("WHITE") && !playerColor.equals("BLACK")) {
            return new JoinResult("Error: bad request");
        }
        ChessGame.TeamColor color = switch(playerColor) {
            case "WHITE" -> ChessGame.TeamColor.WHITE;
            case "BLACK" -> ChessGame.TeamColor.BLACK;
            default -> throw new IllegalStateException("Unexpected value: " + playerColor);
        };

        // join game
        try {
            gameDAO.updateGame(username, color, gameID);
        } catch (DataAccessException e) {
            String errorMessage = e.getMessage();
            return new JoinResult(errorMessage);
        }

        return new JoinResult(null);
    }


}
