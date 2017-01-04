package me.rojo8399.placeholderapi;

import java.util.Optional;
import java.util.function.BiFunction;

import org.spongepowered.api.entity.living.player.Player;

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
	public String replacePlaceholders(Player player, String text);

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
	public boolean registerPlaceholder(Object plugin, BiFunction<Player, Optional<String>, String> function);

}
