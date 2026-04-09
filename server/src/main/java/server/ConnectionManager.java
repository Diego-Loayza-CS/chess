package server;

import com.google.gson.Gson;
import io.javalin.websocket.WsContext;
import websocket.messages.ServerMessage;

import java.util.Iterator;
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

        if (!safeSend(connection, gson.toJson(message))) {
            remove(username, gameID);
        }
    }

    public void broadcastToOthers(String excludeUsername, Integer gameID, ServerMessage message) {
        var gameConnections = connections.get(gameID);
        if (gameConnections == null) {
            return;
        }

        String json = gson.toJson(message);

        Iterator<Map.Entry<String, Connection>> iterator = gameConnections.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            String username = entry.getKey();
            Connection connection = entry.getValue();

            if (username.equals(excludeUsername)) {
                continue;
            }

            if (!safeSend(connection, json)) {
                iterator.remove();
            }
        }

        if (gameConnections.isEmpty()) {
            connections.remove(gameID);
        }
    }

    public void broadcastToAll(Integer gameID, ServerMessage message) {
        var gameConnections = connections.get(gameID);
        if (gameConnections == null) {
            return;
        }

        String json = gson.toJson(message);

        Iterator<Map.Entry<String, Connection>> iterator = gameConnections.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            Connection connection = entry.getValue();

            if (!safeSend(connection, json)) {
                iterator.remove();
            }
        }

        if (gameConnections.isEmpty()) {
            connections.remove(gameID);
        }
    }

    private boolean safeSend(Connection connection, String json) {
        try {
            connection.session.send(json);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}