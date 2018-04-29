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
import java.io.Reader;
import java.io.StringReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.cadrian.macchiato.interpreter.Context;
import net.cadrian.macchiato.interpreter.Function;
import net.cadrian.macchiato.interpreter.InterpreterException;
import net.cadrian.macchiato.ruleset.ast.Ruleset;
import net.cadrian.macchiato.ruleset.parser.ParserBuffer;

public class FromStringFunction extends AbstractObjectReaderFunction implements Function {

	private static final Logger LOGGER = LoggerFactory.getLogger(FromStringFunction.class);

	private static final Class<?>[] ARG_TYPES = { String.class };
	private static final String[] ARG_NAMES = { "data" };

	FromStringFunction(final Ruleset ruleset) {
		super(ruleset);
	}

	@Override
	public String name() {
		return "fromString";
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
		final String data = context.get("data");
		LOGGER.debug("<-- {}", data);
		final Object result;

		try (final Reader reader = new StringReader(data)) {
			final ParserBuffer buffer = new ParserBuffer(reader);
			result = parseObject(buffer);
		} catch (final IOException e) {
			throw new InterpreterException(e.getMessage(), position, e);
		}

		context.set("result", result);
		LOGGER.debug("--> {}", result);
	}

}
