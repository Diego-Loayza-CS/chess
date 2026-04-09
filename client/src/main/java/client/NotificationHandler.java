package client;

import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

public interface NotificationHandler {
    void notifyLoadGame(LoadGameMessage message);
    void notifyNotification(NotificationMessage message);
    void notifyError(ErrorMessage message);
}