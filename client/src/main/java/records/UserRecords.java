package records;

public class UserRecords {

    public record RegisterRequest(String username, String password, String email) {}
    public record RegisterResult(String username, String authToken, String message) {}

    public record LoginRequest(String username, String password) {}
    public record LoginResult(String username, String authToken, String message) {}

    public record LogoutRequest(String authToken) {}
    public record LogoutResult(String message)  {}

}
