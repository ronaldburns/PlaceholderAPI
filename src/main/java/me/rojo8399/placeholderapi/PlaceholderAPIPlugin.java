package me.rojo8399.placeholderapi;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.google.inject.Inject;

import me.rojo8399.placeholderapi.commands.InfoCommand;
import me.rojo8399.placeholderapi.commands.ListCommand;
import me.rojo8399.placeholderapi.commands.ParseCommand;
import me.rojo8399.placeholderapi.configs.Config;
import me.rojo8399.placeholderapi.configs.JavascriptManager;
import me.rojo8399.placeholderapi.expansions.CurrencyExpansion;
import me.rojo8399.placeholderapi.expansions.DateTimeExpansion;
import me.rojo8399.placeholderapi.expansions.Expansion;
import me.rojo8399.placeholderapi.expansions.JavascriptExpansion;
import me.rojo8399.placeholderapi.expansions.PlayerExpansion;
import me.rojo8399.placeholderapi.expansions.RankExpansion;
import me.rojo8399.placeholderapi.expansions.ServerExpansion;
import me.rojo8399.placeholderapi.expansions.SoundExpansion;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

@Plugin(id = PlaceholderAPIPlugin.PLUGIN_ID, name = PlaceholderAPIPlugin.PLUGIN_NAME, version = PlaceholderAPIPlugin.PLUGIN_VERSION, authors = {
		"rojo8399", "Wundero" })
public class PlaceholderAPIPlugin {

	public static final String PLUGIN_ID = "placeholderapi";
	public static final String PLUGIN_NAME = "PlaceholderAPI";
	public static final String PLUGIN_VERSION = "2.1";
	private static PlaceholderAPIPlugin instance;

	@Inject
	Logger logger;

	@Inject
	Game game;

	@Inject
	private PluginContainer plugin;

	private JavascriptManager jsm;

	@Inject
	@DefaultConfig(sharedRoot = false)
	Path path;
	@Inject
	@DefaultConfig(sharedRoot = false)
	ConfigurationLoader<CommentedConfigurationNode> loader;
	Config config;

	PlaceholderService s;

	@Listener
	public void onGamePreInitializationEvent(GamePreInitializationEvent event)
			throws IOException, ObjectMappingException {
		instance = this;
		// Provide default implementation
		game.getServiceManager().setProvider(this, PlaceholderService.class, s = new PlaceholderServiceImpl());
		plugin = game.getPluginManager().getPlugin(PLUGIN_ID).get();
		Asset conf = game.getAssetManager().getAsset(this, "config.conf").get();
		jsm = new JavascriptManager(new File(path.toFile().getParentFile(), "javascript"));
		// Register internal placeholders
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
		s.registerPlaceholder(new JavascriptExpansion(jsm));
		s.registerPlaceholder(new PlayerExpansion());
		s.registerPlaceholder(new ServerExpansion());
		s.registerPlaceholder(new SoundExpansion());
		s.registerPlaceholder(new RankExpansion());
		if (game.getServiceManager().provide(EconomyService.class).isPresent()) {
			s.registerPlaceholder(
					new CurrencyExpansion(game.getServiceManager().provideUnchecked(EconomyService.class)));
		}
		s.registerPlaceholder(new DateTimeExpansion());
	}

	@Listener
	public void onGameInitializationEvent(GameInitializationEvent event) {
		// Reigster Listeners and Commands

		// placeholderapi parse {player} [placeholders]
		CommandSpec parseCmd = CommandSpec.builder()
				.arguments(GenericArguments.onlyOne(GenericArguments.player(Text.of("player"))),
						GenericArguments.remainingJoinedStrings(Text.of("placeholders")))
				.executor(new ParseCommand()).permission("placeholderapi.admin").build();
		// Map of expansion names for info command.
		Map<String, String> map = new HashMap<>();
		for (Expansion e : s.getExpansions()) {
			map.put(e.getIdentifier(), e.getIdentifier());
		}
		// papi list
		CommandSpec listCmd = CommandSpec.builder().permission("placeholderapi.admin").executor(new ListCommand())
				.build();
		// papi info {expansion}
		CommandSpec infoCmd = CommandSpec.builder()
				.arguments(GenericArguments.choices(Text.of("placeholder"), map, false))
				.permission("placeholderapi.admin").executor(new InfoCommand()).build();

		// placeholderapi
		CommandSpec baseCmd = CommandSpec.builder().executor(new CommandExecutor() {
			// send plugin name + version
			@Override
			public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
				src.sendMessage(Text.of(TextColors.GREEN, PLUGIN_NAME, TextColors.GRAY, " version ", TextColors.AQUA,
						PLUGIN_VERSION, TextColors.GRAY, "."));
				return CommandResult.success();
			}
		}).child(parseCmd, "parse", "p").child(listCmd, "list", "l").child(infoCmd, "info", "i").build();
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
