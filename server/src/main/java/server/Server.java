package server;

import io.javalin.*;
import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;

public class Server {

    private final Javalin javalin;
    private final DataAccess dataAccess = new MemoryDataAccess();

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // Register your endpoints and exception handlers here.
        javalin.delete("/db", ctx -> {
            dataAccess.clear();
            ctx.status(200);
            ctx.json(new Object());
        });
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();


    }

    public void stop() {
        javalin.stop();
    }
}
