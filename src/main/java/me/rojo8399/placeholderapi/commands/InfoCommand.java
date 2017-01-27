package me.rojo8399.placeholderapi.commands;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import me.rojo8399.placeholderapi.PlaceholderAPIPlugin;
import me.rojo8399.placeholderapi.PlaceholderService;
import me.rojo8399.placeholderapi.expansions.Expansion;

public class InfoCommand implements CommandExecutor {

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		String placeholder = (String) args.getOne("placeholder")
				.orElseThrow(() -> new CommandException(Text.of(TextColors.RED, "You must specify a placeholder!")));
		PlaceholderService service = PlaceholderAPIPlugin.getInstance().getGame().getServiceManager()
				.provideUnchecked(PlaceholderService.class);
		Expansion e = service.getExpansion(placeholder)
				.orElseThrow(() -> new CommandException(Text.of(TextColors.RED, "That is not a valid placeholder!")));
		src.sendMessage(formatExpansion(e));
		return CommandResult.success();
	}

	private static Text formatExpansion(Expansion e) {
		String name = e.getIdentifier();
		String version = e.getVersion();
		String author = e.getAuthor();
		return Text.of(TextColors.AQUA, name, TextColors.GREEN, " " + version, TextColors.GRAY, " by ", TextColors.GOLD,
				author, TextColors.GRAY, ".");
	}

}
