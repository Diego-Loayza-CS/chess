package client;

import com.google.gson.Gson;
import jakarta.websocket.*;
import websocket.commands.ConnectCommand;
import websocket.commands.LeaveCommand;
import websocket.commands.MakeMoveCommand;
import websocket.commands.ResignCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.net.URI;

@ClientEndpoint
public class WebSocketCommunicator {

    private final NotificationHandler notificationHandler;
    private final Gson gson = new Gson();
    private Session session;

    public WebSocketCommunicator(String serverUrl, NotificationHandler notificationHandler) throws Exception {
        this.notificationHandler = notificationHandler;

        String wsUrl = serverUrl.replace("http://", "ws://").replace("https://", "wss://") + "/ws";
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        container.connectToServer(this, URI.create(wsUrl));
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
    }

    @OnMessage
    public void onMessage(String messageJson) {
        try {
            ServerMessage base = gson.fromJson(messageJson, ServerMessage.class);
            if (base == null || base.getServerMessageType() == null) {
                return;
            }

            switch (base.getServerMessageType()) {
                case LOAD_GAME -> notificationHandler.notifyLoadGame(gson.fromJson(messageJson, LoadGameMessage.class));
                case ERROR -> notificationHandler.notifyError(gson.fromJson(messageJson, ErrorMessage.class));
                case NOTIFICATION -> notificationHandler.notifyNotification(gson.fromJson(messageJson, NotificationMessage.class));
            }
        } catch (Exception ignored) {
        }
    }

    public void connect(String authToken, Integer gameID) throws Exception {
        send(new ConnectCommand(authToken, gameID));
    }

    public void leave(String authToken, Integer gameID) throws Exception {
        send(new LeaveCommand(authToken, gameID));
    }

    public void resign(String authToken, Integer gameID) throws Exception {
        send(new ResignCommand(authToken, gameID));
    }

    public void makeMove(String authToken, Integer gameID, chess.ChessMove move) throws Exception {
        send(new MakeMoveCommand(authToken, gameID, move));
    }

    public void close() {
        try {
            if (session != null && session.isOpen()) {
                session.close();
            }
        } catch (Exception ignored) {
        }
    }

    private void send(Object command) throws Exception {
        if (session == null || !session.isOpen()) {
            throw new Exception("The connection has been closed.");
        }
        session.getBasicRemote().sendText(gson.toJson(command));
    }
}