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

import net.cadrian.macchiato.interpreter.Function;
import net.cadrian.macchiato.ruleset.ast.Ruleset;

@SuppressWarnings("PMD.FieldNamingConventions")
public enum Native {
	Random {
		@Override
		public Function getFunction(final Ruleset ruleset) {
			return new RandomFunction(ruleset);
		}
	},
	GaussRandom {
		@Override
		public Function getFunction(final Ruleset ruleset) {
			return new GaussRandomFunction(ruleset);
		}
	},
	Read {
		@Override
		public Function getFunction(final Ruleset ruleset) {
			return new ReadFunction(ruleset);
		}
	},
	Write {
		@Override
		public Function getFunction(final Ruleset ruleset) {
			return new WriteFunction(ruleset);
		}
	},
	ToString {
		@Override
		public Function getFunction(final Ruleset ruleset) {
			return new ToStringFunction(ruleset);
		}
	},
	FromString {
		@Override
		public Function getFunction(final Ruleset ruleset) {
			return new FromStringFunction(ruleset);
		}
	},
	Print {
		@Override
		public Function getFunction(final Ruleset ruleset) {
			return new PrintFunction(ruleset);
		}
	};

	public abstract Function getFunction(Ruleset ruleset);
}
