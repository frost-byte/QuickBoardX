package net.frostbyte.quickboardx;

import net.frostbyte.quickboardx.config.BoardConfig;
import net.frostbyte.quickboardx.events.PlayerReceiveBoardEvent;
import net.frostbyte.quickboardx.managers.BaseBoardManager;
import net.frostbyte.quickboardx.managers.BaseBoardManager.BoardOperation;
import net.frostbyte.quickboardx.managers.BaseBoardManager.BoardTask;
import net.frostbyte.quickboardx.util.VersionManager;
import org.bukkit.Bukkit;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import static net.frostbyte.quickboardx.managers.BaseBoardManager.BoardOperation.*;

@SuppressWarnings("unused")
public class PlayerBoard {

    private final QuickBoardX plugin;
    private final Logger logger;
    private final BaseBoardManager boardManager;
    @SuppressWarnings("WeakerAccess")
    public static final int NUM_LINES = 16;
    /**
     * The Player Unique ID (board viewer)
     */
    private final UUID playerID;

    /**
     * The board that was displayed before a temporary board is displayed,
     */
    private PlayerBoard currentBoard;
    private String currentBoardName;

    /**
     * Changeable Text Elements and the their currently displayed
     * line of text.
     * The currently displayed value is updated on a regular basis.
     */
    private final HashMap<String, String> changeText = new HashMap<>();

	/**
	 * Currently selected index from the list of elements for
	 * a changeable text element.
	 */
	private final HashMap<String, Integer> changeIndices = new HashMap<>();

    /**
     * List of Title values cycled through on a periodic basis.
     */
    private List<String> titleList = new ArrayList<>(NUM_LINES);

    /**
     * The index of the currently displayed element from the titleList.
     * (The top line of the board)
     */
    private int titleIndex = 0;

    /**
     * The lines of text content for the scoreboard.
     * Can reference changeable or scrollable elements,
     * which will be inserted into each line where they are referenced.
     *
     * Each Line is associated with a Team in the scoreboard.
     * The Team's Prefix and Suffix can be used to allow for a total of
     * 32 characters per line; this is especially useful for versions of
     * MC prior to 1.13.
     *
     * This requires carrying any color codes from the prefix to the suffix,
     * otherwise the color will appear to change midway in the displayed text.
     */
    private List<String> textLines = new ArrayList<>();

    /**
     * The values for each of the boards defined scroll elements.
     * When a scroller is referenced in a line of the boards text content
     * the configured value is substituted and displayed on the board.
     */
    private HashMap<String, String> scrollerText = new HashMap<>();

    /**
     * The number of ticks a temporary board will be displayed
     * before reverting to the previously displayed board.
     */
    @SuppressWarnings("WeakerAccess")
    public static final int BOARD_TEMP_LIFETIME = 20 * 20;

    /**
     * The number of ticks between title updates
     */
    private int updateTitleTicks;

    /**
     * The number of ticks between changes in the primary
     * text content of the board.
     */
    private int updateTextTicks;

    /**
     * Should the Text elements in the scoreboard
     * be updated?
     */
    private boolean preventChange = false;

    /**
     * Specifies the configured title, text, changeable text and scrolling
     * elements for a board type.
     */
    private BoardConfig boardConfig = null;

    /**
     * Is this board only displayed for a short period before reverting
     * to the previously displayed 'permanent' board.
     */
    private boolean isTemporary;

    /**
     * The display name for the scoreboard.
     */
    private String displayName;

    public PlayerBoard(
        QuickBoardX plugin,
        UUID playerID,
        BoardConfig boardConfig
    ) {
        this(plugin, playerID, boardConfig, false);
    }

    public PlayerBoard(
        QuickBoardX plugin,
        UUID playerID,
        BoardConfig boardConfig,
        boolean isTemporary
    ){
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.playerID = playerID;
        this.isTemporary = isTemporary;
        this.boardManager = VersionManager.getBoardManager();
        updateTitleTicks = boardConfig.getUpdaterTitle();
        updateTextTicks = boardConfig.getUpdaterText();
        this.textLines = boardConfig.getText();
        this.titleList = boardConfig.getTitle();
        this.displayName = boardConfig.getTitle().get(0);

        if(isTemporary)
        {
           currentBoard = boardManager.getPlayerBoard(playerID);

            if (currentBoard != null)
                currentBoardName = currentBoard.boardConfig.getPermission();
        }

        // Set the currently displayed value for each of the board's
        // defined changeable elements to their first entries
        List<String> changeKeys = boardConfig.getChangeables();
        if (changeKeys != null)
        {
            for (String changeableID : changeKeys)
            {
                changeText.put(
                    changeableID,
                    boardConfig.getChangeableText(changeableID).get(0)
                );
            }
        }

        this.boardConfig = boardConfig;

        PlayerReceiveBoardEvent event =
            new PlayerReceiveBoardEvent(
                playerID,
                boardConfig.getPermission(),
                boardConfig.getText(),
                boardConfig.getTitle(),
                this
            );

        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            startSetup(event);
        }
    }

    public PlayerBoard(
        QuickBoardX plugin,
        UUID playerID,
        List<String> text,
        List<String> titleLines,
        int updateTitleTicks,
        int updateTextTicks
    ) {
        this(
            plugin,
            playerID,
            titleLines.get(0),
            text,
            titleLines,
            updateTitleTicks,
            updateTextTicks,
            false
        );
    }

    public PlayerBoard(
        QuickBoardX plugin,
        UUID playerID,
        String boardName,
        List<String> text,
        List<String> titleLines,
        int updateTitleTicks,
        int updateTextTicks,
        boolean isTemporary
    ) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.boardManager = VersionManager.getBoardManager();
        this.playerID = playerID;
        this.updateTitleTicks = updateTitleTicks;
        this.updateTextTicks = updateTextTicks;
        this.isTemporary = isTemporary;

        if(isTemporary)
        {
            this.displayName = boardName;

            if (boardManager != null)
            {
                currentBoard = boardManager.getPlayerBoard(playerID);

                if (currentBoard != null && currentBoard.boardConfig != null)
                    currentBoardName = currentBoard.boardConfig.getPermission();
                boardManager.generateListTeams(playerID);
            }
        }

        PlayerReceiveBoardEvent event =
        new PlayerReceiveBoardEvent(
            playerID,
            boardName,
            text,
            titleLines,
            this
        );

        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled())
            startSetup(event);
    }

    /**
     * Retrieve the Scoreboard's display name or title.
     * @return The display name.
     */
    public String getDisplayName()
    {
        return displayName;
    }

    /**
     * Retrieve the values displayed for all of the lines of text in the body of the scoreboard.
     * @return The values displayed within the body of the scoreboard
     */
    public List<String> getTextLines()
    {
        return textLines;
    }

    /**
     * Assign the list of lines displayed in the boady of the scoreboard.
     * @param textLines The list of values for each line of the scoreboard.
     */
    public void setTextLines(List<String> textLines)
    {
        this.textLines = textLines;
    }

    /**
     * Assign a value to one of the lines displayed in the body of the scoreboard
     * @param lineIndex The line number
     * @param line The text to assign to the given line number
     */
    public void setLine(int lineIndex, String line)
    {
        if (lineIndex > 0 && lineIndex < textLines.size())
            textLines.set(lineIndex, line);
    }

    /**
     * Retrieve the value displayed for one of the lines of text in the body of the scoreboard.
     * @param lineIndex An index corresponding to a line of text
     * @return The text value for the given line number
     */
    public String getLine(int lineIndex)
    {
        if (lineIndex > 0 && lineIndex < textLines.size())
            return textLines.get(lineIndex);
        return null;
    }

    /**
     * Retrieve a map of the currently displayed text values for the board's scrolling text elements.
     * @return The map of scrollable text element names to values
     */
    public HashMap<String, String> getScrollerText()
    {
        return scrollerText;
    }

    /**
     * Assign the map of values for the Scoreboard's scrollable text elements
     * @param scrollerText Map of scrollable elements and the values they should display
     */
    public void setScrollerText(HashMap<String, String> scrollerText)
    {
        this.scrollerText = scrollerText;
    }

    public UUID getPlayerID() { return playerID; }
    public BoardConfig getBoardConfig() { return boardConfig; }
    public boolean canUpdate() { return !preventChange; }

    public boolean isTemporary()
    {
        return isTemporary;
    }

    /**
     * Assign the title and text values for the board and create the displayed scoreboard.
     * Also begins the tasks for updating the changeable elements, scrolling elements and currently
     * displayed title.
     *
     * @param event An event containing the configured values for the board's title, changeable
     *              and scrollable text elements.
     */
    private void startSetup(final PlayerReceiveBoardEvent event)
    {
        UUID playerID = event.getPlayerID();

        titleIndex = event.getTitle().size();
        titleList = event.getTitle();
        textLines = event.getText();
        boardManager.addPlayerBoard(playerID, this);

        buildScoreboard(event);
        setUpText();
        startUpdates();
    }

    /**
     * Retrieve the map of changeable text elements and the currently displayed value
     * for each.
     * @return The changeable text element map
     */
    public HashMap<String, String> getChangeText()
    {
        return changeText;
    }

    /**
     * Retrieve the index of the currently displayed title's value.
     * @return The title value index
     */
    public int getTitleIndex()
    {
        return titleIndex;
    }

    /**
     * Retrieve the list of values that will be cycled through, updating the
     * scoreboard's currently displayed title.
     * @return The list of title values
     */
    public List<String> getTitleList()
    {
        return titleList;
    }

    /**
     * Create the scoreboard used to display this board's title and text.
     * @param event An event describing the board configuration
     */
    private void buildScoreboard(PlayerReceiveBoardEvent event)
    {
        boardManager.createScoreboard(this);

        if (event.getTitle().size() == 0)
            event.getTitle().add(" ");
    }

    /**
     * Retrieve the configuration/permission name for this board
     * @return The board's configuration/permission
     */
    public String getBoardName()
    {
        if (isTemporary)
            return "temporary";

        if (boardConfig != null)
            return boardConfig.getPermission();

        return null;
    }

    /**
     * Initialize the display properties for the scoreboard
     */
    private void setUpText()
    {
        boardManager.setUpText(this);
    }

    /**
     * Remove the currently displayed temporary scoreboard
     */
    public void removeTemporary()
    {
        if (isTemporary && currentBoardName != null)
        {
            BoardConfig config = boardManager.getBoardConfig(currentBoardName);
            if (config != null)
            {
                logger.info(
                    "Restoring Board " + currentBoardName + " for " +
                    boardManager.getPlayerName(playerID)
                );
                isTemporary = false;
                changeBoard(config);
            }
        }
    }

    public String getDisplayedTitle() {
        return titleList.get(titleIndex);
    }

    /**
     * Reset the player's scoreboard to the default.
     */
    public void remove()
    {
        boardManager.resetPlayerScoreboard(playerID);
    }

    /**
     * Start a repeating task to update the board's currently displayed text.
     */
    private void startTitleUpdate()
    {
        BoardOperation operation = UPDATE_TITLE;
        operation.setFrequency(updateTitleTicks)
            .setTotalElements(titleList.size());
        BoardTask task = new BoardTask(this, operation);

        boardManager.startTask(task);
    }

    /**
     * Update the board's currently displayed text using a specific item from the list
     * of possible values.
     * @param index The index for the title text to display
     */
    public void updateTitle(int index)
    {
        if (titleList != null && !titleList.isEmpty())
        {
            if (index < 0 || index >= titleList.size())
                index = 0;
            String newTitle = titleList.get(index);
            newTitle = boardManager.setPlaceholders(newTitle, playerID);
            boardManager.setTitle(this, newTitle);
        }
    }

    /**
     * Update the currently displayed title value.
     */
    public void updateTitle()
    {
        if (canUpdate() && titleList != null && !titleList.isEmpty())
        {
            titleIndex = (titleIndex + 1) % titleList.size();
            String newTitle = titleList.get(titleIndex);
            newTitle = boardManager.setPlaceholders(newTitle, playerID);
            boardManager.setTitle(this, newTitle);
        }
        else
            titleIndex = 0;
    }

    /**
     * Start a runnable task that will change the displayed board back to the current one.
     * After the configured number of ticks.
     */
    private void startTempDisplay()
    {
        BoardOperation operation = BoardOperation.REMOVE_TEMPORARY;
        operation.setLifetime(BOARD_TEMP_LIFETIME);
        BoardTask task = new BoardTask(this, operation);

        boardManager.startTask(task);
    }

    /**
     * Start the runnable that updates the board's text elements; does not include the title.
     */
    private void startTextUpdate()
    {
        BoardOperation operation = UPDATE_TEXT;
        operation.setFrequency(updateTextTicks)
            .setTotalElements(textLines.size());
        BoardTask task = new BoardTask(this, operation);

        boardManager.startTask(task);
    }

    /**
     * Update the text displayed in one of the board's scrolling elements
     * @param scrollKey A unique key that identifies a particular scrolling element.
     */
    public void updateScrollerText(String scrollKey)
    {
        Scroller text = boardConfig.getScroller(scrollKey);
        text.setupText(
            boardManager.setPlaceholders(text.getText(), playerID),
            '&'
        );
        scrollerText.put(scrollKey, text.next());
        updateText();
    }

    /**
     * Update all the text elements for the board.
     * (Does not update the title)
     */
    private void updateText()
    {
        boardManager.updateText(this);
    }

    /**
     * Initialize a Runnable for updating one of the board's changeable text elements.
     * @param changeableKey A unique key that identifies a particular changeable element.
     */
    private void startChangeableUpdate(String changeableKey)
    {
        int interval = boardConfig.getChangeableInterval(changeableKey);
        int size = boardConfig.getChangeableText(changeableKey).size();

        BoardOperation operation = UPDATE_CHANGEABLE;
        operation.setFrequency(interval)
            .setTotalElements(size)
            .setKey(changeableKey);
        BoardTask task = new BoardTask(this, operation);

        boardManager.startTask(task);
    }

    /**
     * Update the currently used value for one of the board's changeable text elements.
     *
     * @param changeableKey A unique key that identifies a particular changeable element.
     * @param index The index into the list of possible text values for the element.
     */
    public void updateChangeable(String changeableKey, int index)
    {
        List<String> changeLines = boardConfig.getChangeableText(changeableKey);

        if (index < 0 || index >= changeLines.size())
            index = 0;

        changeText.put(changeableKey, changeLines.get(index));
        updateText();
    }

    /**
     * Initialize a Runnable for updating one of the board's scrolling text elements.
     * @param scrollKey A unique key that identifies a particular scrolling element.
     */
    private void startScrollerUpdate(String scrollKey)
    {
        int interval = boardConfig.getScrollerUpdate(scrollKey);

        BoardOperation operation = UPDATE_SCROLLER;
        operation.setFrequency(interval)
            .setKey(scrollKey);

        BoardTask task = new BoardTask(this, operation);

        boardManager.startTask(task);
    }

    /**
     * Intialize Tasks/Runnables for updating the board's title and text.
     * Also starts runnables for updating any defined changeable or scroller elements displayed
     * within the board's lines of text.
     */
    private void startUpdates()
    {
        // Temporary Boards should not be displayed long enough for changeables or
        // scrolling elements to be useful. The title should not be dynamic either.
        if (isTemporary)
        {
            startTempDisplay();
        }
        else
        {
            startTitleUpdate();
            startTextUpdate();
        }

        if (boardConfig != null)
        {
            if (boardConfig.getChangeables() != null)
            {
                for (final String changeKey : boardConfig.getChangeables())
                    startChangeableUpdate(changeKey);
            }

            if (boardConfig.getScrollerNames() != null)
            {
                for (final String scrollKey : boardConfig.getScrollerNames())
                    startScrollerUpdate(scrollKey);
            }
        }
    }

    /**
     * Change the Board Configuration used by the Player Board.
     * Update all Board Elements - Title, Text, Tasks, Changeables and
     * scrollers.
     *
     * @param info The new board config to use when displaying the scoreboard.
     */
    private void changeBoard(BoardConfig info)
    {
        preventChange = true;
        boardManager.stopTasks(playerID);
        this.boardConfig = info;
        this.displayName = info.getTitle().get(0);
        this.updateTextTicks = info.getUpdaterText();
        this.updateTitleTicks = info.getUpdaterTitle();
        this.titleList = info.getTitle();
        this.textLines = new ArrayList<>(info.getText());

        titleIndex = info.getTitle().size();

        if (this.textLines.size() <= 0)
            this.titleList.add(" ");

        List<String> changeKeys = info.getChangeables();
        if (changeKeys != null)
        {
            for (String changeKey : changeKeys)
            {
                changeText.put(
                    changeKey,
                    info.getChangeableText(changeKey).get(0)
                );
            }
        }
        updateTitle(0);
        setUpText();

        preventChange = false;
        startUpdates();
        boardManager.generateListTeams(playerID);
    }

    /**
     * Unregister this player scoreboard and any repeating tasks it has created.
     */
    private void removeAll()
    {
        titleIndex = 0;
        boardManager.removeAll(this);
    }
}
