package me.rojo8399.placeholderapi;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Attach this configurable field to a placeholder.
 * 
 * The field this is attached to needs the Setting annotation as well.
 * 
 * @author Wundero
 *
 */
@Documented
@Retention(RUNTIME)
@Target(FIELD)
public @interface Attach {

	/**
	 * Whether the placeholder is relational.
	 */
	boolean relational() default false;

	/**
	 * The placeholder to attach to.
	 */
	String value();

}
