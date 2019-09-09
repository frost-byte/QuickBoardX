package me.tade.quickboard.managers;

import me.tade.quickboard.PluginUpdater.UpdateInfo;
import me.tade.quickboard.QuickBoard;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

import static me.tade.quickboard.util.StringConstants.HEADER;

@SuppressWarnings("unused")
public abstract class BaseMessagingManager implements Listener
{
	private static final String METRICS_ENABLED =
		"\n§7If you want to keep me motivated to create more for free, " +
		"\nplease enable your bStats! It will help me a lot in future development!";

	private static final String PAPI_ENABLED =
		"\n§7To parse Placeholders, please download them using command §6/papi ecloud download <Expansion name>"+
		"\n§7For example download Player placeholders: §6/papi ecloud download Player" +
		"\n§7and download Server placeholders: §6/papi ecloud download Server\n" +
		"\n§7After that please restart your server and changes will be made!";

	public static final String PAPI_DISABLED =
		"\n§aWant more placeholders?" +
		"\nDownload PlaceholderAPI from: " +
		"\n§6https://www.spigotmc.org/resources/6245/\n" +
		"\n§aUsing some of the §6Maximvdw §aplugins?" +
		"\nDownload MVdWPlaceholders  §6https://www.spigotmc.org/resources/11182/";

	private static final String INTRO_MESSAGE =
		"                        §6§lQuickBoard                        " +
		"\n§7Hi, thank you for using §6§lQuickBoard§7! Version: <version>" +
		"<metrics_enabled>" +
		"\n\n§7Quickboard just created a new default scoreboard!" +
		"\nYour default scoreboard is named §6scoreboard.default§6." +
		"\nThe name is also the permission, and must be set to see the scoreboard!" +
		"\n§7The config file for the scoreboard, §6scoreboard.default.yml§6, can be" +
		"\nfound in the plugins/QuickBoard folder." +
		"\n§7The default scoreboard should be visible on the right side of your screen!" +
		"<papi_enabled>" +
		"<papi_disabled>" +
		"\n§cFound an error or a bug?" +
		"\nPlease report it on GitHub @ https://github.com/frost-byte/QuickBoard/issues";

	protected final QuickBoard plugin;
	public BaseMessagingManager(QuickBoard plugin) { this.plugin = plugin; }

	public String getWelcomeMessage(String playerName)
	{
		return HEADER +
			"§7Welcome §6" +
			playerName +
			" §7to the QuickBoard commands!" +
			System.lineSeparator() +
			"§7Plugin by §6The_TadeSK§7, version: §6" +
			plugin.getDescription().getVersion();
	}

	public String togglePlayerBoard(UUID playerID)
	{
		return "";
	}

	public String getIntroMessage(String version, boolean papiEnabled, boolean metricsEnabled)
	{
		String output = INTRO_MESSAGE;
		String metrics = (metricsEnabled) ? METRICS_ENABLED : "";
		String papi = (papiEnabled) ? PAPI_ENABLED : "";
		output = output
			.replace("<version>", version)
			.replace("<metrics_enabled>", metrics)
			.replace("<papi_enabled>", papi);

		return output;
	}

	public String getUpdateMessage(UpdateInfo updateInfo)
	{
		return
			HEADER + "§6A new update has been released! (on §a" + updateInfo.getDateUpdated().trim() + "§6)" +
			HEADER + "§6New version number/your current version §a" + updateInfo.getVersion() +
			"§7/§c" + plugin.getDescription().getVersion() +
			HEADER + "§6Download update here: §ahttps://github.com/frost-byte/QuickBoard/releases/";
	}

	public void onJoin(PlayerJoinEvent event)
	{
		final Player player = event.getPlayer();

		if (plugin.isFirstTimeUsePlugin() && (player.isOp() || player.hasPermission("quickboard.creator")))
		{
			new BukkitRunnable()
			{
				@Override
				public void run()
				{
					player.sendMessage(
						getIntroMessage(
							plugin.getDescription().getVersion(),
							plugin.isPlaceholderAPI(),
							plugin.areMetricsEnabled()
						)
					);
					plugin.createPlayerBoards(player);
				}
			}.runTaskLater(plugin, 20 * 2);
		}
		else
			plugin.addPlayerWorldTimer(player.getUniqueId());

		if (!plugin.updaterNeedsUpdate())
			return;

		if (player.isOp() || player.hasPermission("quickboard.update.info"))
		{
			plugin.sendUpdateMessage();
		}
	}

	public void onTeleport(PlayerTeleportEvent event)
	{
		final Player p = event.getPlayer();
		String fromWorld = event.getFrom().getWorld().getName();
		String toWorld = event.getTo().getWorld().getName();

		if (!fromWorld.equalsIgnoreCase(toWorld))
		{
			plugin.addPlayerWorldTimer(p.getUniqueId());
		}
	}

	public void onRespawn(PlayerRespawnEvent event)
	{
		plugin.addPlayerWorldTimer(
			event.getPlayer().getUniqueId()
		);
	}

	public void onQuit(PlayerQuitEvent event)
	{
		plugin.removePlayer(
			event.getPlayer().getUniqueId()
		);
	}
}
