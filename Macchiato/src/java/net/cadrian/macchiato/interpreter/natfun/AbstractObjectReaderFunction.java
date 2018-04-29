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

import java.math.BigInteger;

import net.cadrian.macchiato.container.Array;
import net.cadrian.macchiato.container.Dictionary;
import net.cadrian.macchiato.ruleset.ast.Ruleset;
import net.cadrian.macchiato.ruleset.parser.ParserBuffer;
import net.cadrian.macchiato.ruleset.parser.ParserException;

abstract class AbstractObjectReaderFunction extends AbstractNativeFunction {

	AbstractObjectReaderFunction(final Ruleset ruleset) {
		super(ruleset);
	}

	protected Object parseObject(final ParserBuffer buffer) {
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
