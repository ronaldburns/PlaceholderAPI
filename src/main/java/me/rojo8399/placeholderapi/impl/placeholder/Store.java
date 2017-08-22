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

import me.rojo8399.placeholderapi.Listening;
import me.rojo8399.placeholderapi.Observer;
import me.rojo8399.placeholderapi.Placeholder;
import me.rojo8399.placeholderapi.Relational;
import me.rojo8399.placeholderapi.Source;
import me.rojo8399.placeholderapi.Token;
import me.rojo8399.placeholderapi.impl.PlaceholderAPIPlugin;
import me.rojo8399.placeholderapi.impl.placeholder.gen.ClassPlaceholderFactory;
import me.rojo8399.placeholderapi.impl.placeholder.gen.DefineableClassLoader;
import me.rojo8399.placeholderapi.impl.placeholder.gen.InternalExpansion;
import me.rojo8399.placeholderapi.impl.utils.TypeUtils;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

/**
 * @author Wundero
 *
 */
public class Store {

	private static Store instance;

	public static Store get() {
		if (instance == null) {
			instance = new Store();
		}
		return instance;
	}

	private Store() {
	}

	private Map<String, Expansion<?, ?, ?>> normal = new ConcurrentHashMap<>(), rel = new ConcurrentHashMap<>();

	public Object parse(String id, boolean relational, Object src, Object obs, Optional<String> token)
			throws Exception {
		if (!has(id, relational)) {
			return null;
		}
		@SuppressWarnings("unchecked")
		Expansion<Object, Object, ?> exp = (Expansion<Object, Object, ?>) get(id, relational).get();
		if (!exp.isEnabled()) {
			return null;
		}
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
		return null;
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

	public boolean has(String id, boolean relational) {
		if (id == null) {
			return false;
		}
		return getMap(relational).containsKey(fix(id));
	}

	public boolean has(String id) {
		return has(id, true) || has(id, false);
	}

	public boolean isRelational(String id) {
		return rel.containsKey(fix(id));
	}

	public boolean isBoth(String id) {
		return isRelational(id) && isNormal(id);
	}

	public boolean isNormal(String id) {
		return normal.containsKey(fix(id));
	}

	public Optional<Expansion<?, ?, ?>> get(String id, boolean relational) {
		if (!has(id, relational)) {
			return Optional.empty();
		}
		return Optional.ofNullable(getMap(relational).get(id));
	}

	public List<String> allIds() {
		return Stream.concat(getMap(true).entrySet().stream(), getMap(false).entrySet().stream())
				.filter(e -> e.getValue().isEnabled()).map(Map.Entry::getKey).distinct().collect(Collectors.toList());
	}

	public List<String> ids(boolean relational) {
		return getMap(relational).entrySet().stream().filter(e -> e.getValue().isEnabled()).map(Map.Entry::getKey)
				.collect(Collectors.toList());
	}

	public int reloadAll() {
		return Stream.concat(getMap(true).keySet().stream(), getMap(false).keySet().stream()).distinct()
				.map(this::reload).map(b -> b ? 1 : 0).reduce(0, TypeUtils::add);
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
				out = ExpansionBuilderImpl.builder().fromUnknown(e).from(handle, id, e.getPlugin()).buildAndRegister();
			} catch (Exception e1) {
				return false;
			}
		}
		return out;
	}

	private Map<String, Expansion<?, ?, ?>> getMap(boolean rel) {
		return rel ? this.rel : normal;
	}

	private static final String fix(String id) {
		id = id.toLowerCase().trim();
		if (id.startsWith("rel_")) {
			id = id.substring(4);
		}
		return id.replace("_", "").replace(" ", "");
	}

	private final DefineableClassLoader classLoader = new DefineableClassLoader(
			Sponge.getEventManager().getClass().getClassLoader());
	private final ClassPlaceholderFactory factory = new ClassPlaceholderFactory(
			"me.rojo8399.placeholderapi.placeholder", classLoader);

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

	private int verify(Object object, Method m) {
		if (m.getReturnType().equals(Void.TYPE)) {
			return 2;
		}
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
					return verifyToken(px);
				}
				if (px.getAnnotation(Source.class) != null || px.getAnnotation(Observer.class) != null) {
					return verifySource(px);
				}
				return false;
			}).reduce(true, TypeUtils::and)) {
				return 7;
			}
			return 0;
		}
		return 1;
	}

	Expansion<?, ?, ?> createForMethod(Method m, Object o, Object plugin) {
		Class<?> c = o.getClass();
		boolean l = c.isAnnotationPresent(Listening.class), co = c.isAnnotationPresent(ConfigSerializable.class);
		int code = verify(o, m);
		if (code > 0) {
			switch (code) {
			case 1:
				PlaceholderAPIPlugin.getInstance().getLogger()
						.warn("This should not happen! Please report this bug on GitHub!");
				break;
			case 2:
				PlaceholderAPIPlugin.getInstance().getLogger().warn("Method cannot return void!");
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
			ConfigurationNode node = PlaceholderAPIPlugin.getInstance().getRootConfig().getNode("expansions",
					(r ? "rel_" : "") + Sponge.getPluginManager().fromInstance(plugin).get().getId(),
					p.id().toLowerCase().trim());
			if (node.isVirtual()) {
				try {
					ObjectMapper.forObject(o).serialize(node);
					PlaceholderAPIPlugin.getInstance().saveConfig();
				} catch (Exception e2) {
				}
			}
			try {
				o = ObjectMapper.forObject(o).populate(node);
			} catch (ObjectMappingException e1) {
				try {
					ObjectMapper.forObject(o).serialize(node);
					PlaceholderAPIPlugin.getInstance().saveConfig();
				} catch (Exception e2) {
				}
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
		return pl;
	}

	private static boolean verifyToken(Parameter param) {
		return Optional.class.isAssignableFrom(param.getType()) || String.class.isAssignableFrom(param.getType());
	}

	private static boolean verifySource(Parameter param) {
		return MessageReceiver.class.isAssignableFrom(param.getType())
				|| Locatable.class.isAssignableFrom(param.getType()) || Subject.class.isAssignableFrom(param.getType())
				|| DataHolder.class.isAssignableFrom(param.getType());
	}
}
