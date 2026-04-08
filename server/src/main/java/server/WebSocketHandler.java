package server;

import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import io.javalin.websocket.WsContext;
import model.AuthData;
import model.GameData;
import websocket.commands.ConnectCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

public class WebSocketHandler {

    private final DataAccess dataAccess;
    private final ConnectionManager connections = new ConnectionManager();
    private final Gson gson = new Gson();

    public WebSocketHandler(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public void onMessage(WsContext ctx) {
        try {
            UserGameCommand baseCommand = gson.fromJson(ctx.message(), UserGameCommand.class);
            if (baseCommand == null || baseCommand.commandType == null) {
                sendError(ctx, "Error: invalid websocket command");
                return;
            }

            switch (baseCommand.commandType) {
                case CONNECT -> connect(ctx, gson.fromJson(ctx.message(), ConnectCommand.class));
                case MAKE_MOVE, LEAVE, RESIGN -> sendError(ctx, "Error: command not implemented yet");
            }
        } catch (Exception ex) {
            sendError(ctx, "Error: " + ex.getMessage());
        }
    }

    private void connect(WsContext ctx, ConnectCommand command) throws DataAccessException {
        if (command == null || command.authToken == null || command.gameID == null) {
            sendError(ctx, "Error: invalid connect command");
            return;
        }

        AuthData auth = dataAccess.getAuth(command.authToken);
        if (auth == null) {
            sendError(ctx, "Error: unauthorized");
            return;
        }

        GameData game = dataAccess.getGame(command.gameID);
        if (game == null) {
            sendError(ctx, "Error: game does not exist");
            return;
        }

        String username = auth.username;
        connections.add(username, command.gameID, ctx);

        connections.sendToUser(username, command.gameID, new LoadGameMessage(game));

        String message;
        if (username.equals(game.whiteUsername)) {
            message = username + " connected as white";
        } else if (username.equals(game.blackUsername)) {
            message = username + " connected as black";
        } else {
            message = username + " connected as an observer";
        }

        connections.broadcastToOthers(username, command.gameID, new NotificationMessage(message));
    }

    private void sendError(WsContext ctx, String message) {
        ctx.send(gson.toJson(new ErrorMessage(message)));
    }
}