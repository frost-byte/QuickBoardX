package net.frostbyte.quickboardx.v1_14_R1.managers;

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
				Team.OptionStatus.valueOf(parsedTeam.getCollision().toUpperCase())
			);

			bukkitTeam.setOption(
				NAME_TAG_VISIBILITY,
				Team.OptionStatus.valueOf(parsedTeam.getNameTagVisibility().toUpperCase())
			);
			parsedTeam.getEntries().forEach(bukkitTeam::addEntry);
		}
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

		boards.put(playerID, playerBoard);
		p.setScoreboard(board);

		return playerBoard;
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
	public void removePlayerFromTeam(UUID playerID, net.frostbyte.quickboardx.api.Team currentTeam)
	{
		Map<UUID, Player> players = new HashMap<>();
		Bukkit.getOnlinePlayers().forEach(p -> players.put(p.getUniqueId(), p));
		Set<UUID> ids = players.keySet();

		for (UUID ownerID : ids)
			ids.forEach(targetID -> updateTeamForOtherPlayer(targetID, ownerID));
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

	private Team asBukkitTeam(net.frostbyte.quickboardx.api.Team team, UUID playerID)
	{
		if (team != null && playerID != null && playerTeams.containsKey(playerID))
		{
			String teamName = team.getName();
			net.frostbyte.quickboardx.api.Team parsedTeam = team.applyPlaceholders(playerID);
			Scoreboard scoreboard = getScoreboard(playerID);

			if (scoreboard != null)
			{
				Team bukkitTeam = scoreboard.getTeam(teamName);

				if (bukkitTeam == null) {
					bukkitTeam = scoreboard.registerNewTeam(teamName);
				}
				bukkitTeam.setDisplayName(parsedTeam.getDisplayName());
				bukkitTeam.setPrefix(parsedTeam.getPrefix());
				bukkitTeam.setSuffix(parsedTeam.getSuffix());
				bukkitTeam.setColor(ChatColor.valueOf(parsedTeam.getNameTagVisibility().toUpperCase()));
				bukkitTeam.setAllowFriendlyFire(parsedTeam.allowFriendlyFire());
				bukkitTeam.setCanSeeFriendlyInvisibles(parsedTeam.canSeeFriendlyInvisibles());
				bukkitTeam.setOption(
					COLLISION_RULE,
					Team.OptionStatus.valueOf(parsedTeam.getCollision().toUpperCase())
				);

				bukkitTeam.setOption(
					NAME_TAG_VISIBILITY,
					Team.OptionStatus.valueOf(parsedTeam.getNameTagVisibility().toUpperCase())
				);
				parsedTeam.getEntries().forEach(bukkitTeam::addEntry);

				return bukkitTeam;
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateTeamForOtherPlayer(UUID targetID, UUID boardUserID)
	{
		if (
			targetID == null ||
			boardUserID == null ||
			targetID.equals(boardUserID)
		) {
			return;
		}

		Player target = getPlayerFromID(targetID);
		if (target == null)
			return;

		net.frostbyte.quickboardx.api.Team targetTeam = getPlayerTeam(targetID);
		Team bukkitTeam = asBukkitTeam(targetTeam, boardUserID);

		if (bukkitTeam != null)
		{
			String targetName = target.getDisplayName();

			if (bukkitTeam.hasEntry(targetName))
				bukkitTeam.removeEntry(targetName);

			bukkitTeam.addEntry(targetName);
		}
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

		for (net.frostbyte.quickboardx.api.Team team : getTabListTeams())
		{
			// Each QBX Player List Team should have a entries for each player
			// appearing in the Player Tab List
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
