package me.rojo8399.placeholderapi;

import me.rojo8399.placeholderapi.expansions.Expansion;

public class RegistryEntry {
	
	// TODO add last update data + optional download link

	private final Expansion exp;
	private final String key;
	private final String version;
	private final String author;

	public RegistryEntry(Expansion exp) {
		this.exp = exp;
		this.key = exp.getIdentifier();
		this.version = exp.getVersion();
		this.author = exp.getAuthor();
	}

	/**
	 * @return the exp
	 */
	public Expansion getExpansion() {
		return exp;
	}

	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @return the author
	 */
	public String getAuthor() {
		return author;
	}

}
