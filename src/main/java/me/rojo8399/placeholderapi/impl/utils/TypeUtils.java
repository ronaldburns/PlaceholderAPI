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
package me.rojo8399.placeholderapi.impl.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import me.rojo8399.placeholderapi.impl.PlaceholderAPIPlugin;

public class TypeUtils {

	@SuppressWarnings("unchecked")
	public static <T> T convertPrimitive(String val, Class<T> primitiveClass) throws Exception {
		if (primitiveClass.equals(char.class) || primitiveClass.equals(Character.class)) {
			return (T) (Character) val.charAt(0);
		}
		if (primitiveClass.equals(int.class) || primitiveClass.equals(Integer.class)) {
			return (T) Integer.valueOf(val);
		}
		if (primitiveClass.equals(long.class) || primitiveClass.equals(Long.class)) {
			return (T) Long.valueOf(val);
		}
		if (primitiveClass.equals(short.class) || primitiveClass.equals(Short.class)) {
			return (T) Short.valueOf(val);
		}
		if (primitiveClass.equals(byte.class) || primitiveClass.equals(Byte.class)) {
			return (T) Byte.valueOf(val);
		}
		if (primitiveClass.equals(double.class) || primitiveClass.equals(Double.class)) {
			return (T) Double.valueOf(val);
		}
		if (primitiveClass.equals(float.class) || primitiveClass.equals(Float.class)) {
			return (T) Float.valueOf(val);
		}
		if (primitiveClass.equals(boolean.class) || primitiveClass.equals(Boolean.class)) {
			return (T) (Boolean) isTrue(val);
		}
		throw new IllegalArgumentException("Class is not primitive or a wrapper!");
	}

	public static <T> Optional<T> tryCast(Object val, final Class<T> expected) {
		if (val == null) {
			return Optional.empty();
		}
		if (expected == null) {
			throw new IllegalArgumentException("Must provide an expected class!");
		}
		if (Text.class.isAssignableFrom(expected)) {
			if (val instanceof Text) {
				return TypeUtils.tryOptional(() -> expected.cast(val));
			} else {
				if (val instanceof ItemStack) {
					return TypeUtils.tryOptional(() -> expected.cast(TextUtils.ofItem((ItemStack) val)));
				}
				if (val instanceof Instant) {
					return TypeUtils.tryOptional(() -> expected.cast(
							TextSerializers.FORMATTING_CODE.deserialize(PlaceholderAPIPlugin.getInstance().formatter()
									.format(LocalDateTime.ofInstant((Instant) val, ZoneOffset.systemDefault())))));
				}
				if (val instanceof LocalDateTime) {
					return TypeUtils.tryOptional(() -> expected.cast(TextSerializers.FORMATTING_CODE
							.deserialize(PlaceholderAPIPlugin.getInstance().formatter().format((LocalDateTime) val))));
				}
				if (val instanceof CommandSource) {
					return TypeUtils.tryOptional(() -> expected.cast(TextSerializers.FORMATTING_CODE
							.deserialize(String.valueOf(((CommandSource) val).getName()))));
				}
				if (val instanceof Supplier) {
					Supplier<?> fun = (Supplier<?>) val;
					return tryCast(fun.get(), expected);
				}
				if (val instanceof Iterable) {
					Iterable<?> l = (Iterable<?>) val;
					@SuppressWarnings("unchecked") // should be safe cause we already checked assignability
					final List<Text> l2 = new ArrayList<Object>() {
						{
							for (Object o : l) {
								add(o);
							}
						}
					}.stream().map(o -> tryCast(o, (Class<? extends Text>) expected)).flatMap(unmapOptional())
							.collect(Collectors.toList());
					return TypeUtils.tryOptional(() -> expected.cast(Text.joinWith(Text.of(", "), l2)));
				}
				return TypeUtils.tryOptional(
						() -> expected.cast(TextSerializers.FORMATTING_CODE.deserialize(String.valueOf(val))));
			}
		}
		return TypeUtils.tryOptional(() -> expected.cast(val));
	}

	public static <T> Function<Optional<T>, Stream<? extends T>> unmapOptional() {
		return (opt) -> Stream.of(opt).filter(Optional::isPresent).map(Optional::get);
	}

	public static boolean and(boolean one, boolean two) {
		return one && two;
	}

	public static int add(int one, int two) {
		return one + two;
	}

	public static boolean xor(boolean o, boolean t) {
		return !xnor(o, t);
	}

	public static boolean xnor(boolean o, boolean t) {
		return (o && t) || (!o && !t);
	}

	public static <T> Optional<T> tryOptional(Supplier<T> fun) {
		try {
			return Optional.ofNullable(fun.get());
		} catch (Exception e) {
			return Optional.empty();
		}
	}

	public static <T> T tryOrNull(Supplier<T> funct) {
		try {
			return funct.get();
		} catch (Exception e) {
			return null;
		}
	}

	public static boolean isTrue(String val) {
		switch (val.toLowerCase()) {
		case "t":
		case "true":
		case "1":
		case "y":
		case "yes":
			return true;
		}
		return false;
	}

}
