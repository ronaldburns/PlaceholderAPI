package me.rojo8399.placeholderapi.expansions;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import com.google.common.reflect.TypeToken;

import me.rojo8399.placeholderapi.configs.Messages;
import ninja.leaping.configurate.objectmapping.Setting;

public class StatisticExpansion implements ConfigurableExpansion {

	private static final TypeToken<StatisticExpansion> type = TypeToken.of(StatisticExpansion.class);

	@Setting
	public boolean enabled = true;

	@Override
	public boolean canRegister() {
		return enabled;
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
	public String getDescription() {
		return Messages.get().placeholder.statdesc.value;
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
				.filter(e -> e.getKey().getId().replace("._", ".").toLowerCase().startsWith(token.get().toLowerCase()))
				.map(Map.Entry::getValue).reduce(0l, (a, b) -> a + b));
	}

	@Override
	public TypeToken<? extends ConfigurableExpansion> getToken() {
		return type;
	}

}
