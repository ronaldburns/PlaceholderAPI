package me.rojo8399.placeholderapi.expansions;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import com.google.common.reflect.TypeToken;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class DateTimeExpansion implements ConfigurableExpansion {

	private static TypeToken<DateTimeExpansion> type = TypeToken.of(DateTimeExpansion.class);

	@Setting
	public boolean enabled;
	@Setting
	public String format = "uuuu LLL dd HH:mm:ss";

	@Override
	public boolean canRegister() {
		return enabled;
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
		DateTimeFormatter f = DateTimeFormatter.ofPattern(format);
		return textParser.apply(LocalDateTime.now().format(f));
	}

	@Override
	public TypeToken<DateTimeExpansion> getToken() {
		return type;
	}

}
