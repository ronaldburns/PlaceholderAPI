package me.rojo8399.placeholderapi;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import me.rojo8399.placeholderapi.expansions.ConfigurableExpansion;
import me.rojo8399.placeholderapi.expansions.Expansion;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

public class Registry {

	private Map<String, Expansion> registry = new ConcurrentHashMap<>();

	Registry() {
	}

	public boolean has(String id) {
		return registry.containsKey(id.toLowerCase());
	}

	public Set<Expansion> getAll() {
		return registry.values().stream().collect(Collectors.toSet());
	}

	public boolean register(Expansion e) {
		if (e == null) {
			return false;
		}
		if (e.getIdentifier() == null || e.getIdentifier().isEmpty()) {
			return false;
		}
		boolean ru = false;
		if (e.getIdentifier().contains("_")) {
			ru = true;
		}
		if (registry.containsKey(e.getIdentifier().toLowerCase())) {
			return false;
		}
		if (e instanceof ConfigurableExpansion) {
			ConfigurableExpansion ce = (ConfigurableExpansion) e;
			ConfigurationNode node = PlaceholderAPIPlugin.getInstance().getRootConfig().getNode("expansions",
					e.getIdentifier());
			if (node.isVirtual()) {
				try {
					ObjectMapper.forObject(ce).serialize(node);
					PlaceholderAPIPlugin.getInstance().saveConfig();
				} catch (Exception e2) {
				}
			}
			try {
				e = ce = ObjectMapper.forObject(ce).populate(node);
			} catch (ObjectMappingException e1) {
				try {
					ObjectMapper.forObject(ce).serialize(node);
					PlaceholderAPIPlugin.getInstance().saveConfig();
				} catch (Exception e2) {
				}
				return false;
			}
		}
		if (!e.canRegister()) {
			return false;
		}
		registry.put((ru ? e.getIdentifier().replace("_", "") : e.getIdentifier()).toLowerCase(), e);
		return true;
	}

	public Expansion get(String id) {
		if (!has(id)) {
			return null;
		}
		return registry.get(id);
	}

}
