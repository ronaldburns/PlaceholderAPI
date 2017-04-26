package me.rojo8399.placeholderapi.commands;

import java.util.regex.Pattern;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import me.rojo8399.placeholderapi.PlaceholderAPIPlugin;
import me.rojo8399.placeholderapi.PlaceholderService;
import me.rojo8399.placeholderapi.configs.Messages;

public class ParseCommand implements CommandExecutor {

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

		Player p = args.<Player>getOne("player").get();
		String placeholder = args.<String>getOne(Text.of("placeholders")).get();

		PlaceholderService service = PlaceholderAPIPlugin.getInstance().getGame().getServiceManager()
				.provideUnchecked(PlaceholderService.class);
		if (service == null) {
			throw new CommandException(Messages.t(Messages.get().plugin.serviceUnavailable));
		}
		Text t = service.replacePlaceholders(p, placeholder,
				Pattern.compile("[%{]([^{%} ]+)[%}]", Pattern.CASE_INSENSITIVE));
		if (t == null) {
			t = Text.EMPTY;
		}
		if (!t.isEmpty()) {
			src.sendMessage(t);
		} else {
			src.sendMessage(Messages.t(Messages.get().misc.noValue));
		}
		return CommandResult.success();

	}

}
