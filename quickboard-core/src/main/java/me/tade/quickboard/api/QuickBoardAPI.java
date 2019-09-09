package me.tade.quickboard.api;

import me.tade.quickboard.PlayerBoard;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("unused")
public interface QuickBoardAPI
{
    PlayerBoard createTemporaryBoard(UUID playerID, String boardName);
    PlayerBoard createTemporaryBoard(
        UUID playerID,
        String boardName,
        List<String> title,
        List<String> text,
        int updateTitle,
        int updateText
    );
    @SuppressWarnings("UnusedReturnValue")
    PlayerBoard createBoard(UUID playerID, String name);
    PlayerBoard createBoard(
        UUID playerID,
        List<String> text,
        List<String> title,
        int updateTitle,
        int updateText
    );
    List<PlayerBoard> getAllBoards();
    HashMap<UUID, PlayerBoard> getBoards();
    void removeBoard(UUID playerID);
    void updateText(UUID playerID);
    void updateTitle(UUID playerID);
    void updateAll(UUID playerID);
}
