package me.rojo8399.placeholderapi.impl.utils;

import java.util.regex.Pattern;

public class VersionRange {

	private String actual;
	private Version leftSide, rightSide;
	private boolean leftExc, rightExc;
	private boolean retall = false;

	private static final Pattern VERSION = Pattern.compile("((?:\\d)(?:\\.\\d)*)", Pattern.CASE_INSENSITIVE);

	public VersionRange(String from) {
		this.actual = from;
		if (VERSION.matcher(from).matches()) {
			retall = true;
		} else {
			leftExc = from.startsWith("(");
			rightExc = from.endsWith(")");
			if (rightExc) {
				from = from.replace(")", "");
			} else {
				from = from.replace("]", "");
			}
			if (leftExc) {
				from = from.replace("(", "");
			} else {
				from = from.replace("[", "");
			}
			if (from.contains(",")) {
				String[] parts = from.split(",");
				if (from.startsWith(",")) {
					rightSide = new Version(from.replace(",", ""));
				} else {
					leftSide = new Version(parts[0]);
					if (parts.length > 1 && !parts[1].isEmpty()) {
						rightSide = new Version(parts[1]);
					}
				}
			} else {
				leftSide = rightSide = new Version(from);
			}
		}
	}

	@Override
	public String toString() {
		return actual;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((actual == null) ? 0 : actual.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof VersionRange))
			return false;
		VersionRange other = (VersionRange) obj;
		if (actual == null) {
			return other.actual == null;
		} else return actual.equals(other.actual);
	}

	public boolean isInRange(Version ver) {
		if (retall) {
			return true;
		}
		int leftcomp = leftSide == null ? -1 : leftSide.compareTo(ver);
		int rightcomp = rightSide == null ? 1 : rightSide.compareTo(ver);
		int leftbound = leftExc ? -1 : 0;
		int rightbound = rightExc ? 1 : 0;
		if (leftcomp <= leftbound || leftSide == null) {
			return rightcomp >= rightbound || rightSide == null;
		}
		return false;
	}

}
