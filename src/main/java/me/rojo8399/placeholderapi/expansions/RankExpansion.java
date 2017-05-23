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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import me.rojo8399.placeholderapi.PlaceholderAPIPlugin;
import me.rojo8399.placeholderapi.configs.Messages;

public class RankExpansion implements Expansion {

	/*
	 * (non-Javadoc)
	 * 
	 * @see me.rojo8399.placeholderapi.expansions.Expansion#canRegister()
	 */
	@Override
	public boolean canRegister() {
		return PlaceholderAPIPlugin.getInstance().getConfig().expansions.rank;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see me.rojo8399.placeholderapi.expansions.Expansion#getIdentifier()
	 */
	@Override
	public String getIdentifier() {
		return "rank";
	}

	@Override
	public String getDescription() {
		return Messages.get().placeholder.rankdesc.value;
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
	 * Get the bottommost parent in the inheritance tree.
	 */
	private static Subject getParentGroup(Subject subject) {
		List<Subject> parents = subject.getParents();
		return parents.stream().sorted((s1, s2) -> {
			if (s1.isChildOf(s2)) {
				return 1;
			}
			if (s2.isChildOf(s1)) {
				return -1;
			}
			return 0;
		}).findFirst().orElse(parents.isEmpty() ? subject : parents.get(0));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * me.rojo8399.placeholderapi.expansions.Expansion#onPlaceholderRequest(org.
	 * spongepowered.api.entity.living.player.Player, java.lang.String)
	 */
	@Override
	public Text onPlaceholderRequest(Player player, Optional<String> token) {
		if (!token.isPresent()) {
			return Text.of(getParentGroup(player).getIdentifier());
		}
		Subject rank = getParentGroup(player);
		String t = token.get();
		switch (t) {
		case "prefix":
		case "suffix":
			return TextSerializers.FORMATTING_CODE.deserialize(rank.getOption(t).orElse(""));
		case "name":
			return Text.of(rank.getIdentifier());
		}
		if (t.contains("_") && t.indexOf("_") < t.length()) {
			if (t.startsWith("option")) {
				String opt = t.substring(t.indexOf("_") + 1);
				return TextSerializers.FORMATTING_CODE.deserialize(rank.getOption(opt).orElse(""));
			}
			// this also covers "permission_..."
			// return whether the rank has a permission
			if (t.startsWith("perm")) {
				String perm = t.substring(t.indexOf("_") + 1);
				return Text.of(rank.getPermissionValue(rank.getActiveContexts(), perm).toString());
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see me.rojo8399.placeholderapi.expansions.Expansion#getSupportedTokens()
	 */
	@Override
	public List<String> getSupportedTokens() {
		return Arrays.asList(null, "prefix", "suffix", "name", "option_[option]", "permission_[permission]");
	}

}
