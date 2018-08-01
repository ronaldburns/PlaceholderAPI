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

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import me.rojo8399.placeholderapi.impl.placeholder.Expansion;

/**
 * @author Wundero
 *
 */
public interface ExpansionBuilder<S, O, V, B extends ExpansionBuilder<S, O, V, B>> {
	/**
	 * An interface which represents the parse function to execute. Used as
	 * execution base.
	 * 
	 * @author Wundero
	 *
	 * @param <S>
	 *            The source parameter.
	 * @param <O>
	 *            The observer parameter.
	 * @param <V>
	 *            The value return type.
	 */
	@FunctionalInterface
	interface ExpansionFunction<S, O, V> {
		/**
		 * Parse the placeholder for the provided arguments.
		 * 
		 * @param source
		 *            The source of the placeholder.
		 * @param observer
		 *            The observer of the placeholder.
		 * @param token
		 *            The token describing the placeholder.
		 * @return The parsed value.
		 * @throws Exception
		 *             Thrown if anyting goes wrong.
		 */
		V parse(S source, O observer, Optional<String> token) throws Exception;
	}

	/**
	 * Adds to the list of supported tokens for the expansion.
	 * 
	 * @param tokens
	 *            The supported tokens.
	 * @return This builder.
	 */
	B addTokens(List<String> tokens);

	/**
	 * Adds to the list of supported tokens for the expansion.
	 * 
	 * @param tokens
	 *            The supported tokens.
	 * @return This builder.
	 */
	default B addTokens(String... tokens) {
		return addTokens(Arrays.asList(tokens));
	}

	/**
	 * Sets the author of the expansion.
	 * 
	 * @param author
	 *            The author of the expansion.
	 * @return This builder.
	 */
	B author(String author);

	/**
	 * This method will build the expansion. This is not a terminating operation on
	 * the builder, so if you wish to reuse the builder under a new id, you could do
	 * so.
	 * 
	 * This method will throw an exception if the id, plugin or function have not
	 * been specified.
	 * 
	 * It is recommended NOT to use this method and instead use
	 * {@link ExpansionBuilder#buildAndRegister()}.
	 * 
	 * @return The expansion, if successfully built.
	 * @throws Exception
	 *             If the expansion cannot be created.
	 */
	IExpansion<S, O, V> build() throws Exception;

	/**
	 * This method will build the expansion and then attemp to register the
	 * expansion. This is not a terminating operation on the builder, so if you wish
	 * to reuse the builder under a new id, you could do so.
	 * 
	 * This method will throw an exception if the id, plugin or function have not
	 * been specified.
	 * 
	 * @return Whether the registration was successful.
	 * @throws Exception
	 *             If the expansion cannot be created or registered.
	 */
	boolean buildAndRegister() throws Exception;

	/**
	 * Add an object which holds config values. This object will be populated with
	 * configuration options when the expansion is loaded and reloaded.
	 * 
	 * @param config
	 *            The object to populate.
	 * @return This builder.
	 */
	B config(Object config);

	/**
	 * Execute a function for the parsing of this expansion.
	 * 
	 * @param exec
	 *            The function to execute.
	 * @return This builder.
	 */
	default B consumeDual(BiConsumer<S, O> exec) {
		return function((s, o, t) -> {
			exec.accept(s, o);
			return null;
		});
	}

	/**
	 * Execute a function for the parsing of this expansion.
	 * 
	 * @param exec
	 *            The function to execute.
	 * @return This builder.
	 */
	default B consumeSingle(Consumer<S> exec) {
		return function((s, o, t) -> {
			exec.accept(s);
			return null;
		});
	}

	/**
	 * Execute a function for the parsing of this expansion.
	 * 
	 * @param exec
	 *            The function to execute.
	 * @return This builder.
	 */
	default B consumeSingleToken(BiConsumer<S, Optional<String>> exec) {
		return function((s, o, t) -> {
			exec.accept(s, t);
			return null;
		});
	}

	/**
	 * Execute a function for the parsing of this expansion.
	 * 
	 * @param exec
	 *            The function to execute.
	 * @return This builder.
	 */
	default B consumeToken(Consumer<Optional<String>> exec) {
		return function((s, o, t) -> {
			exec.accept(t);
			return null;
		});
	}

	/**
	 * Utility method for getting the current state of the builder. Mainly just for
	 * the methods in this interface.
	 * 
	 * @return This builder, unchanged.
	 */
	B current();

	/**
	 * Sets the description of the expansion.
	 * 
	 * @param description
	 *            The description of the expansion.
	 * @return This builder.
	 */
	B description(String description);

	/**
	 * Copy settings from the expansion provided in order to modify it.
	 * 
	 * @param exp
	 *            The expansion from which to draw values.
	 * @return This builder, with all fields modified to suit the expansion
	 *         provided.
	 */
	B from(Expansion<S, O, V> exp);

	/**
	 * Create a builder for a method in the object.
	 *
	 * This will attempt to find a method which has the "@Placeholder" annotation
	 * linked to the provided id. If it exists, it will create the builder. This
	 * will attempt to use the relational field to determine if it can parse that
	 * method, which will by default be the normal method. If it does not find that
	 * method, it will check for one which is not relational.
	 * 
	 * This method alters the function, the plugin, the id and the relational
	 * fields. Any other previously existing fields will be conserved.
	 * 
	 * The provided id must exist on at least one method. Any repeated ids will be
	 * ignored unless they are of different relational status.
	 * 
	 * @param handle
	 *            The object containing the placeholder method.
	 * @param id
	 *            The id of the placeholder method.
	 * @param plugin
	 *            The plugin holding the expansion.
	 * @return This builder.
	 */
	B from(Object handle, String id, Object plugin);

	/**
	 * Copy settings from the expansion provided in order to modify it. This method
	 * ignores the types of the provided expansion and attempts to cast them to its
	 * own.
	 * 
	 * @param exp
	 *            The expansion from which to draw values.
	 * @return This builder, with all fields modified to suit the expansion
	 *         provided.
	 * @throws IllegalArgumentException
	 *             If the provided expansion's fields do not match or are not
	 *             subclasses of this builder's fields.
	 */
	B fromUnknown(Expansion<?, ?, ?> exp);

	/**
	 * Execute a function for the parsing of this expansion. ExpansionFunction is a
	 * functional interface, meaning the method code may use lambdas.
	 * 
	 * @param function
	 *            The function to execute.
	 * @return This builder.
	 */
	B function(ExpansionFunction<S, O, V> function);

	/**
	 * Execute a function for the parsing of this expansion.
	 * 
	 * @param exec
	 *            The function to execute.
	 * @return This builder.
	 */
	default B functionDual(BiFunction<S, O, V> exec) {
		return function((s, o, t) -> exec.apply(s, o));
	}

	/**
	 * Execute a function for the parsing of this expansion.
	 * 
	 * @param exec
	 *            The function to execute.
	 * @return This builder.
	 */
	default B functionSingle(Function<S, V> exec) {
		return function((s, o, t) -> exec.apply(s));
	}

	/**
	 * Execute a function for the parsing of this expansion.
	 * 
	 * @param exec
	 *            The function to execute.
	 * @return This builder.
	 */
	default B functionSingleToken(BiFunction<S, Optional<String>, V> exec) {
		return function((s, o, t) -> exec.apply(s, t));
	}

	/**
	 * Execute a function for the parsing of this expansion.
	 * 
	 * @param exec
	 *            The function to execute.
	 * @return This builder.
	 */
	default B functionToken(Function<Optional<String>, V> exec) {
		return function((s, o, t) -> exec.apply(t));
	}

	/**
	 * @return The author of the expansion.
	 */
	String getAuthor();

	/**
	 * @return The description of the expansion.
	 */
	String getDescription();

	/**
	 * @return The id of the expansion.
	 */
	String getId();

	/**
	 * @return The supported tokens for the expansion.
	 */
	List<String> getTokens();

	/**
	 * @return The URL for the expansion.
	 * @throws Exception
	 *             - If the url is not properly formatted or is null.
	 */
	URL getUrl() throws Exception;

	/**
	 * @return The url for the expansion.
	 */
	String getUrlString();

	/**
	 * @return The version of the expansion.
	 */
	String getVersion();

	/**
	 * Set the id of this expansion. This is required to build the expansion and
	 * cannot be null.
	 * 
	 * @param id
	 *            The id to register this expansion under.
	 * @return This builder.
	 */
	B id(String id);

	/**
	 * @return Whether the expansion is relational.
	 */
	boolean isRelational();

	/**
	 * Register listeners via the placeholder. This will attempt to use the provided
	 * plugin object for registration.
	 * 
	 * This listener will be unregistered and then registered again on reload.
	 */
	B listen(Object listeners);

	/**
	 * Set the plugin which holds this expansion. This method is required before
	 * building and cannot accept a null plugin.
	 * 
	 * @param plugin
	 *            The plugin which holds this expansion.
	 * @return This builder.
	 */
	B plugin(Object plugin);

	/**
	 * Execute a function for the parsing of this expansion.
	 * 
	 * @param exec
	 *            The function to execute.
	 * @return This builder.
	 */
	default B provide(Supplier<V> exec) {
		return function((s, o, t) -> exec.get());
	}

	/**
	 * Set whether this expansion is relational. `Relational` means that this
	 * expansion's parse will be called when the id "rel_[id]" is passed into the
	 * service. It also means that, if this expansion's parsing returns null AND the
	 * server's configuration allows it, it will try to return the value of the
	 * placeholder of id "[id]" if it exists for the observer.
	 * 
	 * Setting this to true in no way guarantees that it will use the observer, that
	 * the observer will not be null, or that it requires the observer at all.
	 * 
	 * Conversely, if this is false, that in no way guarantees that it will not use
	 * the observer.
	 * 
	 * @param relational
	 *            Whether this expansion is relational.
	 * @return This builder.
	 */
	B relational(boolean relational);

	/**
	 * Add a function to call upon reload of the placeholder.
	 * 
	 * @param reload
	 *            The function to call when the expansion is reloaded. This is a
	 *            predicate simply because we provide the current state of the
	 *            expansion and require a boolean as to whether the reload was
	 *            successful.
	 * @return This builder.
	 */
	B reloadFunction(Predicate<Expansion<S, O, V>> reload);

	/**
	 * Reset the builder's settings. In this case, it returns a new builder.
	 * 
	 * @return The new builder.
	 */
	B reset();

	/**
	 * Execute a function for the parsing of this expansion.
	 * 
	 * @param exec
	 *            The function to execute.
	 * @return This builder.
	 */
	default B run(Runnable exec) {
		return function((s, o, t) -> {
			exec.run();
			return null;
		});
	}

	/**
	 * Sets the list of supported tokens for the expansion.
	 * 
	 * @param tokens
	 *            The supported tokens.
	 * @return This builder.
	 */
	B tokens(List<String> tokens);

	/**
	 * Sets the list of supported tokens for the expansion.
	 * 
	 * @param tokens
	 *            The supported tokens.
	 * @return This builder.
	 */
	default B tokens(String... tokens) {
		return tokens(Arrays.asList(tokens));
	}

	/**
	 * Sets the url of the expansion. This links to a website or source for
	 * information or downloads on the internet, if the author so chooses to include
	 * one.
	 * 
	 * @param url
	 *            The url of the expansion.
	 * @return This builder.
	 * @throws Exception
	 *             If the url is malformed or null.
	 */
	B url(String url) throws Exception;

	/**
	 * Sets the url of the expansion. This links to a website or source for
	 * information or downloads on the internet, if the author so chooses to include
	 * one.
	 * 
	 * @param url
	 *            The url of the expansion.
	 * @return This builder.
	 */
	default B url(URL url) {
		try {
			return url(url.toString());
		} catch (Exception e) {
			// literally will never happen. Do nothing.
			return current();
		}
	}

	/**
	 * Sets the version of the expansion.
	 * 
	 * @param version
	 *            The version of the expansion.
	 * @return This builder.
	 */
	B version(String version);

}
