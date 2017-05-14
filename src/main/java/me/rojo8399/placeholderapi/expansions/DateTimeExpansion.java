package me.rojo8399.placeholderapi.expansions;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import com.google.common.reflect.TypeToken;

import me.rojo8399.placeholderapi.configs.Messages;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class DateTimeExpansion implements ConfigurableExpansion {

	private static TypeToken<DateTimeExpansion> type = TypeToken.of(DateTimeExpansion.class);

	@Setting
	public boolean enabled;
	@Setting
	public String format = "uuuu LLL dd HH:mm:ss";
	private DateTimeFormatter f;

	@Override
	public boolean canRegister() {
		f = DateTimeFormatter.ofPattern(format);
		return enabled && f != null;
	}

	@Override
	public String getIdentifier() {
		return "time";
	}

	@Override
	public String getDescription() {
		return Messages.get().placeholder.timedesc.value;
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
	public Object onValueRequest(Player player, Optional<String> token) {
		return LocalDateTime.now();
	}

	@Override
	public Text onPlaceholderRequest(Player player, Optional<String> token) {
		return TextSerializers.FORMATTING_CODE.deserialize(LocalDateTime.now().format(f));
	}

	@Override
	public TypeToken<DateTimeExpansion> getToken() {
		return type;
	}

}
