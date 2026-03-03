package service;

import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import model.request.CreateGameRequest;
import model.request.JoinGameRequest;
import model.request.LoginRequest;
import model.request.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class UnitTests {

    private DataAccess dataAccess;
    private ClearService clearService;
    private UserService userService;
    private SessionService sessionService;
    private GameService gameService;

    @BeforeEach
    void setup() {
        dataAccess = new MemoryDataAccess();
        clearService = new ClearService(dataAccess);
        userService = new UserService(dataAccess);
        sessionService = new SessionService(dataAccess);
        gameService = new GameService(dataAccess);
    }

    @Test
    void clearSuccess() throws Exception {
        Object reg = userService.register(registerReq("u", "p", "e@mail.com"));
        String token = (String) read(reg, "authToken", "getAuthToken");
        assertNotNull(token);
        clearService.clear();
        assertThrows(SecurityException.class, () -> sessionService.login(loginReq("u", "p")));
    }

    @Test
    void clearDoesNotThrow() {
        assertDoesNotThrow(() -> clearService.clear());
    }

    @Test
    void registerSuccess() throws Exception {
        Object reg = userService.register(registerReq("u", "p", "e@mail.com"));
        String token = (String) read(reg, "authToken", "getAuthToken");
        assertNotNull(token);
    }

    @Test
    void registerBadRequest() {
        RegisterRequest req = new RegisterRequest();
        req.username = null;
        req.password = "p";
        req.email = "e@mail.com";
        assertThrows(IllegalArgumentException.class, () -> userService.register(req));
    }

    @Test
    void loginSuccess() throws Exception {
        userService.register(registerReq("u", "p", "e@mail.com"));
        Object login = sessionService.login(loginReq("u", "p"));
        String token = (String) read(login, "authToken", "getAuthToken");
        assertNotNull(token);
    }

    @Test
    void loginUnauthorized() throws Exception {
        userService.register(registerReq("u", "p", "e@mail.com"));
        assertThrows(SecurityException.class, () -> sessionService.login(loginReq("u", "bad")));
    }

    @Test
    void logoutSuccess() throws Exception {
        Object reg = userService.register(registerReq("u", "p", "e@mail.com"));
        String token = (String) read(reg, "authToken", "getAuthToken");
        sessionService.logout(token);
        assertThrows(SecurityException.class, () -> gameService.listGames(token));
    }

    @Test
    void logoutUnauthorized() {
        assertThrows(SecurityException.class, () -> sessionService.logout("bad-token"));
    }

    @Test
    void createGameSuccess() throws Exception {
        Object reg = userService.register(registerReq("u", "p", "e@mail.com"));
        String token = (String) read(reg, "authToken", "getAuthToken");
        Object created = gameService.createGame(createReq("g"), token);
        Integer gameId = (Integer) read(created, "gameID", "getGameID");
        assertNotNull(gameId);
        assertTrue(gameId > 0);
    }

    @Test
    void createGameUnauthorized() {
        assertThrows(SecurityException.class, () -> gameService.createGame(createReq("g"), "bad-token"));
    }

    @Test
    void listGamesSuccess() throws Exception {
        Object reg = userService.register(registerReq("u", "p", "e@mail.com"));
        String token = (String) read(reg, "authToken", "getAuthToken");
        gameService.createGame(createReq("g"), token);

        Object list = gameService.listGames(token);
        Object games = read(list, "games", "getGames");
        assertNotNull(games);
        assertEquals(1, sizeOf(games));
    }

    @Test
    void listGamesUnauthorized() {
        assertThrows(SecurityException.class, () -> gameService.listGames("bad-token"));
    }

    @Test
    void joinGameSuccess() throws Exception {
        Object reg = userService.register(registerReq("u", "p", "e@mail.com"));
        String token = (String) read(reg, "authToken", "getAuthToken");
        Object created = gameService.createGame(createReq("g"), token);
        Integer gameId = (Integer) read(created, "gameID", "getGameID");

        gameService.joinGame(joinReq("WHITE", gameId), token);

        Object list = gameService.listGames(token);
        Object games = read(list, "games", "getGames");
        Object first = firstOf(games);
        Object whiteUsername = read(first, "whiteUsername", "getWhiteUsername");
        assertEquals("u", whiteUsername);
    }

    @Test
    void joinGameStealColor() throws Exception {
        Object regA = userService.register(registerReq("a", "p", "a@mail.com"));
        Object regB = userService.register(registerReq("b", "p", "b@mail.com"));
        String tokenA = (String) read(regA, "authToken", "getAuthToken");
        String tokenB = (String) read(regB, "authToken", "getAuthToken");

        Object created = gameService.createGame(createReq("g"), tokenA);
        Integer gameId = (Integer) read(created, "gameID", "getGameID");

        gameService.joinGame(joinReq("WHITE", gameId), tokenA);
        assertThrows(IllegalStateException.class, () -> gameService.joinGame(joinReq("WHITE", gameId), tokenB));
    }

    private RegisterRequest registerReq(String u, String p, String e) {
        RegisterRequest req = new RegisterRequest();
        req.username = u;
        req.password = p;
        req.email = e;
        return req;
    }

    private LoginRequest loginReq(String u, String p) {
        LoginRequest req = new LoginRequest();
        req.username = u;
        req.password = p;
        return req;
    }

    private CreateGameRequest createReq(String name) {
        CreateGameRequest req = new CreateGameRequest();
        req.gameName = name;
        return req;
    }

    private JoinGameRequest joinReq(String color, Integer gameId) {
        JoinGameRequest req = new JoinGameRequest();
        req.playerColor = color;
        req.gameID = gameId;
        return req;
    }

    private Object read(Object obj, String fieldName, String getterName) throws Exception {
        try {
            Method m = obj.getClass().getMethod(getterName);
            return m.invoke(obj);
        } catch (NoSuchMethodException ignored) {
            Field f = obj.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            return f.get(obj);
        }
    }

    private int sizeOf(Object games) {
        if (games == null) {
            return 0;
        }
        if (games.getClass().isArray()) {
            return Array.getLength(games);
        }
        if (games instanceof java.util.Collection<?> c) {
            return c.size();
        }
        return 0;
    }

    private Object firstOf(Object games) {
        if (games.getClass().isArray()) {
            return Array.get(games, 0);
        }
        if (games instanceof java.util.List<?> list) {
            return list.get(0);
        }
        if (games instanceof java.util.Collection<?> c) {
            return c.iterator().next();
        }
        throw new IllegalStateException("Unsupported games container");
    }
}