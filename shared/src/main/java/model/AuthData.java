package model;

public class AuthData {
    public String authToken;
    public String username;

    public AuthData() {}

    public AuthData(String authToken, String username) {
        this.authToken = authToken;
        this.username = username;
    }
}