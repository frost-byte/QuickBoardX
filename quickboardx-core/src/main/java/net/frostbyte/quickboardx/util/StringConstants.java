package net.frostbyte.quickboardx.util;

public class StringConstants
{
	public static final String HEADER = "§7[§6QuickboardX]§7  ";
	public static final String ADDED_EMPTY_LINE = HEADER + "§aAdded empty line!";
	public static final String ADDED_LINE = HEADER + "§aAdded line '%s'!";

	public static final String ERROR_ALREADY_USED = HEADER + "§cThis name is already in use!";
	public static final String ERROR_TEAM_SCOREBOARD_ALREADY_ENABLED = HEADER + "§cThat scoreboard is already enabled.";
	public static final String ERROR_TEAM_SCOREBOARD_NOT_ENABLED = HEADER + "§cThat scoreboard was not enabled.";
	public static final String ERROR_TEAM_SCOREBOARD_NOT_DISABLED = HEADER + "§cThat scoreboard could not be disabled.";
	public static final String ERROR_TEAM_FILE_NOT_CREATED = HEADER + "§cThe team configuration could not be created.";
	public static final String ERROR_TEAM_DOES_NOT_EXIST = HEADER + "§cCould not find that team!";
	public static final String ERROR_WORLD_ALREADY_ENABLED = HEADER + "§cThat world is already enabled.";
	public static final String ERROR_WORLD_NOT_ENABLED = HEADER + "§cThat world was not enabled.";
	public static final String ERROR_WORLD_NOT_DISABLED = HEADER + "§cThat world could not be disabled.";

	public static final String ERROR_CANNOT_CHANGE = HEADER + "§cCould not change the scoreboard!";
	public static final String ERROR_DOES_NOT_EXIST = HEADER + "§cScoreboard does not exist!";
	public static final String ERROR_FAILED = HEADER + "§cOperation failed!";
	public static final String ERROR_FILE_CREATION = HEADER + "§cAn error occurred while creating the file!";
	public static final String ERROR_INVALID_NUMBER = HEADER + "§cThis number is not valid! Use 1, 2, 3, ...";
	public static final String ERROR_NO_PERMISSION = HEADER + "§cYou do not have permission to use that scoreboard!";

	public static final String INSERTED_EMPTY_LINE = HEADER + "§aInserted an empty line!";
	public static final String INSERTED_LINE = HEADER + "§aInserted '%s' into line!";
	public static final String LINE_REMOVED = HEADER + "§aLine removed!";
	public static final String OUT_OF_BOUNDS = HEADER + "§cYou are out of bounds!";
	public static final String SCOREBOARD_CHANGED = "§aScoreboard changed!";
	public static final String TEAM_SCOREBOARD_ENABLED = HEADER + "§aAdded '%s' to the Team's enabled scoreboards!";
	public static final String TEAM_SCOREBOARD_REMOVED = HEADER + "§aRemoved '%s' the Team's enabled scoreboards!";

	public static final String WORLD_ADDED = HEADER + "§aAdded '%s' to enabled worlds!";
	public static final String WORLD_REMOVED = HEADER + "§aRemoved '%s' from enabled worlds!";

	private StringConstants() {}
}
