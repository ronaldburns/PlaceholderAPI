package me.rojo8399.placeholderapi.expansions;

import java.util.Optional;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;

import me.rojo8399.placeholderapi.PlaceholderAPIPlugin;
import me.rojo8399.placeholderapi.configs.Config;

public class ServerExpansion implements Expansion {

	private static Runtime runtime = Runtime.getRuntime();
	private static int MB = 1024 * 1024;

	@Override
	public boolean canRegister() {
		return true;
	}

	@Override
	public String getIdentifier() {
		return "server";
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
	public String onPlaceholderRequest(Player player, Optional<String> identifier) {

		Config config = PlaceholderAPIPlugin.getInstance().getConfig();

		if (config.expansions.server) {
			if (!identifier.isPresent()) {
				return null;
			}
			switch (identifier.get()) {
			case "online":
				return String.valueOf(Sponge.getServer().getOnlinePlayers().size());
			case "ram_used":
				return String.valueOf((runtime.totalMemory() - runtime.freeMemory()) / MB);
			case "ram_free":
				return String.valueOf(runtime.freeMemory() / MB);
			case "ram_total":
				return String.valueOf(runtime.totalMemory() / MB);
			case "ram_max":
				return String.valueOf(runtime.maxMemory() / MB);
			case "cores":
				return String.valueOf(runtime.availableProcessors());
			default:
				return null;
			}
		} else {
			return null;
		}
	}

}
