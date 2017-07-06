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
package me.rojo8399.placeholderapi.placeholder;

import java.net.URL;
import java.util.Optional;

import org.spongepowered.api.Sponge;

import me.rojo8399.placeholderapi.placeholder.gen.InternalPlaceholder;
import me.rojo8399.placeholderapi.placeholder.gen.PlaceholderContainer;
import me.rojo8399.placeholderapi.placeholder.gen.PlaceholderData;

/**
 * @author Wundero
 *
 */
public class FullContainer {

	private final InternalPlaceholder placeholder;
	private final Object holder;
	private final String id;
	private final String author;
	private final String version;
	private final String desc;
	private final String[] tokens;
	private final String url;
	private final boolean relational;
	private boolean enabled = true;

	public static FullContainer from(Placeholder p, InternalPlaceholder pl, boolean r, Object o) {
		return new FullContainer(pl, p.id().toLowerCase().trim(), p.author(), p.version(), p.desc(), p.tokens(),
				p.url(), r, o);
	}

	public FullContainer(InternalPlaceholder placeholder, String id, String author, String version, String desc,
			String[] tokens, String url, boolean relational, Object plugin) {
		this.placeholder = placeholder;
		this.id = id.toLowerCase().trim().replaceFirst("rel\\_", "");
		this.author = author;
		this.version = version;
		this.desc = desc;
		this.tokens = tokens;
		this.url = url;
		this.relational = relational;
		this.holder = plugin;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void toggleEnabled() {
		setEnabled(!this.enabled);
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void disable() {
		this.enabled = false;
	}

	public void enable() {
		this.enabled = true;
	}

	public void unregisterListeners() {
		Sponge.getEventManager().unregisterListeners(getHandle());
	}

	public Object plugin() {
		return holder;
	}

	public Object replace(Object src, Object obs, Optional<String> token) throws Exception {
		if (!enabled) {
			return null;
		}
		return placeholder.handle(new PlaceholderData(src, obs, token));
	}

	Object getHandle() {
		if (placeholder instanceof PlaceholderContainer) {
			return ((PlaceholderContainer) placeholder).getHandle();
		}
		return null;
	}

	public String id() {
		return this.id;
	}

	public String author() {
		return this.author;
	}

	public String version() {
		return this.version;
	}

	public String desc() {
		return this.desc;
	}

	public String[] tokens() {
		return this.tokens;
	}

	public String url() {
		return this.url;
	}

	public URL url2() {
		try {
			return new URL(this.url);
		} catch(Exception e) {
			return null;
		}
	}
	
	public boolean relational() {
		return this.relational;
	}
}
