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
package me.rojo8399.placeholderapi;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.google.inject.Inject;

import me.rojo8399.placeholderapi.commands.InfoCommand;
import me.rojo8399.placeholderapi.commands.ListCommand;
import me.rojo8399.placeholderapi.commands.ParseCommand;
import me.rojo8399.placeholderapi.commands.RefreshCommand;
import me.rojo8399.placeholderapi.configs.Config;
import me.rojo8399.placeholderapi.configs.JavascriptManager;
import me.rojo8399.placeholderapi.configs.Messages;
import me.rojo8399.placeholderapi.placeholder.impl.Defaults;
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
	public static final String PLUGIN_VERSION = "4.0";
	private static PlaceholderAPIPlugin instance;

	@Inject
	private Logger logger;

	@Inject
	private Metrics metrics;

	@Inject
	private Game game;

	@Inject
	private PluginContainer plugin;

	private JavascriptManager jsm;

	@Inject
	@DefaultConfig(sharedRoot = false)
	private Path path;
	@Inject
	@DefaultConfig(sharedRoot = false)
	private ConfigurationLoader<CommentedConfigurationNode> loader;
	@Inject
	@ConfigDir(sharedRoot = false)
	private Path configDir;
	private ConfigurationNode root;
	private Config config;
	private ConfigurationNode msgRoot;
	private Messages msgs;
	private ConfigurationLoader<CommentedConfigurationNode> msgloader;
	private DateTimeFormatter formatter;

	private PlaceholderService s;

	public DateTimeFormatter formatter() {
		return formatter;
	}

	public ConfigurationNode getRootConfig() {
		return root;
	}

	public void saveConfig() {
		try {
			loader.save(root);
		} catch (Exception e) {
		}
	}

	@Listener
	public void onGamePreInitializationEvent(GamePreInitializationEvent event)
			throws IOException, ObjectMappingException {
		instance = this;
		// Provide default implementation
		game.getServiceManager().setProvider(this, PlaceholderService.class, s = PlaceholderServiceImpl.get());
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
			config = root.getValue(Config.type);
		} catch (ObjectMappingException ex) {
			logger.error("Invalid config file!");
			try {
				throw ex;
			} finally {
				mapDefault();
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
		this.formatter = DateTimeFormatter.ofPattern(config.dateFormat);
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
		// placeholderapi
		CommandSpec baseCmd = CommandSpec.builder().executor(new CommandExecutor() {
			// send plugin name + version
			@Override
			public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
				src.sendMessage(Text.of(TextColors.GREEN, PLUGIN_NAME, TextColors.GRAY, " ",
						Messages.get().misc.version, " ", TextColors.AQUA, PLUGIN_VERSION, TextColors.GRAY, "."));
				return CommandResult.success();
			}
		}).child(parseCmd, "parse", "p").child(listCmd, "list", "l").child(infoCmd, "info", "i")
				.child(reloadCommand, "reload", "r").build();
		game.getCommandManager().register(plugin, baseCmd, "placeholderapi", "papi");

	}

	@Listener
	public void onGameStartingServerEvent(GameStartingServerEvent event) {
		registerPlaceholders();
		metrics.addCustomChart(new Metrics.SimpleBarChart("placeholders") {
			@Override
			public HashMap<String, Integer> getValues(HashMap<String, Integer> valueMap) {
				Set<String> rids = ((PlaceholderServiceImpl) s).getStore().ids(true);
				Set<String> ids = ((PlaceholderServiceImpl) s).getStore().ids(false);
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

	public void registerPlaceholders() {
		EconomyService ex = game.getServiceManager().provide(EconomyService.class).orElse(null);
		s.registerPlaceholders(() -> new Defaults(ex, this.jsm), this);
	}

	@Listener
	public void onReload(GameReloadEvent event) throws IOException, ObjectMappingException {
		reloadConfig();

		// Send Messages to console and player
		event.getCause().first(Player.class).ifPresent(p -> p.sendMessage(Messages.get().plugin.reloadSuccess.t()));
		logger.info("Reloaded PlaceholderAPI");
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

	private void mapDefault() throws IOException, ObjectMappingException {
		try {
			config = (root = loadDefault()).getValue(Config.type);
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
