package net.frostbyte.quickboardx.api;

import net.frostbyte.quickboardx.PlayerBoard;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("unused")
public interface QuickBoardAPI
{

    /**
     * Create a Temporarily displayed scoreboard for a Player.
     *
     * @param playerID The Player's UUID
     * @param boardName The name of the scoreboard config to use. (without the extension)
     * @return The Playerboard
     */
    PlayerBoard createTemporaryBoard(UUID playerID, String boardName);

    /**
     * Create a Temporarily displayed scoreboard for a player
     *
     * @param playerID The player's UUID
     * @param text The Text lines for the body of the scoreboard
     * @param title The Lines of Text displayed in the Board Title
     * @param updateTitle The update interval for cycling through the Title Lines, in ticks.
     * @param updateText The update interval for scrollable and changeable elements contained in the scoreboard.
     * @return The PlayerBoard that was created using the given parameters.
     */
    PlayerBoard createTemporaryBoard(
        UUID playerID,
        String boardName,
        List<String> title,
        List<String> text,
        int updateTitle,
        int updateText
    );

    /**
     * Create a scoreboard for a Player.
     *
     * @param playerID The Player's UUID
     * @param name The name of the scoreboard config to use. (without the extension)
     * @return The Playerboard
     */
    @SuppressWarnings("UnusedReturnValue")
    PlayerBoard createBoard(UUID playerID, String name);

    /**
     * Create a Player Board for a player
     *
     * @param playerID The player's UUID
     * @param text The Text lines for the body of the scoreboard
     * @param title The Lines of Text displayed in the Board Title
     * @param updateTitle The update interval for cycling through the Title Lines, in ticks.
     * @param updateText The update interval for scrollable and changeable elements contained in the scoreboard.
     * @return The PlayerBoard that was created using the given parameters.
     */
    PlayerBoard createBoard(
        UUID playerID,
        List<String> text,
        List<String> title,
        int updateTitle,
        int updateText
    );

    /**
     * Retrieve the list of all registered Player Scoreboards.
     * @return The list of player boards
     */
    List<PlayerBoard> getAllBoards();

    /**
     * Retrieve the map of player UUID's to their registered
     * player boards.
     * @return A map of player IDs to Player Boards
     */
    HashMap<UUID, PlayerBoard> getBoards();

    /**
     * Remove a Player's Scoreboard.
     *
     * @param playerID The Player's UUID
     */
    void removeBoard(UUID playerID);

    /**
     * Update the Text for a Player's Scoreboard.
     *
     * @param playerID The Player's UUID
     */
    void updateText(UUID playerID);

    /**
     * Update the Title for a Player's Scoreboard.
     *
     * @param playerID The Player's UUID
     */
    void updateTitle(UUID playerID);

    /**
     * Update the Title, Text, Changeable and Scrollable
     * elements for a Player's Scoreboard.
     *
     * @param playerID The Player's UUID
     */
    void updateAll(UUID playerID);
}
