![QuickBoardX][logo]
##### A fast, free Scoreboard plugin for Spigot and Paper
___
### Features
- Free!
- No Flickering or Artifacts
- Compatible with 1.8.8, 1.12.2 and 1.14.4
- Updating Title 
- Scrolling Text Elements
- Changeable Text Elements
- Support for Placeholders; Placeholder API and MaximVDW
- Tab Completion for all Commands
- Fully Configurable Scoreboards: Control Visibility by World Names
- Scoreboards can be created and configured using Commands
- Multiple Scoreboards for the Same World
- An API for Interacting with Scoreboards and Creating Temporary Scoreboards
___

### Commands and Permissions
All quickboardx commands can be accessed using _/quickboardx_ or either of the aliases: _/qbx_ or _/qb_
I recommend using [LuckPerms][3] to manage your permissions.

#### Install LuckPerms

**Then Use the following commands from your server console**
```
lp creategroup admins
lp group admins parent set default
lp group admins permission set quickboardx.*
lp group default permission set scoreboard.default
```
**Add yourself to the admins group**

`lp user your_username parent set admins`

**(Optional) OR create a Track for easy promotion and demotion**
```
lp createtrack roles
lp track roles append default
lp track roles append admins
lp user your_username promote roles
```

**To give your admin group the ability to use luckperms commands in game**

`lp group admins permission set luckperms.*`
___

##### Commands
> Any command that has a scoreboard for a parameter will require the name of the scoreboard's config file, without the extension.
>
> For example, to set your visible scoreboard to the default scoreboard you would use **_/qbx set scoreboard.default_**
>
> All parameters in **\<brackets>** are required

|Name               | Parameters                            | Aliases   | Permission            | Description   |
|:-----------       |:-----------------                     |:--------- |:-------------         |:---               |
| _/qbx_            | _None_                                | _None_    | _None_                | _Shows all commands and summaries for each_ |
| /qbx check        | _None_                                | _None_    | _quickboardx.check_   | _Displays a list of the available Scoreboards, whether you have permission and if you are in one of its enabled worlds_ |
| /qbx toggle       | _None_                                | _tog_     | _quickboardx.toggle_  | _Toggle your scoreboard's visibility_ |
| /qbx next         | _None_                                | _None_    | _quickboardx.toggle_  | _Select the next available scoreboard from the list of accessible boards._ |
| /qbx prev         | _None_                                | _None_    | _quickboardx.toggle_  | _Select the previous scoreboard from the list of accessible boards._ |
| /qbx on           | _None_                                | _None_    | _quickboardx.toggle_  | _Enable your scoreboard._ |
| /qbx reload       | _None_                                | _rl_      | _quickboardx.reload_  | _Reload the configuration files for QuickBoardX._ |
| /qbx set          | _\<scoreboard\>_                      | _s_       | _quickboardx.set_     | _Sets the visible scoreboard for a player, using the board's name (permission)._ |
| /qbx select       | _\<scoreboard\>_                      | _sl_      | _quickboardx.select_  | _Switch to the selected scoreboard from the list of accessible boards._ |
| /qbx create       | _\<scoreboard\>_                      | _c_       | _quickboardx.create_  | _Create a scoreboard with the provided name (permission)._ |
| /qbx addtitle     | _\<scoreboard> <line_number> <text\>_ | _at_      | _quickboardx.edit_    | _Add a line to a Scoreboard's Title._ |
| /qbx removetitle  | _\<scoreboard> <line_number\>_        | _rt_      | _quickboardx.edit_    | _Remove a line from a Scoreboard's Title._ |
| /qbx inserttitle  | _\<scoreboard> <line_number> <text\>_ | _it_      | _quickboardx.edit_    | _Insert a line into a Scoreboard's Title._ |
| /qbx addline      | _\<scoreboard> <line_number> <text\>_ | _al_      | _quickboardx.edit_    | _Add a line to a Scoreboard's Text._ |
| /qbx removeline   | _\<scoreboard> <line_number\>_        | _rl_      | _quickboardx.edit_    | _Remove a line from a Scoreboard's Text._ |
| /qbx insertline   | _\<scoreboard> <line_number> <text\>_ | _il_      | _quickboardx.edit_    | _Inserts a line into a Scoreboard's Text._ |
| /qbx list         | _None_                                | _ls_      | _quickboardx.info_    | _View a list of all available scoreboards._ |
| /qbx listworlds   | _None_                                | _lsw_     | _quickboardx.info_    | _List the Enabled Worlds for a Scoreboard._ |
| /qbx addworld     | _\<scoreboard> <world\>_              | _aw_      | _quickboardx.edit_    | _Add a world to the enabled worlds for a Scoreboard._ |
| /qbx removeworld  | _\<scoreboard> <world\>_              | _rw_      | _quickboardx.edit_    | _Remove a world from the enabled worlds for a Scoreboard._ |


##### Scoreboards
> Permissions for scoreboards are based upon the name of the scoreboards configuration file.
>
> For example, the default scoreboard configuration file is called **_scoreboard.default.yml_**, the permission
> required to see the default scoreboard is **_scoreboard.default_**
### Placeholders
> Want to use placeholders in your scoreboards? Then follow the steps below to get started!
##### Placeholder API
1. Download [Placeholder API][1]
1. Install in your _plugins_ directory
1. Restart
1. To use the placeholders in the default scoreboard, install the **Server** and **Player** extensions.
    1. _/papi ecloud download Server_
    1. _/papi ecloud download Player_
    1. _/papi reload_
    1. _/qbx reload_ 
1. To learn more about PlaceholderAPI (Papi), visit their [Discord!][4]
##### MaximVdw Placeholders
1. Install a plugin that uses Maxim Placeholders
1. Download and install [mvdwplaceholderapi][2]
1. Reference one of the placeholders in a Scoreboard's config: `{the_placeholder_name}`

### API

The API provides plugin developers with methods for creating scoreboards, both permanent and temporary.
Temporary boards will only be displayed for a short period of time, and using Scrollable or Changeable elements
is not recommended.
Methods are provided for referencing existing scoreboard configuration files. Those methods allow you to reference
the lists of text and titles already defined in that scoreboard's yml file.

#### Maven

##### Artifact
```xml
<!--QuickBoardX-->
<dependency>
    <groupId>net.frostbyte.quickboardx</groupId>
    <artifactId>quickboardx-core</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>
```

##### Repository
```xml
<repository>
    <id>frost-byte</id>
    <url>https://frost-byte.net/nexus/repository/maven-public/</url>
</repository>
```
##### plugin.yml
```yaml
depend: [QuickBoardX]
```

##### Interface
```java
public interface QuickBoardAPI
{

    /**
     * Create a Temporarily displayed scoreboard for a Player.
     *
     * @param playerID The Player's UUID
     * @param boardName The name of the scoreboard config to use. (without the extension)
     * @return The Playerboard
     */
    PlayerBoard createTemporaryBoard(UUID playerID, String boardName);

    /**
     * Create a Temporarily displayed scoreboard for a player
     *
     * @param playerID The player's UUID
     * @param text The Text lines for the body of the scoreboard
     * @param title The Lines of Text displayed in the Board Title
     * @param updateTitle The update interval for cycling through the Title Lines, in ticks.
     * @param updateText The update interval for scrollable and changeable elements contained in the scoreboard.
     * @return The PlayerBoard that was created using the given parameters.
     */
    PlayerBoard createTemporaryBoard(
        UUID playerID,
        String boardName,
        List<String> title,
        List<String> text,
        int updateTitle,
        int updateText
    );

    /**
     * Create a scoreboard for a Player.
     * 
     * @param playerID The Player's UUID
     * @param name The name of the scoreboard config to use. (without the extension)
     * @return The Playerboard
     */
    @SuppressWarnings("UnusedReturnValue")
    PlayerBoard createBoard(UUID playerID, String name);

    /**
     * Create a Player Board for a player
     * 
     * @param playerID The player's UUID
     * @param text The Text lines for the body of the scoreboard
     * @param title The Lines of Text displayed in the Board Title
     * @param updateTitle The update interval for cycling through the Title Lines, in ticks.
     * @param updateText The update interval for scrollable and changeable elements contained in the scoreboard.
     * @return The PlayerBoard that was created using the given parameters.
     */
    PlayerBoard createBoard(
        UUID playerID,
        List<String> text,
        List<String> title,
        int updateTitle,
        int updateText
    );

    /**
     * Retrieve the list of all registered Player Scoreboards.
     * @return The list of player boards
     */
    List<PlayerBoard> getAllBoards();

    /**
     * Retrieve the map of player UUID's to their registered
     * player boards.
     * @return A map of player IDs to Player Boards
     */
    HashMap<UUID, PlayerBoard> getBoards();

    /**
     * Remove a Player's Scoreboard.
     * 
     * @param playerID The Player's UUID
     */
    void removeBoard(UUID playerID);

    /**
     * Update the Text for a Player's Scoreboard.
     *
     * @param playerID The Player's UUID
     */
    void updateText(UUID playerID);

    /**
     * Update the Title for a Player's Scoreboard.
     *
     * @param playerID The Player's UUID
     */
    void updateTitle(UUID playerID);

    /**
     * Update the Title, Text, Changeable and Scrollable 
     * elements for a Player's Scoreboard.
     *
     * @param playerID The Player's UUID
     */
    void updateAll(UUID playerID);
}
```
##### Example Usage
```java
package net.frostbyte.quickboardx.api.example;

import org.bukkit.plugin.java.JavaPlugin;

import net.frostbyte.quickboardx.api.QuickBoardAPI;

public class APIExample extends JavaPlugin {
    @Override
    public void onEnable()
    {
        RegisteredServiceProvider<QuickBoardAPI> boardProvider = serviceManager.getRegistration(QuickBoardAPI.class);

        // The QuickBoard API
        QuickBoardAPI boardAPI;

        if (boardProvider != null) {
            boardAPI = boardProvider.getProvider();

            if (boardAPI != null) {
                new BukkitRunnable()
                {
                    @Override
                    public void run()
                    {
                        for (Player p : Bukkit.getServer().getOnlinePlayers())
                        {
                            List<String> title = new ArrayList<>();
                            title.add("> Temp Title <");
                            title.add("> Temp Titl <");
                            title.add("> Temp Tit <");
                            title.add("> Temp Ti <");
                            title.add("> Temp T <");
                            title.add("> Temp  <");
                            title.add("> Temp <");
                            List<String> lines = new ArrayList<>();
                            lines.add("line 1");
                            lines.add("");
                            lines.add("%player_name%");
                            lines.add("");
                            lines.add("line 5");
                            lines.add("");
        
                            boardAPI.createTemporaryBoard(
                                p.getUniqueId(),
                                "scoreboard.temp",
                                title,
                                lines,
                                6,
                                2
                            );
                        }
                    }
                }.runTask(plugin);
            }
        }
    }
}
```

### Links
[1]: https://www.spigotmc.org/resources/placeholderapi.6245/
[2]: https://www.spigotmc.org/resources/mvdwplaceholderapi.11182/
[3]: https://luckperms.net/
[4]: https://helpch.at/discord
[5]: https://github.com/frost-byte/QuickBoardX
[6]: https://oss.frost-byte.net/nexus/ 
[7]: https://www.spigotmc.org/members/tejdik.28526/
[8]: https://www.spigotmc.org/resources/quickboard-free-scoreboard-plugin-scroller-changeable-text-placeholderapi-anti-flicker.15057/
[9]: https://discord.gg/MZNYhTA
[logo]: https://github.com/frost-byte/QuickBoardX/raw/master/images/Layer-QuickBoardX.png

### Source Code
[Github Repository][5]  
[Nexus Maven Repository][6]  

### Acknowledgements
Thanks to [the_TadeSK][7] for the creation of [QuickBoard][8]; the plugin that QuickBoardX is based upon.

#### Discord
For support please contact me on [the frost-byte central Discord!][9]
