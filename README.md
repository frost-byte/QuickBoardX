![QuickBoardX][logo]
##### A fast, free Scoreboard plugin for Spigot and Paper
___
### Features
- Free!
- No Flickering or Artifacts
- Per Player Scoreboards for the Sidebar and Player Tab List
- Compatible with 1.8.8, 1.12.2, 1.14.4 and 1.15.2
- Updating Title 
- Scrolling Text Elements
- Changeable Text Elements
- Support for Placeholders; Placeholder API and MaximVDW
- Tab Completion for all Commands
- Fully Configurable Scoreboards: Control Visibility by World Names
- Fully Configurable Teams for the Player Tab List, which can change based upon the board displayed
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
| /qbx check_team   | _None_                                | _t_check_ | _quickboardx.check_   | _Displays your current Player Tab List team based upon your active Scoreboard, and whether the team is applied for that scoreboard_ |
| /qbx list_teams   | _None_                                | _lst_     | _quickboardx.info_    | _View the list of all Player Tab List teams._ |
| /qbx show_team    | _None_                                | _t_show_  | _quickboardx.check_   | _Shows the configuration for your Player Tab List team._ |

#### Scoreboards
> QuickBoardX allows for the customization of both the Sidebar Slot and the Player Tab List.
> This is accomplished by creating per player Scoreboards which define the contents in the Sidebar and Tab List.
> The permission required to see a scoreboard comes from the name of the scoreboard's configuration file.
>
> For example, the default scoreboard configuration file is **_scoreboard.default.yml_**, the permission
> required to see the default scoreboard is **_scoreboard.default_**

##### Teams
> The default Player Tab List team definition is in teams/teams.default.yml.  You can add additional
> teams to the config.
>
> The properties for each team defined in the config match up with those defined by the Spigot API which
> is based upon how they are defined in Vanilla Minecraft.
>
> When defining a color for a team prefix or suffix, you must use a color code, preceded by an &; make sure to include the
> reset color code at the end of the prefix (&r), otherwise the color might will be applied to the player's
> name.
>
> The color property for a team applies to all player names that have been added to that team.
> 
> `enabledScoreboards` The defined teams will only apply to the QuickBoardX Scoreboards in this list.
> `applyToAllScoreboards` All Scoreboards defined by QuickBoardX will use the teams defined in the config.
>
> For further details about Scoreboard Teams in Minecraft and the Spigot API for Scoreboard teams
> check out the following links:
>
> [Gamepedia Scoreboard Teams][gp_teams]
>
> [How-to: Create Scoreboard Teams for Dummies][dummies_teams]
>
> [Spigot Javadocs][spigot_teams]

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

The API provides plugin developers with methods for creating custom sidebar scoreboards, both permanent and temporary.
Temporary boards will only be displayed for a short time. Using Scrollable or Changeable elements
should not be used in temporary scoreboards.
Methods have been included for referencing existing scoreboard configuration files. Those methods allow you to access
the lists of text and titles already defined in that scoreboard's configuration file.

In addition to the Sidebar scoreboard, the Player Tab List can be customized per player. Whenever
a player joins the server they will be added to the default team defined in the default team config file.
The recommended way to change a player's team is to call the `PlayerTeamUpdate` event and providing the player's
UUID and the name of the Team that they are joining.
 
#### Maven

##### Artifact
```xml
<!--QuickBoardX-->
<dependency>
    <groupId>net.frostbyte.quickboardx</groupId>
    <artifactId>quickboardx-core</artifactId>
    <version>1.1.0-SNAPSHOT</version>
</dependency>
```


![CloudRepo.io][cloud]
>  _CloudRepo.io allows open source projects to use their services at no cost._
>  _They provide Public, Private and Proxy Repositories_
##### Maven Repositories
```xml
<!-- frost-byte snapshots -->
<repository>
    <id>io.cloudrepo.snapshots</id>
    <url>https://frostbyte.mycloudrepo.io/public/repositories/snapshots</url>
</repository>
<!-- frost-byte releases -->
<repository>
    <id>io.cloudrepo</id>
    <url>https://frostbyte.mycloudrepo.io/public/repositories/releases</url>
</repository>
```
##### plugin.yml
```yaml
depend: [QuickBoardX]
```

[QuickBoardX API][api]

##### Example Usage
```java
package net.frostbyte.quickboardx.api.example;

import org.bukkit.plugin.java.JavaPlugin;

import net.frostbyte.quickboardx.api.QuickBoardAPI;

public class APIExample extends JavaPlugin implements Listener {
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
                        // Create a Temporary Board for each player online
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

                // Register your Plugin as a Listener, so it can respond to the PlayerJoinEvent
                PluginManager pm = getServer().getPluginManager();
        
                pm.registerEvents(
                    this,
                    this
                );
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        if (event == null)
            return;
    
        // Assign the player to a team in the Player List.
        // The team can be based upon a permission, for example, a luckperms group.
        // This also requires you to have the team defined in a team configuration
        // in your QuickBoardX plugin folder (plugins/QuickBoardX/teams/team.default.yml)
        // Assuming that your own method for determining a player's team is called getPlayerTeam...
        String teamName = getPlayerTeam(event.getPlayer());
    
        // Tell QuickBoardX to update each Online Player's Player List to
        // show the player as a member of the team.
        Bukkit.getPluginManager().callEvent(
            new PlayerTeamUpdateEvent(
                player.getUniqueId(),
                teamName
            )
        );
    }
}
```

### Links
[1]: https://www.spigotmc.org/resources/placeholderapi.6245/
[2]: https://www.spigotmc.org/resources/mvdwplaceholderapi.11182/
[3]: https://luckperms.net/
[4]: https://helpch.at/discord
[5]: https://github.com/frost-byte/QuickBoardX
[6]: https://frostbyte.mycloudrepo.io/public/repositories/releases 
[7]: https://www.spigotmc.org/members/tejdik.28526/
[8]: https://www.spigotmc.org/resources/quickboard-free-scoreboard-plugin-scroller-changeable-text-placeholderapi-anti-flicker.15057/
[9]: https://discord.gg/MZNYhTA
[10]: https://frostbyte.mycloudrepo.io/public/repositories/snapshots
[11]: https://www.cloudrepo.io
[logo]: images/Layer-QuickboardX.png
[cloud]: images/CloudRepo-Square-Brand-Blue.png
[gp_teams]: https://minecraft.gamepedia.com/Scoreboard#Teams
[dummies_teams]: https://www.dummies.com/programming/programming-games/minecraft/how-to-create-teams-with-the-scoreboard-in-minecraft/
[spigot_teams]: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/scoreboard/Team.html
[api]: https://github.com/frost-byte/QuickBoardX/blob/master/quickboardx-core/src/main/java/net/frostbyte/quickboardx/api/QuickBoardAPI.java#L10

### Source Code
[Github Repository][5]  
[CloudRepo Maven Repository - Releases][6]  
[CloudRepo Maven Repository - Snapshots][10]

### Acknowledgements
Thanks to [the_TadeSK][7] for the creation of [QuickBoard][8]; the plugin that QuickBoardX is based upon.

#### Discord
For support please contact me on [the frost-byte central Discord!][9]
