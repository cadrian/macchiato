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
import net.cadrian.macchiato.interpreter.InterpreterException;
import net.cadrian.macchiato.interpreter.Method;
import net.cadrian.macchiato.interpreter.core.Context;
import net.cadrian.macchiato.interpreter.core.LocalContext;
import net.cadrian.macchiato.interpreter.objects.container.MacDictionary;
import net.cadrian.macchiato.ruleset.ast.Ruleset;
import net.cadrian.macchiato.ruleset.ast.expression.Identifier;
import net.cadrian.macchiato.ruleset.parser.Position;

public abstract class MacCallable implements MacObject {

	protected static class InvokeMethod extends AbstractMethod<MacCallable> {

		@SuppressWarnings("unchecked")
		private static final Class<? extends MacObject>[] ARG_TYPES = new Class[] { MacDictionary.class };
		private static final Identifier NAME = new Identifier("Invoke", Position.NONE);
		private static final Identifier ARG_ARGS = new Identifier("args", Position.NONE);
		private static final Identifier[] ARG_NAMES = { ARG_ARGS };

		protected InvokeMethod(final Ruleset ruleset) {
			super(ruleset);
		}

		@Override
		public Class<MacCallable> getTargetType() {
			return MacCallable.class;
		}

		@Override
		public void run(final MacCallable target, final Context context, final Position position) {
			final Context c = unpackArgs(target, context, position);
			target.invoke(c, position);
		}

		private Context unpackArgs(final MacCallable target, final Context context, final Position position) {
			final MacDictionary args = context.get(ARG_ARGS);
			final LocalContext result = context.newLocalContext(getRuleset());
			final Identifier[] argNames = target.getArgNames();
			final Class<? extends MacObject>[] argTypes = target.getArgTypes();
			assert argNames.length == argTypes.length;
			for (int i = 0; i < argNames.length; i++) {
				final Identifier argName = argNames[i];
				final Class<? extends MacObject> argType = argTypes[i];

				final MacObject arg = args.get(MacString.valueOf(argName.getName()));
				if (arg == null) {
					throw new InterpreterException("invalid argument " + argName + ": does not exist", position);
				}
				if (!argType.isAssignableFrom(arg.getClass())) {
					throw new InterpreterException("invalid argument " + argName + ": incompatible type", position);
				}

				result.set(argName, arg);
			}
			return result;
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
		public Class<MacObject> getResultType() {
			return MacObject.class;
		}

	};

	public abstract void invoke(final Context context, final Position position);

	@Override
	public <T extends MacObject, R extends MacObject> Field<T, R> getField(final Ruleset ruleset,
			final Identifier name) {
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends MacObject> Method<T> getMethod(final Ruleset ruleset, final Identifier name) {
		switch (name.getName()) {
		case "Invoke":
			return (Method<T>) new InvokeMethod(ruleset);
		}
		return null;
	}

	public abstract Class<? extends MacObject>[] getArgTypes();

	public abstract Identifier[] getArgNames();

}
