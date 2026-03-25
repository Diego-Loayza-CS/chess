package client;

import model.AuthData;
import model.GameListItem;
import org.junit.jupiter.api.*;
import server.Server;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    private static final String USERNAME = "player1";
    private static final String PASSWORD = "password";
    private static final String EMAIL = "player1@email.com";

    @BeforeAll
    public static void init() {
        server = new Server();
        int port = server.run(0);
        facade = new ServerFacade(port);
        System.out.println("Started test HTTP server on " + port);
    }

    @AfterAll
    public static void stopServer() {
        server.stop();
    }

    @BeforeEach
    public void clearDatabase() throws Exception {
        facade.clear();
    }

    @Test
    public void registerPositive() throws Exception {
        AuthData auth = facade.register(USERNAME, PASSWORD, EMAIL);
        assertNotNull(auth);
        assertEquals(USERNAME, auth.username);
        assertNotNull(auth.authToken);
    }

    @Test
    public void registerNegative() {
        assertThrows(Exception.class, () -> facade.register(null, PASSWORD, EMAIL));
    }

    @Test
    public void loginPositive() throws Exception {
        facade.register(USERNAME, PASSWORD, EMAIL);

        AuthData auth = facade.login(USERNAME, PASSWORD);

        assertNotNull(auth);
        assertEquals(USERNAME, auth.username);
        assertNotNull(auth.authToken);
    }

    @Test
    public void loginNegative() throws Exception {
        facade.register(USERNAME, PASSWORD, EMAIL);
        assertThrows(Exception.class, () -> facade.login(USERNAME, "wrongPassword"));
    }

    @Test
    public void logoutPositive() throws Exception {
        AuthData auth = facade.register(USERNAME, PASSWORD, EMAIL);
        assertDoesNotThrow(() -> facade.logout(auth.authToken));
    }

    @Test
    public void logoutNegative() {
        assertThrows(Exception.class, () -> facade.logout("bad-token"));
    }

    @Test
    public void createGamePositive() throws Exception {
        AuthData auth = facade.register(USERNAME, PASSWORD, EMAIL);

        int gameID = facade.createGame(auth.authToken, "test game");

        assertTrue(gameID > 0);
    }

    @Test
    public void createGameNegative() {
        assertThrows(Exception.class, () -> facade.createGame("bad-token", "test game"));
    }

    @Test
    public void listGamesPositive() throws Exception {
        AuthData auth = facade.register(USERNAME, PASSWORD, EMAIL);
        facade.createGame(auth.authToken, "game1");

        List<GameListItem> games = facade.listGames(auth.authToken);

        assertNotNull(games);
        assertEquals(1, games.size());
        assertEquals("game1", games.get(0).gameName);
    }

    @Test
    public void listGamesNegative() {
        assertThrows(Exception.class, () -> facade.listGames("bad-token"));
    }

    @Test
    public void joinGamePositive() throws Exception {
        AuthData auth = facade.register(USERNAME, PASSWORD, EMAIL);
        int gameID = facade.createGame(auth.authToken, "game1");

        assertDoesNotThrow(() -> facade.joinGame(auth.authToken, gameID, "WHITE"));
    }

    @Test
    public void joinGameNegative() throws Exception {
        AuthData auth = facade.register(USERNAME, PASSWORD, EMAIL);
        int gameID = facade.createGame(auth.authToken, "game1");

        assertThrows(Exception.class, () -> facade.joinGame("bad-token", gameID, "WHITE"));
    }

    @Test
    public void clearPositive() {
        assertDoesNotThrow(() -> facade.clear());
    }
}