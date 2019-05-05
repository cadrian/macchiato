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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.cadrian.macchiato.interpreter.Function;
import net.cadrian.macchiato.interpreter.InterpreterException;
import net.cadrian.macchiato.interpreter.impl.Context;
import net.cadrian.macchiato.interpreter.objects.MacObject;
import net.cadrian.macchiato.interpreter.objects.MacString;
import net.cadrian.macchiato.ruleset.ast.Ruleset;

class WriteFunction extends AbstractObjectWriterFunction implements Function {

	private static final Logger LOGGER = LoggerFactory.getLogger(WriteFunction.class);

	@SuppressWarnings("unchecked")
	private static final Class<? extends MacObject>[] ARG_TYPES = new Class[] { MacString.class, MacObject.class };
	private static final String[] ARG_NAMES = { "file", "value" };

	WriteFunction(final Ruleset ruleset) {
		super(ruleset);
	}

	@Override
	public String name() {
		return "write";
	}

	@Override
	public Class<? extends MacObject>[] getArgTypes() {
		return ARG_TYPES;
	}

	@Override
	public String[] getArgNames() {
		return ARG_NAMES;
	}

	@Override
	public Class<? extends MacObject> getResultType() {
		return null;
	}

	@Override
	public void run(final Context context, final int position) {
		final MacString file = context.get("file");
		final MacObject value = context.get("value");
		LOGGER.debug("<-- {}: {}", file, value);

		if (value == null) {
			throw new InterpreterException("invalid value", position);
		}

		try (final Writer writer = new BufferedWriter(new FileWriter(file.getValue()))) {
			if (!writeObject(writer, value)) {
				throw new InterpreterException("invalid value", position);
			}
		} catch (final IOException e) {
			throw new InterpreterException(e.getMessage(), position, e);
		}

		LOGGER.debug("-->");
	}

}
