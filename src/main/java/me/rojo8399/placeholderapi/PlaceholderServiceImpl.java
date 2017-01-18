package me.rojo8399.placeholderapi;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.plugin.PluginContainer;

import me.rojo8399.placeholderapi.expansions.Expansion;

public class PlaceholderServiceImpl implements PlaceholderService {

	private final static Pattern PLACEHOLDER_PATTERN = Pattern.compile("[%]([^% ]+)[%]");

	PlaceholderServiceImpl() {
	}

	private Registry registry = new Registry();

	@Override
	public String replacePlaceholders(Player player, String text) {
		Matcher placeholderMatcher = PLACEHOLDER_PATTERN.matcher(text);
		while (placeholderMatcher.find()) {
			String format = placeholderMatcher.group(1);
			int index = format.indexOf("_");
			if (index == 0 || index == format.length()) {
				continue;
			}
			boolean noToken = false;
			if (index == -1) {
				noToken = true;
				index = format.length();
			}
			String id = format.substring(0, index).toLowerCase();
			if (!registry.has(id)) {
				continue;
			}
			String token = noToken ? null : format.substring(index + 1);
			Expansion exp = registry.get(id);
			String value = exp.onPlaceholderRequest(player, Optional.ofNullable(token).map(s -> s.toLowerCase()));
			PlaceholderAPIPlugin.getInstance().getLogger()
					.debug("Format: " + format + ", ID: " + id + ", Value : " + value);
			if (value == null) {
				value = "%" + format + "%";
			}
			text = text.replace("%" + format + "%", Matcher.quoteReplacement(value));
		}
		return text;
	}

	@Override
	public boolean registerPlaceholder(Expansion expansion) {
		return registry.register(expansion);
	}

	@Override
	public boolean registerPlaceholder(final Object plugin,
			final BiFunction<Player, Optional<String>, String> function) {
		if (plugin == null) {
			return false;
		}
		Optional<PluginContainer> cont = Sponge.getPluginManager().fromInstance(plugin);
		if (!cont.isPresent()) {
			return false;
		}
		final PluginContainer c = cont.get();
		Expansion exp = new Expansion() {

			@Override
			public boolean canRegister() {
				return true;
			}

			@Override
			public String getIdentifier() {
				return c.getId().toLowerCase();
			}

			@Override
			public String getAuthor() {
				return c.getAuthors().toString();
			}

			@Override
			public String getVersion() {
				return c.getVersion().orElse("1.0");
			}

			@Override
			public String onPlaceholderRequest(Player player, Optional<String> token) {
				return function.apply(player, token);
			}

		};
		return registerPlaceholder(exp);
	}

}
