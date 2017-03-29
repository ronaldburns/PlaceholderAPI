package me.rojo8399.placeholderapi.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import me.rojo8399.placeholderapi.PlaceholderAPIPlugin;
import me.rojo8399.placeholderapi.PlaceholderService;
import me.rojo8399.placeholderapi.expansions.Expansion;
import me.rojo8399.placeholderapi.utils.TextUtils;

public class InfoCommand implements CommandExecutor {

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		String placeholder = (String) args.getOne("placeholder")
				.orElseThrow(() -> new CommandException(Text.of(TextColors.RED, "You must specify a placeholder!")));
		PlaceholderService service = PlaceholderAPIPlugin.getInstance().getGame().getServiceManager()
				.provideUnchecked(PlaceholderService.class);
		Expansion e = service.getExpansion(placeholder)
				.orElseThrow(() -> new CommandException(Text.of(TextColors.RED, "That is not a valid placeholder!")));
		Text barrier = TextUtils.repeat(Text.of(TextColors.GOLD, "="), 35);
		src.sendMessage(barrier);
		src.sendMessage(formatExpansion(e, src));
		src.sendMessage(barrier);
		return CommandResult.success();
	}

	private static final Pattern OPT = Pattern.compile("[\\<\\[\\(\\{]([^ \\<\\>\\[\\]\\{\\}\\(\\)]+)[\\>\\}\\]\\)]",
			Pattern.CASE_INSENSITIVE);

	private static Text formatExpansion(Expansion e, CommandSource src) {
		final String name = e.getIdentifier();
		String version = e.getVersion();
		String author = e.getAuthor();
		List<String> tokens = e.getSupportedTokens();
		if (tokens == null) {
			tokens = new ArrayList<>();
		}
		List<Text> supportedTokens = tokens.stream().limit(20).map(s -> s == null || s.isEmpty() ? null : s).distinct()
				.sorted((s1, s2) -> s1 == null ? -1 : (s2 == null ? 1 : s1.compareTo(s2))).map(s -> {
					if (s == null) {
						return token(name, src);
					}
					String s2 = name.concat("_" + s);
					if (OPT.matcher(s2).find()) {
						return token(s2, src, true);
					}
					return token(s2, src);
				}).collect(Collectors.toList());
		Text url = e.getURL() == null ? Text.EMPTY
				: Text.of(Text.NEW_LINE, TextColors.BLUE, TextActions.openUrl(e.getURL()), e.getURL().toString());
		Text desc = e.getDescription() == null ? Text.EMPTY
				: Text.of(Text.NEW_LINE, TextColors.AQUA, e.getDescription());
		Text support = supportedTokens.isEmpty() ? Text.EMPTY
				: Text.of(Text.NEW_LINE, TextColors.GOLD, "Supported placeholders: ", Text.NEW_LINE,
						Text.joinWith(Text.of(", "), supportedTokens));
		return Text.of(TextColors.AQUA, name, TextColors.GREEN, " " + version, TextColors.GRAY, " by ", TextColors.GOLD,
				author, TextColors.GRAY, ".", desc, url, support);
	}

	private static Text token(String token, CommandSource src, boolean opt) {
		if (!opt) {
			return token(token, src);
		}
		if (!(src instanceof Player)) {
			return Text.of(TextColors.GREEN, "%" + token + "%");
		}
		String p = src.getName();
		return Text.of(TextColors.GREEN,
				TextActions.showText(Text.of(TextColors.AQUA, "Click to parse this placeholder for you!")),
				TextActions.suggestCommand("/papi p " + p + " %" + token + "%"), "%" + token + "%");
	}

	private static Text token(String token, CommandSource src) {
		if (!(src instanceof Player)) {
			return Text.of(TextColors.GREEN, "%" + token + "%");
		}
		String p = src.getName();
		return Text.of(TextColors.GREEN,
				TextActions.showText(Text.of(TextColors.AQUA, "Click to parse this placeholder for you!")),
				TextActions.runCommand("/papi p " + p + " %" + token + "%"), "%" + token + "%");
	}

}
