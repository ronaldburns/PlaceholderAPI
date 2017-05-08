package me.rojo8399.placeholderapi.expansions;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.spongepowered.api.Game;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import com.flowpowered.math.vector.Vector3d;

import me.rojo8399.placeholderapi.PlaceholderAPIPlugin;
import me.rojo8399.placeholderapi.configs.Messages;

public class SoundExpansion implements Expansion {

	@Override
	public boolean canRegister() {
		return PlaceholderAPIPlugin.getInstance().getConfig().expansions.sound;
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
	public String getDescription() {
		return Messages.get().placeholder.sounddesc.value;
	}

	@Override
	public String getVersion() {
		return "1.0";
	}

	@Override
	public Text onPlaceholderRequest(Player p, Optional<String> identifier) {

		if (!identifier.isPresent()) {
			// No sound present
			return null;
		}
		Game game = PlaceholderAPIPlugin.getInstance().getGame();

		String[] i = identifier.get().split("-");
		Optional<SoundType> sound = game.getRegistry().getType(SoundType.class, i[0].replace("_", "."));
		Vector3d position = p.getLocation().getPosition();
		Double volume = Double.valueOf((i[1] == null) ? String.valueOf(1) : i[1]);
		Double pitch = Double.valueOf((i[2] == null) ? String.valueOf(1) : i[2]);

		if (sound.isPresent()) {
			p.playSound(sound.get(), position, volume, pitch);
			return Text.EMPTY;// Remove text from replacement
		} else {
			return null;// Leave text in replacement
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see me.rojo8399.placeholderapi.expansions.Expansion#getSupportedTokens()
	 */
	@Override
	public List<String> getSupportedTokens() {
		return Arrays.asList("[sound]-[volume]-[pitch]");
	}

}
