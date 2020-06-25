package net.frostbyte.quickboardx.v1_16_R1.managers;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.frostbyte.quickboardx.PlayerBoard;
import net.frostbyte.quickboardx.QuickBoardX;
import net.frostbyte.quickboardx.config.TeamConfig;
import net.frostbyte.quickboardx.managers.BaseBoardManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.OptionStatus;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.bukkit.scoreboard.Team.Option.COLLISION_RULE;
import static org.bukkit.scoreboard.Team.Option.NAME_TAG_VISIBILITY;

@Singleton
public class BoardManager extends BaseBoardManager
{
	private final ScoreboardManager scoreboardManager;

	@Inject
	public BoardManager(QuickBoardX plugin)
	{
		super(plugin);
		this.scoreboardManager = plugin.getServer().getScoreboardManager();
		this.splitSize = 32;
		this.affixMaxCharacters = 32;
		this.maxCharacters = affixMaxCharacters * 2;
	}

	private Player getPlayerFromID(UUID playerID) { return Bukkit.getPlayer(playerID); }
	private Scoreboard getScoreboard(UUID playerID)
	{
		Player p = getPlayerFromID(playerID);

		if (p != null && p.isOnline())
			return p.getScoreboard();
		else
			return null;
	}

	@SuppressWarnings("unused")
	private ChatColor convertTeamColor(String teamColor)
	{
		return ChatColor.valueOf(teamColor.toUpperCase());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void applyTeamEntries(
		net.frostbyte.quickboardx.api.Team team,
		UUID playerID
	) {
		if (
			team != null &&
			playerID != null
		) {
			String teamName = team.getName();
			net.frostbyte.quickboardx.api.Team parsedTeam = team.applyPlaceholders(playerID);
			Player target = getPlayerFromID(playerID);

			if (target == null)
				return;

			Scoreboard scoreboard = target.getScoreboard();
			Team bukkitTeam = scoreboard.getTeam(teamName);

			if (bukkitTeam == null) {
				bukkitTeam = scoreboard.registerNewTeam(teamName);
			}

			bukkitTeam.setDisplayName(parsedTeam.getDisplayName());
			bukkitTeam.setPrefix(parsedTeam.getPrefix());
			bukkitTeam.setSuffix(parsedTeam.getSuffix());
			bukkitTeam.setColor(ChatColor.valueOf(parsedTeam.getColor().toUpperCase()));
			bukkitTeam.setAllowFriendlyFire(parsedTeam.allowFriendlyFire());
			bukkitTeam.setCanSeeFriendlyInvisibles(parsedTeam.canSeeFriendlyInvisibles());
			bukkitTeam.setOption(
				COLLISION_RULE,
				OptionStatus.valueOf(parsedTeam.getCollision().toUpperCase())
			);

			bukkitTeam.setOption(
				NAME_TAG_VISIBILITY,
				OptionStatus.valueOf(parsedTeam.getNameTagVisibility().toUpperCase())
			);
			parsedTeam.getEntries().forEach(bukkitTeam::addEntry);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BaseBoardManager resetPlayerScoreboard(UUID playerID)
	{
		Player p = getPlayerFromID(playerID);
		PlayerBoard playerBoard = getPlayerBoard(playerID);

		if (playerBoard != null)
			removeAll(playerBoard);

		if (p != null)
		{
			Scoreboard board = scoreboardManager.getNewScoreboard();
			p.setScoreboard(board);
		}

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BaseBoardManager setTitle(PlayerBoard playerBoard, String text)
	{
		Scoreboard board = getScoreboard(playerBoard.getPlayerID());

		if (board != null)
		{
			Objective score = board.getObjective("score");
			text = text.replace("&", "§");
			if (score != null)
				score.setDisplayName(text);
		}
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BaseBoardManager setPlayerScoreboard(PlayerBoard playerBoard)
	{
		Scoreboard scoreboard = getScoreboard(playerBoard.getPlayerID());
		Player p = getPlayerFromID(playerBoard.getPlayerID());

		if (scoreboard != null && p != null)
			p.setScoreboard(scoreboard);

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BaseBoardManager removeAll(PlayerBoard playerBoard)
	{
		UUID playerID = playerBoard.getPlayerID();

		Player p = getPlayerFromID(playerID);
		if (p != null && p.isOnline())
		{
			Scoreboard scoreboard = p.getScoreboard();
			for (Team t : scoreboard.getTeams())
				t.unregister();
			for (String entry : scoreboard.getEntries())
				scoreboard.resetScores(entry);

			Objective score = scoreboard.getObjective("score");
			if (score != null)
				score.unregister();
		}

		stopTasks(playerID);
		allBoards.remove(playerBoard);
		boards.remove(playerID);

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int scheduleTask(Runnable runnable, BoardOperation operation)
	{
		BukkitScheduler scheduler = Bukkit.getScheduler();

		if (operation.shouldRunOnce())
		{
			return scheduler.scheduleSyncDelayedTask(plugin, runnable, operation.getLifetime());
		}
		else
		{
			return scheduler.scheduleSyncRepeatingTask(plugin, runnable, 0, operation.getFrequency());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BaseBoardManager stopTasks(UUID playerID)
	{
		List<Integer> tasksList = getTasksList(playerID);

		if (tasksList != null && !tasksList.isEmpty())
		{
			for (int taskID : tasksList)
				Bukkit.getScheduler().cancelTask(taskID);

			tasksList.clear();
			playerBoardTasks.put(playerID, tasksList);
		}
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BaseBoardManager showPlayerBoard(UUID playerID)
	{
		PlayerBoard playerBoard = getPlayerBoard(playerID);
		Scoreboard scoreboard = getScoreboard(playerID);
		Player p = getPlayerFromID(playerID);

		if (p != null && playerBoard != null && scoreboard != null)
			p.setScoreboard(scoreboard);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PlayerBoard createScoreboard(PlayerBoard playerBoard)
	{
		UUID playerID = playerBoard.getPlayerID();
		Player p = getPlayerFromID(playerID);

		boolean applyPlayerTeam = false;
		net.frostbyte.quickboardx.api.Team playerTeam = getPlayerTeam(playerID);
		List<String> boardTeams = getTeamsForBoard(playerBoard.getBoardName());

		if (boardTeams != null && !boardTeams.isEmpty() && playerTeam != null)
		{
			applyPlayerTeam = true;
			logger.info("BoardManager->createScoreboard: Will Apply existing Player List Teams");
		}

		if (p == null)
			return null;


		Scoreboard board = scoreboardManager.getNewScoreboard();
		// parameters: objective name, criteria, displayName
		Objective score = board.registerNewObjective(
			"score",
			"dummy",
			playerBoard.getDisplayName()
		);
		score.setDisplaySlot(DisplaySlot.SIDEBAR);
		List<String> textLines = playerBoard.getTextLines();

		for (int i = 0; i < ChatColor.values().length; i++)
		{
			if (i == textLines.size())
				break;

			ChatColor chatColor = ChatColor.values()[i];
			String entry = chatColor.toString() + ChatColor.RESET.toString();
			String teamTag = createTeamTag(i);
			Team team = board.registerNewTeam(teamTag);
			team.addEntry(entry);
			score.getScore(entry).setScore(i);
		}

		if (applyPlayerTeam)
		{
			createBoardPlayerTeams(playerID);

			Team bukkitPlayerTeam = board.getTeam(playerTeam.getName());

			if (bukkitPlayerTeam != null)
				bukkitPlayerTeam.addEntry(p.getDisplayName());
		}

		boards.put(playerID, playerBoard);
		p.setScoreboard(board);

		return playerBoard;
	}

	// TODO: Does having per player scoreboards for the sidebar slot require updating all player's scoreboards, when using Teams for the Player List slot?

	/**
	 * Create all the Teams for the Player Tab List (not Sidebar slot)
	 * @param playerID The player ID to generate the teams for.
	 * @return this BoardManager
	 */
	@SuppressWarnings({"unused", "UnusedReturnValue"})
	public BaseBoardManager createBoardPlayerTeams(UUID playerID)
	{
		PlayerBoard playerBoard = getPlayerBoard(playerID);
		Scoreboard scoreboard = getScoreboard(playerID);

		if (playerBoard != null && scoreboard != null)
		{
			List<String> boardTeams = getTeamsForBoard(playerBoard.getBoardName());
			if (boardTeams != null && !boardTeams.isEmpty())
			{
				boardTeams.forEach(teamName -> {
					net.frostbyte.quickboardx.api.Team team = getTeam(teamName);
					Team bukkitTeam = scoreboard.getTeam(teamName);
					if (bukkitTeam == null) {
						bukkitTeam = scoreboard.registerNewTeam(teamName);
						bukkitTeam.setDisplayName(team.getDisplayName());
						bukkitTeam.setPrefix(team.getPrefix());
						bukkitTeam.setSuffix(team.getSuffix());
						bukkitTeam.setColor(ChatColor.valueOf(team.getColor().toUpperCase()));
						bukkitTeam.setAllowFriendlyFire(team.allowFriendlyFire());
						bukkitTeam.setCanSeeFriendlyInvisibles(team.canSeeFriendlyInvisibles());
						bukkitTeam.setOption(
							COLLISION_RULE,
							OptionStatus.valueOf(team.getCollision().toUpperCase())
						);

						bukkitTeam.setOption(
							NAME_TAG_VISIBILITY,
							OptionStatus.valueOf(team.getNameTagVisibility().toUpperCase())
						);
					}
				});
			}
		}
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BaseBoardManager updatePlayerListTeams()
	{
		// Update Teams for all players online
		Bukkit.getOnlinePlayers().forEach(p -> generateListTeams(p.getUniqueId()));
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BaseBoardManager updatePlayerListTeam(net.frostbyte.quickboardx.api.Team team)
	{
		String teamName = team.getName();
		logger.info("BoardManager: Update PlayerList Team: " + teamName);
		TeamConfig teamConfig = findTeamConfig(team.getName());

		if (teamConfig == null)
			return this;

		// Loop through all online players and update the team in their Player List
		Bukkit.getOnlinePlayers().forEach(
			p -> {
				// Iterated Player
				final UUID pID = p.getUniqueId();
				String updatePlayerName = getPlayerName(pID);
				PlayerBoard playerBoard = getPlayerBoard(pID);

				if (playerBoard != null)
				{
					String boardName = playerBoard.getBoardName();
					if (
						teamConfig.applyToAllScoreboards() ||
						teamConfig.getEnabledScoreboards().contains(boardName)
					) {
						logger.info("BoardManager: updatePlayerListTeams -> updating Scoreboard Team for: " + updatePlayerName);
						applyTeamEntries(team, pID);
					}
				}
			}
		);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BaseBoardManager setSidebarTeamText(
		PlayerBoard playerBoard,
		int teamIndex,
		BoardLine boardLine
	)
	{
		Scoreboard board = getScoreboard(playerBoard.getPlayerID());

		if (board != null && boardLine != null)
		{
			String prefix = boardLine.getPrefix();
			String suffix = boardLine.getSuffix();
			String teamTag = createTeamTag(teamIndex);
			Team team = board.getTeam(teamTag);

			if (team != null)
			{
				team.setPrefix(prefix);
				team.setSuffix(suffix);
			}
		}
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isPlayerInvalid(UUID playerID)
	{
		return getPlayerFromID(playerID) == null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removePlayerFromTeam(UUID playerID, net.frostbyte.quickboardx.api.Team currentTeam)
	{
		Player player = getPlayerFromID(playerID);

		if (player == null)
			return;

		currentTeam.removeEntry(player.getName());
		Bukkit.getOnlinePlayers().forEach(p -> applyTeamEntries(currentTeam, p.getUniqueId()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeTeamForOtherPlayer(UUID targetID, UUID boardUserID)
	{
		if (targetID == null || boardUserID == null || targetID.equals(boardUserID))
			return;

		Player target = getPlayerFromID(targetID);
		if (target == null)
			return;

		String targetName = target.getDisplayName();

		Scoreboard userScoreBoard = getScoreboard(boardUserID);

		net.frostbyte.quickboardx.api.Team targetTeam = getPlayerTeam(targetID);
		if (targetTeam != null && userScoreBoard != null)
		{
			Team targetBukkitTeam = userScoreBoard.getTeam(targetTeam.getName());

			if (
				targetBukkitTeam != null &&
				targetBukkitTeam.hasEntry(targetName)
			) {
				targetBukkitTeam.removeEntry(targetName);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateTeamForOtherPlayer(UUID targetID, UUID boardUserID)
	{
		if (targetID == null || boardUserID == null || targetID.equals(boardUserID))
			return;

		applyTeamEntries(getPlayerTeam(targetID), boardUserID);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BaseBoardManager updateTeams()
	{
		Set<UUID> ids = Bukkit.getOnlinePlayers()
			.stream()
			.map(Entity::getUniqueId)
			.collect(Collectors.toSet());

		// For each Team apply that team as a bukkit team to each player's scoreboard
		// Each team includes a list of entries defining which players are on that team.
		for (net.frostbyte.quickboardx.api.Team team : getTabListTeams())
		{
			// Create/Update bukkit scoreboard teams for each player
			// Each QBX Team should have a list of entries of players appearing in the Player Tab List
			ids.forEach(targetID -> applyTeamEntries(team, targetID));
		}

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPlayerWorldName(UUID playerID)
	{
		Player p = getPlayerFromID(playerID);
		return (p != null) ? p.getWorld().getName() : null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean getPlayerPermission(UUID playerID, String boardName)
	{
		Player p = getPlayerFromID(playerID);
		return (p != null) && p.hasPermission(boardName);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPlayerName(UUID playerID)
	{
		Player p = getPlayerFromID(playerID);
		return (p != null) ? p.getName() : null;
	}
}
