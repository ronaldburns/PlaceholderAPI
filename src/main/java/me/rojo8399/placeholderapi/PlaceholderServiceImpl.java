package me.rojo8399.placeholderapi;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextTemplate;
import org.spongepowered.api.text.serializer.TextSerializer;

import me.rojo8399.placeholderapi.expansions.Expansion;
import me.rojo8399.placeholderapi.utils.TextUtils;

/**
 * Implement placeholder service - should not need to be replaced but is a
 * service in case someone decides they need to.
 */
public class PlaceholderServiceImpl implements PlaceholderService {

	/*
	 * Match against %...% such that it does not have a space in the placeholder
	 * (helps resove conflicts)
	 */
	public final static Pattern PLACEHOLDER_PATTERN = Pattern.compile("[%]([^% ]+)[%]");

	// Package level to prevent instantiation but allow the plugin to create one
	PlaceholderServiceImpl() {
	}

	/**
	 * Hold the expansions in a separate file - will be more useful
	 * when @Wundero figures out downloadable expansions
	 */
	private Registry registry = new Registry();

	/**
	 * Replace string to string placeholders
	 */
	@Override
	public String replacePlaceholdersLegacy(Player player, String text) {
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
			String value = exp.onPlaceholderRequestLegacy(player, Optional.ofNullable(token).map(s -> s.toLowerCase()));
			PlaceholderAPIPlugin.getInstance().getLogger()
					.debug("Format: " + format + ", ID: " + id + ", Value : " + value);
			if (value == null) {
				value = "%" + format + "%";
			}
			text = text.replace("%" + format + "%", Matcher.quoteReplacement(value));
		}
		return text;
	}

	/**
	 * Register expansion
	 */
	@Override
	public boolean registerPlaceholder(Expansion expansion) {
		return registry.register(expansion);
	}

	/**
	 * Register expansion
	 */
	@Override
	public boolean registerPlaceholder(final Object plugin, final BiFunction<Player, Optional<String>, Text> function) {
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
			public Text onPlaceholderRequest(Player player, Optional<String> token, Function<String, Text> parser) {
				return function.apply(player, token);
			}

		};
		return registerPlaceholder(exp);
	}

	/**
	 * Replace placeholders
	 */
	@Override
	public Text replacePlaceholders(Player player, String text) {
		return replacePlaceholders(player, TextUtils.parse(text, Text::of));
	}

	/**
	 * Replace placeholders
	 */
	@Override
	public Text replacePlaceholders(Player player, TextTemplate template) {
		return rpt(player, template, Text::of);
	}

	/**
	 * Replace placeholders
	 */
	@Override
	public Text replacePlaceholders(Player player, TextTemplate template, TextSerializer serializer) {
		return rpt(player, template, serializer::deserialize);
	}

	/*
	 * Replace placeholders then parse value using the function
	 */
	private Text rpt(Player player, TextTemplate template, Function<String, Text> func) {
		Map<String, Object> args = new HashMap<>();
		// For every existing argument
		for (String a : template.getArguments().keySet()) {
			String format = a.toLowerCase();
			int index = format.indexOf("_");
			if (index == 0 || index == format.length()) {
				// We want to skip this but we cannot leave required arguments
				// so filler string is used.
				if (!template.getArguments().get(a).isOptional()) {
					args.put(a, template.getOpenArgString() + a + template.getCloseArgString());
				}
				continue;
			}
			boolean noToken = false;
			if (index == -1) {
				noToken = true;
				index = format.length();
			}
			String id = format.substring(0, index).toLowerCase();
			if (!registry.has(id)) {
				// Again, filler string.
				if (!template.getArguments().get(a).isOptional()) {
					args.put(a, template.getOpenArgString() + a + template.getCloseArgString());
				}
				continue;
			}
			String token = noToken ? null : format.substring(index + 1);
			Expansion exp = registry.get(id);
			Text value = exp.onPlaceholderRequest(player, Optional.ofNullable(token).map(s -> s.toLowerCase()), func);
			PlaceholderAPIPlugin.getInstance().getLogger().debug("Format: " + a + ", ID: " + id + ", Value : " + value);
			if (value == null) {
				value = func.apply("%" + a + "%");
			}
			args.put(a, value);
		}
		return template.apply(args).build();
	}

	/**
	 * Replace placeholders
	 */
	@Override
	public Text replacePlaceholders(Player player, String text, TextSerializer serializer) {
		return replacePlaceholders(player, TextUtils.parse(text, serializer::deserialize), serializer);
	}

	/**
	 * Get all placeholders
	 */
	@Override
	public Set<Expansion> getExpansions() {
		return registry.getAll();
	}

	/**
	 * Get a placeholder by the identifier
	 */
	@Override
	public Optional<Expansion> getExpansion(String id) {
		return Optional.ofNullable(registry.get(id));
	}
}
