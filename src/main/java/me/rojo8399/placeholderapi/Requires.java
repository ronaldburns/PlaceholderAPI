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
	 * The required version range of Sponge in <b>Maven version range syntax</b>:
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
	public String spongeVersion() default "[5.2,)";

	/**
	 * An array of plugin dependencies for versions. Not particularly useful for
	 * most cases, but can be useful if you have multiple different systems for
	 * different versions of a plugin.
	 * 
	 * Format: plugin_id:version For example: placeholderapi:[4.3,)
	 * 
	 * The version matching is the same as described in the spongeVersion javadoc.
	 * 
	 * @see {@code #spongeVersion()}
	 */
	public String[] plugins() default {};
}
