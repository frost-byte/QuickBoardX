package me.tade.quickboard.cmds;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.tade.quickboard.PlayerBoard;
import me.tade.quickboard.QuickBoard;
import me.tade.quickboard.config.BoardConfig;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings({"unused"})
@Singleton
@CommandAlias("quickboard|qb")
public class MainCommand extends BaseCommand
{
	private final QuickBoard plugin;
	public static final String HEADER = "§7[§6QuickBoard§7] ";
	private static final String ADDED_EMPTY_LINE = HEADER + "§aAdded empty line!";
	private static final String ADDED_LINE = HEADER + "§aAdded line '%s'!";
	private static final String ERROR_ALREADY_USED = HEADER + "§cThis name is already in use!";
	private static final String ERROR_DOES_NOT_EXIST = HEADER + "§cScoreboard doesn't exists!";
	private static final String ERROR_FAILED = HEADER + "§cOperation failed!";
	private static final String ERROR_FILE_CREATION = HEADER + "§cAn error occurred while creating the file!";
	private static final String ERROR_INVALID_NUMBER = HEADER + "§cThis number is not valid! Use 1, 2, 3, ...";
	private static final String ERROR_NO_PERMISSION = HEADER + "§cYou do not have permission to use that scoreboard!";
	private static final String INSERTED_EMPTY_LINE = HEADER + "§aInserted empty line!";
	private static final String INSERTED_LINE = HEADER + "§aInserted '%s' into line!";
	private static final String LINE_REMOVED = HEADER + "§aLine removed!";
	private static final String OUT_OF_BOUNDS = HEADER + "§cYou are out of bounds!";

	@Inject
	public MainCommand(QuickBoard plugin)
	{
		this.plugin = plugin;
	}

	@HelpCommand
	public void doHelp(CommandSender sender, CommandHelp help)
	{
		sender.sendMessage(HEADER + "§7Welcome §6" + sender.getName() + " §7to the QuickBoard commands!");
		sender.sendMessage(
			HEADER + "§7Plugin by §6The_TadeSK§7, version: §6" +
				plugin.getDescription().getVersion()
		);
		help.showHelp();
	}

	@Description("Toggle your scoreboard.")
	@Subcommand("toggle|tog")
	@CommandPermission("quickboard.toggle")
	public void toggle(Player p)
	{
		String text;

		if (plugin.getBoards().containsKey(p))
		{
			plugin.getBoards().get(p).remove();
			text = plugin.getConfig().getString("messages.ontoggle.false");
		}
		else
		{
			text = plugin.getConfig().getString("messages.ontoggle.true");
			plugin.createDefaultScoreboard(p);
		}

		if (text != null)
			p.sendMessage(text.replace("&", "§"));
	}

	@Description("Select the next available scoreboard from the list of accessible boards.")
	@Subcommand("next")
	@CommandPermission("quickboard.toggle")
	public void next(Player p)
	{
		String text;
		if (plugin.getBoards().containsKey(p))
		{
			plugin.nextBoard(p);
			text = HEADER + " switched to " + plugin.getBoards().get(p).getBoardName();
		}
		else
		{
			text = HEADER + " could not select next scoreboard!";
		}

		p.sendMessage(text.replace("&", "§"));
	}

	@Description("Select the previous scoreboard from the list of accessible boards.")
	@Subcommand("prev")
	@CommandPermission("quickboard.toggle")
	public void previous(Player p)
	{
		String text;
		if (plugin.getBoards().containsKey(p))
		{
			plugin.previousBoard(p);
			text = HEADER + " switched to " + plugin.getBoards().get(p).getBoardName();
		}
		else
		{
			text = HEADER + " could not select previous scoreboard!";
		}

		p.sendMessage(text.replace("&", "§"));
	}

	@Description("Enable your scoreboard.")
	@Subcommand("on")
	@CommandPermission("quickboard.toggle")
	public void on(Player p)
	{
		String text = "";

		if (plugin.getBoards().containsKey(p))
		{
			plugin.getBoards().get(p).remove();
			plugin.getBoards().remove(p);
		}

		if (!plugin.getBoards().containsKey(p))
		{
			text = plugin.getConfig().getString("messages.ontoggle.true");

			plugin.createDefaultScoreboard(p);
		}

		if (text != null)
			p.sendMessage(text.replace("&", "§"));
	}

	@Description("Reload the configuration.")
	@Subcommand("reload|rl")
	@CommandPermission("quickboard.reload")
	public void doReload(CommandSender sender)
	{
		sender.sendMessage(HEADER + "§cReloading config file");
		plugin.reloadConfig();
		File fo = new File(plugin.getDataFolder().getAbsolutePath() + "/scoreboards");
		plugin.loadScoreboards(fo);
		sender.sendMessage(HEADER + "§aConfig file reloaded");
		sender.sendMessage(HEADER + "§cCreating scoreboard for all players");
		sender.sendMessage(" ");
		plugin.setAllowedJoinScoreboard(plugin.getConfig().getBoolean("scoreboard.onjoin.use"));

		if (!plugin.isAllowedJoinScoreboard())
		{
			sender.sendMessage(HEADER + "§cCreating scoreboard failed! Creating scoreboard when player join to server is cancelled!");
		}

		plugin.disableFirstTimeUse();

		for (Player p : Bukkit.getOnlinePlayers())
		{
			if (plugin.getBoards().containsKey(p))
			{
				plugin.getBoards().get(p).remove();
				plugin.getBoards().remove(p);
			}
			plugin.createDefaultScoreboard(p);
		}

		sender.sendMessage(HEADER + "§aScoreboard created for all");
	}

	@Description("Check the configuration.")
	@Subcommand("check")
	@CommandPermission("quickboard.check")
	public void checkConfig(Player p)
	{
		String worldName = p.getWorld().getName();
		File fo = new File(plugin.getDataFolder().getAbsolutePath() + "/scoreboards");
		plugin.loadScoreboards(fo);

		p.sendMessage("§cScoreboard and info:");

		for (String s : plugin.getInfo().keySet())
		{
			String output = "Scoreboard='" + s + "'";
			BoardConfig in = plugin.getInfo().get(s);
			output += " in enabled worlds=" +
				(in.getEnabledWorlds() != null && in.getEnabledWorlds().contains(worldName));
			output += " has permission=" + p.hasPermission(s);
			p.sendMessage(output);
		}
	}

	@Description("Toggle a custom scoreboard for a player using its name (permission).")
	@Syntax("<Player> <name>")
	@Subcommand("set|s")
	@CommandCompletion("@players @boards")
	@CommandPermission("quickboard.set")
	public void toggleCustom(
		CommandSender sender,
		@Flags("other") Player player,
		@Values("@boards") String permission
	){
		if (!plugin.getInfo().containsKey(permission))
		{
			sender.sendMessage(ERROR_DOES_NOT_EXIST);
			return;
		}

		if (plugin.getBoards().containsKey(player))
		{
			BoardConfig in = plugin.getInfo().get(permission);
			plugin.getBoards()
				.get(player)
				.createNew(in);
		}
		else
		{
			new PlayerBoard(plugin, player, plugin.getInfo().get(permission));
		}
	}

	@Description("Switch to the selected scoreboard from the list of accessible boards.")
	@Syntax("<name>")
	@Subcommand("select|sl")
	@CommandCompletion("@boards")
	@CommandPermission("quickboard.select")
	public void toggleCustom(
		Player sender,
		@Values("@boards") String permission
	){
		if (!plugin.getInfo().containsKey(permission))
		{
			sender.sendMessage(ERROR_DOES_NOT_EXIST);
			return;
		}

		if (plugin.getBoards().containsKey(sender))
		{
			if (!sender.hasPermission(permission)) {
				sender.sendMessage(ERROR_FAILED);
			}
			BoardConfig in = plugin.getInfo().get(permission);
			plugin.getBoards()
				.get(sender)
				.createNew(in);
		}
		else
		{
			new PlayerBoard(plugin, sender, plugin.getInfo().get(permission));
		}
	}


	@Description("Create a scoreboard with the provided name (permission).")
	@Syntax("<name (permission)>")
	@Subcommand("create|c")
	@CommandCompletion("@nothing")
	@CommandPermission("quickboard.create")
	public void createScoreboard(Player sender, String name)
	{

		AtomicBoolean fileExists = new AtomicBoolean(false);
		getConfig(name, true).ifPresent((c) -> {
			sender.sendMessage(ERROR_ALREADY_USED);
			fileExists.set(true);
		});
		if (fileExists.get())
			return;

		getConfig(name, false).ifPresent((cfg) -> {
			if (cfg.fileExists()) {
				sender.sendMessage(HEADER + "§aFile created! Now edit scoreboard §6/qb edit " + name);
				cfg.setTitle(Arrays.asList("This is the default title!", "You can edit it any time!"));
				cfg.setText(Arrays.asList("This is the default text!", "You can edit it any time!"));
				cfg.setUpdaterText(20);
				cfg.setUpdaterTitle(30);
				cfg.setEnabledWorlds(Collections.singletonList(sender.getWorld().getName()));
			}
			else {
				sender.sendMessage(ERROR_FILE_CREATION);
			}
		});
	}

	@Description("Add a line to a Scoreboard's Title.")
	@Subcommand("addtitle|at")
	@Syntax("<board> <text>")
	@CommandCompletion("@boards @nothing")
	@CommandPermission("quickboard.edit")
	public void addTitle(Player sender, @Values("@boards") String name, @Optional String text)
	{
		AtomicBoolean fileExists = new AtomicBoolean(false);
		getConfig(name, true).ifPresent((cfg) -> {
			if (cfg.fileExists()) {
				fileExists.set(true);
				List<String> lines = cfg.getTitle();

				if (text == null || text.isEmpty())
				{
					sender.sendMessage(ADDED_EMPTY_LINE);
					lines.add(" ");
					cfg.setTitle(lines);
				}
				else
				{
					lines.add(text);
					cfg.setTitle(lines);
					sender.sendMessage(String.format(ADDED_LINE, text));
				}
			}
		});

		if (!fileExists.get()) {
			sender.sendMessage(ERROR_DOES_NOT_EXIST);
		}
	}

	@Description("Remove a line from a Scoreboard's Title.")
	@Subcommand("removetitle|rt")
	@Syntax("<board> <line_number>")
	@CommandCompletion("@boards @range(1-15)")
	@CommandPermission("quickboard.edit")
	public void removeTitle(Player sender, @Values("@boards") String name, int lineNum)
	{
		if (lineNum <= 0)
		{
			sender.sendMessage(ERROR_INVALID_NUMBER);
			return;
		}

		AtomicBoolean fileExists = new AtomicBoolean(false);
		getConfig(name, true).ifPresent((cfg) -> {
			if (cfg.fileExists()) {
				fileExists.set(true);
				List<String> lines = cfg.getTitle();

				if (lines.size() > (lineNum - 1))
				{
					lines.remove((lineNum - 1));
					sender.sendMessage(LINE_REMOVED);
					cfg.setTitle(lines);
				}
				else
				{
					sender.sendMessage(OUT_OF_BOUNDS);
				}
			}
		});

		if (!fileExists.get()) {
			sender.sendMessage(ERROR_DOES_NOT_EXIST);
		}
	}

	@Description("Insert a line in a Scoreboard's Title.")
	@Subcommand("inserttitle|it")
	@Syntax("<board> <line_number> <text>")
	@CommandCompletion("@boards @range(1-15) @nothing")
	@CommandPermission("quickboard.edit")
	public void insertTitle(Player sender, @Values("@boards") String name, int lineNum, @Optional String text)
	{
		if (lineNum <= 0)
		{
			sender.sendMessage(ERROR_INVALID_NUMBER);
			return;
		}

		AtomicBoolean success = new AtomicBoolean(false);
		getConfig(name, true).ifPresent((cfg) -> {
			List<String> lines = cfg.getTitle();

			if (lines.size() <= (lineNum - 1))
			{
				sender.sendMessage(OUT_OF_BOUNDS);
			}
			else
			{
				if (text == null || text.isEmpty())
				{
					lines.add(lineNum - 1, " ");
					cfg.setTitle(lines);
					sender.sendMessage(INSERTED_EMPTY_LINE);
				}
				else
				{
					lines.add(lineNum - 1, text);
					cfg.setTitle(lines);
					sender.sendMessage(String.format(INSERTED_LINE, text));
				}
				success.set(true);
			}
		});
		if (!success.get())
			sender.sendMessage(ERROR_FAILED);
	}

	private java.util.Optional<BoardConfig> getConfig(String boardName, boolean readFile)
	{
		BoardConfig config = plugin.getInfo().getOrDefault(
			boardName,
			new BoardConfig(plugin, boardName, readFile)
		);

		if (config != null && config.fileExists())
		{
			plugin.getInfo().putIfAbsent(boardName, config);
			return java.util.Optional.of(config);
		}
		else
			return java.util.Optional.empty();
	}

	@Description("Add a line to a Scoreboard.")
	@Subcommand("addline|al")
	@Syntax("<board> <text>")
	@CommandCompletion("@boards @nothing")
	@CommandPermission("quickboard.edit")
	public void addLine(Player sender, @Values("@boards") String name, @Optional String text)
	{
		AtomicBoolean fileExists = new AtomicBoolean(false);
		getConfig(name, true).ifPresent((cfg) -> {

			if (cfg.fileExists())
			{
				List<String> lines = cfg.getText();
				fileExists.set(true);

				if (text == null || text.isEmpty())
				{
					sender.sendMessage(ADDED_EMPTY_LINE);
					lines.add(" ");
					cfg.setTitle(lines);
				}
				else
				{
					lines.add(text);
					cfg.setText(lines);
					sender.sendMessage(String.format(ADDED_LINE, text));
				}
			}
		});

		if (!fileExists.get()) {
			sender.sendMessage(ERROR_DOES_NOT_EXIST);
		}
	}

	@Description("Remove a line from a Scoreboard.")
	@Subcommand("removeline|rmln|rl")
	@Syntax("<board> <line_number>")
	@CommandCompletion("@boards @range(1-15)")
	@CommandPermission("quickboard.edit")
	public void removeLine(Player sender, @Values("@boards") String name, int lineNum)
	{
		if (lineNum <= 0)
		{
			sender.sendMessage(ERROR_INVALID_NUMBER);
			return;
		}

		AtomicBoolean fileExists = new AtomicBoolean(false);

		getConfig(name, true).ifPresent((cfg) -> {
			if (cfg.fileExists()) {
				fileExists.set(true);
				List<String> lines = cfg.getText();

				if (lines.size() > (lineNum - 1))
				{
					lines.remove(lineNum - 1);
					sender.sendMessage(LINE_REMOVED);
					cfg.setText(lines);
				}
				else
				{
					sender.sendMessage(OUT_OF_BOUNDS);
				}
			}
		});

		if (!fileExists.get()) {
			sender.sendMessage(ERROR_DOES_NOT_EXIST);
		}
	}


	@Description("Insert a line in a Scoreboard's Text.")
	@Subcommand("insertline|il")
	@Syntax("<board> <line_number> <text>")
	@CommandCompletion("@boards @range(1-15) @nothing")
	@CommandPermission("quickboard.edit")
	public void insertLine(Player sender, @Values("@boards") String name, int lineNum, @Optional String text)
	{
		if (lineNum <= 0)
		{
			sender.sendMessage(ERROR_INVALID_NUMBER);
			return;
		}

		AtomicBoolean success = new AtomicBoolean(false);
		getConfig(name, true).ifPresent((cfg) -> {
			List<String> lines = cfg.getText();

			if (lines.size() <= (lineNum - 1))
			{
				sender.sendMessage(OUT_OF_BOUNDS);
			}
			else
			{
				if (text == null || text.isEmpty())
				{
					lines.add(lineNum - 1, " ");
					cfg.setText(lines);
					sender.sendMessage(INSERTED_EMPTY_LINE);
				}
				else
				{
					lines.add(lineNum - 1, text);
					cfg.setText(lines);
					sender.sendMessage(String.format(INSERTED_LINE, text));
				}
				success.set(true);
			}
		});
		if (!success.get())
			sender.sendMessage(ERROR_FAILED);
	}

	@Description("View all available scoreboards.")
	@Subcommand("list|ls")
	@CommandPermission("quickboard.edit")
	public void listScoreboards(CommandSender sender)
	{
		File fo = new File(plugin.getDataFolder().getAbsolutePath() + "/scoreboards");
		plugin.loadScoreboards(fo);

		sender.sendMessage("§cAll Loaded scoreboards:");

		for (String name : plugin.getInfo().keySet())
		{
			sender.sendMessage("§a- '" + name + "'");
		}
	}
}
