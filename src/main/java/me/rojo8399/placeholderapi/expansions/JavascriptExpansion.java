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

import java.util.Optional;
import java.util.function.Function;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import me.rojo8399.placeholderapi.PlaceholderAPIPlugin;
import me.rojo8399.placeholderapi.configs.JavascriptManager;

/**
 * @author Wundero
 *
 */
public class JavascriptExpansion implements Expansion {

	private ScriptEngine engine;
	private JavascriptManager manager;

	public JavascriptExpansion(JavascriptManager manager) {
		this.manager = manager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see me.rojo8399.placeholderapi.expansions.Expansion#canRegister()
	 */
	@Override
	public boolean canRegister() {
		return true;
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
		return "1.0";
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
	public Text onPlaceholderRequest(Player player, Optional<String> token, Function<String, Text> textParser) {
		if (!token.isPresent()) {
			return null;
		}
		if (engine == null) {
			engine = new ScriptEngineManager(null).getEngineByName("Nashorn");
			engine.put("server", PlaceholderAPIPlugin.getInstance().getGame().getServer());
		}
		engine.put("player", player);
		engine.put("parser", textParser);
		Object o = manager.eval(engine, token.get());
		if (o == null) {
			return null;
		}
		if (o instanceof Text) {
			return (Text) o;
		} else {
			return textParser.apply(o.toString());
		}
	}

}
