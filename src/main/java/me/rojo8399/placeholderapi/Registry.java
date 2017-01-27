package me.rojo8399.placeholderapi;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import me.rojo8399.placeholderapi.expansions.Expansion;

public class Registry {

	private Map<String, RegistryEntry> registry = new ConcurrentHashMap<>();

	Registry() {
	}

	public boolean has(String id) {
		return registry.containsKey(id.toLowerCase());
	}

	public Set<Expansion> getAll() {
		return registry.values().stream().map(r -> r.getExpansion()).collect(Collectors.toSet());
	}

	public boolean register(Expansion e) {
		if (e == null || !e.canRegister()) {
			return false;
		}
		if (e.getIdentifier() == null || e.getIdentifier().isEmpty()) {
			return false;
		}
		if (registry.containsKey(e.getIdentifier().toLowerCase())) {
			return false;
		}
		registry.put(e.getIdentifier(), new RegistryEntry(e));
		return true;
	}

	public Expansion get(String id) {
		if (!has(id)) {
			return null;
		}
		return getEntry(id).getExpansion();
	}

	public RegistryEntry getEntry(String id) {
		return registry.get(id.toLowerCase());
	}

}
