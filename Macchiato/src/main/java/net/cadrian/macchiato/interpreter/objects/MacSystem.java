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
package net.cadrian.macchiato.interpreter.objects;

import net.cadrian.macchiato.interpreter.Field;
import net.cadrian.macchiato.interpreter.Function;
import net.cadrian.macchiato.interpreter.Method;
import net.cadrian.macchiato.interpreter.core.Context;
import net.cadrian.macchiato.interpreter.functions.natfun.Native;
import net.cadrian.macchiato.ruleset.ast.Ruleset;
import net.cadrian.macchiato.ruleset.ast.expression.Identifier;
import net.cadrian.macchiato.ruleset.parser.Position;

public class MacSystem implements MacObject {

	private static class NativeMethod extends AbstractMethod<MacSystem> {

		private final Native fun;
		private final Function function;

		NativeMethod(final Ruleset ruleset, final Native fun) {
			super(ruleset);
			this.fun = fun;
			function = fun.getFunction(ruleset);
		}

		@Override
		public Class<MacSystem> getTargetType() {
			return MacSystem.class;
		}

		@Override
		public void run(final MacSystem target, final Context context, final Position position) {
			function.run(context, position);
		}

		@Override
		public Identifier name() {
			return new Identifier(fun.name(), Position.NONE);
		}

		@Override
		public Class<? extends MacObject>[] getArgTypes() {
			return function.getArgTypes();
		}

		@Override
		public Identifier[] getArgNames() {
			return function.getArgNames();
		}

		@Override
		public Class<? extends MacObject> getResultType() {
			return function.getResultType();
		}

	}

	@Override
	public <T extends MacObject, R extends MacObject> Field<T, R> getField(final Ruleset ruleset,
			final Identifier name) {
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends MacObject> Method<T> getMethod(final Ruleset ruleset, final Identifier name) {
		try {
			final Native fun = Native.valueOf(name.getName());
			return (Method<T>) new NativeMethod(ruleset, fun);
		} catch (final IllegalArgumentException e) {
			return null;
		}
	}

	@Override
	public <T extends MacObject> T asIndexType(final Class<T> type) {
		if (type == getClass()) {
			return type.cast(this);
		}
		return null;
	}

}
