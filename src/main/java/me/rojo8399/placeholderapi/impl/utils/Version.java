package me.rojo8399.placeholderapi.impl.utils;

import java.util.ArrayList;
import java.util.List;

public class Version implements Comparable<Version> {

	private String actual;
	// ordered DO NOT SORT
	private List<SubVersion> subs;

	public Version(String from) {
		from = from.toLowerCase();
		if (from.contains("v")) {
			from = from.replace("v", "");
		}
		String[] parts = from.split("\\.");
		actual = from;
		subs = new ArrayList<>();
		for (String s : parts) {
			subs.add(new SubVersion(s));
		}
	}

	@Override
	public String toString() {
		return actual;
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof Version && ((Version) other).actual.equals(this.actual);
	}

	private static class SubVersion implements Comparable<SubVersion> {
		private int intVal;

		public SubVersion(String val) {
			try {
				this.intVal = Integer.parseInt(val);
			} catch (Exception e) {
				String top = "";
				for (Character c : val.toCharArray()) {
					if (c.charValue() < 48 || c.charValue() > 57) {
						break;
					}
					top += c.charValue();
				}
				this.intVal = Integer.parseInt(top);
			}
		}

		@Override
		public int compareTo(SubVersion o) {
			return Integer.compare(intVal, o.intVal);
		}

		@Override
		public String toString() {
			return "" + intVal;
		}

		@Override
		public boolean equals(Object other) {
			return other instanceof SubVersion && ((SubVersion) other).intVal == this.intVal;
		}
	}

	@Override
	public int compareTo(Version o) {
		List<SubVersion> svx = o.subs;
		for (int i = 0; i < subs.size(); i++) {
			SubVersion sv = subs.get(i);
			if (svx.size() > i) {
				int dif = sv.intVal - svx.get(i).intVal;
				if (dif != 0) {
					return dif;
				}
			} else {
				return sv.intVal;
			}
		}
		if (svx.size() > subs.size()) {
			for (int i = subs.size(); i < svx.size(); i++) {
				if (svx.get(i).intVal != 0) {
					return -svx.get(i).intVal;
				}
			}
			return 0;
		}
		return 0;
	}

}
