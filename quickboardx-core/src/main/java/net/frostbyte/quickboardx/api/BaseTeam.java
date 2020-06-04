package net.frostbyte.quickboardx.api;

import net.frostbyte.quickboardx.QuickBoardX;
import net.frostbyte.quickboardx.config.TeamConfig;
import org.bukkit.ChatColor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("unused")
public class BaseTeam implements Team
{
	private final QuickBoardX plugin;
	private final String name;
	private String displayName;
	private String prefix;
	private String suffix;
	private boolean allowFriendlyFire;
	private String collision;
	private boolean seeInvisible;
	private String color;
	private String nameTagVisible;

	private final Set<String> entries = new HashSet<>();
	private final HashMap<String, String> options = new HashMap<>();

	// If the factory only creates one type, use @Inject
	// if it can create multiple implementations use @AssistedInject
	// http://google.github.io/guice/api-docs/latest/javadoc/com/google/inject/assistedinject/FactoryModuleBuilder.html
	public BaseTeam(
		QuickBoardX plugin,
		String name,
		String displayName,
		String color,
		String prefix
	){
		this.plugin = plugin;
		this.name = name;
		this.displayName = displayName;
		this.color = color;
		this.prefix = prefix;
		this.suffix = TeamConfig.DEFAULT_SUFFIX;
		this.collision = TeamConfig.COLLISION;
		this.nameTagVisible = TeamConfig.NAME_TAG_VISIBLE;
		this.seeInvisible = true;
		this.allowFriendlyFire = TeamConfig.ALLOW_FRIENDLY_FIRE;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public Team setDisplayName(String displayName)
	{
		this.displayName = displayName;
		return this;
	}

	@Override
	public String getDisplayName()
	{
		return displayName;
	}

	@Override
	public Team setPrefix(String prefix)
	{
		this.prefix = prefix;
		return this;
	}

	@Override
	public String getPrefix()
	{
		return prefix;
	}

	@Override
	public Team setSuffix(String suffix)
	{
		this.suffix = suffix;
		return this;
	}

	@Override
	public String getSuffix()
	{
		return suffix;
	}

	@Override
	public Team setColor(String color)
	{
		this.color = color;
		return this;
	}

	@Override
	public String getColor()
	{
		return color;
	}

	@Override
	public Team setCollision(String collision)
	{
		this.collision = collision;
		return this;
	}

	@Override
	public String getCollision()
	{
		return collision;
	}

	@Override
	public Team setNameTagVisibility(String visibility)
	{
		this.nameTagVisible = visibility;
		return this;
	}

	@Override
	public String getNameTagVisibility()
	{
		return nameTagVisible;
	}

	@Override
	public Set<String> getEntries()
	{
		return entries;
	}

	@Override
	public Team addEntry(String entry)
	{
		entries.add(entry);
		return this;
	}

	@Override
	public Team setOptionStatus(String option, String status)
	{
		options.put(option, status);
		return this;
	}

	@Override
	public String getOptionStatus(String option)
	{
		return options.getOrDefault(option, "");
	}

	@Override
	public boolean canSeeFriendlyInvisibles()
	{
		return seeInvisible;
	}

	@Override
	public Team setCanSeeFriendlyInvisibles(boolean canSeeFriendlies)
	{
		seeInvisible = canSeeFriendlies;
		return this;
	}

	@Override
	public boolean allowFriendlyFire()
	{
		return allowFriendlyFire;
	}

	@Override
	public Team setAllowFriendlyFire(boolean allowFriendlyFire)
	{
		this.allowFriendlyFire = allowFriendlyFire;
		return this;
	}

	@Override
	public int getSize()
	{
		return entries.size();
	}

	@Override
	public boolean hasEntry(String entry)
	{
		return entries.contains(entry);
	}

	@Override
	public boolean removeEntry(String entry)
	{
		return entries.remove(entry);
	}

	@Override
	public Team applyPlaceholders(UUID playerID)
	{
		Team teamClone = new BaseTeam(
			plugin,
			plugin.setTeamPlaceholders(name, playerID),
			plugin.setTeamPlaceholders(displayName, playerID),
			plugin.setTeamPlaceholders(color, playerID),
			plugin.setTeamPlaceholders(prefix, playerID)
		);
		teamClone
			.setCanSeeFriendlyInvisibles(seeInvisible)
			.setCollision(collision)
			.setSuffix(plugin.setTeamPlaceholders(suffix, playerID) + ChatColor.RESET)
			.setAllowFriendlyFire(allowFriendlyFire)
			.setNameTagVisibility(nameTagVisible);
		entries.forEach(teamClone::addEntry);
		options.keySet().forEach(option -> teamClone.setOptionStatus(option, options.get(option)));

		return teamClone;
	}

	@Override
	public Team applyTeam(Team team)
	{
		if (team != null)
		{
			displayName = team.getDisplayName();
			seeInvisible = team.canSeeFriendlyInvisibles();
			allowFriendlyFire = team.allowFriendlyFire();
			nameTagVisible = team.getNameTagVisibility();
			collision = team.getCollision();
			color = team.getColor();
			prefix = team.getPrefix();
			suffix = team.getSuffix();
		}
		return this;
	}

	@Override
	public String information()
	{
		return name + " Team Information" + "\n" +
			 "DisplayName: " + displayName + "\n" +
			 "seeInvisible: " + seeInvisible + "\n" +
			 "allowFriendlyFire: " + allowFriendlyFire + "\n" +
			 "NameTag Visibility: " + nameTagVisible + "\n" +
			 "Collision: " + collision + "\n" +
			 "Color: " + color + "\n" +
			 "Prefix: " + prefix + "\n" +
			 "Suffix: " + suffix +"\n";
	}
}
