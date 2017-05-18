package me.rojo8399.placeholderapi.expansions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;

import com.google.common.reflect.TypeToken;

import me.rojo8399.placeholderapi.configs.Messages;
import ninja.leaping.configurate.objectmapping.Setting;

public class ServerExpansion implements ConfigurableExpansion, ListeningExpansion {

	private static TypeToken<ServerExpansion> type = TypeToken.of(ServerExpansion.class);

	private static Runtime runtime = Runtime.getRuntime();
	private static int MB = 1024 * 1024;
	private UserStorageService storage = null;
	private Set<User> users = new HashSet<>();
	private boolean changed = false;
	@Setting
	private boolean enabled = true;

	@Override
	public boolean canRegister() {
		boolean out = enabled;
		if (out) {
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
	public String getDescription() {
		return Messages.get().placeholder.serverdesc.value;
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
		return "1.1";
	}

	@Override
	public Object onValueRequest(Player player, Optional<String> identifier) {
		if (!identifier.isPresent()) {
			return null;
		}
		switch (identifier.get()) {
		case "online":
			return Sponge.getServer().getOnlinePlayers().stream()
					.filter(p -> !p.getOrElse(Keys.VANISH_PREVENTS_TARGETING, false)).count();
		case "max_players":
			return Sponge.getServer().getMaxPlayers();
		case "unique_players":
			return unique();
		case "motd":
			return Sponge.getServer().getMotd();
		case "ram_used":
			return ((runtime.totalMemory() - runtime.freeMemory()) / MB);
		case "ram_free":
			return (runtime.freeMemory() / MB);
		case "ram_total":
			return (runtime.totalMemory() / MB);
		case "ram_max":
			return (runtime.maxMemory() / MB);
		case "cores":
			return runtime.availableProcessors();
		case "tps":
			return Sponge.getServer().getTicksPerSecond();
		default:
			return null;
		}
	}

	@Override
	public Text onPlaceholderRequest(Player player, Optional<String> identifier) {
		return onValueRequest(player, identifier, Text.class).orElse(null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see me.rojo8399.placeholderapi.expansions.Expansion#getSupportedTokens()
	 */
	@Override
	public List<String> getSupportedTokens() {
		return Arrays.asList("online", "max_players", "unique_players", "motd", "ram_used", "ram_free", "ram_total",
				"ram_max", "cores", "tps");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see me.rojo8399.placeholderapi.expansions.ListeningExpansion#getPlugin()
	 */
	@Override
	public Optional<Object> getPlugin() {
		return Optional.empty();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * me.rojo8399.placeholderapi.expansions.ConfigurableExpansion#getToken()
	 */
	@Override
	public TypeToken<? extends ConfigurableExpansion> getToken() {
		return type;
	}

}
