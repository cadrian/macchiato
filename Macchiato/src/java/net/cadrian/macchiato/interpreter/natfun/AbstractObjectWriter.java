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

import java.io.IOException;
import java.io.Writer;
import java.math.BigInteger;
import java.util.Iterator;

import net.cadrian.macchiato.interpreter.Array;
import net.cadrian.macchiato.interpreter.Dictionary;

abstract class AbstractObjectWriter {

	AbstractObjectWriter() {
	}

	protected boolean writeObject(final Writer writer, final Object value) throws IOException {
		if (value instanceof String) {
			return writeString(writer, (String) value);
		}
		if (value instanceof BigInteger) {
			writer.write(value.toString());
			return true;
		}
		if (value instanceof Boolean) {
			writer.write(value.toString());
			return true;
		}
		if (value instanceof Array) {
			return writeArray(writer, (Array) value);
		}
		if (value instanceof Dictionary) {
			return writeDictionary(writer, (Dictionary) value);
		}
		return false;
	}

	private boolean writeString(final Writer writer, final String string) throws IOException {
		writer.write('"');
		for (final char c : string.toCharArray()) {
			switch (c) {
			case '\\':
				writer.write("\\\\");
				break;
			case '"':
				writer.write("\\\"");
				break;
			case '\n':
				writer.write("\\n");
				break;
			case '\t':
				writer.write("\\t");
				break;
			default:
				writer.write(c);
			}
		}
		writer.write('"');
		return true;
	}

	private boolean writeArray(final Writer writer, final Array array) throws IOException {
		writer.write('[');
		final Iterator<BigInteger> keys = array.keys();
		while (keys.hasNext()) {
			final BigInteger key = keys.next();
			writeObject(writer, key);
			writer.write('=');
			writeObject(writer, array.get(key));
			if (keys.hasNext()) {
				writer.write(',');
			}
		}
		writer.write(']');
		return true;
	}

	private boolean writeDictionary(final Writer writer, final Dictionary dictionary) throws IOException {
		writer.write('{');
		final Iterator<String> keys = dictionary.keys();
		while (keys.hasNext()) {
			final String key = keys.next();
			writeObject(writer, key);
			writer.write('=');
			writeObject(writer, dictionary.get(key));
			if (keys.hasNext()) {
				writer.write(',');
			}
		}
		writer.write('}');
		return true;
	}

}
