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

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Retention(RUNTIME)
@Target(METHOD)
/**
 * This class denotes a placeholder method. Only methods with this annotation
 * will be registered.
 */
public @interface Placeholder {

	/**
	 * @return The id of the placeholder.
	 */
	public String id();

	/**
	 * @return The author of the placeholder. Defaults to empty.
	 */
	public String author() default "";

	/**
	 * @return The version of the placeholder. Defaults to 1.0.
	 */
	public String version() default "1.0";

	/**
	 * @return The description of the placeholder. Defaults to empty.
	 */
	public String desc() default "";

	/**
	 * @return The possible tokens this placeholder can use (informational only, does not affect placeholder usage). Defaults to an empty array.
	 */
	public String[] tokens() default {};

	/**
	 * @return The url for the placeholder. Defaults to empty.
	 */
	public String url() default "";

}
