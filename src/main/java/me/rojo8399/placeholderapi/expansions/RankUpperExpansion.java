package me.rojo8399.placeholderapi.expansions;

import org.spongepowered.api.Game;
import org.spongepowered.api.entity.living.player.User;

import br.net.fabiozumbi12.rankupper.PermsAPI;
import me.rojo8399.placeholderapi.PlaceholderAPIPlugin;
import me.rojo8399.placeholderapi.configs.Config;

public class RankUpperExpansion extends Expansion {

	@Override
	public boolean canRegister() {
		return true;
	}

	@Override
	public String getIdentifier() {
		return "rankupper";
	}

	@Override
	public String getAuthor() {
		return "rojo8399";
	}

	@Override
	public String getVersion() {
		return "1.0";
	}

	public static String onPlaceholderRequest(User p, String identifier) {
		
		Config config = PlaceholderAPIPlugin.getInstance().getConfig();
		Game game = PlaceholderAPIPlugin.getInstance().getGame();
		
		if (config.expansions.rankupper && game.getPluginManager().getPlugin("rankupper").isPresent()) {
			switch (identifier) {
			case "group":
				PermsAPI permsAPI = new PermsAPI(game);
				return permsAPI.getGroup(p);
			default:
				return null;
			}
		} else {
			return null;
		}
	}

}
