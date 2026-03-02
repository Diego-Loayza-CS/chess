package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class MemoryDataAccess implements DataAccess {

    private final Map<String, UserData> users = new ConcurrentHashMap<>();
    private final Map<String, AuthData> authTokens = new ConcurrentHashMap<>();
    private final Map<Integer, GameData> games = new ConcurrentHashMap<>();
    private final AtomicInteger nextGameId = new AtomicInteger(1);

    @Override
    public void clear() {
        users.clear();
        authTokens.clear();
        games.clear();
        nextGameId.set(1);
    }

    // USERS
    @Override
    public UserData getUser(String username) {
        return users.get(username);
    }

    @Override
    public void insertUser(UserData user) throws DataAccessException {
        if (user == null || user.username() == null) {
            throw new DataAccessException("Invalid user");
        }
        users.put(user.username(), user);
    }

    // AUTH
    @Override
    public AuthData getAuth(String authToken) {
        return authTokens.get(authToken);
    }

    @Override
    public void insertAuth(AuthData auth) throws DataAccessException {
        if (auth == null || auth.authToken() == null) {
            throw new DataAccessException("Invalid auth");
        }
        authTokens.put(auth.authToken(), auth);
    }

    @Override
    public void deleteAuth(String authToken) {
        authTokens.remove(authToken);
    }

    // GAMES
    @Override
    public GameData getGame(int gameID) {
        return games.get(gameID);
    }

    @Override
    public Collection<GameData> listGames() {
        return new ArrayList<>(games.values());
    }

    @Override
    public int insertGame(GameData game) throws DataAccessException {
        if (game == null) {
            throw new DataAccessException("Invalid game");
        }

        int id = nextGameId.getAndIncrement();
        GameData stored = new GameData(
                id,
                game.whiteUsername(),
                game.blackUsername(),
                game.gameName(),
                game.game()
        );

        games.put(id, stored);
        return id;
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        if (game == null) {
            throw new DataAccessException("Invalid game");
        }

        if (!games.containsKey(game.gameID())) {
            throw new DataAccessException("Game does not exist");
        }

        games.put(game.gameID(), game);
    }
}