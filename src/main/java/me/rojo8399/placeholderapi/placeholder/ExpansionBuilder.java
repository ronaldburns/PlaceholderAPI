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
package me.rojo8399.placeholderapi.placeholder;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.util.ResettableBuilder;
import org.spongepowered.api.world.Locatable;

import com.google.common.base.Preconditions;

import me.rojo8399.placeholderapi.utils.TypeUtils;

/**
 * @author Wundero
 *
 */
public class ExpansionBuilder<S, O, V> implements ResettableBuilder<Expansion<S, O, V>, ExpansionBuilder<S, O, V>> {

	private ExpansionBuilder(boolean verify) {
		Preconditions.checkArgument(!verify || verify());
	}

	/**
	 * Verify that the source and observer types are valid extensions of
	 * supported classes.
	 * 
	 * @return Whether the class is verified.
	 */
	public boolean verify() {
		Class<?> clazz = this.getClass();
		List<Class<?>> params = Arrays.asList(clazz.getDeclaredMethods()).stream()
				.filter(m -> m.getName().equalsIgnoreCase("parse") && Arrays.asList(m.getGenericParameterTypes())
						.stream().map(Type::getTypeName).anyMatch(s -> s.contains("java.util.Optional")))
				.map(m -> Arrays.asList(m.getParameterTypes())).map(List::stream).reduce(Stream.empty(), Stream::concat)
				.collect(Collectors.toList());
		try {
			return verifySource(params.get(0)) && verifySource(params.get(1)) && verifyToken(params.get(2));
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * @return The class that represents the returned value on parsing.
	 */
	@SuppressWarnings("unchecked")
	public final Class<? extends V> getValueClass() {
		Method m = getParseMethod();
		return (Class<? extends V>) m.getReturnType();
	}

	private final Method getParseMethod() {
		Class<?> clazz = this.getClass();
		return Arrays.asList(clazz.getDeclaredMethods()).stream()
				.filter(m -> m.getName().toLowerCase().contains("parse"))
				.filter(m -> Arrays.asList(m.getGenericParameterTypes()).stream().map(Type::getTypeName)
						.anyMatch(s -> s.contains("java.util.Optional")))
				.findAny().get(); // we know it exists ;)
	}

	/**
	 * @return The class that represents the source object on parsing.
	 */
	@SuppressWarnings("unchecked")
	public final Class<? extends S> getSourceClass() {
		Method m = getParseMethod();
		return (Class<? extends S>) m.getParameterTypes()[0];
	}

	/**
	 * @return The class that represents the observer object on parsing.
	 */
	@SuppressWarnings("unchecked")
	public final Class<? extends O> getObserverClass() {
		Method m = getParseMethod();
		return (Class<? extends O>) m.getParameterTypes()[1];
	}

	/*
	 * Used for type verification
	 */
	private V parse(S s, O o, Optional<String> t) {
		return null;
	}

	private static boolean verifyToken(Class<?> param) {
		return Optional.class.isAssignableFrom(param) || String.class.isAssignableFrom(param);
	}

	private static boolean verifySource(Class<?> param) {
		return CommandSource.class.isAssignableFrom(param) || Locatable.class.isAssignableFrom(param)
				|| User.class.isAssignableFrom(param);
	}

	/**
	 * Create a new ExpansionBuilder based on the source, observer and value
	 * types.
	 * 
	 * Source and observer types must both be an instance or subinstance of
	 * CommandSource, User or Locatable. The value type can be anything.
	 * 
	 * @return The newly created builder.
	 */
	public static <S, O, V> ExpansionBuilder<S, O, V> builder() {
		return new ExpansionBuilder<S, O, V>(true);
	}

	private static <S, O, V> ExpansionBuilder<S, O, V> unverified() {
		return new ExpansionBuilder<S, O, V>(false);
	}

	private String id, auth, ver = "1.0", desc, url;
	private List<String> tokens = new ArrayList<>();
	private ExpansionFunction<S, O, V> func;
	private Predicate<Expansion<S, O, V>> reload = (func) -> true;
	private boolean relational = false;
	private Object plugin, config;

	/**
	 * @return The description of the expansion.
	 */
	public String getDescription() {
		return desc;
	}

	/**
	 * @return Whether the expansion is relational.
	 */
	public boolean isRelational() {
		return relational;
	}

	/**
	 * @return The supported tokens for the expansion.
	 */
	public List<String> getTokens() {
		return tokens;
	}

	/**
	 * @return The URL for the expansion.
	 * @throws Exception
	 *             - If the url is not properly formatted or is null.
	 */
	public URL getUrl() throws Exception {
		return new URL(url);
	}

	/**
	 * @return The url for the expansion.
	 */
	public String getUrlString() {
		return url;
	}

	/**
	 * @return The version of the expansion.
	 */
	public String getVersion() {
		return ver;
	}

	/**
	 * @return The author of the expansion.
	 */
	public String getAuthor() {
		return auth;
	}

	/**
	 * @return The id of the expansion.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Set the plugin which holds this expansion. This method is required before
	 * building and cannot accept a null plugin.
	 * 
	 * @param plugin
	 *            The plugin which holds this expansion.
	 * @return This builder.
	 */
	public ExpansionBuilder<S, O, V> plugin(Object plugin) {
		Optional<PluginContainer> plox = Sponge.getPluginManager().fromInstance(plugin);
		if (!plox.isPresent()) {
			throw new IllegalArgumentException("Plugin object is not valid!");
		}
		this.plugin = plugin;
		return this;
	}

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
	public ExpansionBuilder<S, O, V> reloadFunction(Predicate<Expansion<S, O, V>> reload) {
		this.reload = reload == null ? f -> true : reload;
		return this;
	}

	/**
	 * Add an object which holds config values. This object will be populated
	 * with configuration options when the expansion is loaded and reloaded.
	 * 
	 * @param config
	 *            The object to populate.
	 * @return This builder.
	 */
	public ExpansionBuilder<S, O, V> config(Object config) {
		this.config = config;
		return this;
	}

	/**
	 * Set whether this expansion is relational. `Relational` means that this
	 * expansion's parse will be called when the id "rel_[id]" is passed into
	 * the service. It also means that, if this expansion's parsing returns null
	 * AND the server's configuration allows it, it will try to return the value
	 * of the placeholder of id "[id]" if it exists for the observer.
	 * 
	 * Setting this to true in no way guarantees that it will use the observer,
	 * that the observer will not be null, or that it requires the observer at
	 * all.
	 * 
	 * Conversely, if this is false, that in no way guarantees that it will not
	 * use the observer.
	 * 
	 * @param relational
	 *            Whether this expansion is relational.
	 * @return This builder.
	 */
	public ExpansionBuilder<S, O, V> relational(boolean relational) {
		this.relational = relational;
		return this;
	}

	/**
	 * Execute a function for the parsing of this expansion.
	 * 
	 * @param exec
	 *            The function to execute.
	 * @return This builder.
	 */
	public ExpansionBuilder<S, O, V> run(Runnable exec) {
		return function((s, o, t) -> {
			exec.run();
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
	public ExpansionBuilder<S, O, V> provide(Supplier<V> exec) {
		return function((s, o, t) -> exec.get());
	}

	/**
	 * Execute a function for the parsing of this expansion.
	 * 
	 * @param exec
	 *            The function to execute.
	 * @return This builder.
	 */
	public ExpansionBuilder<S, O, V> consumeSingle(Consumer<S> exec) {
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
	public ExpansionBuilder<S, O, V> consumeToken(Consumer<Optional<String>> exec) {
		return function((s, o, t) -> {
			exec.accept(t);
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
	public ExpansionBuilder<S, O, V> consumeDual(BiConsumer<S, O> exec) {
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
	public ExpansionBuilder<S, O, V> consumeSingleToken(BiConsumer<S, Optional<String>> exec) {
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
	public ExpansionBuilder<S, O, V> functionSingle(Function<S, V> exec) {
		return function((s, o, t) -> exec.apply(s));
	}

	/**
	 * Execute a function for the parsing of this expansion.
	 * 
	 * @param exec
	 *            The function to execute.
	 * @return This builder.
	 */
	public ExpansionBuilder<S, O, V> functionToken(Function<Optional<String>, V> exec) {
		return function((s, o, t) -> exec.apply(t));
	}

	/**
	 * Execute a function for the parsing of this expansion.
	 * 
	 * @param exec
	 *            The function to execute.
	 * @return This builder.
	 */
	public ExpansionBuilder<S, O, V> functionDual(BiFunction<S, O, V> exec) {
		return function((s, o, t) -> exec.apply(s, o));
	}

	/**
	 * Execute a function for the parsing of this expansion.
	 * 
	 * @param exec
	 *            The function to execute.
	 * @return This builder.
	 */
	public ExpansionBuilder<S, O, V> functionSingleToken(BiFunction<S, Optional<String>, V> exec) {
		return function((s, o, t) -> exec.apply(s, t));
	}

	/**
	 * Execute a function for the parsing of this expansion.
	 * 
	 * @param exec
	 *            The function to execute.
	 * @return This builder.
	 */
	public ExpansionBuilder<S, O, V> function(ExpansionFunction<S, O, V> exec) {
		this.func = exec;
		return this;
	}

	private static String fix(String id) {
		id = id.toLowerCase().trim();
		if (id.startsWith("rel_")) {
			id = id.substring(4);
		}
		return id.replace("_", "").replace(" ", "");
	}

	/**
	 * Set the id of this expansion. This is required to build the expansion and
	 * cannot be null.
	 * 
	 * @param id
	 *            The id to register this expansion under.
	 * @return This builder.
	 */
	public ExpansionBuilder<S, O, V> id(String id) {
		this.id = fix(id);
		return this;
	}

	/**
	 * Sets the author of the expansion.
	 * 
	 * @param author
	 *            The author of the expansion.
	 * @return This builder.
	 */
	public ExpansionBuilder<S, O, V> author(String author) {
		this.auth = author;
		return this;
	}

	/**
	 * Sets the version of the expansion.
	 * 
	 * @param version
	 *            The version of the expansion.
	 * @return This builder.
	 */
	public ExpansionBuilder<S, O, V> version(String version) {
		this.ver = version;
		return this;
	}

	/**
	 * Sets the description of the expansion.
	 * 
	 * @param description
	 *            The description of the expansion.
	 * @return This builder.
	 */
	public ExpansionBuilder<S, O, V> description(String description) {
		this.desc = description;
		return this;
	}

	/**
	 * Sets the url of the expansion. This links to a website or source for
	 * information or downloads on the internet, if the author so chooses to
	 * include one.
	 * 
	 * @param url
	 *            The url of the expansion.
	 * @return This builder.
	 * @throws Exception
	 *             If the url is malformed or null.
	 */
	public ExpansionBuilder<S, O, V> url(String url) throws Exception {
		this.url = new URL(url).toString();
		return this;
	}

	/**
	 * Sets the list of supported tokens for the expansion.
	 * 
	 * @param tokens
	 *            The supported tokens.
	 * @return This builder.
	 */
	public ExpansionBuilder<S, O, V> tokens(String... tokens) {
		return tokens(Arrays.asList(tokens));
	}

	/**
	 * Sets the list of supported tokens for the expansion.
	 * 
	 * @param tokens
	 *            The supported tokens.
	 * @return This builder.
	 */
	public ExpansionBuilder<S, O, V> tokens(List<String> tokens) {
		this.tokens = tokens;
		return this;
	}

	/**
	 * Adds to the list of supported tokens for the expansion.
	 * 
	 * @param tokens
	 *            The supported tokens.
	 * @return This builder.
	 */
	public ExpansionBuilder<S, O, V> addTokens(String... tokens) {
		return addTokens(Arrays.asList(tokens));
	}

	/**
	 * Adds to the list of supported tokens for the expansion.
	 * 
	 * @param tokens
	 *            The supported tokens.
	 * @return This builder.
	 */
	public ExpansionBuilder<S, O, V> addTokens(List<String> tokens) {
		if (this.tokens == null) {
			return tokens(tokens);
		}
		this.tokens.addAll(tokens);
		return this;
	}

	/**
	 * This method will build the expansion and then attemp to register the
	 * expansion.
	 * 
	 * This method will throw an exception if the id, plugin or function have
	 * not been specified.
	 * 
	 * @return Whether the registration was successful.
	 * @throws Exception
	 *             If the expansion cannot be created or registered.
	 */
	public boolean buildAndRegister() throws Exception {
		Expansion<S, O, V> exp = build();
		return Store.get().register(exp);
	}

	/**
	 * This method will build the expansion. This is not a terminating operation
	 * on the builder, so if you wish to reuse the builder under a new id, you
	 * could do so.
	 * 
	 * This method will throw an exception if the id, plugin or function have
	 * not been specified.
	 * 
	 * @return Whether the registration was successful.
	 * @throws Exception
	 *             If the expansion cannot be created.
	 */
	public Expansion<S, O, V> build() throws Exception {
		if (id == null || id.isEmpty()) {
			throw new IllegalStateException("ID must be specified!");
		}
		if (func == null) {
			throw new IllegalStateException("Function must be specified!");
		}
		if (plugin == null) {
			throw new IllegalStateException("Plugin cannot be null!");
		}
		Expansion<S, O, V> exp = new Expansion<S, O, V>(id, plugin, auth, desc, ver, url, relational, tokens) {
			@Override
			public V parse(S source, O observer, Optional<String> token) throws Exception {
				return func.parse(source, observer, token);
			}

			@Override
			public boolean reload() {
				return reload.test(this);
			}

			@Override
			public void registerListeners() {
				super.registerListeners();
				if (regList != null) {
					regList.run();
				}
			}

			@Override
			public void unregisterListeners() {
				super.unregisterListeners();
				if (unregList != null) {
					unregList.run();
				}
			}
		};
		if (config != null) {
			exp.setConfig(config);
		}
		return exp;
	}

	/**
	 * Attempt to create expansion builders for every "@Placeholder" annotated
	 * method in the provided object. Every expansion builder returned will have
	 * the id, plugin, function and relational fields filled out already, so all
	 * that remains are optional fields, and any builder in this list can be
	 * built immediately.
	 * 
	 * @param object
	 *            The object in which the methods reside. This object cannot be
	 *            null.
	 * @param plugin
	 *            The plugin which holds the expansions.
	 * @return A list of builders for each method in the object.
	 */
	public static List<ExpansionBuilder<?, ?, ?>> loadAll(Object object, Object plugin) {
		Class<?> clazz = object.getClass();
		Map<Method, Boolean> methods = Store.findAll(object);
		@SuppressWarnings("rawtypes")
		List<ExpansionBuilder> l = methods.entrySet()
				.stream().map(e -> unverified().relational(e.getValue())
						.id(e.getKey().getAnnotation(Placeholder.class).id()).frommethod(object, e.getKey(), plugin))
				.collect(Collectors.toList());
		return l.stream().map(e -> (ExpansionBuilder<?, ?, ?>) e).collect(Collectors.toList());
	}

	@SuppressWarnings({ "rawtypes" })
	private ExpansionBuilder frommethod(Object o, Method m, Object p) {
		Expansion<?, ?, ?> exp = Store.get().createForMethod(m, o, p);
		return from(this, exp);
	}

	@SuppressWarnings("unchecked")
	private static <S, O, V> ExpansionBuilder<S, O, V> from(ExpansionBuilder<?, ?, ?> builder,
			final Expansion<S, O, V> exp) {
		if (!exp.verify()) {
			return (ExpansionBuilder<S, O, V>) builder;
		}
		ExpansionBuilder<S, O, V> n = unverified();
		n.relational(builder.relational).id(builder.id).author(builder.auth).description(builder.desc)
				.config(builder.config).tokens(builder.tokens).version(builder.ver);
		try {
			n.url(builder.url);
		} catch (Exception e) {
		}
		if (builder.plugin != null) {
			n.plugin(builder.plugin);
		}
		if (n.config == null) {
			n.config = exp.getConfiguration();
		}
		n.regList = exp::registerListeners;
		n.unregList = exp::unregisterListeners;
		n.func = exp::parse;
		return n;
	}

	private Runnable regList, unregList;

	/**
	 * Create a builder for a method in the object.
	 *
	 * This will attempt to find a method which has the "@Placeholder"
	 * annotation linked to the provided id. If it exists, it will create the
	 * builder. This will attempt to use the relational field to determine if it
	 * can parse that method, which will by default be the normal method. If it
	 * does not find that method, it will check for one which is not relational.
	 * 
	 * This method alters the function, the plugin, the id and the relational
	 * fields. Any other previously existing fields will be conserved.
	 * 
	 * The provided id must exist on at least one method. Any repeated ids will
	 * be ignored unless they are of different relational status.
	 * 
	 * @param obj
	 *            The object containing the placeholder method.
	 * @param id
	 *            The id of the placeholder method.
	 * @param plugin
	 *            The plugin holding the expansion.
	 * @return This builder.
	 */
	public ExpansionBuilder<S, O, V> from(Object obj, String id, Object plugin) {
		Class<?> clazz = obj.getClass();
		Method m = Store.find(obj, id, relational);
		boolean togglerel = false;
		if (m == null) {
			m = Store.find(obj, id, !relational);
			if (m == null) {
				throw new IllegalArgumentException("No placeholder by that ID found!");
			}
			togglerel = true;
		}
		frommethod(obj, m, plugin);
		this.id(id);
		this.relational = TypeUtils.xor(togglerel, relational);
		return plugin(plugin);
	}

	/**
	 * Copy settings from the expansion provided in order to modify it.
	 * 
	 * @param exp
	 *            The expansion from which to draw values.
	 * @return This builder, with all fields modified to suit the expansion
	 *         provided.
	 */
	@Override
	public ExpansionBuilder<S, O, V> from(Expansion<S, O, V> exp) {
		this.id = exp.id();
		this.auth = exp.author();
		this.desc = exp.description();
		this.ver = exp.version();
		this.url = exp.url().toString();
		this.tokens = exp.tokens();
		this.plugin = exp.getPlugin();
		this.config = exp.getConfiguration();
		this.relational = exp.relational();
		this.regList = exp::registerListeners;
		this.unregList = exp::unregisterListeners;
		this.func = exp::parse;
		return this;
	}

	/**
	 * Copy settings from the expansion provided in order to modify it. This
	 * method ignores the types of the provided expansion and attempts to cast
	 * them to its own.
	 * 
	 * @param exp
	 *            The expansion from which to draw values.
	 * @return This builder, with all fields modified to suit the expansion
	 *         provided.
	 */
	@SuppressWarnings("unchecked")
	public ExpansionBuilder<S, O, V> fromUnknown(Expansion<?, ?, ?> exp) {
		if (!(exp.getObserverClass().isAssignableFrom(this.getObserverClass())
				&& exp.getSourceClass().isAssignableFrom(this.getSourceClass())
				&& this.getValueClass().isAssignableFrom(exp.getValueClass()))) {
			throw new IllegalArgumentException("Expansion types not supported!");
		}
		return from((Expansion<S, O, V>) exp);
	}

	/**
	 * Reset the builder's settings. In this case, it returns a new builder.
	 * 
	 * @return The new builder.
	 */
	@Override
	public ExpansionBuilder<S, O, V> reset() {
		return unverified();
	}

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
	public static interface ExpansionFunction<S, O, V> {
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
		public V parse(S source, O observer, Optional<String> token) throws Exception;
	}

}
