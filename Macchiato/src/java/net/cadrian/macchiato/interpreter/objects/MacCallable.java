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

import net.cadrian.macchiato.interpreter.Callable;
import net.cadrian.macchiato.interpreter.Function;
import net.cadrian.macchiato.interpreter.InterpreterException;
import net.cadrian.macchiato.interpreter.Method;
import net.cadrian.macchiato.interpreter.impl.Context;
import net.cadrian.macchiato.interpreter.impl.LocalContext;
import net.cadrian.macchiato.interpreter.objects.container.MacDictionary;
import net.cadrian.macchiato.ruleset.ast.Ruleset;

public class MacCallable implements MacObject {

	private static class InvokeMethod extends AbstractMethod<MacCallable> {

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

	private final Callable callable;
	private final MacObject target;

	public MacCallable(final Method<? extends MacObject> callable, final MacObject target) {
		this.callable = callable;
		this.target = target;
	}

	public void invoke(final Context c, final int position) {
		if (target == null) {
			runFunction(c, position);
		} else {
			runMethod(c, position);
		}
	}

	private void runFunction(final Context context, final int position) {
		final Function f = (Function) callable;
		f.run(context, position);
	}

	@SuppressWarnings("unchecked")
	private <T extends MacObject> void runMethod(final Context context, final int position) {
		final Method<T> m = (Method<T>) callable;
		final T t = (T) target;
		m.run(t, context, position);
	}

	public MacCallable(final Function callable) {
		this.callable = callable;
		this.target = null;
	}

	@Override
	public Method<? extends MacObject> getMethod(final Ruleset ruleset, final String name) {
		switch (name) {
		case "invoke":
			return new InvokeMethod(ruleset);
		}
		return null;
	}

	public Class<? extends MacObject>[] getArgTypes() {
		return callable.getArgTypes();
	}

	public String[] getArgNames() {
		return callable.getArgNames();
	}

}
