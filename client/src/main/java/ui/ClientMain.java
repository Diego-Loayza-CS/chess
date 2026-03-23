package ui;

public class ClientMain {
    public static void main(String[] args) {
        var port = 8080;
        var repl = new Repl("http://localhost:" + port);
        repl.run();
    }
}