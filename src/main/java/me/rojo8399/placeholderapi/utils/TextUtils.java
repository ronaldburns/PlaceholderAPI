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
package me.rojo8399.placeholderapi.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextTemplate;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextFormat;
import org.spongepowered.api.text.serializer.TextSerializers;

/**
 * @author Wundero
 *
 */
public class TextUtils {

	/**
	 * Find all variables in a string and parse it into a text template.
	 */
	public static TextTemplate parse(final String i, Pattern placeholderPattern) {
		if (i == null) {
			return null;
		}
		Function<String, Text> parser = TextSerializers.FORMATTING_CODE::deserialize;
		if (placeholderPattern == null) {
			placeholderPattern = Pattern.compile("[%]([^ %]+)[%]", Pattern.CASE_INSENSITIVE);
		}
		String in = i;
		if (!placeholderPattern.matcher(in).find()) {
			// No placeholders exist
			return TextTemplate.of(parser.apply(in));
		}
		// What is not a placeholder - can be empty
		String[] textParts = in.split(placeholderPattern.pattern());
		Matcher matcher = placeholderPattern.matcher(in);
		// Check if empty and create starting template
		TextTemplate out = textParts.length == 0 ? TextTemplate.of() : TextTemplate.of(parser.apply(textParts[0]));
		int x = 1;
		TextFormat last = textParts.length == 0 ? TextFormat.NONE : getLastFormat(parser.apply(textParts[0]));
		while ((matcher = matcher.reset(in)).find()) {
			String mg = matcher.group().substring(1); // Get actual placeholder
			mg = mg.substring(0, mg.length() - 1);
			// Get format for arg
			if (x <= textParts.length) {
				last = last.merge(getLastFormat(parser.apply(textParts[x - 1])));
			}
			// Make arg
			out = out.concat(TextTemplate.of(TextTemplate.arg(mg).format(last)));
			if (x < textParts.length) {
				// If there exists a part to insert
				out = out.concat(TextTemplate.of(fix(parser.apply(textParts[x]), last)));
			}
			in = matcher.replaceFirst("");
			x++;
		}
		return out;
	}
	
	public static Text ofItem(@Nullable ItemStack item) {
		if (item == null) {
			return Text.EMPTY;
		}
		return Text.of(TextActions.showItem(item.createSnapshot()), item.getOrElse(Keys.DISPLAY_NAME, Text.of(item)));
	}

	private static Text fix(Text to, TextFormat l) {
		return to.toBuilder().format(l.merge(to.getFormat())).build();
	}

	/**
	 * Get the last TextFormat object for a text and it's children.
	 */
	public static TextFormat getLastFormat(Text text) {
		if (text.getChildren().isEmpty()) {
			return text.getFormat();
		}
		return text.getChildren().stream().map(t -> t.getFormat()).reduce((t, t2) -> t.merge(t2))
				.orElse(text.getFormat());
	}

	/**
	 * Make a text object by repeating a text n times.
	 */
	public static Text repeat(Text original, int times) {
		Text out = original;
		for (int i = 0; i < times; i++) {
			out = out.concat(original);
		}
		return out;
	}

	/**
	 * Turn a text into a list of texts
	 */
	public static List<Text> flatten(Text text) {
		if (text == null || text.isEmpty()) {
			return new ArrayList<>();
		}
		List<Text> out = new ArrayList<>();
		List<Text> children = text.getChildren();
		out.add(text.toBuilder().removeAll().build());
		children.forEach(c -> out.addAll(flatten(c)));
		return out;
	}

	/**
	 * Convert from Text to TextTemplate
	 */
	public static TextTemplate toTemplate(Text text, Pattern pattern) {
		List<Text> flat = flatten(text);
		TextTemplate out = TextTemplate.EMPTY;
		for (Text t : flat) {
			String p = t.toPlain();
			Matcher m;
			if ((m = pattern.matcher(p)).matches()) {
				String ex = m.group(1);
				String pre = p.substring(0, p.indexOf(ex));
				String post = p.substring(p.indexOf(ex) + ex.length());
				out = out.concat(TextTemplate.of(pre, post,
						new Object[] { t.getFormat(), TextTemplate.arg(ex).format(t.getFormat()) }));
			} else if ((m = pattern.matcher(p)).find()) {
				out = out.concat(multi(p, pattern, t));
			} else {
				out = out.concat(TextTemplate.of(t));
			}
		}
		return out;
	}

	private static TextTemplate multi(String p, Pattern pattern, Text t) {
		TextTemplate out = TextTemplate.of();
		Matcher m = pattern.matcher(p);
		m.find();
		String p2 = m.group();
		String ex = m.group(1);
		String pre = p2.substring(0, p2.indexOf(ex));
		String post = p2.substring(p2.indexOf(ex) + ex.length());
		String pt = p.substring(0, p.indexOf(p2));
		String ppt = p.substring(p.indexOf(p2) + p2.length());
		boolean recurse = false;
		if (pattern.matcher(ppt).find()) {
			recurse = true;
		}
		Text.Builder ptt = Text.builder(pt).format(t.getFormat());
		Text.Builder pptt = Text.builder(ppt).format(t.getFormat());
		t.getClickAction().ifPresent(c -> {
			ptt.onClick(c);
			pptt.onClick(c);
		});
		t.getShiftClickAction().ifPresent(c -> {
			ptt.onShiftClick(c);
			pptt.onShiftClick(c);
		});
		t.getHoverAction().ifPresent(c -> {
			ptt.onHover(c);
			pptt.onHover(c);
		});
		Text pretext = ptt.build();
		Text posttext = pptt.build();
		if (recurse) {
			return out
					.concat(TextTemplate
							.of(pre, post,
									new Object[] { t.getFormat(), pretext, t.getFormat(),
											TextTemplate.arg(ex).format(t.getFormat()) }))
					.concat(multi(ppt, pattern, t));
		} else {
			return out.concat(TextTemplate.of(pre, post, new Object[] { t.getFormat(), pretext, t.getFormat(),
					TextTemplate.arg(ex).format(t.getFormat()), t.getFormat(), posttext }));
		}
	}

	/**
	 * Convert from TextTemplate to Text
	 */
	public static Text toText(TextTemplate template, Optional<Map<String, Object>> preexisting) {
		Map<String, Object> a = preexisting.orElse(new HashMap<>());
		template.getArguments().entrySet().stream().filter(e -> !a.containsKey(e.getKey())).forEach(
				e -> a.put(e.getKey(), template.getOpenArgString() + e.getKey() + template.getCloseArgString()));
		return template.apply(a).build();
	}

}
