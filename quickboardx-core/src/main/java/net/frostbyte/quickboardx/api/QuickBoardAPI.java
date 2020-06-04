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
     * Determines if the given team name has been registered via a Team Configuration
     * @param teamName The name of the team
     * @return True if the team has been registered, otherwise false.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean hasTeam(String teamName);

    /**
     * Create a new Team Configuration File and add the specified team's details to it.
     * @param configName The name of the new config file.
     * @param newTeam The Data defining the team to add to the new config file.
     */
    void createTeamConfig(String configName, Team newTeam);

    /**
     * Adds the given Team's details to the specified Team Configuration.
     * @param configName The Team Configuration being updated.
     * @param team The Settings for the Team being added to the Configuration.
     */
    void addTeamToConfig(String configName, Team team);

    /**
     * Remove the specified Team name from the given Team Configuration.
     * @param configName The name of the Team Configuration file.
     * @param teamName The name of the team to remove from the Configuration.
     */
    void removeTeamFromConfig(String configName, String teamName);

    /**
     * Remove the given player's Team from All Player Tab Lists
     * @param playerID The player's ID
     */
    void removePlayersTeam(UUID playerID);

    /**
     * Assign a player to a team in the Tab List, and update that team on all player scoreboards
     * @param playerID The player whose team is being updated.
     * @param teamName The player's new team.
     */
    void setPlayersTeam(UUID playerID, String teamName);

    /**
     * Retrieve's the name of the player's current Team in the Player Tab List
     * @param playerID The player's ID
     * @return The player's current team name; null if the player isn't registered or does not have a team assigned.
     */
    Team getPlayersTeam(UUID playerID);

    /**
     * Generate all Player List Teams for the given player's scoreboard.
     * @param playerID The player's uuid
     */
    void generatePlayersListTeams(UUID playerID);

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

    /**
     * Update a Player List Team in a Team Configuration.
     * @param configName The Team Configuration whose Team Entry will be updated.
     * @param team The Team to Update
     */
    void updateTeamConfig(String configName, Team team);
}
