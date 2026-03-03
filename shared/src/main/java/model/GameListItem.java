package model;

public class GameListItem {
    public int gameID;
    public String whiteUsername;
    public String blackUsername;
    public String gameName;

    public GameListItem() {}

    public GameListItem(int gameID, String whiteUsername, String blackUsername, String gameName) {
        this.gameID = gameID;
        this.whiteUsername = whiteUsername;
        this.blackUsername = blackUsername;
        this.gameName = gameName;
    }
}