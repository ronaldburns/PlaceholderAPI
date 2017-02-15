# PlaceholderAPI [![forthebadge](http://forthebadge.com/images/badges/made-with-crayons.svg)](http://forthebadge.com)

If you were sent here from another plugin, simply download this plugin and install it in your mods folder. After the first start of this plugin, you will need to enable the default expansions in the config file to be able to use those placeholders. Example if you want Player placeholders, open the config and set <code>Player</code> to <code>True</code>. Now you can use %player_name% as a placeholder. This same method applies for every available expansion.

**There is a chance that a plugin may update and cause one of the internal placeholder hooks to break. If this happens, please report the issue immediately so I can update the placeholder hook to the latest version of the plugin that broke.**



----------

##Donations
I love being able to code plugins on Sponge for you! But it takes a lot of time and effort. If you run a profitable server off one of my plugins, please feel free to share the love!
[Donate Here](http://paypal.me/rojo8399)


----------

##FEATURES

* No lag
* Fast Updates
* Easy contribution

### Planned
* More Built In Placeholders
* Vast plugin support (I'll try to create PRs to help the process)

----------

##USAGE
###Server Owners
Just drop this plugin into your mods folder and enable any default expansions you want to use in the config file.
###Developers
*Coming Soon*

If you're looking to contribute, the source code is available on [GitHub](https://github.com/rojo8399/PlaceholderAPI) and I'll accept most pull requests.
*Tutorial on creating variables coming soon.*

----------

##Built-In PLACEHOLDERS
*Suggest more in the comments.*

####Player
    %player_name%
    %player_displayname%
    %player_prefix%
    %player_suffix%
    %player_world%
####Server
    %server_online%
    %server_max_players%
    %server_motd%
    %server_ram_used%
    %server_ram_free%
    %server_ram_total%
    %server_ram_max%
    %server_cores%
####Rank
    %rank_name%
    %rank_prefix%
    %rank_suffix%
    %rank_option_{option}%
    %rank_perm_{permission}%
####JavaScript
    %javascript_{script}%
####Time
    %time%
####Economy
    %economy_balance_<currency>%
    %economy_balformat_<currency>%
    %economy_display_<currency>%
    %economy_pluraldisplay_<currency>%
    %economy_symbol_<currency>%
####Sound
    %sound_{sound-id}_{volume}_{pitch}% - Plays the sound at the player's location

----------

##COMMANDS
    /papi - View plugin information
    /papi list - List all registered placeholders
    /papi info {placeholder} - View more information about a placeholder
    /papi parse {player} {placeholder} - View the result of a placeholder on a player

----------

##PERMISSIONS
    placeholderapi.admin - Ability to use the Admin commands
----------

##CONFIG 
    expansions {
      player = true
      server = true
      sound = false
      rank = false
      currency = false
      javascript = false
      time {
        enabled = false
        format = "uuuu MMM dd HH:mm:ss"
      }
    }

----------

##BUGS
If you find any bugs or would like to suggest features, please create an issue on [GitHub](https://github.com/rojo8399/PlaceholderAPI/issues)
