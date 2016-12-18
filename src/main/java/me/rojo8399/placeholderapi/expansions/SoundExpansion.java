package me.rojo8399.placeholderapi.expansions;

import java.util.Optional;

import org.spongepowered.api.Game;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.entity.living.player.Player;

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

		Optional<SoundType> sound = game.getRegistry().getType(SoundType.class, i[0]);
		Vector3d position = p.getLocation().getPosition();
		Double volume = Double.valueOf((i[1] == null) ? String.valueOf(1) : i[1]);
		Double pitch = Double.valueOf((i[2] == null) ? String.valueOf(1) : i[2]);

		if (config.expansions.sound && sound.isPresent()) {
			p.playSound(sound.get(), position, volume, pitch);
			return "";
		} else {
			return null;
		}

	}

}
