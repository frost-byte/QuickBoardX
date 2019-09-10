package net.frostbyte.quickboardx;

import com.google.inject.Inject;
import net.frostbyte.quickboardx.api.QuickBoardAPI;
import net.frostbyte.quickboardx.cmds.BoardCommand;
import net.frostbyte.quickboardx.cmds.CommandSetup;
import net.frostbyte.quickboardx.config.BoardConfig;
import net.frostbyte.quickboardx.managers.BaseBoardManager;
import net.frostbyte.quickboardx.managers.BaseMessagingManager;
import net.frostbyte.quickboardx.util.VersionManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings({"WeakerAccess", "FieldCanBeLocal", "unused"})
public class QuickBoardX extends JavaPlugin implements Listener, QuickBoardAPI
{
	private boolean allowedJoinScoreboard;
	private boolean MVdWPlaceholderAPI, PlaceholderAPI;
	private HashMap<UUID, Long> playerWorldTimer = new HashMap<>();
	private PluginUpdater pluginUpdater;
	private boolean firstTimeUsePlugin = false;
	private Metrics metrics;
	public static final String SEP = System.lineSeparator();

	@Inject
	private BaseBoardManager boardManager;

	@Inject
	private BaseMessagingManager messagingManager;

	@Override
	public void onEnable()
	{
		getLogger().info("------------------------------");
		getLogger().info("          QuickBoard          ");

		Bukkit.getPluginManager().registerEvents(this, this);
		getLogger().info("Listeners registered");

		registerAPI();
		if (!registerDependencies())
			return;

		registerCommands();
		loadScoreboards();
		registerConfig();
		registerMetrics();
		startUpdateTasks();
		registerListeners();
	}

	@Override
	public void onDisable()
	{
		if (boardManager != null)
			boardManager.removePlayerBoards();
	}

	public Metrics getMetrics()
	{
		return metrics;
	}

	private void registerAPI()
	{
		getServer().getServicesManager().register(
			QuickBoardAPI.class,
			this,
			this,
			ServicePriority.Normal
		);
		getLogger().info("API registered");
	}

	private boolean registerDependencies()
	{
		getLogger().info("Registering Dependencies");
		metrics = new Metrics(this);
		pluginUpdater = new PluginUpdater(this);

		try
		{
			getLogger().info("Binding Module Bridge");
			VersionManager.loadBinderModule(this);
		}
		catch (Exception e)
		{
			getLogger().warning("Error! Could not load Binder Bridge!");
			e.printStackTrace();
			getServer().getPluginManager().disablePlugin(this);
			return false;
		}

		try
		{
			getLogger().info("Binding Version Bridge");
			VersionManager.loadBridge();
		}
		catch (Exception e)
		{
			getLogger().warning("Error! Could not load Version Bridge!");
			getServer().getPluginManager().disablePlugin(this);
			return false;
		}

		boardManager = VersionManager.getBoardManager();
		messagingManager = VersionManager.getMessagingManager();
		VersionManager.injectMembers(this);
		return true;
	}

	private void registerListeners() {
		PluginManager pm = getServer().getPluginManager();

		getLogger().info("Registering listeners");
		pm.registerEvents(
			messagingManager,
			this
		);
	}

	public boolean isFirstTimeUsePlugin() {
		return firstTimeUsePlugin;
	}

	public boolean updaterNeedsUpdate()
	{
		return pluginUpdater.needUpdate();
	}

	public boolean areMetricsEnabled() {
		return metrics.isEnabled();
	}

	private void loadScoreboards()
	{
		getLogger().info("Loading Board Configurations...");
		File fo = new File(getDataFolder().getAbsolutePath() + "/scoreboards/");
		if (!fo.exists())
		{
			//noinspection ResultOfMethodCallIgnored
			fo.mkdirs();

			firstTimeUsePlugin = true;

			File scFile = new File(getDataFolder().getAbsolutePath() + "/scoreboards/scoreboard.default.yml");

			try
			{
				//noinspection ResultOfMethodCallIgnored
				scFile.createNewFile();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

			InputStream str = getResource("scoreboard.default.yml");
			try
			{
				if (str == null)
					throw new IOException("Could not load default scoreboard settings.");
				copy(str, scFile);
			}
			catch (IOException e1)
			{
				e1.printStackTrace();
			}
		}
		loadScoreboards(fo);
		getLogger().info("Scoreboards loaded");
	}

	private void registerConfig() {
		getConfig().options().copyDefaults(true);
		saveConfig();
		getLogger().info("Config registered");

		allowedJoinScoreboard = getConfig().getBoolean("scoreboard.onjoin.use");
		MVdWPlaceholderAPI = getConfig().getBoolean("scoreboard.MVdWPlaceholderAPI", false);
		PlaceholderAPI = getConfig().getBoolean("scoreboard.PlaceholderAPI", true);

		if (isMVdWPlaceholderAPI())
		{
			if (!Bukkit.getPluginManager().isPluginEnabled("MVdWPlaceholderAPI"))
			{
				setMVdWPlaceholderAPI(false);
			}
		}
		if (isPlaceholderAPI())
		{
			if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI"))
			{
				setPlaceholderAPI(false);
			}
		}
	}

	private void registerMetrics() {
		metrics.addCustomChart(new Metrics.SingleLineChart("scoreboards")
		{
			public int getValue()
			{
				return boardManager.getConfigCount();
			}
		});

	}

	private void startUpdateTasks() {
		getLogger().info("Update Tasks started");

		getLogger().info("          QuickBoardX          ");
		getLogger().info("-------------------------------");

		for (Player p : Bukkit.getOnlinePlayers())
		{
			playerWorldTimer.put(
				p.getUniqueId(),
				System.currentTimeMillis() + 5000
			);
		}

		new BukkitRunnable()
		{
			@Override
			public void run()
			{
				for (UUID pid : playerWorldTimer.keySet())
				{
					long time = playerWorldTimer.get(pid);

					if (time < System.currentTimeMillis())
						continue;

					if (isAllowedJoinScoreboard())
					{
						createDefaultScoreboard(pid);
					}
				}
			}
		}.runTaskTimer(this, 0, 20);
	}

	private List<String> getBoardsForPlayer(UUID playerID) {
		return boardManager.getBoardsForPlayer(playerID);
	}

	/**
	 * Register Console and Player Commands with ACF
	 */
	private void registerCommands()
	{
		getLogger().info("Registering Commands");

		CommandSetup setup = new CommandSetup(this);
		BoardCommand command = new BoardCommand(this);

		setup
			.registerCommandContexts()
			.registerCommandReplacements()
			.registerCommandCompletions()
			.registerCommand(command);
	}

	public void createDefaultScoreboard(UUID playerID)
	{
		Player player = Bukkit.getPlayer(playerID);
		if (player == null)
			return;

		boardManager.createDefaultScoreboard(playerID);
		playerWorldTimer.remove(playerID);
	}

	public void loadScoreboards(File fo)
	{

		if (fo == null)
		{
			getLogger().warning("Invalid Scoreboards Directory!");
			return;
		}

		File[] files = fo.listFiles();

		if (files == null || files.length <= 0)
		{
			getLogger().warning("Invalid Scoreboards Directory!");
			return;
		}

		for (File f : files)
		{
			if (f.getName().endsWith(".yml"))
			{
				String perm = f.getName().replace(".yml", "");
				BoardConfig info = new BoardConfig(this, perm, true);
				boardManager.addBoardConfig(perm, info);
				getLogger().info("Loaded '" + f.getName() + "' with permission '" + perm + "'");
			}
			else
			{
				getLogger().warning("File '" + f.getName() + "' is not accepted! Accepted only '.yml' files (YAML)");
			}
		}
	}

	public String listScoreboards()
	{
		File fo = new File(getDataFolder().getAbsolutePath() + "/scoreboards");
		loadScoreboards(fo);

		StringBuilder result = new StringBuilder("§cAll Loaded scoreboards:");

		for (String name : getInfo().keySet())
		{
			result
				.append("§a- '")
				.append(name)
				.append("'§a")
				.append(SEP);
		}

		return result.toString();
	}

	public void copy(InputStream src, File dst) throws IOException
	{
		try
		{
			try (OutputStream out = new FileOutputStream(dst))
			{
				byte[] buf = new byte[1024];
				int len;
				while ((len = src.read(buf)) > 0)
				{
					out.write(buf, 0, len);
				}
			}
		}
		finally
		{
			src.close();
		}
	}

	public boolean isAllowedJoinScoreboard()
	{
		return allowedJoinScoreboard;
	}

	public void setAllowedJoinScoreboard(boolean allowedJoinScoreboard)
	{
		this.allowedJoinScoreboard = allowedJoinScoreboard;
	}

	public boolean isMVdWPlaceholderAPI()
	{
		return MVdWPlaceholderAPI;
	}

	public boolean isPlaceholderAPI()
	{
		return PlaceholderAPI;
	}

	public void setMVdWPlaceholderAPI(boolean MVdWPlaceholderAPI)
	{
		this.MVdWPlaceholderAPI = MVdWPlaceholderAPI;
	}

	public void setPlaceholderAPI(boolean PlaceholderAPI)
	{
		this.PlaceholderAPI = PlaceholderAPI;
	}

	@SuppressWarnings("SameParameterValue")
	protected Optional<BoardConfig> getConfig(
		String boardName,
		boolean readFile
	) {
		BoardConfig config = getInfo().getOrDefault(
			boardName,
			new BoardConfig(this, boardName, readFile)
		);

		if (config != null && config.fileExists())
		{
			getInfo().putIfAbsent(boardName, config);
			return Optional.of(config);
		}
		else
			return Optional.empty();
	}

	public void sendUpdateMessage()
	{
		String updateMessage = messagingManager.getUpdateMessage(getPluginUpdater().getUpdateInfo());
		new BukkitRunnable()
		{
			@Override
			public void run()
			{
			for (Player player : Bukkit.getOnlinePlayers())
			{
				if (player.isOp() || player.hasPermission("quickboardx.update.info"))
				{
					player.sendMessage(updateMessage);
				}
			}
			}
		}.runTaskLater(this, 20);
	}

	public String toggleCustomPlayerBoard(UUID playerID, String boardName)
	{
		return boardManager.toggleCustomPlayerBoard(playerID, boardName);
	}

	public String createNewBoard(String boardName, String worldName)
	{
		return boardManager.createNewBoard(boardName, worldName);
	}

	public String addBoardTitle(String boardName, String title)
	{
		return boardManager.addBoardTitle(boardName, title);
	}

	public String insertBoardTitleLine(
		String boardName,
		int lineNum,
		String text
	) {
		return boardManager.insertBoardTitleLine(boardName, lineNum, text);
	}

	public String removeBoardTitleLine(String boardName, int lineNum)
	{
		return boardManager.removeBoardTitleLine(boardName, lineNum);
	}

	public String addBoardText(String boardName, String text)
	{
		return boardManager.addBoardText(boardName, text);
	}

	public String insertBoardTextLine(
		String boardName,
		int lineNum,
		String text
	) {
		return boardManager.insertBoardTextLine(boardName, lineNum, text);
	}

	public String removeBoardTextLine(String boardName, int lineNum)
	{
		return boardManager.removeBoardTextLine(boardName, lineNum);
	}

	public String selectPlayerBoard(UUID playerID, String boardName)
	{
		return boardManager.selectPlayerBoard(playerID, boardName);
	}

	public String togglePlayerBoard(UUID playerID)
	{
		return boardManager.togglePlayerBoard(playerID);
	}

	public String nextPlayerBoard(UUID playerID)
	{
		return boardManager.nextBoard(playerID);
	}

	public String prevPlayerBoard(UUID playerID)
	{
		return boardManager.previousBoard(playerID);
	}

	public String checkPlayerConfig(UUID playerID)
	{
		return boardManager.checkPlayerConfig(playerID);
	}

	public String enablePlayerBoard(UUID playerID)
	{
		return boardManager.enablePlayerBoard(playerID);
	}

	public String reloadAllPlayerBoards()
	{
		return boardManager.reloadAllPlayerBoards();
	}

	public void addPlayerWorldTimer(UUID playerID)
	{
		playerWorldTimer.put(
			playerID,
			System.currentTimeMillis() + 3000
		);
	}

	public void removePlayerWorldTimer(UUID playerID)
	{
		playerWorldTimer.remove(playerID);
	}

	public void removePlayer(UUID playerID)
	{
		removePlayerWorldTimer(playerID);
		removePlayerBoards(playerID);
	}

	public void removePlayerBoards(UUID playerID)
	{
		boardManager.removePlayerBoard(playerID);
	}

	public void removePlayerBoard(
		UUID playerID,
		PlayerBoard playerBoard
	) {
		boardManager.removePlayerBoard(playerID);
	}

	public void createPlayerBoards(Player player)
	{
		boardManager.createPlayerBoards(player.getUniqueId());
	}

	public void addPlayerBoard(UUID playerID, PlayerBoard playerBoard)
	{
		boardManager.addPlayerBoard(playerID, playerBoard);
	}

	@Override
	public PlayerBoard createBoard(UUID playerID, String boardName)
	{
		return boardManager.createBoard(playerID, boardName);
	}

	@Override
	public PlayerBoard createTemporaryBoard(UUID playerID, String boardName)
	{
		return boardManager.createTemporaryBoard(playerID, boardName);
	}

	@Override
	public PlayerBoard createTemporaryBoard(
		UUID playerID,
		String boardName,
		List<String> title,
		List<String> text, int updateTitle,
		int updateText
	){
		return boardManager.createTemporaryBoard(
			playerID,
			text,
			title,
			updateTitle,
			updateText
		);
	}

	@Override
	public PlayerBoard createBoard(
		UUID playerID,
		List<String> text,
		List<String> title,
		int updateTitle,
		int updateText
	) {
		return boardManager.createBoard(
			playerID,
			text,
			title,
			updateTitle,
			updateText
		);
	}

	@Override
	public List<PlayerBoard> getAllBoards()
	{
		return boardManager.getAllBoards();
	}

	@Override
	public HashMap<UUID, PlayerBoard> getBoards()
	{
		return boardManager.getBoards();
	}

	@Override
	public void removeBoard(UUID playerID)
	{
		boardManager.removeBoard(playerID);
	}

	@Override
	public void updateText(UUID playerID)
	{
		boardManager.updateText(playerID);
	}

	@Override
	public void updateTitle(UUID playerID) {
		boardManager.updateTitle(playerID);
	}

	@Override
	public void updateAll(UUID playerID) {
		boardManager
			.updateTitle(playerID)
			.updateText(playerID);
	}

	public HashMap<String, BoardConfig> getInfo()
	{
		return boardManager.getBoardConfigMap();
	}

	@SuppressWarnings("unused")
	public HashMap<UUID, Long> getPlayerWorldTimer()
	{
		return playerWorldTimer;
	}

	@SuppressWarnings("unused")
	public PluginUpdater getPluginUpdater()
	{
		return pluginUpdater;
	}

	public void disableFirstTimeUse()
	{
		firstTimeUsePlugin = false;
	}
}
