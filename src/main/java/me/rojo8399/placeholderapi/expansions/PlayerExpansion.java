package me.rojo8399.placeholderapi.expansions;

import java.text.NumberFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.statistic.Statistics;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.serializer.TextSerializers;

import com.flowpowered.math.vector.Vector3d;

import me.rojo8399.placeholderapi.PlaceholderAPIPlugin;
import me.rojo8399.placeholderapi.PlaceholderServiceImpl;
import me.rojo8399.placeholderapi.configs.Messages;

public class PlayerExpansion implements Expansion {

	@Override
	public boolean canRegister() {
		return PlaceholderAPIPlugin.getInstance().getConfig().expansions.player;
	}

	@Override
	public String getIdentifier() {
		return "player";
	}

	@Override
	public String getAuthor() {
		return "rojo8399";
	}

	@Override
	public String getDescription() {
		return Messages.get().placeholder.playerdesc.value;
	}

	@Override
	public String getVersion() {
		return "1.2";
	}

	@Override
	public Object onValueRequest(Player p, Optional<String> identifier) {
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
			return p.getItemInHand(HandTypes.MAIN_HAND).orElse(null);
		case "item_in_off_hand":
			return p.getItemInHand(HandTypes.OFF_HAND).orElse(null);
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

	private static Text ofItem(@Nullable ItemStack item) {
		if (item == null) {
			return Text.EMPTY;
		}
		return Text.of(TextActions.showItem(item.createSnapshot()), item.getOrElse(Keys.DISPLAY_NAME, Text.of(item)));
	}

	@Override
	public Text onPlaceholderRequest(Player p, Optional<String> identifier) {
		Object val = onValueRequest(p, identifier);
		if (val == null) {
			return null;
		}
		if (val instanceof Text) {
			return (Text) val;
		}
		if (val instanceof String) {
			return TextSerializers.FORMATTING_CODE.deserialize((String) val);
		}
		if (val instanceof ItemStack) {
			return ofItem((ItemStack) val);
		}
		if (val instanceof Instant) {
			if (PlaceholderServiceImpl.get().getExpansion("time").isPresent()) {
				return TextSerializers.FORMATTING_CODE.deserialize(DateTimeFormatter
						.ofPattern(((DateTimeExpansion) PlaceholderServiceImpl.get().getExpansion("time").get()).format)
						.format(LocalDateTime.ofInstant((Instant) val, ZoneOffset.systemDefault())));
			}
		}
		return Text.of(val);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see me.rojo8399.placeholderapi.expansions.Expansion#getSupportedTokens()
	 */
	@Override
	public List<String> getSupportedTokens() {
		return Arrays.asList(null, "prefix", "suffix", "name", "displayname", "world", "option_[option]",
				"permission_[permission]", "ping", "flying", "language", "health", "max_health", "food", "saturation",
				"gamemode", "x", "y", "z", "direction", "time_played", "exp_total", "level", "exp_to_next", "exp",
				"first_join", "fly_speed", "walk_speed", "max_air", "remaining_air", "item_in_main_hand",
				"item_in_off_hand", "time_played_seconds", "time_played_ticks", "time_played_minutes",
				"time_played_hours", "time_played_days", "can_fly", "uuid");
	}

}