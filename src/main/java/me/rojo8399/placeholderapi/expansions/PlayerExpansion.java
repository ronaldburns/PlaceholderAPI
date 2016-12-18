package me.rojo8399.placeholderapi.expansions;

import org.spongepowered.api.entity.living.player.Player;

import me.rojo8399.placeholderapi.PlaceholderAPIPlugin;

public class PlayerExpansion extends Expansion {
	
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

	public static String onPlaceholderRequest(Player p, String identifier) {
		PlaceholderAPIPlugin.getInstance().getLogger().debug("PlayerExpansion Called");
		switch (identifier) {
		case "name":
			return p.getName();
		default:
			return null;
		}
	}	
	
}
