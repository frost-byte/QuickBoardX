package net.frostbyte.quickboardx.config;

import net.frostbyte.quickboardx.QuickBoardX;
import net.frostbyte.quickboardx.api.BaseTeam;
import net.frostbyte.quickboardx.api.Team;
import org.bukkit.Bukkit;

import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal", "MismatchedQueryAndUpdateOfCollection", "unused"})
public class TeamConfig extends ConfigurationHandler
{
	protected QuickBoardX plugin;
	public static final boolean ALLOW_FRIENDLY_FIRE = false;
	public static final boolean SEE_INVISIBLE = false;
	public static final String NAME_TAG_VISIBLE = "visible";
	public static final String COLLISION = "always";
	public static final String COLOR = "white";
	public static final String DEFAULT_DISPLAY_NAME = "";
	public static final String DEFAULT_PREFIX = "";
	public static final String DEFAULT_SUFFIX = "";

	private final HashMap<String, Team> teams = new HashMap<>();

	//TODO: Refactor so that each scoreboard references teams registered in a given TeamConfig/the default? instead of using enabledScoreboards?
	//TODO: Use Namespaces including the config name and team name? i.e "teams.default:default"?
	public TeamConfig(
		QuickBoardX plugin,
		String fileName,
		boolean readFile
	)
	{
		super(plugin.getDataFolder(), plugin.getLogger(), "teams", fileName, readFile);
		logger.info("Loading TeamConfig for " + fileName);
		this.plugin = plugin;

		init();

		Bukkit.getScheduler().runTaskTimer(
			plugin,
			() -> {
				if (shouldSave())
				{
					logger.info("Saving Team " + fileName);
					saveConfig();
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

			if (teams.isEmpty()) {
				for (String s : getTeamNames()) {
					ConfigurationSection section;
					section = config.getConfigurationSection("teams." + s);

					if (section != null) {
						Team team = new BaseTeam(
							plugin,
							s,
							section.getString("displayName"),
							section.getString("color"),
							section.getString("prefix")
						);
						team
							.setAllowFriendlyFire(section.getBoolean("allowFriendlyFire"))
							.setNameTagVisibility(section.getString("nameTagVisible"))
							.setCollision(section.getString("collision"))
							.setSuffix(section.getString("suffix"))
							.setCanSeeFriendlyInvisibles(section.getBoolean("seeInvisible"));
						teams.put(s, team);
						plugin.registerTabListTeam(team);
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

	protected ConfigurationSection createDefaultTeamSection(ConfigurationSection teamsSection, String teamName)
	{
		return teamsSection.createSection(
			teamName,
			new HashMap<String, Object>() {{
				put("displayName", DEFAULT_DISPLAY_NAME);
				put("seeInvisible", SEE_INVISIBLE);
				put("allowFriendlyFire", ALLOW_FRIENDLY_FIRE);
				put("nameTagVisible", NAME_TAG_VISIBLE);
				put("collision", COLLISION);
				put("color", COLOR);
				put("prefix", DEFAULT_PREFIX);
				put("suffix", DEFAULT_SUFFIX);
			}}
		);
	}

	@Override
	public void addDefaults()
	{
		ConfigurationSection teamsSection = config.createSection("teams");
		ConfigurationSection defaultTeamSection = createDefaultTeamSection(teamsSection, "default");
		ConfigurationSection supportersTeamSection = createDefaultTeamSection(teamsSection, "supporters");
		supportersTeamSection.set("color", "blue");
		supportersTeamSection.set("prefix", "[S]");

		config.addDefault("applyToAllScoreboards", true);
		config.addDefault("enabledScoreboards", Collections.singletonList("scoreboard.default"));
		saveConfig(true);
	}

	public boolean applyToAllScoreboards()
	{
		return config.getBoolean("applyToAllScoreboards");
	}

	public void setApplyToAllScoreboards(boolean apply)
	{
		config.set("applyToAllScoreboards", apply);
		isDirty = true;
	}

	public List<String> getEnabledScoreboards() {
		return config.getStringList("enabledScoreboards");
	}

	public void setEnabledScoreboards(List<String> scoreboards) {
		config.set("enabledScoreboards", scoreboards);
		isDirty = true;
	}

	public void enableScoreboard(String scoreboardName)
	{
		List<String> enabledBoards = getEnabledScoreboards();

		if (enabledBoards != null && !enabledBoards.contains(scoreboardName))
		{
			enabledBoards.add(scoreboardName);
			setEnabledScoreboards(enabledBoards);
		}
	}

	public boolean hasTeam(String teamName)
	{
		ConfigurationSection teamsSection = getTeamsSection();

		return teamsSection.getConfigurationSection(teamName) != null;
	}

	private ConfigurationSection getTeamsSection()
	{
		ConfigurationSection teamsSection = config.getConfigurationSection("teams");

		if (teamsSection == null)
			teamsSection = config.createSection("teams");

		return teamsSection;
	}

	public void addTeam(Team newTeam) {
		ConfigurationSection teamsSection = getTeamsSection();

		if (newTeam != null)
		{
			teamsSection.createSection(
				newTeam.getName(),
				new HashMap<String, Object>() {{
					put("displayName", newTeam.getDisplayName());
					put("seeInvisible", newTeam.canSeeFriendlyInvisibles());
					put("allowFriendlyFire", newTeam.allowFriendlyFire());
					put("nameTagVisible", newTeam.getNameTagVisibility());
					put("collision", newTeam.getCollision());
					put("color", newTeam.getColor());
					put("prefix", newTeam.getPrefix());
					put("suffix", newTeam.getSuffix());
				}}
			);
			isDirty = true;
		}
	}

	public void removeTeam(String teamName) {
		ConfigurationSection teamsSection = getTeamsSection();

		if (teamName != null)
		{
			ConfigurationSection teamSection = teamsSection.getConfigurationSection(teamName);
			if (teamSection != null)
				teamsSection.set(teamName, null);

			teams.remove(teamName);
			isDirty = true;
		}
	}

	public List<String> getTeamNames() {
		ConfigurationSection section;
		section = config.getConfigurationSection("teams");

		if (section == null)
			return null;

		Set<String> keys = section.getKeys(false);
		return new ArrayList<>(keys);
	}

	public HashMap<String, Team> getTeams() {
		return teams;
	}

	public Team getTeam(String teamName) {
		return teams.getOrDefault(teamName, null);
	}
}
