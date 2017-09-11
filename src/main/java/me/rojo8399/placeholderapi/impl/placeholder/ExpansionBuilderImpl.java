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
package me.rojo8399.placeholderapi.impl.placeholder;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.world.Locatable;

import com.google.common.base.Preconditions;

import me.rojo8399.placeholderapi.ExpansionBuilder;
import me.rojo8399.placeholderapi.Placeholder;
import me.rojo8399.placeholderapi.impl.PlaceholderAPIPlugin;

/**
 * @author Wundero
 *
 */
public class ExpansionBuilderImpl<S, O, V> implements ExpansionBuilder<S, O, V, ExpansionBuilderImpl<S, O, V>> {

	private ExpansionBuilderImpl(boolean verify, Class<? extends S> s, Class<? extends O> o, Class<? extends V> v) {
		this.verify = verify;
		this.sourceClass = s;
		this.returnClass = v;
		this.observerClass = o;
	}

	/**
	 * Verify that the source and observer types are valid extensions of supported
	 * classes.
	 * 
	 * @return Whether the class is verified.
	 */
	public boolean verify() {
		boolean out = false;
		try {
			out = verifySource(sourceClass) && verifySource(observerClass);
		} catch (Exception e) {
		}
		if (out) {
			verify = false;
		}
		return out;
	}

	/**
	 * @return The class that represents the returned value on parsing.
	 */
	public final Class<? extends V> getValueClass() {
		return returnClass;
	}

	/**
	 * @return The class that represents the source object on parsing.
	 */
	public final Class<? extends S> getSourceClass() {
		return sourceClass;
	}

	/**
	 * @return The class that represents the observer object on parsing.
	 */
	public final Class<? extends O> getObserverClass() {
		return observerClass;
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
		return param == null || MessageReceiver.class.isAssignableFrom(param) || Locatable.class.isAssignableFrom(param)
				|| Subject.class.isAssignableFrom(param) || DataHolder.class.isAssignableFrom(param);
	}

	/**
	 * Create a new ExpansionBuilder based on the source, observer and value types.
	 * 
	 * Source and observer types must both be an instance or subinstance of
	 * CommandSource, User or Locatable. The value type can be anything.
	 * 
	 * @return The newly created builder.
	 */
	public static <S, O, V> ExpansionBuilderImpl<S, O, V> builder(Class<? extends S> s, Class<? extends O> o,
			Class<? extends V> v) {
		return new ExpansionBuilderImpl<S, O, V>(true, s, o, v);
	}

	public static <S, O, V> ExpansionBuilderImpl<S, O, V> unverified(Class<? extends S> s, Class<? extends O> o,
			Class<? extends V> v) {
		return new ExpansionBuilderImpl<S, O, V>(false, s, o, v);
	}

	private String id, auth, ver = "1.0", desc, url;
	private List<String> tokens = new ArrayList<>();
	private ExpansionFunction<S, O, V> func;
	private Predicate<Expansion<S, O, V>> reload = (func) -> true;
	private boolean relational = false;
	private Object plugin, config, listeners;
	private boolean verify = true;
	private Class<? extends S> sourceClass;
	private Class<? extends O> observerClass;
	private Class<? extends V> returnClass;

	/**
	 * @return The description of the expansion.
	 */
	@Override
	public String getDescription() {
		return desc;
	}

	/**
	 * @return Whether the expansion is relational.
	 */
	@Override
	public boolean isRelational() {
		return relational;
	}

	/**
	 * @return The supported tokens for the expansion.
	 */
	@Override
	public List<String> getTokens() {
		return tokens;
	}

	/**
	 * @return The URL for the expansion.
	 * @throws Exception
	 *             - If the url is not properly formatted or is null.
	 */
	@Override
	public URL getUrl() throws Exception {
		return new URL(url);
	}

	/**
	 * @return The url for the expansion.
	 */
	@Override
	public String getUrlString() {
		return url;
	}

	/**
	 * @return The version of the expansion.
	 */
	@Override
	public String getVersion() {
		return ver;
	}

	/**
	 * @return The author of the expansion.
	 */
	@Override
	public String getAuthor() {
		return auth;
	}

	/**
	 * @return The id of the expansion.
	 */
	@Override
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
	@Override
	public ExpansionBuilderImpl<S, O, V> plugin(Object plugin) {
		Optional<PluginContainer> plox = Sponge.getPluginManager().fromInstance(plugin);
		if (!plox.isPresent()) {
			throw new IllegalArgumentException("Plugin object is not valid!");
		}
		this.plugin = plugin;
		return this;
	}

	@Override
	public ExpansionBuilderImpl<S, O, V> listen(Object o) {
		if (o != null) {
			this.listeners = o;
		}
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
	@Override
	public ExpansionBuilderImpl<S, O, V> reloadFunction(Predicate<Expansion<S, O, V>> reload) {
		this.reload = reload == null ? f -> true : reload;
		return this;
	}

	/**
	 * Add an object which holds config values. This object will be populated with
	 * configuration options when the expansion is loaded and reloaded.
	 * 
	 * @param config
	 *            The object to populate.
	 * @return This builder.
	 */
	@Override
	public ExpansionBuilderImpl<S, O, V> config(Object config) {
		this.config = config;
		return this;
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
	@Override
	public ExpansionBuilderImpl<S, O, V> relational(boolean relational) {
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
	@Override
	public ExpansionBuilderImpl<S, O, V> function(ExpansionFunction<S, O, V> exec) {
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
	@Override
	public ExpansionBuilderImpl<S, O, V> id(String id) {
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
	@Override
	public ExpansionBuilderImpl<S, O, V> author(String author) {
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
	@Override
	public ExpansionBuilderImpl<S, O, V> version(String version) {
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
	@Override
	public ExpansionBuilderImpl<S, O, V> description(String description) {
		this.desc = description;
		return this;
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
	@Override
	public ExpansionBuilderImpl<S, O, V> url(String url) throws Exception {
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
	@Override
	public ExpansionBuilderImpl<S, O, V> tokens(List<String> tokens) {
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
	@Override
	public ExpansionBuilderImpl<S, O, V> addTokens(List<String> tokens) {
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
	 * This method will throw an exception if the id, plugin or function have not
	 * been specified.
	 * 
	 * @return Whether the registration was successful.
	 * @throws Exception
	 *             If the expansion cannot be created or registered.
	 */
	@Override
	public boolean buildAndRegister() throws Exception {
		Expansion<S, O, V> exp = build();
		return Store.get().register(exp);
	}

	/**
	 * This method will build the expansion. This is not a terminating operation on
	 * the builder, so if you wish to reuse the builder under a new id, you could do
	 * so.
	 * 
	 * This method will throw an exception if the id, plugin or function have not
	 * been specified.
	 * 
	 * @return Whether the registration was successful.
	 * @throws Exception
	 *             If the expansion cannot be created.
	 */
	@Override
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
		if (verify) {
			Preconditions.checkArgument(verify());
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
		};
		if (config != null) {
			exp.setConfig(config);
		}
		if (listeners != null) {
			exp.setReloadListeners(() -> {
				PlaceholderAPIPlugin.getInstance().unregisterListeners(listeners);
				PlaceholderAPIPlugin.getInstance().registerListeners(listeners, plugin);
			});
		}
		return exp;
	}

	/**
	 * Attempt to create expansion builders for every "@Placeholder" annotated
	 * method in the provided object. Every expansion builder returned will have the
	 * id, plugin, function and relational fields filled out already, so all that
	 * remains are optional fields, and any builder in this list can be built
	 * immediately.
	 * 
	 * @param object
	 *            The object in which the methods reside. This object cannot be
	 *            null.
	 * @param plugin
	 *            The plugin which holds the expansions.
	 * @return A list of builders for each method in the object.
	 */
	public static List<ExpansionBuilderImpl<?, ?, ?>> loadAll(Object object, Object plugin) {
		Map<Method, Boolean> methods = Store.findAll(object);
		Store store = Store.get();
		@SuppressWarnings("rawtypes")
		List<ExpansionBuilder> l = methods.entrySet().stream().map(e -> lfm(object, e.getKey(), plugin, e.getValue()))
				.collect(Collectors.toList());
		return l.stream().map(e -> (ExpansionBuilderImpl<?, ?, ?>) e).collect(Collectors.toList());
	}

	private static ExpansionBuilderImpl<?, ?, ?> lfm(Object o, Method m, Object p, boolean rel) {
		Store s = Store.get();
		return unverified(s.getSourceType(m).orElse(Locatable.class), s.getObserverType(m).orElse(Locatable.class),
				m.getReturnType()).relational(rel).id(m.getAnnotation(Placeholder.class).id()).frommethod(o, m, p);
	}

	public static ExpansionBuilderImpl<?, ?, ?> load(Object src, String id, Object plugin) {
		Method m = Store.find(src, id, false);
		boolean relational = false;
		if (m == null) {
			m = Store.find(src, id, true);
			relational = true;
			if (m == null) {
				throw new IllegalArgumentException("No placeholder exists with that id!");
			}
		}
		return lfm(src, m, plugin, relational);
	}

	@SuppressWarnings({ "rawtypes" })
	private ExpansionBuilderImpl frommethod(Object o, Method m, Object p) {
		Expansion<?, ?, ?> exp = Store.get().createForMethod(m, o, p);
		return from(this.id(exp.id()).plugin(p), exp);
	}

	@SuppressWarnings("unchecked")
	private static <S, O, V> ExpansionBuilderImpl<S, O, V> from(ExpansionBuilderImpl<?, ?, ?> builder,
			final Expansion<S, O, V> exp) {
		if (!exp.verify()) {
			return (ExpansionBuilderImpl<S, O, V>) builder;
		}
		ExpansionBuilderImpl<?, ?, ?> b = builder;
		ExpansionBuilderImpl<S, O, V> n = unverified(exp.getSourceClass(), exp.getObserverClass(), exp.getValueClass());
		n.relational(b.relational).id(b.id).author(b.auth).description(b.desc).config(b.config).tokens(b.tokens)
				.version(b.ver);
		try {
			n.url(b.url);
		} catch (Exception e) {
		}
		if (b.plugin != null) {
			n.plugin(b.plugin);
		}
		if (n.config == null) {
			n.config = exp.getConfiguration();
		}
		n.func = exp::parse;
		return n;
	}

	private Runnable regList, unregList;

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
	 * @param obj
	 *            The object containing the placeholder method.
	 * @param id
	 *            The id of the placeholder method.
	 * @param plugin
	 *            The plugin holding the expansion.
	 * @return This builder.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public ExpansionBuilderImpl<S, O, V> from(Object obj, String id, Object plugin) {
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
		return frommethod(obj, m, plugin);
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
	public ExpansionBuilderImpl<S, O, V> from(Expansion<S, O, V> exp) {
		this.id = exp.id();
		this.auth = exp.author();
		this.desc = exp.description();
		this.ver = exp.version();
		this.url = exp.url().toString();
		this.tokens = exp.tokens();
		this.plugin = exp.getPlugin();
		this.config = exp.getConfiguration();
		this.relational = exp.relational();
		this.func = exp::parse;
		this.sourceClass = exp.getSourceClass();
		this.observerClass = exp.getObserverClass();
		this.returnClass = exp.getValueClass();
		return this;
	}

	/**
	 * Copy settings from the expansion provided in order to modify it. This method
	 * ignores the types of the provided expansion and attempts to cast them to its
	 * own.
	 * 
	 * @param exp
	 *            The expansion from which to draw values.
	 * @return This builder, with all fields modified to suit the expansion
	 *         provided.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public ExpansionBuilderImpl<S, O, V> fromUnknown(Expansion<?, ?, ?> exp) {
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
	public ExpansionBuilderImpl<S, O, V> reset() {
		return unverified(getSourceClass(), getObserverClass(), getValueClass());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see me.rojo8399.placeholderapi.ExpansionBuilder#current()
	 */
	@Override
	public ExpansionBuilderImpl<S, O, V> current() {
		return this;
	}

}
