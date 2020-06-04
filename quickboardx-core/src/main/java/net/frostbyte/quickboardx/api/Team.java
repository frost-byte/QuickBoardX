package net.frostbyte.quickboardx.api;

import java.util.Set;
import java.util.UUID;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface Team
{
	String getName();
	Team setDisplayName(String displayName);
	String getDisplayName();

	Team setPrefix(String prefix);
	String getPrefix();

	Team setSuffix(String suffix);
	String getSuffix();

	Team setColor(String color);
	String getColor();

	Team setCollision(String collision);
	String getCollision();

	Team setNameTagVisibility(String visibility);
	String getNameTagVisibility();

	Set<String> getEntries();
	Team addEntry(String entry);

	boolean hasEntry(String entry);
	boolean removeEntry(String entry);

	Team setOptionStatus(String option, String status);
	String getOptionStatus(String option);

	boolean canSeeFriendlyInvisibles();
	Team setCanSeeFriendlyInvisibles(boolean canSeeFriendlies);

	boolean allowFriendlyFire();
	Team setAllowFriendlyFire(boolean allowFriendlyFire);
	int getSize();

	Team applyPlaceholders(UUID playerID);
	Team applyTeam(Team team);

	String information();
}
