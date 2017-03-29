/*
 The MIT License (MIT)

 Copyright (c) 2016 Wundero

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

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextTemplate;
import org.spongepowered.api.text.format.TextColors;
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
	public static TextTemplate parse(final String i, Function<String, Text> parser, Pattern placeholderPattern) {
		if (i == null) {
			return null;
		}
		// default impl if not present
		if (parser == null) {
			parser = TextSerializers.FORMATTING_CODE::deserialize;
		}
		if (placeholderPattern == null) {
			placeholderPattern = Pattern.compile("[\\%]([^ \\%]+)[\\%]", Pattern.CASE_INSENSITIVE);
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
				last = strictMerge(last, getLastFormat(parser.apply(textParts[x - 1])));
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

	private static Text fix(Text to, TextFormat l) {
		return to.toBuilder().format(strictMerge(l, to.getFormat())).build();
	}

	/**
	 * Merge formats like vanilla Minecraft
	 * 
	 * Similar to onto.merge(from), except if from has TextColor RESET, the
	 * previous styles are abandoned
	 * 
	 * @param onto
	 *            the first part of the format
	 * @param from
	 * @return
	 */
	public static TextFormat strictMerge(TextFormat onto, TextFormat from) {
		// Vanilla merge resets style with reset color, sponge does not.
		if (from.getColor() == TextColors.RESET) { // reset style + color
			return from.color(TextColors.NONE);
		}
		return onto.merge(from); // else merge normally
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
	 * Return a text literal value of an object
	 */
	public static <T> Text textOf(T obj) {
		return Text.of(String.valueOf(obj));
	}

}
