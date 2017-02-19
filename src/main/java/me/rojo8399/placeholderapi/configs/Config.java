package me.rojo8399.placeholderapi.configs;

import com.google.common.reflect.TypeToken;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class Config {
    public static final TypeToken<Config> type = TypeToken.of(Config.class);
    @Setting
    public Expansions expansions;

    @ConfigSerializable
    public static class Expansions {
	@Setting
	public boolean player = true;
	@Setting
	public boolean server = true;
	@Setting
	public boolean rank = false;
	@Setting
	public boolean sound = false;
	@Setting
	public boolean javascript = false;
	@Setting
	public boolean currency = false;

    }
}
