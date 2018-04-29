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
import java.io.StringWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.cadrian.macchiato.interpreter.Context;
import net.cadrian.macchiato.interpreter.Function;
import net.cadrian.macchiato.interpreter.InterpreterException;
import net.cadrian.macchiato.ruleset.ast.Ruleset;

public class ToStringFunction extends AbstractObjectWriterFunction implements Function {

	private static final Logger LOGGER = LoggerFactory.getLogger(ToStringFunction.class);

	private static final Class<?>[] ARG_TYPES = { Object.class };
	private static final String[] ARG_NAMES = { "value" };

	ToStringFunction(final Ruleset ruleset) {
		super(ruleset);
	}

	@Override
	public String name() {
		return "toString";
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
		return String.class;
	}

	@Override
	public void run(final Context context, final int position) {
		final Object value = context.get("value");
		LOGGER.debug("<-- {}", value);

		if (value == null) {
			throw new InterpreterException("invalid value", position);
		}

		final String result;
		try (final StringWriter writer = new StringWriter()) {
			if (!writeObject(writer, value)) {
				throw new InterpreterException("invalid value", position);
			}
			result = writer.getBuffer().toString();
		} catch (final IOException e) {
			throw new InterpreterException(e.getMessage(), position, e);
		}

		context.set("result", result);
		LOGGER.debug("--> {}", result);
	}

}
