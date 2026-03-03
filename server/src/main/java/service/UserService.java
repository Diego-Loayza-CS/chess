package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;
import model.request.RegisterRequest;
import model.result.RegisterResult;

import java.util.UUID;

public class UserService {
    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public RegisterResult register(RegisterRequest req) throws DataAccessException {
        if (req == null || isBlank(req.username) || isBlank(req.password) || isBlank(req.email)) {
            throw new IllegalArgumentException("bad request");
        }
        if (dataAccess.getUser(req.username) != null) {
            throw new IllegalStateException("username already taken");
        }

        dataAccess.insertUser(new UserData(req.username, req.password, req.email));

        String token = UUID.randomUUID().toString();
        dataAccess.insertAuth(new AuthData(token, req.username));
        return new RegisterResult(req.username, token);
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}