package me.rojo8399.placeholderapi.commands;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;

import me.rojo8399.placeholderapi.PlaceholderAPIPlugin;
import me.rojo8399.placeholderapi.PlaceholderServiceImpl;
import me.rojo8399.placeholderapi.configs.Messages;
import me.rojo8399.placeholderapi.expansions.Expansion;

public class RefreshCommand implements CommandExecutor {

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		String placeholderid = args.<String>getOne("id").orElse(null);
		if (placeholderid == null) {
			try {
				PlaceholderAPIPlugin.getInstance().reloadConfig();
			} catch (Exception e) {
				throw new CommandException(Messages.t(Messages.get().plugin.reloadFailed));
			}
			src.sendMessage(Messages.t(Messages.get().plugin.reloadSuccess));
			return CommandResult.success();
		}
		Expansion e = PlaceholderServiceImpl.get().getExpansion(placeholderid).orElse(null);
		if (e == null) {
			throw new CommandException(Messages.t(Messages.get().placeholder.invalidPlaceholder));
		}
		boolean s = PlaceholderServiceImpl.get().refreshPlaceholder(placeholderid);
		if (!s) {
			src.sendMessage(Messages.t(Messages.get().placeholder.reloadFailed));
		} else {
			src.sendMessage(Messages.t(Messages.get().placeholder.reloadSuccess));
		}
		return s ? CommandResult.success() : CommandResult.successCount(0);
	}

}
