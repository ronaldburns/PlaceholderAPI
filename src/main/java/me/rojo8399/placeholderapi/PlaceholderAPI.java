package me.rojo8399.placeholderapi;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.spongepowered.api.entity.living.player.Player;

import me.rojo8399.placeholderapi.expansions.PlayerExpansion;
import me.rojo8399.placeholderapi.expansions.RankUpperExpansion;
import me.rojo8399.placeholderapi.expansions.ServerExpansion;
import me.rojo8399.placeholderapi.expansions.SoundExpansion;

public class PlaceholderAPI {

	private final static Pattern PLACEHOLDER_PATTERN = Pattern.compile("[%]([^%]+)[%]");

	@SuppressWarnings("unused")
	private final static Pattern BRACKET_PLACEHOLDER_PATTERN = Pattern.compile("[{]([^{}]+)[}]");

	
	
	/**
	 * set placeholders in the text specified
	 * placeholders are matched with the pattern {<placeholder>} when set with this method
	 * @param player Player to set the placeholders for
	 * @param text text to set the placeholder values to
	 * @return original text with all valid placeholders set to the correct values if the String contains valid placeholders
	 */
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

			String value;

			switch (id) {
			case "player":
					value = PlayerExpansion.onPlaceholderRequest(p, identifier);
					break;
			case "server":
					value = ServerExpansion.onPlaceholderRequest(identifier);
					break;
			case "rankupper":
					value = RankUpperExpansion.onPlaceholderRequest(p, identifier);
					break;
			case "sound":
				value = SoundExpansion.onPlaceholderRequest(p, identifier);
				break;
			default:
				value = null;
				break;
			}

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
	 * set placeholders in the list<String> text provided
	 * placeholders are matched with the pattern {<placeholder>} when set with this method
	 * @param p Player to set the placeholders for
	 * @param text text to set the placeholder values in
	 * @return original list with all valid placeholders set to the correct values if the list contains any valid placeholders
	 */
	public static List<String> setPlaceholders(Player player, List<String> text) {
		if (text == null) {
			return text;
		}
		
		List<String> temp = new ArrayList<String>();
		for (String line : text) {
			temp.add(setPlaceholders(player, line));
		}
		
		return temp;
		
	}

}
