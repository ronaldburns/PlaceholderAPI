package me.rojo8399.placeholderapi.commands;

import java.util.List;
import java.util.stream.Collectors;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import me.rojo8399.placeholderapi.PlaceholderAPIPlugin;
import me.rojo8399.placeholderapi.PlaceholderService;
import me.rojo8399.placeholderapi.configs.Messages;

public class ListCommand implements CommandExecutor {

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		PlaceholderService service = PlaceholderAPIPlugin.getInstance().getGame().getServiceManager()
				.provide(PlaceholderService.class)
				.orElseThrow(() -> new CommandException(Messages.get().plugin.serviceUnavailable.t()));
		List<Text> l = service.getExpansions().stream()
				.map(e -> Text.of(TextColors.GOLD, TextActions.runCommand("/papi i " + e.getIdentifier()),
						TextActions.showText(Messages.get().placeholder.infoButtonHover.t()), e.getIdentifier()))
				.collect(Collectors.toList());
		src.sendMessage(Messages.get().placeholder.availablePlaceholders.t());
		src.sendMessage(Text.joinWith(Text.of(", "), l));
		return CommandResult.success();
	}

}
