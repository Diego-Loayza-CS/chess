package model;

public class UserData {
    public String username;
    public String password;
    public String email;

    public UserData() {}

    public UserData(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }
}