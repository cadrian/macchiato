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
package net.cadrian.macchiato.interpreter.functions.natfun;

import net.cadrian.macchiato.interpreter.objects.MacBoolean;
import net.cadrian.macchiato.interpreter.objects.MacNumber;
import net.cadrian.macchiato.interpreter.objects.MacObject;
import net.cadrian.macchiato.interpreter.objects.MacString;
import net.cadrian.macchiato.interpreter.objects.container.MacArray;
import net.cadrian.macchiato.interpreter.objects.container.MacDictionary;
import net.cadrian.macchiato.ruleset.ast.Ruleset;
import net.cadrian.macchiato.ruleset.parser.ParserBuffer;
import net.cadrian.macchiato.ruleset.parser.ParserException;

abstract class AbstractObjectReaderFunction extends AbstractNativeFunction {

	AbstractObjectReaderFunction(final Ruleset ruleset) {
		super(ruleset);
	}

	protected MacObject parseObject(final ParserBuffer buffer) {
		final MacObject result;
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
			case '-':
				result = parseNumber(buffer);
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

	private MacDictionary parseDictionary(final ParserBuffer buffer) {
		assert buffer.current() == '{';
		final MacDictionary result = new MacDictionary();
		buffer.next();
		buffer.skipBlanks();
		boolean more = !buffer.off() && buffer.current() != '}';
		while (more) {
			if (buffer.off()) {
				return null;
			}
			final MacString index = parseString(buffer);
			if (index == null) {
				return null;
			}
			buffer.skipBlanks();
			if (buffer.off() || buffer.current() != '=') {
				return null;
			}
			buffer.next();
			buffer.skipBlanks();
			final MacObject value = parseObject(buffer);
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
		}
		return result;
	}

	private MacArray parseArray(final ParserBuffer buffer) {
		assert buffer.current() == '[';
		final MacArray result = new MacArray();
		buffer.next();
		buffer.skipBlanks();
		boolean more = !buffer.off() && buffer.current() != ']';
		while (more) {
			if (buffer.off()) {
				return null;
			}
			final MacNumber index = parseNumber(buffer);
			if (index == null) {
				return null;
			}
			buffer.skipBlanks();
			if (buffer.off() || buffer.current() != '=') {
				return null;
			}
			buffer.next();
			buffer.skipBlanks();
			final MacObject value = parseObject(buffer);
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
		}
		return result;
	}

	private MacBoolean parseTrue(final ParserBuffer buffer) {
		if (buffer.readKeyword("true")) {
			return MacBoolean.TRUE;
		}
		return null;
	}

	private MacBoolean parseFalse(final ParserBuffer buffer) {
		if (buffer.readKeyword("false")) {
			return MacBoolean.FALSE;
		}
		return null;
	}

	private MacString parseString(final ParserBuffer buffer) {
		return MacString.valueOf(buffer.readString());
	}

	private MacNumber parseNumber(final ParserBuffer buffer) {
		return MacNumber.valueOf(buffer.readBigInteger());
	}

}
