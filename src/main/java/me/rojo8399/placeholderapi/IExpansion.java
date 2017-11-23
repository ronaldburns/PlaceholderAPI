package me.rojo8399.placeholderapi;

import java.net.URL;
import java.util.List;
import java.util.Optional;

public interface IExpansion<S, O, V> {

	public V parse(S source, O observer, Optional<String> token) throws Exception;

	public boolean relational();

	public String author();

	public String description();

	public default void enable() {
		setEnabled(true);
	}

	public default void disable() {
		setEnabled(false);
	}

	public boolean isEnabled();

	public void setEnabled(boolean enabled);

	public Object getPlugin();

	public List<String> getSuggestions(String token);

	public String id();

	public URL url();

	public String version();

}
