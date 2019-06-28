package me.tade.quickboard;

import me.clip.placeholderapi.PlaceholderAPI;
import me.tade.quickboard.config.BoardConfig;
import me.tade.quickboard.events.PlayerReceiveBoardEvent;
import me.tade.quickboard.events.WhenPluginUpdateTextEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PlayerBoard {

    private QuickBoard plugin;

    private Scoreboard board;
    private Objective score;
    private Player player;

    private List<Team> teams = new ArrayList<>();
    private HashMap<Team, String> lot = new HashMap<>();
    private List<String> list;
    private List<String> title;
    private List<String> chlist = new ArrayList<>();
    private int updateTitle;
    private int updateText;
    private int titleTask;
    private int textTask;
    private boolean ch = false;
    private BoardConfig info = null;
    private HashMap<String, String> chanText = new HashMap<>();
    private HashMap<String, Integer> chanTextInt = new HashMap<>();
    private HashMap<String, String> scrollerText = new HashMap<>();
    private List<Integer> tasks = new ArrayList<>();
    private boolean ver13 = false;

    private int index = 15;
    private int titleIndex = 0;

    public PlayerBoard(QuickBoard plugin, Player player, BoardConfig info) {
        this.plugin = plugin;
        this.player = player;
        list = info.getText();
        title = info.getTitle();
        updateTitle = info.getUpdaterTitle();
        updateText = info.getUpdaterText();

        List<String> changeKeys = info.getChangeables();
        for (String s : changeKeys) {
            chanTextInt.put(s, 0);
            chanText.put(s, info.getChangeableText(s).get(0));
        }

        this.info = info;

        PlayerReceiveBoardEvent event = new PlayerReceiveBoardEvent(getPlayer(), list, title, this);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            startSetup(event);
        }
    }

    PlayerBoard(
        QuickBoard plugin,
        Player player,
        List<String> text,
        List<String> title,
        int updateTitle,
        int updateText
    ) {
        this.plugin = plugin;
        this.player = player;
        this.updateTitle = updateTitle;
        this.updateText = updateText;

        this.title = title;
        this.list = text;

        PlayerReceiveBoardEvent event = new PlayerReceiveBoardEvent(getPlayer(), this.list, this.title, this);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            startSetup(event);
        }
    }

    private void startSetup(final PlayerReceiveBoardEvent event) {
        if (plugin.getBoards().containsKey(getPlayer())) {
            plugin.getBoards().get(getPlayer()).remove();
        }
        String version = Bukkit.getBukkitVersion(); // Returns in format like: 1.12.2-R0.1-SNAPSHOT
        ver13 = version.startsWith("1.13") || version.startsWith("1.14");
        colorize();
        titleIndex = event.getTitle().size();
        plugin.getBoards().put(getPlayer(), this);
        plugin.getAllBoards().add(this);

        buildScoreboard(event);

        setUpText(event.getText());

        updater();
    }

    private void buildScoreboard(PlayerReceiveBoardEvent event) {
        board = Bukkit.getScoreboardManager().getNewScoreboard();
        score = board.registerNewObjective("score", "dummy", event.getTitle().get(0));
        score.setDisplaySlot(DisplaySlot.SIDEBAR);

        if (event.getTitle().size() == 0)
            event.getTitle().add(" ");

        getPlayer().setScoreboard(board);
    }

    private void setUpText(final List<String> text) {
        int oi = 0;
        for (String s : text)
        {
            Team t = board.registerNewTeam("Team:" + index);
            String normal = s;
            String sc = chlist.get(oi);
            t.addEntry(sc);
            s = setHolders(s);
            if (s.length() < 3)
            {
                s = s + "§r";
            }
            String[] ts = splitString(s);
            setPrefix(t, ts[0]);
            setSuffix(t, ts[1]);
            score.getScore(sc).setScore(index);
            teams.add(t);
            lot.put(t, normal);
            index--;
            oi++;
        }
    }

    private void colorize() {
        for (ChatColor color : ChatColor.values()) {
            chlist.add(color.toString() + ChatColor.RESET.toString());
        }
    }

    void updateText() {
        if (ch) {
            return;
        }

        for (Team t : this.teams)
        {
            String s = lot.get(t);
            if (info != null)
            {
                for (String a : info.getChangeables())
                {
                    if (s.contains("{CH_" + a + "}"))
                    {
                        s = s.replace("{CH_" + a + "}", "");
                        s = s + chanText.get(a);
                    }
                }
                for (String a : info.getScrollerNames())
                {
                    if (s.contains("{SC_" + a + "}"))
                    {
                        s = s.replace("{SC_" + a + "}", "");
                        s = s + scrollerText.get(a);
                    }
                }
            }
            s = setHolders(s);
            WhenPluginUpdateTextEvent event = new WhenPluginUpdateTextEvent(getPlayer(), s);
            Bukkit.getPluginManager().callEvent(event);

            String[] ts = splitString(event.getText());
            setPrefix(t, ts[0]);
            setSuffix(t, ts[1]);
        }
    }

    private void setPrefix(Team t, String string) {
        if (string.length() > getMaxSize()) {
            t.setPrefix(maxChars(getMaxSize(), string));
            return;
        }
        t.setPrefix(string);
    }

    private void setSuffix(Team t, String string) {
        if (string.length() > getMaxSize()) {
            t.setSuffix(maxChars(getMaxSize(), string));
            return;
        }
        t.setSuffix(string);
    }

    private String setHolders(String s) {
        s = s.replace("{PLAYER}", getPlayer().getName()).replace("{ONLINE}", Bukkit.getOnlinePlayers().size() + "")
                .replace("{TIME}", getPlayer().getWorld().getTime() + "");
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI") && PlaceholderAPI.containsPlaceholders(s))
            s = PlaceholderAPI.setPlaceholders(getPlayer(), s);
        if (plugin.isMVdWPlaceholderAPI())
            s = be.maximvdw.placeholderapi.PlaceholderAPI.replacePlaceholders(getPlayer(), s);

        s = ChatColor.translateAlternateColorCodes('&', s);
        return s;
    }

    private int getMaxSize() {
        if (ver13)
            return 64;
        return 16;
    }

    void updateTitle() {
        if (ch) {
            return;
        }
        if (titleIndex > (title.size() - 1)) {
            titleIndex = 0;
        }
        score.setDisplayName(maxChars(ver13 ? 128 : 32, setHolders(title.get(titleIndex))));
        titleIndex++;
    }

    private String maxChars(int characters, String string) {
        if (ChatColor.translateAlternateColorCodes('&', string).length() > characters)
            return string.substring(0, characters);
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public void remove() {
        stopTasks();
        plugin.getBoards().remove(getPlayer());
        plugin.getAllBoards().remove(this);
        getPlayer().setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
    }

    void stopTasks() {
        Bukkit.getScheduler().cancelTask(titleTask);
        Bukkit.getScheduler().cancelTask(textTask);

        for (int i : tasks)
            Bukkit.getScheduler().cancelTask(i);
    }

    private void updater() {
        titleTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::updateTitle, 0, updateTitle);

        textTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::updateText, 0, updateText);

        if (info != null) {
            for (final String s : info.getChangeables()) {
                int inter = info.getChangeableInterval(s);

                BukkitTask task = new BukkitRunnable() {
                    public void run() {
                        List<String> text = info.getChangeableText(s);
                        chanTextInt.put(s, chanTextInt.get(s) + 1);

                        if (chanTextInt.get(s) >= text.size()) {
                            chanTextInt.put(s, 0);
                        }
                        int ta = chanTextInt.get(s);
                        chanText.put(s, text.get(ta));
                        updateText();
                    }
                }.runTaskTimer(plugin, 1, inter);

                tasks.add(task.getTaskId());
            }

            for (final String s : info.getScrollerNames()) {
                int inter = info.getScrollerUpdate(s);

                BukkitTask task = new BukkitRunnable() {
                    public void run() {
                    Scroller text = info.getScroller(s);
                    text.setupText(
                        setHolders(text.text),
                        '&'
                    );
                    scrollerText.put(s, text.next());
                    updateText();
                    }
                }.runTaskTimer(plugin, 1, inter);

                tasks.add(task.getTaskId());
            }
        }
    }

    public void createNew(
        List<String> text,
        List<String> title,
        int updateTitle,
        int updateText
    ) {
        ch = true;
        stopTasks();
        removeAll();
        colorize();
        this.list = text;
        this.title = title;
        this.updateText = updateText;
        this.updateTitle = updateTitle;
        titleIndex = this.title.size();

        score = board.getObjective("score");

        if (score != null)
        {
            score.setDisplaySlot(DisplaySlot.SIDEBAR);
            score.setDisplayName(this.title.get(0));
        }

        if (this.title.size() <= 0) {
            this.title.add(" ");
        }

        setUpText(text);

        ch = false;
        updater();
    }

    private void removeAll() {
        chlist.clear();
        score = null;
        titleIndex = 0;
        index = 15;
        lot.clear();
        teams.clear();

        for (Team t : board.getTeams()) {
            t.unregister();
        }

        for (String s : board.getEntries()) {
            board.resetScores(s);
        }
    }

    private String getResult(
        boolean BOLD,
        boolean ITALIC,
        boolean MAGIC,
        boolean STRIKETHROUGH,
        boolean UNDERLINE,
        ChatColor color
    ) {
        return ((color != null) && (!color.equals(ChatColor.WHITE)) ? color : "") + "" + (BOLD ? ChatColor.BOLD : "") + (ITALIC ? ChatColor.ITALIC : "") + (MAGIC ? ChatColor.MAGIC : "") + (STRIKETHROUGH ? ChatColor.STRIKETHROUGH : "") + (UNDERLINE ? ChatColor.UNDERLINE : "");
    }

    private String[] splitString(String string) {
        StringBuilder prefix = new StringBuilder(string.substring(0, string.length() >= getMaxSize() ? getMaxSize() : string.length()));
        StringBuilder suffix = new StringBuilder(string.length() > getMaxSize() ? string.substring(getMaxSize()) : "");
        if (prefix.toString().length() > 1 && prefix.charAt(prefix.length() - 1) == '§') {
            prefix.deleteCharAt(prefix.length() - 1);
            suffix.insert(0, '§');
        }
        int length = prefix.length();
        boolean PASSED, UNDERLINE, STRIKETHROUGH, MAGIC, ITALIC;
        boolean BOLD = ITALIC = MAGIC = STRIKETHROUGH = UNDERLINE = PASSED = false;
        ChatColor textColor = null;
        for (int index = length - 1; index > -1; index--) {
            char section = prefix.charAt(index);
            if ((section == '§') && (index < prefix.length() - 1)) {
                char c = prefix.charAt(index + 1);
                ChatColor color = ChatColor.getByChar(c);
                if (color != null) {
                    if (color.equals(ChatColor.RESET)) {
                        break;
                    }
                    if (color.isFormat()) {
                        if ((color.equals(ChatColor.BOLD)) && (!BOLD)) {
                            BOLD = true;
                        } else if ((color.equals(ChatColor.ITALIC)) && (!ITALIC)) {
                            ITALIC = true;
                        } else if ((color.equals(ChatColor.MAGIC)) && (!MAGIC)) {
                            MAGIC = true;
                        } else if ((color.equals(ChatColor.STRIKETHROUGH)) && (!STRIKETHROUGH)) {
                            STRIKETHROUGH = true;
                        } else if ((color.equals(ChatColor.UNDERLINE)) && (!UNDERLINE)) {
                            UNDERLINE = true;
                        }
                    } else if (color.isColor()) {
                        textColor = color;
                    }
                }
            } else if ((index > 0) && (!PASSED)) {
                char c = prefix.charAt(index);
                char c1 = prefix.charAt(index - 1);
                if ((c != '§') && (c1 != '§') && (c != ' ')) {
                    PASSED = true;
                }
            }
            if ((!PASSED) && (prefix.charAt(index) != ' ')) {
                prefix.deleteCharAt(index);
            }
            if (textColor != null) {
                break;
            }
        }
        String result = suffix.toString().isEmpty() ? "" : getResult(BOLD, ITALIC, MAGIC, STRIKETHROUGH, UNDERLINE, textColor);
        if ((!suffix.toString().isEmpty()) && (!suffix.toString().startsWith("§"))) {
            suffix.insert(0, result);
        }
        return new String[]{prefix.toString().length() > getMaxSize() ? prefix.toString().substring(0, getMaxSize()) : prefix.toString(), suffix.toString().length() > getMaxSize() ? suffix.toString().substring(0, getMaxSize()) : suffix.toString()};
    }

    Scoreboard getBoard() {
        return board;
    }

    @SuppressWarnings("unused")
    public Objective getScore() {
        return score;
    }

    public Player getPlayer() {
        return player;
    }

    @SuppressWarnings("WeakerAccess")
    public List<String> getList() {
        return list;
    }

    @SuppressWarnings("unused")
    public List<String> getTitle() {
        return title;
    }
}
