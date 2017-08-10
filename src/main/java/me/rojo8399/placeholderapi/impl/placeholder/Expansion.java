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
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.world.Locatable;

import com.google.common.base.Preconditions;

import me.rojo8399.placeholderapi.impl.PlaceholderAPIPlugin;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

/**
 * @author Wundero
 *
 */
public abstract class Expansion<S, O, V> {

	private String id;
	private String desc;
	private URL url;
	private String ver;
	private List<String> tokens;
	private String author;
	private Object configObject;
	private Object plugin;
	private Class<? extends S> sourceClass;
	private Class<? extends O> observerClass;
	private Class<? extends V> valueClass;
	private boolean relational = false;
	private boolean enabled = true;
	private Runnable reloadListeners;

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
	public abstract V parse(S source, O observer, Optional<String> token) throws Exception;

	/**
	 * Reload + basic reloading method calls.
	 * 
	 * @return whether reload() returns true.
	 */
	final boolean refresh() {
		populateConfig();
		reloadListeners();
		return reload();
	}

	final void reloadListeners() {
		if (reloadListeners != null) {
			reloadListeners.run();
		}
	}

	/**
	 * Set the function to call to reload listeners. Will be called upon reload.
	 * 
	 * @param run
	 *            The code to execute.
	 */
	final void setReloadListeners(Runnable run) {
		this.reloadListeners = run;
	}

	/**
	 * Attempt to cast an object to the Observer type. Returns null if it fails.
	 * 
	 * @param observer
	 *            The object to cast.
	 * @return The casted object.
	 */
	public final O convertObserver(Object observer) {
		if (observer == null) {
			return null;
		}
		try {
			return observerClass.cast(observer);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Attempt to cast an object to the Source type. Returns null if it fails.
	 * 
	 * @param source
	 *            The object to cast.
	 * @return The casted object.
	 */
	public final S convertSource(Object source) {
		if (source == null) {
			return null;
		}
		try {
			return sourceClass.cast(source);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Set the valid tokens for this expansion. Useful for reloading a dynamic
	 * token expansion.
	 * 
	 * @param tokens
	 *            The new tokens to use.
	 */
	public void setTokens(String... tokens) {
		setTokens(Arrays.asList(tokens));
	}

	/**
	 * Set the valid tokens for this expansion. Useful for reloading a dynamic
	 * token expansion.
	 * 
	 * @param tokens
	 *            The new tokens to use.
	 */
	public void setTokens(List<String> tokens) {
		this.tokens = tokens;
	}

	/**
	 * Reload the placeholder. By default it does nothing, but it can be
	 * overridden to do whatever the implementer needs to do on reload.
	 * 
	 * @return Whether the reload was succesful.
	 */
	public boolean reload() {
		return true;
	}

	/**
	 * Create a new Expansion with the provided arguments.
	 * 
	 * @param id
	 *            The expansion id.
	 * @param plugin
	 *            The holding plugin.
	 * @param author
	 *            The author of the expansion.
	 * @param desc
	 *            The expansion's description.
	 * @param ver
	 *            The expansion's version.
	 * @param url
	 *            The url of the expansion. Can be null.
	 * @param relational
	 *            Whether the expansion is relational.
	 * @param tokens
	 *            The supported tokens.
	 */
	public Expansion(String id, Object plugin, String author, String desc, String ver, URL url, boolean relational,
			String... tokens) {
		this(id, plugin, author, desc, ver, url, relational, Arrays.asList(tokens));
	}

	/**
	 * Create a new Expansion with the provided arguments.
	 * 
	 * @param id
	 *            The expansion id.
	 * @param plugin
	 *            The holding plugin.
	 * @param author
	 *            The author of the expansion.
	 * @param desc
	 *            The expansion's description.
	 * @param ver
	 *            The expansion's version.
	 * @param url
	 *            The url of the expansion. Can be null.
	 * @param relational
	 *            Whether the expansion is relational.
	 * @param tokens
	 *            The supported tokens.
	 * @throws Exception
	 *             If the url is not formatted properly.
	 */
	public Expansion(String id, Object plugin, String author, String desc, String ver, String url, boolean relational,
			String... tokens) throws Exception {
		this(id, plugin, author, desc, ver, url != null && !url.isEmpty() ? new URL(url) : null, relational, tokens);
	}

	/**
	 * Create a new Expansion with the provided arguments.
	 * 
	 * @param id
	 *            The expansion id.
	 * @param plugin
	 *            The holding plugin.
	 * @param author
	 *            The author of the expansion.
	 * @param desc
	 *            The expansion's description.
	 * @param ver
	 *            The expansion's version.
	 * @param url
	 *            The url of the expansion. Can be null.
	 * @param relational
	 *            Whether the expansion is relational.
	 * @param tokens
	 *            The supported tokens.
	 * @throws Exception
	 *             If the url is not formatted properly.
	 */
	public Expansion(String id, Object plugin, String author, String desc, String ver, String url, boolean relational,
			List<String> tokens) throws Exception {
		this(id, plugin, author, desc, ver, url != null && !url.isEmpty() ? new URL(url) : null, relational, tokens);
	}

	/**
	 * Null constructor. Used by method wrapper generator. DO NOT USE THIS
	 * UNLESS YOU KNOW WHAT YOU ARE DOING.
	 * 
	 * @param id
	 *            The id of the expansion.
	 */
	protected Expansion(String id) {
		this(id, PlaceholderAPIPlugin.getInstance(), null, null, null, (URL) null, false, new ArrayList<>());
	}

	/**
	 * Create a new Expansion with the provided arguments.
	 * 
	 * @param id
	 *            The expansion id.
	 * @param plugin
	 *            The holding plugin.
	 * @param author
	 *            The author of the expansion.
	 * @param desc
	 *            The expansion's description.
	 * @param ver
	 *            The expansion's version.
	 * @param url
	 *            The url of the expansion. Can be null.
	 * @param relational
	 *            Whether the expansion is relational.
	 * @param tokens
	 *            The supported tokens.
	 */
	public Expansion(String id, Object plugin, String author, String desc, String ver, URL url, boolean relational,
			List<String> tokens) {
		this(id, plugin, author, desc, ver, url, relational, tokens, null, null, null);
	}

	/**
	 * Create a new Expansion with the provided arguments.
	 * 
	 * @param id
	 *            The expansion id.
	 * @param plugin
	 *            The holding plugin.
	 * @param author
	 *            The author of the expansion.
	 * @param desc
	 *            The expansion's description.
	 * @param ver
	 *            The expansion's version.
	 * @param url
	 *            The url of the expansion. Can be null.
	 * @param relational
	 *            Whether the expansion is relational.
	 * @param tokens
	 *            The supported tokens.
	 * @param source
	 *            The class representing the source type.
	 * @param observer
	 *            The class representing the observer type.
	 * @param value
	 *            The class representing the value type.
	 */
	public Expansion(String id, Object plugin, String author, String desc, String ver, URL url, boolean relational,
			List<String> tokens, Class<? extends S> source, Class<? extends O> observer, Class<? extends V> value) {
		this.id = fix(Preconditions.checkNotNull(id));
		this.plugin = Preconditions.checkNotNull(plugin);
		this.author = author;
		this.desc = desc;
		this.ver = ver;
		this.url = url;
		this.tokens = tokens;
		this.sourceClass = source;
		this.valueClass = value;
		this.observerClass = observer;
		this.relational = relational;
		this.checkClasses();
	}

	/**
	 * @return The holding plugin.
	 */
	public Object getPlugin() {
		return plugin;
	}

	/**
	 * Get the configuration object. Will attempt to cast to requested type,
	 * returns null if failed.
	 * 
	 * @return The configuration object.
	 */
	@SuppressWarnings("unchecked")
	public <T> T getConfiguration() {
		try {
			return (T) configObject;
		} catch (Exception e) {
			return null;
		}
	}

	void setConfig(Object o) {
		this.configObject = o;
	}

	/**
	 * Popoulate configuration object. Override only if necessary.
	 */
	protected void populateConfigObject() {
		if (configObject == null || PlaceholderAPIPlugin.getInstance() == null
				|| PlaceholderAPIPlugin.getInstance().getRootConfig() == null) {
			return;
		}
		ConfigurationNode node = PlaceholderAPIPlugin.getInstance().getRootConfig().getNode("expansions",
				Sponge.getPluginManager().fromInstance(plugin).get().getId(),
				(relational ? "rel_" : "") + id().toLowerCase().trim(), "data");
		if (node.isVirtual()) {
			try {
				ObjectMapper.forObject(configObject).serialize(node);
				PlaceholderAPIPlugin.getInstance().saveConfig();
			} catch (Exception e2) {
			}
		}
		try {
			configObject = ObjectMapper.forObject(configObject).populate(node);
		} catch (ObjectMappingException e1) {
			try {
				ObjectMapper.forObject(configObject).serialize(node);
				PlaceholderAPIPlugin.getInstance().saveConfig();
			} catch (Exception e2) {
			}
		}
	}

	/**
	 * Populate the configuration of this expansion. Will update the values
	 * received when calling getConfiguration().
	 */
	public void populateConfig() {
		populateConfigObject();
		ConfigurationNode node = PlaceholderAPIPlugin.getInstance().getRootConfig().getNode("expansions",
				Sponge.getPluginManager().fromInstance(plugin).get().getId(),
				(relational ? "rel_" : "") + id().toLowerCase().trim());
		if (node.isVirtual()) {
			node.getNode("enabled").setValue(enabled);
		} else {
			this.enabled = node.getNode("enabled").getBoolean(true);
			node.getNode("enabled").setValue(enabled);
		}
	}

	final void setId(String id) {
		this.id = fix(id);
	}

	final void setRelational(boolean relational) {
		this.relational = relational;
	}

	/**
	 * @return Whether the expansion is relational.
	 */
	public final boolean relational() {
		return this.relational;
	}

	/**
	 * Toggle whether this expansion is enabled.
	 */
	public final void toggleEnabled() {
		setEnabled(isEnabled());
	}

	/**
	 * @return Whether this expansion is enabled.
	 */
	public final boolean isEnabled() {
		return this.enabled;
	}

	/**
	 * Enable or disable this expansion.
	 * 
	 * @param enabled
	 *            Whether the expansion is enabled.
	 */
	public final void setEnabled(boolean enabled) {
		ConfigurationNode node = PlaceholderAPIPlugin.getInstance().getRootConfig().getNode("expansions",
				Sponge.getPluginManager().fromInstance(plugin).get().getId(),
				(relational ? "rel_" : "") + id().toLowerCase().trim());
		this.enabled = enabled;
		node.getNode("enabled").setValue(enabled);
	}

	/**
	 * Enable this expansion.
	 */
	public final void enable() {
		setEnabled(true);
	}

	/**
	 * Disable this expansion.
	 */
	public final void disable() {
		setEnabled(false);
	}

	private final void checkClasses() {
		Class<? extends V> v = getValueClass();
		if (this.valueClass == null || !v.isAssignableFrom(this.valueClass)) {
			this.valueClass = v;
		}
		Class<? extends S> s = getSourceClass();
		if (this.sourceClass == null || !s.isAssignableFrom(this.sourceClass)) {
			this.sourceClass = s;
		}
		Class<? extends O> o = getObserverClass();
		if (this.observerClass == null || !o.isAssignableFrom(this.observerClass)) {
			this.observerClass = o;
		}
	}

	/**
	 * @return The class representing the return type.
	 */
	@SuppressWarnings("unchecked")
	public final Class<? extends V> getValueClass() {
		Method m = getParseMethod();
		return (Class<? extends V>) m.getReturnType();
	}

	private final Method getParseMethod() {
		Class<?> clazz = this.getClass();
		return Arrays.asList(clazz.getDeclaredMethods()).stream()
				.filter(m -> m.getName().equalsIgnoreCase("parse") && Arrays.asList(m.getGenericParameterTypes())
						.stream().map(Type::getTypeName).anyMatch(s -> s.contains("java.util.Optional")))
				.findAny().get(); // we know it exists ;)
	}

	/**
	 * @return The class representing the source type.
	 */
	@SuppressWarnings("unchecked")
	public final Class<? extends S> getSourceClass() {
		Method m = getParseMethod();
		return (Class<? extends S>) m.getParameterTypes()[0];
	}

	/**
	 * @return The class representing the observer type.
	 */
	@SuppressWarnings("unchecked")
	public final Class<? extends O> getObserverClass() {
		Method m = getParseMethod();
		return (Class<? extends O>) m.getParameterTypes()[1];
	}

	/**
	 * Verify that the parameters of the parse method are extensions of User,
	 * CommandSource or Locatable.
	 * 
	 * @return Whether the parameters are valid.
	 */
	public final boolean verify() {
		Class<?> clazz = this.getClass();
		List<Class<?>> params = Arrays.asList(clazz.getDeclaredMethods()).stream()
				.filter(m -> m.getName().equalsIgnoreCase("parse") && Arrays.asList(m.getGenericParameterTypes())
						.stream().map(Type::getTypeName).anyMatch(s -> s.contains("java.util.Optional")))
				.map(m -> Arrays.asList(m.getParameterTypes())).map(List::stream).reduce(Stream.empty(), Stream::concat)
				.collect(Collectors.toList());
		try {
			return verifySource(params.get(0)) && verifySource(params.get(1));
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private static final boolean verifySource(Class<?> param) {
		return MessageReceiver.class.isAssignableFrom(param) || Locatable.class.isAssignableFrom(param)
				|| Subject.class.isAssignableFrom(param) || DataHolder.class.isAssignableFrom(param);
	}

	/**
	 * @return The author of the expansion.
	 */
	public final String author() {
		return author;
	}

	/**
	 * @return The description of the expansion.
	 */
	public final String description() {
		return desc;
	}

	/**
	 * @return The id of the expansion.
	 */
	public final String id() {
		return id;
	}

	/**
	 * @return The tokens this expansion supports. A null/empty token means %id%
	 *         is supported.
	 */
	public final List<String> tokens() {
		return tokens;
	}

	/**
	 * @return The version of the expansion.
	 */
	public final String version() {
		return ver;
	}

	/**
	 * @return The url of the expansion.
	 */
	public final URL url() {
		return url;
	}

	private static final String fix(String id) {
		id = id.toLowerCase().trim();
		if (id.startsWith("rel_")) {
			id = id.substring(4);
		}
		return id.replace("_", "").replace(" ", "");
	}

}