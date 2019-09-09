package me.tade.quickboard.cmds;

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
import me.tade.quickboard.QuickBoard;
import me.tade.quickboard.util.VersionManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static me.tade.quickboard.util.StringConstants.HEADER;

@SuppressWarnings("unused")
@CommandAlias("quickboard|qb")
public class BoardCommand extends BaseCommand
{
	protected final QuickBoard plugin;

	public BoardCommand(QuickBoard plugin) { this.plugin = plugin; }

	@HelpCommand
	public void doHelp(CommandSender sender, CommandHelp help)
	{
		String message = VersionManager.getWelcomeMessage(sender.getName());

		if (message != null && !message.isEmpty())
			sender.sendMessage(message);

		help.showHelp();
	}

	@Description("Toggle your scoreboard.")
	@Subcommand("toggle|tog")
	@CommandPermission("quickboard.toggle")
	public void toggle(Player p)
	{
		String text = plugin.togglePlayerBoard(p.getUniqueId());

		if (text != null)
			p.sendMessage(text);
	}

	@Description("Select the next available scoreboard from the list of accessible boards.")
	@Subcommand("next")
	@CommandPermission("quickboard.toggle")
	public void next(Player p)
	{
		String text = plugin.nextPlayerBoard(p.getUniqueId());

		if (text != null)
			p.sendMessage(text);
	}

	@Description("Select the previous scoreboard from the list of accessible boards.")
	@Subcommand("prev")
	@CommandPermission("quickboard.toggle")
	public void previous(Player p)
	{
		String text = plugin.prevPlayerBoard(p.getUniqueId());

		if (text != null)
			p.sendMessage(text);
	}

	@Description("Enable your scoreboard.")
	@Subcommand("on")
	@CommandPermission("quickboard.toggle")
	public void on(Player p)
	{
		String text = plugin.enablePlayerBoard(p.getUniqueId());

		if (text != null)
			p.sendMessage(text);
	}

	@Description("Reload the configuration.")
	@Subcommand("reload|rl")
	@CommandPermission("quickboard.reload")
	public void doReload(CommandSender sender)
	{
		sender.sendMessage(HEADER + "§cReloading config file");
		String text = plugin.reloadAllPlayerBoards();

		if (text != null && !text.isEmpty())
			sender.sendMessage(text);
	}

	@Description("Check the configuration.")
	@Subcommand("check")
	@CommandPermission("quickboard.check")
	public void checkConfig(Player p)
	{
		String text = plugin.checkPlayerConfig(p.getUniqueId());

		if (text != null && p.isOnline())
			p.sendMessage(text);
	}

	@Description("Switch to a custom scoreboard, for a player, using the board's name (permission).")
	@Syntax("<Player> <name>")
	@Subcommand("set|s")
	@CommandCompletion("@players @boards")
	@CommandPermission("quickboard.set")
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
	@CommandPermission("quickboard.select")
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
	@CommandPermission("quickboard.create")
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
	@CommandPermission("quickboard.edit")
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
	@CommandPermission("quickboard.edit")
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
	@CommandPermission("quickboard.edit")
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
	@CommandPermission("quickboard.edit")
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
	@CommandPermission("quickboard.edit")
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
	@CommandPermission("quickboard.edit")
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

	@Description("View all available scoreboards.")
	@Subcommand("list|ls")
	@CommandPermission("quickboard.edit")
	public void listScoreboards(CommandSender sender)
	{
		String result = plugin.listScoreboards();

		if (result != null && !result.isEmpty())
			sender.sendMessage(result);
	}
}
