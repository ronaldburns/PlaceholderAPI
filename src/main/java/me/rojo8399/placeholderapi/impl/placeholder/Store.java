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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.world.Locatable;

import me.rojo8399.placeholderapi.Attach;
import me.rojo8399.placeholderapi.Listening;
import me.rojo8399.placeholderapi.NoValueException;
import me.rojo8399.placeholderapi.Observer;
import me.rojo8399.placeholderapi.Placeholder;
import me.rojo8399.placeholderapi.Relational;
import me.rojo8399.placeholderapi.Source;
import me.rojo8399.placeholderapi.Token;
import me.rojo8399.placeholderapi.impl.PlaceholderAPIPlugin;
import me.rojo8399.placeholderapi.impl.configs.Messages;
import me.rojo8399.placeholderapi.impl.placeholder.gen.ClassPlaceholderFactory;
import me.rojo8399.placeholderapi.impl.placeholder.gen.DefineableClassLoader;
import me.rojo8399.placeholderapi.impl.placeholder.gen.InternalExpansion;
import me.rojo8399.placeholderapi.impl.utils.TypeUtils;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

/**
 * @author Wundero
 *
 */
public class Store {

	private static Store instance;

	static Method find(Object object, String id, boolean rel) {
		for (Method m : object.getClass().getMethods()) {
			Placeholder p;
			if ((p = m.getAnnotation(Placeholder.class)) != null && fix(p.id()).equals(fix(id))) {
				if (rel) {
					if (m.isAnnotationPresent(Relational.class)) {
						return m;
					} else {
						continue;
					}
				}
				return m;
			}
		}
		return null;
	}

	static Map<Method, Boolean> findAll(Object object) {
		Class<?> c = object.getClass();
		if (!Modifier.isPublic(c.getModifiers())) {
			throw new IllegalArgumentException("Class must be public!");
		}
		Map<Method, Boolean> out = new HashMap<>();
		for (Method m : c.getMethods()) {
			if (!Modifier.isPublic(m.getModifiers())) {
				continue;
			}
			if (m.isAnnotationPresent(Placeholder.class)) {
				out.put(m, m.isAnnotationPresent(Relational.class));
			}
		}
		return out;
	}

	private static final String fix(String id) {
		id = id.toLowerCase().trim();
		if (id.startsWith("rel_")) {
			id = id.substring(4);
		}
		return id.replace("_", "").replace(" ", "");
	}

	public static Store get() {
		if (instance == null) {
			instance = new Store();
		}
		return instance;
	}

	private static boolean verifySource(Parameter param) {
		return MessageReceiver.class.isAssignableFrom(param.getType())
				|| Locatable.class.isAssignableFrom(param.getType()) || Subject.class.isAssignableFrom(param.getType())
				|| DataHolder.class.isAssignableFrom(param.getType());
	}

	private final DefineableClassLoader classLoader = new DefineableClassLoader(
			Sponge.getEventManager().getClass().getClassLoader());

	private final ClassPlaceholderFactory factory = new ClassPlaceholderFactory(
			"me.rojo8399.placeholderapi.placeholder", classLoader);

	private Map<String, Expansion<?, ?, ?>> normal = new ConcurrentHashMap<>(), rel = new ConcurrentHashMap<>();

	private Store() {
	}

	public List<String> allIds() {
		return Stream.concat(getMap(true).entrySet().stream(), getMap(false).entrySet().stream())
				.filter(e -> e.getValue().isEnabled()).map(Map.Entry::getKey).distinct().collect(Collectors.toList());
	}

	Expansion<?, ?, ?> createForMethod(Method m, Object o, Object plugin) {
		Class<?> c = o.getClass();
		boolean l = c.isAnnotationPresent(Listening.class), co = c.isAnnotationPresent(ConfigSerializable.class);
		int code = verify(o, m);
		if (code > 0) {
			PlaceholderAPIPlugin.getInstance().getLogger()
					.warn("Method " + m.getName() + " in " + o.getClass().getName() + " cannot be loaded!");
			switch (code) {
			case 1:
				PlaceholderAPIPlugin.getInstance().getLogger()
						.warn("This should not happen! Please report this bug on GitHub!");
				break;
			case 5:
			case 6:
				PlaceholderAPIPlugin.getInstance().getLogger().warn("Placeholder already registered!");
				break;
			case 7:
			case 4:
				PlaceholderAPIPlugin.getInstance().getLogger().warn("Method contains incorrect or extra parameters!");
				break;
			}
			return null;
		}
		Placeholder p = m.getAnnotation(Placeholder.class);
		boolean r = m.isAnnotationPresent(Relational.class);
		Expansion<?, ?, ?> pl;
		try {
			pl = factory.create(o, m);
		} catch (Exception e) {
			PlaceholderAPIPlugin.getInstance().getLogger().warn("An exception occured while creating the placeholder!");
			e.printStackTrace();
			return null;
		}
		if (co) {
			try {
				this.fillExpansionConfig(pl);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (l) {
			final Object o2 = o;
			pl.setReloadListeners(() -> {
				PlaceholderAPIPlugin.getInstance().unregisterListeners(o2);
				PlaceholderAPIPlugin.getInstance().registerListeners(o2, plugin);
			});
		}
		pl.setId(p.id());
		pl.setRelational(r);
		pl.reloadListeners();
		pl.refresh();
		return pl;
	}

	/**
	 * Super sketchy deserialization ;)
	 */
	public void fillExpansionConfig(Expansion<?, ?, ?> exp) throws Exception {
		Class<?> fieldData = Class.forName(ObjectMapper.class.getName() + "$FieldData");
		Class<?> expClass = exp.getConfiguration().getClass();
		ObjectMapper<?> mapper = ObjectMapper.forClass(expClass);
		Field fieldDataMap = mapper.getClass().getDeclaredField("cachedFields");
		fieldDataMap.setAccessible(true);
		@SuppressWarnings("unchecked")
		Map<String, ?> map = (Map<String, ?>) fieldDataMap.get(mapper);
		for (Map.Entry<String, ?> entry : map.entrySet()) {
			Object fd = entry.getValue();
			Field fx = fieldData.getDeclaredField("field");
			fx.setAccessible(true);
			Field actual = (Field) fx.get(fd);
			actual.setAccessible(true);
			if (actual.isAnnotationPresent(Attach.class)
					&& actual.getAnnotation(Attach.class).value().equalsIgnoreCase(exp.id())
					&& actual.getAnnotation(Attach.class).relational() == exp.relational()) {
				fieldData.getDeclaredMethod("deserializeFrom", Object.class, ConfigurationNode.class).invoke(fd,
						exp.getConfiguration(), getNode(exp).getNode(entry.getKey()));
			} else if (!actual.isAnnotationPresent(Attach.class)) {
				PlaceholderAPIPlugin.getInstance().getLogger().warn("Field " + fx.getName() + " in placeholder id="
						+ exp.id() + "\'s config is not attached to a placeholder!");
			}
		}
	}

	public Optional<Expansion<?, ?, ?>> get(String id, boolean relational) {
		if (!has(id, relational)) {
			return Optional.empty();
		}
		return Optional.ofNullable(getMap(relational).get(id));
	}

	private Map<String, Expansion<?, ?, ?>> getMap(boolean rel) {
		return rel ? this.rel : normal;
	}

	public ConfigurationNode getNode(Expansion<?, ?, ?> exp) {
		if (exp.getConfiguration() == null) {
			return null;
		}
		String plid = Sponge.getPluginManager().fromInstance(exp.getPlugin()).get().getId();
		return PlaceholderAPIPlugin.getInstance().getRootConfig().getNode("expansions", plid,
				(exp.relational() ? "rel_" : "") + exp.id(), "data");
	}

	public Optional<Class<?>> getObserverType(Method m) {
		return getType(m, Observer.class);
	}

	public Optional<Class<?>> getSourceType(Method m) {
		return getType(m, Source.class);
	}

	public Optional<Class<?>> getTokenType(Method m) {
		return getType(m, Token.class);
	}

	private Optional<Class<?>> getType(Method m, Class<? extends Annotation> annotation) {
		List<Parameter> params = Arrays.asList(m.getParameters());
		if (!params.stream().anyMatch(p -> p.isAnnotationPresent(annotation))) {
			return Optional.empty();
		}
		return Optional.of(params.stream().filter(p -> p.isAnnotationPresent(annotation)).findAny().get().getType());
	}

	public boolean has(String id) {
		return has(id, true) || has(id, false);
	}

	public boolean has(String id, boolean relational) {
		if (id == null) {
			return false;
		}
		return getMap(relational).containsKey(fix(id));
	}

	public List<String> ids(boolean relational) {
		return getMap(relational).entrySet().stream().filter(e -> e.getValue().isEnabled()).map(Map.Entry::getKey)
				.collect(Collectors.toList());
	}

	public boolean isBoth(String id) {
		return isRelational(id) && isNormal(id);
	}

	public boolean isNormal(String id) {
		return normal.containsKey(fix(id));
	}

	public boolean isRelational(String id) {
		return rel.containsKey(fix(id));
	}

	public Object parse(String id, boolean relational, Object src, Object obs, Optional<String> token)
			throws Exception {
		if (!has(id, relational)) {
			return null;
		}
		@SuppressWarnings("unchecked")
		Expansion<Object, Object, ?> exp = (Expansion<Object, Object, ?>) get(id, relational).get();
		if (!exp.isEnabled()) {
			throw new NoValueException(Messages.get().placeholder.notEnabled.t());
		}
		try {
			if (src == null || obs == null) {
				if (src != null && exp.getSourceClass().isAssignableFrom(src.getClass())) {
					return exp.parse(exp.convertSource(src), null, token);
				} else if (obs != null && exp.getSourceClass().isAssignableFrom(obs.getClass())) {
					return exp.parse(null, exp.convertObserver(obs), token);
				} else {
					return exp.parse(null, null, token);
				}
			}
			if (exp.getSourceClass().isAssignableFrom(src.getClass())
					&& exp.getObserverClass().isAssignableFrom(obs.getClass())) {
				return exp.parse(exp.convertSource(src), exp.convertObserver(obs), token);
			}
		} catch (NoValueException e) {
			throw new NoValueException(e.getMessage(), exp.getSuggestions(token.orElse(null)));
		}
		throw new NoValueException(Messages.get().placeholder.invalidSrcObs.t());
	}

	public <T> Optional<T> parse(String id, boolean relational, Object src, Object obs, Optional<String> token,
			Class<T> expected) throws Exception {
		Object o = parse(id, relational, src, obs, token);
		return TypeUtils.tryCast(o, expected);
	}

	public boolean register(Expansion<?, ?, ?> expansion) {
		String id = fix(expansion.id());
		if (getMap(expansion.relational()).containsKey(id)) {
			return false;
		}
		expansion.populateConfig();
		expansion.reloadListeners();
		getMap(expansion.relational()).put(id, expansion);
		return true;
	}

	private boolean reload(Expansion<?, ?, ?> e, String id, boolean rel) {
		if (!(e instanceof Expansion)) {
			return false;
		}
		if (!((Expansion<?, ?, ?>) e).refresh()) {
			return false;
		}
		boolean out = true;
		if (e instanceof InternalExpansion<?, ?, ?>) {
			Object handle = ((InternalExpansion<?, ?, ?>) e).getHandle();
			getMap(rel).remove(id);
			try {
				out = ExpansionBuilderImpl.builder(e.getSourceClass(), e.getObserverClass(), e.getValueClass())
						.fromUnknown(e).from(handle, id, e.getPlugin()).buildAndRegister();
			} catch (Exception e1) {
				return false;
			}
		}
		return out;
	}

	public boolean reload(String id) {
		if (!has(id)) {
			return false;
		}
		id = fix(id);
		Optional<Expansion<?, ?, ?>> rel = get(id, true);
		Optional<Expansion<?, ?, ?>> norm = get(id, false);
		boolean out = true;
		if (rel.isPresent()) {
			out = out && reload(rel.get(), id, true);
		}
		if (norm.isPresent()) {
			out = out && reload(norm.get(), id, false);
		}
		return out;
	}

	public int reloadAll() {
		return Stream.concat(getMap(true).keySet().stream(), getMap(false).keySet().stream()).distinct()
				.map(this::reload).map(b -> b ? 1 : 0).reduce(0, TypeUtils::add);
	}

	public void saveAll() {
		Stream.concat(getMap(true).values().stream(), getMap(false).values().stream()).forEach(Expansion::saveConfig);
	}

	public void saveExpansionConfig(Expansion<?, ?, ?> exp) throws Exception {
		Class<?> objectMapper = ObjectMapper.class;
		Class<?> fieldData = Class.forName(objectMapper.getName() + "$FieldData");
		Class<?> expClass = exp.getConfiguration().getClass();
		ObjectMapper<?> mapper = ObjectMapper.forClass(expClass);
		Field fieldDataMap = mapper.getClass().getDeclaredField("cachedFields");
		fieldDataMap.setAccessible(true);
		@SuppressWarnings("unchecked")
		Map<String, ?> map = (Map<String, ?>) fieldDataMap.get(mapper);
		for (Map.Entry<String, ?> entry : map.entrySet()) {
			Object fd = entry.getValue();
			Field fx = fieldData.getDeclaredField("field");
			fx.setAccessible(true);
			Field actual = (Field) fx.get(fd);
			actual.setAccessible(true);
			if (actual.isAnnotationPresent(Attach.class)
					&& actual.getAnnotation(Attach.class).value().equalsIgnoreCase(exp.id())
					&& actual.getAnnotation(Attach.class).relational() == exp.relational()) {
				fieldData.getDeclaredMethod("serializeTo", Object.class, ConfigurationNode.class).invoke(fd,
						exp.getConfiguration(), getNode(exp).getNode(entry.getKey()));
			} else if (!actual.isAnnotationPresent(Attach.class)) {
				PlaceholderAPIPlugin.getInstance().getLogger().warn("Field " + fx.getName() + " in placeholder id="
						+ exp.id() + "\'s config is not attached to a placeholder!");
			}
		}
	}

	private int verify(Object object, Method m) {
		Placeholder p;
		if ((p = m.getAnnotation(Placeholder.class)) != null) {
			String id = fix(p.id());
			List<Parameter> params = Arrays.asList(m.getParameters());
			boolean relational = m.isAnnotationPresent(Relational.class);
			if (relational) {
				if (this.rel.containsKey(id)) {
					return 5;
				}
			} else {
				if (this.normal.containsKey(id)) {
					return 6;
				}
			}
			if (!params.stream().map(px -> {
				if (px.getAnnotation(Token.class) != null) {
					return true;
				}
				if (px.getAnnotation(Source.class) != null || px.getAnnotation(Observer.class) != null) {
					return verifySource(px);
				}
				return false;
			}).reduce(true, TypeUtils::and)) {
				return 7;
			}
			if (params.stream().map(px -> {
				if (px.getAnnotation(Token.class) != null) {
					return true;
				}
				if (px.getAnnotation(Source.class) != null || px.getAnnotation(Observer.class) != null) {
					return verifySource(px);
				}
				return false;
			}).filter(px -> px).count() != params.size()) {
				return 4;
			}
			return 0;
		}
		return 1;
	}
}
