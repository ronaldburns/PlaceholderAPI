package me.rojo8399.placeholderapi;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.google.inject.Inject;

import me.rojo8399.placeholderapi.commands.ParseCommand;
import me.rojo8399.placeholderapi.configs.Config;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

@Plugin(id = PlaceholderAPIPlugin.PLUGIN_ID)
public class PlaceholderAPIPlugin {

	public static final String PLUGIN_ID = "placeholderapi";
	public static final String PLUGIN_NAME = "PlaceholderAPI";
	public static final String PLUGIN_VERSION = "1.0";

	private static PlaceholderAPIPlugin instance;

	@Inject
	Logger logger;

	@Inject
	Game game;

	@Inject
	private PluginContainer plugin;

	@Inject
	@DefaultConfig(sharedRoot = false)
	Path path;
	@Inject
	@DefaultConfig(sharedRoot = false)
	ConfigurationLoader<CommentedConfigurationNode> loader;
	Config config;

	@Listener
	public void onGamePreInitializationEvent(GamePreInitializationEvent event)
			throws IOException, ObjectMappingException {
		instance = this;
		plugin = game.getPluginManager().getPlugin(PLUGIN_ID).get();
		Asset conf = game.getAssetManager().getAsset(this, "config.conf").get();

		if (!Files.exists(path)) {
			try {
				conf.copyToFile(path);
			} catch (IOException ex) {
				logger.error("Could not copy the config file!");
				try {
					throw ex;
				} finally {
					mapDefault();
				}

			}
		}
		ConfigurationNode root;
		try {
			root = loader.load();
		} catch (IOException ex) {
			logger.error("Could not load the config file!");
			try {
				throw ex;
			} finally {
				mapDefault();
			}
		}
		updateConfig(root.getNode("version").getInt());
		try {
			config = root.getValue(Config.type);
		} catch (ObjectMappingException ex) {
			logger.error("Invalid config file!");
			try {
				throw ex;
			} finally {
				mapDefault();
			}
		}

	}

	@Listener
	public void onGameInitializationEvent(GameInitializationEvent event) {
		// Reigster Listeners and Commands

		// placeholderapi parse {player} [placeholders]
		CommandSpec parseCmd = CommandSpec.builder()
				.arguments(GenericArguments.onlyOne(GenericArguments.player(Text.of("player"))),
						GenericArguments.remainingJoinedStrings(Text.of("placeholders")))
				.executor(new ParseCommand())
				.build();

		// placeholderapi
		CommandSpec baseCmd = CommandSpec.builder()
				.child(parseCmd, "parse", "p")
				.build();

		game.getCommandManager().register(plugin, baseCmd, "placeholderapi", "papi");

	}

	@Listener
	public void onReload(GameReloadEvent event) throws IOException, ObjectMappingException {
		try {
			config = loader.load().getValue(Config.type);
		} catch (IOException ex) {
			logger.error("Could not reload config!");
			throw ex;
		} catch (ObjectMappingException ex) {
			logger.error("Invalid Config!");
			throw ex;
		}

		// Send Messages to console and player
		event.getCause().first(Player.class).ifPresent(p -> p.sendMessage(
				Text.builder().color(TextColors.GREEN).append(Text.of("Reloaded PlaceholderAPI")).build()));
		logger.info("Reloaded PlaceholderAPI");
	}

	private void updateConfig(int v) {
		switch (v) {
		case 1:
			// We're good!
		case 2:
			// How dafuq
		}
	}

	private void mapDefault() throws IOException, ObjectMappingException {
		try {
			config = loadDefault().getValue(Config.type);
		} catch (IOException | ObjectMappingException ex) {
			logger.error("Could not load the embedded default config! Disabling plugin.");
			game.getEventManager().unregisterPluginListeners(this);
			throw ex;
		}
	}

	private ConfigurationNode loadDefault() throws IOException {
		return HoconConfigurationLoader.builder()
				.setURL(game.getAssetManager().getAsset(this, "config.conf").get().getUrl()).build()
				.load(loader.getDefaultOptions());
	}

	public Config getConfig() {
		return config;
	}

	public Game getGame() {
		return game;
	}

	public Logger getLogger() {
		return logger;
	}

	public static PlaceholderAPIPlugin getInstance() {
		return instance;
	}

}
