package me.rojo8399.placeholderapi.expansions;

import java.util.Optional;

public interface ListeningExpansion extends Expansion {

	/**
	 * Return the plugin which the listener will be attached to.
	 * 
	 * If the optional is empty, PlaceholderAPI will use itself as the plugin.
	 */
	public Optional<Object> getPlugin();
	
}
