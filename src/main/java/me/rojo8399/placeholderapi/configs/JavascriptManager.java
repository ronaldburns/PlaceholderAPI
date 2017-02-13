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
package me.rojo8399.placeholderapi.configs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

/**
 * @author Wundero
 */
public class JavascriptManager {

	private Map<String, File> scripts = new HashMap<>();

	public JavascriptManager(File scriptFolder) {
		if (scriptFolder.exists() && scriptFolder.isFile()) {
			// Scripts folder incorrect
			scriptFolder.delete();
		}
		if (!scriptFolder.exists()) {
			scriptFolder.mkdirs();
		}
		// Add references to all scripts
		for (File sub : scriptFolder.listFiles((f, s) -> s.endsWith(".js"))) {
			scripts.put(sub.getName().replace(".js", "").toLowerCase(), sub);
		}
	}

	public List<String> getScriptNames() {
		List<String> out = new ArrayList<>();
		out.addAll(scripts.keySet());
		return out;
	}

	public FileReader getScript(String name) {
		if (!scripts.containsKey(name)) {
			// Prevents creating filereader on null
			return null;
		}
		try {
			return new FileReader(scripts.get(name));
		} catch (FileNotFoundException e) {
			return null;
		}
	}

	public Object eval(ScriptEngine engine, String token) {
		if (token.replace("_", "").isEmpty()) {
			// no script name
			return null;
		}
		if (token.contains("_")) {
			// script has args
			String[] arr = token.split("_");
			if (arr.length == 1) {
				// script args not present, just script_
				if (getScript(arr[0]) == null) {
					// script doesn't exist
					return null;
				}
				try {
					// evaluate script
					return engine.eval(getScript(arr[0]));
				} catch (ScriptException e) {
					return "ERROR: " + e.getMessage();
				}
			} else {
				FileReader f = getScript(arr[0]);
				if (f == null) {
					return null;
				}
				// skip script name in args
				List<String> l = Arrays.asList(arr).stream().skip(1).collect(Collectors.toList());
				// put args as array
				engine.put("args", l.toArray(new String[l.size() - 1]));
				try {
					// evaluate script
					return engine.eval(f);
				} catch (ScriptException e) {
					return "ERROR: " + e.getMessage();
				}
			}
		} else {
			if (getScript(token) == null) {
				return null;
			}
			try {
				return engine.eval(getScript(token));
			} catch (ScriptException e) {
				return "ERROR: " + e.getMessage();
			}
		}
	}

}
