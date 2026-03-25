package ui;

import client.ServerFacade;

import java.util.Scanner;

public class Repl {
    private final Scanner scanner = new Scanner(System.in);
    private final State state = new State();
    private final PreloginClient preloginClient;
    private final PostloginClient postloginClient;

    public Repl(String serverUrl) {
        ServerFacade server = new ServerFacade(serverUrl);
        this.preloginClient = new PreloginClient(server, state);
        this.postloginClient = new PostloginClient(server, state);
    }

    public void run() {
        System.out.println("Welcome to Chess. Type help to get started.");

        while (state.getMode() != State.Mode.QUIT) {
            try {
                printPrompt();
                String input = scanner.nextLine();

                if (state.getMode() == State.Mode.PRELOGIN) {
                    System.out.println(preloginClient.eval(input));
                } else if (state.getMode() == State.Mode.POSTLOGIN) {
                    System.out.println(postloginClient.eval(input));
                }
            } catch (Exception ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        }

        System.out.println("Goodbye.");
    }

    private void printPrompt() {
        if (state.getMode() == State.Mode.PRELOGIN) {
            System.out.print("[LOGGED_OUT] >>> ");
        } else {
            System.out.print("[" + state.getUsername() + "] >>> ");
        }
    }
}