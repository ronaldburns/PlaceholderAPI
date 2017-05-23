/*
 The MIT License (MIT)

 Copyright (c) 2017 Wundero

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */
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
import me.rojo8399.placeholderapi.configs.Messages;
import me.rojo8399.placeholderapi.expansions.Expansion;
import me.rojo8399.placeholderapi.utils.TextUtils;

public class InfoCommand implements CommandExecutor {

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		String placeholder = (String) args.getOne("placeholder")
				.orElseThrow(() -> new CommandException(Messages.get().placeholder.mustSpecify.t()));
		PlaceholderService service = PlaceholderAPIPlugin.getInstance().getGame().getServiceManager()
				.provideUnchecked(PlaceholderService.class);
		Expansion e = service.getExpansion(placeholder)
				.orElseThrow(() -> new CommandException(Messages.get().placeholder.invalidPlaceholder.t()));
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
		List<Text> supportedTokens = tokens.stream().map(s -> s == null || s.isEmpty() ? null : s).distinct()
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
		boolean seeall = false;
		List<Text> t2 = new ArrayList<Text>(supportedTokens);
		if (supportedTokens.size() > 20) {
			supportedTokens = supportedTokens.subList(0, 20);
			seeall = true;
		}
		Text url = e.getURL() == null ? Text.EMPTY
				: Text.of(Text.NEW_LINE, TextColors.BLUE, TextActions.openUrl(e.getURL()), e.getURL().toString());
		Text desc = e.getDescription() == null ? Text.EMPTY
				: Text.of(Text.NEW_LINE, TextColors.AQUA, e.getDescription());
		Text reload = Text.of(Text.NEW_LINE, Messages.get().placeholder.clickReload.t(), " ",
				reload(e.getIdentifier()));
		Text support = supportedTokens.isEmpty() ? Text.EMPTY
				: Text.of(Text.NEW_LINE, Messages.get().placeholder.supportedPlaceholders.t(),
						seeall ? (seeall(t2)) : "", Text.NEW_LINE, Text.joinWith(Text.of(", "), supportedTokens));
		return Text.of(TextColors.AQUA, name, TextColors.GREEN, " " + version, TextColors.GRAY, " ",
				Messages.get().misc.by.t(), " ", TextColors.GOLD, author, TextColors.GRAY, ".", reload, desc, url,
				support);
	}

	private static Text seeall(List<Text> tokens) {
		final Text t = Text.joinWith(Text.of(", "), tokens);
		return Text.of("    ", TextActions.showText(Text.of(Messages.get().placeholder.allPlaceholdersHover.t())),
				TextActions.executeCallback(s -> {
					s.sendMessage(Messages.get().placeholder.allSupportedPlaceholders.t());
					s.sendMessage(t);
				}), Messages.get().placeholder.allPlaceholdersButton.t());
	}

	private static Text reload(String token) {
		return Messages.get().placeholder.reloadButton.t().toBuilder()
				.onHover(TextActions.showText(Messages.get().placeholder.reloadButtonHover.t()))
				.onClick(TextActions.runCommand("/papi r " + token)).build();
	}

	private static Text token(String token, CommandSource src, boolean opt) {
		if (!opt) {
			return token(token, src);
		}
		if (!(src instanceof Player)) {
			return Text.of(TextColors.GREEN, "%" + token + "%");
		}
		String p = src.getName();
		return Text.of(TextColors.GREEN, TextActions.showText(Messages.get().placeholder.parseButtonHover.t()),
				TextActions.suggestCommand("/papi p " + p + " %" + token + "%"), "%" + token + "%");
	}

	private static Text token(String token, CommandSource src) {
		if (!(src instanceof Player)) {
			return Text.of(TextColors.GREEN, "%" + token + "%");
		}
		String p = src.getName();
		return Text.of(TextColors.GREEN, TextActions.showText(Messages.get().placeholder.parseButtonHover.t()),
				TextActions.runCommand("/papi p " + p + " %" + token + "%"), "%" + token + "%");
	}

}
