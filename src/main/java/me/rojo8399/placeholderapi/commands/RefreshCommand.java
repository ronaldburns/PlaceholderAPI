package me.rojo8399.placeholderapi.commands;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;

import me.rojo8399.placeholderapi.PlaceholderAPIPlugin;
import me.rojo8399.placeholderapi.PlaceholderServiceImpl;
import me.rojo8399.placeholderapi.expansions.Expansion;

public class RefreshCommand implements CommandExecutor {

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		String placeholderid = args.<String>getOne("id").orElse(null);
		if (placeholderid == null) {
			try {
				PlaceholderAPIPlugin.getInstance().reloadConfig();
			} catch (Exception e) {
				throw new CommandException(Text.of(TextColors.RED, "PlaceholderAPI failed to reload!"));
			}
			src.sendMessage(Text.of(TextColors.GREEN, "PlaceholderAPI reloaded successfully!"));
			return CommandResult.success();
		}
		Expansion e = PlaceholderServiceImpl.get().getExpansion(placeholderid).orElse(null);
		if (e == null) {
			throw new CommandException(Text.of(TextColors.RED, "That is not a valid placeholder!"));
		}
		boolean s = PlaceholderServiceImpl.get().refreshPlaceholder(placeholderid);
		TextColor c = s ? TextColors.GREEN : TextColors.RED;
		src.sendMessage(Text.of(c, "The placeholder was " + (s ? "" : "un") + "successfully reloaded."));
		return s ? CommandResult.success() : CommandResult.successCount(0);
	}

}
