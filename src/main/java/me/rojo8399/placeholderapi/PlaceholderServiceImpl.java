/*
 The MIT License (MIT)

 Copyright (c) 2017 Wundero

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */
package me.rojo8399.placeholderapi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextTemplate;
import org.spongepowered.api.text.format.TextColors;

import me.rojo8399.placeholderapi.expansions.Expansion;
import me.rojo8399.placeholderapi.expansions.RelationalExpansion;
import me.rojo8399.placeholderapi.utils.TextUtils;

/**
 * Implement placeholder service - should not need to be replaced but is a
 * service in case someone decides they need to.
 */
public class PlaceholderServiceImpl implements PlaceholderService {

	private static Pattern generatePattern(String openText, String closeText) {
		String o = backslash(openText);
		String c = backslash(closeText);
		String m = backslash(uniqueChars(openText, closeText));
		String inco = "(?:" + o + ")";
		String exc = "[^" + m + " ]";
		String incc = "(?:" + c + ")";
		return Pattern.compile(inco + "(" + exc + "+)" + incc);
	}

	private static String uniqueChars(String a, String b) {
		String out = a;
		for (char c : b.toCharArray()) {
			if (!out.contains("" + c)) {
				out += c;
			}
		}
		return out;
	}

	private static String backslash(String pattern) {
		String out = "";
		boolean skip = false;
		for (Character c : pattern.toCharArray()) {
			if (skip) {
				switch (c) {
				case 'D':
				case 'd':
				case 'S':
				case 's':
				case 'w':
				case 'W':
				case 'b':
				case 'B':
				case 'A':
				case 'G':
				case 'Z':
				case 'z':
				case 'n':
					out += "\\";
				}
				out += c;
				skip = false;
				continue;
			}
			switch (c) {
			case '\\':
			case '[':
			case ']':
			case '(':
			case ')':
			case '$':
			case '^':
			case '+':
			case '?':
			case '*':
			case '{':
			case '}':
			case '|':
			case '.':
			case '=':
			case ':':
			case '>':
			case '<':
			case '!':
			case '-':
				out += "\\";
			}
			out += c;
		}
		return out;
	}

	private static PlaceholderServiceImpl instance; // lazy instantiation

	private PlaceholderServiceImpl() {
	}

	public static PlaceholderServiceImpl get() {
		return instance == null ? instance = new PlaceholderServiceImpl() : instance;
	}

	public boolean refreshPlaceholder(String id) {
		return registry.refresh(id);
	}

	public int refreshAll() {
		return registry.refreshAll();
	}

	/**
	 * Hold the expansions in a separate file
	 */
	private Registry registry = new Registry();

	@Override
	public String replacePlaceholdersLegacy(Player player, String text, String o, String c) {
		return rptl(player, text, generatePattern(o, c));
	}

	@Override
	public String replacePlaceholdersLegacy(Player player, String text, Pattern pattern) {
		return rptl(player, text, pattern);
	}

	private String rptl(Player player, String text, Pattern p) {
		Matcher placeholderMatcher = p.matcher(text);
		while (placeholderMatcher.find()) {
			String total = placeholderMatcher.group();
			String format;
			try {
				format = placeholderMatcher.group(1);
			} catch (Exception e) {
				format = total.substring(1, total.length() - 1);
			}
			if (!format.isEmpty() && format.toLowerCase().startsWith("rel")) {
				continue;
			}
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
			String value = null;
			try {
				value = exp.onPlaceholderRequestLegacy(player, Optional.ofNullable(token));
			} catch (Exception e) {
				if (e instanceof NullPointerException && e.getMessage().equals("null")) {
					// Should theoretically only happen if player is null.
					value = null;
				} else {
					value = "ERROR: " + e.getMessage();
				}
				e.printStackTrace();
			}
			PlaceholderAPIPlugin.getInstance().getLogger()
					.debug("Format: " + format + ", ID: " + id + ", Value : " + value);
			if (value == null) {
				value = total;
			}
			text = text.replace(total, Matcher.quoteReplacement(value));
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
				return StringUtils.join(c.getAuthors(), ", ");
			}

			@Override
			public String getVersion() {
				return c.getVersion().orElse("1.0");
			}

			@Override
			public Object onValueRequest(Player player, Optional<String> token) {
				return function.apply(player, token);
			}

			@Override
			public List<String> getSupportedTokens() {
				return new ArrayList<>();
			}

		};
		return registerPlaceholder(exp);
	}

	/**
	 * Replace placeholders
	 */
	@Override
	public Text replacePlaceholders(Player player, TextTemplate template, Map<String, Object> args) {
		return template.apply(rpt(player, template, args)).build();
	}

	/**
	 * Fill placeholders
	 */
	@Override
	public Map<String, Object> fillPlaceholders(Player player, TextTemplate template) {
		return rpt(player, template, null);
	}

	/*
	 * Replace placeholders then parse value using the function
	 */
	private Map<String, Object> rpt(Player player, TextTemplate template, Map<String, Object> args) {
		if (args == null) {
			args = new HashMap<>();
		}
		// For every existing argument
		for (String a : template.getArguments().keySet()) {
			if (args.containsKey(a)) {
				continue;
			}
			String format = a.toLowerCase();
			if (!format.isEmpty() && format.toLowerCase().startsWith("rel")) {
				// Again, filler string.
				if (!template.getArguments().get(a).isOptional()) {
					args.put(a, template.getOpenArgString() + a + template.getCloseArgString());
				}
				continue;
			}
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
			Text value = null;
			try {
				value = exp.onPlaceholderRequest(player, Optional.ofNullable(token));
			} catch (Exception e) {
				value = Text.of(TextColors.RED, "ERROR: " + e.getMessage());
				e.printStackTrace();
			}

			PlaceholderAPIPlugin.getInstance().getLogger().debug("Format: " + a + ", ID: " + id + ", Value : " + value);
			if (value == null) {
				value = Text.of(template.getOpenArgString() + a + template.getCloseArgString());
			}
			args.put(a, value);
		}
		return args;
	}

	/**
	 * Replace placeholders
	 */
	@Override
	public Text replacePlaceholders(Player player, String text, Pattern pattern) {
		return replacePlaceholders(player, TextUtils.parse(text, pattern));
	}

	/**
	 * Replace placeholders
	 */
	@Override
	public Text replacePlaceholders(Player player, String text, String o, String c) {
		return replacePlaceholders(player, TextUtils.parse(text, generatePattern(o, c)));
	}

	/**
	 * Replace placeholders
	 */
	@Override
	public Text replacePlaceholders(Player player, Text text, String o, String c) {
		return replacePlaceholders(player, TextUtils.toTemplate(text, generatePattern(o, c)));
	}

	/**
	 * Replace placeholders
	 */
	@Override
	public Text replacePlaceholders(Player player, Text text, Pattern pattern) {
		return replacePlaceholders(player, TextUtils.toTemplate(text, pattern));
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * me.rojo8399.placeholderapi.PlaceholderService#registerReloadListener(java
	 * .lang.Runnable, java.util.Optional)
	 */
	@Override
	public void registerReloadListener(Runnable run, Optional<String> placeholder) {
		registry.registerListener(run, placeholder);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see me.rojo8399.placeholderapi.PlaceholderService#
	 * replaceRelationalPlaceholders(org.spongepowered.api.entity.living.player.
	 * Player, org.spongepowered.api.entity.living.player.Player,
	 * java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public Text replaceRelationalPlaceholders(Player one, Player two, String text, String openText, String closeText) {
		return replaceRelationalPlaceholders(one, two, text, generatePattern(openText, closeText));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see me.rojo8399.placeholderapi.PlaceholderService#
	 * replaceRelationalPlaceholders(org.spongepowered.api.entity.living.player.
	 * Player, org.spongepowered.api.entity.living.player.Player,
	 * java.lang.String, java.util.regex.Pattern)
	 */
	@Override
	public Text replaceRelationalPlaceholders(Player one, Player two, String text, Pattern pattern) {
		return replaceRelationalPlaceholders(one, two, TextUtils.parse(text, pattern));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see me.rojo8399.placeholderapi.PlaceholderService#
	 * replaceRelationalPlaceholders(org.spongepowered.api.entity.living.player.
	 * Player, org.spongepowered.api.entity.living.player.Player,
	 * org.spongepowered.api.text.Text, java.lang.String, java.lang.String)
	 */
	@Override
	public Text replaceRelationalPlaceholders(Player one, Player two, Text text, String openText, String closeText) {
		return replaceRelationalPlaceholders(one, two,
				TextUtils.toTemplate(text, generatePattern(openText, closeText)));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see me.rojo8399.placeholderapi.PlaceholderService#
	 * replaceRelationalPlaceholders(org.spongepowered.api.entity.living.player.
	 * Player, org.spongepowered.api.entity.living.player.Player,
	 * org.spongepowered.api.text.Text, java.util.regex.Pattern)
	 */
	@Override
	public Text replaceRelationalPlaceholders(Player one, Player two, Text text, Pattern pattern) {
		return replaceRelationalPlaceholders(one, two, TextUtils.toTemplate(text, pattern));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see me.rojo8399.placeholderapi.PlaceholderService#
	 * replaceRelationalPlaceholders(org.spongepowered.api.entity.living.player.
	 * Player, org.spongepowered.api.entity.living.player.Player,
	 * org.spongepowered.api.text.TextTemplate, java.util.Map)
	 */
	@Override
	public Text replaceRelationalPlaceholders(Player one, Player two, TextTemplate template,
			Map<String, Object> arguments) {
		return template.apply(rrpt(one, two, template, arguments)).build();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * me.rojo8399.placeholderapi.PlaceholderService#fillRelationalPlaceholders(
	 * org.spongepowered.api.entity.living.player.Player,
	 * org.spongepowered.api.entity.living.player.Player,
	 * org.spongepowered.api.text.TextTemplate)
	 */
	@Override
	public Map<String, Object> fillRelationalPlaceholders(Player one, Player two, TextTemplate template) {
		return rrpt(one, two, template, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see me.rojo8399.placeholderapi.PlaceholderService#
	 * replaceRelationalPlaceholdersLegacy(org.spongepowered.api.entity.living.
	 * player.Player, org.spongepowered.api.entity.living.player.Player,
	 * java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public String replaceRelationalPlaceholdersLegacy(Player one, Player two, String text, String openText,
			String closeText) {
		return rrptl(one, two, text, generatePattern(openText, closeText));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see me.rojo8399.placeholderapi.PlaceholderService#
	 * replaceRelationalPlaceholdersLegacy(org.spongepowered.api.entity.living.
	 * player.Player, org.spongepowered.api.entity.living.player.Player,
	 * java.lang.String, java.util.regex.Pattern)
	 */
	@Override
	public String replaceRelationalPlaceholdersLegacy(Player one, Player two, String text, Pattern pattern) {
		return rrptl(one, two, text, pattern);
	}

	private Map<String, Object> rrpt(Player one, Player two, TextTemplate template, Map<String, Object> args) {
		if (args == null) {
			args = new HashMap<>();
		}
		// For every existing argument
		for (String a : template.getArguments().keySet()) {
			if (args.containsKey(a)) {
				continue;
			}
			String format = a.toLowerCase();
			if (!format.isEmpty() && !format.toLowerCase().startsWith("rel")) {
				// Again, filler string.
				if (!template.getArguments().get(a).isOptional()) {
					args.put(a, template.getOpenArgString() + a + template.getCloseArgString());
				}
				continue;
			}
			format = format.replace("rel_", "");
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
			Expansion exp1 = registry.get(id);
			if (!(exp1 instanceof RelationalExpansion)) {
				// Again, filler string.
				if (!template.getArguments().get(a).isOptional()) {
					args.put(a, template.getOpenArgString() + a + template.getCloseArgString());
				}
				continue;
			}
			RelationalExpansion exp = (RelationalExpansion) exp1;
			Text value = null;
			try {
				value = exp.onRelationalRequest(one, two, Optional.ofNullable(token));
			} catch (Exception e) {
				value = Text.of(TextColors.RED, "ERROR: " + e.getMessage());
				e.printStackTrace();
			}
			if (value == null && PlaceholderAPIPlugin.getInstance().getConfig().relationaltoregular) {
				try {
					value = exp.onPlaceholderRequest(two, Optional.ofNullable(token));
				} catch (Exception e) {
					value = Text.of(TextColors.RED, "ERROR: " + e.getMessage());
					e.printStackTrace();
				}
			}
			PlaceholderAPIPlugin.getInstance().getLogger().debug("Format: " + a + ", ID: " + id + ", Value : " + value);
			if (value == null) {
				value = Text.of(template.getOpenArgString() + a + template.getCloseArgString());
			}
			args.put(a, value);
		}
		return args;
	}

	private String rrptl(Player one, Player two, String text, Pattern p) {
		Matcher placeholderMatcher = p.matcher(text);
		while (placeholderMatcher.find()) {
			String total = placeholderMatcher.group();
			String format;
			try {
				format = placeholderMatcher.group(1);
			} catch (Exception e) {
				format = total.substring(1, total.length() - 1);
			}
			if (!format.isEmpty() && !format.toLowerCase().startsWith("rel")) {
				continue;
			}
			format = format.replace("rel_", "");
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
			Expansion exp1 = registry.get(id);
			if (!(exp1 instanceof RelationalExpansion)) {
				continue;
			}
			RelationalExpansion exp = (RelationalExpansion) exp1;
			String value = null;
			try {
				value = exp.onRelationalRequestLegacy(one, two, Optional.ofNullable(token));
			} catch (Exception e) {
				if (e instanceof NullPointerException && e.getMessage().equals("null")) {
					// Should theoretically only happen if player is null.
					value = null;
				} else {
					value = "ERROR: " + e.getMessage();
				}
				e.printStackTrace();
			}
			if (value == null && PlaceholderAPIPlugin.getInstance().getConfig().relationaltoregular) {
				try {
					value = exp.onPlaceholderRequestLegacy(two, Optional.ofNullable(token));
				} catch (Exception e) {
					if (e instanceof NullPointerException && e.getMessage().equals("null")) {
						// Should theoretically only happen if player is null.
						value = null;
					} else {
						value = "ERROR: " + e.getMessage();
					}
					e.printStackTrace();
				}
			}
			PlaceholderAPIPlugin.getInstance().getLogger()
					.debug("Format: " + format + ", ID: " + id + ", Value : " + value);
			if (value == null) {
				value = total;
			}
			text = text.replace(total, Matcher.quoteReplacement(value));
		}
		return text;
	}
}
