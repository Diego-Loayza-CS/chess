package model.result;

import model.GameListItem;
import java.util.List;

public class ListGamesResult {
    public List<GameListItem> games;

    public ListGamesResult() {}

    public ListGamesResult(List<GameListItem> games) {
        this.games = games;
    }
}