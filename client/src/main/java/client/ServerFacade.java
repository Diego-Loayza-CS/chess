package client;

import com.google.gson.Gson;
import model.AuthData;
import model.GameListItem;
import model.request.CreateGameRequest;
import model.request.JoinGameRequest;
import model.request.LoginRequest;
import model.request.RegisterRequest;
import model.result.CreateGameResult;
import model.result.ErrorResult;
import model.result.ListGamesResult;
import model.result.LoginResult;
import model.result.RegisterResult;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.List;

public class ServerFacade {
    private final String serverUrl;
    private final Gson gson = new Gson();

    public ServerFacade(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public ServerFacade(int port) {
        this.serverUrl = "http://localhost:" + port;
    }

    public AuthData register(String username, String password, String email) throws Exception {
        var req = new RegisterRequest();
        req.username = username;
        req.password = password;
        req.email = email;

        RegisterResult result = makeRequest("POST", "/user", null, req, RegisterResult.class);
        if (result == null || result.authToken == null || result.username == null) {
            throw new Exception("Error: invalid server response");
        }

        return new AuthData(result.authToken, result.username);
    }

    public AuthData login(String username, String password) throws Exception {
        var req = new LoginRequest();
        req.username = username;
        req.password = password;

        LoginResult result = makeRequest("POST", "/session", null, req, LoginResult.class);
        if (result == null || result.authToken == null || result.username == null) {
            throw new Exception("Error: invalid server response");
        }

        return new AuthData(result.authToken, result.username);
    }

    public void logout(String authToken) throws Exception {
        makeRequest("DELETE", "/session", authToken, null, null);
    }

    public int createGame(String authToken, String gameName) throws Exception {
        var req = new CreateGameRequest();
        req.gameName = gameName;

        CreateGameResult result = makeRequest("POST", "/game", authToken, req, CreateGameResult.class);
        if (result == null) {
            throw new Exception("Error: invalid server response");
        }

        return result.gameID;
    }

    public List<GameListItem> listGames(String authToken) throws Exception {
        ListGamesResult result = makeRequest("GET", "/game", authToken, null, ListGamesResult.class);
        if (result == null || result.games == null) {
            throw new Exception("Error: invalid server response");
        }

        return result.games;
    }

    public void joinGame(String authToken, int gameID, String playerColor) throws Exception {
        var req = new JoinGameRequest();
        req.gameID = gameID;
        req.playerColor = playerColor;

        makeRequest("PUT", "/game", authToken, req, null);
    }

    public void clear() throws Exception {
        makeRequest("DELETE", "/db", null, null, null);
    }

    private <T> T makeRequest(String method, String path, String authToken, Object request, Class<T> responseClass)
            throws Exception {
        URI uri = new URI(serverUrl + path);
        HttpURLConnection http = (HttpURLConnection) uri.toURL().openConnection();
        http.setRequestMethod(method);
        http.setDoInput(true);

        if (authToken != null) {
            http.setRequestProperty("Authorization", authToken);
        }

        if (request != null) {
            http.setDoOutput(true);
            http.setRequestProperty("Content-Type", "application/json");
            try (OutputStream body = http.getOutputStream()) {
                body.write(gson.toJson(request).getBytes());
            }
        }

        http.connect();

        int status = http.getResponseCode();
        if (status / 100 != 2) {
            throw readError(http);
        }

        if (responseClass == null) {
            return null;
        }

        try (InputStream body = http.getInputStream()) {
            return gson.fromJson(new String(body.readAllBytes()), responseClass);
        }
    }

    private Exception readError(HttpURLConnection http) {
        try (InputStream err = http.getErrorStream()) {
            if (err != null) {
                String json = new String(err.readAllBytes());
                ErrorResult error = gson.fromJson(json, ErrorResult.class);
                if (error != null && error.message != null) {
                    return new Exception(error.message);
                }
            }
        } catch (Exception ignored) {
        }
        return new Exception("request failed: " + safeStatus(http));
    }

    private int safeStatus(HttpURLConnection http) {
        try {
            return http.getResponseCode();
        } catch (Exception ex) {
            return -1;
        }
    }
}