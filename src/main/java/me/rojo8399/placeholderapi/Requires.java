package me.rojo8399.placeholderapi;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Retention(RUNTIME)
@Target(METHOD)
public @interface Requires {

	/**
	 * todo describe version parsing.
	 */
	public String spongeVersion() default "[5.2)";

	/**
	 * Todo describe. id:version
	 * 
	 * version parsing as above
	 */
	public String[] plugins() default {};
}
