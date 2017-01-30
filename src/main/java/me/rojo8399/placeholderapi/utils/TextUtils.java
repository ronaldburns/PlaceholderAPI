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

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextTemplate;
import org.spongepowered.api.text.serializer.TextSerializers;

import me.rojo8399.placeholderapi.PlaceholderServiceImpl;

/**
 * @author Wundero
 *
 */
public class TextUtils {

	/**
	 * Find all variables in a string and parse it into a text template.
	 */
	public static TextTemplate parse(final String i, Function<String, Text> parser) {
		if (i == null) {
			return null;
		}
		String in = i;
		if (!PlaceholderServiceImpl.PLACEHOLDER_PATTERN.matcher(in).find()) {
			return TextTemplate.of(TextSerializers.FORMATTING_CODE.deserialize(in));
		}
		String[] textParts = in.split(PlaceholderServiceImpl.PLACEHOLDER_PATTERN.pattern());
		/*
		 * if (textParts.length == 0) { return
		 * TextTemplate.of(TextTemplate.arg(in.substring(1, in.length() - 1)));
		 * }
		 */
		Matcher matcher = PlaceholderServiceImpl.PLACEHOLDER_PATTERN.matcher(in);
		TextTemplate out = textParts.length == 0 ? TextTemplate.of() : TextTemplate.of(parser.apply(textParts[0]));
		int x = 1;
		while ((matcher = matcher.reset(in)).find()) {
			String mg = matcher.group().substring(1);
			mg = mg.substring(0, mg.length() - 1);
			out = out.concat(TextTemplate.of(TextTemplate.arg(mg)));
			if (x < textParts.length) {
				out = out.concat(TextTemplate.of(parser.apply(textParts[x])));
			}
			in = matcher.replaceFirst("");
			x++;
		}
		return out;
	}

}
