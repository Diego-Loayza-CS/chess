package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.Collection;

public interface DataAccess {

    // CLEAR
    void clear() throws DataAccessException;

    // USERS
    UserData getUser(String username) throws DataAccessException;
    void insertUser(UserData user) throws DataAccessException;

    // AUTH
    AuthData getAuth(String authToken) throws DataAccessException;
    void insertAuth(AuthData auth) throws DataAccessException;
    void deleteAuth(String authToken) throws DataAccessException;

    // GAMES
    GameData getGame(int gameID) throws DataAccessException;
    Collection<GameData> listGames() throws DataAccessException;
    int insertGame(GameData game) throws DataAccessException;
    void updateGame(GameData game) throws DataAccessException;
}