package me.rojo8399.placeholderapi;

import me.rojo8399.placeholderapi.expansions.Expansion;

public class RegistryEntry {

	private Expansion exp;
	private String key;
	private String version;
	private String author;

	public RegistryEntry(Expansion exp) {
		this.setExpansion(exp);
		this.setKey(exp.getIdentifier());
		this.setVersion(exp.getVersion());
		this.setAuthor(exp.getAuthor());
	}

	/**
	 * @return the exp
	 */
	public Expansion getExpansion() {
		return exp;
	}

	/**
	 * @param exp
	 *            the exp to set
	 */
	public void setExpansion(Expansion exp) {
		this.exp = exp;
	}

	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @param key
	 *            the key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param version
	 *            the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * @return the author
	 */
	public String getAuthor() {
		return author;
	}

	/**
	 * @param author
	 *            the author to set
	 */
	public void setAuthor(String author) {
		this.author = author;
	}

}
