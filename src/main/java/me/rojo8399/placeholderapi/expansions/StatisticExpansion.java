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
package me.rojo8399.placeholderapi.expansions;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import com.google.common.reflect.TypeToken;

import me.rojo8399.placeholderapi.configs.Messages;
import ninja.leaping.configurate.objectmapping.Setting;

public class StatisticExpansion implements ConfigurableExpansion {

	private static final TypeToken<StatisticExpansion> type = TypeToken.of(StatisticExpansion.class);

	@Setting
	public boolean enabled = true;

	@Override
	public boolean canRegister() {
		return enabled;
	}

	@Override
	public String getIdentifier() {
		return "statistic";
	}

	@Override
	public String getAuthor() {
		return "Wundero";
	}

	@Override
	public String getDescription() {
		return Messages.get().placeholder.statdesc.value;
	}

	@Override
	public String getVersion() {
		return "1.0";
	}

	@Override
	public List<String> getSupportedTokens() {
		return null;
	}

	@Override
	public Object onValueRequest(Player player, Optional<String> token) {
		if (!token.isPresent()) {
			return null;
		}
		return player.getOrNull(Keys.STATISTICS).entrySet().stream()
				.filter(e -> e.getKey().getId().replace("._", ".").toLowerCase().startsWith(token.get().toLowerCase()))
				.map(Map.Entry::getValue).reduce(0l, (a, b) -> a + b);
	}

	@Override
	public Text onPlaceholderRequest(Player player, Optional<String> token) {
		if (!token.isPresent()) {
			return null;
		}
		return Text.of(onValueRequest(player, token));
	}

	@Override
	public TypeToken<? extends ConfigurableExpansion> getToken() {
		return type;
	}

}
