package me.rojo8399.placeholderapi.expansions;

import java.util.Optional;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.serializer.TextSerializers;

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
	public String onPlaceholderRequest(Player p, Optional<String> identifier) {
		Config config = PlaceholderAPIPlugin.getInstance().getConfig();
		if (config.expansions.player) {
			if (!identifier.isPresent()) {
				return p.getName();
			}
			switch (identifier.get()) {
			case "prefix":
			case "suffix":
				return p.getOption(identifier.get()).orElse("");
			case "name":
				return p.getName();
			case "displayname":
				return TextSerializers.FORMATTING_CODE.serialize(p.getDisplayNameData().displayName().get());
			default:
				return null;
			}
		} else {
			return null;
		}
	}

}
