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

import java.util.List;
import java.util.Optional;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import me.rojo8399.placeholderapi.utils.TypeUtils;

/**
 * @author Wundero
 *
 */
public interface RelationalExpansion extends Expansion {

	@Override
	public default Object onValueRequest(Player player, Optional<String> token) {
		return null;
	}

	@Override
	public default List<String> getSupportedTokens() {
		return null;
	}

	/**
	 * Get all supported relational tokens. Null token means just the parent
	 * token.
	 * 
	 * @return the supported tokens.
	 */
	public List<String> getSupportedRelationalTokens();

	/**
	 * Parse the token for the players, with priority given to player one.
	 * 
	 * @return the value as a Text.
	 */
	public default Text onRelationalRequest(Player one, Player two, Optional<String> token) {
		return onRelationalValueRequest(one, two, token, Text.class).orElse(null);
	}

	/**
	 * Parse the token for the players, with priority given to player one.
	 * 
	 * @return the value as a String with & formatting codes.
	 */
	public default String onRelationalRequestLegacy(Player one, Player two, Optional<String> token) {
		return TextSerializers.FORMATTING_CODE.serialize(onRelationalRequest(one, two, token));
	}

	/**
	 * Parse the token for the players, with priority given to player one.
	 * 
	 * @return the value, if it matches the class.
	 */
	public default <T> Optional<T> onRelationalValueRequest(Player one, Player two, Optional<String> token,
			Class<T> expected) {
		Object val = onRelationalValueRequest(one, two, token);
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
	 * Parse the token for the players, with priority given to player one.
	 * 
	 * Consider calling onValueRequest(two, token) if you are returning null to
	 * allow for recipient parsing.
	 * 
	 * @return the value, as an object
	 */
	public Object onRelationalValueRequest(Player one, Player two, Optional<String> token);
}
