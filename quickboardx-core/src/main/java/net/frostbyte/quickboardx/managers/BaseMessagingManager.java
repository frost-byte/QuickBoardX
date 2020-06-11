package net.frostbyte.quickboardx.managers;

import net.frostbyte.quickboardx.PluginUpdater.UpdateInfo;
import net.frostbyte.quickboardx.QuickBoardX;
import net.frostbyte.quickboardx.events.PlayerTeamUpdateEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

import static net.frostbyte.quickboardx.util.StringConstants.HEADER;

@SuppressWarnings("unused")
public abstract class BaseMessagingManager implements Listener
{
	private static final String METRICS_ENABLED =
		"\n§7Developing and maintaining plugins takes a lot of time and effort!, " +
		"\nPlease enable your bStats, it will help contribute to future development!";

	private static final String PAPI_ENABLED =
		"\n§7To parse Placeholders, please download them using command §6/papi ecloud download <Expansion name>"+
		"\n§7For example download Player placeholders: §6/papi ecloud download Player" +
		"\n§7and download Server placeholders: §6/papi ecloud download Server\n" +
		"\n§7After that please restart your server and the changes will be applied!";

	public static final String PAPI_DISABLED =
		"\n§aWant more placeholders?" +
		"\nDownload PlaceholderAPI from: " +
		"\n§6https://www.spigotmc.org/resources/6245/\n" +
		"\n§aUsing §6Maximvdw §aplugins?" +
		"\nDownload MVdWPlaceholders  §6https://www.spigotmc.org/resources/11182/";

	private static final String INTRO_MESSAGE =
		"                        §6§lQuickBoardX                        " +
		"\n§7Hello! Thank you for using §6§lQuickBoardX§7! Version: <version>" +
		"<metrics_enabled>" +
		"\n\n§7QuickboardX just created a new default scoreboard!" +
		"\nYour default scoreboard is named §6scoreboard.default§6." +
		"\nThe name is also the permission, and players must have the permission to see the scoreboard!" +
		"\n§7The config file for the scoreboard, §6scoreboard.default.yml§6, can be" +
		"\nfound in the plugins/QuickBoardX folder." +
		"\n§7The default scoreboard should be visible on the right side of your screen!" +
		"<papi_enabled>" +
		"<papi_disabled>" +
		"\n§cFound an error or a bug?" +
		"\nPlease report it on GitHub @ https://github.com/frost-byte/QuickBoardX/issues";

	protected final QuickBoardX plugin;
	public BaseMessagingManager(QuickBoardX plugin) { this.plugin = plugin; }

	public String getWelcomeMessage(String playerName)
	{
		return HEADER +
			"§7Welcome §6" +
			playerName +
			" §7to the QuickBoardX commands!" +
			System.lineSeparator() +
			"§7Plugin by §6frost-byte§7, version: §6" +
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
			HEADER + "§6New version/your current version §a" + updateInfo.getVersion() +
			"§7/§c" + plugin.getDescription().getVersion() +
			HEADER + "§6Download update here: §ahttps://github.com/frost-byte/QuickBoardX/releases/";
	}

	public void onTeamUpdate(PlayerTeamUpdateEvent event)
	{
		if (event != null)
		{
			plugin.getLogger().info("BaseMessagingManager: onTeamUpdate...");
			plugin.onTeamUpdate(
				event.getTeamName(),
				event.getPlayerId()
			);
		}
	}

	public void onJoin(PlayerJoinEvent event)
	{
		final Player player = event.getPlayer();

		if (plugin.isFirstTimeUsePlugin() && (player.isOp() || player.hasPermission("quickboardx.creator")))
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

		if (player.isOp() || player.hasPermission("quickboardx.update.info"))
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
