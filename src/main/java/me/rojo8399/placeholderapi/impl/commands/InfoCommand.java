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
package me.rojo8399.placeholderapi.impl.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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

import me.rojo8399.placeholderapi.PlaceholderService;
import me.rojo8399.placeholderapi.impl.PlaceholderAPIPlugin;
import me.rojo8399.placeholderapi.impl.configs.Messages;
import me.rojo8399.placeholderapi.impl.placeholder.Expansion;
import me.rojo8399.placeholderapi.impl.placeholder.Store;
import me.rojo8399.placeholderapi.impl.utils.TextUtils;

public class InfoCommand implements CommandExecutor {

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		String placeholder = (String) args.getOne("placeholder")
				.orElseThrow(() -> new CommandException(Messages.get().placeholder.mustSpecify.t()));
		PlaceholderService service = PlaceholderAPIPlugin.getInstance().getGame().getServiceManager()
				.provideUnchecked(PlaceholderService.class);
		if (!service.isRegistered(placeholder)) {
			throw new CommandException(Messages.get().placeholder.invalidPlaceholder.t());
		}
		Text barrier = TextUtils.repeat(Text.of(TextColors.GOLD, "="), 35);
		src.sendMessage(barrier);
		src.sendMessage(formatExpansion(placeholder, src));
		src.sendMessage(barrier);
		return CommandResult.success();
	}

	private static final Pattern OPT = Pattern.compile("[\\<\\[\\(\\{]([^ \\<\\>\\[\\]\\{\\}\\(\\)]+)[\\>\\}\\]\\)]",
			Pattern.CASE_INSENSITIVE);

	private static Text formatExpansion(String e, CommandSource src) {
		List<Expansion<?, ?, ?>> conts = Arrays.asList(Store.get().get(e, true), Store.get().get(e, false)).stream()
				.filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
		Expansion<?, ?, ?> norm;
		try {
			norm = conts.get(0);
		} catch (Exception ex) {
			return Text.of(
					TextColors.RED + "Placeholder was not registered correctly! Please check the logs for details.");
		}
		Expansion<?, ?, ?> rel = null;
		if (conts.size() > 1) {
			rel = conts.get(1);
		}
		Text out = format(norm, src);
		if (rel != null) {
			out = out.concat(Text.NEW_LINE);
			out = out.concat(format(rel, src));
		}
		return out;
	}

	private static Text format(Expansion<?, ?, ?> e, CommandSource src) {
		final String name = e.id();
		String version = e.version();
		String author = e.author();
		final boolean rel = e.relational();
		List<String> tokens = e.tokens();
		if (tokens == null) {
			tokens = new ArrayList<>();
		}
		List<Text> supportedTokens = tokens.stream().map(s -> s == null || s.isEmpty() ? null : s)
				.map(s -> s == null ? null : s.toLowerCase().trim()).distinct()
				.sorted((s1, s2) -> s1 == null ? -1 : (s2 == null ? 1 : s1.compareTo(s2))).map(s -> {
					if (s == null) {
						return token(name, src, rel);
					}
					String s2 = name.concat("_" + s);
					if (OPT.matcher(s2).find()) {
						return token(s2, src, rel, true);
					}
					return token(s2, src, rel);
				}).collect(Collectors.toList());
		boolean seeall = false;
		List<Text> t2 = new ArrayList<Text>(supportedTokens);
		if (supportedTokens.size() > 20) {
			supportedTokens = supportedTokens.subList(0, 20);
			seeall = true;
		}
		Text url = e.url() == null ? Text.EMPTY
				: Text.of(Text.NEW_LINE, TextColors.BLUE, TextActions.openUrl(e.url()), e.url().toString());
		String d = e.description();
		Text desc = d == null || d.isEmpty() ? Text.EMPTY : Text.of(Text.NEW_LINE, TextColors.AQUA, e.description());
		Text reload = Text.of(Text.NEW_LINE, Messages.get().placeholder.clickReload.t(), " ", reload(e.id()));
		Text support = supportedTokens.isEmpty() ? Text.EMPTY
				: Text.of(Text.NEW_LINE, Messages.get().placeholder.supportedPlaceholders.t(),
						seeall ? (seeall(t2, false)) : "", Text.NEW_LINE,
						Text.joinWith(Text.of(", "), supportedTokens));
		return Text.of(TextColors.AQUA, (rel ? "rel_" : "") + name, TextColors.GREEN, " " + version, TextColors.GRAY,
				" ", Messages.get().misc.by.t(), " ", TextColors.GOLD, author, TextColors.GRAY, ".", reload, desc, url,
				support);
	}

	private static Text seeall(List<Text> tokens, boolean relational) {
		final Text t = Text.joinWith(Text.of(", "), tokens);
		Text h = relational ? Messages.get().placeholder.allPlaceholdersHoverRelational.t()
				: Messages.get().placeholder.allPlaceholdersHover.t();
		Text a = relational ? Messages.get().placeholder.allSupportedPlaceholdersRelational.t()
				: Messages.get().placeholder.allSupportedPlaceholders.t();
		return Text.of("    ", TextActions.showText(Text.of(h)), TextActions.executeCallback(s -> {
			s.sendMessage(a);
			s.sendMessage(t);
		}), Messages.get().placeholder.allPlaceholdersButton.t());
	}

	private static Text reload(String token) {
		return Messages.get().placeholder.reloadButton.t().toBuilder()
				.onHover(TextActions.showText(Messages.get().placeholder.reloadButtonHover.t()))
				.onClick(TextActions.runCommand("/papi r " + token)).build();
	}

	private static Text token(String token, CommandSource src, boolean relational, boolean opt) {
		if (!opt) {
			return token(token, src, relational);
		}
		if (!(src instanceof Player)) {
			return Text.of(TextColors.GREEN, token);
		}
		String p = src.getName();
		return Text.of(TextColors.GREEN, TextActions.showText(Messages.get().placeholder.parseButtonHover.t()),
				TextActions.suggestCommand("/papi p " + p + " %" + (relational ? "rel_" : "") + token + "%"),
				(relational ? "rel_" : "") + token);
	}

	private static Text token(String token, CommandSource src, boolean relational) {
		if (!(src instanceof Player)) {
			return Text.of(TextColors.GREEN, token);
		}
		String p = src.getName();
		return Text.of(TextColors.GREEN, TextActions.showText(Messages.get().placeholder.parseButtonHover.t()),
				TextActions.runCommand("/papi p " + p + " %" + (relational ? "rel_" : "") + token + "%"),
				(relational ? "rel_" : "") + token);
	}

}
