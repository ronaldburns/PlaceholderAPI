package me.rojo8399.placeholderapi.expansions;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import me.rojo8399.placeholderapi.PlaceholderAPIPlugin;

public class PlayerExpansion implements Expansion {

    @Override
    public boolean canRegister() {
	return PlaceholderAPIPlugin.getInstance().getConfig().expansions.player;
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
    public Text onPlaceholderRequest(Player p, Optional<String> identifier) {
	if (!identifier.isPresent()) {
	    return Text.of(p.getName());
	}
	String token = identifier.get();
	if (token.startsWith("option_")) {
	    String op = token.substring("option_".length());
	    return TextSerializers.FORMATTING_CODE.deserialize(p.getOption(op).orElse(""));
	}
	if (token.startsWith("perm") && token.contains("_")) {
	    String op = token.substring(token.indexOf("_"));
	    return Text.of(p.getPermissionValue(p.getActiveContexts(), op).toString());
	}
	switch (token) {
	case "prefix":
	case "suffix":
	    return TextSerializers.FORMATTING_CODE.deserialize(p.getOption(identifier.get()).orElse(""));
	case "name":
	    return Text.of(p.getName());
	case "displayname":
	    return p.getDisplayNameData().displayName().get();
	case "world":
	    return Text.of(p.getWorld().getName());
	default:
	    return null;
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see me.rojo8399.placeholderapi.expansions.Expansion#getSupportedTokens()
     */
    @Override
    public List<String> getSupportedTokens() {
	return Arrays.asList(null, "prefix", "suffix", "name", "displayname", "world", "option_[option]",
		"permission_[permission]");
    }

}