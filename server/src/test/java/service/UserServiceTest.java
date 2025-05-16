package service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserServiceTest extends ServiceTest {
    @Test
    @DisplayName("Register new user")
    void register() {
        // Register user
        String username = "user";
        String password = "1234";
        UserService.RegisterResult registerResult = userService.register(new UserService.RegisterRequest(username,
                password, "null"));

        // check result
        Assertions.assertNull(registerResult.message());
        Assertions.assertEquals(username, registerResult.username());
        Assertions.assertNotNull(registerResult.authToken());
    }

    @Test
    @DisplayName("Try to reuse a username")
    void failRegister() {
        // register user
        String username = "user";
        String password = "1234";
        userService.register(new UserService.RegisterRequest(username,
                password, "null"));


        // reuse username
        UserService.RegisterResult registerResult = userService.register(new UserService.RegisterRequest(username,
                password, "null"));

        // check results
        Assertions.assertEquals("Error: already taken", registerResult.message());
        Assertions.assertNull(registerResult.authToken());
        Assertions.assertNull(registerResult.username());
    }

    @Test
    @DisplayName("Login")
    void login() {
        // register user
        String username = "user";
        String password = "1234";
        userService.register(new UserService.RegisterRequest(username,
                password, "null"));

        // login
        UserService.LoginResult loginResult = userService.login(new UserService.LoginRequest(username, password));

        // ensure login worked
        Assertions.assertNull(loginResult.message());
        Assertions.assertEquals(username, loginResult.username());
        Assertions.assertNotNull(loginResult.authToken());
    }

    @Test
    @DisplayName("login with wrong passcode")
    void failLogin() {
        // register user
        String username = "user";
        String password = "1234";
        userService.register(new UserService.RegisterRequest(username,
                password, "null"));

        // login with incorrect password
        UserService.LoginResult loginResult = userService.login(new UserService.LoginRequest(username, "123"));

        // ensure login fails
        Assertions.assertEquals("Error: unauthorized", loginResult.message());
        Assertions.assertNull(loginResult.username());
        Assertions.assertNull(loginResult.authToken());
    }

    @Test
    @DisplayName("Successfully logout")
    void logout() {
        // register user
        String username = "user";
        String password = "1234";
        String authToken = userService.register(new UserService.RegisterRequest(username,
                password, "null")).authToken();

        // logout
        UserService.LogoutResult logoutResult = userService.logout(new UserService.LogoutRequest(authToken));

        // ensure no error message
        Assertions.assertNull(logoutResult.message());

        // try logging out twice
        logoutResult = userService.logout(new UserService.LogoutRequest(authToken));
        Assertions.assertEquals("Error: unauthorized", logoutResult.message());
    }

    @Test
    @DisplayName("Logout without authToken")
    void failLogout() {
        // register user
        String username = "user";
        String password = "1234";
        userService.register(new UserService.RegisterRequest(username,
                password, "null"));

        // try logging out with null authToken
        UserService.LogoutResult logoutResult = userService.logout(new UserService.LogoutRequest(null));
        Assertions.assertEquals("Error: unauthorized", logoutResult.message());
    }
}