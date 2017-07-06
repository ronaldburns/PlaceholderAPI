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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextTemplate;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Locatable;

import com.google.common.base.Preconditions;

import me.rojo8399.placeholderapi.placeholder.FullContainer;
import me.rojo8399.placeholderapi.placeholder.Store;

/**
 * Implement placeholder service - should not need to be replaced but is a
 * service in case someone decides they need to.
 */
public class PlaceholderServiceImpl implements PlaceholderService {

	private static PlaceholderServiceImpl instance; // lazy instantiation

	private PlaceholderServiceImpl() {
	}

	public static PlaceholderServiceImpl get() {
		return instance == null ? instance = new PlaceholderServiceImpl() : instance;
	}

	public boolean refreshPlaceholder(String id) {
		if (store.norm(id) && store.rel(id)) {
			return store.reload(id, true) && store.reload(id, false);
		}
		return store.reload(id, store.rel(id));
	}

	public void refreshAll() {
		store.reload();
	}

	public List<FullContainer> getContainers(String id) {
		List<FullContainer> out = new ArrayList<>();
		store.cont(id, false).ifPresent(out::add);
		store.cont(id, true).ifPresent(out::add);
		return out;
	}

	Store getStore() {
		return store;
	}

	public List<String> getIds() {
		return Stream.concat(store.ids(false).stream(), store.ids(true).stream()).distinct()
				.collect(Collectors.toList());
	}

	/**
	 * Hold the expansions in a separate file
	 */
	private Store store = Store.get();

	/**
	 * Register expansion
	 */
	@Override
	public void registerPlaceholders(Supplier<Object> getter, Object plugin) {
		Preconditions.checkNotNull(getter, "getter");
		Preconditions.checkNotNull(plugin);
		store.register(getter, plugin);
	}

	/*
	 * Replace placeholders then parse value using the function
	 */
	private Map<String, Object> rpt(Object s, Object o, TextTemplate template, Map<String, Object> args) {
		if (args == null) {
			args = new HashMap<>();
		}
		// For every existing argument
		for (String a : template.getArguments().keySet()) {
			if (args.containsKey(a)) {
				continue;
			}
			String format = a.toLowerCase();
			boolean rel = !format.isEmpty() && format.toLowerCase().startsWith("rel");
			if (rel) {
				format = format.substring(4);
			}
			int index = format.indexOf("_");
			if (index == 0 || index == format.length()) {
				// We want to skip this but we cannot leave required arguments
				// so filler string is used.
				if (!template.getArguments().get(a).isOptional()) {
					args.put(a, template.getOpenArgString() + a + template.getCloseArgString());
				}
				continue;
			}
			boolean noToken = false;
			if (index == -1) {
				noToken = true;
				index = format.length();
			}
			String id = format.substring(0, index).toLowerCase();
			if (!store.has(id)) {
				// Again, filler string.
				if (!template.getArguments().get(a).isOptional()) {
					args.put(a, template.getOpenArgString() + a + template.getCloseArgString());
				}
				continue;
			}
			String token = noToken ? null : format.substring(index + 1);
			Text value = null;
			try {
				value = store.parse(id, rel, s, o, Optional.ofNullable(token), Text.class).orElse(null);
			} catch (Exception e) {
				value = Text.of(TextColors.RED, "ERROR: " + e.getMessage());
				e.printStackTrace();
			}

			if (value == null && PlaceholderAPIPlugin.getInstance().getConfig().relationaltoregular && rel) {
				try {
					value = store.parse(id, false, o, o, Optional.ofNullable(token), Text.class).orElse(null);
				} catch (Exception e) {
					value = Text.of(TextColors.RED, "ERROR: " + e.getMessage());
					e.printStackTrace();
				}
			}

			PlaceholderAPIPlugin.getInstance().getLogger().debug("Format: " + a + ", ID: " + id + ", Value : " + value);
			if (value == null) {
				value = Text.of(template.getOpenArgString() + a + template.getCloseArgString());
			}
			args.put(a, value);
		}
		return args;
	}

	private void validate(Object source, Object observer) {
		Preconditions.checkNotNull(source, "source");
		Preconditions.checkNotNull(source, "observer");
		Preconditions.checkArgument(verifySource(source), "Source is not the right type!");
		Preconditions.checkArgument(verifySource(source), "Observer is not the right type!");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see me.rojo8399.placeholderapi.PlaceholderService#fillPlaceholders(org.
	 * spongepowered.api.text.TextTemplate, java.lang.Object, java.lang.Object)
	 */
	@Override
	public Map<String, Object> fillPlaceholders(TextTemplate template, Object source, Object observer) {
		validate(source, observer);
		return rpt(source, observer, template, new HashMap<>());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * me.rojo8399.placeholderapi.PlaceholderService#parse(java.lang.String,
	 * java.lang.Object, java.lang.Object)
	 */
	@Override
	public Object parse(String placeholder, Object source, Object observer) {
		validate(source, observer);
		placeholder = placeholder.trim().toLowerCase();
		if (placeholder.contains("_")) {
			String id = placeholder.substring(0, placeholder.indexOf('_'));
			String token = placeholder.substring(id.indexOf('-'));
			try {
				Object out = store.parse(id, store.rel(id), source, observer,
						Optional.ofNullable(token.isEmpty() ? null : token));
				if (out == null && store.norm(id)) {
					return store.parse(id, false, source, observer,
							Optional.ofNullable(token.isEmpty() ? null : token));
				} else {
					return out;
				}
			} catch (Exception e) {
				return null;
			}
		} else {
			try {
				return store.parse(placeholder, store.rel(placeholder), source, observer, Optional.empty());
			} catch (Exception e) {
				return null;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * me.rojo8399.placeholderapi.PlaceholderService#replacePlaceholders(org.
	 * spongepowered.api.text.TextTemplate, java.lang.Object, java.lang.Object)
	 */
	@Override
	public Text replacePlaceholders(TextTemplate template, Object source, Object observer) {
		return template.apply(fillPlaceholders(template, source, observer)).build();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * me.rojo8399.placeholderapi.PlaceholderService#verifySource(java.lang.
	 * Object)
	 */
	@Override
	public boolean verifySource(Object source) {
		return source != null
				&& (source instanceof Locatable || source instanceof CommandSource || source instanceof User);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * me.rojo8399.placeholderapi.PlaceholderService#isRegistered(java.lang.
	 * String)
	 */
	@Override
	public boolean isRegistered(String id) {
		return store.has(id);
	}

}
