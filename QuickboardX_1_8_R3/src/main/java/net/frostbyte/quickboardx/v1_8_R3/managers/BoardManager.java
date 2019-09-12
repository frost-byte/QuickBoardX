package net.frostbyte.quickboardx.v1_8_R3.managers;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.frostbyte.quickboardx.PlayerBoard;
import net.frostbyte.quickboardx.QuickBoardX;
import net.frostbyte.quickboardx.managers.BaseBoardManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import java.util.List;
import java.util.UUID;

@SuppressWarnings({"FieldCanBeLocal", "unused"})
@Singleton
public class BoardManager extends BaseBoardManager
{
	private ScoreboardManager scoreboardManager;

	@Inject
	public BoardManager(QuickBoardX plugin)
	{
		super(plugin);
		this.scoreboardManager = plugin.getServer().getScoreboardManager();
		this.splitSize = 16;
		this.affixMaxCharacters = 16;
		this.maxCharacters = affixMaxCharacters * 2;
	}

	private Player getPlayerFromID(UUID playerID)
	{
		return Bukkit.getPlayer(playerID);
	}
	private Scoreboard getScoreboard(UUID playerID)
	{
		Player p = getPlayerFromID(playerID);

		if (p != null && p.isOnline())
			return p.getScoreboard();
		else
			return null;
	}

	@Override
	public BaseBoardManager resetPlayerScoreboard(UUID playerID)
	{
		Player p = getPlayerFromID(playerID);
		PlayerBoard playerBoard = getPlayerBoard(playerID);

		if (playerBoard != null)
			removeAll(playerBoard);

		if (p != null)
			p.setScoreboard(scoreboardManager.getNewScoreboard());

		return this;
	}

	@Override
	public BaseBoardManager setTitle(PlayerBoard playerBoard, String text)
	{
		Scoreboard board = getScoreboard(playerBoard.getPlayerID());

		if (board != null)
		{
			Objective score = board.getObjective("score");
			text = text.replace("&", "§");
			score.setDisplayName(text);
		}
		return this;
	}

	@Override
	public BaseBoardManager setPlayerScoreboard(PlayerBoard playerBoard)
	{
		Scoreboard scoreboard = getScoreboard(playerBoard.getPlayerID());
		Player p = getPlayerFromID(playerBoard.getPlayerID());

		if (scoreboard != null && p != null)
			p.setScoreboard(scoreboard);

		return this;
	}

	@Override
	public BaseBoardManager removeAll(PlayerBoard playerBoard)
	{
		UUID playerID = playerBoard.getPlayerID();

		Player p = getPlayerFromID(playerID);
		if (p != null && p.isOnline())
		{
			Scoreboard scoreboard = p.getScoreboard();
			if (scoreboard != null)
			{
				for (Team t : scoreboard.getTeams())
					t.unregister();
				for (String entry : scoreboard.getEntries())
					scoreboard.resetScores(entry);

				Objective score = scoreboard.getObjective("score");
				if (score != null)
					score.unregister();
			}
		}

		stopTasks(playerID);
		allBoards.remove(playerBoard);
		boards.remove(playerID);

		return this;
	}

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

	@Override
	public PlayerBoard createScoreboard(PlayerBoard playerBoard)
	{
		UUID playerID = playerBoard.getPlayerID();
		Player p = getPlayerFromID(playerID);

		if (p == null)
			return null;

		Scoreboard board = scoreboardManager.getNewScoreboard();
		Objective score = board.registerNewObjective(
			"score",
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

		boards.putIfAbsent(playerID, playerBoard);
		p.setScoreboard(board);

		return playerBoard;
	}

	@Override
	public BaseBoardManager setTeamText(
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

	@Override
	public boolean isPlayerInvalid(UUID playerID)
	{
		return getPlayerFromID(playerID) == null;
	}

	@Override
	public String getPlayerWorldName(UUID playerID)
	{
		Player p = getPlayerFromID(playerID);
		return (p != null) ? p.getWorld().getName() : null;
	}

	@Override
	public boolean getPlayerPermission(UUID playerID, String boardName)
	{
		Player p = getPlayerFromID(playerID);
		return (p != null) && p.hasPermission(boardName);
	}

	@Override
	public String getPlayerName(UUID playerID)
	{
		Player p = getPlayerFromID(playerID);
		return (p != null) ? p.getName() : null;
	}

}
