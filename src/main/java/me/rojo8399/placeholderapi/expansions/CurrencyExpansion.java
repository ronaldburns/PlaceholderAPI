package me.rojo8399.placeholderapi.expansions;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.text.Text;

import me.rojo8399.placeholderapi.PlaceholderAPIPlugin;
import me.rojo8399.placeholderapi.configs.Messages;

public class CurrencyExpansion implements Expansion {

	private EconomyService service;
	private Map<String, Currency> currencies = new HashMap<String, Currency>();
	private Currency def;

	public CurrencyExpansion(EconomyService service) {
		this.service = service;
		this.def = service.getDefaultCurrency();
		service.getCurrencies().forEach(this::putCur);
	}

	private void putCur(Currency c) {
		currencies.put(c.getName(), c);
	}

	@Override
	public boolean canRegister() {
		return service != null && PlaceholderAPIPlugin.getInstance().getConfig().expansions.economy;
	}

	@Override
	public String getIdentifier() {
		return "economy";
	}

	@Override
	public String getAuthor() {
		return "Wundero";
	}

	@Override
	public String getDescription() {
		return Messages.get().placeholder.curdesc.value;
	}

	@Override
	public String getVersion() {
		return "1.0";
	}

	@Override
	public List<String> getSupportedTokens() {
		return Arrays.asList(null, "balance", "balance_[currency]", "display", "display_[currency]", "symbol",
				"symbol_[currency]", "balformat", "balformat_[currency]", "pluraldisplay", "pluraldisplay_[currency]");
	}

	@Override
	public Object onValueRequest(Player player, Optional<String> token) {
		if (!token.isPresent()) {
			Text amt = def.format(BigDecimal.valueOf(1234.56));
			Text v = Text.of(def.getName() + " (" + def.getId() + ") - ");
			return v.concat(amt);
		}
		String t = token.get();
		Currency toUse = def;
		if (t.contains("_")) {
			String[] a = t.split("_");
			t = a[0];
			String c = a[1];
			if (currencies.containsKey(c)) {
				toUse = currencies.get(def);
			}
		}
		// Don't handle nonexistent accounts here, instead throw error
		UniqueAccount acc = service.getOrCreateAccount(player.getUniqueId()).get();
		switch (t) {
		case "balance":
			return acc.getBalance(toUse);
		}
		return onPlaceholderRequest(player, token);
	}

	@Override
	public Text onPlaceholderRequest(Player player, Optional<String> token) {
		if (!token.isPresent()) {
			Text amt = def.format(BigDecimal.valueOf(1234.56));
			Text v = Text.of(def.getName() + " (" + def.getId() + ") - ");
			return v.concat(amt);
		}
		String t = token.get();
		Currency toUse = def;
		if (t.contains("_")) {
			String[] a = t.split("_");
			t = a[0];
			String c = a[1];
			if (currencies.containsKey(c)) {
				toUse = currencies.get(def);
			}
		}
		// Don't handle nonexistent accounts here, instead throw error
		UniqueAccount acc = service.getOrCreateAccount(player.getUniqueId()).get();
		switch (t) {
		case "balance":
			return Text.of(acc.getBalance(toUse).toPlainString());
		case "balformat":
			return toUse.format(acc.getBalance(toUse));
		case "display":
			return toUse.getDisplayName();
		case "pluraldisplay":
			return toUse.getPluralDisplayName();
		case "symbol":
			return toUse.getSymbol();
		}
		return null;
	}

}
