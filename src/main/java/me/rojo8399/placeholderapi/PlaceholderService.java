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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextTemplate;

import me.rojo8399.placeholderapi.expansions.Expansion;

public interface PlaceholderService {

	/**
	 * Replace all placeholders in a string.
	 * 
	 * @param player
	 *            The player to parse with respect to.
	 * @param text
	 *            The text to parse.
	 * @return The parsed text.
	 */
	public default Text replacePlaceholders(Player player, String text) {
		return replacePlaceholders(player, text, "%", "%");
	}

	/**
	 * Replace all placeholders in a string.
	 * 
	 * @param player
	 *            The player to parse with respect to.
	 * @param text
	 *            The text to parse.
	 * @param openText
	 *            The opening string to prefix a placeholder
	 * @param closeText
	 *            The closing string to suffix a placeholder
	 * @return The parsed text.
	 */
	public Text replacePlaceholders(Player player, String text, String openText, String closeText);

	/**
	 * Replace all placeholders in a string.
	 * 
	 * @param player
	 *            The player to parse with respect to.
	 * @param text
	 *            The text to parse.
	 * @param pattern
	 *            The pattern to match against. The parser assumes regular
	 *            expression group 1 is the group that gets the whole
	 *            placeholder. You can use non-capturing groups (?:...) to have
	 *            matching groups ahead of the group. If there are no groups,
	 *            the parser assumes one special character on either end of the
	 *            whole matched string is not part of the placeholder and the
	 *            rest is.
	 * @return The parsed text.
	 */
	public Text replacePlaceholders(Player player, String text, Pattern pattern);

	/**
	 * Replace all placeholders in a string.
	 * 
	 * @param player
	 *            The player to parse with respect to.
	 * @param text
	 *            The text to parse.
	 * @param openText
	 *            The opening string to prefix a placeholder
	 * @param closeText
	 *            The closing string to suffix a placeholder
	 * @return The parsed text.
	 */
	public Text replacePlaceholders(Player player, Text text, String openText, String closeText);

	/**
	 * Replace all placeholders in a string.
	 * 
	 * @param player
	 *            The player to parse with respect to.
	 * @param text
	 *            The text to parse.
	 * @param pattern
	 *            The pattern to match against. The parser assumes regular
	 *            expression group 1 is the group that gets the whole
	 *            placeholder. You can use non-capturing groups (?:...) to have
	 *            matching groups ahead of the group. If there are no groups,
	 *            the parser assumes one special character on either end of the
	 *            whole matched string is not part of the placeholder and the
	 *            rest is.
	 * @return The parsed text.
	 */
	public Text replacePlaceholders(Player player, Text text, Pattern pattern);

	/**
	 * Replace all placeholders in a texttemplate.
	 * 
	 * @param player
	 *            The player to parse with respect to.
	 * @param template
	 *            The template to parse.
	 * @return The parsed text.
	 */
	public default Text replacePlaceholders(Player player, TextTemplate template) {
		return replacePlaceholders(player, template, new HashMap<>());
	}

	/**
	 * Replace all placeholders in a texttemplate.
	 * 
	 * @param player
	 *            The player to parse with respect to.
	 * @param template
	 *            The template to parse.
	 * @return The parsed text.
	 */
	public Text replacePlaceholders(Player player, TextTemplate template, Map<String, Object> arguments);

	/**
	 * Fill a map with placeholder replacements for a player.
	 * 
	 * @param player
	 *            The player to parse with respect to.
	 * @param template
	 *            The template from which to take placeholders
	 * @return the arguments that will fill the template.
	 */
	public Map<String, Object> fillPlaceholders(Player player, TextTemplate template);

	/**
	 * Replace all placeholders in a string.
	 * 
	 * @param player
	 *            The player to parse with respect to.
	 * @param text
	 *            The text to parse.
	 * @return The parsed text.
	 */
	public default String replacePlaceholdersLegacy(Player player, String text) {
		return replacePlaceholdersLegacy(player, text, "%", "%");
	}

	/**
	 * Replace all placeholders in a string.
	 * 
	 * @param player
	 *            The player to parse with respect to.
	 * @param text
	 *            The text to parse.
	 * @param openText
	 *            The opening string to prefix a placeholder
	 * @param closeText
	 *            The closing string to suffix a placeholder
	 * @return The parsed text.
	 */
	public String replacePlaceholdersLegacy(Player player, String text, String openText, String closeText);

	/**
	 * Replace all placeholders in a string.
	 * 
	 * @param player
	 *            The player to parse with respect to.
	 * @param text
	 *            The text to parse.
	 * @param pattern
	 *            The pattern to match against. The parser assumes regular
	 *            expression group 1 is the group that gets the whole
	 *            placeholder. You can use non-capturing groups (?:...) to have
	 *            matching groups ahead of the group. If there are no groups,
	 *            the parser assumes one special character on either end of the
	 *            whole matched string is not part of the placeholder and the
	 *            rest is.
	 * @return The parsed text.
	 */
	public String replacePlaceholdersLegacy(Player player, String text, Pattern pattern);

	/**
	 * Return the Expansion represented by a key.
	 * 
	 * @param the
	 *            identifier of the expansion.
	 * 
	 * @return the expansion, if present.
	 */
	public Optional<Expansion> getExpansion(String id);

	/**
	 * Return all registered expansions.
	 * 
	 * @return the set of expansions.
	 */
	public Set<Expansion> getExpansions();

	/**
	 * Register a placeholder.
	 * 
	 * @param expansion
	 *            The placeholder to register.
	 * @return Whether the placeholder was successfully registered.
	 */
	public boolean registerPlaceholder(Expansion expansion);

	/**
	 * Register a placholder.
	 * 
	 * @param plugin
	 *            The owner of the placeholder.
	 * @param function
	 *            The function that parses placeholders.
	 * @return Whether the placeholder was successfully registered.
	 */
	public boolean registerPlaceholder(Object plugin, BiFunction<Player, Optional<String>, Text> function);

}
