package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;
import model.request.LoginRequest;
import model.result.LoginResult;
import org.mindrot.jbcrypt.BCrypt;

import java.util.UUID;

public class SessionService {
    private final DataAccess dataAccess;

    public SessionService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public LoginResult login(LoginRequest req) throws DataAccessException {
        if (req == null || isBlank(req.username) || isBlank(req.password)) {
            throw new IllegalArgumentException("bad request");
        }

        UserData user = dataAccess.getUser(req.username);
        if (user == null || user.password == null || !BCrypt.checkpw(req.password, user.password)) {
            throw new SecurityException("unauthorized");
        }

        String token = UUID.randomUUID().toString();
        dataAccess.insertAuth(new AuthData(token, req.username));
        return new LoginResult(req.username, token);
    }

    public void logout(String authToken) throws DataAccessException {
        if (isBlank(authToken)) {
            throw new SecurityException("unauthorized");
        }
        if (dataAccess.getAuth(authToken) == null) {
            throw new SecurityException("unauthorized");
        }
        dataAccess.deleteAuth(authToken);
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}