package me.rojo8399.placeholderapi.expansions;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Optional;

import org.spongepowered.api.CatalogType;
import org.spongepowered.api.Game;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import com.flowpowered.math.vector.Vector3d;

import me.rojo8399.placeholderapi.PlaceholderAPIPlugin;
import me.rojo8399.placeholderapi.configs.Config;

public class SoundExpansion extends Expansion {

	@Override
	public boolean canRegister() {
		return true;
	}

	@Override
	public String getIdentifier() {
		return "sound";
	}

	@Override
	public String getAuthor() {
		return "rojo8399";
	}

	@Override
	public String getVersion() {
		return "1.0";
	}

	public static String onPlaceholderRequest(Player p, String identifier) {

		Config config = PlaceholderAPIPlugin.getInstance().getConfig();
		Game game = PlaceholderAPIPlugin.getInstance().getGame();

		String[] i = identifier.split("-");
		
		HashMap<String, String> soundConverter = new HashMap<String, String>();

		Arrays.stream(SoundTypes.class.getDeclaredFields()).filter(x -> Modifier.isStatic(x.getModifiers()))
				.sorted(Comparator.comparing(Field::getName)).forEach(x -> {
					try {
						x.setAccessible(true);
						soundConverter.put(x.getName(), ((CatalogType) x.get(null)).getId());
					} catch (IllegalAccessException ex) {
						PlaceholderAPIPlugin.getInstance().getLogger()
								.error("Field: " + x.getName() + " - could not get.");
					}
				});

		Optional<SoundType> sound = game.getRegistry().getType(SoundType.class, soundConverter.get(i[0]));
		Vector3d position = p.getLocation().getPosition();
		Double volume = Double.valueOf(i[1]);
		Double pitch = Double.valueOf(i[2]);

		if (config.expansions.sound && sound.isPresent()) {
			p.playSound(sound.get(), position, volume, pitch);
			return "";
		} else {
			// For Debuging
			p.sendMessage(Text.of("Sound: " + i[0] + ", Volume: " + i[1] + ", Pitch: " + i[2]));
			return null;
		}

	}

}
