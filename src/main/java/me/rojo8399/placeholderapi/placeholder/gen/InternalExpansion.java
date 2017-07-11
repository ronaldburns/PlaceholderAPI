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
package me.rojo8399.placeholderapi.placeholder.gen;

import org.spongepowered.api.Sponge;

import me.rojo8399.placeholderapi.PlaceholderAPIPlugin;
import me.rojo8399.placeholderapi.placeholder.Expansion;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

/**
 * @author Wundero
 *
 */
public abstract class InternalExpansion<S, O, V> extends Expansion<S, O, V> {

	public InternalExpansion(String id, Object handle) {
		super(id);
		this.handle = handle;
	}

	protected Object handle;

	public Object getHandle() {
		return handle;
	}

	@Override
	public void unregisterListeners() {
		Sponge.getEventManager().unregisterListeners(handle);
	}

	@Override
	public void registerListeners() {
		Sponge.getEventManager().registerListeners(PlaceholderAPIPlugin.getInstance(), handle);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getConfiguration() {
		try {
			return (T) handle;
		} catch (Exception e) {
			return null;
		}
	}
	
	@Override
	protected void populateConfigObject() {
		ConfigurationNode node = PlaceholderAPIPlugin.getInstance().getRootConfig().getNode("expansions",
				(relational() ? "rel_" : "") + Sponge.getPluginManager().fromInstance(getPlugin()).get().getId(),
				id().toLowerCase().trim());
		if (node.isVirtual()) {
			try {
				ObjectMapper.forObject(handle).serialize(node);
				PlaceholderAPIPlugin.getInstance().saveConfig();
			} catch (Exception e2) {
			}
		}
		try {
			handle = ObjectMapper.forObject(handle).populate(node);
		} catch (ObjectMappingException e1) {
			try {
				ObjectMapper.forObject(handle).serialize(node);
				PlaceholderAPIPlugin.getInstance().saveConfig();
			} catch (Exception e2) {
			}
		}
	}

}
