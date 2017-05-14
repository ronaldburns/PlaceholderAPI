/*
 The MIT License (MIT)

 Copyright (c) 2016 Wundero

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
import java.util.Optional;
import java.util.regex.Pattern;

import javax.annotation.Nullable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import me.rojo8399.placeholderapi.PlaceholderAPIPlugin;
import me.rojo8399.placeholderapi.PlaceholderService;
import me.rojo8399.placeholderapi.configs.JavascriptManager;
import me.rojo8399.placeholderapi.configs.Messages;

/**
 * @author Wundero
 *
 */
public class JavascriptExpansion implements ReloadableExpansion {

	private ScriptEngine engine;
	private JavascriptManager manager;
	private PlaceholderService s;
	private Server server;

	@Override
	public boolean reload() {
		try {
			manager.reloadScripts();
		} catch (Exception e) {
			return false;
		}
		server = PlaceholderAPIPlugin.getInstance().getGame().getServer();
		s = Sponge.getServiceManager().provideUnchecked(PlaceholderService.class);
		return true;
	}

	public JavascriptExpansion(JavascriptManager manager) {
		this.manager = manager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see me.rojo8399.placeholderapi.expansions.Expansion#getDescription()
	 */
	@Override
	public String getDescription() {
		return Messages.get().placeholder.jsdesc.value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see me.rojo8399.placeholderapi.expansions.Expansion#canRegister()
	 */
	@Override
	public boolean canRegister() {
		server = PlaceholderAPIPlugin.getInstance().getGame().getServer();
		s = Sponge.getServiceManager().provideUnchecked(PlaceholderService.class);
		return PlaceholderAPIPlugin.getInstance().getConfig().expansions.javascript && s != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see me.rojo8399.placeholderapi.expansions.Expansion#getIdentifier()
	 */
	@Override
	public String getIdentifier() {
		return "javascript";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see me.rojo8399.placeholderapi.expansions.Expansion#getAuthor()
	 */
	@Override
	public String getAuthor() {
		return "Wundero";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see me.rojo8399.placeholderapi.expansions.Expansion#getVersion()
	 */
	@Override
	public String getVersion() {
		return "1.1";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * me.rojo8399.placeholderapi.expansions.Expansion#onPlaceholderRequest(org.
	 * spongepowered.api.entity.living.player.Player, java.util.Optional,
	 * java.util.function.Function)
	 */
	@Override
	public Text onPlaceholderRequest(Player player, Optional<String> token) {
		return onValueRequest(player, token, Text.class).orElse(Text.EMPTY);
	}

	@Override
	public @Nullable Object onValueRequest(Player player, Optional<String> token) {
		if (!token.isPresent()) {
			// No script
			return null;
		}
		if (engine == null) {
			// Lazily instantiate engine
			engine = new ScriptEngineManager(null).getEngineByName("Nashorn");
			// Insert default server variable - constant
			engine.put("server",
					server == null ? (server = PlaceholderAPIPlugin.getInstance().getGame().getServer()) : server);
		}
		// Insert player + parser objects, which change every time
		engine.put("player", player);
		// Allow retrieving values for registered expansions except for
		// javascript
		// scripts - will parse like a normal string (e.g. "%player_name%")
		Service service = new Service(s, player);
		engine.put("service", service);
		// Evaluate the script
		return manager.eval(engine, token.get());
	}

	public static class Service {
		private PlaceholderService s;
		private Player p;

		public Service(PlaceholderService s, Player p) {
			this.s = s;
			this.p = p;
		}

		public String value(String placeholder) {
			return value(placeholder, "[%]([^% ]+)[%]");
		}

		public String value(String placeholder, String pattern) {
			if (placeholder.toLowerCase().contains("javascript")) {
				return placeholder;
			}
			return s.replacePlaceholdersLegacy(p, placeholder, Pattern.compile(pattern, Pattern.CASE_INSENSITIVE));
		}

		public Text get(String placeholders) {
			return get(placeholders, "[%]([^% ]+)[%]");
		}

		public Text get(String placeholders, String pattern) {
			if (placeholders.toLowerCase().contains("javascript")) {
				return Text.of(placeholders);
			}
			return s.replacePlaceholders(p, placeholders, Pattern.compile(pattern, Pattern.CASE_INSENSITIVE));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see me.rojo8399.placeholderapi.expansions.Expansion#getSupportedTokens()
	 */
	@Override
	public List<String> getSupportedTokens() {
		return manager.getScriptNames();
	}

}
