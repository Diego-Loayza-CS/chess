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

}