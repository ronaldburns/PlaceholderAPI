package me.rojo8399.placeholderapi.commands;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import me.rojo8399.placeholderapi.PlaceholderAPIPlugin;
import me.rojo8399.placeholderapi.PlaceholderService;

public class ParseCommand implements CommandExecutor {

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

		Player p = args.<Player>getOne("player").get();
		String[] placeholders = args.<String>getOne(Text.of("placeholders")).get().split(" ");

		PlaceholderService service = PlaceholderAPIPlugin.getInstance().getGame().getServiceManager()
				.provideUnchecked(PlaceholderService.class);
		if (service == null) {
			throw new CommandException(Text.of(TextColors.RED, "ERROR: Placeholders not registered!"));
		}
		for (String placeholder : placeholders) {
			String t = service.replacePlaceholders(p, placeholder);
			if (t == null) {
				t = "";
			}
			if (!t.replace(" ", "").equals("")) {
				src.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(t));
			} else {
				src.sendMessage(Text.of(TextColors.RED, "No value present!"));
			}
		}
		return CommandResult.success();

	}

}
