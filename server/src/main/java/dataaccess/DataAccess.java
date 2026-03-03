package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.List;

public interface DataAccess {
    void clear() throws DataAccessException;

    UserData getUser(String username) throws DataAccessException;
    void insertUser(UserData user) throws DataAccessException;

    AuthData getAuth(String token) throws DataAccessException;
    void insertAuth(AuthData auth) throws DataAccessException;
    void deleteAuth(String token) throws DataAccessException;

    GameData getGame(int gameID) throws DataAccessException;
    int insertGame(GameData game) throws DataAccessException;
    void updateGame(GameData game) throws DataAccessException;
    List<GameData> listGames() throws DataAccessException;
}