package me.rojo8399.placeholderapi.expansions;

import org.spongepowered.api.entity.living.player.Player;

import me.rojo8399.placeholderapi.PlaceholderAPIPlugin;
import me.rojo8399.placeholderapi.configs.Config;

public class PlayerExpansion implements Expansion {

	@Override
	public boolean canRegister() {
		return true;
	}

	@Override
	public String getIdentifier() {
		return "player";
	}

	@Override
	public String getAuthor() {
		return "rojo8399";
	}

	@Override
	public String getVersion() {
		return "1.0";
	}

	@Override
	public String onPlaceholderRequest(Player p, String identifier) {

		Config config = PlaceholderAPIPlugin.getInstance().getConfig();

		if (config.expansions.player) {
			switch (identifier) {
			case "name":
				return p.getName();
			default:
				return null;
			}
		} else {
			return null;
		}
	}

}
