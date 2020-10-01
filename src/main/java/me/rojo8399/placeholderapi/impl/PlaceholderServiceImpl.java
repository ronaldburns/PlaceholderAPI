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
package me.rojo8399.placeholderapi.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextTemplate;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Locatable;

import com.google.common.base.Preconditions;
import com.google.common.reflect.TypeToken;

import me.rojo8399.placeholderapi.ExpansionBuilder;
import me.rojo8399.placeholderapi.NoValueException;
import me.rojo8399.placeholderapi.PlaceholderService;
import me.rojo8399.placeholderapi.impl.configs.Messages;
import me.rojo8399.placeholderapi.impl.placeholder.Expansion;
import me.rojo8399.placeholderapi.impl.placeholder.ExpansionBuilderImpl;
import me.rojo8399.placeholderapi.impl.placeholder.Store;
import me.rojo8399.placeholderapi.impl.utils.TypeUtils;

/**
 * Implement placeholder service - should not need to be replaced but is a
 * service in case someone decides they need to.
 */
public class PlaceholderServiceImpl implements PlaceholderService {

	private static PlaceholderServiceImpl instance; // lazy instantiation

	public static PlaceholderServiceImpl get() {
		return instance == null ? instance = new PlaceholderServiceImpl() : instance;
	}

	private Store store = Store.get();

	private PlaceholderServiceImpl() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see me.rojo8399.placeholderapi.PlaceholderService#builder()
	 */
	@Override
	public <S, O, V> ExpansionBuilder<S, O, V, ? extends ExpansionBuilder<S, O, V, ?>> builder(Class<? extends S> s,
			Class<? extends O> o, Class<? extends V> v) {
		return ExpansionBuilderImpl.builder(s, o, v);
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

	private Map<String, Text> fillToText(TextTemplate template, Object source, Object observer) {
		validate(source, observer);
		Map<String, Object> rpt = rpt(source, observer, template, new HashMap<>());
		Map<String, Text> map = new HashMap<>();
		rpt.forEach((key, value) -> {
			Optional<Text> obj = TypeUtils.tryCast(value, Text.class);
			obj.ifPresent(text -> map.put(key, text));
		});
		return map;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see me.rojo8399.placeholderapi.PlaceholderService#load(java.lang.Object,
	 * java.lang.String, java.lang.Object)
	 */
	@Override
	public ExpansionBuilder<?, ?, ?, ?> load(Object handle, String id, Object plugin) {
		return ExpansionBuilderImpl.load(handle, id, plugin);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * me.rojo8399.placeholderapi.PlaceholderService#loadAll(java.lang.Object,
	 * java.lang.Object)
	 */
	@Override
	public List<? extends ExpansionBuilder<?, ?, ?, ?>> loadAll(Object handle, Object plugin) {
		return ExpansionBuilderImpl.loadAll(handle, plugin);
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
		Map<String, Object> replacement = rpt(source, observer, TextTemplate.of(TextTemplate.arg(placeholder)), null);
		if (replacement == null || replacement.isEmpty()) {
			return null;
		}
		if (!replacement.containsKey(placeholder)) {
			return null;
		}
		return replacement.get(placeholder);
	}

	public void refreshAll() {
		store.reloadAll();
	}

	public boolean refreshPlaceholder(String id) {
		return store.reload(id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see me.rojo8399.placeholderapi.PlaceholderService#registerExpansion(me.
	 * rojo8399.placeholderapi.placeholder.Expansion)
	 */
	@Override
	public boolean registerExpansion(Expansion<?, ?, ?> expansion) {
		return store.register(expansion);
	}

	/* (non-Javadoc)
	 * @see me.rojo8399.placeholderapi.PlaceholderService#registerTypeDeserializer(java.util.function.Function)
	 */
	@Override
	public <T> void registerTypeDeserializer(TypeToken<T> token, Function<String, T> deserializer) {
		TypeUtils.registerDeserializer(token, deserializer);
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
		return template.apply(fillToText(template, source, observer)).build();
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
					Text suggestions = Text.NEW_LINE;
					suggestions = suggestions.concat(Text.of(Messages.get().misc.suggestions.t(),
							Text.joinWith(Text.of(", "),
									store.allIds().stream().filter(str -> TypeUtils.closeTo(id, str)).map(Text::of)
											.collect(Collectors.toList()))));
					args.put(a,
							Text.of(TextColors.WHITE,
									TextActions.showText(Text.of(Messages.get().misc.invalid.t("ID"), suggestions)),
									template.getOpenArgString(), TextColors.RED, a, TextColors.WHITE,
									template.getCloseArgString()));
				}
				continue;
			}
			String token = noToken ? null : format.substring(index + 1);
			Object value;
			boolean empty = true;
			Text errorMsg = Messages.get().placeholder.tokenNeeded.t();
			List<String> suggestions = new ArrayList<>();
			try {
				value = store.parse(id, rel, s, o, Optional.ofNullable(token));
			} catch (Exception e) {
				if (e instanceof NoValueException) {
					value = null;
					empty = false;
					errorMsg = ((NoValueException) e).getTextMessage();
					suggestions = ((NoValueException) e).suggestions();
				} else {
					String cl = "";
					try {
						cl = e.getCause().getMessage() + " ";
					} catch (Exception ignored) {
					}
					value = Text.of(TextColors.RED,
							TextActions.showText(Text.of(TextColors.RED, "Check the console for details!")),
							"ERROR: " + cl + e.getMessage());
					e.printStackTrace();
				}
			}

			if (value == null && PlaceholderAPIPlugin.getInstance().getConfig().relationaltoregular && rel && empty) {
				try {
					value = store.parse(id, false, s, o, Optional.ofNullable(token));
				} catch (Exception e) {
					if (e instanceof NoValueException) {
						value = null;
						empty = false;
						errorMsg = ((NoValueException) e).getTextMessage();
						suggestions = ((NoValueException) e).suggestions();
					} else {
						String cl = "";
						try {
							cl = e.getCause().getMessage() + " ";
						} catch (Exception ignored) {
						}
						value = Text.of(TextColors.RED,
								TextActions.showText(Text.of(TextColors.RED, "Check the console for details!")),
								"ERROR: " + cl + e.getMessage());
						e.printStackTrace();
					}
				}
			}
			boolean enabled = store.get(id, false).orElseGet(() -> store.get(id, true).get()).isEnabled();
			if (value == null && !empty) {
				Text arg;
				if (noToken) {
					if (enabled) {
						arg = Text.of(TextColors.RED, TextActions.showText(Text.of(TextColors.RED, errorMsg)), a);
					} else {
						arg = Text.of(TextColors.RED, TextActions.showText(Messages.get().placeholder.notEnabled.t()),
								a);
					}

				} else {
					TextColor idCol;
					if (store.has(id)) {
						idCol = enabled ? TextColors.WHITE : TextColors.RED;
					} else {
						idCol = TextColors.RED;
					}
					Text sug = Text.EMPTY;
					if (!suggestions.isEmpty()) {
						sug = Text.of(Text.NEW_LINE, Messages.get().misc.suggestions, Text.joinWith(Text.of(", "),
								suggestions.stream().map(Text::of).collect(Collectors.toList())));
					}
					arg = Text.of(idCol, TextActions.showText(Text.of(TextColors.RED, errorMsg, sug)), id,
							TextColors.RED, "_" + token);
				}
				value = Text.of(TextColors.WHITE, template.getOpenArgString(), arg, TextColors.WHITE,
						template.getCloseArgString());
			} else if (value == null) {
				value = Text.EMPTY;
			}
			args.put(a, value);
		}
		return args;
	}

	private void validate(Object source, Object observer) {
		Preconditions.checkArgument(verifySource(source), "Source is not the right type!");
		Preconditions.checkArgument(verifySource(observer), "Observer is not the right type!");
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
		return source == null || (source instanceof Locatable || source instanceof MessageReceiver
				|| source instanceof Subject || source instanceof DataHolder);
	}

}
