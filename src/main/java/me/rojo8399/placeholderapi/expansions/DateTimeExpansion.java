package me.rojo8399.placeholderapi.expansions;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import me.rojo8399.placeholderapi.PlaceholderAPIPlugin;

public class DateTimeExpansion implements Expansion {

	@Override
	public boolean canRegister() {
		return PlaceholderAPIPlugin.getInstance().getConfig().expansions.date.enabled;
	}

	@Override
	public String getIdentifier() {
		return "time";
	}

	@Override
	public String getAuthor() {
		return "Wundero";
	}

	@Override
	public String getVersion() {
		return "1.0";
	}

	@Override
	public List<String> getSupportedTokens() {
		return Arrays.asList((String) null);
	}

	@Override
	public Text onPlaceholderRequest(Player player, Optional<String> token, Function<String, Text> textParser) {
		DateTimeFormatter f = DateTimeFormatter
				.ofPattern(PlaceholderAPIPlugin.getInstance().getConfig().expansions.date.format);
		return textParser.apply(LocalDateTime.now().format(f));
	}

}
