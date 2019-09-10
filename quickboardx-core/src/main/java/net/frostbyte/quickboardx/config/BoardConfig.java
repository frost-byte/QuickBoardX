package net.frostbyte.quickboardx.config;

import net.frostbyte.quickboardx.QuickBoardX;
import net.frostbyte.quickboardx.Scroller;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;


import java.util.*;


@SuppressWarnings({"FieldCanBeLocal", "MismatchedQueryAndUpdateOfCollection", "unused"})
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
	private HashMap<String, Scroller> scrollerText = new HashMap<>();

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

		Bukkit.getScheduler().runTaskTimer(
			plugin,
			() -> {
				if (shouldSave())
				{
					logger.info("Saving Board " + fileName);
					saveConfig();
					//plugin.getInfo().put(fileName, this);
				}
			}, 3 * 20, 3 * 20
		);
	}

	@Override
	public void init()
	{
		if (readFile)
		{
			loadFile();
			config = loadConfig();
			List<String> scrollerNames = getScrollerNames();

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
			file = createFile(filePath, fileName);
			config = loadConfig();
			addDefaults();
		}

		isDirty = false;
	}

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

	public List<String> getTitle() {
		return config.getStringList("title");
	}

	public void setTitle(List<String> title) {
		config.set("title", title);
		isDirty = true;
	}

	public List<String> getText() {
		return config.getStringList("text");
	}

	public void setText(List<String> text) {
		config.set("text", text);
		isDirty = true;
	}

	public int getUpdaterTitle() {
		return config.getInt("updater.title");
	}

	public void setUpdaterTitle(int time) {
		config.set("updater.title", time);
		isDirty = true;
	}

	public int getUpdaterText() {
		return config.getInt("updater.text");
	}

	public void setUpdaterText(int time) {
		config.set("updater.text", time);
		isDirty = true;
	}

	public List<String> getEnabledWorlds() {
		return config.getStringList("enabledWorlds");
	}

	public void setEnabledWorlds(List<String> worlds) {
		config.set("enabledWorlds", worlds);
		isDirty = true;
	}

	public List<String> getChangeableText(String changeName)
	{
		String key = "changeableText." + changeName + ".text";
		return config.getStringList(key);
	}

	public void setChangeableText(String changeName, List<String> healthText) {
		String key = "changeableText." + changeName + ".text";
		config.set(key, healthText);
		isDirty = true;
	}

	public List<String> getChangeables() {
		ConfigurationSection section;
		section = config.getConfigurationSection("changeableText");

		if (section == null)
			return null;

		Set<String> keys = section.getKeys(false);
		return new ArrayList<>(keys);
	}

	public int getChangeableInterval(String changeName)
	{
		String key = "changeableText." + changeName + ".interval";
		return config.getInt(key);
	}

	public String getScrollerText(String scrollerName) {
		String key = "scroller." + scrollerName + ".text";
		return config.getString(key);
	}

	public int getScrollerWidth(String scrollerName)
	{
		String key = "scroller." + scrollerName + ".width";
		return config.getInt(key);
	}

	public int getScrollerSpaceBetween(String scrollerName)
	{
		String key = "scroller." + scrollerName + ".spaceBetween";
		return config.getInt(key);
	}

	public int getScrollerUpdate(String scrollerName) {
		String key = "scroller." + scrollerName + ".update";
		return config.getInt(key);
	}

	public List<String> getScrollerNames() {
		ConfigurationSection section;
		section = config.getConfigurationSection("scroller");

		if (section == null)
			return null;

		Set<String> keys = section.getKeys(false);
		return new ArrayList<>(keys);
	}

	public HashMap<String, Scroller> getScrollers() {
		return scrollerText;
	}

	public Scroller getScroller(String name) {
		if(scrollerText.containsKey(name)) {
			return scrollerText.get(name);
		}
		return null;
	}

	public String getPermission() { return fileName; }
}
