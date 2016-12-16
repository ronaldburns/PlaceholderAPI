package me.rojo8399.placeholderapi.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import me.rojo8399.placeholderapi.PlaceholderAPIPlugin;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

public class Config implements Configurable {
	private static Config config = new Config();

	private Config() {
		;
	}

	public static Config getConfig() {
		return config;
	}

	private Path configFile = Paths.get(PlaceholderAPIPlugin.getInstance().getConfigDir() + "/config.conf");
	private ConfigurationLoader<CommentedConfigurationNode> configLoader = HoconConfigurationLoader.builder()
			.setPath(configFile).build();
	private CommentedConfigurationNode configNode;

	@Override
	public void setup() {
		if (!Files.exists(configFile)) {
			try {
				Files.createFile(configFile);
				load();
				populate();
				save();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			load();
		}
	}

	@Override
	public void load() {
		try {
			configNode = configLoader.load();
			
			/* 
			 * Config Updates
			 */
			switch (get().getNode("ConfigVersion").getInt()) {
			case 1: {
				get().getNode("ConfigVersion").setValue(2);
				get().getNode("PlayerCount")
						.setComment("Can be a math expression such as 5 + {online} * 2"
								+ "\nUse {online} for actual player count."
								+ "\nUse {randInt} for a random number. Set its min and max below.");
				get().getNode("RandomMin").setValue(0).setComment("The minimum the random number will be.");
				get().getNode("RandomMax").setValue(10).setComment("The maximum the random number will be.");
			}
			case 2: {
				// Alls Good :)
			}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void save() {
		try {
			configLoader.save(configNode);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void populate() {
		get().getNode("ConfigVersion").setValue(2);
		get().getNode("PlayerCount").setValue("{online} + 5 * {randInt}").setComment(
				"Can be a math expression such as 5 + {online} * 2" + "\nUse {online} for actual player count."
						+ "\nUse {randInt} for a random number. Set its min and max below.");
		get().getNode("RandomMin").setValue(0).setComment("The minimum the random number will be.");
		get().getNode("RandomMax").setValue(10).setComment("The maximum the random number will be.");
	}

	@Override
	public CommentedConfigurationNode get() {
		return configNode;
	}

}
