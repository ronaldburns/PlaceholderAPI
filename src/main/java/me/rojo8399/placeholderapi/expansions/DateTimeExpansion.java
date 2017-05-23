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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import com.google.common.reflect.TypeToken;

import me.rojo8399.placeholderapi.configs.Messages;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class DateTimeExpansion implements ConfigurableExpansion {

	private static TypeToken<DateTimeExpansion> type = TypeToken.of(DateTimeExpansion.class);

	@Setting
	public boolean enabled;
	@Setting
	public String format = "uuuu LLL dd HH:mm:ss";
	private DateTimeFormatter f;

	@Override
	public boolean canRegister() {
		f = DateTimeFormatter.ofPattern(format);
		return enabled && f != null;
	}

	@Override
	public String getIdentifier() {
		return "time";
	}

	@Override
	public String getDescription() {
		return Messages.get().placeholder.timedesc.value;
	}

	@Override
	public String getAuthor() {
		return "Wundero";
	}

	@Override
	public String getVersion() {
		return "1.0";
	}

	@Override
	public List<String> getSupportedTokens() {
		return Arrays.asList((String) null);
	}

	@Override
	public Object onValueRequest(Player player, Optional<String> token) {
		return LocalDateTime.now();
	}

	@Override
	public Text onPlaceholderRequest(Player player, Optional<String> token) {
		return TextSerializers.FORMATTING_CODE.deserialize(LocalDateTime.now().format(f));
	}

	@Override
	public TypeToken<DateTimeExpansion> getToken() {
		return type;
	}

}
