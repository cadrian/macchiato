package net.cadrian.macchiato.recipe.parser;

import java.io.IOException;
import java.io.Reader;

class ParserBuffer {

	private final Reader reader;
	private boolean eof;
	private char[] content;
	private int position;

	public ParserBuffer(final Reader reader) {
		this.reader = reader;
		this.content = new char[0];
		this.position = 0;
		readMore();
	}

	public boolean off() {
		if (!eof && position == content.length) {
			readMore();
		}
		return eof && position == content.length;
	}

	public char current() {
		assert !off();
		return content[position];
	}

	private void readMore() {
		final char[] buffer = new char[4096];
		int n;
		try {
			n = reader.read(buffer);
		} catch (final IOException e) {
			throw new ParserException(error("Error while reading input file"), e);
		}
		if (n == -1) {
			eof = true;
		} else {
			final char[] newContent = new char[content.length + n];
			System.arraycopy(content, 0, newContent, 0, content.length);
			System.arraycopy(buffer, 0, newContent, content.length, n);
			content = newContent;
		}
	}

	public int position() {
		return position;
	}

	public void next() {
		if (!eof && position == content.length) {
			readMore();
		}
		if (!off()) {
			position++;
		}
	}

	public void rewind(final int position) {
		assert position >= 0 && position < this.position;
		this.position = position;
	}

	public String error(final String message) {
		return error(message, position);
	}

	public String error(final String message, final int position) {
		final int oldPosition = this.position;

		while (!eof && content.length < position) {
			readMore();
		}
		int startPosition = position;
		if (startPosition >= content.length) {
			startPosition = content.length - 1;
		}
		while (startPosition > 0 && content[startPosition] != '\n') {
			startPosition--;
		}
		if (content[startPosition] == '\n' && startPosition < position) {
			startPosition++;
		}
		final StringBuilder text = new StringBuilder();
		final StringBuilder carret = new StringBuilder();
		for (int i = startPosition; i < position; i++) {
			final char c = content[i];
			text.append(c);
			if (c == '\t') {
				carret.append('\t');
			} else {
				carret.append(' ');
			}
		}
		carret.append('^');
		if (position < content.length && content[position] != '\n') {
			text.append(content[position]);
			rewind(position);
			next();
			while (!off() && current() != '\n') {
				final char c = current();
				text.append(c);
				next();
			}
			rewind(oldPosition);
		}

		return (message == null ? "at " : message + " at ") + position + '\n' + text + '\n' + carret;
	}

	public String error(String message, final int... positions) {
		final StringBuilder result = new StringBuilder();
		for (final int position : positions) {
			if (result.length() > 0) {
				result.append('\n');
			}
			result.append(error(message, position));
			message = null;
		}
		return result.toString();
	}
}
