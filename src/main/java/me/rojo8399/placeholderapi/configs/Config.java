package me.rojo8399.placeholderapi.configs;

import com.google.common.reflect.TypeToken;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class Config {
	public static final TypeToken<Config> type = TypeToken.of(Config.class);
	@Setting public Integer version;
	@Setting public Expansions expansions;
	@ConfigSerializable
	public static class Expansions {
		@Setting public boolean player;
		@Setting public boolean server;
		@Setting public boolean rankupper;
		@Setting public boolean sound;
	}
}
