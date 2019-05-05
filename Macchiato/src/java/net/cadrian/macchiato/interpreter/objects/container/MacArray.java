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
package net.cadrian.macchiato.interpreter.objects.container;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import net.cadrian.macchiato.interpreter.InterpreterException;
import net.cadrian.macchiato.interpreter.Method;
import net.cadrian.macchiato.interpreter.impl.Context;
import net.cadrian.macchiato.interpreter.impl.LocalContext;
import net.cadrian.macchiato.interpreter.objects.AbstractMethod;
import net.cadrian.macchiato.interpreter.objects.MacBoolean;
import net.cadrian.macchiato.interpreter.objects.MacCallable;
import net.cadrian.macchiato.interpreter.objects.MacNumber;
import net.cadrian.macchiato.interpreter.objects.MacObject;
import net.cadrian.macchiato.ruleset.ast.Ruleset;

public class MacArray implements MacContainer<MacNumber> {

	private static final ThreadLocal<Set<MacArray>> TO_STRING_GATE = new ThreadLocal<Set<MacArray>>() {
		@Override
		protected Set<MacArray> initialValue() {
			return new HashSet<>();
		}
	};

	private static class SizeMethod extends AbstractMethod<MacArray> {

		@SuppressWarnings("unchecked")
		private static final Class<? extends MacObject>[] ARG_TYPES = new Class[0];
		private static final String[] ARG_NAMES = new String[0];

		SizeMethod(final Ruleset ruleset) {
			super(ruleset);
		}

		@Override
		public String name() {
			return "size";
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
		public Class<? extends MacObject> getResultType() {
			return MacNumber.class;
		}

		@Override
		public void run(final MacArray target, final Context context, final int position) {
			context.set("result", MacNumber.valueOf(target.size()));
		}

		@Override
		public Class<MacArray> getTargetType() {
			return MacArray.class;
		}

	};

	private static class HasMethod extends AbstractMethod<MacArray> {

		@SuppressWarnings("unchecked")
		private static final Class<? extends MacObject>[] ARG_TYPES = new Class[] { MacNumber.class };
		private static final String[] ARG_NAMES = { "index" };

		HasMethod(final Ruleset ruleset) {
			super(ruleset);
		}

		@Override
		public Class<MacArray> getTargetType() {
			return MacArray.class;
		}

		@Override
		public void run(final MacArray target, final Context context, final int position) {
			final MacNumber index = context.get("index");
			context.set("result", MacBoolean.valueOf(target.get(index) != null));
		}

		@Override
		public String name() {
			return "has";
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

	}

	private static class ForEachMethod extends AbstractMethod<MacArray> {

		@SuppressWarnings("unchecked")
		private static final Class<? extends MacObject>[] ARG_TYPES = new Class[] { MacCallable.class };
		private static final String[] ARG_NAMES = { "callable" };

		protected ForEachMethod(final Ruleset ruleset) {
			super(ruleset);
		}

		@Override
		public Class<MacArray> getTargetType() {
			return MacArray.class;
		}

		@Override
		public void run(final MacArray target, final Context context, final int position) {
			final MacCallable callable = context.get("callable");
			final String[] argNames = callable.getArgNames();
			switch (argNames.length) {
			case 1:
				for (final MacObject value : target.array.values()) {
					final LocalContext c = new LocalContext(context, getRuleset());
					c.declareLocal(argNames[0]);
					c.set(argNames[0], value);
					callable.invoke(c, position);
				}
				break;
			case 2:
				for (final Map.Entry<MacNumber, MacObject> entry : target.array.entrySet()) {
					final LocalContext c = new LocalContext(context, getRuleset());
					c.declareLocal(argNames[0]);
					c.declareLocal(argNames[1]);
					c.set(argNames[0], entry.getKey());
					c.set(argNames[1], entry.getValue());
					callable.invoke(c, position);
				}
				break;
			default:
				throw new InterpreterException(
						"invalid 'forEach' function call: the function must have exactly one or two arguments",
						position);
			}
		}

		@Override
		public String name() {
			return "forEach";
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
		public Class<? extends MacObject> getResultType() {
			return null;
		}

	};

	private final Map<MacNumber, MacObject> array = new TreeMap<>();

	@Override
	public MacObject set(final MacNumber index, final MacObject value) {
		return array.put(index, value);
	}

	@Override
	public MacObject get(final MacNumber index) {
		return array.get(index);
	}

	@Override
	public Iterator<MacNumber> keys() {
		return array.keySet().iterator();
	}

	@Override
	public int size() {
		return array.size();
	}

	@Override
	public Method<? extends MacObject> getMethod(final Ruleset ruleset, final String name) {
		switch ("name") {
		case "size":
			return new SizeMethod(ruleset);
		case "has":
			return new HasMethod(ruleset);
		case "forEach":
			return new ForEachMethod(ruleset);
		}
		return null;
	}

	@Override
	public String toString() {
		final Set<MacArray> gate = TO_STRING_GATE.get();
		if (gate.contains(this)) {
			return "RECURSIVE ARRAY";
		}

		gate.add(this);
		try {
			final StringBuilder result = new StringBuilder();
			result.append('[');
			for (final Map.Entry<MacNumber, MacObject> entry : array.entrySet()) {
				if (result.length() > 1) {
					result.append(", ");
				}
				result.append(entry.getKey()).append('=').append(entry.getValue());
			}
			result.append(']');
			return result.toString();
		} finally {
			gate.remove(this);
		}
	}

}
