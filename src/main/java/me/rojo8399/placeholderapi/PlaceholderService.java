package me.rojo8399.placeholderapi;

import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

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
	 * @return The parsed text.
	 */
	public Text replacePlaceholders(Player player, String text, String openText, String closeText);

	/**
	 * Replace all placeholders in a texttemplate.
	 * 
	 * @param player
	 *            The player to parse with respect to.
	 * @param template
	 *            The template to parse.
	 * @return The parsed text.
	 */
	public Text replacePlaceholders(Player player, TextTemplate template);

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
	 *            The opening arguments
	 * @return The parsed text.
	 */
	public String replacePlaceholdersLegacy(Player player, String text, String openText, String closeText);

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
