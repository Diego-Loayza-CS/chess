package ui;

import client.ServerFacade;
import model.GameListItem;

import java.util.ArrayList;
import java.util.List;

public class PostloginClient {
    private final ServerFacade server;
    private final State state;
    private List<GameListItem> lastListedGames = new ArrayList<>();

    public PostloginClient(ServerFacade server, State state) {
        this.server = server;
        this.state = state;
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
                case "logout" -> logout();
                case "create" -> createGameCommand(tokens);
                case "list" -> listGamesCommand(tokens);
                case "play" -> playGame(tokens);
                case "observe" -> observeGame(tokens);
                default -> "Unknown command. Type help to see available commands.";
            };
        } catch (Exception ex) {
            return "Error: " + ex.getMessage();
        }
    }

    private String help() {
        return """
                Available commands:
                  help
                  logout
                  create game <game name>
                  list games
                  play <game number> <WHITE|BLACK>
                  observe <game number>
                """;
    }

    private String logout() throws Exception {
        server.logout(state.getAuthToken());
        state.clearSession();
        return "Logged out.";
    }

    private String createGame(String[] tokens) throws Exception {
        if (tokens.length < 2) {
            return "Usage: create <game name>";
        }

        String gameName = joinTail(tokens, 1);
        int gameID = server.createGame(state.getAuthToken(), gameName);
        return "Created game with id " + gameID + ".";
    }

    private String createGameCommand(String[] tokens) throws Exception {
        if (tokens.length >= 2 && tokens[1].equalsIgnoreCase("game")) {
            if (tokens.length < 3) {
                return "Usage: create game <game name>";
            }
            String gameName = joinTail(tokens, 2);
            int gameID = server.createGame(state.getAuthToken(), gameName);
            return "Created game with id " + gameID + ".";
        }
        return createGame(tokens);
    }

    private String listGames() throws Exception {
        lastListedGames = server.listGames(state.getAuthToken());
        return formatGameList(lastListedGames);
    }

    private String listGamesCommand(String[] tokens) throws Exception {
        if (tokens.length == 2 && tokens[1].equalsIgnoreCase("games")) {
            return listGames();
        }
        if (tokens.length == 1) {
            return listGames();
        }
        return "Usage: list games";
    }

    private String playGame(String[] tokens) throws Exception {
        if (tokens.length != 3) {
            return "Usage: play <game number> <WHITE|BLACK>";
        }

        int number = parseGameNumber(tokens[1]);
        String color = tokens[2].toUpperCase();

        if (!color.equals("WHITE") && !color.equals("BLACK")) {
            return "Usage: play <game number> <WHITE|BLACK>";
        }

        GameListItem game = requireListedGame(number);
        server.joinGame(state.getAuthToken(), game.gameID, color);

        boolean whitePerspective = !color.equals("BLACK");
        return ChessBoardPrinter.drawBoard(whitePerspective);
    }

    private String observeGame(String[] tokens) throws Exception {
        if (tokens.length != 2) {
            return "Usage: observe <game number>";
        }

        int number = parseGameNumber(tokens[1]);
        requireListedGame(number);
        return ChessBoardPrinter.drawBoard(true);
    }

    private int parseGameNumber(String token) {
        try {
            return Integer.parseInt(token);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("invalid game number");
        }
    }

    private GameListItem requireListedGame(int number) {
        if (number < 1 || number > lastListedGames.size()) {
            throw new IllegalArgumentException("invalid game number");
        }
        return lastListedGames.get(number - 1);
    }

    private String formatGameList(List<GameListItem> games) {
        if (games.isEmpty()) {
            return "No games found.";
        }

        StringBuilder out = new StringBuilder();
        for (int i = 0; i < games.size(); i++) {
            GameListItem g = games.get(i);
            out.append(i + 1)
                    .append(". ")
                    .append(g.gameName)
                    .append(" | white: ")
                    .append(displayName(g.whiteUsername))
                    .append(" | black: ")
                    .append(displayName(g.blackUsername))
                    .append("\n");
        }
        return out.toString().trim();
    }

    private String displayName(String username) {
        return username == null ? "-" : username;
    }

    private String joinTail(String[] tokens, int start) {
        StringBuilder out = new StringBuilder();
        for (int i = start; i < tokens.length; i++) {
            if (i > start) {
                out.append(" ");
            }
            out.append(tokens[i]);
        }
        return out.toString();
    }
}