package server;

import com.google.gson.Gson;
import io.javalin.websocket.WsContext;
import websocket.messages.ServerMessage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {

    public static class Connection {
        public final String username;
        public final Integer gameID;
        public final WsContext session;

        public Connection(String username, Integer gameID, WsContext session) {
            this.username = username;
            this.gameID = gameID;
            this.session = session;
        }
    }

    private final ConcurrentHashMap<Integer, ConcurrentHashMap<String, Connection>> connections = new ConcurrentHashMap<>();
    private final Gson gson = new Gson();

    public void add(String username, Integer gameID, WsContext session) {
        connections.computeIfAbsent(gameID, id -> new ConcurrentHashMap<>())
                .put(username, new Connection(username, gameID, session));
    }

    public void remove(String username, Integer gameID) {
        var gameConnections = connections.get(gameID);
        if (gameConnections == null) {
            return;
        }

        gameConnections.remove(username);

        if (gameConnections.isEmpty()) {
            connections.remove(gameID);
        }
    }

    public void sendToUser(String username, Integer gameID, ServerMessage message) {
        var gameConnections = connections.get(gameID);
        if (gameConnections == null) {
            return;
        }

        var connection = gameConnections.get(username);
        if (connection == null) {
            return;
        }

        if (connection.session.session.isOpen()) {
            connection.session.send(gson.toJson(message));
        }
    }

    public void broadcastToOthers(String excludeUsername, Integer gameID, ServerMessage message) {
        var gameConnections = connections.get(gameID);
        if (gameConnections == null) {
            return;
        }

        String json = gson.toJson(message);
        for (Map.Entry<String, Connection> entry : gameConnections.entrySet()) {
            if (entry.getKey().equals(excludeUsername)) {
                continue;
            }

            var connection = entry.getValue();
            if (connection.session.session.isOpen()) {
                connection.session.send(json);
            }
        }
    }

    public void broadcastToAll(Integer gameID, ServerMessage message) {
        var gameConnections = connections.get(gameID);
        if (gameConnections == null) {
            return;
        }

        String json = gson.toJson(message);
        for (Connection connection : gameConnections.values()) {
            if (connection.session.session.isOpen()) {
                connection.session.send(json);
            }
        }
    }
}