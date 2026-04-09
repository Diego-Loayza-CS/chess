package server;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import io.javalin.websocket.WsMessageContext;
import model.AuthData;
import model.GameData;
import websocket.commands.ConnectCommand;
import websocket.commands.MakeMoveCommand;
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

    public void onMessage(WsMessageContext ctx) {
        try {
            String messageJson = ctx.message();

            UserGameCommand baseCommand = gson.fromJson(messageJson, UserGameCommand.class);
            if (baseCommand == null || baseCommand.getCommandType() == null) {
                sendError(ctx, "Error: invalid websocket command");
                return;
            }

            switch (baseCommand.getCommandType()) {
                case CONNECT -> connect(ctx, gson.fromJson(messageJson, ConnectCommand.class));
                case MAKE_MOVE -> makeMove(ctx, gson.fromJson(messageJson, MakeMoveCommand.class));
                case LEAVE, RESIGN -> sendError(ctx, "Error: command not implemented yet");
            }
        } catch (Exception ex) {
            sendError(ctx, "Error: " + ex.getMessage());
        }
    }

    private void connect(WsMessageContext ctx, ConnectCommand command) throws DataAccessException {
        if (command == null || command.getAuthToken() == null || command.getGameID() == null) {
            sendError(ctx, "Error: invalid connect command");
            return;
        }

        AuthData auth = dataAccess.getAuth(command.getAuthToken());
        if (auth == null) {
            sendError(ctx, "Error: unauthorized");
            return;
        }

        GameData game = dataAccess.getGame(command.getGameID());
        if (game == null) {
            sendError(ctx, "Error: game does not exist");
            return;
        }

        String username = auth.username;
        connections.add(username, command.getGameID(), ctx);

        connections.sendToUser(username, command.getGameID(), new LoadGameMessage(game));

        String message;
        if (username.equals(game.whiteUsername)) {
            message = username + " connected as white";
        } else if (username.equals(game.blackUsername)) {
            message = username + " connected as black";
        } else {
            message = username + " connected as an observer";
        }

        connections.broadcastToOthers(username, command.getGameID(), new NotificationMessage(message));
    }

    private void makeMove(WsMessageContext ctx, MakeMoveCommand command) throws DataAccessException {
        if (command == null || command.getAuthToken() == null || command.getGameID() == null || command.move == null) {
            sendError(ctx, "Error: invalid move command");
            return;
        }

        AuthData auth = dataAccess.getAuth(command.getAuthToken());
        if (auth == null) {
            sendError(ctx, "Error: unauthorized");
            return;
        }

        GameData gameData = dataAccess.getGame(command.getGameID());
        if (gameData == null) {
            sendError(ctx, "Error: game does not exist");
            return;
        }

        if (gameData.gameOver) {
            sendError(ctx, "Error: game is over");
            return;
        }

        String username = auth.username;
        boolean isWhite = username.equals(gameData.whiteUsername);
        boolean isBlack = username.equals(gameData.blackUsername);

        if (!isWhite && !isBlack) {
            sendError(ctx, "Error: observers cannot make moves");
            return;
        }

        ChessGame.TeamColor playerColor = isWhite ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
        if (gameData.game.getTeamTurn() != playerColor) {
            sendError(ctx, "Error: not your turn");
            return;
        }

        ChessMove move = command.move;

        try {
            gameData.game.makeMove(move);
        } catch (Exception ex) {
            sendError(ctx, "Error: illegal move");
            return;
        }

        dataAccess.updateGame(gameData);

        connections.broadcastToAll(command.getGameID(), new LoadGameMessage(gameData));
        connections.broadcastToOthers(username, command.getGameID(),
                new NotificationMessage(username + " made move " + describeMove(move)));

        ChessGame.TeamColor sideToMove = gameData.game.getTeamTurn();
        String sideToMoveName = sideToMove == ChessGame.TeamColor.WHITE ? gameData.whiteUsername : gameData.blackUsername;

        if (gameData.game.isInCheckmate(sideToMove)) {
            gameData.gameOver = true;
            dataAccess.updateGame(gameData);
            connections.broadcastToAll(command.getGameID(),
                    new NotificationMessage(sideToMoveName + " is in checkmate"));
        } else if (gameData.game.isInStalemate(sideToMove)) {
            gameData.gameOver = true;
            dataAccess.updateGame(gameData);
            connections.broadcastToAll(command.getGameID(),
                    new NotificationMessage("Stalemate"));
        } else if (gameData.game.isInCheck(sideToMove)) {
            connections.broadcastToAll(command.getGameID(),
                    new NotificationMessage(sideToMoveName + " is in check"));
        }
    }

    private String describeMove(ChessMove move) {
        return positionName(move.getStartPosition().getRow(), move.getStartPosition().getColumn()) +
                " to " +
                positionName(move.getEndPosition().getRow(), move.getEndPosition().getColumn());
    }

    private String positionName(int row, int col) {
        char file = (char) ('a' + col - 1);
        return "" + file + row;
    }

    private void sendError(WsMessageContext ctx, String message) {
        ctx.send(gson.toJson(new ErrorMessage(message)));
    }
}