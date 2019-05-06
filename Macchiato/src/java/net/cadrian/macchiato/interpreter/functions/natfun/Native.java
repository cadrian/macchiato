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

public enum Native {
	random {
		@Override
		public Function getFunction(final Ruleset ruleset) {
			return new RandomFunction(ruleset);
		};
	},
	read {
		@Override
		public Function getFunction(final Ruleset ruleset) {
			return new ReadFunction(ruleset);
		}
	},
	write {
		@Override
		public Function getFunction(final Ruleset ruleset) {
			return new WriteFunction(ruleset);
		}
	},
	toString {
		@Override
		public Function getFunction(final Ruleset ruleset) {
			return new ToStringFunction(ruleset);
		}
	},
	fromString {
		@Override
		public Function getFunction(final Ruleset ruleset) {
			return new FromStringFunction(ruleset);
		}
	},
	print {
		@Override
		public Function getFunction(final Ruleset ruleset) {
			return new PrintFunction(ruleset);
		}
	};

	public abstract Function getFunction(Ruleset ruleset);
}
