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
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextTemplate;

import com.google.common.reflect.TypeToken;

import me.rojo8399.placeholderapi.impl.placeholder.Expansion;
import me.rojo8399.placeholderapi.impl.utils.TextUtils;
import me.rojo8399.placeholderapi.impl.utils.TypeUtils;

public interface PlaceholderService {

	Pattern DEFAULT_PATTERN = Pattern.compile("[%{]([^ {}%]+)[}%]");

	/**
	 * Create a new ExpansionBuilder to build an expansion.
	 * 
	 * ExpansionBuilder is a fluent, resettable builder, which allows you to chain
	 * method calls and then build.
	 * 
	 * This is the recommended way of creating and registering expansions.
	 * 
	 * See ExpansionBuilder method docs for more information.
	 * 
	 * @return the expansion builder.
	 */
	<S, O, V> ExpansionBuilder<S, O, V, ? extends ExpansionBuilder<S, O, V, ?>> builder(
			Class<? extends S> source, Class<? extends O> observer, Class<? extends V> value);

	/**
	 * Fill a map with placeholders from a template.
	 * 
	 * @param template
	 *            The template to draw the values from. Any non-optional values
	 *            which cannot be parsed will be filled with the string
	 *            representation of the template argument.
	 * @param source
	 *            The source to draw placeholders from. This object must be of the
	 *            type CommandSource, Locatable, User, or any subtypes of those
	 *            classes. Any other object type will throw an exception.
	 * @param observer
	 *            The observer of the placeholders drawn from the source. For
	 *            example, "distance" would be the distance from the source to the
	 *            observer, and "visible" would be if the observer can see the
	 *            source. This object must be of the type CommandSource, Locatable,
	 *            User, or any subtypes of those classes. Any other object type will
	 *            throw an exception.
	 * @return The filled map.
	 */
	Map<String, Object> fillPlaceholders(TextTemplate template, Object source, Object observer);

	/**
	 * Gets the default placeholder pattern for use with other parsers. Group 1 is
	 * the group that matches to placeholders. There are no other capturing groups.
	 * This is the pattern used if no pattern is specified for the other methods.
	 * 
	 * @return The default pattern.
	 */
	default Pattern getDefaultPattern() {
		return DEFAULT_PATTERN;
	}

	/**
	 * Check to see if a placeholder id has been registered.
	 * 
	 * @param id
	 *            The id of the placeholder to check.
	 * @return Whether the placeholder has been registered.
	 */
	boolean isRegistered(String id);

	/**
	 * Create an expansion builder based off of the '@Placeholder' annotated method
	 * with the provided id.
	 * 
	 * This method returns a builder that is ready to register. This means that the
	 * builder has the id, plugin and function fields filled in, and can therefor be
	 * built and registered immediately.
	 * 
	 * @return the expansion builder.
	 */
	ExpansionBuilder<?, ?, ?, ? extends ExpansionBuilder<?, ?, ?, ?>> load(Object handle, String id, Object plugin);

	/**
	 * Create new ExpansionBuilders for all methods annotated with '@Placeholder' in
	 * an object.
	 * 
	 * This is equivalent to calling load(handle, id, plugin) on all methods in an
	 * object, but much faster.
	 * 
	 * Every builder is ready to register upon reception. This means that all
	 * builders have the necessary id, function and plugin fields filled in, and all
	 * that is left are the optional fields.
	 * 
	 * This method is likely to be the one primarily used by developers registering
	 * placeholders. Since the annotated method system is more powerful, this method
	 * allows quick use of that system and provides a streamable list of builders.
	 * The only downside is that it takes a bit more effort to link builders to
	 * their optional fields, but a simple switch statement in a stream map works
	 * well.
	 * 
	 * @param handle
	 *            The object from which to draw parsing methods.
	 * @param plugin
	 *            The plugin which holds the placeholders.
	 * @return The list of builders matching the methods.
	 */
	List<? extends ExpansionBuilder<?, ?, ?, ? extends ExpansionBuilder<?, ?, ?, ?>>> loadAll(Object handle, Object plugin);

	/**
	 * Parse a placeholder.
	 * 
	 * @param placeholder
	 *            The placeholder to parse.
	 * @param source
	 *            The source to draw placeholders from. This object must be of the
	 *            type CommandSource, Locatable, User, or any subtypes of those
	 *            classes. Any other object type will throw an exception.
	 * @param observer
	 *            The observer of the placeholders drawn from the source. For
	 *            example, "distance" would be the distance from the source to the
	 *            observer, and "visible" would be if the observer can see the
	 *            source. This object must be of the type CommandSource, Locatable,
	 *            User, or any subtypes of those classes. Any other object type will
	 *            throw an exception.
	 * @return The parsed object.
	 */
	Object parse(String placeholder, Object source, Object observer);

	/**
	 * Parse a placeholder.
	 * 
	 * @param placeholder
	 *            The placeholder to parse.
	 * @param source
	 *            The source to draw placeholders from. This object must be of the
	 *            type CommandSource, Locatable, User, or any subtypes of those
	 *            classes. Any other object type will throw an exception.
	 * @param observer
	 *            The observer of the placeholders drawn from the source. For
	 *            example, "distance" would be the distance from the source to the
	 *            observer, and "visible" would be if the observer can see the
	 *            source. This object must be of the type CommandSource, Locatable,
	 *            User, or any subtypes of those classes. Any other object type will
	 *            throw an exception.
	 * @param expected
	 *            The expected class. If possible, this will try to cast the
	 *            returned parsed object to the provided class. If said class is the
	 *            Text class, it will parse the object into a text object
	 *            automatically. If it is the String class, it will return the
	 *            object's toString method. Any other objects will just be casted.
	 * @return The parsed object, if available.
	 */
	default <T> Optional<T> parse(String placeholder, Object source, Object observer, Class<T> expected) {
		return TypeUtils.tryCast(parse(placeholder, source, observer), expected);
	}

	/**
	 * Parse a list of placeholders.
	 * 
	 * @param placeholders
	 *            The placeholders to parse.
	 * @param source
	 *            The source to draw placeholders from. This object must be of the
	 *            type CommandSource, Locatable, User, or any subtypes of those
	 *            classes. Any other object type will throw an exception.
	 * @param observer
	 *            The observer of the placeholders drawn from the source. For
	 *            example, "distance" would be the distance from the source to the
	 *            observer, and "visible" would be if the observer can see the
	 *            source. This object must be of the type CommandSource, Locatable,
	 *            User, or any subtypes of those classes. Any other object type will
	 *            throw an exception.
	 * @return The parsed placeholders mapped to their identifiers.
	 */
	default Map<String, Object> parseAll(List<String> placeholders, Object source, Object observer) {
		return placeholders.stream().collect(Collectors.toMap(s -> s, s -> parse(s, source, observer)));
	}

	/**
	 * Parse a placeholder.
	 * 
	 * @param placeholder
	 *            The placeholder to parse.
	 * @param source
	 *            The source to draw placeholders from. This object must be of the
	 *            type CommandSource, Locatable, User, or any subtypes of those
	 *            classes. Any other object type will throw an exception.
	 * @param observer
	 *            The observer of the placeholders drawn from the source. For
	 *            example, "distance" would be the distance from the source to the
	 *            observer, and "visible" would be if the observer can see the
	 *            source. This object must be of the type CommandSource, Locatable,
	 *            User, or any subtypes of those classes. Any other object type will
	 *            throw an exception.
	 * @param expected
	 *            The expected class. If possible, this will try to cast the
	 *            returned parsed object to the provided class. If said class is the
	 *            Text class, it will parse the object into a text object
	 *            automatically. If it is the String class, it will return the
	 *            object's toString method. Any other objects will just be casted.
	 * @return The parsed object.
	 */
	default <T> T parseNullable(String placeholder, Object source, Object observer, Class<T> expected) {
		return parse(placeholder, source, observer, expected).orElse(null);
	}

	/**
	 * Register an Expansion.
	 * 
	 * This method is not preferred as the builder allows standardization of most
	 * features and, more importantly, both prevents many errors and simplifies the
	 * creation process. The builder also allows you to register your placeholder
	 * upon creation, which makes this not a very useful method. It is included in
	 * case you want to create an expansion then modify it heavily.
	 * 
	 * @param expansion
	 *            The expansion to attempt to register.
	 * 
	 * @return whether the expansion was successfully registered.
	 */
	boolean registerExpansion(Expansion<?, ?, ?> expansion);

	/**
	 * Register a deserializer for type T to the plugin.
	 * 
	 * This will, on parsing a token, attempt to deserialize the token into the
	 * type. If this throws an exception or returns null, the plugin will move on to
	 * the next deserializer.
	 * 
	 * NOTE: There is no reason to provide a Text deserializer or a primitive type
	 * (basic numbers + char and boolean) deserializer as those are handled by the
	 * plugin by default.
	 */
	<T> void registerTypeDeserializer(TypeToken<T> token, Function<String, T> deserializer);

	/**
	 * Replace placeholders in a text with parsed values.
	 * 
	 * @param text
	 *            The string to draw values from. This will replace any placeholders
	 *            found by the pattern.
	 * @param source
	 *            The source to draw placeholders from. This object must be of the
	 *            type CommandSource, Locatable, User, or any subtypes of those
	 *            classes. Any other object type will throw an exception.
	 * @param observer
	 *            The observer of the placeholders drawn from the source. For
	 *            example, "distance" would be the distance from the source to the
	 *            observer, and "visible" would be if the observer can see the
	 *            source. This object must be of the type CommandSource, Locatable,
	 *            User, or any subtypes of those classes. Any other object type will
	 *            throw an exception.
	 * @return The parsed text.
	 */
	default Text replacePlaceholders(String text, Object source, Object observer) {
		return replacePlaceholders(text, source, observer, DEFAULT_PATTERN);
	}

	/**
	 * Replace placeholders in a string with parsed values.
	 * 
	 * @param text
	 *            The string to draw values from. This will replace any placeholders
	 *            found by the pattern.
	 * @param source
	 *            The source to draw placeholders from. This object must be of the
	 *            type CommandSource, Locatable, User, or any subtypes of those
	 *            classes. Any other object type will throw an exception.
	 * @param observer
	 *            The observer of the placeholders drawn from the source. For
	 *            example, "distance" would be the distance from the source to the
	 *            observer, and "visible" would be if the observer can see the
	 *            source. This object must be of the type CommandSource, Locatable,
	 *            User, or any subtypes of those classes. Any other object type will
	 *            throw an exception.
	 * @param pattern
	 *            The pattern to match placeholders with. Placeholders will be
	 *            matched to group 1, so any other groups ahead of where you want
	 *            the placeholders in the pattern should be non-capturing.
	 * @return The parsed text.
	 */
	default Text replacePlaceholders(String text, Object source, Object observer, Pattern pattern) {
		return replacePlaceholders(TextUtils.parse(text, pattern), source, observer);
	}

	/**
	 * Replace placeholders in a text with parsed values.
	 * 
	 * @param text
	 *            The text to draw values from. This will replace any placeholders
	 *            found by the pattern.
	 * @param source
	 *            The source to draw placeholders from. This object must be of the
	 *            type CommandSource, Locatable, User, or any subtypes of those
	 *            classes. Any other object type will throw an exception.
	 * @param observer
	 *            The observer of the placeholders drawn from the source. For
	 *            example, "distance" would be the distance from the source to the
	 *            observer, and "visible" would be if the observer can see the
	 *            source. This object must be of the type CommandSource, Locatable,
	 *            User, or any subtypes of those classes. Any other object type will
	 *            throw an exception.
	 * @return The parsed text.
	 */
	default Text replacePlaceholders(Text text, Object source, Object observer) {
		return replacePlaceholders(text, source, observer, DEFAULT_PATTERN);
	}

	/**
	 * Replace placeholders in a text with parsed values.
	 * 
	 * @param text
	 *            The text to draw values from. This will replace any placeholders
	 *            found by the pattern.
	 * @param source
	 *            The source to draw placeholders from. This object must be of the
	 *            type CommandSource, Locatable, User, or any subtypes of those
	 *            classes. Any other object type will throw an exception.
	 * @param observer
	 *            The observer of the placeholders drawn from the source. For
	 *            example, "distance" would be the distance from the source to the
	 *            observer, and "visible" would be if the observer can see the
	 *            source. This object must be of the type CommandSource, Locatable,
	 *            User, or any subtypes of those classes. Any other object type will
	 *            throw an exception.
	 * @param pattern
	 *            The pattern to match placeholders with. Placeholders will be
	 *            matched to group 1, so any other groups ahead of where you want
	 *            the placeholders in the pattern should be non-capturing.
	 * @return The parsed text.
	 */
	default Text replacePlaceholders(Text text, Object source, Object observer, Pattern pattern) {
		return replacePlaceholders(TextUtils.toTemplate(text, pattern), source, observer);
	}

	/**
	 * Replace placeholders in a template with parsed values.
	 * 
	 * @param template
	 *            The template to draw the values from. Any non-optional values
	 *            which cannot be parsed will be replaced with the string version of
	 *            the template argument.
	 * @param source
	 *            The source to draw placeholders from. This object must be of the
	 *            type CommandSource, Locatable, User, or any subtypes of those
	 *            classes. Any other object type will throw an exception.
	 * @param observer
	 *            The observer of the placeholders drawn from the source. For
	 *            example, "distance" would be the distance from the source to the
	 *            observer, and "visible" would be if the observer can see the
	 *            source. This object must be of the type CommandSource, Locatable,
	 *            User, or any subtypes of those classes. Any other object type will
	 *            throw an exception.
	 * @return The parsed text.
	 */
	Text replacePlaceholders(TextTemplate template, Object source, Object observer);

	/**
	 * Replace placeholders in a text with parsed values.
	 * 
	 * @param text
	 *            The string to draw values from. This will replace any placeholders
	 *            found by the pattern.
	 * @param source
	 *            The source to draw placeholders from. This object must be of the
	 *            type CommandSource, Locatable, User, or any subtypes of those
	 *            classes. Any other object type will throw an exception. If there
	 *            are relational placeholders to be parsed, source will be used as
	 *            both the source and the observer.
	 * @return The parsed text.
	 */
	default Text replaceSourcePlaceholders(String text, Object source) {
		return replaceSourcePlaceholders(text, source, DEFAULT_PATTERN);
	}

	/**
	 * Replace placeholders in a text with parsed values.
	 * 
	 * @param text
	 *            The string to draw values from. This will replace any placeholders
	 *            found by the pattern.
	 * @param source
	 *            The source to draw placeholders from. This object must be of the
	 *            type CommandSource, Locatable, User, or any subtypes of those
	 *            classes. Any other object type will throw an exception. If there
	 *            are relational placeholders to be parsed, source will be used as
	 *            both the source and the observer.
	 * @param pattern
	 *            The pattern to match placeholders with. Placeholders will be
	 *            matched to group 1, so any other groups ahead of where you want
	 *            the placeholders in the pattern should be non-capturing.
	 * @return The parsed text.
	 */
	default Text replaceSourcePlaceholders(String text, Object source, Pattern pattern) {
		return replacePlaceholders(text, source, source, pattern);
	}

	/**
	 * Replace placeholders in a text with parsed values.
	 * 
	 * @param text
	 *            The text to draw values from. This will replace any placeholders
	 *            found by the pattern.
	 * @param source
	 *            The source to draw placeholders from. This object must be of the
	 *            type CommandSource, Locatable, User, or any subtypes of those
	 *            classes. Any other object type will throw an exception. If there
	 *            are relational placeholders to be parsed, source will be used as
	 *            both the source and the observer.
	 * @return The parsed text.
	 */
	default Text replaceSourcePlaceholders(Text text, Object source) {
		return replaceSourcePlaceholders(text, source, DEFAULT_PATTERN);
	}

	/**
	 * Replace placeholders in a text with parsed values.
	 * 
	 * @param text
	 *            The text to draw values from. This will replace any placeholders
	 *            found by the pattern.
	 * @param source
	 *            The source to draw placeholders from. This object must be of the
	 *            type CommandSource, Locatable, User, or any subtypes of those
	 *            classes. Any other object type will throw an exception. If there
	 *            are relational placeholders to be parsed, source will be used as
	 *            both the source and the observer.
	 * @param pattern
	 *            The pattern to match placeholders with. Placeholders will be
	 *            matched to group 1, so any other groups ahead of where you want
	 *            the placeholders in the pattern should be non-capturing.
	 * @return The parsed text.
	 */
	default Text replaceSourcePlaceholders(Text text, Object source, Pattern pattern) {
		return replacePlaceholders(text, source, source, pattern);
	}

	/**
	 * Replace placeholders in a template with parsed values.
	 * 
	 * @param template
	 *            The template to draw the values from. Any non-optional values
	 *            which cannot be parsed will be replaced with the string version of
	 *            the template argument.
	 * @param source
	 *            The source to draw placeholders from. This object must be of the
	 *            type CommandSource, Locatable, User, or any subtypes of those
	 *            classes. Any other object type will throw an exception. If there
	 *            are relational placeholders to be parsed, source will be used as
	 *            both the source and the observer.
	 * @return The parsed text.
	 */
	default Text replaceSourcePlaceholders(TextTemplate template, Object source) {
		return replacePlaceholders(template, source, source);
	}

	/**
	 * Verify that the object provided matches the valid types of CommandSource,
	 * Locatable or User. In the future these types may change, so using this method
	 * is preferred if you do not know the type or if the type you are going to
	 * provide is correct. This method is identical to verifySource, but is provided
	 * for the sake of differentiation and potential change in the future.
	 * 
	 * @param target
	 *            The object to verify.
	 * @return Whether the object is valid.
	 */
	default boolean verifyObserver(Object target) {
		return verifySource(target);
	}

	/**
	 * Verify that the object provided matches the valid types of CommandSource,
	 * Locatable or User. In the future these types may change, so using this method
	 * is preferred if you do not know the type or if the type you are going to
	 * provide is correct.
	 * 
	 * @param source
	 *            The object to verify.
	 * @return Whether the object is valid.
	 */
	boolean verifySource(Object source);

}
