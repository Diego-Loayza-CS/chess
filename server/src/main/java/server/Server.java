package server;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import io.javalin.Javalin;
import io.javalin.http.Context;
import model.*;
import model.request.*;
import model.result.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Server {

    private final Javalin javalin;
    private final DataAccess dataAccess = new MemoryDataAccess();
    private final Gson gson = new Gson();

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        javalin.exception(DataAccessException.class, (ex, ctx) -> {
            ctx.status(500);
            writeJson(ctx, new ErrorResult("Error: " + ex.getMessage()));
        });

        registerRoutes();
    }

    private void registerRoutes() {

        // Clear application
        javalin.delete("/db", ctx -> {
            dataAccess.clear();
            okEmpty(ctx);
        });

        // Register
        javalin.post("/user", ctx -> {
            RegisterRequest req = safeBody(ctx, RegisterRequest.class);
            if (req == null || isBlank(req.username) || isBlank(req.password) || isBlank(req.email)) {
                badRequest(ctx);
                return;
            }

            UserData existing = dataAccess.getUser(req.username);
            if (existing != null) {
                ctx.status(403);
                writeJson(ctx, new ErrorResult("Error: username already taken"));
                return;
            }

            dataAccess.insertUser(new UserData(req.username, req.password, req.email));

            String token = newToken();
            dataAccess.insertAuth(new AuthData(token, req.username));

            ctx.status(200);
            writeJson(ctx, new RegisterResult(req.username, token));
        });

        // Login
        javalin.post("/session", ctx -> {
            LoginRequest req = safeBody(ctx, LoginRequest.class);

            if (req == null || isBlank(req.username) || isBlank(req.password)) {
                badRequest(ctx);
                return;
            }

            UserData user = dataAccess.getUser(req.username);

            if (user == null || user.password == null || !user.password.equals(req.password)) {
                unauthorized(ctx);
                return;
            }

            String token = newToken();
            dataAccess.insertAuth(new AuthData(token, req.username));

            ctx.status(200);
            writeJson(ctx, new LoginResult(req.username, token));
        });

        // Logout
        javalin.delete("/session", ctx -> {
            String token = requireAuth(ctx);
            if (token == null) {
                return;
            }

            AuthData auth = dataAccess.getAuth(token);
            if (auth == null) {
                unauthorized(ctx);
                return;
            }

            dataAccess.deleteAuth(token);
            okEmpty(ctx);
        });

        // List games
        javalin.get("/game", ctx -> {
            String token = requireAuth(ctx);
            if (token == null) {
                return;
            }

            AuthData auth = dataAccess.getAuth(token);
            if (auth == null) {
                unauthorized(ctx);
                return;
            }

            List<GameData> games = dataAccess.listGames();
            List<GameListItem> items = new ArrayList<>();

            for (GameData g : games) {
                items.add(new GameListItem(
                        g.gameID,
                        g.whiteUsername,
                        g.blackUsername,
                        g.gameName));
            }

            ctx.status(200);
            writeJson(ctx, new ListGamesResult(items));
        });

        // Create game
        javalin.post("/game", ctx -> {
            String token = requireAuth(ctx);
            if (token == null) {
                return;
            }

            AuthData auth = dataAccess.getAuth(token);
            if (auth == null) {
                unauthorized(ctx);
                return;
            }

            CreateGameRequest req = safeBody(ctx, CreateGameRequest.class);
            if (req == null || isBlank(req.gameName)) {
                badRequest(ctx);
                return;
            }

            GameData game = new GameData(0, null, null, req.gameName, new ChessGame());
            int id = dataAccess.insertGame(game);

            ctx.status(200);
            writeJson(ctx, new CreateGameResult(id));
        });

        // Join game
        javalin.put("/game", ctx -> {
            String token = requireAuth(ctx);
            if (token == null) {
                return;
            }

            AuthData auth = dataAccess.getAuth(token);
            if (auth == null) {
                unauthorized(ctx);
                return;
            }

            JoinGameRequest req = safeBody(ctx, JoinGameRequest.class);
            if (req == null || req.gameID == null) {
                badRequest(ctx);
                return;
            }

            if (req.playerColor == null || req.playerColor.isBlank()) {
                badRequest(ctx);
                return;
            }

            if (!req.playerColor.equals("WHITE") && !req.playerColor.equals("BLACK")) {
                badRequest(ctx);
                return;
            }

            GameData game = dataAccess.getGame(req.gameID);
            if (game == null) {
                badRequest(ctx);
                return;
            }

            String username = auth.username;

            if (req.playerColor.equals("WHITE")) {
                if (game.whiteUsername != null && !game.whiteUsername.equals(username)) {
                    alreadyTaken(ctx);
                    return;
                }
                game.whiteUsername = username;
            } else {
                if (game.blackUsername != null && !game.blackUsername.equals(username)) {
                    alreadyTaken(ctx);
                    return;
                }
                game.blackUsername = username;
            }

            dataAccess.updateGame(game);
            okEmpty(ctx);
        });
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }

    // ---------------- Helpers ----------------

    private <T> T safeBody(Context ctx, Class<T> clazz) {
        try {
            String body = ctx.body();
            if (body == null || body.isBlank()) {
                return null;
            }
            return gson.fromJson(body, clazz);
        } catch (Exception e) {
            return null;
        }
    }

    private void writeJson(Context ctx, Object obj) {
        ctx.contentType("application/json");
        ctx.result(gson.toJson(obj));
    }

    private String requireAuth(Context ctx) {
        String token = ctx.header("authorization");
        if (token == null) {
            token = ctx.header("Authorization");
        }

        if (token == null || token.isBlank()) {
            unauthorized(ctx);
            return null;
        }

        return token;
    }

    private void okEmpty(Context ctx) {
        ctx.status(200);
        ctx.contentType("application/json");
        ctx.result("{}");
    }

    private void unauthorized(Context ctx) {
        ctx.status(401);
        writeJson(ctx, new ErrorResult("Error: unauthorized"));
    }

    private void badRequest(Context ctx) {
        ctx.status(400);
        writeJson(ctx, new ErrorResult("Error: bad request"));
    }

    private void alreadyTaken(Context ctx) {
        ctx.status(403);
        writeJson(ctx, new ErrorResult("Error: already taken"));
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private String newToken() {
        return UUID.randomUUID().toString();
    }
}