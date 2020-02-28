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

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import net.cadrian.macchiato.ruleset.ast.Ruleset;

abstract class AbstractRandomFunction extends AbstractNativeFunction {

	protected static final SecureRandom RANDOM;
	static {
		SecureRandom random;
		try {
			random = SecureRandom.getInstanceStrong();
		} catch (final NoSuchAlgorithmException e) {
			random = new SecureRandom();
		}
		RANDOM = random;
	}

	AbstractRandomFunction(final Ruleset ruleset) {
		super(ruleset);
	}

}
