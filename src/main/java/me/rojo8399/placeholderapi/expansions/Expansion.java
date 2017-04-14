package me.rojo8399.placeholderapi.expansions;

import java.net.URL;
import java.util.List;
import java.util.Optional;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

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
	 * Get all supported tokens. Null token means just the parent token.
	 * 
	 * @return the supported tokens.
	 */
	public List<String> getSupportedTokens();

	/**
	 * Get the website for this expansion.
	 * 
	 * @return the URL for this expansion
	 */
	public default URL getURL() {
		return null;
	}

	/**
	 * Get the description for this expansion.
	 * 
	 * @return the description.
	 */
	public default String getDescription() {
		return null;
	}

	/**
	 * Parse the token for the player
	 * 
	 * @return the result of the parse as a text. If strings need to be
	 *         converted to text, use the parser.
	 */
	public Text onPlaceholderRequest(Player player, Optional<String> token);

	/**
	 * Parse the token for the player
	 * 
	 * @return the result of the parse as a string created with the text parser
	 */
	public default String onPlaceholderRequestLegacy(Player player, Optional<String> token) {
		Text t = onPlaceholderRequest(player, token);
		if (t == null) {
			return null;
		}
		return TextSerializers.FORMATTING_CODE.serialize(t);
	}

}
