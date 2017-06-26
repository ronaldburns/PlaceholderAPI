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
package me.rojo8399.placeholderapi.expansions;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.statistic.Statistics;
import org.spongepowered.api.text.Text;

import com.flowpowered.math.vector.Vector3d;

import me.rojo8399.placeholderapi.PlaceholderAPIPlugin;
import me.rojo8399.placeholderapi.configs.Messages;

public class PlayerExpansion implements RelationalExpansion {

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
		return "1.3";
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see me.rojo8399.placeholderapi.expansions.RelationalExpansion#
	 * onRelationalValueRequest(org.spongepowered.api.entity.living.player.
	 * Player, org.spongepowered.api.entity.living.player.Player,
	 * java.util.Optional)
	 */
	@Override
	public Object onRelationalValueRequest(Player one, Player two, Optional<String> token) {
		if (!token.isPresent()) {
			return null;
		}
		String t = token.get();
		switch (t.toLowerCase().trim()) {
		case "distance":
			return Math.round(one.getLocation().getPosition().distance(two.getLocation().getPosition()));
		case "visible":
			return one.canSee(two) && !two.get(Keys.VANISH).orElse(false);
		case "audible":
			return one.getMessageChannel().getMembers().contains(two);
		case "distance_x":
			return Math.abs(one.getLocation().getBlockX() - two.getLocation().getBlockX());
		case "distance_y":
			return Math.abs(one.getLocation().getBlockY() - two.getLocation().getBlockY());
		case "distance_z":
			return Math.abs(one.getLocation().getBlockZ() - two.getLocation().getBlockZ());
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see me.rojo8399.placeholderapi.expansions.RelationalExpansion#
	 * getSupportedRelationalTokens()
	 */
	@Override
	public List<String> getSupportedRelationalTokens() {
		return Arrays.asList("distance", "distance_x", "distance_y", "distance_z", "visible", "audible");
	}

}