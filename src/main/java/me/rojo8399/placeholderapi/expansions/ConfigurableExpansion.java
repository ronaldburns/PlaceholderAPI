package me.rojo8399.placeholderapi.expansions;

import com.google.common.reflect.TypeToken;

import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public interface ConfigurableExpansion extends Expansion {

	/**
	 * Get the TypeToken with which to load the object.
	 * 
	 * @return the token to load the object with
	 */
	public TypeToken<? extends ConfigurableExpansion> getToken();

}
