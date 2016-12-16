package me.rojo8399.placeholderapi;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.google.inject.Inject;

import me.rojo8399.placeholderapi.config.Config;

@Plugin(id = PlaceholderAPIPlugin.PLUGIN_ID)
public class PlaceholderAPIPlugin {

	public static final String PLUGIN_ID = "placeholders";
	public static final String PLUGIN_NAME = "Placeholders";
	public static final String PLUGIN_VERSION = "1.0";
	
	private static PlaceholderAPIPlugin instance;
	
	@Inject
	private Logger logger;
	
	@SuppressWarnings("unused")
	@Inject
	private PluginContainer plugin;

	@Inject
	@ConfigDir(sharedRoot = false)
	private Path configDir;
	
	@Listener
	public void onGamePreInitializationEvent(GamePreInitializationEvent event) {
		instance = this;
		plugin = Sponge.getPluginManager().getPlugin(PLUGIN_ID).get();

		// Create Configuration Directory for CustomPlayerCount
		if (!Files.exists(configDir)) {
			try {
				Files.createDirectories(configDir);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		Config.getConfig().setup();
	}
	
	@Listener
	public void onClientJoinEvent(ClientConnectionEvent.Join e) {
		Player p = e.getTargetEntity();
		e.setMessage(Text.of(PlaceholderAPI.setPlaceholders(p, "%player_name% just joined! Server ram is at %ram_used%/%ram_total% MB!")));
	}
	
	@Listener
	public void onGameInitializationEvent(GameInitializationEvent event) {
		
		// Reigster Listeners and Commands
	}

	@Listener
	public void onReload(GameReloadEvent event) {
		Config.getConfig().load();
		event.getCause().first(Player.class).ifPresent(p -> p.sendMessage(Text.builder().color(TextColors.GREEN).append(Text.of("Reloading PlaceholderAPI...")).build()));
		getLogger().info("Reloading PlaceholderAPI...");
	}

	public Logger getLogger() {
		return logger;
	}

	public static PlaceholderAPIPlugin getInstance() {
		return instance;
	}

	public Path getConfigDir() {
		return configDir;
	}
	
}
