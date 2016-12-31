package me.rojo8399.placeholderapi.expansions;

import org.spongepowered.api.entity.living.player.Player;

public interface Expansion {

	/**
	 * If any requirements are required to be checked before this hook can
	 * register, add them here
	 * 
	 * @return true if this hook meets all the requirements to register
	 */
	public boolean canRegister();

	/**
	 * Get the identifier that this placeholder expansion uses to be passed
	 * placeholder requests
	 * 
	 * @return placeholder identifier that is associated with this class
	 */
	public String getIdentifier();

	/**
	 * Get the author of this PlaceholderExpansion
	 * 
	 * @return name of the author for this expansion
	 */
	public String getAuthor();

	/**
	 * Get the version of this PlaceholderExpansion
	 * 
	 * @return current version of this expansion
	 */
	public String getVersion();

	/**
	 * Parse the token for the player
	 * 
	 * @return the result of the parse
	 */
	public String onPlaceholderRequest(Player player, String token);

}
