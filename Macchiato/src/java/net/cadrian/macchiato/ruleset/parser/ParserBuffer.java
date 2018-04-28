/*
 * This file is part of Macchiato.
 *
 * Macchiato is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * Macchiato is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Macchiato.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package net.cadrian.macchiato.ruleset.parser;

import java.io.IOException;
import java.io.Reader;
import java.math.BigInteger;
import java.util.regex.Pattern;

public class ParserBuffer {

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

	public void skipBlanks() {
		int state = 0;
		int pos = 0;
		while (!off()) {
			switch (state) {
			case 0: // normal text
				switch (current()) {
				case ' ':
				case '\t':
				case '\r':
				case '\n':
					break;
				case '#':
					state = 10;
					break;
				case '/':
					pos = position();
					state = 1;
					break;
				default:
					return;
				}
				break;
			case 1: // after '/'
				switch (current()) {
				case '/':
					state = 10;
					break;
				case '*':
					state = 20;
					break;
				default:
					rewind(pos);
					return;
				}
				break;
			case 10: // in one-line comment
				switch (current()) {
				case '\r':
				case '\n':
					state = 0;
					break;
				}
				break;
			case 20: // block comment
				switch (current()) {
				case '*':
					state = 21;
					break;
				default:
					break;
				}
				break;
			case 21: // block comment after '*'
				switch (current()) {
				case '/':
					state = 0;
					break;
				default:
					state = 20;
					break;
				}
				break;
			default:
				throw new ParserException("BUG: invalid state " + state);
			}
			next();
		}
	}

	public boolean readKeyword(final String keyword) {
		skipBlanks();
		final int position = position();
		for (final char kw : keyword.toCharArray()) {
			if (off() || current() != kw) {
				rewind(position);
				return false;
			}
			next();
		}
		if (!off() && Character.isJavaIdentifierPart(current())) {
			rewind(position);
			return false;
		}
		return true;
	}

	public BigInteger readBigInteger() {
		assert Character.isDigit(current());
		final StringBuilder b = new StringBuilder();
		while (!off() && Character.isDigit(current())) {
			b.append(current());
			next();
		}
		return new BigInteger(b.toString());
	}

	public String readString() {
		assert current() == '"';
		final int position = position();
		next();
		final StringBuilder b = new StringBuilder();
		int state = 0;
		do {
			if (off()) {
				throw new ParserException(error("Unfinished string", position));
			}
			switch (state) {
			case 0: // normal
				switch (current()) {
				case '\\':
					state = 1;
					break;
				case '"':
					state = -1;
					break;
				default:
					b.append(current());
				}
				break;
			case 1: // backslash
				switch (current()) {
				case 't':
					b.append('\t');
					break;
				case 'n':
					b.append('\n');
					break;
				default:
					b.append(current());
				}
				break;
			default:
				throw new ParserException(error("BUG: unexpected state " + state, position));
			}
			next();
		} while (state >= 0);
		return b.toString();
	}

	Pattern readRegex() {
		assert current() == '/';
		final int position = position();
		next();
		final StringBuilder b = new StringBuilder();
		int state = 0;
		do {
			if (off()) {
				throw new ParserException(error("Unfinished string", position));
			}
			switch (state) {
			case 0: // normal
				switch (current()) {
				case '\\':
					state = 1;
					break;
				case '/':
					state = -1;
					break;
				default:
					b.append(current());
				}
				break;
			case 1: // backslash
				switch (current()) {
				case 't':
					b.append('\t');
					break;
				case 'n':
					b.append('\n');
					break;
				default:
					b.append(current());
				}
				break;
			default:
				throw new ParserException(error("BUG: unexpected state " + state, position));
			}
			next();
		} while (state >= 0);
		final Pattern readRegex = Pattern.compile(b.toString());
		return readRegex;
	}
}
