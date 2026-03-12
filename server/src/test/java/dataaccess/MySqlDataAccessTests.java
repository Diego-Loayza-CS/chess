package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MySqlDataAccessTests {

    private MySqlDataAccess dao;

    @BeforeEach
    public void setUp() throws DataAccessException {
        dao = new MySqlDataAccess();
        dao.clear();
    }

    @Test
    public void clearPositive() throws DataAccessException {
        dao.insertUser(new UserData("u1", "p1", "e1@test.com"));
        dao.insertAuth(new AuthData("t1", "u1"));
        dao.insertGame(new GameData(0, null, null, "game1", new ChessGame()));

        dao.clear();

        assertNull(dao.getUser("u1"));
        assertNull(dao.getAuth("t1"));
        assertEquals(0, dao.listGames().size());
    }

    @Test
    public void getUserPositive() throws DataAccessException {
        dao.insertUser(new UserData("u1", "p1", "e1@test.com"));

        UserData user = dao.getUser("u1");

        assertNotNull(user);
        assertEquals("u1", user.username);
        assertEquals("e1@test.com", user.email);
        assertNotEquals("p1", user.password);
    }

    @Test
    public void getUserNegative() throws DataAccessException {
        assertNull(dao.getUser("missing"));
    }

    @Test
    public void insertUserPositive() throws DataAccessException {
        dao.insertUser(new UserData("u1", "p1", "e1@test.com"));

        UserData user = dao.getUser("u1");
        assertNotNull(user);
        assertEquals("u1", user.username);
    }

    @Test
    public void insertUserNegative() throws DataAccessException {
        dao.insertUser(new UserData("u1", "p1", "e1@test.com"));

        assertThrows(DataAccessException.class, () ->
                dao.insertUser(new UserData("u1", "p2", "e2@test.com")));
    }

    @Test
    public void getAuthPositive() throws DataAccessException {
        dao.insertUser(new UserData("u1", "p1", "e1@test.com"));
        dao.insertAuth(new AuthData("t1", "u1"));

        AuthData auth = dao.getAuth("t1");

        assertNotNull(auth);
        assertEquals("t1", auth.authToken);
        assertEquals("u1", auth.username);
    }

    @Test
    public void getAuthNegative() throws DataAccessException {
        assertNull(dao.getAuth("missing"));
    }

    @Test
    public void insertAuthPositive() throws DataAccessException {
        dao.insertUser(new UserData("u1", "p1", "e1@test.com"));

        dao.insertAuth(new AuthData("t1", "u1"));

        assertNotNull(dao.getAuth("t1"));
    }

    @Test
    public void insertAuthNegative() {
        assertThrows(DataAccessException.class, () ->
                dao.insertAuth(new AuthData(null, "u1")));
    }

    @Test
    public void deleteAuthPositive() throws DataAccessException {
        dao.insertUser(new UserData("u1", "p1", "e1@test.com"));
        dao.insertAuth(new AuthData("t1", "u1"));

        dao.deleteAuth("t1");

        assertNull(dao.getAuth("t1"));
    }

    @Test
    public void deleteAuthNegative() {
        assertDoesNotThrow(() -> dao.deleteAuth("missing"));
    }

    @Test
    public void getGamePositive() throws DataAccessException {
        int id = dao.insertGame(new GameData(0, null, null, "game1", new ChessGame()));

        GameData game = dao.getGame(id);

        assertNotNull(game);
        assertEquals(id, game.gameID);
        assertEquals("game1", game.gameName);
        assertNotNull(game.game);
    }

    @Test
    public void getGameNegative() throws DataAccessException {
        assertNull(dao.getGame(99999));
    }

    @Test
    public void insertGamePositive() throws DataAccessException {
        int id = dao.insertGame(new GameData(0, null, null, "game1", new ChessGame()));

        assertTrue(id > 0);
        assertNotNull(dao.getGame(id));
    }

    @Test
    public void insertGameNegative() {
        assertThrows(DataAccessException.class, () ->
                dao.insertGame(new GameData(0, null, null, null, new ChessGame())));
    }

    @Test
    public void updateGamePositive() throws DataAccessException {
        int id = dao.insertGame(new GameData(0, null, null, "game1", new ChessGame()));
        GameData game = dao.getGame(id);

        game.whiteUsername = "whitePlayer";
        dao.updateGame(game);

        GameData updated = dao.getGame(id);
        assertEquals("whitePlayer", updated.whiteUsername);
    }

    @Test
    public void updateGameNegative() {
        assertThrows(DataAccessException.class, () ->
                dao.updateGame(new GameData(99999, null, null, "bad", new ChessGame())));
    }

    @Test
    public void listGamesPositive() throws DataAccessException {
        dao.insertGame(new GameData(0, null, null, "g1", new ChessGame()));
        dao.insertGame(new GameData(0, null, null, "g2", new ChessGame()));

        List<GameData> games = dao.listGames();

        assertEquals(2, games.size());
    }

    @Test
    public void listGamesNegative() throws DataAccessException {
        List<GameData> games = dao.listGames();
        assertNotNull(games);
        assertEquals(0, games.size());
    }
}