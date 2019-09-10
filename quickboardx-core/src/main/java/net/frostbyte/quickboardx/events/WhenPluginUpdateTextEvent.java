package net.frostbyte.quickboardx.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@SuppressWarnings("unused")
public class WhenPluginUpdateTextEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private UUID playerID;
    private String text;

    public WhenPluginUpdateTextEvent(UUID playerID, String text) {
        this.playerID = playerID;
        this.text = text;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
    public static HandlerList getHandlersList() {
        return handlers;
    }

    public UUID getPlayerID() { return playerID; }
    public void setPlayerID(UUID playerID) { this.playerID = playerID; }

    public String getText() {
        return text;
    }

    public String setText(String text) {
        this.text = text;
        return text;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}

