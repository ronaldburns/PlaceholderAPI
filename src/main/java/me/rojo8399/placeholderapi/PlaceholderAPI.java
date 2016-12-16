package me.rojo8399.placeholderapi;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.spongepowered.api.entity.living.player.Player;

import me.rojo8399.placeholderapi.expansions.PlayerExpansion;
import me.rojo8399.placeholderapi.expansions.ServerExpansion;

public class PlaceholderAPI {

	private final static Pattern PLACEHOLDER_PATTERN = Pattern.compile("[%]([^%]+)[%]");

	@SuppressWarnings("unused")
	private final static Pattern BRACKET_PLACEHOLDER_PATTERN = Pattern.compile("[{]([^{}]+)[}]");

	public static String setPlaceholders(Player p, String text) {

		Matcher placeholderMatcher = PLACEHOLDER_PATTERN.matcher(text);

		while (placeholderMatcher.find()) {

			String format = placeholderMatcher.group(1);

			int index = format.indexOf("_");

			if (index <= 0 || index >= format.length()) {
				continue;
			}

			String id = format.substring(0, index);
			String identifier = format.substring(index + 1);

			String value = null;

			switch (id) {
			case "player":
				value = PlayerExpansion.onPlaceholderRequest(p, identifier);
			}

			text = text.replace("%" + format + "%", Matcher.quoteReplacement(value));

		}

		return text;
	}

	public static String setPlaceholders(String text) {

		Matcher placeholderMatcher = PLACEHOLDER_PATTERN.matcher(text);

		while (placeholderMatcher.find()) {

			String format = placeholderMatcher.group(1);

			int index = format.indexOf("_");

			if (index <= 0 || index >= format.length()) {
				continue;
			}

			String id = format.substring(0, index);
			String identifier = format.substring(index + 1);

			String value = null;

			switch (id) {
			case "server":
				value = ServerExpansion.onPlaceholderRequest(identifier);
			}

			text = text.replace("%" + format + "%", Matcher.quoteReplacement(value));

		}

		return text;
	}

}
