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
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.world.Locatable;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import me.rojo8399.placeholderapi.PlaceholderAPIPlugin;
import me.rojo8399.placeholderapi.placeholder.gen.ClassPlaceholderFactory;
import me.rojo8399.placeholderapi.placeholder.gen.DefineableClassLoader;
import me.rojo8399.placeholderapi.placeholder.gen.InternalPlaceholder;
import me.rojo8399.placeholderapi.utils.TypeUtils;
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

	public Set<String> ids(boolean rel) {
		return getLoader(rel).keySet();
	}

	private Map<String, LoaderTuple> getLoader(boolean rel) {
		return rel ? relLoader : normalLoader;
	}

	public boolean has(String id, boolean... rel) {
		id = fix(id);
		if (rel.length == 0) {
			return normalLoader.containsKey(id) || this.relLoader.containsKey(id);
		}
		return rel[0] ? this.relLoader.containsKey(id) : normalLoader.containsKey(id);
	}

	private static String fix(String id) {
		return id.toLowerCase().trim().replaceFirst("rel\\_", "");
	}

	public Optional<FullContainer> cont(String id, boolean rel) {
		if (!has(id, rel)) {
			return Optional.empty();
		}
		id = fix(id);
		try {
			return Optional.ofNullable(getCache(rel).get(id));
		} catch (Exception e) {
			return Optional.empty();
		}
	}

	public void reload() {
		normal.invalidateAll();
		rel.invalidateAll();
		for (LoaderTuple n : normalLoader.values()) {
			List<FullContainer> l = createAll(n.getter, n.plugin, false);
			l.forEach(c -> {
				normal.put(fix(c.id()), c);
			});
		}

		for (LoaderTuple n : relLoader.values()) {
			List<FullContainer> l = createAll(n.getter, n.plugin, true);
			l.forEach(c -> {
				rel.put(fix(c.id()), c);
			});
		}
	}

	public boolean rel(String id) {
		return rel.asMap().containsKey(fix(id));
	}

	public boolean norm(String id) {
		return normal.asMap().containsKey(fix(id));
	}

	private LoadingCache<String, FullContainer> getCache(boolean rel) {
		return rel ? this.rel : this.normal;
	}

	public boolean isEnabled(String id, boolean rel) {
		if (!has(id, rel)) {
			return false;
		}
		LoadingCache<String, FullContainer> cache = getCache(rel);
		try {
			return cache.get(fix(id)).isEnabled();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void disable(String id, boolean rel) {
		if (!has(id, rel)) {
			return;
		}
		LoadingCache<String, FullContainer> cache = getCache(rel);
		try {
			cache.get(fix(id)).disable();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

	public void enable(String id, boolean rel) {
		if (!has(id, rel)) {
			return;
		}
		LoadingCache<String, FullContainer> cache = getCache(rel);
		try {
			cache.get(fix(id)).enable();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

	public boolean reload(String id, boolean rel) {
		if (!has(id, rel)) {
			return false;
		}
		id = fix(id);
		try {
			if (rel) {
				this.rel.get(id).unregisterListeners();
				this.rel.refresh(id);
			} else {
				this.normal.get(id).unregisterListeners();
				this.normal.refresh(id);
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public <T> Optional<T> parse(String p, boolean r, Object s, Object o, Optional<String> t, Class<T> expected)
			throws Exception {
		Object val = parse(p, r, s, o, t);
		return TypeUtils.tryCast(val, expected);
	}

	public Object parse(String id, boolean rel, Object source, Object observer, Optional<String> token)
			throws Exception {
		id = fix(id);
		if (!has(id, rel)) {
			return null;
		}
		if (rel) {
			return this.rel.get(id).replace(source, observer, token);
		} else {
			return this.normal.get(id).replace(source, observer, token);
		}
	}

	private final DefineableClassLoader classLoader = new DefineableClassLoader(
			Sponge.getEventManager().getClass().getClassLoader());
	private final ClassPlaceholderFactory factory = new ClassPlaceholderFactory(
			"me.rojo8399.placeholderapi.placeholder", classLoader);

	private final LoadingCache<String, FullContainer> normal = CacheBuilder.newBuilder().initialCapacity(15)
			.concurrencyLevel(4).removalListener(not -> ((FullContainer) not.getValue()).unregisterListeners())
			.build(new CacheLoader<String, FullContainer>() {

				@Override
				public FullContainer load(String key) throws Exception {
					key = fix(key);
					if (!normalLoader.containsKey(key)) {
						throw new IllegalArgumentException("Placeholder must be registered!");
					}
					LoaderTuple t = normalLoader.get(key);
					return create(t.getter, key, t.plugin, false);
				}

			});
	private final LoadingCache<String, FullContainer> rel = CacheBuilder.newBuilder().initialCapacity(15)
			.concurrencyLevel(4).removalListener(not -> ((FullContainer) not.getValue()).unregisterListeners())
			.build(new CacheLoader<String, FullContainer>() {

				@Override
				public FullContainer load(String key) throws Exception {
					key = fix(key);
					if (!relLoader.containsKey(key)) {
						throw new IllegalArgumentException("Placeholder must be registered!");
					}
					LoaderTuple t = relLoader.get(key);
					return create(t.getter, key, t.plugin, true);
				}

			});

	public void register(Supplier<Object> getter, Object plugin) {
		Object o = getter.get();
		Preconditions.checkNotNull(o, "object");
		LoaderTuple t = new LoaderTuple(getter, plugin);
		List<String> ids = findNames(o, false);
		for (String s : ids) {
			s = fix(s);
			if (!normalLoader.containsKey(s)) {
				normalLoader.put(s, t);
			}
		}
		ids = findNames(o, true);
		for (String s : ids) {
			s = fix(s);
			if (!relLoader.containsKey(s)) {
				relLoader.put(s, t);
			}
		}
	}

	private final Map<String, LoaderTuple> normalLoader = new HashMap<>(), relLoader = new HashMap<>();

	private static class LoaderTuple {
		private Supplier<Object> getter;
		private Object plugin;

		public LoaderTuple(Supplier<Object> getter, Object plugin) {
			this.getter = getter;
			this.plugin = plugin;
		}

		public Supplier<Object> getter() {
			return getter;
		}

		public Object plugin() {
			return plugin;
		}
	}

	private static List<String> findNames(Object object, boolean rel) {
		return findAll(object, rel).stream().map(m -> m.getAnnotation(Placeholder.class).id())
				.collect(Collectors.toList());
	}

	private static List<Method> findAll(Object object, boolean rel) {
		return Arrays.asList(object.getClass().getMethods()).stream()
				.filter(m -> TypeUtils.xnor(m.isAnnotationPresent(Relational.class), rel))
				.filter(m -> m.isAnnotationPresent(Placeholder.class)).collect(Collectors.toList());
	}

	private static Method find(Object object, String id, boolean rel) {
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
				if (this.rel.asMap().containsKey(id)) {
					return 5;
				}
			} else {
				if (this.normal.asMap().containsKey(id)) {
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

	private List<FullContainer> createAll(Supplier<Object> getter, Object plugin, boolean rel) {
		Object o = getter.get();
		Preconditions.checkNotNull(o, "object");
		List<FullContainer> out = new ArrayList<>();
		for (Method m : findAll(o, rel)) {
			out.add(createForMethod(m, o, plugin));
		}
		return out;
	}

	private FullContainer create(Supplier<Object> getter, String id, Object plugin, boolean rel) {
		Object o = getter.get();
		Preconditions.checkNotNull(o, "object");
		Method m = find(o, id, rel);
		if (m == null) {
			PlaceholderAPIPlugin.getInstance().getLogger().warn("No placeholder found with id " + id + " in object!");
			return null;
		}
		return createForMethod(m, o, plugin);
	}

	private FullContainer createForMethod(Method m, Object o, Object plugin) {
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
		InternalPlaceholder pl;
		try {
			pl = factory.create(o, m);
		} catch (Exception e) {
			PlaceholderAPIPlugin.getInstance().getLogger().warn("An exception occured while creating the placeholder!");
			e.printStackTrace();
			return null;
		}
		if (co) {
			ConfigurationNode node = PlaceholderAPIPlugin.getInstance().getRootConfig().getNode("expansions",
					Sponge.getPluginManager().fromInstance(plugin).get().getId(),
					o.getClass().getSimpleName().replace(".class", "").toLowerCase().trim());
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
			Sponge.getEventManager().registerListeners(plugin, o);
		}
		boolean r = m.isAnnotationPresent(Relational.class);
		Placeholder p = m.getAnnotation(Placeholder.class);
		return FullContainer.from(p, pl, r, plugin);
	}

	private static boolean verifyToken(Parameter param) {
		return Optional.class.isAssignableFrom(param.getType()) || String.class.isAssignableFrom(param.getType());
	}

	private static boolean verifySource(Parameter param) {
		return CommandSource.class.isAssignableFrom(param.getType())
				|| Locatable.class.isAssignableFrom(param.getType()) || User.class.isAssignableFrom(param.getType());
	}
}
