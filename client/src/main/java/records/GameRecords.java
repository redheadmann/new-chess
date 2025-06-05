package records;

import java.util.List;

public class GameRecords {
    public record ReducedGameData(int gameID, String whiteUsername, String blackUsername, String gameName) {}
    public record ListResult(List<ReducedGameData> games, String message) {}

    public record CreateRequest(String gameName) {}
    public record CreateResult(Integer gameID, String message) {}

    public record JoinRequest(String playerColor, Integer gameID) {}
    public record JoinResult(String message) {}


}
