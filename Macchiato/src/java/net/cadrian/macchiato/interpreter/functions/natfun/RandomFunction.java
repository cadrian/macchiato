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
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.cadrian.macchiato.interpreter.Identifiers;
import net.cadrian.macchiato.interpreter.InterpreterException;
import net.cadrian.macchiato.interpreter.impl.Context;
import net.cadrian.macchiato.interpreter.objects.MacNumber;
import net.cadrian.macchiato.interpreter.objects.MacObject;
import net.cadrian.macchiato.ruleset.ast.Ruleset;
import net.cadrian.macchiato.ruleset.ast.expression.Identifier;

class RandomFunction extends AbstractNativeFunction {

	private static final Logger LOGGER = LoggerFactory.getLogger(RandomFunction.class);

	private static final Identifier NAME = new Identifier("random", 0);
	private static final Identifier ARG_MAX = new Identifier("max", 0);

	private static final SecureRandom RANDOM;
	static {
		SecureRandom random;
		try {
			random = SecureRandom.getInstanceStrong();
		} catch (final NoSuchAlgorithmException e) {
			random = new SecureRandom();
		}
		RANDOM = random;
	}

	@SuppressWarnings("unchecked")
	private static final Class<? extends MacObject>[] ARG_TYPES = new Class[] { MacNumber.class };
	private static final Identifier[] ARG_NAMES = { ARG_MAX };

	RandomFunction(final Ruleset ruleset) {
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
	public Class<MacNumber> getResultType() {
		return MacNumber.class;
	}

	@Override
	public void run(final Context context, final int position) {
		final BigInteger max = ((MacNumber) context.get(ARG_MAX)).getValue();
		LOGGER.debug("<-- {}", max);
		if (max.signum() != 1) {
			throw new InterpreterException("invalid max value: must be strictly positive", position);
		}
		final int randomSize = (max.bitLength() + 7) / 8;
		final byte[] randomBytes = new byte[randomSize];
		RANDOM.nextBytes(randomBytes);
		LOGGER.debug("random bytes={}", randomBytes);
		final BigInteger random = new BigInteger(1, randomBytes);
		LOGGER.debug("random={}", random);
		final BigInteger result = random.mod(max);
		LOGGER.debug("<-- {}", result);
		context.set(Identifiers.RESULT, MacNumber.valueOf(result));
	}

}
