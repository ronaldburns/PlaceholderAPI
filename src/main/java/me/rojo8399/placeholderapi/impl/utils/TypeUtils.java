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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import com.google.common.base.Preconditions;
import com.google.common.reflect.TypeToken;

import me.rojo8399.placeholderapi.impl.PlaceholderAPIPlugin;
import me.rojo8399.placeholderapi.impl.PlaceholderServiceImpl;

public class TypeUtils {

	private static Map<TypeToken<?>, Function<String, ?>> deserializers = new HashMap<>();

	private static final Pattern STRING_TO_VAL_PATTERN = Pattern
			.compile("(parse.*)|(valueOf)|(deserialize)|(fromString)|(from)", Pattern.CASE_INSENSITIVE);

	public static int add(int one, int two) {
		return one + two;
	}

	public static boolean and(boolean one, boolean two) {
		return one && two;
	}

	/**
	 * Case insensitive
	 */
	public static boolean closeTo(String a, String b) {
		if (a == null && b == null) {
			return true;
		}
		if (a == null || b == null) {
			return false;
		}
		if (a.isEmpty() && b.isEmpty()) {
			return true;
		}
		if (a.isEmpty() || b.isEmpty()) {
			return false;
		}
		if (a.equalsIgnoreCase(b)) {
			return true;
		}
		a = a.toLowerCase();
		b = b.toLowerCase();
		if (StringUtils.getJaroWinklerDistance(a, b) > 0.7) {
			return true;
		}
		if (a.contains(b) && b.length() > 1) {
			return true;
		}
		if (b.contains(a) && a.length() > 1) {
			return true;
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public static <T> T convertPrimitive(String val, Class<T> primitiveClass) {
		val = val.toLowerCase().trim();
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

	public static String formatDuration(Duration duration) {
		long seconds = duration.getSeconds();
		long absSeconds = Math.abs(seconds);
		String positive = String.format("%d h %d m %d s", absSeconds / 3600, (absSeconds % 3600) / 60, absSeconds % 60);
		return seconds < 0 ? "-" + positive : positive;
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

	public static int mult(int one, int two) {
		return one * two;
	}

	public static boolean nand(boolean one, boolean two) {
		return !and(one, two);
	}

	public static boolean nor(boolean one, boolean two) {
		return !or(one, two);
	}

	public static boolean or(boolean one, boolean two) {
		return one || two;
	}

	/**
	 * The required version range of the dependency in <b>Maven version range
	 * syntax</b>:
	 *
	 * <table>
	 * <tr>
	 * <th>Range</th>
	 * <th>Meaning</th>
	 * </tr>
	 * <tr>
	 * <td>1.0</td>
	 * <td>Any dependency version, 1.0 is recommended</td>
	 * </tr>
	 * <tr>
	 * <td>[1.0]</td>
	 * <td>x == 1.0</td>
	 * </tr>
	 * <tr>
	 * <td>[1.0,)</td>
	 * <td>x &gt;= 1.0</td>
	 * </tr>
	 * <tr>
	 * <td>(1.0,)</td>
	 * <td>x &gt; 1.0</td>
	 * </tr>
	 * <tr>
	 * <td>(,1.0]</td>
	 * <td>x &lt;= 1.0</td>
	 * </tr>
	 * <tr>
	 * <td>(,1.0)</td>
	 * <td>x &lt; 1.0</td>
	 * </tr>
	 * <tr>
	 * <td>(1.0,2.0)</td>
	 * <td>1.0 &lt; x &lt; 2.0</td>
	 * </tr>
	 * <tr>
	 * <td>[1.0,2.0]</td>
	 * <td>1.0 &lt;= x &lt;= 2.0</td>
	 * </tr>
	 * </table>
	 *
	 * @return The required version range, or an empty string if unspecified
	 * @see <a href="https://goo.gl/edrup4">Maven version range specification</a>
	 * @see <a href="https://goo.gl/WBsFIu">Maven version design document</a>
	 */
	public static boolean matchVersion(String pattern, String actual) {
		VersionRange range = new VersionRange(pattern);
		Version ver = new Version(actual);
		return range.isInRange(ver);
	}

	public static void registerDeserializer(TypeToken<?> token, Function<String, ?> fun) {
		Preconditions.checkNotNull(fun, "deserializer");
		Preconditions.checkNotNull(token, "token");
		deserializers.put(token, fun);
	}

	public static int sub(int one, int two) {
		return one - two;
	}

	@SuppressWarnings("unchecked")
	public static <T> Optional<T> tryCast(Object val, final Class<T> expected) {
		if (val == null) {
			return Optional.empty();
		}
		if (expected == null) {
			throw new IllegalArgumentException("Must provide an expected class!");
		}
		if (val instanceof BaseValue<?> && !BaseValue.class.isAssignableFrom(expected)) {
			return tryCast(((BaseValue<?>) val).get(), expected);
		}
		if (val instanceof Supplier) {
			Supplier<?> fun = (Supplier<?>) val;
			return tryCast(fun.get(), expected);
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
									.format(LocalDateTime.ofInstant((Instant) val, ZoneId.systemDefault())))));
				}
				if (val instanceof Duration) {
					String dur = formatDuration((Duration) val);
					return TypeUtils.tryOptional(() -> expected.cast(TextSerializers.FORMATTING_CODE.deserialize(dur)));
				}
				if (val instanceof LocalDateTime) {
					return TypeUtils.tryOptional(() -> expected.cast(TextSerializers.FORMATTING_CODE
							.deserialize(PlaceholderAPIPlugin.getInstance().formatter().format((LocalDateTime) val))));
				}
				if (val instanceof CommandSource) {
					return TypeUtils.tryOptional(() -> expected.cast(TextSerializers.FORMATTING_CODE
							.deserialize(String.valueOf(((CommandSource) val).getName()))));
				}
				if (val.getClass().isArray()) {
					List<Text> l2 = unboxPrimitiveArray(val).stream()
							.map(o -> tryCast(o, (Class<? extends Text>) expected)).flatMap(unmapOptional())
							.collect(Collectors.toList());
					return TypeUtils.tryOptional(() -> expected.cast(Text.joinWith(Text.of(", "), l2)));
				}
				if (val instanceof Iterable) {
					Iterable<?> l = (Iterable<?>) val;
					// should be safe cause we already checked assignability
					@SuppressWarnings("serial")
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
		if (val instanceof String) {
			if (String.class.isAssignableFrom(expected)) {
				return tryOptional(() -> expected.cast(val));
			}
			if (expected.isArray() && String.class.isAssignableFrom(expected.getComponentType())) {
				String v = (String) val;
				if (v.isEmpty()) {
					return Optional.empty();
				}
				if (!v.contains("_")) {
					return tryOptional(() -> expected.cast(new String[] { v }));
				}
				String[] x = v.split("_");
				if (x.length == 0) {
					return Optional.empty();
				}
				boolean ne = false;
				for (String s : x) {
					ne = ne || !s.isEmpty();
				}
				if (!ne) {
					return Optional.empty();
				}
				return tryOptional(() -> expected.cast(x));
			}
			if (List.class.isAssignableFrom(expected)
					&& String.class.isAssignableFrom(expected.getTypeParameters()[0].getGenericDeclaration())) {
				String v = (String) val;
				if (v.isEmpty()) {
					return Optional.empty();
				}
				if (!v.contains("_")) {
					return tryOptional(() -> expected.cast(Arrays.asList(v)));
				}
				String[] x = v.split("_");
				if (x.length == 0) {
					return Optional.empty();
				}
				boolean ne = false;
				for (String s : x) {
					ne = ne || !s.isEmpty();
				}
				if (!ne) {
					return Optional.empty();
				}
				return tryOptional(() -> expected.cast(Arrays.asList(x)));
			}
			Optional<T> opt = tryOptional(() -> convertPrimitive((String) val, expected));
			if (opt.isPresent()) {
				return opt;
			}
			opt = deserializers.entrySet().stream().filter(e -> e.getKey().isAssignableFrom(expected))
					.map(e -> e.getValue()).map(f -> tryOptional(() -> f.apply((String) val))).flatMap(unmapOptional())
					.findAny().flatMap(o -> tryOptional(() -> expected.cast(o)));
			if (opt.isPresent()) {
				return opt;
			}
			try {
				// should theoretically match any string -> object conversions, such as deser

				// for now im filtering against method names as well just to avoid issues where
				// expected result is not obtained due to weird methods, might change in future
				Method method = Arrays.asList(expected.getDeclaredMethods()).stream()
						.filter(m -> Modifier.isStatic(m.getModifiers()) && Modifier.isPublic(m.getModifiers()))
						.filter(m -> Arrays.asList(m.getParameterTypes()).stream().filter(c -> c.equals(String.class))
								.findAny().isPresent())
						.filter(m -> m.getReturnType().equals(expected) || m.getReturnType().equals(Optional.class))
						.filter(m -> STRING_TO_VAL_PATTERN.matcher(m.getName()).find()).findAny().get(); // error if no
				Object valout = method.invoke(null, (String) val);
				if (valout == null) {
					return Optional.empty();
				}
				if (expected.isInstance(valout)) {
					// Register a new deserializer once we confirm it works. Should prevent
					// extremely long parsing from happening multiple times.
					final MethodHandle mh = MethodHandles.publicLookup().unreflect(method);
					PlaceholderServiceImpl.get().registerTypeDeserializer(TypeToken.of(expected), str -> {
						try {
							return (T) expected.cast(mh.invokeExact((String) val));
						} catch (Throwable e1) {
							throw new RuntimeException(e1);
						}
					});
					return tryOptional(() -> expected.cast(valout));
				}
				if (valout instanceof Optional) {
					Optional<?> valopt = (Optional<?>) valout;
					if (!valopt.isPresent()) {
						return Optional.empty();
					}
					Object v = valopt.get();
					if (expected.isInstance(v)) {
						// Register a new deserializer once we confirm it works. Should prevent
						// extremely long parsing from happening multiple times.
						final MethodHandle mh = MethodHandles.publicLookup().unreflect(method);
						PlaceholderServiceImpl.get().registerTypeDeserializer(TypeToken.of(expected), str -> {
							try {
								Optional<?> optx = (Optional<?>) mh.invokeExact((String) val);
								return (T) expected.cast(optx.get());
							} catch (Throwable e1) {
								throw new RuntimeException(e1);
							}
						});
						return tryOptional(() -> expected.cast(v));
					} else {
						return Optional.empty();
					}
				}
				return Optional.empty();
			} catch (Exception e) {
				// fires if no method found, if invoke throws, if something else goes wrong
				return Optional.empty();
			}
		}
		return TypeUtils.tryOptional(() -> expected.cast(val));
	}

	public static <T> Optional<T> tryCast(Object val, final Class<T> expected, Boolean fixStrings) {
		if (val instanceof String && fixStrings) {
			return tryCast(((String) val).toLowerCase().trim(), expected);
		} else {
			return tryCast(val, expected);
		}
	}

	/**
	 * Will only work on unchecked but catchable exceptions.
	 */
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

	public static List<?> unboxPrimitiveArray(Object primArr) {
		if (primArr.getClass().isArray()) {
			if (primArr.getClass().getComponentType().isPrimitive()) {
				Class<?> primClazz = primArr.getClass().getComponentType();
				if (primClazz.equals(int.class)) {
					int[] a = (int[]) primArr;
					List<Integer> out = new ArrayList<>();
					for (int i : a) {
						out.add(i);
					}
					return out;
				}
				if (primClazz.equals(long.class)) {
					long[] a = (long[]) primArr;
					List<Long> out = new ArrayList<>();
					for (long i : a) {
						out.add(i);
					}
					return out;
				}
				if (primClazz.equals(short.class)) {
					short[] a = (short[]) primArr;
					List<Short> out = new ArrayList<>();
					for (short i : a) {
						out.add(i);
					}
					return out;
				}
				if (primClazz.equals(byte.class)) {
					byte[] a = (byte[]) primArr;
					List<Byte> out = new ArrayList<>();
					for (byte i : a) {
						out.add(i);
					}
					return out;
				}
				if (primClazz.equals(char.class)) {
					char[] a = (char[]) primArr;
					List<Character> out = new ArrayList<>();
					for (char i : a) {
						out.add(i);
					}
					return out;
				}
				if (primClazz.equals(boolean.class)) {
					boolean[] a = (boolean[]) primArr;
					List<Boolean> out = new ArrayList<>();
					for (boolean i : a) {
						out.add(i);
					}
					return out;
				}
				if (primClazz.equals(float.class)) {
					float[] a = (float[]) primArr;
					List<Float> out = new ArrayList<>();
					for (float i : a) {
						out.add(i);
					}
					return out;
				}
				if (primClazz.equals(double.class)) {
					double[] a = (double[]) primArr;
					List<Double> out = new ArrayList<>();
					for (double i : a) {
						out.add(i);
					}
					return out;
				}
			}
			return Arrays.asList((Object[]) primArr);
		} else {
			return Arrays.asList(primArr);
		}
	}

	public static <T> Function<Optional<? extends T>, Stream<? extends T>> unmapOptional() {
		return (opt) -> Stream.of(opt).filter(Optional::isPresent).map(Optional::get);
	}

	public static <T> Stream<? extends T> unmapOptional(Stream<Optional<? extends T>> stream) {
		return stream.flatMap(unmapOptional());
	}

	public static boolean xnor(boolean o, boolean t) {
		return (o && t) || (!o && !t);
	}

	public static boolean xor(boolean o, boolean t) {
		return !xnor(o, t);
	}

}
