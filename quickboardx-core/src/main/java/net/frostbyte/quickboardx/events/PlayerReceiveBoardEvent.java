package net.frostbyte.quickboardx.events;

import net.frostbyte.quickboardx.PlayerBoard;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("unused")
public class PlayerReceiveBoardEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;

    private UUID playerID;
    private List<String> text;
    private List<String> title;
    private PlayerBoard board;
    private String boardName;

    public PlayerReceiveBoardEvent(
        UUID playerID,
        String boardName,
        List<String> text,
        List<String> title,
        PlayerBoard board
    ) {
        this.playerID = playerID;
        this.text = new ArrayList<>(text);
        this.title = title;
        this.board = board;
        this.boardName = boardName;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public static HandlerList getHandlersList() {
        return handlers;
    }

    public UUID getPlayerID() {
        return playerID;
    }

    public void setPlayerID(UUID playerID)
    {
        this.playerID = playerID;
    }

    public String getBoardName()
    {
        return boardName;
    }

    public void setBoardName(String boardName)
    {
        this.boardName = boardName;
    }

    public List<String> getText() {
        return text;
    }

    public List<String> getTitle() {
        return title;
    }

    public PlayerBoard getBoard() {
        return board;
    }

    public List<String> setText(List<String> text) {
        this.text = text;
        return text;
    }

    public List<String> setTitle(List<String> title) {
        this.title = title;
        return title;
    }

    public PlayerBoard setBoard(PlayerBoard board) {
        this.board = board;
        return board;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

}
