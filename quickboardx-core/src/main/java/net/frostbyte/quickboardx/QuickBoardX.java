package net.frostbyte.quickboardx;

import com.google.inject.Inject;
import net.frostbyte.quickboardx.api.QuickBoardAPI;
import net.frostbyte.quickboardx.api.Team;
import net.frostbyte.quickboardx.api.TeamFactory;
import net.frostbyte.quickboardx.cmds.BoardCommand;
import net.frostbyte.quickboardx.cmds.CommandSetup;
import net.frostbyte.quickboardx.config.BoardConfig;
import net.frostbyte.quickboardx.config.TeamConfig;
import net.frostbyte.quickboardx.managers.BaseBoardManager;
import net.frostbyte.quickboardx.managers.BaseMessagingManager;
import net.frostbyte.quickboardx.util.VersionManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

import static net.frostbyte.quickboardx.util.StringConstants.ERROR_FAILED;

@SuppressWarnings({"WeakerAccess", "FieldCanBeLocal", "unused"})
public class QuickBoardX extends JavaPlugin implements Listener, QuickBoardAPI
{
	private boolean allowedJoinScoreboard;
	private boolean MVdWPlaceholderAPI, PlaceholderAPI;
	private final HashMap<UUID, Long> playerWorldTimer = new HashMap<>();
	private PluginUpdater pluginUpdater;
	private boolean firstTimeUsePlugin = false;
	private Metrics metrics;
	public static final String SEP = System.lineSeparator();

	@Inject
	private BaseBoardManager boardManager;

	@Inject
	private BaseMessagingManager messagingManager;

	@Inject
	private TeamFactory teamFactory;

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
		loadTeams();
		registerConfig();
		registerMetrics();
		startUpdateTasks();
		registerListeners();
		//TODO: Add Commands for Player Team List: TeamConfig: Team: Add, Remove, Edit; Scoreboard: Enable, Disable, EnableAll, DisableAll
		//TODO: More Player Team List Commands: Include players on Teams when displaying team info.
		//TODO: Schedule Scoreboard updates when Player Tab List Teams change
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

	/**
	 * Register Version Dependencies via Version Bindings and Guice
	 * @return true if the registration completes successfully
	 */
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

	/**
	 * Register QuickBoardX's Event Listeners
	 */
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

	public TeamFactory getTeamFactory() { return teamFactory; }

	/**
	 * Load all available Scoreboard Configurations
	 */
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

	/**
	 * Register/Load QuickBoardX's plugin configuration
	 */
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

	/**
	 * Start the Update Tasks for all Player Scoreboards
	 */
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

	/**
	 * Retrieve the list of Scoreboard names that a player can use.
	 * @param playerID The player's UUID
	 * @return the list of Scoreboard names that a player can use.
	 */
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

	/**
	 * Create the Default Sidebar Scoreboard and the Player Tab List for the given player.
	 * @param playerID The player's UUID
	 */
	public void createDefaultScoreboard(UUID playerID)
	{
		Player player = Bukkit.getPlayer(playerID);
		if (player == null)
			return;

		boardManager.createDefaultScoreboard(playerID);
		boardManager.setPlayerTeam(playerID, "default");
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

	/**
	 * Load the Player Tab List Team Configuration, Installing the default Team Configuration if one doesn't exist.
	 */
	private void loadTeams()
	{
		getLogger().info("Loading Team Configurations...");
		File fo = new File(getDataFolder().getAbsolutePath() + "/teams/");
		if (!fo.exists())
		{
			//noinspection ResultOfMethodCallIgnored
			fo.mkdirs();

			firstTimeUsePlugin = true;

			File scFile = new File(getDataFolder().getAbsolutePath() + "/teams/teams.default.yml");

			try
			{
				//noinspection ResultOfMethodCallIgnored
				scFile.createNewFile();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

			InputStream str = getResource("teams.default.yml");
			try
			{
				if (str == null)
					throw new IOException("Could not load default team settings.");
				copy(str, scFile);
			}
			catch (IOException e1)
			{
				e1.printStackTrace();
			}
		}
		loadTeams(fo);
		getLogger().info("Teams loaded");
	}

	/**
	 * Load All Player Tab List Team Configurations from QuickBoardX's teams directory.
	 * @param fo The file object for the team configuration directory.
	 */
	public void loadTeams(File fo)
	{
		if (fo == null)
		{
			getLogger().warning("Invalid Teams Directory!");
			return;
		}

		File[] files = fo.listFiles();

		if (files == null || files.length <= 0)
		{
			getLogger().warning("Invalid Teams Directory!");
			return;
		}

		for (File f : files)
		{
			if (f.getName().endsWith(".yml"))
			{
				String fileName = f.getName().replace(".yml", "");
				TeamConfig info = new TeamConfig(this, fileName, true);
				boardManager.addTeamConfig(fileName, info);
				getLogger().info("Loaded '" + f.getName() + "' from '" + fileName + "'");
			}
			else
			{
				getLogger().warning("File '" + f.getName() + "' is not accepted! Accepted only '.yml' files (YAML)");
			}
		}
	}

	/**
	 * Add a team to the Player Tab List
	 * @param team The team to add to the list
	 */
	public void registerTabListTeam(Team team)
	{
		boardManager.registerTabListTeam(team);
	}

	/**
	 * Retrieve a list of the names of all currently loaded Scoreboard Configs
	 * @return the list of the names of all currently loaded Scoreboard Configs
	 */
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

	public String setTeamPlaceholders(String s, UUID playerID)
	{
		Player p = Bukkit.getPlayer(playerID);

		if (p != null && s != null && !s.isEmpty())
		{
			if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI") && me.clip.placeholderapi.PlaceholderAPI.containsPlaceholders(s))
				s = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(p, s);
			if (isMVdWPlaceholderAPI())
				s = be.maximvdw.placeholderapi.PlaceholderAPI.replacePlaceholders(p, s);

			s = ChatColor.translateAlternateColorCodes('&', s);
		}
		return s;
	}

	/**
	 * Copy the contents of the Source InputStream to the Destination File
	 * @param src The source input stream
	 * @param dst The destination file
	 * @throws IOException Thrown when opening or writing to the output stream fails.
	 */
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

	/**
	 * Retrieve a Scoreboard Configuration by its name
	 * @param boardName The name of the scoreboard
	 * @param readFile Should the config be loaded from an existing file.
	 * @return An Optional BoardConfig containing a valid config if it was loaded or created.
	 */
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

	/**
	 * Retrieve a Player Tab List Team Configuration by its name
	 * @param teamConfigName The name of the team configuration
	 * @param readFile Should the config be loaded from an existing file.
	 * @return An Optional TeamConfig containing a valid config if it was loaded or created.
	 */
	@SuppressWarnings("SameParameterValue")
	protected Optional<TeamConfig> getTeamConfig(
		String teamConfigName,
		boolean readFile
	) {
		TeamConfig config = getTeamInfo().getOrDefault(
			teamConfigName,
			new TeamConfig(this, teamConfigName, readFile)
		);

		if (config != null && config.fileExists())
		{
			getTeamInfo().putIfAbsent(teamConfigName, config);
			return Optional.of(config);
		}
		else
			return Optional.empty();
	}

	/**
	 * Notify players with the proper permission that an update to QuickBoardX is available.
	 */
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

	/**
	 * Switch the given player's current scoreboard to the specified board, if it exists.
	 * @param playerID The player's UUID
	 * @param boardName The name of the scoreboard that will be displayed
	 * @return A message describing the success or failure of the operation.
	 */
	public String toggleCustomPlayerBoard(UUID playerID, String boardName)
	{
		return boardManager.toggleCustomPlayerBoard(playerID, boardName);
	}

	/**
	 * Create a new Scoreboard, visible when players are in the specified world.
	 * @param boardName The name of the scoreboard that will be displayed
	 * @param worldName The name of the world in which the scoreboard will be visible.
	 * @return A message describing the success or failure of the operation.
	 */
	public String createNewBoard(String boardName, String worldName)
	{
		return boardManager.createNewBoard(boardName, worldName);
	}

	/**
	 * Retrieve Information for a Player Tab List Team
	 * @param teamName The name of the team
	 * @return a Player Tab List Team's Information
	 */
	public String getTeamInformation(String teamName)
	{
		return boardManager.getTeamInformation(teamName);
	}

	/**
	 * Add an entry to the list of values displayed in the Title of a Sidebar Scoreboard
	 * @param boardName The name of the scoreboard
	 * @param title The text entry that will be displayed in the Title.
	 * @return A message containing the results of the operation.
	 */
	public String addBoardTitle(String boardName, String title)
	{
		return boardManager.addBoardTitle(boardName, title);
	}

	/**
	 * Insert an entry into the list of values displayed in the Title of a Sidebar Scoreboard.
	 * @param boardName The name of the scoreboard
	 * @param lineNum The index into the Title list where the entry will be inserted.
	 * @param text The text entry that will be displayed in the Title.
	 * @return A message containing the results of the operation.
	 */
	public String insertBoardTitleLine(
		String boardName,
		int lineNum,
		String text
	) {
		return boardManager.insertBoardTitleLine(boardName, lineNum, text);
	}

	/**
	 * Remove an entry from the list of values displayed in the Title of a Sidebar Scoreboard.
	 * @param boardName The name of the scoreboard
	 * @param lineNum The index of the entry in the Title list that will be removed.
	 * @return A message containing the results of the operation.
	 */
	public String removeBoardTitleLine(String boardName, int lineNum)
	{
		return boardManager.removeBoardTitleLine(boardName, lineNum);
	}

	/**
	 * Add a Line of Text to the Body of a Sidebar Scoreboard
	 * @param boardName The name of the scoreboard
	 * @param text The line of text that will be added.
	 * @return A message containing the results of the operation.
	 */
	public String addBoardText(String boardName, String text)
	{
		return boardManager.addBoardText(boardName, text);
	}

	/**
	 * Insert an entry into the list of values displayed in the Body of a Sidebar Scoreboard.
	 * @param boardName The name of the scoreboard
	 * @param lineNum The index into the Body text list where the entry will be inserted.
	 * @param text The text entry that will be displayed in the Body of the scoreboard.
	 * @return A message containing the results of the operation.
	 */
	public String insertBoardTextLine(
		String boardName,
		int lineNum,
		String text
	) {
		return boardManager.insertBoardTextLine(boardName, lineNum, text);
	}

	/**
	 * Remove an entry from the list of values displayed in the Body of a Sidebar Scoreboard.
	 * @param boardName The name of the scoreboard
	 * @param lineNum The index of the entry in the Body text list that will be removed.
	 * @return A message containing the results of the operation.
	 */
	public String removeBoardTextLine(String boardName, int lineNum)
	{
		return boardManager.removeBoardTextLine(boardName, lineNum);
	}

	/**
	 * Set the given player's scoreboard
	 * @param playerID The Player's UUID
	 * @param boardName The name of the scoreboard that will be displayed
	 * @return A message containing the results of the operation.
	 */
	public String selectPlayerBoard(UUID playerID, String boardName)
	{
		return boardManager.selectPlayerBoard(playerID, boardName);
	}

	/**
	 * Toggle display of the given player's scoreboard on/off
	 * @param playerID The Player's UUID
	 * @return A message containing the results of the operation.
	 */
	public String togglePlayerBoard(UUID playerID)
	{
		return boardManager.togglePlayerBoard(playerID);
	}

	/**
	 * Display the given player's next available scoreboard viewable in their current world
	 * @param playerID The Player's UUID
	 * @return A message containing the results of the operation.
	 */
	public String nextPlayerBoard(UUID playerID)
	{
		return boardManager.nextBoard(playerID);
	}

	/**
	 * Switch the given player's Displayed scoreboard the one before it in the list the player's viewable scoreboards
	 * @param playerID The Player's UUID
	 * @return A message containing the results of the operation.
	 */
	public String prevPlayerBoard(UUID playerID)
	{
		return boardManager.previousBoard(playerID);
	}

	/**
	 * Describe the given player's settings for each of the available Sidebar scoreboards
	 * @param playerID The player's UUID
	 * @return The description of the board settings for the given player, based upon their current world/permissions
	 */
	public String checkPlayerConfig(UUID playerID)
	{
		return boardManager.checkPlayerConfig(playerID);
	}

	/**
	 * Describe the given player's settings for each of the available Player Tab List Teams
	 * @param playerID The player's UUID
	 * @return The description of the Player Tab List Team settings for the given player, based upon their current
	 * scoreboard/permissions
	 */
	public String checkPlayerTeamConfig(UUID playerID)
	{
		return boardManager.checkPlayerTeamConfig(playerID);
	}

	public String enablePlayerBoard(UUID playerID)
	{
		return boardManager.enablePlayerBoard(playerID);
	}

	public String reloadAllPlayerBoards()
	{
		return boardManager.reloadAllPlayerBoards();
	}

	public void reloadPlayerTeams()
	{
		boardManager.reloadPlayerTeams();
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PlayerBoard createBoard(UUID playerID, String boardName)
	{
		return boardManager.createBoard(playerID, boardName);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PlayerBoard createTemporaryBoard(UUID playerID, String boardName)
	{
		return boardManager.createTemporaryBoard(playerID, boardName);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PlayerBoard createTemporaryBoard(
		UUID playerID,
		String boardName,
		List<String> title,
		List<String> text,
		int updateTitle,
		int updateText
	){
		return boardManager.createTemporaryBoard(
			playerID,
			boardName,
			text,
			title,
			updateTitle,
			updateText
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasTeam(String teamName)
	{
		return getTeamInfo().containsKey(teamName);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createTeamConfig(String configName, Team newTeam)
	{
		if (
			configName != null &&
			!configName.isEmpty() &&
			newTeam != null &&
			!hasTeam(newTeam.getName())
		) {
			TeamConfig teamConfig = new TeamConfig(this, configName, false);
			teamConfig.addTeam(newTeam);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addTeamToConfig(String configName, Team team)
	{
		if (
			configName != null &&
			!configName.isEmpty() &&
			team != null
		) {
			TeamConfig teamConfig = getTeamInfo().getOrDefault(configName, new TeamConfig(this, configName, false));
			teamConfig.addTeam(team);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeTeamFromConfig(String configName, String teamName)
	{
		if (
			configName != null &&
			!configName.isEmpty() &&
			teamName != null &&
			!teamName.isEmpty()
		) {
			TeamConfig teamConfig = getTeamInfo().getOrDefault(configName, new TeamConfig(this, configName, false));
			// TODO: Update all player's showing the team in their Player Tab List
			teamConfig.removeTeam(teamName);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removePlayersTeam(UUID playerID)
	{
		boardManager.removePlayerFromTeam(playerID, getPlayersTeam(playerID));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setPlayersTeam(UUID playerID, String teamName)
	{
		boardManager.setPlayerTeam(playerID, teamName);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Team getPlayersTeam(UUID playerID)
	{
		return boardManager.getPlayerTeam(playerID);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void generatePlayersListTeams(UUID playerID)
	{
		boardManager.generateListTeams(playerID);
	}

	/**
	 * {@inheritDoc}
	 */
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<PlayerBoard> getAllBoards()
	{
		return boardManager.getAllBoards();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public HashMap<UUID, PlayerBoard> getBoards()
	{
		return boardManager.getBoards();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeBoard(UUID playerID)
	{
		boardManager.removeBoard(playerID);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateText(UUID playerID)
	{
		boardManager.updateText(playerID);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateTitle(UUID playerID) {
		boardManager.updateTitle(playerID);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateAll(UUID playerID) {
		boardManager
			.updateTitle(playerID)
			.updateText(playerID);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateTeamConfig(String configName, Team team)
	{
		if (configName == null || configName.isEmpty() || team == null)
			return;

		getTeamConfig(configName, false)
		.ifPresent(
			config -> {
				Team currentTeam = config.getTeam(team.getName());
				currentTeam.applyTeam(team);
				boardManager.updateTeams();
			}
		);
	}

	/**
	 * Process a Player Team Update Event; Fired by other plugins
	 * that want to assign a player's team when they login or update
	 * the team dynamically.
	 * @param teamName The name of the player's new team
	 * @param playerId The player's uuid
	 */
	public void onTeamUpdate(String teamName, UUID playerId)
	{
		getLogger().info("onTeamUpdate: " + teamName + "; " + playerId.toString());

		boardManager
			.setPlayerTeam(playerId, teamName)
			.generateListTeams(playerId)
			.updatePlayerListTeam(teamName);
	}

	public String listEnabledWorlds(String boardName)
	{
		return boardManager.listEnabledWorlds(boardName);
	}

	public String listPlayerTablistTeams(String configName)
	{
		TeamConfig config = getTeamInfo().getOrDefault(configName, null);

		if (config != null)
		{
			StringBuilder builder = new StringBuilder();
			for (String teamName : config.getTeamNames())
			{
				builder.append(teamName)
					.append("\n");
			}
			return builder.toString();
		}

		return ERROR_FAILED;
	}

	public void onBoardChange(UUID playerId)
	{

	}

	public List<String> tabListTeamNames()
	{
		return boardManager.getTabListTeamNames();
	}

	public String listEnabledTeamScoreboards(String configName)
	{
		return boardManager.listEnabledTeamScoreboards(configName);
	}

	public String addEnabledWorld(String boardName, String worldName)
	{
		return boardManager.addEnabledWorld(boardName, worldName);
	}

	public String removeEnabledWorld(String boardName, String worldName)
	{
		return boardManager.removeEnabledWorld(boardName, worldName);
	}

	public HashMap<String, BoardConfig> getInfo()
	{
		return boardManager.getBoardConfigMap();
	}

	public HashMap<String, TeamConfig> getTeamInfo()
	{
		return boardManager.getTeamConfigMap();
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
