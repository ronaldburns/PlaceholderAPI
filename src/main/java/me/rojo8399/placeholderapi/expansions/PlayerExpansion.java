package me.rojo8399.placeholderapi.expansions;

import static me.rojo8399.placeholderapi.utils.TextUtils.textOf;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import me.rojo8399.placeholderapi.PlaceholderAPIPlugin;

public class PlayerExpansion implements Expansion {

	@Override
	public boolean canRegister() {
		return PlaceholderAPIPlugin.getInstance().getConfig().expansions.player;
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
	public Text onPlaceholderRequest(Player p, Optional<String> identifier) {
		if (!identifier.isPresent()) {
			return Text.of(p.getName());
		}
		String token = identifier.get();
		if (token.toLowerCase().startsWith("option_")) {
			String op = token.substring("option_".length());
			return TextSerializers.FORMATTING_CODE.deserialize(p.getOption(op).orElse(""));
		}
		if (token.toLowerCase().startsWith("perm") && token.contains("_")) {
			String op = token.substring(token.indexOf("_"));
			return Text.of(p.getPermissionValue(p.getActiveContexts(), op).toString());
		}
		if (token.toLowerCase().startsWith("stat") && token.contains("_")) {
			String s = token.substring(token.indexOf("_"));
			return textOf(p.getStatisticData().statistics().entrySet().stream()
					.filter(e -> e.getKey().getId().equalsIgnoreCase(s) || e.getKey().getName().equalsIgnoreCase(s))
					.sorted((e1, e2) -> e1.getKey().getId().equalsIgnoreCase(s) ? -1 : 1).map(e -> e.getValue())
					.findFirst().orElse(-1l));
		}
		switch (token) {
		case "prefix":
		case "suffix":
			return TextSerializers.FORMATTING_CODE.deserialize(p.getOption(identifier.get()).orElse(""));
		case "name":
			return Text.of(p.getName());
		case "displayname":
			return p.getDisplayNameData().displayName().get();
		case "world":
			return Text.of(p.getWorld().getName());
		case "ping":
			return Text.of(p.getConnection().getLatency());
		case "language":
			return Text.of(p.getLocale().getDisplayName());
		case "flying":
			return textOf(!p.isOnGround());
		case "health":
			return textOf(Math.round(p.health().get()));
		case "max_health":
			return textOf(Math.round(p.maxHealth().get()));
		case "food":
			return textOf(p.foodLevel().get());
		case "saturation":
			return textOf(Math.round(p.saturation().get()));
		case "gamemode":
			return Text.of(p.gameMode().get().getName());
		case "x":
			return textOf(p.getLocation().getPosition().toInt().getX());
		case "y":
			return textOf(p.getLocation().getPosition().toInt().getY());
		case "z":
			return textOf(p.getLocation().getPosition().toInt().getZ());
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
		return Arrays.asList(null, "prefix", "suffix", "name", "displayname", "world", "option_[option]",
				"permission_[permission]", "statistic_[statistic]", "ping", "flying", "language", "health",
				"max_health", "food", "saturation", "gamemode", "x", "y", "z");
	}

}