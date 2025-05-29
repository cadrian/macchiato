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

import java.math.BigInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.cadrian.macchiato.interpreter.Identifiers;
import net.cadrian.macchiato.interpreter.InterpreterException;
import net.cadrian.macchiato.interpreter.core.Context;
import net.cadrian.macchiato.interpreter.objects.MacNumber;
import net.cadrian.macchiato.interpreter.objects.MacObject;
import net.cadrian.macchiato.ruleset.ast.Ruleset;
import net.cadrian.macchiato.ruleset.ast.expression.Identifier;
import net.cadrian.macchiato.ruleset.parser.Position;

class GaussRandomFunction extends AbstractRandomFunction {

	private static final Logger LOGGER = LoggerFactory.getLogger(GaussRandomFunction.class);

	private static final Identifier NAME = new Identifier("gaussRandom", Position.NONE);
	private static final Identifier ARG_RANGE = new Identifier("range", Position.NONE);

	@SuppressWarnings("unchecked")
	private static final Class<? extends MacObject>[] ARG_TYPES = new Class[] { MacNumber.class };
	private static final Identifier[] ARG_NAMES = { ARG_RANGE };

	GaussRandomFunction(final Ruleset ruleset) {
		super(ruleset);
	}

	@Override
	public Identifier name() {
		return NAME;
	}

	@Override
	public Class<? extends MacObject>[] getArgTypes() {
		return ARG_TYPES.clone();
	}

	@Override
	public Identifier[] getArgNames() {
		return ARG_NAMES.clone();
	}

	@Override
	public Class<MacNumber> getResultType() {
		return MacNumber.class;
	}

	@Override
	public void run(final Context context, final Position position) {
		final BigInteger range = ((MacNumber) context.get(ARG_RANGE)).getValue();
		LOGGER.debug("<-- {}", range);
		if (range.signum() != 1) {
			throw new InterpreterException("invalid max value: must be strictly positive", position);
		}

		final double u1 = getRandom();
		final double u2 = getRandom();
		LOGGER.debug("u1={} u2={}", u1, u2);

		// https://en.wikipedia.org/wiki/Box%E2%80%93Muller_transform
		// sqrt(-2 * ln(u1)) * cos(2 * pi * u2)
		final double gauss = Math.sqrt(-2L * Math.log(1L / u1)) * Math.cos(2 * Math.PI / u2);
		LOGGER.debug("gauss={}", gauss);

		final BigInteger result = new BigInteger(Long.toString(Math.round(gauss * range.doubleValue())));

		LOGGER.debug("<-- {}", result);
		context.set(Identifiers.RESULT, MacNumber.valueOf(result));
	}

	private double getRandom() {
		while (true) {
			final double result = RANDOM.nextDouble();
			if (1L / result != 0L) {
				return result;
			}
		}
	}

}
