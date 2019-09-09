package me.tade.quickboard.v1_8_R3.managers;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.tade.quickboard.QuickBoard;
import me.tade.quickboard.managers.BaseMessagingManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

@SuppressWarnings("unused")
@Singleton
public class MessagingManager extends BaseMessagingManager
{
	@Inject
	public MessagingManager(QuickBoard plugin)
	{
		super(plugin);
	}

	@Override
	@EventHandler
	public void onJoin(PlayerJoinEvent event)
	{
		super.onJoin(event);
	}

	@Override
	@EventHandler
	public void onTeleport(PlayerTeleportEvent event)
	{
		super.onTeleport(event);
	}

	@Override
	@EventHandler
	public void onRespawn(PlayerRespawnEvent event)
	{
		super.onRespawn(event);
	}

	@Override
	@EventHandler
	public void onQuit(PlayerQuitEvent event)
	{
		super.onQuit(event);
	}
}
