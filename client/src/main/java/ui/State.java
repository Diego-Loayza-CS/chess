package ui;

public class State {
    public enum Mode {
        PRELOGIN,
        POSTLOGIN,
        QUIT
    }

    private Mode mode = Mode.PRELOGIN;
    private String authToken;
    private String username;
    private boolean inGameplay = false;
    private Integer currentGameID;

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isInGameplay() {
        return inGameplay;
    }

    public void setInGameplay(boolean inGameplay) {
        this.inGameplay = inGameplay;
    }

    public Integer getCurrentGameID() {
        return currentGameID;
    }

    public void setCurrentGameID(Integer currentGameID) {
        this.currentGameID = currentGameID;
    }

    public void clearSession() {
        this.authToken = null;
        this.username = null;
        this.mode = Mode.PRELOGIN;
        this.inGameplay = false;
        this.currentGameID = null;
    }
}