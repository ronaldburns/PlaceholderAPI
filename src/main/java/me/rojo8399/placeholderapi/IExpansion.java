package me.rojo8399.placeholderapi;

import java.net.URL;
import java.util.List;
import java.util.Optional;

public interface IExpansion<S, O, V> {

	/**
	 * Parse the given inputs into a placeholder value.
	 * 
	 * @param source
	 *            The source of the placeholder. For instance, if someone types a
	 *            message in chat, the source would be the message sender.
	 * @param observer
	 *            The observer of the placeholder. For instance, if someone types a
	 *            message in chat, the observer would be the message recipient.
	 * @param token
	 *            The token of the placeholder. This is any string appended to the
	 *            placeholder after it's id, and can be used for contextual data.
	 * @return The parsed value.
	 * @throws Exception
	 *             Throws this exception in order to protect itself from hard
	 *             failure.
	 */
	public V parse(S source, O observer, Optional<String> token) throws Exception;

	/**
	 * @return Whether the expansion is relational.
	 */
	public boolean relational();

	/**
	 * @return The author of the expansion.
	 */
	public String author();

	/**
	 * @return The description of the expansion.
	 */
	public String description();

	/**
	 * Enable this expansion.
	 */
	public default void enable() {
		setEnabled(true);
	}

	/**
	 * Disable this expansion.
	 */
	public default void disable() {
		setEnabled(false);
	}

	/**
	 * @return Whether the expansion is enabled.
	 */
	public boolean isEnabled();

	/**
	 * Set whether this expansion is enabled.
	 * 
	 * @param enabled
	 *            The state to set the plugin to.
	 */
	public void setEnabled(boolean enabled);

	/**
	 * @return The plugin which owns this expansion.
	 */
	public Object getPlugin();

	/**
	 * Decide what the user may have meant when inputting a token.
	 * 
	 * @param token
	 *            The token which was inputted by the user.
	 * @return A list of possible tokens which are approximate to the input.
	 */
	public List<String> getSuggestions(String token);

	/**
	 * @return The id of the expansion.
	 */
	public String id();

	/**
	 * @return The website of the expansion.
	 */
	public URL url();

	/**
	 * @return The version of the expansion.
	 */
	public String version();

}
