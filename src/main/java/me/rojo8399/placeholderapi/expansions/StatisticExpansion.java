package me.rojo8399.placeholderapi.expansions;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

public class StatisticExpansion implements Expansion {

	@Override
	public boolean canRegister() {
		return true;
	}

	@Override
	public String getIdentifier() {
		return "statistic";
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
		return null;
	}

	@Override
	public Text onPlaceholderRequest(Player player, Optional<String> token) {
		if (!token.isPresent()) {
			return null;
		}
		return Text.of(player.getOrNull(Keys.STATISTICS).entrySet().stream()
				.peek(e -> System.out.println(e.getKey().getId().replace("._", ".")))
				.filter(e -> e.getKey().getId().replace("._", ".").toLowerCase().startsWith(token.get().toLowerCase()))
				.map(Map.Entry::getValue).reduce(0l, (a, b) -> a + b));
	}

}
