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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextTemplate;

import me.rojo8399.placeholderapi.utils.TextUtils;
import me.rojo8399.placeholderapi.utils.TypeUtils;

public interface PlaceholderService {

	/**
	 * Register placeholder methods in an object with the API.
	 * 
	 * Methods denoted with the "@Placeholder" annotation will be parsed to see
	 * if they support placeholder replacing. If they do, they will be loaded in
	 * as placeholders. See the documentation for "@Placeholder", "@Relational",
	 * "@Source", "@Observer", "@Token" and "@Listening" for more information on
	 * configuring a placeholder.
	 * 
	 * @param loader
	 *            A supplier to provide the object in which the methods reside.
	 *            This will be called on method call, and every time
	 *            PlaceholderAPI or a placeholder held by the object is
	 *            reloaded.
	 * @param plugin
	 *            The plugin which holds the placeholder.
	 */
	public void registerPlaceholders(Supplier<Object> loader, Object plugin);

	/**
	 * Register placeholder methods in an object with the API.
	 * 
	 * Methods denoted with the "@Placeholder" annotation will be parsed to see
	 * if they support placeholder replacing. If they do, they will be loaded in
	 * as placeholders. See the documentation for "@Placeholder", "@Relational",
	 * "@Source", "@Observer", "@Token" and "@Listening" for more information on
	 * configuring a placeholder.
	 * 
	 * @param object
	 *            The object in which the methods reside. On reloading
	 *            PlaceholderAPI or any placeholder within this object, the same
	 *            object will be re-added to the register. Note that this should
	 *            reload any fields with the "@Setting" annotation in a
	 *            configurable placeholder, however it does nothing else to the
	 *            object. Note that the object should be final or effectively
	 *            final.
	 * @param plugin
	 *            The plugin which holds the placeholder.
	 */
	public default void registerPlaceholders(final Object object, Object plugin) {
		registerPlaceholders(() -> object, plugin);
	}

	/**
	 * Fill a map with placeholders from a template.
	 * 
	 * @param template
	 *            The template to draw the values from. Any non-optional values
	 *            which cannot be parsed will be should be filled with the
	 *            string representation of the template argument.
	 * @param source
	 *            The source to draw placeholders from. This object must be of
	 *            the type CommandSource, Locatable, User, or any subtypes of
	 *            those classes. Any other object type will throw an exception.
	 * @param observer
	 *            The observer of the placeholders drawn from the source. For
	 *            example, "distance" would be the distance from the source to
	 *            the observer, and "visible" would be if the observer can see
	 *            the source. This object must be of the type CommandSource,
	 *            Locatable, User, or any subtypes of those classes. Any other
	 *            object type will throw an exception.
	 * @return The filled map.
	 */
	public Map<String, Object> fillPlaceholders(TextTemplate template, Object source, Object observer);

	/**
	 * Parse a placeholder.
	 * 
	 * @param placeholder
	 *            The placeholder to parse.
	 * @param source
	 *            The source to draw placeholders from. This object must be of
	 *            the type CommandSource, Locatable, User, or any subtypes of
	 *            those classes. Any other object type will throw an exception.
	 * @param observer
	 *            The observer of the placeholders drawn from the source. For
	 *            example, "distance" would be the distance from the source to
	 *            the observer, and "visible" would be if the observer can see
	 *            the source. This object must be of the type CommandSource,
	 *            Locatable, User, or any subtypes of those classes. Any other
	 *            object type will throw an exception.
	 * @return The parsed object.
	 */
	public Object parse(String placeholder, Object source, Object observer);

	/**
	 * Parse a list of placeholders.
	 * 
	 * @param placeholders
	 *            The placeholders to parse.
	 * @param source
	 *            The source to draw placeholders from. This object must be of
	 *            the type CommandSource, Locatable, User, or any subtypes of
	 *            those classes. Any other object type will throw an exception.
	 * @param observer
	 *            The observer of the placeholders drawn from the source. For
	 *            example, "distance" would be the distance from the source to
	 *            the observer, and "visible" would be if the observer can see
	 *            the source. This object must be of the type CommandSource,
	 *            Locatable, User, or any subtypes of those classes. Any other
	 *            object type will throw an exception.
	 * @return The parsed placeholders mapped to their identifiers.
	 */
	public default Map<String, Object> fill(List<String> placeholders, Object source, Object observer) {
		return placeholders.stream().collect(Collectors.toMap(s -> s, s -> parse(s, source, observer)));
	}

	/**
	 * Parse a placeholder.
	 * 
	 * @param placeholder
	 *            The placeholder to parse.
	 * @param source
	 *            The source to draw placeholders from. This object must be of
	 *            the type CommandSource, Locatable, User, or any subtypes of
	 *            those classes. Any other object type will throw an exception.
	 * @param observer
	 *            The observer of the placeholders drawn from the source. For
	 *            example, "distance" would be the distance from the source to
	 *            the observer, and "visible" would be if the observer can see
	 *            the source. This object must be of the type CommandSource,
	 *            Locatable, User, or any subtypes of those classes. Any other
	 *            object type will throw an exception.
	 * @param expected
	 *            The expected class. If possible, this will try to cast the
	 *            returned parsed object to the provided class. If said class is
	 *            the Text class, it will parse the object into a text object
	 *            automatically. If it is the String class, it will return the
	 *            object's toString method. Any other objects will just be
	 *            casted.
	 * @return The parsed object, if available.
	 */
	public default <T> Optional<T> parse(String placeholder, Object source, Object observer, Class<T> expected) {
		return TypeUtils.tryCast(parse(placeholder, source, observer), expected);
	}

	/**
	 * Parse a placeholder.
	 * 
	 * @param placeholder
	 *            The placeholder to parse.
	 * @param source
	 *            The source to draw placeholders from. This object must be of
	 *            the type CommandSource, Locatable, User, or any subtypes of
	 *            those classes. Any other object type will throw an exception.
	 * @param observer
	 *            The observer of the placeholders drawn from the source. For
	 *            example, "distance" would be the distance from the source to
	 *            the observer, and "visible" would be if the observer can see
	 *            the source. This object must be of the type CommandSource,
	 *            Locatable, User, or any subtypes of those classes. Any other
	 *            object type will throw an exception.
	 * @param expected
	 *            The expected class. If possible, this will try to cast the
	 *            returned parsed object to the provided class. If said class is
	 *            the Text class, it will parse the object into a text object
	 *            automatically. If it is the String class, it will return the
	 *            object's toString method. Any other objects will just be
	 *            casted.
	 * @return The parsed object.
	 */
	public default <T> T parseNullable(String placeholder, Object source, Object observer, Class<T> expected) {
		return parse(placeholder, source, observer, expected).orElse(null);
	}

	/**
	 * Replace placeholders in a template with parsed values.
	 * 
	 * @param template
	 *            The template to draw the values from. Any non-optional values
	 *            which cannot be parsed will be replaced with the string
	 *            version of the template argument.
	 * @param source
	 *            The source to draw placeholders from. This object must be of
	 *            the type CommandSource, Locatable, User, or any subtypes of
	 *            those classes. Any other object type will throw an exception.
	 * @param observer
	 *            The observer of the placeholders drawn from the source. For
	 *            example, "distance" would be the distance from the source to
	 *            the observer, and "visible" would be if the observer can see
	 *            the source. This object must be of the type CommandSource,
	 *            Locatable, User, or any subtypes of those classes. Any other
	 *            object type will throw an exception.
	 * @return The parsed text.
	 */
	public Text replacePlaceholders(TextTemplate template, Object source, Object observer);

	/**
	 * Replace placeholders in a text with parsed values.
	 * 
	 * @param text
	 *            The text to draw values from. This will replace any
	 *            placeholders found by the pattern.
	 * @param source
	 *            The source to draw placeholders from. This object must be of
	 *            the type CommandSource, Locatable, User, or any subtypes of
	 *            those classes. Any other object type will throw an exception.
	 * @param observer
	 *            The observer of the placeholders drawn from the source. For
	 *            example, "distance" would be the distance from the source to
	 *            the observer, and "visible" would be if the observer can see
	 *            the source. This object must be of the type CommandSource,
	 *            Locatable, User, or any subtypes of those classes. Any other
	 *            object type will throw an exception.
	 * @param pattern
	 *            The pattern to match placeholders with. Placeholders will be
	 *            matched to group 1, so any other groups ahead of where you
	 *            want the placeholders in the pattern should be non-capturing.
	 * @return The parsed text.
	 */
	public default Text replacePlaceholders(Text text, Object source, Object observer, Pattern pattern) {
		return replacePlaceholders(TextUtils.toTemplate(text, pattern), source, observer);
	}

	/**
	 * Replace placeholders in a string with parsed values.
	 * 
	 * @param text
	 *            The string to draw values from. This will replace any
	 *            placeholders found by the pattern.
	 * @param source
	 *            The source to draw placeholders from. This object must be of
	 *            the type CommandSource, Locatable, User, or any subtypes of
	 *            those classes. Any other object type will throw an exception.
	 * @param observer
	 *            The observer of the placeholders drawn from the source. For
	 *            example, "distance" would be the distance from the source to
	 *            the observer, and "visible" would be if the observer can see
	 *            the source. This object must be of the type CommandSource,
	 *            Locatable, User, or any subtypes of those classes. Any other
	 *            object type will throw an exception.
	 * @param pattern
	 *            The pattern to match placeholders with. Placeholders will be
	 *            matched to group 1, so any other groups ahead of where you
	 *            want the placeholders in the pattern should be non-capturing.
	 * @return The parsed text.
	 */
	public default Text replacePlaceholders(String text, Object source, Object observer, Pattern pattern) {
		return replacePlaceholders(TextUtils.parse(text, pattern), source, observer);
	}

	static final Pattern DEFAULT_PATTERN = Pattern.compile("[%\\{]([^ \\{\\}%]+)[\\}%]");

	/**
	 * Gets the default placeholder pattern for use with other parsers. Group 1
	 * is the group that matches to placeholders. There are no other capturing
	 * groups. This is the pattern used if no pattern is specified for the other
	 * methods.
	 * 
	 * @return The default pattern.
	 */
	public default Pattern getDefaultPattern() {
		return DEFAULT_PATTERN;
	}

	/**
	 * Replace placeholders in a text with parsed values.
	 * 
	 * @param text
	 *            The text to draw values from. This will replace any
	 *            placeholders found by the pattern.
	 * @param source
	 *            The source to draw placeholders from. This object must be of
	 *            the type CommandSource, Locatable, User, or any subtypes of
	 *            those classes. Any other object type will throw an exception.
	 * @param observer
	 *            The observer of the placeholders drawn from the source. For
	 *            example, "distance" would be the distance from the source to
	 *            the observer, and "visible" would be if the observer can see
	 *            the source. This object must be of the type CommandSource,
	 *            Locatable, User, or any subtypes of those classes. Any other
	 *            object type will throw an exception.
	 * @return The parsed text.
	 */
	public default Text replacePlaceholders(Text text, Object source, Object observer) {
		return replacePlaceholders(text, source, observer, DEFAULT_PATTERN);
	}

	/**
	 * Replace placeholders in a text with parsed values.
	 * 
	 * @param text
	 *            The string to draw values from. This will replace any
	 *            placeholders found by the pattern.
	 * @param source
	 *            The source to draw placeholders from. This object must be of
	 *            the type CommandSource, Locatable, User, or any subtypes of
	 *            those classes. Any other object type will throw an exception.
	 * @param observer
	 *            The observer of the placeholders drawn from the source. For
	 *            example, "distance" would be the distance from the source to
	 *            the observer, and "visible" would be if the observer can see
	 *            the source. This object must be of the type CommandSource,
	 *            Locatable, User, or any subtypes of those classes. Any other
	 *            object type will throw an exception.
	 * @return The parsed text.
	 */
	public default Text replacePlaceholders(String text, Object source, Object observer) {
		return replacePlaceholders(text, source, observer, DEFAULT_PATTERN);
	}

	/**
	 * Replace placeholders in a template with parsed values.
	 * 
	 * @param template
	 *            The template to draw the values from. Any non-optional values
	 *            which cannot be parsed will be replaced with the string
	 *            version of the template argument.
	 * @param source
	 *            The source to draw placeholders from. This object must be of
	 *            the type CommandSource, Locatable, User, or any subtypes of
	 *            those classes. Any other object type will throw an exception.
	 *            If there are relational placeholders to be parsed, source will
	 *            be used as both the source and the observer.
	 * @return The parsed text.
	 */
	public default Text replacePlaceholders(TextTemplate template, Object source) {
		return replacePlaceholders(template, source, source);
	}

	/**
	 * Replace placeholders in a text with parsed values.
	 * 
	 * @param text
	 *            The text to draw values from. This will replace any
	 *            placeholders found by the pattern.
	 * @param source
	 *            The source to draw placeholders from. This object must be of
	 *            the type CommandSource, Locatable, User, or any subtypes of
	 *            those classes. Any other object type will throw an exception.
	 *            If there are relational placeholders to be parsed, source will
	 *            be used as both the source and the observer.
	 * @param pattern
	 *            The pattern to match placeholders with. Placeholders will be
	 *            matched to group 1, so any other groups ahead of where you
	 *            want the placeholders in the pattern should be non-capturing.
	 * @return The parsed text.
	 */
	public default Text replacePlaceholders(Text text, Object source, Pattern pattern) {
		return replacePlaceholders(text, source, source, pattern);
	}

	/**
	 * Replace placeholders in a text with parsed values.
	 * 
	 * @param text
	 *            The string to draw values from. This will replace any
	 *            placeholders found by the pattern.
	 * @param source
	 *            The source to draw placeholders from. This object must be of
	 *            the type CommandSource, Locatable, User, or any subtypes of
	 *            those classes. Any other object type will throw an exception.
	 *            If there are relational placeholders to be parsed, source will
	 *            be used as both the source and the observer.
	 * @param pattern
	 *            The pattern to match placeholders with. Placeholders will be
	 *            matched to group 1, so any other groups ahead of where you
	 *            want the placeholders in the pattern should be non-capturing.
	 * @return The parsed text.
	 */
	public default Text replacePlaceholders(String text, Object source, Pattern pattern) {
		return replacePlaceholders(text, source, source, pattern);
	}

	/**
	 * Replace placeholders in a text with parsed values.
	 * 
	 * @param text
	 *            The text to draw values from. This will replace any
	 *            placeholders found by the pattern.
	 * @param source
	 *            The source to draw placeholders from. This object must be of
	 *            the type CommandSource, Locatable, User, or any subtypes of
	 *            those classes. Any other object type will throw an exception.
	 *            If there are relational placeholders to be parsed, source will
	 *            be used as both the source and the observer.
	 * @return The parsed text.
	 */
	public default Text replacePlaceholders(Text text, Object source) {
		return replacePlaceholders(text, source, DEFAULT_PATTERN);
	}

	/**
	 * Replace placeholders in a text with parsed values.
	 * 
	 * @param text
	 *            The string to draw values from. This will replace any
	 *            placeholders found by the pattern.
	 * @param source
	 *            The source to draw placeholders from. This object must be of
	 *            the type CommandSource, Locatable, User, or any subtypes of
	 *            those classes. Any other object type will throw an exception.
	 *            If there are relational placeholders to be parsed, source will
	 *            be used as both the source and the observer.
	 * @return The parsed text.
	 */
	public default Text replacePlaceholders(String text, Object source) {
		return replacePlaceholders(text, source, DEFAULT_PATTERN);
	}

	/**
	 * Check to see if a placeholder id has been registered.
	 * 
	 * @param id
	 *            The id of the placeholder to check.
	 * @return Whether the placeholder has been registered.
	 */
	public boolean isRegistered(String id);

	/**
	 * Verify that the object provided matches the valid types of CommandSource,
	 * Locatable or User. In the future these types may change, so using this
	 * method is preferred if you do not know the type or if the type you are
	 * going to provide is correct.
	 * 
	 * @param source
	 *            The object to verify.
	 * @return Whether the object is valid.
	 */
	public boolean verifySource(Object source);

	/**
	 * Verify that the object provided matches the valid types of CommandSource,
	 * Locatable or User. In the future these types may change, so using this
	 * method is preferred if you do not know the type or if the type you are
	 * going to provide is correct.
	 * 
	 * @param source
	 *            The object to verify.
	 * @return Whether the object is valid.
	 */
	public default boolean verifyObserver(Object target) {
		return verifySource(target);
	}

}
