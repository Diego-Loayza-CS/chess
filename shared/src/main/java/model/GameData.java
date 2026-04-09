package model;

import chess.ChessGame;

public class GameData {
    public int gameID;
    public String whiteUsername;
    public String blackUsername;
    public String gameName;
    public ChessGame game;
    public boolean gameOver;

    public GameData(int gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game) {
        this(gameID, whiteUsername, blackUsername, gameName, game, false);
    }

    public GameData(int gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game, boolean gameOver) {
        this.gameID = gameID;
        this.whiteUsername = whiteUsername;
        this.blackUsername = blackUsername;
        this.gameName = gameName;
        this.game = game;
        this.gameOver = gameOver;
    }
}