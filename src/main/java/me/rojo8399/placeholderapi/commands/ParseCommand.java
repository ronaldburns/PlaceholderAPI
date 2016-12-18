package me.rojo8399.placeholderapi.commands;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import me.rojo8399.placeholderapi.PlaceholderAPI;

public class ParseCommand implements CommandExecutor {

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

		Player p = args.<Player>getOne("player").get();
		String[] placeholders = args.<String>getOne(Text.of("placeholders")).get().split(" ");

		for (String placeholder : placeholders) {
			src.sendMessage(Text.of(PlaceholderAPI.setPlaceholders(p, placeholder)));
		}

		return CommandResult.success();

	}

}
