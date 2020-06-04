package net.frostbyte.quickboardx.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@SuppressWarnings("unused")
public class PlayerTeamUpdateEvent extends Event implements Cancellable
{
	private String teamName;
	private UUID playerId;
	private boolean cancelled;
	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}
	public static HandlerList getHandlersList() {
		return handlers;
	}

	public PlayerTeamUpdateEvent(
		@NotNull UUID playerId,
		String teamName
	)
	{
		this.teamName = teamName;
		this.playerId = playerId;
	}

	public UUID getPlayerId()
	{
		return playerId;
	}

	public void setPlayerId(UUID playerId)
	{
		this.playerId = playerId;
	}

	public String getTeamName()
	{
		return teamName;
	}

	public void setTeamName(String teamName)
	{
		this.teamName = teamName;
	}

	@Override
	public @NotNull HandlerList getHandlers()
	{
		return handlers;
	}

	@Override
	public boolean isCancelled()
	{
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled)
	{
		this.cancelled = cancelled;
	}
}
