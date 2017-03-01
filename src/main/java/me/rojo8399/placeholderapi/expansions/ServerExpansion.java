package me.rojo8399.placeholderapi.expansions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;

import me.rojo8399.placeholderapi.PlaceholderAPIPlugin;

public class ServerExpansion implements Expansion {

	private static Runtime runtime = Runtime.getRuntime();
	private static int MB = 1024 * 1024;
	private UserStorageService storage = null;
	private Set<User> users = new HashSet<>();
	private boolean changed = false;

	@Override
	public boolean canRegister() {
		boolean out = PlaceholderAPIPlugin.getInstance().getConfig().expansions.server;
		if (out) {
			Sponge.getEventManager().registerListeners(PlaceholderAPIPlugin.getInstance(), this);
			Optional<UserStorageService> o = Sponge.getServiceManager().provide(UserStorageService.class);
			if (o.isPresent()) {
				this.storage = o.get();
				changed = true;
				sync();
			}
		}
		return out;
	}

	@Listener
	public void onJoin(ClientConnectionEvent.Join event, @Getter("getTargetEntity") Player player) {
		if (contains(player)) {
			return;
		}
		changed = users.add(player) || changed;
	}

	private boolean contains(Player p) {
		return users.stream().map(u -> u.getUniqueId()).anyMatch(p.getUniqueId()::equals);
	}

	public void sync() {
		if (!changed) {
			return;
		}
		if (storage == null) {
			return;
		}
		try {
			users = storage.getAll().stream().map(g -> storage.get(g)).filter(o -> o.isPresent()).map(o -> o.get())
					.collect(Collectors.toSet());
			changed = false;
		} catch (Exception e) {
		}
	}

	public int unique() {
		return users.size();
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

	@Override
	public Text onPlaceholderRequest(Player player, Optional<String> identifier) {
		if (!identifier.isPresent()) {
			return null;
		}
		switch (identifier.get()) {
		case "online":
			return Text.of(String.valueOf(Sponge.getServer().getOnlinePlayers().size()));
		case "max_players":
			return Text.of(String.valueOf(Sponge.getServer().getMaxPlayers()));
		case "unique_players":
			return Text.of(String.valueOf(unique()));
		case "motd":
			return Sponge.getServer().getMotd();
		case "ram_used":
			return Text.of(String.valueOf((runtime.totalMemory() - runtime.freeMemory()) / MB));
		case "ram_free":
			return Text.of(String.valueOf(runtime.freeMemory() / MB));
		case "ram_total":
			return Text.of(String.valueOf(runtime.totalMemory() / MB));
		case "ram_max":
			return Text.of(String.valueOf(runtime.maxMemory() / MB));
		case "cores":
			return Text.of(String.valueOf(runtime.availableProcessors()));
		default:
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see me.rojo8399.placeholderapi.expansions.Expansion#getSupportedTokens()
	 */
	@Override
	public List<String> getSupportedTokens() {
		return Arrays.asList("online", "max_players", "unique_players", "motd", "ram_used", "ram_free", "ram_total",
				"ram_max", "cores");
	}

}
