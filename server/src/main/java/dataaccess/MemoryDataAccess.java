package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class MemoryDataAccess implements DataAccess {

    private final Map<String, UserData> usersByUsername = new ConcurrentHashMap<>();
    private final Map<String, AuthData> authByToken = new ConcurrentHashMap<>();
    private final Map<Integer, GameData> gamesById = new ConcurrentHashMap<>();
    private final AtomicInteger nextGameId = new AtomicInteger(1);

    @Override
    public void clear() throws DataAccessException {
        usersByUsername.clear();
        authByToken.clear();
        gamesById.clear();
        nextGameId.set(1);
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        if (username == null) {
            return null;
        }
        return usersByUsername.get(username);
    }

    @Override
    public void insertUser(UserData user) throws DataAccessException {
        if (user == null || user.username == null) {
            throw new DataAccessException("User or username was null");
        }
        usersByUsername.put(user.username, user);
    }

    @Override
    public AuthData getAuth(String token) throws DataAccessException {
        if (token == null) {
            return null;
        }
        return authByToken.get(token);
    }

    @Override
    public void insertAuth(AuthData auth) throws DataAccessException {
        if (auth == null || auth.authToken == null) {
            throw new DataAccessException("Auth or token was null");
        }
        authByToken.put(auth.authToken, auth);
    }

    @Override
    public void deleteAuth(String token) throws DataAccessException {
        if (token == null) {
            return;
        }
        authByToken.remove(token);
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        return gamesById.get(gameID);
    }

    @Override
    public int insertGame(GameData game) throws DataAccessException {
        if (game == null) {
            throw new DataAccessException("Game was null");
        }
        int id = nextGameId.getAndIncrement();
        game.gameID = id;
        gamesById.put(id, game);
        return id;
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        if (game == null) {
            throw new DataAccessException("Game was null");
        }
        gamesById.put(game.gameID, game);
    }

    @Override
    public List<GameData> listGames() throws DataAccessException {
        return new ArrayList<>(gamesById.values());
    }
}