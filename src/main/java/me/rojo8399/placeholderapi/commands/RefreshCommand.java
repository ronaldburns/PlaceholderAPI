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
				int current = PlaceholderServiceImpl.get().getExpansions().size();
				int success = PlaceholderServiceImpl.get().refreshAll();
				int failed = current - success;
				src.sendMessage(Messages.get().plugin.reloadSuccess.t());
				src.sendMessage(Messages.get().plugin.reloadCount.t(success, failed));
			} catch (Exception e) {
				throw new CommandException(Messages.get().plugin.reloadFailed.t());
			}
			return CommandResult.success();
		}
		Expansion e = PlaceholderServiceImpl.get().getExpansion(placeholderid).orElse(null);
		if (e == null) {
			throw new CommandException(Messages.get().placeholder.invalidPlaceholder.t());
		}
		boolean s = PlaceholderServiceImpl.get().refreshPlaceholder(placeholderid);
		if (!s) {
			src.sendMessage(Messages.get().placeholder.reloadFailed.t());
		} else {
			src.sendMessage(Messages.get().placeholder.reloadSuccess.t());
		}
		return s ? CommandResult.success() : CommandResult.successCount(0);
	}

}
