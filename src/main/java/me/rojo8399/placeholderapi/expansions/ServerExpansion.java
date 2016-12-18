package me.rojo8399.placeholderapi.expansions;

import org.spongepowered.api.Sponge;

public class ServerExpansion extends Expansion {
	

	private static Runtime runtime = Runtime.getRuntime();
	private static int MB = 1024*1024;
	
	@Override
	public boolean canRegister() {
		return true;
	}

	@Override
	public String getIdentifier() {
		return "server";
	}

	@Override
	public String getAuthor() {
		return "rojo8399";
	}

	@Override
	public String getVersion() {
		return "1.0";
	}

	public static String onPlaceholderRequest(String identifier) {		
		switch (identifier) {
		case "online":
			return String.valueOf(Sponge.getServer().getOnlinePlayers().size());
		case "ram_used":
			return String.valueOf((runtime.totalMemory() - runtime.freeMemory()) / MB);
		case "ram_free":
			return String.valueOf(runtime.freeMemory() / MB);
		case "ram_total":
			return String.valueOf(runtime.totalMemory() / MB);
		case "ram_max":
			return String.valueOf(runtime.maxMemory() / MB);
		default:
			return null;
		}
	}	
	
}
