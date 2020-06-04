package net.frostbyte.quickboardx.cmds;

import co.aikar.commands.BaseCommand;

import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Flags;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import co.aikar.commands.annotation.Values;
import net.frostbyte.quickboardx.QuickBoardX;
import net.frostbyte.quickboardx.api.Team;
import net.frostbyte.quickboardx.util.VersionManager;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

import static net.frostbyte.quickboardx.util.StringConstants.ERROR_TEAM_DOES_NOT_EXIST;
import static net.frostbyte.quickboardx.util.StringConstants.HEADER;

@SuppressWarnings("unused")
@CommandAlias("qbx|qb")
public class BoardCommand extends BaseCommand
{
	protected final QuickBoardX plugin;

	public BoardCommand(QuickBoardX plugin) { this.plugin = plugin; }

	@HelpCommand
	public void doHelp(CommandSender sender, CommandHelp help)
	{
		String message = VersionManager.getWelcomeMessage(sender.getName());

		if (message != null && !message.isEmpty())
			sender.sendMessage(message);

		help.showHelp();
	}

	@Description("Toggle your scoreboard's visibility.")
	@Subcommand("toggle|tog")
	@CommandPermission("quickboardx.toggle")
	public void toggle(Player p)
	{
		String text = plugin.togglePlayerBoard(p.getUniqueId());

		if (text != null)
			p.sendMessage(text);
	}

	@Description("Select the next available scoreboard from the list of accessible boards.")
	@Subcommand("next")
	@CommandPermission("quickboardx.toggle")
	public void next(Player p)
	{
		String text = plugin.nextPlayerBoard(p.getUniqueId());

		if (text != null)
			p.sendMessage(text);
	}

	@Description("Select the previous scoreboard from the list of accessible boards.")
	@Subcommand("prev")
	@CommandPermission("quickboardx.toggle")
	public void previous(Player p)
	{
		String text = plugin.prevPlayerBoard(p.getUniqueId());

		if (text != null)
			p.sendMessage(text);
	}

	@Description("Enable your scoreboard.")
	@Subcommand("on")
	@CommandPermission("quickboardx.toggle")
	public void on(Player p)
	{
		String text = plugin.enablePlayerBoard(p.getUniqueId());

		if (text != null)
			p.sendMessage(text);
	}

	@Description("Reload the configuration.")
	@Subcommand("reload|rl")
	@CommandPermission("quickboardx.reload")
	public void doReload(CommandSender sender)
	{
		sender.sendMessage(HEADER + "§cReloading config file");
		String text = plugin.reloadAllPlayerBoards();

		if (text != null && !text.isEmpty())
			sender.sendMessage(text);
	}

	@Description("Check the configuration.")
	@Subcommand("check")
	@CommandPermission("quickboardx.check")
	public void checkConfig(Player p)
	{
		String text = plugin.checkPlayerConfig(p.getUniqueId());

		if (text != null && p.isOnline())
			p.sendMessage(text);
	}

	@Description("Check the Team configuration.")
	@Subcommand("check_team|t_check")
	@CommandPermission("quickboardx.check")
	public void checkTeamConfig(Player p)
	{
		String text = plugin.checkPlayerTeamConfig(p.getUniqueId());

		if (text != null && p.isOnline())
			p.sendMessage(text);
	}

	@Description("View your Player Tab List Team Information.")
	@Subcommand("show_team|t_show")
	@CommandPermission("quickboardx.check")
	public void showTeamInfo(Player p)
	{
		Team team = plugin.getPlayersTeam(p.getUniqueId());
		if (team != null)
			p.sendMessage(team.information());
		else
			p.sendMessage(ERROR_TEAM_DOES_NOT_EXIST);
	}

	@Description("Sets the visible scoreboard for a player, using the board's name (permission).")
	@Syntax("<Player> <name>")
	@Subcommand("set|s")
	@CommandCompletion("@players @boards")
	@CommandPermission("quickboardx.set")
	public void selectPlayerBoard(
		CommandSender sender,
		@Flags("other") Player player,
		@Values("@boards") String permission
	){
		String text = plugin.toggleCustomPlayerBoard(
			player.getUniqueId(),
			permission
		);

		if (text != null)
			sender.sendMessage(text);
	}

	@Description("Switch to the selected scoreboard from the list of accessible boards.")
	@Syntax("<name>")
	@Subcommand("select|sl")
	@CommandCompletion("@boards")
	@CommandPermission("quickboardx.select")
	public void selectPlayerBoard(
		Player sender,
		@Values("@boards") String permission
	){
		String text = plugin.selectPlayerBoard(sender.getUniqueId(), permission);

		if (text != null)
			sender.sendMessage(text);
	}

	@Description("Create a scoreboard with the provided name (permission).")
	@Syntax("<name (permission)>")
	@Subcommand("create|c")
	@CommandCompletion("@nothing")
	@CommandPermission("quickboardx.create")
	public void createScoreboard(Player sender, String boardName)
	{
		String result = plugin.createNewBoard(
			boardName,
			sender.getWorld().getName()
		);

		if (result != null)
			sender.sendMessage(result);
	}

	@Description("Add a line to a Scoreboard's Title.")
	@Subcommand("addtitle|at")
	@Syntax("<board> <text>")
	@CommandCompletion("@boards @nothing")
	@CommandPermission("quickboardx.edit")
	public void addTitle(
		Player sender,
		@Values("@boards") String boardName,
		@Optional String title
	) {
		String result = plugin.addBoardTitle(boardName, title);

		if (result != null)
			sender.sendMessage(result);
	}

	@Description("Remove a line from a Scoreboard's Title.")
	@Subcommand("removetitle|rt")
	@Syntax("<board> <line_number>")
	@CommandCompletion("@boards @range(1-15)")
	@CommandPermission("quickboardx.edit")
	public void removeTitle(
		Player sender,
		@Values("@boards") String boardName,
		int lineNum
	) {
		String result = plugin.removeBoardTitleLine(boardName, lineNum);

		if (result != null)
			sender.sendMessage(result);
	}

	@Description("Insert a line in a Scoreboard's Title.")
	@Subcommand("inserttitle|it")
	@Syntax("<board> <line_number> <text>")
	@CommandCompletion("@boards @range(1-15) @nothing")
	@CommandPermission("quickboardx.edit")
	public void insertTitle(
		Player sender,
		@Values("@boards") String boardName,
		int lineNum,
		@Optional String text
	) {
		String result = plugin.insertBoardTitleLine(
			boardName,
			lineNum,
			text
		);

		if (result != null && !result.isEmpty())
			sender.sendMessage(result);
	}

	@Description("Add a line to the Text of a Scoreboard.")
	@Subcommand("addline|al")
	@Syntax("<board> <text>")
	@CommandCompletion("@boards @nothing")
	@CommandPermission("quickboardx.edit")
	public void addLine(
		Player sender,
		@Values("@boards") String boardName,
		@Optional String text
	) {
		String result = plugin.addBoardText(
			boardName,
			text
		);

		if (result != null && !result.isEmpty())
			sender.sendMessage(result);
	}

	@Description("Remove a line from a Scoreboard's Text.")
	@Subcommand("removeline|rmln|rl")
	@Syntax("<board> <line_number>")
	@CommandCompletion("@boards @range(1-15)")
	@CommandPermission("quickboardx.edit")
	public void removeLine(
		Player sender,
		@Values("@boards") String boardName,
		int lineNum
	) {
		String result = plugin.removeBoardTextLine(
			boardName,
			lineNum
		);

		if (result != null && !result.isEmpty())
			sender.sendMessage(result);
	}

	@Description("Insert a line in a Scoreboard's Text.")
	@Subcommand("insertline|il")
	@Syntax("<board> <line_number> <text>")
	@CommandCompletion("@boards @range(1-15) @nothing")
	@CommandPermission("quickboardx.edit")
	public void insertLine(
		Player sender,
		@Values("@boards") String boardName,
		int lineNum,
		@Optional String text
	) {
		String result = plugin.insertBoardTextLine(
			boardName,
			lineNum,
			text
		);

		if (result != null && !result.isEmpty())
			sender.sendMessage(result);
	}

	@Description("View a list of all available scoreboards.")
	@Subcommand("list|ls")
	@CommandPermission("quickboardx.info")
	public void listScoreboards(CommandSender sender)
	{
		String result = plugin.listScoreboards();

		if (result != null && !result.isEmpty())
			sender.sendMessage(result);
	}

	@Description("List the Enabled Worlds for a Scoreboard.")
	@Subcommand("listworlds|lsw")
	@Syntax("<board>")
	@CommandCompletion("@boards")
	@CommandPermission("quickboardx.info")
	public void listWorlds(
		Player sender,
		@Values("@boards") String boardName
	) {
		String result = plugin.listEnabledWorlds(boardName);

		if (result != null && !result.isEmpty())
			sender.sendMessage(result);
	}

	@Description("List the Names of all Player Tab List Teams.")
	@Subcommand("list_teams|lst")
	@CommandPermission("quickboardx.info")
	public void listTabTeams(CommandSender sender)
	{
		List<String> teamNames = plugin.tabListTeamNames();
		StringBuilder builder = new StringBuilder("Tab List Teams");
		builder.append("\n++++++++++++++++\n");

		for (String teamName : teamNames)
		{
			builder.append(teamName)
				.append("\n");
		}
		sender.sendMessage(builder.toString());
	}

	@Description("Add a world to the enabled worlds for a Scoreboard.")
	@Subcommand("addworld|aw")
	@Syntax("<board> <world>")
	@CommandCompletion("@boards @worlds")
	@CommandPermission("quickboardx.edit")
	public void addWorld(
		Player sender,
		@Values("@boards") String boardName,
		@Values("@worlds") World world
	) {
		String result = plugin.addEnabledWorld(boardName, world.getName());

		if (result != null && !result.isEmpty())
			sender.sendMessage(result);
	}

	@Description("Remove a world from the enabled worlds for a Scoreboard.")
	@Subcommand("remworld|rw")
	@Syntax("<board> <world>")
	@CommandCompletion("@boards @worlds")
	@CommandPermission("quickboardx.edit")
	public void removeWorld(
		Player sender,
		@Values("@boards") String boardName,
		@Values("@worlds") World world
	) {
		String result = plugin.removeEnabledWorld(boardName, world.getName());

		if (result != null && !result.isEmpty())
			sender.sendMessage(result);
	}
}
