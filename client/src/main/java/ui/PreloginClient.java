package ui;

import client.ServerFacade;
import model.AuthData;

public class PreloginClient {
    private final ServerFacade server;
    private final State state;

    public PreloginClient(ServerFacade server, State state) {
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
                case "quit" -> quit();
                case "login" -> login(tokens);
                case "register" -> register(tokens);
                default -> "Unknown command. Type help to see available commands.";
            };
        } catch (Exception ex) {
            return ex.getMessage();
        }
    }

    private String help() {
        return """
                Available commands:
                  help
                  quit
                  login <username> <password>
                  register <username> <password> <email>
                """;
    }

    private String quit() {
        state.setMode(State.Mode.QUIT);
        return "Exiting...";
    }

    private String login(String[] tokens) throws Exception {
        if (tokens.length != 3) {
            return "Usage: login <username> <password>";
        }

        AuthData auth = server.login(tokens[1], tokens[2]);
        state.setAuthToken(auth.authToken);
        state.setUsername(auth.username);
        state.setMode(State.Mode.POSTLOGIN);
        return "Logged in as " + auth.username + ".";
    }

    private String register(String[] tokens) throws Exception {
        if (tokens.length != 4) {
            return "Usage: register <username> <password> <email>";
        }

        AuthData auth = server.register(tokens[1], tokens[2], tokens[3]);
        state.setAuthToken(auth.authToken);
        state.setUsername(auth.username);
        state.setMode(State.Mode.POSTLOGIN);
        return "Registered and logged in as " + auth.username + ".";
    }
}