package websocket.messages;

public class ServerMessage {
    public enum ServerMessageType {
        LOAD_GAME,
        ERROR,
        NOTIFICATION
    }

    public ServerMessageType serverMessageType;

    public ServerMessage(ServerMessageType serverMessageType) {
        this.serverMessageType = serverMessageType;
    }

    public ServerMessageType getServerMessageType() {
        return serverMessageType;
    }
}