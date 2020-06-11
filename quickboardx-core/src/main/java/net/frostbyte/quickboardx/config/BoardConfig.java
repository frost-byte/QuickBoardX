package net.frostbyte.quickboardx.config;

import net.frostbyte.quickboardx.QuickBoardX;
import net.frostbyte.quickboardx.Scroller;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;


import java.util.*;

/**
 * Custom Yaml Configuration that defines the settings for a QuickBoardX Sidebar Scoreboard
 * Includes Timings for Scroller, Changeable and Title Updating as well as their Text contents
 *
 * @author frost-byte
 * @since 1.0.0
 */
@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal", "MismatchedQueryAndUpdateOfCollection", "unused"})
public class BoardConfig extends ConfigurationHandler
{
	protected QuickBoardX plugin;
	private static int TITLE_UPDATE = 2;
	private static int TEXT_UPDATE = 5;
	private static int CHANGE_INTERVAL = 60;
	private static int SCROLLER_WIDTH = 26;
	private static int SCROLLER_SPACE = 6;
	private static int SCROLLER_UPDATE = 1;
	private static char SCROLLER_CHAR = '&';
	private final HashMap<String, Scroller> scrollerText = new HashMap<>();

	public BoardConfig(
		QuickBoardX plugin,
		String fileName,
		boolean readFile
	)
	{
		super(plugin.getDataFolder(), plugin.getLogger(), "scoreboards", fileName, readFile);
		logger.info("Loading BoardConfig for " + fileName);
		this.plugin = plugin;

		init();

		// Repeating task to save changes to the config if it has been updated.
		Bukkit.getScheduler().runTaskTimer(
			plugin,
			() -> {
				if (shouldSave())
				{
					logger.info("Saving Board " + fileName);
					saveConfig();
				}
			}, 3 * 20, 3 * 20
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init()
	{
		// Assuming that the config already exists on disk, load/read the config including the Scrollers if any have
		// been defined.
		if (readFile)
		{
			loadFile();
			config = loadConfig();
			List<String> scrollerNames = getScrollerNames();

			// Read any defined Scrollers into the config
			if (scrollerNames != null && !scrollerNames.isEmpty()) {
				for (String s : getScrollerNames()) {
					ConfigurationSection section;
					section = config.getConfigurationSection("scroller." + s);

					if (section != null) {
						Scroller scroller  = new Scroller(
							section.getString("text"),
							section.getInt("width"),
							section.getInt("spaceBetween"),
							SCROLLER_CHAR
						);
						scrollerText.put(s, scroller);
					}
				}
			}
		}
		else
		{
			// Create a new config and add the defaults
			file = createFile(filePath, fileName);
			config = loadConfig();
			addDefaults();
		}

		isDirty = false;
	}

	/**
	 * Add the Default Scoreboard Properties and Settings to the Board Config
	 */
	@Override
	protected void addDefaults()
	{
		config.addDefault(
			"text",
			Arrays.asList(
				"&aWelcome &e{PLAYER}",
				"",
				"{CH_healthloc}",
				"",
				"&aOnline Players",
				"&7%server_online%",
				"",
				"&aYour latency",
				"&7%player_ping%ms",
				"",
				"&aRunning on &6&lQuickBoard"
			)
		);
		config.addDefault(
			"title",
			Arrays.asList(
				"&f&l> &c&lQuickBoard&f&l <",
				"&f&l> &c&lQuickBoar&f&l <"
			)
		);
		config.addDefault("updater.title", TITLE_UPDATE);
		config.addDefault("updater.text", TEXT_UPDATE);
		config.addDefault(
			"changeableText.healthloc.text",
			Arrays.asList(
				"&aHealth: &c%player_health% &lHP",
				"&e%player_x% %player_y% %player_z%",
				"&aJoined &e%player_first_join_date%"
			)
		);
		config.addDefault(
			"changeableText.healthloc.interval",
			CHANGE_INTERVAL
		);
		config.addDefault(
			"scroller.info.text",
			"&7Welcome &e{PLAYER} &7to the Server! This server is running on &e&lQuickBoard"
		);
		Scroller info = new Scroller(
			"'&7Welcome &e{PLAYER} &7to the Server! This server is running on &e&lQuickBoard'",
			SCROLLER_WIDTH,
			SCROLLER_SPACE,
			SCROLLER_CHAR
		);
		scrollerText.put("info", info);
		config.addDefault("scroller.info.width", SCROLLER_WIDTH);
		config.addDefault("scroller.info.spaceBetween", SCROLLER_SPACE);
		config.addDefault("scroller.info.update", SCROLLER_UPDATE);
		config.addDefault("enabledWorlds", Collections.singletonList("world"));
		saveConfig(true);
	}

	/**
	 * @return Retrieve the list of Strings that represent the Title contents
	 */
	public List<String> getTitle() {
		return config.getStringList("title");
	}

	/**
	 * @param title The new contents for the Title of the Sidebar Scoreboard
	 */
	public void setTitle(List<String> title) {
		config.set("title", title);
		isDirty = true;
	}

	/**
	 * @return Retrieve the list of Strings that represent the Body of the Scoreboard (below the title)
	 */
	public List<String> getText() {
		return config.getStringList("text");
	}

	/**
	 * @param text The new contents for the Body of the Sidebar Scoreboard
	 */
	public void setText(List<String> text) {
		config.set("text", text);
		isDirty = true;
	}

	/**
	 * @return Retrieve the number of ticks between Title Updates
	 */
	public int getUpdaterTitle() {
		return config.getInt("updater.title");
	}

	/**
	 * @param time The number of ticks between Title Updates (cycling each entry in the Title List)
	 */
	public void setUpdaterTitle(int time) {
		config.set("updater.title", time);
		isDirty = true;
	}

	/**
	 * @return Retrieve the number of ticks between Text Updates (Body of the Scoreboard)
	 */
	public int getUpdaterText() {
		return config.getInt("updater.text");
	}

	/**
	 * @param time The number of ticks between Text Updates (Body of the Scoreboard)
	 */
	public void setUpdaterText(int time) {
		config.set("updater.text", time);
		isDirty = true;
	}

	/**
	 * @return Retrieve the list of World names where the Sidebar Scoreboard is active.
	 */
	public List<String> getEnabledWorlds() {
		return config.getStringList("enabledWorlds");
	}

	/**
	 * @param worlds Update/Set the list of world names where the Sidebar Scoreboard is active.
	 */
	public void setEnabledWorlds(List<String> worlds) {
		config.set("enabledWorlds", worlds);
		isDirty = true;
	}

	/**
	 * Retrieve the list of strings that are cycled through for the given changeable text element.
	 * A changeable element can be included within a scroller or text element in the body of the scoreboard.
	 * A changeable element is indicated in the config using a placeholder {CH_changeable_name}
	 * @param changeName The name of the changeable element
	 * @return the list of strings that are cycled through for the given changeable text element
	 */
	public List<String> getChangeableText(String changeName)
	{
		String key = "changeableText." + changeName + ".text";
		return config.getStringList(key);
	}

	/**
	 * Sets the list of changeable text values for the given changeable text.
	 * @param changeableName The name of the changeable text element.
	 * @param changeableValues The list of values displayed for the changeable text element.
	 */
	public void setChangeableText(String changeableName, List<String> changeableValues) {
		String key = "changeableText." + changeableName + ".text";
		config.set(key, changeableValues);
		isDirty = true;
	}

	/**
	 * @return The names of all Changeable Text elements in the configuration
	 */
	public List<String> getChangeables() {
		ConfigurationSection section;
		section = config.getConfigurationSection("changeableText");

		if (section == null)
			return null;

		Set<String> keys = section.getKeys(false);
		return new ArrayList<>(keys);
	}

	/**
	 * Retrieve the display interval for a Changeable Text element.
	 * @param changeName The name of the Changeable Text element
	 * @return The delay in ticks before the next value of the Changeable Text element is displayed.
	 */
	public int getChangeableInterval(String changeName)
	{
		String key = "changeableText." + changeName + ".interval";
		return config.getInt(key);
	}

	/**
	 * Get the value for a Scroller Text element
	 * @param scrollerName The name of the Scroller element
	 * @return The current value displayed by the Scroller element
	 */
	public String getScrollerText(String scrollerName) {
		String key = "scroller." + scrollerName + ".text";
		return config.getString(key);
	}

	/**
	 * Retrieve the Display width of a Scroller Element
	 * @param scrollerName The name of the Scroller Element
	 * @return The element's display width in characters
	 */
	public int getScrollerWidth(String scrollerName)
	{
		String key = "scroller." + scrollerName + ".width";
		return config.getInt(key);
	}

	/**
	 * Retrieve the number of character's displayed after the Scroller Elements Text before wrapping
	 * @param scrollerName The name of the Scroller element
	 * @return The number of characters added after the Scroller element when wrapping its contents
	 */
	public int getScrollerSpaceBetween(String scrollerName)
	{
		String key = "scroller." + scrollerName + ".spaceBetween";
		return config.getInt(key);
	}

	/**
	 * Retrieve the number of ticks between visual updates to the given Scroller element
	 * @param scrollerName The name of the scroller element
	 * @return The ticks between updates of the Scroller Element
	 */
	public int getScrollerUpdate(String scrollerName) {
		String key = "scroller." + scrollerName + ".update";
		return config.getInt(key);
	}

	/**
	 * @return A list of all the registered Scroller elements in the Scoreboard
	 */
	public List<String> getScrollerNames() {
		ConfigurationSection section;
		section = config.getConfigurationSection("scroller");

		if (section == null)
			return null;

		Set<String> keys = section.getKeys(false);
		return new ArrayList<>(keys);
	}

	/**
	 * @return The Map of Scroller Element names and associated Scroller
	 */
	public HashMap<String, Scroller> getScrollers() {
		return scrollerText;
	}

	/**
	 * Retrieve one of the Scoreboard's Scroller Elements by name
	 * @param name The name of the Element
	 * @return The Scroller Element
	 */
	public Scroller getScroller(String name) {
		if(scrollerText.containsKey(name)) {
			return scrollerText.get(name);
		}
		return null;
	}

	/**
	 * @return The permission a user needs to be able to use/see the Scoreboard.
	 */
	public String getPermission() { return fileName; }
}
