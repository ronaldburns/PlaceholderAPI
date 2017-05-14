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

import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author Wundero
 *
 */
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

	public static <T> Optional<T> tryOptional(Supplier<T> fun) {
		try {
			return Optional.of(fun.get());
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
