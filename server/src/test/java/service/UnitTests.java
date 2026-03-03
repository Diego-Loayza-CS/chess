package service;

import chess.ChessGame;
import org.junit.jupiter.api.*;
import passoff.model.*;
import passoff.server.TestServerFacade;
import server.Server;

import java.net.HttpURLConnection;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UnitTests {

    private static Server server;
    private static TestServerFacade serverFacade;

    private static TestUser userA;
    private static TestUser userB;

    private String authA;

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeAll
    static void init() {
        server = new Server();
        var port = server.run(0);
        serverFacade = new TestServerFacade("localhost", Integer.toString(port));

        userA = new TestUser("UserA", "passA", "a@mail.com");
        userB = new TestUser("UserB", "passB", "b@mail.com");
    }

    @BeforeEach
    void setup() {
        serverFacade.clear();
        TestAuthResult reg = serverFacade.register(userA);
        authA = reg.getAuthToken();
    }

    @Test
    @Order(1)
    void staticFiles() {
        String html = serverFacade.file("/").replaceAll("\r", "");
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, serverFacade.getStatusCode());
        Assertions.assertNotNull(html);
        Assertions.assertTrue(html.contains("CS 240 Chess Server Web API"));
    }

    @Test
    @Order(2)
    void registerSuccess() {
        TestAuthResult reg = serverFacade.register(userB);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, serverFacade.getStatusCode());
        Assertions.assertEquals(userB.getUsername(), reg.getUsername());
        Assertions.assertNotNull(reg.getAuthToken());
    }

    @Test
    @Order(3)
    void loginBadRequest() {
        TestAuthResult r1 = serverFacade.login(new TestUser(null, userA.getPassword()));
        Assertions.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, serverFacade.getStatusCode());
        Assertions.assertNull(r1.getUsername());
        Assertions.assertNull(r1.getAuthToken());

        TestAuthResult r2 = serverFacade.login(new TestUser(userA.getUsername(), null));
        Assertions.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, serverFacade.getStatusCode());
        Assertions.assertNull(r2.getUsername());
        Assertions.assertNull(r2.getAuthToken());
    }

    @Test
    @Order(4)
    void createGameUnauthorized() {
        serverFacade.logout(authA);
        TestCreateResult create = serverFacade.createGame(new TestCreateRequest("G"), authA);
        Assertions.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, serverFacade.getStatusCode());
        Assertions.assertNull(create.getGameID());
    }

    @Test
    @Order(5)
    void joinStealColorForbidden() {
        TestCreateResult created = serverFacade.createGame(new TestCreateRequest("Seat"), authA);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, serverFacade.getStatusCode());

        TestJoinRequest joinBlack = new TestJoinRequest(ChessGame.TeamColor.BLACK, created.getGameID());
        TestResult join1 = serverFacade.joinPlayer(joinBlack, authA);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, serverFacade.getStatusCode());

        TestAuthResult regB = serverFacade.register(userB);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, serverFacade.getStatusCode());

        TestResult join2 = serverFacade.joinPlayer(joinBlack, regB.getAuthToken());
        Assertions.assertEquals(HttpURLConnection.HTTP_FORBIDDEN, serverFacade.getStatusCode());
        Assertions.assertNotNull(join2.getMessage());
    }
}