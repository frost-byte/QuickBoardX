package me.tade.quickboard;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.BukkitCommandManager;
import com.google.inject.Injector;
import me.tade.quickboard.api.QuickBoardAPI;
import me.tade.quickboard.config.BoardConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.reflections.Reflections;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static me.tade.quickboard.cmds.MainCommand.HEADER;


@SuppressWarnings({"WeakerAccess", "FieldCanBeLocal", "unused"})
public class QuickBoard extends JavaPlugin implements Listener, QuickBoardAPI
{

	private HashMap<Player, PlayerBoard> boards = new HashMap<>();
	private List<PlayerBoard> allBoards = new ArrayList<>();
	private HashMap<String, BoardConfig> info = new HashMap<>();
	private boolean allowedJoinScoreboard;
	private boolean MVdWPlaceholderAPI, PlaceholderAPI;
	private HashMap<Player, Long> playerWorldTimer = new HashMap<>();
	private PluginUpdater pluginUpdater;
	private boolean firstTimeUsePlugin = false;
	private Metrics metrics;

	/**
	 * Guice Injection
	 */
	private Injector injector;

	/**
	 * ACF CommandManager for Bukkit used for registering in game and console commands, contexts, completions, and
	 * replacements
	 *
	 * @see co.aikar.commands.BukkitCommandManager
	 * @see co.aikar.commands.CommandManager
	 */
	private BukkitCommandManager commandManager;

	/**
	 * The reflections for the plugin.
	 */
	private Reflections pluginReflections;

	@Override
	public void onEnable()
	{
		getLogger().info("------------------------------");
		getLogger().info("          QuickBoard          ");

		Bukkit.getPluginManager().registerEvents(this, this);
		getLogger().info("Listeners registered");

		registerAPI();
		registerDependencies();
		generateReflections();
		registerCommands();
		loadScoreboards();
		registerConfig();
		registerMetrics();
		startUpdateTasks();
	}

	@Override
	public void onDisable()
	{
		for (Player p : Bukkit.getOnlinePlayers())
		{
			p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
		}
	}

	private void registerAPI() {
		getServer().getServicesManager().register(
			QuickBoardAPI.class,
			this,
			this,
			ServicePriority.Normal
		);
		getLogger().info("API registered");
	}

	private void registerDependencies() {
		getLogger().info("Registering Dependencies");
		metrics = new Metrics(this);
		pluginUpdater = new PluginUpdater(this);

		// Initialize the ACF Command Manger
		commandManager = new BukkitCommandManager(this);

		// Initialize Dependency Injection
		PluginBinderModule module = new PluginBinderModule(
			this,
			commandManager,
			getLogger(),
			metrics
		);

		injector = module.createInjector();
		injector.injectMembers(this);
	}

	private void generateReflections() {
		// Create Reflections
		getLogger().info("Performing Reflections...");
		pluginReflections = new Reflections("me.tade.quickboard");
	}

	private void loadScoreboards() {
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
				return info.size();
			}
		});

	}

	private void startUpdateTasks() {
		getLogger().info("Update Tasks started");

		getLogger().info("          QuickBoard          ");
		getLogger().info("------------------------------");

		for (Player p : Bukkit.getOnlinePlayers())
		{
			playerWorldTimer.put(p, System.currentTimeMillis() + 5000);
		}

		new BukkitRunnable()
		{
			@Override
			public void run()
			{
			for (Player player : playerWorldTimer.keySet())
			{
				long time = playerWorldTimer.get(player);

				if (time < System.currentTimeMillis())
					continue;

				if (isAllowedJoinScoreboard())
					createDefaultScoreboard(player);
			}
			}
		}.runTaskTimer(this, 0, 20);
	}

	private List<String> getBoardsForPlayer(Player p) {
		List<String> usableBoards = new ArrayList<>();

		Set<String> perms = info.keySet();
		perms.forEach(s -> {
			Boolean hasPermission = p.hasPermission(s);
			usableBoards.add(s);
		});

		return usableBoards;
	}

	/**
	 * Register Console and Player Commands with ACF
	 */
	private void registerCommands()
	{
		getLogger().info("Registering Commands");
		//noinspection deprecation
		commandManager.enableUnstableAPI("help");

		ACFSetup acfSetup = injector.getInstance(ACFSetup.class);
		acfSetup.registerCommandContexts();
		acfSetup.registerCommandReplacements();
		acfSetup.registerCommandCompletions();

		Set<Class<? extends BaseCommand>> commands = pluginReflections.getSubTypesOf(BaseCommand.class);
		commands.forEach(c -> commandManager.registerCommand(injector.getInstance(c)));
	}

	@SuppressWarnings("unused")
	@EventHandler
	public void onJoin(PlayerJoinEvent e)
	{
		final Player player = e.getPlayer();

		if (firstTimeUsePlugin && (player.isOp() || player.hasPermission("quickboard.creator")))
		{
			new BukkitRunnable()
			{
				@Override
				public void run()
				{
					player.sendMessage(" ");

					player.sendMessage("                        §6§lQuickBoard                        ");
					player.sendMessage("§7Hi, thank you for using §6§lQuickBoard§7! Version: " + getDescription().getVersion());

					if (!metrics.isEnabled())
					{
						player.sendMessage(
							"§7If you want to keep me motivated to create more for free, please enable your bStats! It will help me a lot in future development!");
					}

					player.sendMessage(" ");
					player.sendMessage(
						"§7Plugin just created a new default scoreboard! Your default scoreboard is named §6scoreboard.default §7with permission as the §6name of the scoreboard!");
					player.sendMessage(" ");
					player.sendMessage("§7In QuickBoard files you can see this scoreboard as §6scoreboard.default.yml");
					player.sendMessage(" ");
					player.sendMessage(
						"§7Default scoreboard is being displayed on the right side of your Minecraft client!");
					player.sendMessage(" ");
					for (String s : info.keySet())
					{
						BoardConfig in = info.get(s);

						new PlayerBoard((QuickBoard) metrics.getPlugin(), player, in);
					}

					if (isPlaceholderAPI())
					{
						player.sendMessage(
							"§7To parse Placeholders, please download them using command §6/papi ecloud download <Expansion name>");
						player.sendMessage("§7For example download Player placeholders: §6/papi ecloud download Player");
						player.sendMessage("§7and download Server placeholders: §6/papi ecloud download Server");
						player.sendMessage(" ");
						player.sendMessage("§7After that please restart your server and changes will be made!");
					}
					else
					{
						player.sendMessage(
							"§aIf you want more placeholders, please download PlaceholderAPI from here: §6https://www.spigotmc.org/resources/6245/");
						player.sendMessage(" ");
						player.sendMessage(
							"§aIf you are using some of the §6Maximvdw §aplugins, use MVdWPlaceholders from here: §6https://www.spigotmc.org/resources/11182/");
					}

					player.sendMessage(" ");
					player.sendMessage(
						"§cIf you find an error or a bug, please report it on GitHub @ https://github.com/frost-byte/QuickBoard/issues");
				}
			}.runTaskLater(this, 45);
		}
		else
			playerWorldTimer.put(player, System.currentTimeMillis() + 3000);

		if (!pluginUpdater.needUpdate())
			return;

		if (player.isOp() || player.hasPermission("quickboard.update.info"))
		{
			sendUpdateMessage();
		}
	}

	@SuppressWarnings("unused")
	@EventHandler
	public void onTeleport(PlayerTeleportEvent e)
	{
		final Player p = e.getPlayer();
		String fromWorld = e.getFrom().getWorld().getName();
		String toWorld = e.getTo().getWorld().getName();

		if (!fromWorld.equalsIgnoreCase(toWorld))
		{
			playerWorldTimer.put(p, System.currentTimeMillis() + 3000);
		}
	}

	@SuppressWarnings("unused")
	@EventHandler
	public void onRespawn(PlayerRespawnEvent e)
	{
		final Player p = e.getPlayer();
		playerWorldTimer.put(p, System.currentTimeMillis() + 3000);
	}

	@SuppressWarnings("unused")
	@EventHandler
	public void onQuit(PlayerQuitEvent e)
	{
		playerWorldTimer.remove(e.getPlayer());

		if (boards.containsKey(e.getPlayer()))
		{
			PlayerBoard board = boards.get(e.getPlayer());

			if (board != null)
			{
				board.stopTasks();
				allBoards.remove(board);
				boards.remove(e.getPlayer());
			}
		}
	}

	public void createDefaultScoreboard(Player player)
	{
		getLogger().info("createDefaultScoreboard for " + player.getName());
		for (String s : info.keySet())
		{
			BoardConfig in = info.get(s);
			String playerWorld = player.getWorld().getName();

			if (
				in.getEnabledWorlds() != null &&
				in.getEnabledWorlds().contains(playerWorld)
			) {
				if (player.hasPermission(s))
				{
					if (boards.containsKey(player))
					{
						getLogger().info("createDefaultScoreboard named " + s + " was found for " + player.getName());
						if (boards.get(player).getList().equals(in.getText()))
						{
							player.setScoreboard(boards.get(player).getBoard());
							return;
						}
						boards.get(player)
							.createNew(in);
					}
					else
					{
						getLogger().info("createDefaultScoreboard board generated for player " + player.getName());
						new PlayerBoard(this, player, info.get(s));
					}
					return;
				}
			}
		}
		PlayerBoard board = boards.getOrDefault(player, null);
		if (board != null)
		{
			board.remove();
		}
		getLogger().info("createDefaultScoreboard could not create a board for player " + player.getName());
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
				this.info.put(perm, info);
				getLogger().info("Loaded '" + f.getName() + "' with permission '" + perm + "'");
			}
			else
			{
				getLogger().warning("File '" + f.getName() + "' is not accepted! Accepted only '.yml' files (YAML)");
			}
		}
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

	public void sendUpdateMessage()
	{
		new BukkitRunnable()
		{
			@Override
			public void run()
			{
				for (Player player : Bukkit.getOnlinePlayers())
				{
					if (player.isOp() || player.hasPermission("quickboard.update.info"))
					{
						player.sendMessage(" ");
						player.sendMessage(
							HEADER + "§6A new update has been released! (on §a" + pluginUpdater.getUpdateInfo()[1].trim() + "§6)"
						);
						player.sendMessage(
							HEADER + "§6New version number/your current version §a" +
								pluginUpdater.getUpdateInfo()[0] + "§7/§c" + getDescription().getVersion()
						);
						player.sendMessage(
							HEADER + "§6Download update here: §ahttps://github.com/frost-byte/QuickBoard/releases/"
						);
					}
				}
			}
		}.runTaskLater(this, 20);
	}

	public void nextBoard(Player p) {
		if (getBoards().containsKey(p)) {
			List<String> usableBoards = getBoardsForPlayer(p);
			String currentBoard = getBoards().get(p).getBoardName();

			if(!currentBoard.isEmpty()) {
				int newIndex;
				for (int i = 0; i < usableBoards.size(); i++) {
					String boardName = usableBoards.get(i);
					if (boardName.equals(currentBoard)) {
						newIndex = (i + 1) % usableBoards.size();
						String nextBoardName = usableBoards.get(newIndex);
						BoardConfig in = getInfo().get(nextBoardName);
						getBoards().get(p).createNew(in);
						return;
					}
				}
			}
		}
	}

	public void previousBoard(Player p) {
		if (getBoards().containsKey(p)) {
			List<String> usableBoards = getBoardsForPlayer(p);
			String currentBoard = getBoards().get(p).getBoardName();

			if(!currentBoard.isEmpty()) {
				int newIndex;
				for (int i = usableBoards.size() - 1; i >= 0; i--) {
					String boardName = usableBoards.get(i);
					if (boardName.equals(currentBoard)) {
						newIndex = Math.abs(i - 1) % usableBoards.size();
						String nextBoardName = usableBoards.get(newIndex);
						BoardConfig in = getInfo().get(nextBoardName);
						getBoards().get(p).createNew(in);
						return;
					}
				}
			}
		}
	}

	@Override
	public PlayerBoard createBoard(Player player, String name) {
		getLogger().info("Creating Board " + name + " for " + player.getName());
		if (getInfo().containsKey(name)) {
			return new PlayerBoard(this, player, getInfo().get(name));
		}
		return null;
	}

	@Override
	public PlayerBoard createTemporaryBoard(Player player, String name)
	{
		if (getInfo().containsKey(name)) {
			return new PlayerBoard(this, player, getInfo().get(name), true);
		}
		return null;
	}

	@Override
	public PlayerBoard createTemporaryBoard(
		Player player,
		List<String> text,
		List<String> title,
		int updateTitle,
		int updateText
	){
		return new PlayerBoard(this, player, text, title, updateTitle, updateText, true);
	}

	@Override
	public PlayerBoard createBoard(Player player, List<String> text, List<String> title, int updateTitle, int updateText) {
		return new PlayerBoard(this, player, text, title, updateTitle, updateText);
	}

	@Override
	public List<PlayerBoard> getAllBoards() {
		return allBoards;
	}

	@Override
	public HashMap<Player, PlayerBoard> getBoards() {
		return boards;
	}

	@Override
	public void removeBoard(Player player) {
		boards.remove(player);
	}

	@Override
	public void updateText(Player player) {
		if (boards.containsKey(player)) {
			boards.get(player).updateText();
		}
	}

	@Override
	public void updateTitle(Player player) {
		if (boards.containsKey(player)) {
			boards.get(player).updateTitle();
		}
	}

	@Override
	public void updateAll(Player player) {
		if (boards.containsKey(player)) {
			boards.get(player).updateText();
			boards.get(player).updateTitle();
		}
	}

	public HashMap<String, BoardConfig> getInfo()
	{
		return info;
	}

	@SuppressWarnings("unused")
	public HashMap<Player, Long> getPlayerWorldTimer()
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
