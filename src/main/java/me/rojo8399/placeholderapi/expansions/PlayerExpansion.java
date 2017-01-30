package me.rojo8399.placeholderapi.expansions;

import java.util.Optional;
import java.util.function.Function;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import me.rojo8399.placeholderapi.PlaceholderAPIPlugin;
import me.rojo8399.placeholderapi.configs.Config;

public class PlayerExpansion implements Expansion {

	@Override
	public boolean canRegister() {
		return true;
	}

	@Override
	public String getIdentifier() {
		return "player";
	}

	@Override
	public String getAuthor() {
		return "rojo8399";
	}

	@Override
	public String getVersion() {
		return "1.0";
	}

	@Override
	public Text onPlaceholderRequest(Player p, Optional<String> identifier, Function<String, Text> parser) {
		Config config = PlaceholderAPIPlugin.getInstance().getConfig();
		if (config.expansions.player) {
			if (!identifier.isPresent()) {
				return parser.apply(p.getName());
			}
			switch (identifier.get()) {
			case "prefix":
			case "suffix":
				return parser.apply(p.getOption(identifier.get()).orElse(""));
			case "name":
				return parser.apply(p.getName());
			case "displayname":
				return p.getDisplayNameData().displayName().get();
			default:
				return null;
			}
		} else {
			return null;
		}
	}

}