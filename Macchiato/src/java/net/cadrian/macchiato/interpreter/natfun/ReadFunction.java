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
package net.cadrian.macchiato.interpreter.natfun;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.math.BigInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.cadrian.macchiato.interpreter.Array;
import net.cadrian.macchiato.interpreter.Context;
import net.cadrian.macchiato.interpreter.Dictionary;
import net.cadrian.macchiato.interpreter.Function;
import net.cadrian.macchiato.interpreter.InterpreterException;
import net.cadrian.macchiato.ruleset.parser.ParserBuffer;
import net.cadrian.macchiato.ruleset.parser.ParserException;

public class ReadFunction implements Function {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReadFunction.class);

	private static final Class<?>[] ARG_TYPES = { String.class };
	private static final String[] ARG_NAMES = { "file" };

	ReadFunction() {
	}

	@Override
	public String name() {
		return "read";
	}

	@Override
	public Class<?>[] getArgTypes() {
		return ARG_TYPES;
	}

	@Override
	public String[] getArgNames() {
		return ARG_NAMES;
	}

	@Override
	public Class<?> getResultType() {
		return Object.class;
	}

	@Override
	public void run(final Context context, final int position) {
		final String file = context.get("file");
		LOGGER.debug("<-- {}", file);
		final Object result;

		try (final Reader reader = new BufferedReader(new FileReader(file))) {
			final ParserBuffer buffer = new ParserBuffer(reader);
			result = parseObject(buffer);
		} catch (final IOException e) {
			throw new InterpreterException(e.getMessage(), position, e);
		}

		context.set("result", result);
		LOGGER.debug("--> {}", result);
	}

	private Object parseObject(final ParserBuffer buffer) {
		final Object result;
		buffer.skipBlanks();
		if (buffer.off()) {
			result = null;
		} else {
			switch (buffer.current()) {
			case '{':
				result = parseDictionary(buffer);
				break;
			case '[':
				result = parseArray(buffer);
				break;
			case 't':
				result = parseTrue(buffer);
				break;
			case 'f':
				result = parseFalse(buffer);
				break;
			case '"':
				result = parseString(buffer);
				break;
			default:
				if (Character.isDigit(buffer.current())) {
					result = parseNumber(buffer);
				} else {
					result = null;
				}
			}
		}
		if (result == null) {
			throw new ParserException("invalid file");
		}
		return result;
	}

	private Dictionary parseDictionary(final ParserBuffer buffer) {
		assert buffer.current() == '{';
		final Dictionary result = new Dictionary();
		buffer.next();
		buffer.skipBlanks();
		boolean more = true;
		do {
			if (buffer.off()) {
				return null;
			}
			final String index = parseString(buffer);
			if (index == null) {
				return null;
			}
			buffer.skipBlanks();
			if (buffer.off() || buffer.current() != '=') {
				return null;
			}
			buffer.next();
			buffer.skipBlanks();
			final Object value = parseObject(buffer);
			if (value == null) {
				return null;
			}
			result.set(index, value);
			buffer.skipBlanks();
			if (!buffer.off()) {
				switch (buffer.current()) {
				case '}':
					more = false;
					buffer.next();
					break;
				case ',':
					buffer.next();
					break;
				default:
					return null;
				}
			}
		} while (more);
		return result;
	}

	private Array parseArray(final ParserBuffer buffer) {
		assert buffer.current() == '[';
		final Array result = new Array();
		buffer.next();
		buffer.skipBlanks();
		boolean more = true;
		do {
			if (buffer.off()) {
				return null;
			}
			final BigInteger index = parseNumber(buffer);
			if (index == null) {
				return null;
			}
			buffer.skipBlanks();
			if (buffer.off() || buffer.current() != '=') {
				return null;
			}
			buffer.next();
			buffer.skipBlanks();
			final Object value = parseObject(buffer);
			if (value == null) {
				return null;
			}
			result.set(index, value);
			buffer.skipBlanks();
			if (!buffer.off()) {
				switch (buffer.current()) {
				case ']':
					more = false;
					buffer.next();
					break;
				case ',':
					buffer.next();
					break;
				default:
					return null;
				}
			}
		} while (more);
		return result;
	}

	private Boolean parseTrue(final ParserBuffer buffer) {
		if (buffer.readKeyword("true")) {
			return Boolean.TRUE;
		}
		return null;
	}

	private Boolean parseFalse(final ParserBuffer buffer) {
		if (buffer.readKeyword("false")) {
			return Boolean.FALSE;
		}
		return null;
	}

	private String parseString(final ParserBuffer buffer) {
		return buffer.readString();
	}

	private BigInteger parseNumber(final ParserBuffer buffer) {
		return buffer.readBigInteger();
	}

}
