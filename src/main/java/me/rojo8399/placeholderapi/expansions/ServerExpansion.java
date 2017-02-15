package me.rojo8399.placeholderapi.expansions;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import me.rojo8399.placeholderapi.PlaceholderAPIPlugin;

public class ServerExpansion implements Expansion {

	private static Runtime runtime = Runtime.getRuntime();
	private static int MB = 1024 * 1024;

	@Override
	public boolean canRegister() {
		return PlaceholderAPIPlugin.getInstance().getConfig().expansions.server;
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
	public Text onPlaceholderRequest(Player player, Optional<String> identifier, Function<String, Text> parser) {
		if (!identifier.isPresent()) {
			return null;
		}
		switch (identifier.get()) {
		case "online":
			return parser.apply(String.valueOf(Sponge.getServer().getOnlinePlayers().size()));
		case "max_players":
			return parser.apply(String.valueOf(Sponge.getServer().getMaxPlayers()));
		case "motd":
			return parser.apply(TextSerializers.FORMATTING_CODE.serialize(Sponge.getServer().getMotd()));
		case "ram_used":
			return parser.apply(String.valueOf((runtime.totalMemory() - runtime.freeMemory()) / MB));
		case "ram_free":
			return parser.apply(String.valueOf(runtime.freeMemory() / MB));
		case "ram_total":
			return parser.apply(String.valueOf(runtime.totalMemory() / MB));
		case "ram_max":
			return parser.apply(String.valueOf(runtime.maxMemory() / MB));
		case "cores":
			return parser.apply(String.valueOf(runtime.availableProcessors()));
		default:
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see me.rojo8399.placeholderapi.expansions.Expansion#getSupportedTokens()
	 */
	@Override
	public List<String> getSupportedTokens() {
		return Arrays.asList("online", "max_players", "motd", "ram_used", "ram_free", "ram_total", "ram_max", "cores");
	}

}
