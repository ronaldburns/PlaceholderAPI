package me.rojo8399.placeholderapi;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

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
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.google.inject.Inject;

import me.rojo8399.placeholderapi.commands.InfoCommand;
import me.rojo8399.placeholderapi.commands.ListCommand;
import me.rojo8399.placeholderapi.commands.ParseCommand;
import me.rojo8399.placeholderapi.configs.Config;
import me.rojo8399.placeholderapi.expansions.Expansion;
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
		// Register internal placeholders
		s.registerPlaceholder(new PlayerExpansion());
		s.registerPlaceholder(new ServerExpansion());
		s.registerPlaceholder(new SoundExpansion());
		s.registerPlaceholder(new RankExpansion());
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
		Task.builder().async().execute(() -> {
			try {
				// Since classloading is intense but doesn't require Sponge
				// resource access, can be done async
				loadExpansions();
			} catch (Exception e) {
				logger.error("Error loading expansions!");
			}
		}).submit(this);
	}

	private void loadExpansions() throws Exception {
		// Get dir of config and insert folder
		File dir = new File(this.path.toFile().getParentFile(), "expansions");
		if (dir.exists() && !dir.isDirectory()) {
			// Exists but is not a directory so need to remove
			dir.delete();
		}
		if (!dir.exists()) {
			dir.mkdirs();
			// No placeholders present, just create folder then ignore
			return;
		}
		// Skip already loaded files in case a .class + .java of the same file
		// exist
		List<String> loaded = new ArrayList<>();
		for (File exp : dir.listFiles()) {
			// Ignore non .class/.java files
			if (exp.isDirectory()) {
				continue;
			}
			if (!exp.getName().endsWith(".class") && !exp.getName().endsWith(".java")) {
				continue;
			}
			// Name without file extension
			String wofe = exp.getName().substring(0, exp.getName().lastIndexOf("."));
			if (loaded.contains(wofe)) {
				// Already loaded
				continue;
			}
			if (exp.getName().endsWith(".java")) {
				// Compile into .class. Compiler will create the file
				// automatically.
				JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
				compiler.run(null, null, null, exp.getPath());
				// Redirect attention to .class to load expansion properly.
				exp = new File(exp.getParentFile(), exp.getName().replace(".java", ".class"));
			}
			Class<?> clazz;
			// File content
			byte[] content = getClassContent(exp);
			// Shady reflection to use this method. Only have to do this because
			// loadClass does not work.
			Method def = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class,
					int.class, ProtectionDomain.class);
			boolean a = def.isAccessible();// Revert unwanted changes.
			def.setAccessible(true);
			// Load class from file
			clazz = (Class<?>) def.invoke(this.getClass().getClassLoader(), null, content, 0, content.length,
					this.getClass().getProtectionDomain());
			def.setAccessible(a);
			// Make sure it is an implementation of Expansion.
			if (!Expansion.class.isAssignableFrom(clazz)) {
				continue;
			}
			Expansion e = null;
			try {
				// We don't care about the class if it is not instantiable, so
				// we can force this.
				e = (Expansion) clazz.newInstance();
			} catch (Exception ex) {
				continue;
			}
			// Load placeholder.
			s.registerPlaceholder(e);
			loaded.add(wofe);
		}
	}

	/*
	 * Load file into a byte array.
	 */
	private static byte[] getClassContent(File f) {
		try {
			FileInputStream input = new FileInputStream(f);
			byte[] content = new byte[(int) f.length()];
			input.read(content);
			input.close();
			return content;
		} catch (Exception e) {
			return new byte[0];
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
