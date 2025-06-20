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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("PMD.CyclomaticComplexity")
public final class ParserBuffer {

	private static final Logger LOGGER = LoggerFactory.getLogger(ParserBuffer.class);

	private static final Map<String, ParserBuffer> FLYWEIGHT = new ConcurrentHashMap<>();

	final String path;

	private final Reader reader;
	private boolean eof;
	private char[] content;
	private int length;
	private int offset;

	public static ParserBuffer getParserBuffer(final String path) throws IOException {
		final AtomicReference<IOException> exception = new AtomicReference<>();
		final ParserBuffer result = FLYWEIGHT.computeIfAbsent(path, p -> {
			try {
				return new ParserBuffer(p, null);
			} catch (final IOException e) {
				LOGGER.error("Error reading file: {}", path, e);
				exception.set(e);
			}
			return null;
		});
		if (result == null) {
			assert exception.get() != null;
			throw exception.get();
		}
		result.rewind(0);
		return result;
	}

	public static ParserBuffer getParserBuffer(final Reader reader) throws IOException {
		return new ParserBuffer(null, reader);
	}

	private ParserBuffer(final String path, final Reader reader) throws IOException {
		this.path = path;
		this.reader = reader == null ? Files.newBufferedReader(Paths.get(path)) : reader;
		content = new char[4096];
		offset = 0;
		readMore();
	}

	public boolean off() {
		if (!eof && offset == length) {
			readMore();
		}
		return eof && offset == length;
	}

	public char current() {
		assert !off();
		return content[offset];
	}

	private char[] ensureContentLength(final int wantedLength) {
		int newLength = content.length;
		if (wantedLength < newLength) {
			return content;
		}
		do {
			newLength *= 2;
		} while (newLength < wantedLength);
		final char[] newContent = new char[newLength];
		System.arraycopy(content, 0, newContent, 0, content.length);
		content = newContent;
		return newContent;
	}

	private void readMore() {
		final char[] buffer = new char[4096];
		final int n;
		try {
			n = reader.read(buffer);
		} catch (final IOException e) {
			throw new ParserException(error("Error while reading input file"), e);
		}
		final int startLength = length;
		if (n == -1) {
			eof = true;
			try {
				reader.close();
			} catch (final IOException e) {
				LOGGER.debug("Ignored close exception", e);
			}
		} else {
			System.arraycopy(buffer, 0, ensureContentLength(startLength + n), startLength, n);
			length += n;
		}
	}

	public Position position() {
		return new Position(path, offset);
	}

	public void next() {
		if (!eof && offset == length) {
			readMore();
		}
		if (!off()) {
			offset++;
		}
	}

	public void rewind(final Position position) {
		if (position == null) {
			throw new IllegalArgumentException("null position");
		}
		rewind(position.offset);
	}

	private void rewind(final int offset) {
		assert offset >= 0 && offset <= this.offset : "offset must be before current offset";
		this.offset = offset;
	}

	public String error(final String message) {
		return error(message, offset);
	}

	public String error(final String message, final Position position) throws IOException {
		return (Objects.equals(position.path, path) ? this : getParserBuffer(position.path)).error(message,
				position.offset);
	}

	private String error(final String message, final int offset) {
		final int oldOffset = this.offset;

		while (!eof && length < offset) {
			readMore();
		}

		final int startPosition = getStartPosition(offset);
		int line = 1;
		int column = 0;
		for (int i = 0; i < offset && i < length; i++) {
			if (content[i] == '\n') {
				line++;
				column = 0;
			} else {
				column++;
			}
		}
		final StringBuilder text = new StringBuilder();
		final String carret = fillErrorToCarret(offset, startPosition, text);
		fillErrorToEndOfLine(offset, text);

		this.offset = oldOffset;
		return (message == null ? "" : "**** " + message + '\n') + path + " at line " + line + ", column " + column
				+ '\n' + text + '\n' + carret;
	}

	private void fillErrorToEndOfLine(final int offset, final StringBuilder text) {
		if (offset < length && content[offset] != '\n') {
			text.append(content[offset]);
			rewind(offset);
			next();
			while (!off() && current() != '\n') {
				final char c = current();
				text.append(c);
				next();
			}
		}
	}

	private int getStartPosition(final int offset) {
		int startPosition = offset;
		if (startPosition >= length) {
			startPosition = length - 1;
		}
		while (startPosition > 0 && content[startPosition] != '\n') {
			startPosition--;
		}
		if (content[startPosition] == '\n' && startPosition < offset && startPosition < length) {
			startPosition++;
		}
		return startPosition;
	}

	public String error(final String message, final Position... positions) throws IOException {
		final StringBuilder result = new StringBuilder();
		for (int i = 0; i < positions.length; i++) {
			final Position position = positions[i];
			if (i == 0) {
				result.append(error(message, position));
			} else {
				result.append('\n').append(error(null, position));
			}
		}
		return result.toString();
	}

	private String fillErrorToCarret(final int offset, final int startPosition, final StringBuilder text) {
		final StringBuilder carret = new StringBuilder();
		for (int i = startPosition; i < offset && i < length; i++) {
			final char c = content[i];
			text.append(c);
			if (c == '\t') {
				carret.append('\t');
			} else {
				carret.append(' ');
			}
		}
		carret.append('^');
		return carret.toString();
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
					pos = offset;
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
				default:
					break;
				}
				break;
			case 20: // block comment
				if (current() == '*') {
					state = 21;
				}
				break;
			case 21: // block comment after '*'
				if (current() == '/') {
					state = 0;
				} else {
					state = 20;
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
		final int pos = offset;
		for (final char kw : keyword.toCharArray()) {
			if (off() || current() != kw) {
				rewind(pos);
				return false;
			}
			next();
		}
		if (!off() && Character.isJavaIdentifierPart(current())) {
			rewind(pos);
			return false;
		}
		return true;
	}

	public BigInteger readBigInteger() {
		assert Character.isDigit(current()) || current() == '-';
		final StringBuilder b = new StringBuilder();
		if (current() == '-') {
			b.append('-');
			next();
		}
		if (off() || !Character.isDigit(current())) {
			throw new ParserException("Invalid number: " + current() + " at " + offset);
		}
		do {
			b.append(current());
			next();
		} while (!off() && Character.isDigit(current()));
		return new BigInteger(b.toString());
	}

	public String readString() {
		assert current() == '"';
		final int pos = offset;
		next();
		final StringBuilder b = new StringBuilder();
		int state = 0;
		do {
			if (off()) {
				throw new ParserException(error("Unfinished string", pos));
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
				throw new ParserException(error("BUG: unexpected state " + state, pos));
			}
			next();
		} while (state >= 0);
		return b.toString();
	}

	Pattern readRegex() {
		assert current() == '/';
		final int pos = offset;
		next();
		final StringBuilder b = new StringBuilder();
		int state = 0;
		do {
			if (off()) {
				throw new ParserException(error("Unfinished string", pos));
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
				throw new ParserException(error("BUG: unexpected state " + state, pos));
			}
			next();
		} while (state >= 0);
		return Pattern.compile(b.toString());
	}
}
