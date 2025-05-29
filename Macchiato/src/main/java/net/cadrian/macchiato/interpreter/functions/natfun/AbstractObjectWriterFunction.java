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

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

import net.cadrian.macchiato.interpreter.objects.MacBoolean;
import net.cadrian.macchiato.interpreter.objects.MacNumber;
import net.cadrian.macchiato.interpreter.objects.MacObject;
import net.cadrian.macchiato.interpreter.objects.MacString;
import net.cadrian.macchiato.interpreter.objects.container.MacArray;
import net.cadrian.macchiato.interpreter.objects.container.MacDictionary;
import net.cadrian.macchiato.ruleset.ast.Ruleset;

abstract class AbstractObjectWriterFunction extends AbstractNativeFunction {

	AbstractObjectWriterFunction(final Ruleset ruleset) {
		super(ruleset);
	}

	protected boolean writeObject(final Writer writer, final MacObject value) throws IOException {
		if (value instanceof MacString) {
			return writeString(writer, ((MacString) value).getValue());
		}
		if (value instanceof MacNumber || value instanceof MacBoolean) {
			writer.write(value.toString());
			return true;
		}
		if (value instanceof MacArray) {
			return writeArray(writer, (MacArray) value);
		}
		if (value instanceof MacDictionary) {
			return writeDictionary(writer, (MacDictionary) value);
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

	private boolean writeArray(final Writer writer, final MacArray array) throws IOException {
		writer.write('[');
		final Iterator<MacNumber> keys = array.keys();
		while (keys.hasNext()) {
			final MacNumber key = keys.next();
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

	private boolean writeDictionary(final Writer writer, final MacDictionary dictionary) throws IOException {
		writer.write('{');
		final Iterator<MacString> keys = dictionary.keys();
		while (keys.hasNext()) {
			final MacString key = keys.next();
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
