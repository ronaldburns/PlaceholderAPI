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
package me.rojo8399.placeholderapi.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.google.inject.Inject;

import me.rojo8399.placeholderapi.PlaceholderService;
import me.rojo8399.placeholderapi.impl.commands.InfoCommand;
import me.rojo8399.placeholderapi.impl.commands.ListCommand;
import me.rojo8399.placeholderapi.impl.commands.ParseCommand;
import me.rojo8399.placeholderapi.impl.commands.RefreshCommand;
import me.rojo8399.placeholderapi.impl.configs.Config;
import me.rojo8399.placeholderapi.impl.configs.JavascriptManager;
import me.rojo8399.placeholderapi.impl.configs.Messages;
import me.rojo8399.placeholderapi.impl.placeholder.Defaults;
import me.rojo8399.placeholderapi.impl.placeholder.Expansion;
import me.rojo8399.placeholderapi.impl.placeholder.Store;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

@Plugin(id = PlaceholderAPIPlugin.PLUGIN_ID, name = PlaceholderAPIPlugin.PLUGIN_NAME, version = PlaceholderAPIPlugin.PLUGIN_VERSION, authors = {
		"rojo8399", "Wundero" })
public class PlaceholderAPIPlugin {

	private static PlaceholderAPIPlugin instance;
	public static final String PLUGIN_ID = "placeholderapi";
	public static final String PLUGIN_NAME = "PlaceholderAPI";
	public static final String PLUGIN_VERSION = "4.4";

	public static PlaceholderAPIPlugin getInstance() {
		return instance;
	}

	private Set<Object> alreadyRegistered = new HashSet<>();

	private Config config;

	@Inject
	@ConfigDir(sharedRoot = false)
	private Path configDir;

	private DateTimeFormatter formatter;

	@Inject
	private Game game;
	private JavascriptManager jsm;
	@Inject
	@DefaultConfig(sharedRoot = false)
	private ConfigurationLoader<CommentedConfigurationNode> loader;
	@Inject
	private Logger logger;
	@Inject
	private Metrics metrics;
	private ConfigurationLoader<CommentedConfigurationNode> msgloader;
	private ConfigurationNode msgRoot;
	private Messages msgs;
	@Inject
	@DefaultConfig(sharedRoot = false)
	private Path path;

	@Inject
	private PluginContainer plugin;

	private ConfigurationNode root;

	private PlaceholderService s;

	public DateTimeFormatter formatter() {
		return formatter;
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

	public ConfigurationNode getRootConfig() {
		return root;
	}

	private ConfigurationNode loadDefault() throws IOException {
		return HoconConfigurationLoader.builder()
				.setURL(game.getAssetManager().getAsset(this, "config.conf").get().getUrl()).build()
				.load(loader.getDefaultOptions());
	}

	private void mapDefault() throws IOException, ObjectMappingException {
		try {
			config = (root = loadDefault()).getValue(Config.type);
		} catch (IOException | ObjectMappingException ex) {
			logger.error("Could not load the embedded default config! Disabling plugin.");
			game.getEventManager().unregisterPluginListeners(this);
			throw ex;
		}
	}

	@Listener
	public void onGameInitializationEvent(GameInitializationEvent event) {
		// Reigster Listeners and Commands

		// placeholderapi parse {player} [placeholders]
		CommandSpec parseCmd = CommandSpec.builder()
				.arguments(GenericArguments.onlyOne(GenericArguments.player(Text.of("player"))),
						GenericArguments.remainingJoinedStrings(Text.of("placeholders")))
				.executor(new ParseCommand()).permission("placeholderapi.admin").build();
		// papi list
		CommandSpec listCmd = CommandSpec.builder().permission("placeholderapi.admin").executor(new ListCommand())
				.build();
		// papi info {expansion}
		CommandSpec infoCmd = CommandSpec.builder().arguments(GenericArguments.string(Text.of("placeholder")))
				.permission("placeholderapi.admin").executor(new InfoCommand()).build();
		CommandSpec reloadCommand = CommandSpec.builder()
				.arguments(GenericArguments.optional(GenericArguments.string(Text.of("id"))))
				.permission("placeholderapi.admin").executor(new RefreshCommand()).build();
		CommandSpec enable = CommandSpec.builder().executor((src, params) -> {
			String id = params.<String>getOne("id").orElse(null);
			if (!Store.get().has(id)) {
				throw new CommandException(Messages.get().placeholder.invalidPlaceholder.t());
			}
			Boolean rel = params.<Boolean>getOne("relational").orElse(null);
			if (rel != null) {
				Optional<Expansion<?, ?, ?>> e = Store.get().get(id, rel);
				if (!e.isPresent()) {
					throw new CommandException(Messages.get().placeholder.invalidPlaceholder.t());
				}
				e.get().enable();
				src.sendMessage(Messages.get().placeholder.placeholderEnabled.t());
				return CommandResult.success();
			} else {
				Expansion<?, ?, ?> e = Store.get().get(id, false).orElse(Store.get().get(id, false)
						.orElseThrow(() -> new CommandException(Messages.get().placeholder.invalidPlaceholder.t())));
				e.enable();
				src.sendMessage(Messages.get().placeholder.placeholderEnabled.t());
				return CommandResult.success();
			}
		}).arguments(GenericArguments.string(Text.of("id")),
				GenericArguments.optional(GenericArguments.bool(Text.of("relational"))))
				.permission("placeholderapi.admin").build();

		CommandSpec disable = CommandSpec.builder().executor((src, params) -> {
			String id = params.<String>getOne("id").orElse(null);
			if (!Store.get().has(id)) {
				throw new CommandException(Messages.get().placeholder.invalidPlaceholder.t());
			}
			Boolean rel = params.<Boolean>getOne("relational").orElse(null);
			if (rel != null) {
				Optional<Expansion<?, ?, ?>> e = Store.get().get(id, rel);
				if (!e.isPresent()) {
					throw new CommandException(Messages.get().placeholder.invalidPlaceholder.t());
				}
				e.get().disable();
				src.sendMessage(Messages.get().placeholder.placeholderDisabled.t());
				return CommandResult.success();
			} else {
				Expansion<?, ?, ?> e = Store.get().get(id, false).orElse(Store.get().get(id, false)
						.orElseThrow(() -> new CommandException(Messages.get().placeholder.invalidPlaceholder.t())));
				e.disable();
				src.sendMessage(Messages.get().placeholder.placeholderDisabled.t());
				return CommandResult.success();
			}
		}).arguments(GenericArguments.string(Text.of("id")),
				GenericArguments.optional(GenericArguments.bool(Text.of("relational"))))
				.permission("placeholderapi.admin").build();
		// placeholderapi
		CommandSpec baseCmd = CommandSpec.builder().executor(new CommandExecutor() {
			// send plugin name + version
			@Override
			public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
				src.sendMessage(Text.of(TextColors.GREEN, PLUGIN_NAME, TextColors.GRAY, " ",
						Messages.get().misc.version.t(), " ", TextColors.AQUA, PLUGIN_VERSION, TextColors.GRAY, "."));
				return CommandResult.success();
			}
		}).child(parseCmd, "parse", "p").child(listCmd, "list", "l").child(infoCmd, "info", "i")
				.child(reloadCommand, "reload", "r").child(enable, "enable").child(disable, "disable").build();
		game.getCommandManager().register(plugin, baseCmd, "placeholderapi", "papi");

	}

	@Listener
	public void onGamePreInitializationEvent(GamePreInitializationEvent event)
			throws IOException, ObjectMappingException {
		instance = this;
		plugin = game.getPluginManager().getPlugin(PLUGIN_ID).get();
		Asset conf = game.getAssetManager().getAsset(this, "config.conf").get();
		jsm = new JavascriptManager(new File(path.toFile().getParentFile(), "javascript"));
		// Load config
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
		try {
			// default config object should now properly load i hope
			config = root.getValue(Config.type, new Config());
		} catch (ObjectMappingException ex) {
			logger.error("Invalid config file!");
			try {
				throw ex;
			} finally {
				mapDefault();
			}
		}
		if (config == null) {
			config = new Config();
			this.root.setValue(Config.type, config);
			try {
				this.loader.save(root);
			} catch (Exception e) {
			}
		}
		File msgFile = new File(configDir.toFile(), "messages.conf");
		msgloader = HoconConfigurationLoader.builder().setFile(msgFile).build();
		try {
			msgs = (msgRoot = msgloader.load()).getValue(Messages.type);
			if (msgs == null) {
				msgs = new Messages();
				msgRoot.setValue(Messages.type, msgs);
				msgloader.save(msgRoot);
			}
		} catch (IOException ex) {
			logger.error("Could not load the messages file!");
			try {
				throw ex;
			} finally {
				msgs = new Messages();
				msgRoot.setValue(Messages.type, msgs);
				msgloader.save(msgRoot);
			}
		} catch (ObjectMappingException ex) {
			logger.error("Invalid messages file!");
			try {
				throw ex;
			} finally {
				msgs = new Messages();
				msgRoot.setValue(Messages.type, msgs);
				msgloader.save(msgRoot);
			}
		}
		Messages.init(msgs);
		this.formatter = DateTimeFormatter
				.ofPattern(config.dateFormat == null ? "uuuu LLL dd HH:mm:ss" : config.dateFormat);
		s = PlaceholderServiceImpl.get();
		registerPlaceholders();
		// Provide default implementation
		game.getServiceManager().setProvider(this, PlaceholderService.class, s);
	}

	@Listener
	public void onGameStartingServerEvent(GameStartingServerEvent event) {
		metrics.addCustomChart(new Metrics.SimpleBarChart("placeholders") {
			@Override
			public HashMap<String, Integer> getValues(HashMap<String, Integer> valueMap) {
				List<String> rids = Store.get().ids(true);
				List<String> ids = Store.get().ids(false);
				if (rids.isEmpty() && ids.isEmpty()) {
					HashMap<String, Integer> out = new HashMap<>();
					out.put("none", 1);
					return out;
				}
				List<String> exp = new ArrayList<>();
				rids.forEach(e -> exp.add("rel_" + e));
				ids.forEach(exp::add);
				return (HashMap<String, Integer>) exp.stream().collect(Collectors.toMap(e -> e, e -> 1));
			}
		});
	}

	@Listener
	public void onReload(GameReloadEvent event) throws IOException, ObjectMappingException {
		reloadConfig();

		// Send Messages to console and player
		event.getCause().first(Player.class).ifPresent(p -> p.sendMessage(Messages.get().plugin.reloadSuccess.t()));
		logger.info("Reloaded PlaceholderAPI");
	}

	@Listener
	public void onStop(GameStoppingEvent event) {
		saveConfig();
		Store.get().saveAll();
	}

	public void registerListeners(Object object) {
		registerListeners(object, this);
	}

	public void registerListeners(Object object, Object plugin) {
		if (alreadyRegistered.contains(object)) {
			return;
		}
		Sponge.getEventManager().registerListeners(plugin, object);
		alreadyRegistered.add(object);
	}

	public void registerPlaceholders() {
		EconomyService ex = game.getServiceManager().provide(EconomyService.class).orElse(null);
		Defaults handle = new Defaults(ex, this.jsm, s);
		registerListeners(handle);
		s.loadAll(handle, this).stream().map(builder -> {
			switch (builder.getId()) {
			case "player": {
				if (builder.isRelational()) {
					return builder.description(Messages.get().placeholder.relplayerdesc.value)
							.tokens("distance", "audible", "visible", "distance_x", "distance_y", "distance_z")
							.version("2.0");
				} else {
					return builder.description(Messages.get().placeholder.playerdesc.value)
							.tokens(null, "prefix", "suffix", "option_[option]", "permission_[permission]", "name",
									"displayname", "uuid", "can_fly", "world", "ping", "language", "flying", "health",
									"max_health", "food", "saturation", "gamemode", "x", "y", "z", "direction", "exp",
									"exp_total", "exp_to_next", "level", "first_join", "fly_speed", "max_air",
									"remaining_air", "item_in_main_hand", "item_in_off_hand", "walk_speed",
									"time_played", "time_played_ticks", "time_played_seconds", "time_played_minutes",
									"time_played_hours", "time_played_days")
							.version("2.0");
				}
			}
			case "rank": {
				if (builder.isRelational()) {
					return builder.description(Messages.get().placeholder.relrankdesc.value)
							.tokens("greater_than", "less_than").version("1.0");
				} else {
					return builder.description(Messages.get().placeholder.rankdesc.value)
							.tokens(null, "prefix", "suffix", "name", "permission_[permission]", "option_[option]")
							.version("2.0");
				}
			}
			case "javascript":
				return builder.description(Messages.get().placeholder.jsdesc.value).tokens(jsm.getScriptNames())
						.reloadFunction(e -> {
							try {
								jsm.reloadScripts();
							} catch (Exception exc) {
								getLogger().warn("Error reloading JavaScript placeholders!");
								exc.printStackTrace();
								return false;
							}
							e.setTokens(jsm.getScriptNames());
							return true;
						}).version("2.0");
			case "economy":
				return builder.description(Messages.get().placeholder.curdesc.value)
						.tokens("", "[currency]", "balance", "balance_[currency]", "bal_format_[currency]",
								"bal_format", "display", "display_[currency]", "plural_display_[currency]",
								"symbol_[currency]", "plural_display", "symbol", "baltop_<number>",
								"baltop_<number>_[currency]")
						.version("2.0");
			case "server":
				return builder.description(Messages.get().placeholder.serverdesc.value)
						.tokens("time_[world]", "game_time_[world]", "unique_players", "online", "max_players", "motd",
								"cores", "tps", "ram_used", "ram_free", "ram_total", "ram_max", "uptime",
								"uptime_percent", "uptime_total")
						.version("2.0");
			case "sound":
				return builder.description(Messages.get().placeholder.sounddesc.value)
						.tokens("[sound]-[volume]-[pitch]", "[sound]-[volume]-[pitch]_all").version("2.0");
			case "statistic":
				return builder.description(Messages.get().placeholder.statdesc.value).version("2.0");
			case "time":
				return builder.description(Messages.get().placeholder.timedesc.value).tokens("").version("2.0");
			}
			return builder;
		}).forEach(t -> {
			try {
				String i = t.getId();
				boolean r = t.isRelational();
				t.author("Wundero").plugin(this).buildAndRegister();
				Store.get().get(i, r).ifPresent(e -> e.reloadListeners());
			} catch (Exception e) {
			}
		});
	}

	public void reloadConfig() throws IOException, ObjectMappingException {
		try {
			config = (root = loader.load()).getValue(Config.type);
		} catch (IOException ex) {
			logger.error("Could not reload config!");
			throw ex;
		} catch (ObjectMappingException ex) {
			logger.error("Invalid Config!");
			throw ex;
		}
		try {
			msgs = (msgRoot = msgloader.load()).getValue(Messages.type);
			if (msgs == null) {
				msgs = new Messages();
				msgRoot.setValue(Messages.type, msgs);
				msgloader.save(msgRoot);
			}
		} catch (IOException ex) {
			logger.error("Could not reload config!");
			throw ex;
		} catch (ObjectMappingException ex) {
			logger.error("Invalid Config!");
			throw ex;
		}
		Messages.init(msgs);
	}

	public void saveConfig() {
		try {
			loader.save(root);
		} catch (Exception e) {
		}
	}

	public void unregisterListeners(Object object) {
		if (alreadyRegistered.contains(object)) {
			alreadyRegistered.remove(object);
			Sponge.getEventManager().unregisterListeners(object);
		}
	}

}
