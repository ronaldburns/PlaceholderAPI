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
package me.rojo8399.placeholderapi.impl.placeholder.impl;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

import javax.annotation.Nullable;
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
import org.spongepowered.api.event.game.state.GameStoppingEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.event.service.ChangeServiceProviderEvent;
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

import me.rojo8399.placeholderapi.Attach;
import me.rojo8399.placeholderapi.Listening;
import me.rojo8399.placeholderapi.NoValueException;
import me.rojo8399.placeholderapi.Observer;
import me.rojo8399.placeholderapi.Placeholder;
import me.rojo8399.placeholderapi.PlaceholderService;
import me.rojo8399.placeholderapi.Relational;
import me.rojo8399.placeholderapi.Source;
import me.rojo8399.placeholderapi.Token;
import me.rojo8399.placeholderapi.impl.PlaceholderAPIPlugin;
import me.rojo8399.placeholderapi.impl.configs.JavascriptManager;
import me.rojo8399.placeholderapi.impl.configs.Messages;
import me.rojo8399.placeholderapi.impl.placeholder.Expansion;
import me.rojo8399.placeholderapi.impl.placeholder.Store;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@Listening
@ConfigSerializable
public class Defaults {

	public static class Service {
		private CommandSource o;
		private Player p;
		private PlaceholderService s;

		public Service(PlaceholderService s, Player p, CommandSource o) {
			this.s = s;
			this.p = p;
			this.o = o;
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
	}

	@ConfigSerializable
	private static class Uptime {

		@Setting("end-time")
		public long finishTimeMillis;
		@Setting("start-time")
		public long startTimeMillis;

		public void finish() {
			finishTimeMillis = System.currentTimeMillis();
		}

		public void start() {
			startTimeMillis = System.currentTimeMillis();
		}

		@Override
		public String toString() {
			return startTimeMillis + " to " + finishTimeMillis;
		}
	}

	private static final Pattern ALLSOUND_PATTERN = Pattern.compile("([_]?all[_]?)", Pattern.CASE_INSENSITIVE);

	private static int MB = 1024 * 1024;

	private static Runtime runtime = Runtime.getRuntime();

	private static boolean between(double o, double min, double max) {
		return o >= min && o <= max;
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

	/* ordered */
	private Map<Currency, List<UniqueAccount>> balTop = new HashMap<>();

	private boolean changed = false;

	private Map<String, Currency> currencies = new HashMap<String, Currency>();

	private Uptime current;

	private Currency def;

	private boolean eco = true;

	private ScriptEngine engine;

	private Map<Currency, Long> lastUpdate = new HashMap<>();

	private JavascriptManager manager;
	private PlaceholderService s;
	private Server server;
	private EconomyService service;
	private UserStorageService storage = null;

	@Setting
	@Attach("server")
	private List<Uptime> uptimes = new ArrayList<>();

	private Set<User> users = new HashSet<>();

	public Defaults(EconomyService service, JavascriptManager manager, PlaceholderService s) {
		if (service != null) {
			eco = true;
			this.service = service;
			this.def = service.getDefaultCurrency();
			service.getCurrencies().forEach(this::putCur);
		} else {
			eco = false;
		}
		this.manager = manager;
		try {
			manager.reloadScripts();
		} catch (Exception e) {
		}
		server = PlaceholderAPIPlugin.getInstance().getGame().getServer();
		this.s = s;
		Optional<UserStorageService> o = Sponge.getServiceManager().provide(UserStorageService.class);
		if (o.isPresent()) {
			this.storage = o.get();
			changed = true;
			sync();
		}
		current = new Uptime();
		current.start();
	}

	private void calculateBalTop(Currency cur) {
		if (lastUpdate.containsKey(cur) && lastUpdate.get(cur) != null) {
			long last = lastUpdate.get(cur);
			if (Duration.ofMillis(System.currentTimeMillis() - last).toMinutes() <= 3) {
				return;
			}
		}
		List<UniqueAccount> baltop = new ArrayList<>();
		baltop = this.users.stream().map(u -> u.getUniqueId()).filter(service::hasAccount)
				.map(service::getOrCreateAccount).filter(Optional::isPresent).map(Optional::get)
				.sorted((a, b) -> a.getBalance(cur).compareTo(b.getBalance(cur))).collect(Collectors.toList());
		balTop.put(cur, baltop);
		lastUpdate.put(cur, System.currentTimeMillis());
	}

	private boolean contains(Player p) {
		return users.stream().map(u -> u.getUniqueId()).anyMatch(p.getUniqueId()::equals);
	}

	@Placeholder(id = "economy")
	public Object economy(@Token(fix = true) @Nullable String token, @Nullable @Source User player)
			throws NoValueException {
		if (service == null || !eco) {
			throw new NoValueException();
		}
		if (token == null) {
			Text amt = def.format(BigDecimal.valueOf(1234.56));
			Text v = Text.of(def.getName() + " (" + def.getId() + ") - ");
			return v.concat(amt);
		}
		String t = token;
		Currency toUse = def;
		if (t.contains("_")) {
			String[] a = t.split("_");
			t = a[0];
			for (int i = 1; i < a.length - 1; i++) {
				t += "_" + a[i];
			}
			String c = a[a.length - 1];
			if (currencies.containsKey(c)) {
				toUse = currencies.get(c);
			}
		}
		final Currency toUseFinal = toUse;
		if (player == null) {
			// fix t for baltop
			int baltop = 0;
			if (t.startsWith("baltop")) {
				if (t.contains("_")) {
					String aft = t.substring(t.indexOf("_") + 1);
					t = t.substring(0, t.indexOf("_"));
					try {
						baltop = Integer.parseInt(aft);
					} catch (Exception e) {
						baltop = 5;
					}
				} else {
					baltop = 5;
				}
				if (baltop <= 0) {
					baltop = 1;
				}
				calculateBalTop(toUse);
				List<UniqueAccount> baltop2 = balTop.get(toUse).subList(0, baltop);
				if (t.startsWith("baltopf")) {
					return Text.joinWith(Text.of(", "), baltop2.stream()
							.map(a -> Text.of(a.getDisplayName(), ": " + toUseFinal.format(a.getBalance(toUseFinal))))
							.collect(Collectors.toList()));
				}
				return Text.joinWith(Text.of(", "),
						baltop2.stream()
								.map(a -> Text.of(a.getDisplayName(), ": " + a.getBalance(toUseFinal).toPlainString()))
								.collect(Collectors.toList()));
			}
			switch (t) {
			case "display":
				return toUse.getDisplayName();
			case "plural_display":
				return toUse.getPluralDisplayName();
			case "symbol":
				return toUse.getSymbol();
			}
			if (currencies.containsKey(t)) {
				toUse = currencies.get(t);
				Text amt = toUse.format(BigDecimal.valueOf(1234.56));
				Text v = Text.of(toUse.getName() + " (" + toUse.getId() + ") - ");
				return v.concat(amt);
			}
			throw new NoValueException();
		}
		// Don't handle nonexistent accounts here, instead throw error
		UniqueAccount acc = service.getOrCreateAccount(player.getUniqueId()).get();
		switch (t) {
		case "balance":
			return acc.getBalance(toUse).toPlainString();
		case "bal_format":
			return toUse.format(acc.getBalance(toUse));
		case "display":
			return toUse.getDisplayName();
		case "plural_display":
			return toUse.getPluralDisplayName();
		case "symbol":
			return toUse.getSymbol();
		}
		if (currencies.containsKey(t)) {
			toUse = currencies.get(t);
			Text amt = toUse.format(BigDecimal.valueOf(1234.56));
			Text v = Text.of(toUse.getName() + " (" + toUse.getId() + ") - ");
			return v.concat(amt);
		}
		throw new NoValueException();
	}

	private long getDowntimeMillis() {
		long lastFinTime = 0;
		long out = 0;
		for (Uptime u : uptimes) {
			if (lastFinTime == 0) {
				lastFinTime = u.finishTimeMillis;
				continue;
			}
			out += u.startTimeMillis - lastFinTime;
			lastFinTime = u.finishTimeMillis;
		}
		if (out == 0) {
			out = System.currentTimeMillis() - lastFinTime;
		}
		return out;
	}

	private long getUptimeMillis() {
		return uptimes.stream().map(u -> u.finishTimeMillis - u.startTimeMillis)
				.reduce((long) Sponge.getServer().getRunningTimeTicks() * 50, (a, b) -> a + b);
	}

	@Placeholder(id = "rank")
	@Relational
	public Boolean isAbove(@Token String token, @Source User underrank, @Observer User overrank)
			throws NoValueException {
		if (!(token.equalsIgnoreCase("greater_than") || token.equalsIgnoreCase("less_than"))) {
			throw new NoValueException();
		}
		if (token.equalsIgnoreCase("greater_than")) {
			if (underrank.isChildOf(overrank)) {
				return true;
			}
			return getParentGroup(underrank).isChildOf(getParentGroup(overrank));
		} else {
			if (overrank.isChildOf(underrank)) {
				return true;
			}
			return getParentGroup(overrank).isChildOf(getParentGroup(underrank));
		}
	}

	@Placeholder(id = "javascript")
	public Object js(@Nullable @Source Player player, @Nullable @Observer CommandSource observer, @Token String token) {
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
		return manager.eval(engine, token);
	}

	@Listener
	public void newEco(ChangeServiceProviderEvent event) {
		if (event.getService().equals(EconomyService.class)) {
			EconomyService s = (EconomyService) event.getNewProvider();
			if (s != null) {
				eco = true;
				this.service = s;
				this.def = service.getDefaultCurrency();
				this.currencies.clear();
				service.getCurrencies().forEach(this::putCur);
			} else {
				eco = false;
			}
		}
	}

	@Placeholder(id = "player")
	public Object normalPlayer(@Source Player p, @Token(fix = true) @Nullable String token) throws NoValueException {
		if (token == null) {
			return p.getName();
		}
		if (token.startsWith("option_")) {
			String op = token.substring("option_".length());
			return p.getOption(op).orElse("");
		}
		if (token.startsWith("perm") && token.contains("_")) {
			String op = token.substring(token.indexOf("_"));
			return p.getPermissionValue(p.getActiveContexts(), op).toString();
		}
		switch (token) {
		case "prefix":
		case "suffix":
			return p.getOption(token).orElse("");
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
			throw new NoValueException();
		}
	}

	@Listener
	public void onJoin(ClientConnectionEvent.Join event, @Getter("getTargetEntity") Player player) {
		uptimes = uptimes.stream()
				.filter(u -> Duration.ofMillis(System.currentTimeMillis() - u.finishTimeMillis).toDays() <= 30)
				.collect(Collectors.toList());
		if (contains(player)) {
			return;
		}
		changed = users.add(player) || changed;
	}

	@Listener
	public void onStopping(GameStoppingEvent event) {
		current.finish();
		uptimes.add(current);
		Store.get().get("server", false).ifPresent(Expansion::saveConfig);
	}

	/*
	 * @Placeholder(id = "playerlist") public List<Player> list(@Nullable @Token(fix
	 * = true) String token) { if (token == null) { return
	 * Sponge.getServer().getOnlinePlayers().stream() .filter(p ->
	 * !p.getOrElse(Keys.VANISH_PREVENTS_TARGETING,
	 * false)).collect(Collectors.toList()); } Stream<Player> out =
	 * Sponge.getServer().getOnlinePlayers().stream() .filter(p ->
	 * !p.getOrElse(Keys.VANISH_PREVENTS_TARGETING, false)); if
	 * (PERM.matcher(token).find()) { Matcher m = PERM.matcher(token); while
	 * (m.find()) { String permission = m.group(1); out = out.filter(p ->
	 * p.hasPermission(permission)); } } if (WORLD.matcher(token).find()) { Matcher
	 * m = WORLD.matcher(token); while (m.find()) { String world = m.group(1); out =
	 * out.filter(p -> p.getWorld().getName().toLowerCase().startsWith(world)); } }
	 * // TODO: /* better token matching data key boolean filter -> load key from t
	 * - IS_FLYING, for example data key numeric filter -> load key again, but also
	 * load comparator (>, >=, <, <=, =) and number - Health > 10, for example -
	 * sort greatest value for key??? better idea??: placeholder value filter ->
	 * load placeholder from key, load value from key, load comparator sanity checks
	 * (no > for boolean, no value present = true/0/max int, depending, no comp
	 * present: =) sort by highest comparison limiter, top X players if available ->
	 * sort by highest comp then alphabetically
	 *//*
		 * return out.collect(Collectors.toList()); }
		 */

	private void putCur(Currency c) {
		currencies.put(c.getName().toLowerCase().replace(" ", ""), c);
	}

	@Placeholder(id = "rank")
	public Object rank(@Source User player, @Token(fix = true) @Nullable String token) throws NoValueException {
		if (token == null) {
			return getParentGroup(player).getIdentifier();
		}
		Subject rank = getParentGroup(player);
		String t = token;
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
		throw new NoValueException();
	}

	@Placeholder(id = "player")
	@Relational
	public Object relPlayer(@Source Player one, @Observer CommandSource two, @Token(fix = true) @Nullable String token)
			throws NoValueException {
		if (!(two instanceof Player)) {
			if (token == null) {
				return two.getName();
			}
			String t = token;
			if (t.startsWith("option_")) {
				String op = t.substring("option_".length());
				return two.getOption(op).orElse("");
			}
			if (t.startsWith("perm") && t.contains("_")) {
				String op = t.substring(t.indexOf("_"));
				return two.getPermissionValue(two.getActiveContexts(), op).toString();
			}
			if (t.equalsIgnoreCase("prefix") || t.equalsIgnoreCase("suffix")) {
				return two.getOption(t).orElse("");
			}
		}
		if (token == null) {
			throw new NoValueException();
		}
		String t = token;
		switch (t) {
		case "distance":
			if (!(two instanceof Locatable)) {
				return 0;
			}
			return Math.round(one.getLocation().getPosition().distance(((Locatable) two).getLocation().getPosition()));
		case "visible":
			if (!(two instanceof Entity)) {
				return false;
			}
			return ((Entity) two).canSee(one) && !((Entity) one).get(Keys.VANISH).orElse(false);
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
		throw new NoValueException();
	}

	@Placeholder(id = "server")
	public Object server(@Token(fix = true) String identifier) throws NoValueException {
		switch (identifier) {
		case "online":
			return Sponge.getServer().getOnlinePlayers().stream()
					.filter(p -> !p.getOrElse(Keys.VANISH_PREVENTS_TARGETING, false)).count();
		case "max_players":
			return Sponge.getServer().getMaxPlayers();
		case "unique_players":
			return unique();
		case "motd":
			return Sponge.getServer().getMotd();
		case "uptime":
		case "uptime_percent":
			long um = this.getUptimeMillis();
			long dm = this.getDowntimeMillis();
			NumberFormat fmt = NumberFormat.getPercentInstance();
			fmt.setMaximumFractionDigits(2);
			fmt.setMinimumFractionDigits(2);
			return fmt.format((um / ((double) dm + (double) um)));
		case "uptime_total":
			Duration dur = Duration.ofMillis(this.getUptimeMillis());
			return dur;
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
			throw new NoValueException();
		}
	}

	@Placeholder(id = "sound")
	public Text sound(@Nullable @Source Player p, @Token(fix = true) String identifier) {
		boolean all = identifier.contains("all");
		if (all) {
			identifier = ALLSOUND_PATTERN.matcher(identifier).replaceAll("");
		}
		Game game = PlaceholderAPIPlugin.getInstance().getGame();
		String[] i = identifier.split("-");
		Optional<SoundType> sound = game.getRegistry().getType(SoundType.class, i[0].replace("_", "."));
		Double volume = Double.valueOf((i[1] == null) ? String.valueOf(1) : i[1]);
		Double pitch = Double.valueOf((i[2] == null) ? String.valueOf(1) : i[2]);
		if (sound.isPresent()) {
			if (p != null && !all) {
				Vector3d position = p.getLocation().getPosition();
				p.playSound(sound.get(), position, volume, pitch);
			} else {
				Vector3d position;
				for (Player pl : Sponge.getServer().getOnlinePlayers()) {
					position = pl.getLocation().getPosition();
					pl.playSound(sound.get(), position, volume, pitch);
				}
			}
		}
		return null;
	}

	@Placeholder(id = "statistic")
	public Long stat(@Source Player player, @Token(fix = true) String t) {
		return player.getOrNull(Keys.STATISTICS).entrySet().stream().filter(e -> {
			String s = e.getKey().getId().replace("._", ".").toLowerCase();
			String x = s.replace("_", "");
			String z = s.replace("_", ".");
			return s.startsWith(t) || x.startsWith(t) || z.startsWith(t);
		}).map(Map.Entry::getValue).reduce(-1l, (a, b) -> a >= 0 ? a + b : b);
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

	@Placeholder(id = "time")
	public LocalDateTime time() {
		return LocalDateTime.now();
	}
	/*
	 * private static final Pattern PERM = Pattern.compile(
	 * "perm(?:ission)?\\_([A-Za-z0-9*\\-]+(?:\\.[A-Za-z0-9*\\-]+)+)",
	 * Pattern.CASE_INSENSITIVE), WORLD =
	 * Pattern.compile("world\\_([A-Za-z0-9\\_\\-]+)", Pattern.CASE_INSENSITIVE);
	 */

	public int unique() {
		return users.size();
	}

}
