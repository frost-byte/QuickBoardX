package me.tade.quickboard.api;

import me.tade.quickboard.PlayerBoard;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;

@SuppressWarnings("unused")
public interface QuickBoardAPI {
    PlayerBoard createBoard(Player player, String name);
    PlayerBoard createBoard(Player player, List<String> text, List<String> title, int updateTitle, int updateText);
    List<PlayerBoard> getAllBoards();
    HashMap<Player, PlayerBoard> getBoards();
    void removeBoard(Player player);
    void updateText(Player player);
    void updateTitle(Player player);
    void updateAll(Player player);
}
