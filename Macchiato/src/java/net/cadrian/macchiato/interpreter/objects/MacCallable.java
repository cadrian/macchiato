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
import net.cadrian.macchiato.interpreter.impl.Context;
import net.cadrian.macchiato.interpreter.impl.LocalContext;
import net.cadrian.macchiato.interpreter.objects.container.MacDictionary;
import net.cadrian.macchiato.ruleset.ast.Ruleset;

public abstract class MacCallable implements MacObject {

	protected static class InvokeMethod extends AbstractMethod<MacCallable> {

		@SuppressWarnings("unchecked")
		private static final Class<? extends MacObject>[] ARG_TYPES = new Class[] { MacDictionary.class };
		private static final String[] ARG_NAMES = { "args" };

		protected InvokeMethod(final Ruleset ruleset) {
			super(ruleset);
		}

		@Override
		public Class<MacCallable> getTargetType() {
			return MacCallable.class;
		}

		@Override
		public void run(final MacCallable target, final Context context, final int position) {
			final Context c = unpackArgs(target, context, position);
			target.invoke(c, position);
		}

		private Context unpackArgs(final MacCallable target, final Context context, final int position) {
			final MacDictionary args = context.get("args");
			final LocalContext result = new LocalContext(context, getRuleset());
			final String[] argNames = target.getArgNames();
			final Class<? extends MacObject>[] argTypes = target.getArgTypes();
			assert argNames.length == argTypes.length;
			for (int i = 0; i < argNames.length; i++) {
				final String argName = argNames[i];
				final Class<? extends MacObject> argType = argTypes[i];

				final MacObject arg = args.get(MacString.valueOf(argName));
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
		public String name() {
			return "invoke";
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
		public Class<MacObject> getResultType() {
			return MacObject.class;
		}

	};

	public abstract void invoke(final Context context, final int position);

	@Override
	public <T extends MacObject, R extends MacObject> Field<T, R> getField(final Ruleset ruleset, final String name) {
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends MacObject> Method<T> getMethod(final Ruleset ruleset, final String name) {
		switch (name) {
		case "invoke":
			return (Method<T>) new InvokeMethod(ruleset);
		}
		return null;
	}

	public abstract Class<? extends MacObject>[] getArgTypes();

	public abstract String[] getArgNames();

}
