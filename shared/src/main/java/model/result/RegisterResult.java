package model.result;

public class RegisterResult {
    public String username;
    public String authToken;

    public RegisterResult() {}

    public RegisterResult(String username, String authToken) {
        this.username = username;
        this.authToken = authToken;
    }
}