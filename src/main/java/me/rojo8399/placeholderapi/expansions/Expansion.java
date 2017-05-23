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
package me.rojo8399.placeholderapi.expansions;

import java.net.URL;
import java.util.List;
import java.util.Optional;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import me.rojo8399.placeholderapi.utils.TypeUtils;

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
	 * Parse the token for the player.
	 * 
	 * @return the value, if it matches the class.
	 */
	public default <T> Optional<T> onValueRequest(Player player, Optional<String> token, Class<T> expected) {
		Object val = onValueRequest(player, token);
		if (val == null) {
			return Optional.empty();
		}
		if (expected == null) {
			throw new IllegalArgumentException(
					"Must provide an expected class! If you do not know which class to use, use onValueRequest(player, token) instead.");
		}
		if (Text.class.isAssignableFrom(expected)) {
			if (val instanceof Text) {
				return TypeUtils.tryOptional(() -> expected.cast(val));
			} else {
				return TypeUtils.tryOptional(
						() -> expected.cast(TextSerializers.FORMATTING_CODE.deserialize(String.valueOf(val))));
			}
		}
		return TypeUtils.tryOptional(() -> expected.cast(val));
	}

	/**
	 * Parse the token for the player
	 * 
	 * @return the value, as an object
	 */
	public default Object onValueRequest(Player player, Optional<String> token) {
		return onPlaceholderRequestLegacy(player, token);
	}

	/**
	 * Parse the token for the player
	 * 
	 * @return the result of the parse as a text.
	 */
	public Text onPlaceholderRequest(Player player, Optional<String> token);

	/**
	 * Parse the token for the player
	 * 
	 * @return the result of the parse as a string
	 */
	public default String onPlaceholderRequestLegacy(Player player, Optional<String> token) {
		Text t = onPlaceholderRequest(player, token);
		if (t == null) {
			return null;
		}
		return TextSerializers.FORMATTING_CODE.serialize(t);
	}

}
