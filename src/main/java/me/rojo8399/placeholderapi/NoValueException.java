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
package me.rojo8399.placeholderapi;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.api.text.Text;

import me.rojo8399.placeholderapi.impl.configs.Messages;

/**
 * A no value exception representing when the returned placeholder should not
 * parse.
 * 
 * @author Wundero
 */
public class NoValueException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4162128874653399415L;

	private List<String> suggestions = new ArrayList<>();
	private Text message;

	/**
	 * Create a new NoValueException.
	 */
	public NoValueException() {
		this(Messages.get().misc.invalid.t("token"));
	}

	/**
	 * Create a new NoValueException.
	 * 
	 * @param message
	 *            Unused.
	 */
	public NoValueException(String message) {
		super(message);
	}

	public NoValueException(Text message) {
		this.message = message;
	}

	public NoValueException(Text message, List<String> suggestions) {
		this.message = message;
		this.suggestions = suggestions;
	}

	public NoValueException(String message, List<String> suggestions) {
		super(message);
		this.suggestions = suggestions;
	}

	/**
	 * Create a new NoValueException.
	 * 
	 * @param message
	 *            Unused.
	 * @param cause
	 *            Unused.
	 */
	public NoValueException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Create a new NoValueException.
	 * 
	 * @param message
	 *            Unused.
	 * @param cause
	 *            Unused.
	 * @param enableSuppression
	 *            whether or not suppression is enabled or disabled
	 * @param writableStackTrace
	 *            whether or not the stack trace should be writable
	 */
	public NoValueException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	/**
	 * Create a new NoValueException.
	 * 
	 * @param cause
	 *            Unused.
	 */
	public NoValueException(Throwable cause) {
		super(cause);
	}

	public List<String> suggestions() {
		return suggestions;
	}

	public Text getTextMessage() {
		if (this.message == null) {
			return Text.of(super.getMessage());
		}
		return this.message;
	}

}
