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
import java.io.Reader;
import java.io.StringReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.cadrian.macchiato.interpreter.Function;
import net.cadrian.macchiato.interpreter.Identifiers;
import net.cadrian.macchiato.interpreter.InterpreterException;
import net.cadrian.macchiato.interpreter.impl.Context;
import net.cadrian.macchiato.interpreter.objects.MacObject;
import net.cadrian.macchiato.interpreter.objects.MacString;
import net.cadrian.macchiato.ruleset.ast.Ruleset;
import net.cadrian.macchiato.ruleset.ast.expression.Identifier;
import net.cadrian.macchiato.ruleset.parser.ParserBuffer;

class FromStringFunction extends AbstractObjectReaderFunction implements Function {

	private static final Logger LOGGER = LoggerFactory.getLogger(FromStringFunction.class);

	private static final Identifier NAME = new Identifier("fromString", 0);
	private static final Identifier ARG_DATA = new Identifier("data", 0);

	@SuppressWarnings("unchecked")
	private static final Class<? extends MacObject>[] ARG_TYPES = new Class[] { MacString.class };
	private static final Identifier[] ARG_NAMES = { ARG_DATA };

	FromStringFunction(final Ruleset ruleset) {
		super(ruleset);
	}

	@Override
	public Identifier name() {
		return NAME;
	}

	@Override
	public Class<? extends MacObject>[] getArgTypes() {
		return ARG_TYPES;
	}

	@Override
	public Identifier[] getArgNames() {
		return ARG_NAMES;
	}

	@Override
	public Class<? extends MacObject> getResultType() {
		return MacObject.class;
	}

	@Override
	public void run(final Context context, final int position) {
		final MacString data = context.get(ARG_DATA);
		LOGGER.debug("<-- {}", data);
		final MacObject result;

		try (final Reader reader = new StringReader(data.getValue())) {
			final ParserBuffer buffer = new ParserBuffer(reader);
			result = parseObject(buffer);
		} catch (final IOException e) {
			throw new InterpreterException(e.getMessage(), e, position);
		}

		context.set(Identifiers.RESULT, result);
		LOGGER.debug("--> {}", result);
	}

}
