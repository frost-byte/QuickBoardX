package net.frostbyte.quickboardx.v1_15_R1.managers;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.frostbyte.quickboardx.QuickBoardX;
import net.frostbyte.quickboardx.events.PlayerTeamUpdateEvent;
import net.frostbyte.quickboardx.managers.BaseMessagingManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

@Singleton
public class MessagingManager extends BaseMessagingManager
{
	@Inject
	public MessagingManager(QuickBoardX plugin)
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
	public void onTeamUpdate(PlayerTeamUpdateEvent event) { super.onTeamUpdate(event); }

	@Override
	@EventHandler
	public void onQuit(PlayerQuitEvent event)
	{
		super.onQuit(event);
	}
}
