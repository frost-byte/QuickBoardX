package me.tade.quickboard.v1_12_R1.managers;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.tade.quickboard.PlayerBoard;
import me.tade.quickboard.QuickBoard;
import me.tade.quickboard.managers.BaseBoardManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class BoardManager extends BaseBoardManager
{
	/**
	 * Map of Unique IDs for Boards to Bukkit Scoreboards
	 */
	private Map<UUID, Scoreboard> boardsMap = new ConcurrentHashMap<>();

	/**
	 * Player Scoreboard Tasks for updating the Title, Changeable and Scrolling text elements.
	 * Also includes timers for removing Temporary boards.
	 */
	private Map<UUID, List<BukkitRunnable>> playerBoardRunnables = new HashMap<>();

	private ScoreboardManager scoreboardManager;

	@Inject
	public BoardManager(QuickBoard plugin)
	{
		super(plugin);
		this.scoreboardManager = plugin.getServer().getScoreboardManager();
		this.splitSize = 16;
		this.affixMaxCharacters = 16;
		this.maxCharacters = affixMaxCharacters * 2;
	}

	/**
	 * Retrieve a Player using their UUID
	 * @param playerID The player's Unique ID
	 * @return The player matching the Unique ID
	 */
	private Player getPlayerFromID(UUID playerID) { return Bukkit.getPlayer(playerID); }

	/**
	 * Retrieve the stored scoreboard for the given player ID
	 * @param playerID The Player's Unique ID
	 * @return The stored scoreboard, or null, if one is not stored.
	 */
	private Scoreboard getScoreboard(UUID playerID) { return boardsMap.getOrDefault(playerID, null); }
	private List<BukkitRunnable> getRunnablesList(UUID playerID) { return playerBoardRunnables.getOrDefault(playerID, null); }

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
		boardsMap.remove(playerID);
		allBoards.remove(playerBoard);
		boards.remove(playerID);

		return this;
	}

	@Override
	public int startTask(BoardTask task)
	{
		BukkitRunnable runnable;
		BukkitTask bukkitTask;
		BoardOperation operation = task.getOperation();
		PlayerBoard board = task.getPlayerBoard();
		UUID playerID = board.getPlayerID();
		String key = operation.getKey();

		switch (operation)
		{
			case UPDATE_TITLE:
				runnable = new BukkitRunnable()
				{
					private int index = 0;
					private int size = operation.getTotalElements();

					@Override
					public void run()
					{
						if (board.canUpdate())
						{
							if (++index >= size)
								index = 0;
							try
							{
								board.updateTitle(index);
							}
							catch (Exception ex)
							{
								this.cancel();
							}
						}
					}
				};
				bukkitTask = runnable.runTaskTimerAsynchronously(plugin, 0, operation.getFrequency());
				break;
			case UPDATE_TEXT:
				runnable = new BukkitRunnable()
				{
					@Override
					public void run()
					{
						try
						{
							updateText(board);
						}
						catch (Exception ex)
						{
							this.cancel();
						}
					}
				};
				bukkitTask = runnable.runTaskTimerAsynchronously(plugin, 0, operation.getFrequency());
				break;
			case UPDATE_SCROLLER:
				runnable = new BukkitRunnable()
				{
					@Override
					public void run()
					{
						try
						{
							board.updateScrollerText(key);
						}
						catch (Exception ex)
						{
							this.cancel();
						}
					}
				};
				bukkitTask = runnable.runTaskTimerAsynchronously(plugin, 1, operation.getFrequency());
				break;
			case UPDATE_CHANGEABLE:
				runnable = new BukkitRunnable()
				{
					private int changeSize = operation.getTotalElements();
					int idx = 0;

					public void run()
					{
						idx = (idx + 1) % changeSize;
						if (idx >= changeSize)
							idx = 0;

						try
						{
							board.updateChangeable(key, idx);
						}
						catch (Exception ex)
						{
							this.cancel();
						}
					}
				};
				bukkitTask = runnable.runTaskTimer(plugin, 0, operation.getFrequency());
				break;
			case REMOVE_TEMPORARY:
				runnable = new BukkitRunnable()
				{
					@Override
					public void run()
					{
						try
						{
							board.removeTemporary();
						}
						catch (Exception ex)
						{
							this.cancel();
						}
					}
				};
				bukkitTask = runnable.runTaskLater(plugin, operation.getLifetime());
				break;
			case UNKNOWN:
			default:
				throw new IllegalStateException("Unexpected value: " + operation);
		}

		List<BukkitRunnable> runnables = getRunnablesList(playerID);

		if (runnables == null)
			runnables = new ArrayList<>();

		runnables.add(runnable);
		playerBoardRunnables.put(playerID, runnables);

		if (bukkitTask != null)
			return bukkitTask.getTaskId();

		return -1;
	}

	@Override
	public BaseBoardManager stopTasks(UUID playerID)
	{
		List<BukkitRunnable> runnablesList = getRunnablesList(playerID);
		if (runnablesList != null && !runnablesList.isEmpty())
		{
			for (BukkitRunnable runnable : runnablesList)
				runnable.cancel();

			runnablesList.clear();
			playerBoardRunnables.put(playerID, runnablesList);
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

		boards.put(playerID, playerBoard);
		boardsMap.put(playerID, board);
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
