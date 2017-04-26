package me.rojo8399.placeholderapi.configs;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import com.google.common.reflect.TypeToken;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class Messages {

	public static final TypeToken<Messages> type = TypeToken.of(Messages.class);

	private static Messages inst;

	public static void init(Messages inst) {
		Messages.inst = inst;
	}

	public static Messages get() {
		return inst == null ? new Messages() : inst;
	}

	public static Text t(String m, Object... args) {
		return TextSerializers.FORMATTING_CODE.deserialize(String.format(m, args));
	}

	@Setting
	public Misc misc = new Misc();

	@ConfigSerializable
	public static class Misc {
		@Setting
		public String invalid = "&cThat is not a valid %s!";
		@Setting("no-permission")
		public String noPerm = "&cYou are not allowed to do that!";
		@Setting("no-value")
		public String noValue = "&cNo value present.";
		@Setting
		public String by = "by";
		@Setting
		public String version = "version";
		@Setting
		public Direction directions = new Direction();

		@ConfigSerializable
		public static class Direction {
			@Setting
			public String south = "South";
			@Setting
			public String southwest = "Southwest";
			@Setting
			public String west = "West";
			@Setting
			public String northwest = "Northwest";
			@Setting
			public String north = "North";
			@Setting
			public String northeast = "Northeast";
			@Setting
			public String east = "East";
			@Setting
			public String southeast = "Southeast";
		}
	}

	@Setting
	public Placeholders placeholder = new Placeholders();

	@ConfigSerializable
	public static class Placeholders {
		@Setting("javascript-description")
		public String jsdesc = "Execute JavaScripts.";
		@Setting("currency-description")
		public String curdesc = "View information about the server's economy.";
		@Setting("time-description")
		public String timedesc = "View the current date and time.";
		@Setting("player-description")
		public String playerdesc = "View information about a player.";
		@Setting("rank-description")
		public String rankdesc = "View information about a player's rank.";
		@Setting("server-description")
		public String serverdesc = "View information about the server.";
		@Setting("sound-description")
		public String sounddesc = "Play sounds to players.";
		@Setting("statistics-description")
		public String statdesc = "View a player's statistics.";
		@Setting("click-to-reload")
		public String clickReload = "&bClick to reload:";
		@Setting("reload-button-hover")
		public String reloadButtonHover = "&bClick to reload this placeholder!";
		@Setting("reload-button")
		public String reloadButton = "&c[RELOAD]";
		@Setting("supported-placeholders")
		public String supportedPlaceholders = "&6Supported placeholders:";
		@Setting("parse-button-hover")
		public String parseButtonHover = "&bClick to parse this placeholder for you!";
		@Setting("info-button-hover")
		public String infoButtonHover = "&bClick to get more info!";
		@Setting("available-placeholders")
		public String availablePlaceholders = "&aAvailable placeholders:";
		@Setting("must-specify")
		public String mustSpecify = "&cYou must specify a placeholder!";
		@Setting("invalid-placeholder")
		public String invalidPlaceholder = "&cThat is not a valid placeholder!";
		@Setting("reload-success")
		public String reloadSuccess = "&aPlaceholder reloaded successfully!";
		@Setting("reload-failed")
		public String reloadFailed = "&cPlaceholder failed to reload!";
	}

	@Setting
	public Plugins plugin = new Plugins();

	@ConfigSerializable
	public static class Plugins {
		@Setting("service-unavailable")
		public String serviceUnavailable = "&cPlaceholders are unavailable!";
		@Setting("reload-success")
		public String reloadSuccess = "&aPlaceholderAPI reloaded successfully!";
		@Setting("reload-failed")
		public String reloadFailed = "&cPlaceholderAPI failed to reload!";
	}

}
