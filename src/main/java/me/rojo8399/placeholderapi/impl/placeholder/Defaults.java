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
package me.rojo8399.placeholderapi.impl.placeholder;

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
import org.spongepowered.api.world.storage.WorldProperties;

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
import me.rojo8399.placeholderapi.impl.utils.TypeUtils;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

/**
 * The default placeholders provided by the plugin. These also server as
 * examples of many, though not all, possibilities for the plugin to use as
 * placeholders.
 * 
 * The class is a listener class AND a configurable class, allowing the
 * placeholders to be attached directly to PlaceholderAPI.
 * 
 * @author Wundero
 *
 */
@Listening
@ConfigSerializable
public class Defaults {

	/**
	 * This class simply represents the service PlaceholderAPI provides, except
	 * simplified in order to be used in JavaScript placeholders.
	 */
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

	/**
	 * Dynamic object used for configuring the "server" placeholder. Notice that the
	 * fields inside this class do NOT need to be attached to any one placeholder;
	 * the fields in this class do not know what placeholder is going to use them so
	 * they do not need to be attached.
	 */
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

	/**
	 * Pattern to match against a sound placeholder containing _all or all_.
	 */
	private static final Pattern ALLSOUND_PATTERN = Pattern.compile("([_]?all[_]?)", Pattern.CASE_INSENSITIVE);

	/**
	 * Runtime utility to convert to MB from bytes.
	 */
	private static int MB = 1024 * 1024;

	/**
	 * Current runtime; used by server placeholder.
	 */
	private static Runtime runtime = Runtime.getRuntime();

	/*
	 * Begin utilities methods: these methods are used throughout the class 
	 * but do not play major roles in explaining the plugin.
	 */

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

	/**
	 * This field is what the object will use as a configurable field; fields
	 * DIRECTLY inside of the object registered MUST be attached to a placeholder
	 * using the Attach annotation. If they are not attached, they will be ignored
	 * completely.
	 */
	@Setting
	@Attach("server") // Use the id of the placeholder you would like to attach to. The actual
						// placeholder you attach to is arbitrary, however it must exist within this
						// class.
	private List<Uptime> uptimes = new ArrayList<>();

	private Set<User> users = new HashSet<>();

	// Since the object is instantiated before being passed to PlaceholderAPI, you
	// can do whatever for this.
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
				.sorted((a, b) -> -a.getBalance(cur).compareTo(b.getBalance(cur))).collect(Collectors.toList());
		balTop.put(cur, baltop);
		lastUpdate.put(cur, System.currentTimeMillis());
	}

	private boolean contains(Player p) {
		return users.stream().map(u -> u.getUniqueId()).anyMatch(p.getUniqueId()::equals);
	}

	/**
	 * This is the first placeholder in the object. This handles how the placeholder
	 * "economy" parses. I return an object to allow me to return any value without
	 * having to make a nice supertype.
	 * 
	 * The parameters in this method are annotated with Nullable. If they have this
	 * annotation, they can have null values and you should handle accordingly; if
	 * they do not have this annotation, you can safely assume those parameters will
	 * NEVER be null.
	 * 
	 * @param token
	 *            This parameter is the part in the placeholder after the first _.
	 *            For example, {economy_balance} gives a token of "balance". The
	 *            'fix = true' parameter tells PlaceholderAPI to make the token
	 *            lower case and remove excess characters.
	 * 
	 *            For the token, you can request types other than String if you
	 *            want. If you know your token will have multiple _ separated
	 *            sections, you can request a String[]. If you want a number, you
	 *            can request a Double or an Integer. PlaceholderAPI will handle the
	 *            conversion for you. PlaceholderAPI will also try it's best to cast
	 *            to a given type but, if the type does not have nice
	 *            deserialization options, may not ever parse the placeholder. If
	 *            you want the placeholder to parse to any object type, you must
	 *            register a type serializer into PlaceholderService.
	 * @param player
	 *            This parameter is the Source parameter, or who the placeholder is
	 *            replacing for. In this case, it would be who's balance to draw
	 *            from.
	 * @return whatever the value of the placeholder is.
	 * @throws NoValueException
	 *             - Throw this exception if you do not want the placeholder
	 *             replaced. For example, "{economy_a}" should throw a
	 *             NoValueException so that PlaceholderAPI helps the user fix issues
	 *             with their placeholders. If you return null, PlaceholderAPI will
	 *             fill with an empty string.
	 */
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
			} else {
				t += "_" + c;
			}
		}
		final Currency toUseFinal = toUse;
		// fix t for baltop
		int baltop = 0;
		if (t.startsWith("baltop")) {
			if (t.contains("_")) {
				String aft = t.substring(t.indexOf("_") + 1);
				t = t.substring(0, t.indexOf("_"));
				try {
					baltop = Integer.parseInt(aft);
				} catch (Exception e) {
					if (aft.contains("_")) {
						String cur = aft.split("_")[1];
						throw new NoValueException("That is not a valid currency!", currencies.keySet().stream()
								.filter(s -> TypeUtils.closeTo(cur, s)).collect(Collectors.toList()));
					} else {
						throw new NoValueException("That is not a valid currency!", currencies.keySet().stream()
								.filter(s -> TypeUtils.closeTo(aft, s)).collect(Collectors.toList()));
					}
				}
			} else {
				baltop = 5;
			}
			if (baltop <= 0) {
				baltop = 1;
			}
			calculateBalTop(toUse);
			List<UniqueAccount> baltop2 = balTop.get(toUse).stream().limit(baltop).collect(Collectors.toList());
			if (t.startsWith("baltopf")) {
				return Text.joinWith(Text.of(", "),
						baltop2.stream().map(
								a -> Text.of(a.getDisplayName(), ": " + toUseFinal.format(a.getBalance(toUseFinal))))
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
		if (player != null) {
			// Don't handle nonexistent accounts here, instead throw error
			UniqueAccount acc = service.getOrCreateAccount(player.getUniqueId()).get();
			switch (t) {
			case "balance":
				return acc.getBalance(toUse).toPlainString();
			case "bal_format":
				return toUse.format(acc.getBalance(toUse));
			}
		}
		if (currencies.containsKey(t)) {
			toUse = currencies.get(t);
			Text amt = toUse.format(BigDecimal.valueOf(1234.56));
			Text v = Text.of(toUse.getName() + " (" + toUse.getId() + ") - ");
			return v.concat(amt);
		}
		if (t.contains("_")) {
			String[] arr = t.split("_");
			t = arr[arr.length - 1];
		}
		final String ft = t;
		throw new NoValueException("That is not a valid currency!",
				currencies.keySet().stream().filter(s -> TypeUtils.closeTo(ft, s)).collect(Collectors.toList()));
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

	/**
	 * This is another example of a placeholder. This one is simple in comparison to
	 * the others.
	 * 
	 * The Relational annotation forces this to be used with the rel_ prefix in the
	 * placeholder, like this: {rel_rank_greater_than}. This provides no guarantees
	 * that it will actually need or use both source and observer. Again, all
	 * parameters here are null-safe (will not call the method if those parameters
	 * are null).
	 * 
	 * @param token
	 *            This is the token, like in all other placeholders.
	 * @param underrank
	 *            This is the player comparing. For instance, if you say source >
	 *            observer, source will be a child of observer and thus have more
	 *            permissions.
	 * @param overrank
	 *            This is the player being compared to.
	 * @return Whether the expression, greater_than or less_than, returns true.
	 * @throws NoValueException
	 */
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

	/**
	 * Listener is registered because of the @Listening annotation. No need to
	 * attach it to your plugin, PlaceholderAPI will do this for you.
	 */
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
			// ItemStack return types are parsed nicely
			return p.getItemInHand(HandTypes.MAIN_HAND).orElse(ItemStackSnapshot.NONE.createStack());
		case "item_in_off_hand":
			return p.getItemInHand(HandTypes.OFF_HAND).orElse(ItemStackSnapshot.NONE.createStack());
		case "walk_speed":
			return p.getOrElse(Keys.WALKING_SPEED, 1.0);
		case "time_played_seconds":
			return getTime(p, TimeUnit.SECONDS, true); // Instant and Duration return types are serialized
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
		boolean gt = false;
		if (identifier.startsWith("time")) {
			gt = true;
			identifier = identifier.substring("time".length());
		}
		if (identifier.startsWith("game_time")) {
			gt = true;
			identifier = identifier.substring("game_time".length());
		}
		if (gt) {
			Optional<WorldProperties> w = Sponge.getServer().getDefaultWorld();
			if (!identifier.isEmpty()) {
				String world = identifier.substring(1);
				if (world.isEmpty()) {
					if (w.isPresent()) {
						return w.get().getWorldTime() % 24000;
					} else {
						String id1 = identifier;
						throw new NoValueException(Messages.get().misc.invalid.t("world"),
								Sponge.getServer().getAllWorldProperties().stream().map(wp -> wp.getWorldName())
										.filter(n -> TypeUtils.closeTo(id1.replaceFirst("_", ""), n))
										.map(s -> "time_" + s).collect(Collectors.toList()));
					}
				}
				WorldProperties wp = Sponge.getServer().getWorld(world).map(wo -> wo.getProperties()).orElse(null);
				if (wp != null) {
					return wp.getWorldTime() % 24000;
				}
			} else {
				if (w.isPresent()) {
					return w.get().getWorldTime() % 24000;
				}
			}
			String id1 = identifier;
			throw new NoValueException(Messages.get().misc.invalid.t("world"),
					Sponge.getServer().getAllWorldProperties().stream().map(wp -> wp.getWorldName())
							.filter(n -> n.equalsIgnoreCase(w.get().getWorldName())
									|| TypeUtils.closeTo(id1.replaceFirst("_", ""), n))
							.map(s -> "time_" + s).collect(Collectors.toList()));
		}
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
		case "uptime_percent": // Uptime config item used here.
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
	public Text sound(@Nullable @Source Player p, @Token(fix = true) String identifier) throws NoValueException {
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
		} else {
			throw new NoValueException(Messages.get().misc.invalid.t("sound"));
		}
		return null;
	}

	@Placeholder(id = "statistic")
	// @Requires(spongeVersion = "[6.0)")
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

	/**
	 * This method does not need parameters, but can still be called. LocalDateTime
	 * is also nicely serialized.
	 */
	@Placeholder(id = "time")
	public LocalDateTime time() {
		return LocalDateTime.now();
	}

	public int unique() {
		return users.size();
	}

}
