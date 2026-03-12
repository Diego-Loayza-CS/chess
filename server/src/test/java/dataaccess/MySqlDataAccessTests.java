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
}