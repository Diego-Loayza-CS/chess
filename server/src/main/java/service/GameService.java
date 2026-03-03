package service;

import chess.ChessGame;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import model.GameListItem;
import model.request.CreateGameRequest;
import model.request.JoinGameRequest;
import model.result.CreateGameResult;
import model.result.ListGamesResult;

import java.util.ArrayList;
import java.util.List;

public class GameService {
    private final DataAccess dataAccess;

    public GameService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public ListGamesResult listGames(String authToken) throws DataAccessException {
        AuthData auth = requireAuth(authToken);

        List<GameData> games = dataAccess.listGames();
        List<GameListItem> items = new ArrayList<>();
        for (GameData g : games) {
            items.add(new GameListItem(g.gameID, g.whiteUsername, g.blackUsername, g.gameName));
        }
        return new ListGamesResult(items);
    }

    public CreateGameResult createGame(CreateGameRequest req, String authToken) throws DataAccessException {
        requireAuth(authToken);

        if (req == null || isBlank(req.gameName)) {
            throw new IllegalArgumentException("bad request");
        }

        GameData game = new GameData(0, null, null, req.gameName, new ChessGame());
        int id = dataAccess.insertGame(game);
        return new CreateGameResult(id);
    }

    public void joinGame(JoinGameRequest req, String authToken) throws DataAccessException {
        AuthData auth = requireAuth(authToken);

        if (req == null || req.gameID == null) {
            throw new IllegalArgumentException("bad request");
        }
        if (req.playerColor == null || req.playerColor.isBlank()) {
            throw new IllegalArgumentException("bad request");
        }
        if (!req.playerColor.equals("WHITE") && !req.playerColor.equals("BLACK")) {
            throw new IllegalArgumentException("bad request");
        }

        GameData game = dataAccess.getGame(req.gameID);
        if (game == null) {
            throw new IllegalArgumentException("bad request");
        }

        String username = auth.username;

        if (req.playerColor.equals("WHITE")) {
            if (game.whiteUsername != null && !game.whiteUsername.equals(username)) {
                throw new IllegalStateException("already taken");
            }
            game.whiteUsername = username;
        } else {
            if (game.blackUsername != null && !game.blackUsername.equals(username)) {
                throw new IllegalStateException("already taken");
            }
            game.blackUsername = username;
        }

        dataAccess.updateGame(game);
    }

    private AuthData requireAuth(String token) throws DataAccessException {
        if (isBlank(token)) {
            throw new SecurityException("unauthorized");
        }
        AuthData auth = dataAccess.getAuth(token);
        if (auth == null) {
            throw new SecurityException("unauthorized");
        }
        return auth;
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}