package me.rojo8399.placeholderapi.expansions;

public abstract class Expansion {
	
	/**
	 * If any requirements are required to be checked before this hook can register, add them here
	 * @return true if this hook meets all the requirements to register
	 */
	public abstract boolean canRegister();
	
	/**
	 * Get the identifier that this placeholder expansion uses to be passed placeholder requests
	 * @return placeholder identifier that is associated with this class
	 */
	public abstract String getIdentifier();
	
	/**
	 * Get the author of this PlaceholderExpansion
	 * @return name of the author for this expansion
	 */
	public abstract String getAuthor();
	
	/**
	 * Get the version of this PlaceholderExpansion
	 * @return current version of this expansion
	 */
	public abstract String getVersion();
	
}
