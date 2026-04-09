package ui;

import chess.*;
import client.NotificationHandler;
import client.WebSocketCommunicator;
import model.GameListItem;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GameplayClient implements NotificationHandler {

    private final State state;
    private final String serverUrl;

    private WebSocketCommunicator communicator;
    private Integer gameID;
    private boolean whitePerspective = true;
    private boolean observer = true;
    private ChessGame currentGame;
    private final List<String> notifications = new ArrayList<>();

    public GameplayClient(String serverUrl, State state) {
        this.serverUrl = serverUrl;
        this.state = state;
    }

    public String enterAsPlayer(GameListItem game, String color) throws Exception {
        observer = false;
        whitePerspective = !color.equalsIgnoreCase("BLACK");
        gameID = game.gameID;

        communicator = new WebSocketCommunicator(serverUrl, this);
        communicator.connect(state.getAuthToken(), gameID);

        return "Connected to game. Waiting for board...";
    }

    public String enterAsObserver(GameListItem game) throws Exception {
        observer = true;
        whitePerspective = true;
        gameID = game.gameID;

        communicator = new WebSocketCommunicator(serverUrl, this);
        communicator.connect(state.getAuthToken(), gameID);

        return "Observing game. Waiting for board...";
    }

    public String eval(String input) {
        try {
            String trimmed = input == null ? "" : input.trim();
            if (trimmed.isEmpty()) {
                return help();
            }

            String[] tokens = trimmed.split("\\s+");
            String command = tokens[0].toLowerCase();

            return switch (command) {
                case "help" -> help();
                case "redraw" -> redraw();
                case "leave" -> leave();
                case "resign" -> resign(tokens);
                case "move" -> move(tokens);
                case "highlight" -> highlight(tokens);
                default -> "Unknown command. Type help to see available commands.";
            };
        } catch (Exception ex) {
            return "Error: " + ex.getMessage();
        }
    }

    private String help() {
        return """
                Available gameplay commands:
                  help
                  redraw
                  leave
                  move <start> <end> [promotion]
                  resign yes
                  highlight <position>
                """;
    }

    private String redraw() {
        if (currentGame == null) {
            return "No game loaded yet.";
        }
        return ChessBoardPrinter.drawBoard(currentGame.getBoard(), whitePerspective, null);
    }

    private String leave() throws Exception {
        if (communicator != null) {
            communicator.leave(state.getAuthToken(), gameID);
            communicator.close();
        }
        state.setInGameplay(false);
        state.setCurrentGameID(null);
        return "Left game.";
    }

    private String resign(String[] tokens) throws Exception {
        if (observer) {
            return "Error: observers cannot resign.";
        }

        if (tokens.length < 2 || !tokens[1].equalsIgnoreCase("yes")) {
            return "Type 'resign yes' to confirm resignation.";
        }

        communicator.resign(state.getAuthToken(), gameID);
        return "Resignation sent.";
    }

    private String move(String[] tokens) throws Exception {
        if (observer) {
            return "Error: observers cannot make moves.";
        }
        if (tokens.length < 3 || tokens.length > 4) {
            return "Usage: move <start> <end> [promotion]";
        }
        if (currentGame == null) {
            return "No game loaded yet.";
        }

        ChessPosition start = parsePosition(tokens[1]);
        ChessPosition end = parsePosition(tokens[2]);
        ChessPiece.PieceType promotion = null;

        if (tokens.length == 4) {
            promotion = parsePromotion(tokens[3]);
        }

        ChessMove move = new ChessMove(start, end, promotion);
        communicator.makeMove(state.getAuthToken(), gameID, move);
        return "Move sent.";
    }

    private String highlight(String[] tokens) {
        if (tokens.length != 2) {
            return "Usage: highlight <position>";
        }
        if (currentGame == null) {
            return "No game loaded yet.";
        }

        ChessPosition position = parsePosition(tokens[1]);
        ChessPiece piece = currentGame.getBoard().getPiece(position);
        if (piece == null) {
            return "Error: no piece at that position.";
        }

        Collection<ChessMove> moves = piece.pieceMoves(currentGame.getBoard(), position);
        List<ChessPosition> highlights = new ArrayList<>();
        highlights.add(position);

        for (ChessMove move : moves) {
            highlights.add(move.getEndPosition());
        }

        return ChessBoardPrinter.drawBoard(currentGame.getBoard(), whitePerspective, highlights);
    }

    private ChessPosition parsePosition(String text) {
        if (text == null || text.length() != 2) {
            throw new IllegalArgumentException("invalid position");
        }

        char file = Character.toLowerCase(text.charAt(0));
        char rank = text.charAt(1);

        if (file < 'a' || file > 'h' || rank < '1' || rank > '8') {
            throw new IllegalArgumentException("invalid position");
        }

        int col = file - 'a' + 1;
        int row = rank - '0';
        return new ChessPosition(row, col);
    }

    private ChessPiece.PieceType parsePromotion(String text) {
        return switch (text.toUpperCase()) {
            case "Q", "QUEEN" -> ChessPiece.PieceType.QUEEN;
            case "R", "ROOK" -> ChessPiece.PieceType.ROOK;
            case "B", "BISHOP" -> ChessPiece.PieceType.BISHOP;
            case "N", "KNIGHT" -> ChessPiece.PieceType.KNIGHT;
            default -> throw new IllegalArgumentException("invalid promotion piece");
        };
    }

    @Override
    public void notifyLoadGame(LoadGameMessage message) {
        this.currentGame = message.game.game;
        System.out.println();
        System.out.println(ChessBoardPrinter.drawBoard(currentGame.getBoard(), whitePerspective, null));
        printNotificationsIfAny();
        printPrompt();
    }

    @Override
    public void notifyNotification(NotificationMessage message) {
        notifications.add(message.message);
        System.out.println();
        System.out.println("[Notification] " + message.message);
        printPrompt();
    }

    @Override
    public void notifyError(ErrorMessage message) {
        System.out.println();
        System.out.println(message.errorMessage);
        printPrompt();
    }

    private void printNotificationsIfAny() {
        if (!notifications.isEmpty()) {
            for (String n : notifications) {
                System.out.println("[Notification] " + n);
            }
            notifications.clear();
        }
    }

    private void printPrompt() {
        System.out.print("[GAMEPLAY] >>> ");
    }
}