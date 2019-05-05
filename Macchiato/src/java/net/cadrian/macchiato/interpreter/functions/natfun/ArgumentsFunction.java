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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.cadrian.macchiato.interpreter.Function;
import net.cadrian.macchiato.interpreter.impl.Context;
import net.cadrian.macchiato.interpreter.objects.MacObject;
import net.cadrian.macchiato.interpreter.objects.container.MacArray;
import net.cadrian.macchiato.ruleset.ast.Ruleset;

class ArgumentsFunction extends AbstractNativeFunction implements Function {

	private static final Logger LOGGER = LoggerFactory.getLogger(ArgumentsFunction.class);

	@SuppressWarnings("unchecked")
	private static final Class<? extends MacObject>[] ARG_TYPES = new Class[0];
	private static final String[] ARG_NAMES = {};

	ArgumentsFunction(final Ruleset ruleset) {
		super(ruleset);
	}

	@Override
	public String name() {
		return "arguments";
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
	public Class<MacArray> getResultType() {
		return MacArray.class;
	}

	@Override
	public void run(final Context context, final int position) {
		LOGGER.debug("<--");
		final MacArray result = context.getArguments();
		context.set("result", result);
		LOGGER.debug("--> {}", result);
	}

}
