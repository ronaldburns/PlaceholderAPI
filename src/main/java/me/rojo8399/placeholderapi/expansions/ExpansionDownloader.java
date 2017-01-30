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

import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * @author Wundero
 *
 */
public class ExpansionDownloader {

	private static final String FOLDER = "https://api.github.com/repos/rojo8399/PlaceholderAPI/contents/src/main/java/me/rojo8399/placeholderapi/expansions";

	private static final String[] ESCAPED = { "Expansion.java", "ExpansionDownloader.java" };

	private static ExpansionDownloader inst = new ExpansionDownloader();

	public static ExpansionDownloader getInstance() {
		return inst == null ? inst = new ExpansionDownloader() : inst;
	}

	private Map<JsonObject, String> contents = new ConcurrentHashMap<>();
	private BiMap<String, JsonObject> ids = HashBiMap.create();

	private ExpansionDownloader() {
		try {
			getAll();
		} catch (Exception e) {
		}
	}

	private String convertStreamToString(InputStream is) {
		@SuppressWarnings("resource")
		Scanner s = new Scanner(is).useDelimiter("\\A");
		try {
			return s.hasNext() ? s.next() : "";
		} finally {
			s.close();
		}
	}

	public boolean download(String id, File f) {
		if (!ids.containsKey(id)) {
			return false;
		}
		if (!f.getParentFile().exists()) {
			f.getParentFile().mkdirs();
		}
		if (f.exists() && f.isDirectory()) {
			f.delete();
		}
		if (!f.exists()) {
			try {
				f.createNewFile();
			} catch (Exception e) {
				return false;
			}
		}
		try {
			String content = downloadContent(get(id));
			PrintWriter p = new PrintWriter(f);
			p.write(content);
			p.flush();
			p.close();
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	private static String convertToId(String cname) {
		return cname.toLowerCase().replace("expansion.java", "");
	}

	public JsonObject get(String id) {
		if (!ids.containsKey(id)) {
			return null;
		}
		return ids.get(id);
	}

	private String getID(JsonObject o) {
		if (ids.inverse().containsKey(o)) {
			return ids.inverse().get(o);
		}
		ids.put(convertToId(o.get("name").getAsString()), o);
		return ids.inverse().get(o);
	}

	private URL getDownloadUrl(JsonObject o) throws Exception {
		return new URL(o.get("download_url").getAsString());
	}

	private String downloadContent(JsonObject o) throws Exception {
		if (contents.containsKey(o)) {
			return contents.get(o);
		}
		URL url = getDownloadUrl(o);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		connection.connect();
		String s = convertStreamToString(connection.getInputStream());
		contents.put(o, s);
		return s;
	}

	private List<JsonObject> getAll() throws Exception {
		URL url = new URL(FOLDER);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		connection.connect();
		InputStream stream = connection.getInputStream();
		String json = convertStreamToString(stream);
		JsonParser p = new JsonParser();
		JsonElement e = p.parse(json);
		List<JsonObject> out = new ArrayList<>();
		e.getAsJsonArray().forEach(elmt -> {
			JsonObject o = elmt.getAsJsonObject();
			String name = o.get("name").getAsString();
			for (String s : ESCAPED) {
				if (name.equals(s)) {
					return;
				}
			}
			out.add(o);
			getID(o);
		});
		return out;
	}

}
