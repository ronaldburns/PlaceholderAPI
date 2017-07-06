/*
 The MIT License (MIT)

 Copyright (c) 2017 Wundero

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */
package me.rojo8399.placeholderapi.placeholder.impl;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.spongepowered.api.Game;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.statistic.Statistics;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.world.Locatable;

import com.flowpowered.math.vector.Vector3d;

import me.rojo8399.placeholderapi.PlaceholderAPIPlugin;
import me.rojo8399.placeholderapi.PlaceholderService;
import me.rojo8399.placeholderapi.configs.JavascriptManager;
import me.rojo8399.placeholderapi.configs.Messages;
import me.rojo8399.placeholderapi.placeholder.Listening;
import me.rojo8399.placeholderapi.placeholder.Observer;
import me.rojo8399.placeholderapi.placeholder.Placeholder;
import me.rojo8399.placeholderapi.placeholder.Relational;
import me.rojo8399.placeholderapi.placeholder.Source;
import me.rojo8399.placeholderapi.placeholder.Token;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
@Listening
public class Defaults {

	public Defaults(EconomyService service, JavascriptManager manager) {
		if (service != null) {
			eco = true;
			this.service = service;
			this.def = service.getDefaultCurrency();
			service.getCurrencies().forEach(this::putCur);
		} else {
			eco = false;
		}
		this.manager = manager;
		js = true;
		try {
			manager.reloadScripts();
		} catch (Exception e) {
			js = false;
		}
		server = PlaceholderAPIPlugin.getInstance().getGame().getServer();
		s = Sponge.getServiceManager().provideUnchecked(PlaceholderService.class);
		Optional<UserStorageService> o = Sponge.getServiceManager().provide(UserStorageService.class);
		if (o.isPresent()) {
			this.storage = o.get();
			changed = true;
			sync();
		}
	}

	private boolean eco = true;

	private boolean js = true;

	private void putCur(Currency c) {
		currencies.put(c.getName(), c);
	}

	@Placeholder(id = "player")
	public Object normalPlayer(@Source Player p, @Token Optional<String> identifier) {
		if (!identifier.isPresent()) {
			return p.getName();
		}
		String token = identifier.get();
		if (token.toLowerCase().startsWith("option_")) {
			String op = token.substring("option_".length());
			return p.getOption(op).orElse("");
		}
		if (token.toLowerCase().startsWith("perm") && token.contains("_")) {
			String op = token.substring(token.indexOf("_"));
			return p.getPermissionValue(p.getActiveContexts(), op).toString();
		}
		switch (token) {
		case "prefix":
		case "suffix":
			return p.getOption(identifier.get()).orElse("");
		case "name":
			return p.getName();
		case "displayname":
			return p.getOrElse(Keys.DISPLAY_NAME, Text.of(p.getName()));
		case "uuid":
			return p.getUniqueId();
		case "can_fly":
			return p.getOrElse(Keys.CAN_FLY, false);
		case "world":
			return p.getWorld().getName();
		case "ping":
			return p.getConnection().getLatency();
		case "language":
			return p.getLocale().getDisplayName();
		case "flying":
			return p.getOrElse(Keys.IS_FLYING, false);
		case "health":
			return Math.round(p.health().get());
		case "max_health":
			return Math.round(p.maxHealth().get());
		case "food":
			return p.foodLevel().get();
		case "saturation":
			return Math.round(p.saturation().get());
		case "gamemode":
			return p.gameMode().get().getName();
		case "x":
			return p.getLocation().getPosition().toInt().getX();
		case "y":
			return p.getLocation().getPosition().toInt().getY();
		case "z":
			return p.getLocation().getPosition().toInt().getZ();
		case "direction":
			return getCardinal(p);
		case "exp_total":
			return p.getOrElse(Keys.TOTAL_EXPERIENCE, 0);
		case "exp":
			return p.getOrElse(Keys.EXPERIENCE_SINCE_LEVEL, 0);
		case "exp_to_next":
			return p.getOrElse(Keys.EXPERIENCE_FROM_START_OF_LEVEL, 0);
		case "level":
			return p.getOrElse(Keys.EXPERIENCE_LEVEL, 0);
		case "first_join":
			return p.getOrNull(Keys.FIRST_DATE_PLAYED);
		case "fly_speed":
			return p.getOrElse(Keys.FLYING_SPEED, 1.0);
		case "max_air":
			return p.getOrElse(Keys.MAX_AIR, 300);
		case "remaining_air":
			return p.getOrElse(Keys.REMAINING_AIR, 300);
		case "item_in_main_hand":
			return p.getItemInHand(HandTypes.MAIN_HAND).orElse(ItemStackSnapshot.NONE.createStack());
		case "item_in_off_hand":
			return p.getItemInHand(HandTypes.OFF_HAND).orElse(ItemStackSnapshot.NONE.createStack());
		case "walk_speed":
			return p.getOrElse(Keys.WALKING_SPEED, 1.0);
		case "time_played_seconds":
			return getTime(p, TimeUnit.SECONDS, true);
		case "time_played_minutes":
			return getTime(p, TimeUnit.MINUTES, true);
		case "time_played_ticks":
			return getTime(p, null, true);
		case "time_played_hours":
			return getTime(p, TimeUnit.HOURS, true);
		case "time_played_days":
			return getTime(p, TimeUnit.DAYS, true);
		case "time_played":
			long d = getTime(p, TimeUnit.DAYS, false);
			long h = getTime(p, TimeUnit.HOURS, false);
			long m = getTime(p, TimeUnit.MINUTES, false);
			double s = getTime(p, TimeUnit.SECONDS);
			NumberFormat f = NumberFormat.getInstance(Locale.getDefault());
			f.setMaximumFractionDigits(2);
			String out = "";
			if (d > 0) {
				out += f.format(d) + " d ";
				h -= 24 * d;
				m -= 24 * 60 * d;
				s -= 24 * 3600 * d;
			}
			if (h > 0) {
				out += f.format(h) + " h ";
				m -= 60 * h;
				s -= 3600 * h;
			}
			if (m > 0) {
				out += f.format(m) + " m ";
				s -= 60 * m;
			}
			if (s > 0) {
				out += f.format(s) + " s";
			}
			return out.trim();
		default:
			return null;
		}
	}

	private static double getTime(Player player, TimeUnit unit) {
		boolean ticks = unit == null;
		long time = Optional.ofNullable(
				player.get(Keys.STATISTICS).orElseThrow(() -> new IllegalStateException("Player must have statistics!"))
						.get(Statistics.TIME_PLAYED))
				.orElse(0L);
		if (ticks) {
			return time;
		} else {
			double time2 = ((double) time) / 20;
			if (unit == TimeUnit.SECONDS) {
				return time2;
			}
			double value = time2 / TimeUnit.SECONDS.convert(1, unit);
			return value;
		}
	}

	private static long getTime(Player player, TimeUnit unit, boolean round) {
		boolean ticks = unit == null;
		long time = Optional.ofNullable(
				player.get(Keys.STATISTICS).orElseThrow(() -> new IllegalStateException("Player must have statistics!"))
						.get(Statistics.TIME_PLAYED))
				.orElse(0L);
		if (ticks) {
			return time;
		} else {
			double time2 = ((double) time) / 20;
			if (unit == TimeUnit.SECONDS) {
				if (!round) {
					return (long) time2;
				}
				return Math.round(time2);
			}
			double value = time2 / TimeUnit.SECONDS.convert(1, unit);
			if (!round) {
				return (long) value;
			}
			return Math.round(value);
		}
	}

	private static String getCardinal(Player player) {
		Vector3d rot = player.getHeadRotation();
		double rotation = rot.abs().getY();
		if (between(rotation, 0, 22.5) || between(rotation, 337.5, 360)) {
			return Messages.get().misc.directions.south.value;
		}
		if (between(rotation, 22.5, 67.5)) {
			return Messages.get().misc.directions.southwest.value;
		}
		if (between(rotation, 67.5, 112.5)) {
			return Messages.get().misc.directions.west.value;
		}
		if (between(rotation, 112.5, 157.5)) {
			return Messages.get().misc.directions.northwest.value;
		}
		if (between(rotation, 157.5, 202.5)) {
			return Messages.get().misc.directions.north.value;
		}
		if (between(rotation, 202.5, 247.5)) {
			return Messages.get().misc.directions.northeast.value;
		}
		if (between(rotation, 247.5, 292.5)) {
			return Messages.get().misc.directions.east.value;
		}
		if (between(rotation, 292.5, 337.5)) {
			return Messages.get().misc.directions.southeast.value;
		}
		return "ERROR";
	}

	private static boolean between(double o, double min, double max) {
		return o >= min && o <= max;
	}

	@Placeholder(id = "player")
	@Relational
	public Object relPlayer(@Source Player one, @Observer CommandSource two, @Token Optional<String> token) {
		if (!token.isPresent()) {
			return null;
		}
		String t = token.get();
		switch (t.toLowerCase().trim()) {
		case "distance":
			if (!(two instanceof Locatable)) {
				return 0;
			}
			return Math.round(one.getLocation().getPosition().distance(((Locatable) two).getLocation().getPosition()));
		case "visible":
			if (!(two instanceof Entity)) {
				return false;
			}
			return one.canSee((Entity) two) && !((Entity) two).get(Keys.VANISH).orElse(false);
		case "audible":
			return one.getMessageChannel().getMembers().contains(two);
		case "distance_x":
			if (!(two instanceof Locatable)) {
				return 0;
			}
			return Math.abs(one.getLocation().getBlockX() - ((Locatable) two).getLocation().getBlockX());
		case "distance_y":
			if (!(two instanceof Locatable)) {
				return 0;
			}
			return Math.abs(one.getLocation().getBlockY() - ((Locatable) two).getLocation().getBlockY());
		case "distance_z":
			if (!(two instanceof Locatable)) {
				return 0;
			}
			return Math.abs(one.getLocation().getBlockZ() - ((Locatable) two).getLocation().getBlockZ());
		}
		return null;
	}

	private static Subject getParentGroup(Subject subject) {
		List<Subject> parents = subject.getParents();
		return parents.stream().sorted((s1, s2) -> {
			if (s1.isChildOf(s2)) {
				return 1;
			}
			if (s2.isChildOf(s1)) {
				return -1;
			}
			return 0;
		}).findFirst().orElse(parents.isEmpty() ? subject : parents.get(0));
	}

	@Placeholder(id = "rank")
	public Object rank(@Source User player, @Token Optional<String> token) {
		if (!token.isPresent()) {
			return getParentGroup(player).getIdentifier();
		}
		Subject rank = getParentGroup(player);
		String t = token.get();
		switch (t) {
		case "prefix":
		case "suffix":
			return rank.getOption(t).orElse("");
		case "name":
			return rank.getIdentifier();
		}
		if (t.contains("_") && t.indexOf("_") < t.length()) {
			if (t.startsWith("option")) {
				String opt = t.substring(t.indexOf("_") + 1);
				return rank.getOption(opt).orElse("");
			}
			// this also covers "permission_..."
			// return whether the rank has a permission
			if (t.startsWith("perm")) {
				String perm = t.substring(t.indexOf("_") + 1);
				return rank.getPermissionValue(rank.getActiveContexts(), perm).toString();
			}
		}
		return null;
	}

	private static Runtime runtime = Runtime.getRuntime();
	private static int MB = 1024 * 1024;
	private UserStorageService storage = null;
	private Set<User> users = new HashSet<>();
	private boolean changed = false;

	public int unique() {
		return users.size();
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

	@Placeholder(id = "server")
	public Object server(@Token Optional<String> identifier) {
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

	private EconomyService service;
	private Map<String, Currency> currencies = new HashMap<String, Currency>();
	private Currency def;

	@Placeholder(id = "economy")
	public Object economy(@Token Optional<String> token, @Source User player) {
		if (service == null || !eco) {
			return null;
		}
		if (!token.isPresent()) {
			Text amt = def.format(BigDecimal.valueOf(1234.56));
			Text v = Text.of(def.getName() + " (" + def.getId() + ") - ");
			return v.concat(amt);
		}
		String t = token.get();
		Currency toUse = def;
		if (t.contains("_")) {
			String[] a = t.split("_");
			t = a[0];
			String c = a[1];
			if (currencies.containsKey(c)) {
				toUse = currencies.get(def);
			}
		}
		// Don't handle nonexistent accounts here, instead throw error
		UniqueAccount acc = service.getOrCreateAccount(player.getUniqueId()).get();
		switch (t) {
		case "balance":
			return acc.getBalance(toUse).toPlainString();
		case "balformat":
			return toUse.format(acc.getBalance(toUse));
		case "display":
			return toUse.getDisplayName();
		case "pluraldisplay":
			return toUse.getPluralDisplayName();
		case "symbol":
			return toUse.getSymbol();
		}
		return null;
	}

	@Placeholder(id = "time")
	public LocalDateTime time() {
		return LocalDateTime.now();
	}

	@Placeholder(id = "sound")
	public Text sound(@Source Player p, @Token Optional<String> identifier) {
		if (!identifier.isPresent()) {
			return null;
		}
		Game game = PlaceholderAPIPlugin.getInstance().getGame();
		String[] i = identifier.get().split("-");
		Optional<SoundType> sound = game.getRegistry().getType(SoundType.class, i[0].replace("_", "."));
		Vector3d position = p.getLocation().getPosition();
		Double volume = Double.valueOf((i[1] == null) ? String.valueOf(1) : i[1]);
		Double pitch = Double.valueOf((i[2] == null) ? String.valueOf(1) : i[2]);
		if (sound.isPresent()) {
			p.playSound(sound.get(), position, volume, pitch);
			return Text.EMPTY;// Remove text from replacement
		} else {
			return null;// Leave text in replacement
		}
	}

	@Placeholder(id = "statistic")
	public Long stat(@Source Player player, @Token String token) {
		if (token == null) {
			return null;
		}
		final String t = token.trim().toLowerCase();
		return player.getOrNull(Keys.STATISTICS).entrySet().stream().filter(e -> {
			String s = e.getKey().getId().replace("._", ".").toLowerCase();
			String x = s.replace("_", "");
			String z = s.replace("_", ".");
			return s.startsWith(t) || x.startsWith(t) || z.startsWith(t);
		}).map(Map.Entry::getValue).reduce(-1l, (a, b) -> a >= 0 ? a + b : b);
	}

	private ScriptEngine engine;
	private JavascriptManager manager;
	private PlaceholderService s;
	private Server server;

	@Placeholder(id = "javascript")
	public Object js(@Source Player player, @Observer CommandSource observer, @Token Optional<String> token) {
		if (!token.isPresent()) {
			// No script
			return null;
		}
		if (engine == null) {
			// Lazily instantiate engine
			engine = new ScriptEngineManager(null).getEngineByName("Nashorn");
			// Insert default server variable - constant
			engine.put("server",
					server == null ? (server = PlaceholderAPIPlugin.getInstance().getGame().getServer()) : server);
		}
		// Insert player + parser objects, which change every time
		engine.put("player", player);
		engine.put("observer", observer);
		// Allow retrieving values for registered expansions except for
		// javascript
		// scripts - will parse like a normal string (e.g. "%player_name%")
		Service service = new Service(s, player, observer);
		engine.put("service", service);
		// Evaluate the script
		return manager.eval(engine, token.get());
	}

	public static class Service {
		private PlaceholderService s;
		private Player p;
		private CommandSource o;

		public Service(PlaceholderService s, Player p, CommandSource o) {
			this.s = s;
			this.p = p;
			this.o = o;
		}

		public String value(String placeholder) {
			return value(placeholder, s.getDefaultPattern().pattern());
		}

		public String value(String placeholder, String pattern) {
			if (placeholder.toLowerCase().contains("javascript")) {
				return placeholder;
			}
			return TextSerializers.FORMATTING_CODE.serialize(
					s.replacePlaceholders(placeholder, p, o, Pattern.compile(pattern, Pattern.CASE_INSENSITIVE)));
		}

		public Text get(String placeholders) {
			return get(placeholders, s.getDefaultPattern().pattern());
		}

		public Text get(String placeholders, String pattern) {
			if (placeholders.toLowerCase().contains("javascript")) {
				return Text.of(placeholders);
			}
			return s.replacePlaceholders(placeholders, p, s, Pattern.compile(pattern, Pattern.CASE_INSENSITIVE));
		}
	}

}
