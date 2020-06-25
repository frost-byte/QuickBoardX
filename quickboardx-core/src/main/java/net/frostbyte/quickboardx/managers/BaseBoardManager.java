package net.frostbyte.quickboardx.managers;

import me.clip.placeholderapi.PlaceholderAPI;
import net.frostbyte.quickboardx.PlayerBoard;
import net.frostbyte.quickboardx.QuickBoardX;
import net.frostbyte.quickboardx.api.Team;
import net.frostbyte.quickboardx.config.BoardConfig;
import net.frostbyte.quickboardx.config.TeamConfig;
import net.frostbyte.quickboardx.events.WhenPluginUpdateTextEvent;
import net.frostbyte.quickboardx.tasks.RemoveTempTask;
import net.frostbyte.quickboardx.tasks.UpdateChangeableTask;
import net.frostbyte.quickboardx.tasks.UpdateScrollerTask;
import net.frostbyte.quickboardx.tasks.UpdateTextTask;
import net.frostbyte.quickboardx.tasks.UpdateTitleTask;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import static net.frostbyte.quickboardx.util.StringConstants.*;

@SuppressWarnings({"unused", "WeakerAccess", "UnusedReturnValue"})
public abstract class BaseBoardManager
{
	protected final QuickBoardX plugin;
	protected final Logger logger;

	/**
	 * Player Scoreboard Tasks for updating the Title, Changeable and Scrolling text elements.
	 * Also includes timers for removing Temporary boards.
	 */
	protected Map<UUID, List<Integer>> playerBoardTasks = new HashMap<>();

	/**
	 * A map of player UUIDs to their teams.
	 */
	protected Map<UUID, Team> playerTeams = new HashMap<>();

	/**
	 * Map of Team Names to Teams
	 */
	protected Map<String, Team> teams = new HashMap<>();

	/**
	 * The maximum number of characters that a Team Prefix or Suffix can contain
	 */
	protected int affixMaxCharacters = 16;

	/**
	 * The maximum number of characters that a scoreboard line can contain.
	 * Includes the Team Prefix and Suffix
	 */
	protected int maxCharacters = affixMaxCharacters * 2;

	/**
	 * The number of characters to split an input text string
	 * into a Team prefix and suffix that will be displayed on a line
	 * in a scoreboard.
	 */
	protected int splitSize = affixMaxCharacters;

	/**
	 * The Prefix applied to all team entries as part of a unique identifier
	 */
	protected static final String TEAM_TAG = "Team:";

	/**
	 * Map of Player IDs to PlayerBoards
	 */
	protected HashMap<UUID, PlayerBoard> boards = new HashMap<>();

	/**
	 * List of all active PlayerBoards
	 */
	protected List<PlayerBoard> allBoards = new ArrayList<>();

	/**
	 * Map of Board Names to their Configs
	 */
	protected HashMap<String, BoardConfig> boardConfigMap = new HashMap<>();

	/**
	 * Map of Team Names to their Configs
	 */
	protected HashMap<String, TeamConfig> teamConfigMap = new HashMap<>();

	/**
	 * Initialize the Scoreboard - including its objective, teams,
	 * team entries and display slot - and adds all related board
	 * information.
	 *
	 * @param playerBoard The player board
	 * @return The instance of the BoardManager implementation
	 */
	public abstract PlayerBoard createScoreboard(PlayerBoard playerBoard);

	/**
	 * Set the current scoreboard for a player board
	 * @param playerBoard The player board
	 * @return The instance of the BoardManager implementation
	 */
	public abstract BaseBoardManager setPlayerScoreboard(PlayerBoard playerBoard);

	/**
	 * Reset a Player's scoreboard to the default. (Non-Quickboard)
	 *
	 * @param playerID The id of the player
	 * @return The instance of the BoardManager implementation
	 */
	public abstract BaseBoardManager resetPlayerScoreboard(UUID playerID);

	/**
	 * Updates the Displayed Title for a Player Board.
	 *
	 * @param playerBoard The player board
	 * @param text The new value for the scoreboard's displayed title.
	 * @return The instance of the BoardManager implementation
	 */
	public abstract BaseBoardManager setTitle(PlayerBoard playerBoard, String text);

	/**
	 * Set the displayed text for a line in the player scoreboard.
	 * A team corresponds to one of the 16 lines of text that can be
	 * displayed on a board.
	 *
	 * @param playerBoard The player board
	 * @param teamIndex The numerical index for the team whose text will be updated
	 * @param boardLine The parsed suffix and prefix that will be used to update the
	 *                  text displayed for the team.
	 * @return The instance of the BoardManager implementation
	 */
	public abstract BaseBoardManager setSidebarTeamText(
		PlayerBoard playerBoard,
		int teamIndex,
		BoardLine boardLine
	);

	/**
	 * Displays the stored scoreboard for the given player.
	 *
	 * @param playerID The Player's Unique ID
	 * @return The instance of the BoardManager implementation
	 */
	public abstract BaseBoardManager showPlayerBoard(UUID playerID);

	/**
	 * Remove all scoreboard related entries for the given player board;
	 * based upon the player associated with the player board.
	 *
	 * @param playerBoard The Player's scoreboard state and configuration
	 * @return The instance of the BoardManager implementation
	 */
	public abstract BaseBoardManager removeAll(PlayerBoard playerBoard);

	/**
	 * Get the name of the given player's current world.
	 *
	 * @param playerID The player's Unique ID
	 * @return The Name of the Player's current world, or null
	 * if one could not be determined.
	 */
	public abstract String getPlayerWorldName(UUID playerID);

	/**
	 * Determine if the given player has access to view the specified
	 * board.
	 *
	 * @param playerID The player's Unique ID
	 * @param boardName The name of a board configuration
	 * @return True, if the player has permission to view the given board.
	 */
	public abstract boolean getPlayerPermission(UUID playerID, String boardName);

	/**
	 * Retrieve the name of the player matching the given Player Unique ID.
	 * @param playerID The player's Unique ID
	 * @return The name of the player matching the ID, or null if one could not
	 * be found.
	 */
	public abstract String getPlayerName(UUID playerID);

	/**
	 * Determines if the given player ID has any registered Player
	 * Boards.
	 *
	 * @param playerID The player's Unique ID
	 * @return True if the player has a registered board.
	 */
	public abstract boolean isPlayerInvalid(UUID playerID);

	/**
	 * Set the Player Tab List Team for a player
	 * @param playerID The player's uuid
	 * @param teamName The player's team in the Tab List
	 * @return this board manager
	 */
	public BaseBoardManager setPlayerTeam(UUID playerID, String teamName)
	{
		if (teamName == null || teamName.isEmpty())
			return this;

		logger.info("BaseBoardManager: setPlayerTeam " + teamName);
		Team currentTeam = getPlayerTeam(playerID);
		Team team = teams.getOrDefault(teamName, null);

		if (team == null)
			return this;

		if (currentTeam != null)
		{
			if (teamName.equals(currentTeam.getName()))
			{
				logger.info("BaseBoardManager: setPlayerTeam: using current Team.");
				return this;
			}
			logger.info("BaseBoardManager: setPlayerTeam: removing player from current Team.");

			removePlayerFromTeam(playerID, currentTeam);
		}

		logger.info("BaseBoardManager: setPlayerTeam: adding Player to Team.");
		team.addEntry(getPlayerName(playerID));

		return this;
	}

	/**
	 * Update the Player Tab List team on all player scoreboards.
	 * @param team The updated team
	 * @return this board manager
	 */
	public abstract BaseBoardManager updatePlayerListTeam(Team team);

	/**
	 * Update the Player Tab List team on all player scoreboards.
	 * @param teamName The name of the updated team
	 * @return this board manager
	 */
	public BaseBoardManager updatePlayerListTeam(String teamName)
	{
		net.frostbyte.quickboardx.api.Team team = getTeam(teamName);

		if (team == null)
			return this;

		updatePlayerListTeam(team);

		return this;
	}

	/**
	 * Create the Bukkit Teams for the Player Tab List for the given player's scoreboard.
	 * @param playerId The player
	 * @return This board manager
	 */
	public BaseBoardManager generateListTeams(UUID playerId)
	{
		String playerName = getPlayerName(playerId);
		PlayerBoard playerBoard = getPlayerBoard(playerId);

		logger.info("BoardManager: generateListTeams for " + playerName);

		if (playerBoard == null)
			return this;

		String boardName = playerBoard.getBoardName();

		teams.values().forEach(team -> {
			if (canApplyTeam(team, boardName)) {
				applyTeamEntries(team, playerId);
			}
		});

		return this;
	}

	/**
	 * Determines if a scoreboard should display the entries for a given Player Tab list team
	 * @param team the Player tab list team
	 * @param boardName the scoreboard's name
	 * @return true if the scoreboard is allowed to show the team's entries in the Tab list
	 */
	protected boolean canApplyTeam(Team team, String boardName)
	{
		TeamConfig teamConfig = findTeamConfig(team.getName());

		return teamConfig != null && (teamConfig.applyToAllScoreboards() ||teamConfig.getEnabledScoreboards().contains(boardName));
	}

	/**
	 * Remove the specified player from the given team.
	 * @param playerID The player to remove
	 * @param currentTeam The team that the player will be removed from.
	 */
	public abstract void removePlayerFromTeam(UUID playerID, Team currentTeam);

	/**
	 * Remove the target player from their team on another user's board.
	 * @param targetID The target player who will be removed from their team on another player's board..
	 * @param boardUserID The board user
	 */
	public abstract void removeTeamForOtherPlayer(UUID targetID, UUID boardUserID);

	/**
	 * Update the target player's team entry, for the Player Tab List, on another user's scoreboard.
	 * @param targetID The target player whose team will be updated on the user player's scoreboard.
	 * @param boardUserID The board user whose scoreboard team, matching the target player's team,
	 *                    will have an entry updated.
	 */
	public abstract void updateTeamForOtherPlayer(UUID targetID, UUID boardUserID);

	/**
	 * Retrieve the given player's current Scoreboard Team (for the Player Tab List)
	 * @param playerID The given player ID
	 * @return The player's team.
	 */
	public Team getPlayerTeam(UUID playerID)
	{
		String playerName = getPlayerName(playerID);
		if (playerName == null)
			return null;

		return teams
			.values()
			.stream()
			.filter(t -> t.hasEntry(playerName))
			.findFirst()
			.orElse(null);
	}

	public BaseBoardManager registerTabListTeam(Team team)
	{
		if (team != null)
		{
			logger.info("Registering Player Tab List Team " + team.getName());
			teams.put(team.getName(), team);
		}
		else
			logger.info("ERROR! Could not registering Player Tab List Team!");

		return this;
	}

	/**
	 * Retrieve a registered Player Tab List Team by name.
	 * @param name The Team Name
	 * @return The Registered Team
	 */
	public Team getTeam(String name)
	{
		return teams.getOrDefault(name, null);
	}

	/**
	 * Describes the Details for a Player Tab List Team, given a team name.
	 * @param teamName The Team Name
	 * @return Details for the Team or an error message if no team was registered with the given
	 * name.
	 */
	public String getTeamInformation(String teamName)
	{
		Team team = getTeam(teamName);
		if (team != null)
			return team.information();

		return ERROR_TEAM_DOES_NOT_EXIST;
	}

	/**
	 * Set the Prefix for a Player Tab List Team
	 * @param teamName The Tab List Team to UPdate
	 * @param teamPrefix The new Prefix for the Team
	 * @return this board manager
	 */
	public BaseBoardManager setTeamPrefix(String teamName, String teamPrefix)
	{
		if (
			teamName != null &&
			!teamName.isEmpty() &&
			teamPrefix != null &&
			!teamPrefix.isEmpty()
		) {
			Team team = teams.getOrDefault(teamName, null);
			if (team != null)
			{
				team.setPrefix(teamPrefix);
				updateTeams();
			}
		}
		return this;
	}

	/**
	 * Update all Player Tab list Teams
	 * @return this board manager
	 */
	public abstract BaseBoardManager updateTeams();

	/**
	 * All Player Tab List Teams
	 * @return A List of Registered Teams that can be displayed in the Player Tab List
	 */
	public List<Team> getTabListTeams()
	{
		return new ArrayList<>(teams.values());
	}

	/**
	 * All Player Tab List Team Names
	 * @return A List of Registered Team Names that can be displayed in the Player Tab List
	 */
	public List<String> getTabListTeamNames()
	{
		return new ArrayList<>(teams.keySet());
	}

	/**
	 * The Default Constructor for BaseBoardManager
	 * @param plugin The Quickboard Plugin
	 */
	public BaseBoardManager(QuickBoardX plugin) {
		this.plugin = plugin;
		this.logger = plugin.getLogger();
	}

	protected List<Integer> getTasksList(UUID playerID) { return playerBoardTasks.getOrDefault(playerID, null); }

	/**
	 * Add a board configuration to the manager.
	 *
	 * @param boardName The name and permission for the board configuration
	 * @param boardConfig The board configuration
	 * @return The instance of the BoardManager implementation
	 */
	public BaseBoardManager addBoardConfig(String boardName, BoardConfig boardConfig)
	{
		boardConfigMap.put(boardName, boardConfig);
		return this;
	}

	/**
	 * Add a Team configuration to the manager.
	 *
	 * @param teamName The name and permission for the board configuration
	 * @param teamConfig The board configuration
	 * @return The instance of the BoardManager implementation
	 */
	public BaseBoardManager addTeamConfig(String teamName, TeamConfig teamConfig)
	{
		teamConfigMap.put(teamName, teamConfig);
		return this;
	}

	/**
	 * Stop all running tasks for the given player board.
	 *
	 * @param playerID The Player Scoreboard wrapper
	 * @return The instance of the BoardManager implementation
	 */
	public abstract BaseBoardManager stopTasks(UUID playerID);

	public abstract int scheduleTask(Runnable runnable, BoardOperation operation);

	/**
	 * Start the Specified BoardTask
	 *
	 * @param task A Board Task for a specific Player Board
	 * @return The unique id for the created task
	 */
	public int startTask(BoardTask task)
	{
		BoardOperation operation = task.getOperation();
		UUID playerID = task.getPlayerBoard().getPlayerID();
		Runnable runnable;
		switch (operation)
		{
			case UPDATE_TITLE:
				runnable = new UpdateTitleTask(task);
				break;
			case UPDATE_TEXT:
				runnable = new UpdateTextTask(task.getPlayerBoard(), this);
				break;
			case UPDATE_SCROLLER:
				runnable = new UpdateScrollerTask(task);
				break;
			case UPDATE_CHANGEABLE:
				runnable = new UpdateChangeableTask(task);
				break;
			case REMOVE_TEMPORARY:
				runnable = new RemoveTempTask(task);
				break;
			case UNKNOWN:
			default:
				throw new IllegalStateException("Unexpected value: " + operation);
		}

		int taskID = scheduleTask(runnable, operation);
		List<Integer> tasksList = getTasksList(playerID);

		if (tasksList == null)
			tasksList = new ArrayList<>();

		tasksList.add(taskID);
		playerBoardTasks.put(playerID, tasksList);

		return taskID;
	}

	/**
	 * Update the Scoreboard Text for the given player's
	 * registered board.
	 *
	 * @param playerID The player's Unique ID
	 * @return The instance of the BoardManager implementation
	 */
	public BaseBoardManager updateText(UUID playerID)
	{
		PlayerBoard playerBoard = getPlayerBoard(playerID);
		if (playerBoard != null)
			updateText(playerBoard);
		return this;
	}

	/**
	 * Retrieve the map of loaded unique scoreboard configurations.
	 *
	 * @return The scoreboard configurations
	 */
	public HashMap<String, BoardConfig> getBoardConfigMap()
	{
		return boardConfigMap;
	}

	/**
	 * Retrieve the map of loaded unique team configurations.
	 *
	 * @return The team configurations
	 */
	public HashMap<String, TeamConfig> getTeamConfigMap()
	{
		return teamConfigMap;
	}

	/**
	 * Retrieve the total number of registered Scoreboard Configurations.
	 * @return The number of unique scoreboard configurations loaded.
	 */
	public int getConfigCount()	{ return boardConfigMap.size(); }

	/**
	 * Find a loaded Board Config using the given Board Name
	 * @param boardName The name of the board
	 * @return The Board's Configuration or null, if none was found.
	 */
	public BoardConfig getBoardConfig(String boardName)
	{
		return boardConfigMap.getOrDefault(boardName, null);
	}

	/**
	 * Find a loaded Team Config using the given Team Name
	 * @param teamName The name of the team
	 * @return The Team's Configuration or null, if none was found.
	 */
	public TeamConfig getTeamConfig(String teamName) { return teamConfigMap.getOrDefault(teamName, null); }

	/**
	 * Find a TeamConfig containing the given team
	 * @param teamName The name of the team
	 * @return A TeamConfig containing the team or null, if none was found.
	 */
	public TeamConfig findTeamConfig(String teamName) {
		for (Map.Entry<String, TeamConfig> entry : teamConfigMap.entrySet())
		{
			String name = entry.getKey();
			TeamConfig config = entry.getValue();
			if (config.getTeamNames().contains(teamName))
				return config;
		}
		return null;
	}

	/**
	 * Retrieve a list of the Enabled Scoreboards from a team's configuration.
	 * @param configName The name of the Team Configuration
	 * @return A string containing a list of the enabled scoreboards.
	 */
	public String listEnabledTeamScoreboards(String configName)
	{
		TeamConfig config = getTeamConfig(configName);
		if (config != null)
		{
			StringBuilder builder = new StringBuilder();
			for (String boardName : config.getEnabledScoreboards())
			{
				builder.append(boardName)
					.append("\n");
			}
			return builder.toString();
		}

		return ERROR_FAILED;
	}

	/**
	 * Enable a Scoreboard for the Specified Team Configuration.
	 * @param teamConfigName The Name of the Team
	 * @param boardName The Scoreboard's name.
	 * @return A message describing the result of the operation.
	 */
	public String addEnabledBoard(String teamConfigName, String boardName)
	{
		TeamConfig config = getTeamConfig(teamConfigName);
		if (config != null)
		{
			List<String> enabledBoards = config.getEnabledScoreboards();
			if (!enabledBoards.contains(boardName))
			{
				enabledBoards.add(boardName);
				config.setEnabledScoreboards(enabledBoards);
				return String.format(TEAM_SCOREBOARD_ENABLED, boardName);
			}
			else
				return ERROR_TEAM_SCOREBOARD_ALREADY_ENABLED;
		}
		return ERROR_FAILED;
	}

	/**
	 * Remove an Enabled Scoreboard from the Configuration for a Team displayed in the Player Tab List
	 * @param configName The Team Configuration Name
	 * @param boardName The Scoreboard to disable fo
	 * @return A message describing the result of the operation
	 */
	public String removeEnabledBoard(String configName, String boardName)
	{
		TeamConfig config = getTeamConfig(configName);
		if (config != null)
		{
			List<String> enabledBoards = config.getEnabledScoreboards();
			if (enabledBoards.contains(boardName))
			{
				enabledBoards.remove(boardName);
				config.setEnabledScoreboards(enabledBoards);
				return String.format(TEAM_SCOREBOARD_REMOVED, boardName);
			}
			else
				return ERROR_TEAM_SCOREBOARD_NOT_DISABLED;
		}
		return ERROR_FAILED;
	}

	/**
	 * Retrieve a list of the Enabled Worlds from a scoreboard's configuration.
	 * @param boardName The name of the scoreboard
	 * @return A string containing a list of the enabled worlds.
	 */
	public String listEnabledWorlds(String boardName)
	{
		BoardConfig config = getBoardConfig(boardName);
		if (config != null)
		{
			StringBuilder builder = new StringBuilder();
			for (String worldName : config.getEnabledWorlds())
			{
				builder.append(worldName)
					.append("\n");
			}
			return builder.toString();
		}
		else
			return ERROR_FAILED;
	}

	/**
	 * Add a World to a BoardConfiguration, so that it's scoreboard will be displayed when
	 * players are in that world.
	 * @param boardName The Name of the Board Configuration
	 * @param worldName The Name of the World to Enable for the Board Configuration
	 * @return A message describing the results of the operation.
	 */
	public String addEnabledWorld(String boardName, String worldName)
	{
		BoardConfig config = getBoardConfig(boardName);
		if (config != null)
		{
			List<String> enabledWorlds = config.getEnabledWorlds();
			if (!enabledWorlds.contains(worldName))
			{
				enabledWorlds.add(worldName);
				config.setEnabledWorlds(enabledWorlds);
				return String.format(WORLD_ADDED, worldName);
			}
			else
				return ERROR_WORLD_ALREADY_ENABLED;
		}
		return ERROR_FAILED;
	}

	/**
	 * Remove a World from a BoardConfiguration, so that it's scoreboard will NOT be displayed when
	 * players are in that world.
	 * @param boardName The Name of the Board Configuration
	 * @param worldName The Name of the World to Disable for the Board Configuration
	 * @return A message describing the results of the operation.
	 */
	public String removeEnabledWorld(String boardName, String worldName)
	{
		BoardConfig config = getBoardConfig(boardName);
		if (config != null)
		{
			List<String> enabledWorlds = config.getEnabledWorlds();
			if (enabledWorlds.contains(worldName))
			{
				enabledWorlds.remove(worldName);
				config.setEnabledWorlds(enabledWorlds);
				return String.format(WORLD_REMOVED, worldName);
			}
			else
				return ERROR_WORLD_NOT_DISABLED;
		}
		return ERROR_FAILED;
	}

	/**
	 * Finds a Board Configuration for the given board name; will load the config, if necessary (and it exists).
	 *
	 * @param boardName The name of the board
	 * @param readFile Whether the Board's Config file should be read or not
	 * @return An Optional containing the Board Configuration matching the given board name,
	 * if there is no file for the given board name, then the optional will be empty.
	 */
	protected Optional<BoardConfig> getConfig(String boardName, boolean readFile)
	{
		BoardConfig config = getBoardConfigMap().getOrDefault(
			boardName,
			null
		);

		if (config == null)
			config = new BoardConfig(plugin, boardName, readFile);

		if (config.fileExists())
		{
			boardConfigMap.putIfAbsent(boardName, config);
			return Optional.of(config);
		}
		else
			return Optional.empty();
	}

	/**
	 * Finds a Team Configuration for the given team name; will load the config, if necessary (and it exists).
	 *
	 * @param configName The name of the team configuration
	 * @param readFile Whether the Team's Config file should be read or not
	 * @return An Optional containing the Team Configuration matching the given team name,
	 * if there is no file for the given team name, then the optional will be empty.
	 */
	protected Optional<TeamConfig> getTeamConfig(String configName, boolean readFile)
	{
		TeamConfig config = getTeamConfigMap().getOrDefault(
			configName,
			null
		);

		if (config == null)
			config = new TeamConfig(plugin, configName, readFile);

		if (config.fileExists())
		{
			teamConfigMap.putIfAbsent(configName, config);
			return Optional.of(config);
		}
		else
			return Optional.empty();
	}

	/**
	 * Retrieve the PlayerBoard for the given player UUID.
	 *
	 * @param playerID The player UUID
	 * @return The player's current board or null, if none was found.
	 */
	public PlayerBoard getPlayerBoard(UUID playerID)
	{
		return boards.getOrDefault(playerID, null);
	}

	/**
	 * Retrieve the Team Names for the Given Scoreboard
	 * @param boardName The Scoreboard
	 * @return The list of teams available to the Player Tab List for the scoreboard.
	 */
	public List<String> getTeamsForBoard(String boardName)
	{
		List<String> usableTeams = new ArrayList<>();
		List<TeamConfig> teamConfigs = new ArrayList<>(teamConfigMap.values());
		teamConfigs.forEach(config -> {
			if (
				config.applyToAllScoreboards() ||
				config.getEnabledScoreboards().contains(boardName)
			) {
				config.getTeamNames().forEach(tn -> {
					if (!usableTeams.contains(tn))
						usableTeams.add(tn);
				});
			}
		});
		return usableTeams;
	}

	/**
	 * Returns a list of boards that the given player can use.
	 *
	 * @param playerID The player's UUID
	 * @return A list of board names that the player has permission to use or
	 * an empty list, indicating that they cannot use any of the boards.
	 */
	public List<String> getBoardsForPlayer(UUID playerID)
	{
		List<String> usableBoards = new ArrayList<>();
		String worldName = getPlayerWorldName(playerID);
		Set<String> perms = boardConfigMap.keySet();
		perms.forEach(
			permission ->
			{
				BoardConfig boardConfig = getBoardConfig(permission);

				if (boardConfig != null)
				{
					List<String> enabledWorlds = boardConfig.getEnabledWorlds();
					if (
						worldName != null &&
						enabledWorlds != null &&
						enabledWorlds.contains(worldName) &&
						getPlayerPermission(playerID, permission)
					)
						usableBoards.add(permission);
				}
			}
		);

		return usableBoards;
	}

	/**
	 * Displays the next scoreboard in the list of boards the player has access
	 * to for their current world.
	 *
	 * @param playerID The player's UUID
	 * @return Message indicating the result of the operation
	 */
	@SuppressWarnings("UnusedReturnValue")
	public String nextBoard(UUID playerID)
	{
		PlayerBoard playerBoard = getPlayerBoard(playerID);

		if (playerBoard != null)
		{
			List<String> usableBoards = getBoardsForPlayer(playerID);
			String currentBoard = playerBoard.getBoardName();

			if (
				!usableBoards.isEmpty() &&
				usableBoards.size() > 1 &&
				usableBoards.contains(currentBoard)
			) {
				int newIndex = usableBoards.indexOf(currentBoard);

				if (newIndex >= 0)
				{
					newIndex += 1;

					if (newIndex >= usableBoards.size())
						newIndex = 0;

					BoardConfig boardConfig = getBoardConfig(usableBoards.get(newIndex));

					if (boardConfig != null)
					{
						changePlayerBoard(playerID, boardConfig.getPermission());
						generateListTeams(playerID);
						return SCOREBOARD_CHANGED;
					}
				}
			}
		}
		return ERROR_CANNOT_CHANGE;
	}

	/**
	 * Displays the previous scoreboard in the list of boards the player has access to for their current world.
	 *
	 * @param playerID The player's UUID
	 * @return A message indicating the result of the operation.
	 */
	public String previousBoard(UUID playerID)
	{
		PlayerBoard playerBoard = getPlayerBoard(playerID);
		if (playerBoard != null)
		{
			List<String> usableBoards = getBoardsForPlayer(playerID);
			String currentBoard = playerBoard.getBoardName();

			if (
				!usableBoards.isEmpty() &&
				usableBoards.size() > 1 &&
				usableBoards.contains(currentBoard)
			)
			{
				int newIndex = usableBoards.indexOf(currentBoard);

				if (newIndex >= 0)
				{
					newIndex -= 1;

					if (newIndex < 0)
						newIndex = usableBoards.size() - 1;

					BoardConfig boardConfig = getBoardConfig(usableBoards.get(newIndex));

					if (boardConfig != null)
					{
						changePlayerBoard(playerID, boardConfig.getPermission());
						generateListTeams(playerID);
						return SCOREBOARD_CHANGED;
					}
				}
			}
		}
		return ERROR_CANNOT_CHANGE;
	}

	/**
	 * Toggle a custom scoreboard for the given player;
	 * will create a new PlayerBoard for the player, if necessary.
	 * @param playerID The player's UUID
	 * @param boardName The name of the board to create, using it's associated
	 *                  configuration.
	 * @return A string describing whether the board was toggled or not
	 */
	public String toggleCustomPlayerBoard(UUID playerID, String boardName)
	{
		BoardConfig boardConfig = getBoardConfig(boardName);

		if (boardConfig == null)
			return ERROR_DOES_NOT_EXIST;

		PlayerBoard playerBoard = getPlayerBoard(playerID);

		if (playerBoard != null)
			changePlayerBoard(playerID, boardName);
		else
			new PlayerBoard(plugin, playerID, boardConfig);

		generateListTeams(playerID);

		return null;
	}

	/**
	 * Creates a Default Scoreboard from the first board found
	 * that the player can use in their current world and permissions.
	 *
	 * @param playerID The Player's UUID
	 * @return The instance of the BoardManager implementation
	 */
	@SuppressWarnings("UnusedReturnValue")
	public BaseBoardManager createDefaultScoreboard(UUID playerID)
	{
		if (isPlayerInvalid(playerID))
			return this;

		String playerName = getPlayerName(playerID);
		String playerWorld = getPlayerWorldName(playerID);
		List<String> usableBoards = getBoardsForPlayer(playerID);
		logger.info("Creating default scoreboard for " + playerName);

		if (usableBoards != null && !usableBoards.isEmpty())
		{
			String boardName = usableBoards.get(0);
			BoardConfig boardConfig = getBoardConfig(boardName);
			PlayerBoard playerBoard = getPlayerBoard(playerID);

			if (playerBoard != null)
			{
				logger.info("createDefaultScoreboard named " + boardName + " was found for " + playerName);

				if (playerBoard.getBoardName().equals(boardName))
				{
					logger.info("Replacing existing board!");
					setPlayerScoreboard(playerBoard);
					return this;
				}
				else
				{
					logger.info("Creating new board!");
					changePlayerBoard(playerID, boardName);
					return this;
				}
			}
			else
			{
				logger.info("Generated board for player " + playerName);
				new PlayerBoard(plugin, playerID, boardConfig);
				return this;
			}
		}
		PlayerBoard playerBoard = getPlayerBoard(playerID);
		if (playerBoard != null)
			removeAll(playerBoard);

		logger.info("createDefaultScoreboard could not create a board for player " + playerName);
		return this;
	}

	/**
	 * Creates a new Board Configuration with the given name and usable
	 * in the given World.
	 *
	 * @param boardName The name of the new board
	 * @param worldName The world that board will be enabled in.
	 * @return A message describing the success or failure when creating the new configuration.
	 */
	public String createNewBoard(String boardName, String worldName)
	{
		AtomicReference<String> result = new AtomicReference<>("");
		getConfig(boardName, true)
			.ifPresent((c) -> result.set(ERROR_ALREADY_USED));

		if (!result.get().isEmpty())
			return result.get();

		getConfig(boardName, false)
			.ifPresent(
				(cfg) ->
				{
					if (cfg.fileExists())
					{
						result.set(
							HEADER + "§aFile created! Now add lines using §6/qbx addline " +
								boardName
						);
						cfg.setTitle(
							Arrays.asList(
								"This is the default title!",
								"You can edit it any time!"
							)
						);
						cfg.setText(
							Arrays.asList(
								"This is the default text!",
								"You can edit it any time!"
							)
						);
						cfg.setUpdaterText(20);
						cfg.setUpdaterTitle(30);
						cfg.setEnabledWorlds(
							Collections.singletonList(
								worldName
							)
						);
					}
					else
						result.set(ERROR_FILE_CREATION);
				}
			);

		return result.get();
	}

	/**
	 * Creates a new Team Configuration with the given name and usable
	 * by the given Scoreboard.
	 *
	 * @param newTeam The new player list team to create
	 * @param boardName The name of the board that can use the Team
	 * @return A message describing the success or failure when creating the new configuration.
	 */
	public String createTeamConfig(Team newTeam, String boardName, boolean applyToAll)
	{
		if (newTeam != null && !plugin.hasTeam(newTeam.getName()))
		{
			String configName = "teams." + newTeam.getName();
			AtomicReference<String> result = new AtomicReference<>("");
			getTeamConfig(configName, true)
				.ifPresent((c) -> result.set(ERROR_ALREADY_USED));

			if (!result.get().isEmpty())
				return result.get();

			getTeamConfig(configName, false)
			.ifPresent(
				(cfg) ->
				{
					if (cfg.fileExists())
					{
						result.set(
							HEADER + "§aTeam Config File created! Edit the file to configure " +
							configName
						);
						cfg.addTeam(newTeam);

						if (boardName != null && !boardName.isEmpty())
						{
							String newBoardName;

							if (!boardName.startsWith("scoreboard."))
							{
								newBoardName = "scoreboard." + boardName;
							}
							else
								newBoardName = boardName;
							cfg.enableScoreboard(newBoardName);
						}

						cfg.setApplyToAllScoreboards(applyToAll);
					}
					else
						result.set(ERROR_FILE_CREATION);
				}
			);

			return result.get();
		}
		return ERROR_TEAM_FILE_NOT_CREATED;
	}

	/**
	 * Add a Title to the Config for a Scoreboard
	 * @param boardName The name of the Scoreboard
	 * @param title The title string that will be used
	 * @return A message indicating the result of the operation.
	 */
	public String addBoardTitle(String boardName, String title)
	{
		AtomicReference<String> result = new AtomicReference<>();
		getConfig(boardName, true)
			.ifPresent(
				(cfg) ->
				{
					if (cfg.fileExists())
					{
						List<String> lines = cfg.getTitle();

						if (title == null || title.isEmpty())
						{
							result.set(ADDED_EMPTY_LINE);
							lines.add(" ");
							cfg.setTitle(lines);
						}
						else
						{
							lines.add(title);
							cfg.setTitle(lines);
							result.set(String.format(ADDED_LINE, title));
						}
					}
					else
						result.set(ERROR_DOES_NOT_EXIST);
				}
			);

		return result.get();
	}

	/**
	 * Inserts a line into the Title in a Board Configuration
	 * @param boardName The name of the board
	 * @param lineNum The line number where the text will be inserted
	 * @param text The contents of the line that will be inserted.
	 * @return A message indicating the result of the operation.
	 */
	public String insertBoardTitleLine(
		String boardName,
		int lineNum,
		String text
	) {
		if (lineNum <= 0)
			return ERROR_INVALID_NUMBER;

		AtomicReference<String> result = new AtomicReference<>(ERROR_FAILED);

		getConfig(boardName, true)
			.ifPresent(
				(cfg) ->
				{
					List<String> lines = cfg.getTitle();

					if (lines.size() <= (lineNum - 1))
						result.set(OUT_OF_BOUNDS);
					else
					{
						if (text == null || text.isEmpty())
						{
							lines.add(lineNum - 1, " ");
							cfg.setTitle(lines);
							result.set(INSERTED_EMPTY_LINE);
						}
						else
						{
							lines.add(lineNum - 1, text);
							cfg.setTitle(lines);
							result.set(String.format(INSERTED_LINE, text));
						}
					}
				}
			);

		return result.get();
	}

	/**
	 * Remove the board for the given player.
	 * @param playerID The player's UUID
	 * @return The instance of the BoardManager implementation
	 */
	public BaseBoardManager removeBoard(UUID playerID)
	{
		PlayerBoard playerBoard = getPlayerBoard(playerID);
		if (playerBoard != null)
		{
			stopTasks(playerID);
			boards.remove(playerID);
		}
		return this;
	}

	/**
	 * Removes a line from the Title in a Board Configuration
	 * @param boardName The name of the board
	 * @param lineNum The line number where the text will be removed
	 * @return A message indicating the result of the operation.
	 */
	public String removeBoardTitleLine(String boardName, int lineNum)
	{
		if (lineNum <= 0)
			return ERROR_INVALID_NUMBER;

		AtomicReference<String> result = new AtomicReference<>(ERROR_DOES_NOT_EXIST);

		getConfig(boardName, true)
			.ifPresent(
				(cfg) ->
				{
					if (cfg.fileExists())
					{
						List<String> lines = cfg.getTitle();

						if (lines.size() > (lineNum - 1))
						{
							lines.remove((lineNum - 1));
							result.set(LINE_REMOVED);
							cfg.setTitle(lines);
						}
						else
							result.set(OUT_OF_BOUNDS);
					}
				}
			);

		return result.get();
	}

	/**
	 * Appends a line onto the Title in a Board Configuration
	 * @param boardName The name of the board
	 * @param text The contents of the line that will be appended.
	 * @return A message indicating the result of the operation.
	 */
	public String addBoardText(String boardName, String text)
	{
		AtomicReference<String> result = new AtomicReference<>();
		getConfig(boardName, true)
			.ifPresent(
				(cfg) ->
				{
					if (cfg.fileExists())
					{
						List<String> lines = cfg.getText();

						if (text == null || text.isEmpty())
						{
							result.set(ADDED_EMPTY_LINE);
							lines.add(" ");
							cfg.setText(lines);
						}
						else
						{
							lines.add(text);
							cfg.setText(lines);
							result.set(String.format(ADDED_LINE, text));
						}
					}
					else
						result.set(ERROR_DOES_NOT_EXIST);
				}
			);

		return result.get();
	}

	/**
	 * Inserts a line into the Text list for a Board Configuration
	 * @param boardName The name of the board
	 * @param lineNum The line number where the text will be inserted
	 * @param text The contents of the line that will be inserted.
	 * @return A message indicating the result of the operation.
	 */
	public String insertBoardTextLine(
		String boardName,
		int lineNum,
		String text
	) {
		if (lineNum <= 0)
			return ERROR_INVALID_NUMBER;

		AtomicReference<String> result = new AtomicReference<>(ERROR_FAILED);

		getConfig(boardName, true)
			.ifPresent(
				(cfg) ->
				{
					List<String> lines = cfg.getText();

					if (lines.size() <= (lineNum - 1))
						result.set(OUT_OF_BOUNDS);
					else
					{
						if (text == null || text.isEmpty())
						{
							lines.add(lineNum - 1, " ");
							cfg.setText(lines);
							result.set(INSERTED_EMPTY_LINE);
						}
						else
						{
							lines.add(lineNum - 1, text);
							cfg.setText(lines);
							result.set(String.format(INSERTED_LINE, text));
						}
					}
				}
			);

		return result.get();
	}

	/**
	 * Removes a line from the Text list in a Board Configuration
	 * @param boardName The name of the board
	 * @param lineNum The line number where the text will be removed
	 * @return A message indicating the result of the operation.
	 */
	public String removeBoardTextLine(String boardName, int lineNum)
	{
		if (lineNum <= 0)
			return ERROR_INVALID_NUMBER;

		AtomicReference<String> result = new AtomicReference<>(ERROR_DOES_NOT_EXIST);

		getConfig(boardName, true)
			.ifPresent(
				(cfg) ->
				{
					if (cfg.fileExists())
					{
						List<String> lines = cfg.getText();

						if (lines.size() > (lineNum - 1))
						{
							lines.remove((lineNum - 1));
							result.set(LINE_REMOVED);
							cfg.setText(lines);
						}
						else
							result.set(OUT_OF_BOUNDS);
					}
				}
			);

		return result.get();
	}

	/**
	 * Change the given player's currently viewed scoreboard to the specified type.
	 *
	 * @param playerID The player's Unique ID
	 * @param boardName The board configuration name
	 * @return A Message indicating the result of the operation
	 */
	public String selectPlayerBoard(UUID playerID, String boardName)
	{
		if (!getBoardConfigMap().containsKey(boardName))
			return ERROR_DOES_NOT_EXIST;

		if (isPlayerInvalid(playerID))
			return ERROR_DOES_NOT_EXIST;

		PlayerBoard playerBoard = getPlayerBoard(playerID);

		if (playerBoard != null)
		{
			if (!getPlayerPermission(playerID, boardName))
				return ERROR_FAILED;

			changePlayerBoard(playerID, boardName);
		}
		else
			new PlayerBoard(plugin, playerID, getBoardConfigMap().get(boardName));

		generateListTeams(playerID);

		return null;
	}

	/**
	 * Toggle the visibility of a player's scoreboard.
	 *
	 * @param playerID The player's Unique ID
	 * @return A Message indicating the result of the operation
	 */
	public String togglePlayerBoard(UUID playerID)
	{
		String text;
		PlayerBoard playerBoard = boards.getOrDefault(playerID, null);

		if (playerBoard != null)
		{
			removeAll(playerBoard);
			text = plugin.getConfig().getString("messages.ontoggle.false");
		}
		else
		{
			text = plugin.getConfig().getString("messages.ontoggle.true");
			createDefaultScoreboard(playerID);
			generateListTeams(playerID);
		}

		if (text != null && !text.isEmpty())
			text = text.replace("&", "§");

		return text;
	}

	/**
	 * Provide details about the board configurations to the specified player.
	 *
	 * @param playerID The Unique ID for the player issuing the request
	 * @return A message describing the result of the operation
	 */
	public String checkPlayerConfig(UUID playerID)
	{
		String worldName = getPlayerWorldName(playerID);
		File fo = new File(plugin.getDataFolder().getAbsolutePath() + "/scoreboards");
		plugin.loadScoreboards(fo);

		StringBuilder result = new StringBuilder("§cScoreboard and info:");

		for (String boardName : getBoardConfigMap().keySet())
		{
			BoardConfig boardConfig = getBoardConfigMap().get(boardName);
			boolean inEnabledWorlds = (boardConfig.getEnabledWorlds() != null && boardConfig.getEnabledWorlds().contains(worldName));
			result.append("Scoreboard='").append(boardName).append("'\n");
			result.append(" in enabled worlds=").append(inEnabledWorlds).append("\n");
			result.append(" has permission=").append(getPlayerPermission(playerID, boardName)).append("\n");
		}

		return result.toString();
	}

	/**
	 * Show details for the given players currently used TeamConfiguration that determines which team they player is in
	 * for the Player Tab List.
	 * @param playerID The player's id.
	 * @return The Team Config details
	 */
	public String checkPlayerTeamConfig(UUID playerID)
	{
		checkPlayerConfig(playerID);
		File fo = new File(plugin.getDataFolder().getAbsolutePath() + "/teams");
		plugin.loadTeams(fo);
		StringBuilder result = new StringBuilder("§cTeam info:");

		PlayerBoard playerBoard = getPlayerBoard(playerID);

		String boardName = (playerBoard != null) ? playerBoard.getBoardName() : "";

		for (String teamName : getTeamConfigMap().keySet())
		{
			TeamConfig teamConfig = getTeamConfigMap().get(teamName);
			List<String> boards = teamConfig.getEnabledScoreboards();
			boolean boardEnabled = (boards != null && !boardName.isEmpty() && boards.contains(boardName));
			result.append("Team='").append(teamName).append("'\n");
			result.append(" using enabled board=").append(boardEnabled).append("\n");
		}

		return result.toString();
	}

	/**
	 * Enable the default scoreboard for the given player.
	 *
	 * @param playerID The player's unique ID
	 * @return Message indicating the result of the operation
	 */
	public String enablePlayerBoard(UUID playerID)
	{
		String text = "";

		PlayerBoard playerBoard = getPlayerBoard(playerID);
		if (playerBoard != null)
		{
			playerBoard.remove();
			boards.remove(playerID);
		}

		if (!boards.containsKey(playerID))
		{
			text = plugin.getConfig().getString("messages.ontoggle.true");

			createDefaultScoreboard(playerID);
			generateListTeams(playerID);
		}

		if (text != null && !text.isEmpty())
			text = text.replace("&", "§");

		return text;
	}

	/**
	 * Reload the Team Config and any related Teams for the Player Tab List,
	 * then reassign all online player's to their respective teams for
	 * all active Player Scoreboards.
	 * @return A message describing the result of the operation.
	 */
	public String reloadPlayerTeams()
	{
		String text =
			HEADER + "§aTeam Config file reloaded\n" +
			HEADER + "§cCreating Teams for player's tab list\n";

		// Preserve existing team's for each player
		Map<String, List<String>> tempPlayerTeams = new HashMap<>();
		for (Player p : Bukkit.getOnlinePlayers())
		{
			UUID pid = p.getUniqueId();
			Team team = getPlayerTeam(pid);

			if (team != null)
			{
				String teamName = team.getName();
				List<String> teamPlayers = tempPlayerTeams.getOrDefault(teamName, new ArrayList<>());
				teamPlayers.add(p.getName());
				tempPlayerTeams.put(team.getName(), teamPlayers);
			}
		}

		File fo = new File(plugin.getDataFolder().getAbsolutePath() + "/teams");
		plugin.loadTeams(fo);

		// reassign teams for each player
		tempPlayerTeams.forEach(
			(String teamName, List<String> players) -> players.forEach(
				playerName -> {
					Team team = getTeam(teamName);
					if (team != null && !team.hasEntry(playerName))
					{
						team.addEntry(playerName);
					}
				}
			)
		);

		updateTeams();
		text += HEADER + "§aDefault Tab List Teams created for each player";
		return text;
	}

	/**
	 * Reload all Board configurations and Recreate Scoreboards for all
	 * online players.
	 *
	 * @return A message indicating the result of the operation.
	 */
	public String reloadAllPlayerBoards()
	{
		String text =
			HEADER + "§aConfig file reloaded" +
			HEADER + "§cCreating scoreboard for all players";

		plugin.reloadConfig();
		File fo = new File(plugin.getDataFolder().getAbsolutePath() + "/scoreboards");
		plugin.loadScoreboards(fo);

		plugin.setAllowedJoinScoreboard(
			plugin.getConfig().getBoolean("scoreboard.onjoin.use")
		);

		if (!plugin.isAllowedJoinScoreboard())
		{
			text += HEADER + "§cCreating scoreboard failed!";
			text += "Creating scoreboard when player join to server is cancelled!";
		}

		plugin.disableFirstTimeUse();

		for (Player p : Bukkit.getOnlinePlayers())
		{
			UUID pid = p.getUniqueId();
			PlayerBoard playerBoard = getPlayerBoard(pid);

			if (playerBoard != null)
			{
				playerBoard.remove();
				boards.remove(pid);
			}

			createDefaultScoreboard(pid);
		}

		text += HEADER + "§aDefault Scoreboard created for each player";
		return text;
	}

	/**
	 * Remove the given player's scoreboard and world timer.
	 *
	 * @param playerID The player's Unique ID
	 * @return The instance of the BoardManager implementation
	 */
	public BaseBoardManager removePlayer(UUID playerID)
	{
		plugin.removePlayerWorldTimer(playerID);
		removePlayerBoard(playerID);
		return this;
	}

	/**
	 * Remove registered player boards while stopping their running tasks
	 *
	 * @return The instance of the BoardManager implementation
	 */
	public BaseBoardManager removePlayerBoards()
	{
		if (boards != null && !boards.isEmpty())
		{
			for (PlayerBoard playerBoard : boards.values())
			{
				if (playerBoard != null)
				{
					playerBoard.remove();
					stopTasks(playerBoard.getPlayerID());
				}
			}
			boards.clear();
		}

		if (allBoards != null && !allBoards.isEmpty())
			allBoards.clear();

		return this;
	}

	/**
	 * Remove any registered boards for the specified Player ID
	 *
	 * @param playerID The player's Unique ID
	 * @return The instance of the BoardManager implementation
	 */
	public BaseBoardManager removePlayerBoard(UUID playerID)
	{
		PlayerBoard playerBoard = getPlayerBoard(playerID);
		if (playerBoard != null)
		{
			stopTasks(playerID);
			allBoards.remove(playerBoard);
			boards.remove(playerID);
		}
		return this;
	}

	/**
	 * Create a Player Board for the given player ID.
	 *
	 * @param playerID The player's Unique ID
	 * @return The instance of the BoardManager implementation
	 */
	public BaseBoardManager createPlayerBoards(UUID playerID)
	{
		List<String> usableBoards = getBoardsForPlayer(playerID);

		if (usableBoards != null && !usableBoards.isEmpty())
		{
			String boardName = usableBoards.get(0);
			BoardConfig boardConfig = boardConfigMap.get(boardName);
			new PlayerBoard(plugin, playerID, boardConfig);
		}
		return this;
	}

	/**
	 * Register a Player Board using the given Player's Unique ID.
	 *
	 * @param playerID The player's Unique ID
	 * @param playerBoard The given player's board
	 * @return The instance of the BoardManager implementation
	 */
	public BaseBoardManager addPlayerBoard(UUID playerID, PlayerBoard playerBoard)
	{
		boards.put(playerID, playerBoard);
		allBoards.add(playerBoard);
		return this;
	}

	/**
	 * Change the Given Player's active scoreboard, using the specified BoardConfiguration
	 * @param playerID The player's ID
	 * @param configName The Name of the BoardConfiguration describing the player's new Sidebar Scoreboard
	 * @return This board manager
	 */
	public BaseBoardManager changePlayerBoard(UUID playerID, String configName)
	{
		Team team = getPlayerTeam(playerID);
		// Reset the Player Scoreboard to a default board and remove all references
		resetPlayerScoreboard(playerID);

		// TODO: Update Player Tab Lists
		// Create the new Board using the given config
		createBoard(playerID, configName);

		setPlayerTeam(playerID, team.getName());
		logger.info("BaseBoardManager->changePlayerBoard: " + team.getName() + ":" + getPlayerName(playerID));
		generateListTeams(playerID);
		updatePlayerListTeam(team);
		return this;
	}

	/**
	 * Create and register a scoreboard for a player using
	 * the specified configuration name and player unique ID.
	 *
	 * @param playerID The player's Unique ID
	 * @param configName The board configuration name
	 * @return The created Player Board
	 */
	public PlayerBoard createBoard(UUID playerID, String configName) {
		logger.info("Creating Board " + configName + " for " + playerID);
		BoardConfig boardConfig = getBoardConfig(configName);

		if (boardConfig != null)
			return new PlayerBoard(plugin, playerID, boardConfig);

		return null;
	}

	/**
	 * Create a Temporary Scoreboard for the given player, using
	 * the player's unique ID and the name of a specific board configuration
	 *
	 * @param playerID The player's Unique ID
	 * @param name The board configuration name
	 * @return The created Player Board
	 */
	public PlayerBoard createTemporaryBoard(UUID playerID, String name)
	{
		BoardConfig info = getBoardConfigMap().getOrDefault(name, null);

		if (info != null)
		{
			PlayerBoard result = new PlayerBoard(
				plugin,
				playerID,
				info,
				true
			);
		}
		return null;
	}

	/**
	 * Create a Temporary Scoreboard for the given player, using
	 * the player's unique ID, text, title and update intervals
	 * for each.
	 *
	 * @param playerID The player's Unique ID
	 * @param boardName The name of the temporary board formatted as scoreboard.name
	 * @param text The text content to be displayed in the body of the scoreboard
	 * @param title The list of titles to be displayed as the scoreboard's titles.
	 * @param updateTitleTicks The update frequency, in ticks, for changing the title
	 * @param updateTextTicks The update frequency, in ticks, for changing the text
	 * @return The created Player Board
	 */
	public PlayerBoard createTemporaryBoard(
		UUID playerID,
		String boardName,
		List<String> text,
		List<String> title,
		int updateTitleTicks,
		int updateTextTicks
	){
		return new PlayerBoard(
			plugin,
			playerID,
			boardName,
			text,
			title,
			updateTitleTicks,
			updateTextTicks,
			true
		);
	}

	/**
	 * Create a Scoreboard for the given player, using
	 * the player's unique ID, text, title and update intervals
	 * for each.
	 *
	 * @param playerID The player's Unique ID
	 * @param text The text content to be displayed in the body of the scoreboard
	 * @param title The list of titles to be displayed as the scoreboard's titles.
	 * @param updateTitleTicks The update frequency, in ticks, for changing the title
	 * @param updateTextTicks The update frequency, in ticks, for changing the text
	 * @return The created Player Board
	 */
	public PlayerBoard createBoard(
		UUID playerID,
		List<String> text,
		List<String> title,
		int updateTitleTicks,
		int updateTextTicks
	) {
		return new PlayerBoard(
			plugin,
			playerID,
			text,
			title,
			updateTitleTicks,
			updateTextTicks
		);
	}

	/**
	 * Retrieve a list of all registered player boards.
	 *
	 * @return The list of registered player boards
	 */
	public List<PlayerBoard> getAllBoards() {
		return allBoards;
	}

	/**
	 * Retrieve the map of Unique Player IDs and their
	 * related Player Boards.
	 *
	 * @return The Player ID/Player Board map
	 */
	public HashMap<UUID, PlayerBoard> getBoards() {
		return boards;
	}

	/**
	 * Update the Title for a Player Board for the given Player
	 * @param playerID The player's Unique ID
	 * @return The instance of the BoardManager implementation
	 */
	public BaseBoardManager updateTitle(UUID playerID)
	{
		PlayerBoard board = getPlayerBoard(playerID);

		if (board != null)
			board.updateTitle();

		return this;
	}

	/**
	 * Apply A Player List Team's Settings and Entries to the Bukkit Scoreboard Team for the specified player.
	 * @param team The Player List Team
	 * @param playerID The Player whose Player List Scoreboard Team will be updated.
	 */
	public abstract void applyTeamEntries(
		net.frostbyte.quickboardx.api.Team team,
		UUID playerID
	);

	/**
	 * Update the Player's Team for Display in the Player Tab List (not the Sidebar)
	 * @return this BoardManager
	 */
	public abstract BaseBoardManager updatePlayerListTeams();

	/**
	 * Setup all Text lines for the Sidebar for the given Player Scoreboard.
	 * Each board displays one line for each team entry, in the Sidebar Slot, and each team can have a prefix and suffix,
	 * as well as entries.  On older MC versions, the prefix, suffix and entry are limited to 16 characters each.
	 * So a total of 48 characters could potentially displayed on one line.  However, in order to display
	 * properly, the input string must be parsed for Chat Color codes to carry over from prefix to entry to suffix.
	 *
	 * @param playerBoard The Player Scoreboard wrapper
	 * @return This board manager
	 */
	public BaseBoardManager setUpText(PlayerBoard playerBoard)
	{
		UUID playerID = playerBoard.getPlayerID();

		List<String> textLines = playerBoard.getTextLines();
		int lineIndex = textLines.size() - 1;

		for (String line : textLines)
		{
			if (lineIndex < 0)
				break;

			line = setPlaceholders(line, playerID);

			if (line.length() < 3)
				line = line + "§r";

			setSidebarTeamText(playerBoard, lineIndex, new BoardLine(affixMaxCharacters, line));
			lineIndex--;
		}
		return this;
	}

	/**
	 * Updates the text displayed in the Sidebar Slot for the given Player's Scoreboard - includes scroller elements,
	 * changeable text elements and the title.
	 *
	 * @param playerBoard The player board
	 * @return The instance of the BoardManager implementation
	 */
	public BaseBoardManager updateText(PlayerBoard playerBoard)
	{
		UUID playerID = playerBoard.getPlayerID();
		BoardConfig boardConfig = playerBoard.getBoardConfig();
		List<String> textLines = playerBoard.getTextLines();

		if (!textLines.isEmpty())
		{
			int teamIndex = textLines.size() - 1;
			// Cycle through the Teams on the player's scoreboard
			for (String textLine : textLines)
			{
				if (teamIndex < 0)
					break;

				if (boardConfig != null)
				{
					// Process all changeables
					List<String> changeables = boardConfig.getChangeables();
					if (changeables != null)
					{
						// Use each changeable key
						for (String changeKey : changeables)
						{
							// search the line of text for a changeable key
							// and replace the key with the relevant text
							// from the changeable's lines of text
							if (textLine.contains("{CH_" + changeKey + "}"))
							{
								textLine = textLine.replace("{CH_" + changeKey + "}", "");
								String changeText = playerBoard.getChangeText().get(changeKey);
								if (changeText != null && !changeText.isEmpty())
									textLine += changeText;
							}
						}
					}
					if (boardConfig.getScrollerNames() != null)
					{
						HashMap<String, String> scrollText = playerBoard.getScrollerText();

						for (String scrollerKey : boardConfig.getScrollerNames())
						{
							if (textLine.contains("{SC_" + scrollerKey + "}"))
							{
								textLine = textLine.replace("{SC_" + scrollerKey + "}", "");
								if (scrollText != null && !scrollText.isEmpty() && scrollText.containsKey(scrollerKey))
									textLine += scrollText.get(scrollerKey);
							}
						}
					}
				}
				textLine = setPlaceholders(textLine, playerID);
				WhenPluginUpdateTextEvent event = new WhenPluginUpdateTextEvent(playerID, textLine);

				new BukkitRunnable()
				{
					@Override
					public void run()
					{
						Bukkit.getPluginManager().callEvent(event);
					}
				}.runTaskLater(plugin, 1);

				BoardLine boardLine = new BoardLine(affixMaxCharacters, event.getText());
				setSidebarTeamText(
					playerBoard,
					teamIndex,
					boardLine
				);
				teamIndex--;
			}
		}

		return this;
	}

	/**
	 * Replace the placeholders in the given string based
	 * upon the given player's information.
	 *
	 * @param s The input string whose placeholders will be replaced
	 * @param playerID The Player's Unique ID
	 * @return The processed string
	 */
	public String setPlaceholders(String s, UUID playerID)
	{
		Player p = Bukkit.getPlayer(playerID);
		if (p != null)
		{
			s = s
				.replace("{PLAYER}", p.getName())
				.replace("{ONLINE}", Bukkit.getOnlinePlayers().size() + "")
				.replace("{TIME}", p.getWorld().getTime() + "");

			if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI") && PlaceholderAPI.containsPlaceholders(s))
				s = PlaceholderAPI.setPlaceholders(p, s);
			if (plugin.isMVdWPlaceholderAPI())
				s = be.maximvdw.placeholderapi.PlaceholderAPI.replacePlaceholders(p, s);

			s = ChatColor.translateAlternateColorCodes('&', s);
		}
		return s;
	}

	//TODO: Add the ability to Process Placeholders in TeamConfigurations: name, displayName, color, prefix, suffix, etc.
	/**
	 * Convert Chat Color characters in an input string, while trimming it to specific length.
	 *
	 * @param characters The maximum length of the output string
	 * @param string The input string to translate and trim
	 * @return The translated and trimmed version of the input string
	 */
	protected String maxChars(int characters, String string)
	{
		if (ChatColor.translateAlternateColorCodes('&', string).length() > characters)
			return string.substring(0, characters);
		return ChatColor.translateAlternateColorCodes('&', string);
	}

	/**
	 * Create an identifier tag for a Team using the given index.
	 *
	 * @param teamIndex The team index
	 * @return The Team Tag for the given index.
	 */
	protected String createTeamTag(int teamIndex)
	{
		return TEAM_TAG + teamIndex;
	}

	/**
	 * Split an input string into an a Prefix and Suffix.
	 * Also process color codes and splits those across the
	 * output prefix and suffix.
	 *
	 * @param string The input string that needs to be split
	 * @return The array of split strings - a prefix and suffix
	 */
	protected String[] splitString(String string) {
		StringBuilder prefix = new StringBuilder(
			string.substring(
				0,
				Math.min(string.length(), splitSize)
			));
		StringBuilder suffix = new StringBuilder(string.length() > splitSize ? string.substring(splitSize) : "");

		if (prefix.toString().length() > 1 && prefix.charAt(prefix.length() - 1) == '§')
		{
			prefix.deleteCharAt(prefix.length() - 1);
			suffix.insert(0, '§');
		}

		int length = prefix.length();
		boolean PASSED, UNDERLINE, STRIKETHROUGH, MAGIC, ITALIC;
		boolean BOLD = ITALIC = MAGIC = STRIKETHROUGH = UNDERLINE = PASSED = false;
		ChatColor textColor = null;

		for (int index = length - 1; index > -1; index--)
		{
			char section = prefix.charAt(index);

			if ((section == '§') && (index < prefix.length() - 1))
			{
				char c = prefix.charAt(index + 1);
				ChatColor color = ChatColor.getByChar(c);

				if (color != null)
				{
					if (color.equals(ChatColor.RESET))
						break;

					if (color.isFormat())
					{
						if ((color.equals(ChatColor.BOLD)) && (!BOLD))
						{
							BOLD = true;
						}
						else if ((color.equals(ChatColor.ITALIC)) && (!ITALIC))
						{
							ITALIC = true;
						}
						else if ((color.equals(ChatColor.MAGIC)) && (!MAGIC))
						{
							MAGIC = true;
						}
						else if ((color.equals(ChatColor.STRIKETHROUGH)) && (!STRIKETHROUGH))
						{
							STRIKETHROUGH = true;
						}
						else if ((color.equals(ChatColor.UNDERLINE)) && (!UNDERLINE))
						{
							UNDERLINE = true;
						}
					}
					else if (color.isColor())
						textColor = color;
				}
			}
			else if ((index > 0) && (!PASSED))
			{
				char c = prefix.charAt(index);
				char c1 = prefix.charAt(index - 1);

				if ((c != '§') && (c1 != '§') && (c != ' '))
					PASSED = true;
			}

			if ((!PASSED) && (prefix.charAt(index) != ' '))
				prefix.deleteCharAt(index);

			if (textColor != null)
				break;
		}

		String suf = suffix.toString();
		String result = suf.isEmpty() ? "" : getResult(BOLD, ITALIC, MAGIC, STRIKETHROUGH, UNDERLINE, textColor);

		if ((!suf.isEmpty()) && (!suf.startsWith("§")))
		{
			suffix.insert(0, result);
			suf = suffix.toString();
		}

		String pre = prefix.toString();

		return new String[]{
			pre.length() > splitSize ? pre.substring(0, splitSize) : pre,
			suf.length() > splitSize ? suf.substring(0, splitSize) : suf
		};
	}

	/**
	 * Helper method for converting a Chat Color to a string;
	 * strips/retains Color Codes based upon the given parameters.
	 *
	 * @param BOLD If true, and in the input, then retained in the output
	 * @param ITALIC If true, and in the input, then retained in the output
	 * @param MAGIC If true, and in the input, then retained in the output
	 * @param STRIKETHROUGH If true, and in the input, then retained in the output
	 * @param UNDERLINE If true, and in the input, then retained in the output
	 * @param color The input chat color
	 * @return The input chat color converted to a string
	 */
	protected String getResult(
		boolean BOLD,
		boolean ITALIC,
		boolean MAGIC,
		boolean STRIKETHROUGH,
		boolean UNDERLINE,
		ChatColor color
	) {
		return ((color != null) && (!color.equals(ChatColor.WHITE)) ? color : "") + "" + (BOLD ? ChatColor.BOLD : "") + (ITALIC ? ChatColor.ITALIC : "") + (MAGIC ? ChatColor.MAGIC : "") + (STRIKETHROUGH ? ChatColor.STRIKETHROUGH : "") + (UNDERLINE ? ChatColor.UNDERLINE : "");
	}

	@SuppressWarnings("unused")
	public static class BoardTask
	{
		private int taskID;
		private PlayerBoard playerBoard;
		private BoardOperation operation;

		public BoardTask(PlayerBoard playerBoard, BoardOperation operation)
		{
			this.playerBoard = playerBoard;
			this.operation = operation;
		}

		public BoardTask setTaskID(int taskID)
		{
			this.taskID = taskID;
			return this;
		}

		public int getTaskID() { return taskID; }

		public BoardTask setPlayerBoard(PlayerBoard playerBoard)
		{
			this.playerBoard = playerBoard;
			return this;
		}

		public PlayerBoard getPlayerBoard()
		{
			return playerBoard;
		}

		public BoardTask setOperation(BoardOperation operation)
		{
			this.operation = operation;
			return this;
		}

		public BoardOperation getOperation()
		{
			return operation;
		}
	}

	@SuppressWarnings("unused")
	public enum BoardOperation
	{
		UPDATE_TITLE(false),
		UPDATE_TEXT(false),
		UPDATE_SCROLLER(false),
		UPDATE_CHANGEABLE(false),
		REMOVE_TEMPORARY(true),
		UNKNOWN(false);

		int frequency;
		int lifetime;
		int totalElements;
		boolean runOnce;
		String key;

		BoardOperation(boolean runOnce) {
			this.lifetime = 0;
			this.frequency = 0;
			this.totalElements = 0;
			this.key = "";
			this.runOnce = runOnce;
		}

		public boolean shouldRunOnce()
		{
			return runOnce;
		}

		public int getTotalElements()
		{
			return totalElements;
		}

		public BoardOperation setTotalElements(int totalElements)
		{
			this.totalElements = totalElements;
			return this;
		}

		public BoardOperation setFrequency(int frequency)
		{
			this.frequency = frequency;
			return this;
		}

		public BoardOperation setLifetime(int lifetime)
		{
			this.lifetime = lifetime;
			return this;
		}

		public int getFrequency() { return frequency; }
		public int getLifetime() { return lifetime; }

		public String getKey()
		{
			return key;
		}

		public BoardOperation setKey(String key)
		{
			this.key = key;
			return this;
		}

		public boolean isEqual(BoardOperation op)
		{
			if (op == null)
				return false;
			if (frequency != op.frequency)
				return false;
			if (lifetime != op.lifetime)
				return false;
			if (!key.equals(op.key))
				return false;
			return ordinal() == op.ordinal();
		}
	}

	@SuppressWarnings("InnerClassMayBeStatic")
	public class BoardLine
	{
		private String prefix;
		private String entry;
		private String suffix = "" + ChatColor.RESET;
		private final String input;
		private int affixMaxChars = 16;
		private final int maxCharacters = affixMaxChars * 2;

		public BoardLine(int affixMax, String input) {
			this.affixMaxChars = affixMax;
			this.input = input;
			splitInput();
		}

		public BoardLine(String input) {
			this.input = input;
			splitInput();
		}

		public String getPrefix()
		{
			return prefix;
		}

		public String getSuffix()
		{
			return suffix;
		}

		private String[] splitString(int splitSize, String string)
		{
			StringBuilder left = new StringBuilder(
				string.substring(
					0,
					Math.min(string.length(), splitSize)
				));
			StringBuilder right = new StringBuilder(string.length() > splitSize ? string.substring(
				splitSize) : "");

			if (left.toString().length() > 1 && left.charAt(left.length() - 1) == '§')
			{
				left.deleteCharAt(left.length() - 1);
				right.insert(0, '§');
			}

			int length = left.length();
			boolean PASSED, UNDERLINE, STRIKETHROUGH, MAGIC, ITALIC;
			boolean BOLD = ITALIC = MAGIC = STRIKETHROUGH = UNDERLINE = PASSED = false;
			ChatColor textColor = null;

			for (int index = length - 1; index > -1; index--)
			{
				char section = left.charAt(index);

				if ((section == '§') && (index < left.length() - 1))
				{
					char c = left.charAt(index + 1);
					ChatColor color = ChatColor.getByChar(c);

					if (color != null)
					{
						if (color.equals(ChatColor.RESET))
							break;

						if (color.isFormat())
						{
							if ((color.equals(ChatColor.BOLD)) && (!BOLD))
							{
								BOLD = true;
							}
							else if ((color.equals(ChatColor.ITALIC)) && (!ITALIC))
							{
								ITALIC = true;
							}
							else if ((color.equals(ChatColor.MAGIC)) && (!MAGIC))
							{
								MAGIC = true;
							}
							else if ((color.equals(ChatColor.STRIKETHROUGH)) && (!STRIKETHROUGH))
							{
								STRIKETHROUGH = true;
							}
							else if ((color.equals(ChatColor.UNDERLINE)) && (!UNDERLINE))
							{
								UNDERLINE = true;
							}
						}
						else if (color.isColor())
							textColor = color;
					}
				}
				else if ((index > 0) && (!PASSED))
				{
					char c = left.charAt(index);
					char c1 = left.charAt(index - 1);

					if ((c != '§') && (c1 != '§') && (c != ' '))
						PASSED = true;
				}

				if ((!PASSED) && (left.charAt(index) != ' '))
					left.deleteCharAt(index);

				if (textColor != null)
					break;
			}

			String two = right.toString();
			String result = two.isEmpty() ? "" : getResult(BOLD, ITALIC, MAGIC, STRIKETHROUGH, UNDERLINE, textColor);

			if ((!two.isEmpty()) && (!two.startsWith("§")))
			{
				right.insert(0, result);
				two = right.toString();
			}

			String one = left.toString();

			return new String[]{
				one.length() > splitSize ? one.substring(0, splitSize) : one,
				two.length() > splitSize ? two.substring(0, splitSize) : two
			};
		}

		private void splitInput()
		{
			int iLength = input.length();
			ChatColor lastColor;
			String[] results;
			// Is the input string bigger than the prefix max?
			if (iLength > affixMaxChars)
			{
				results = splitString(affixMaxChars, input);
				prefix = maxChars(affixMaxChars, results[0]);
				suffix = maxChars(affixMaxChars, results[1]);
			}
			else
			{
				// The input will fit within the prefix
				prefix = input;
			}
		}
	}
}
