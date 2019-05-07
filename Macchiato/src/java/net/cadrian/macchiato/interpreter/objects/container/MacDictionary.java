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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.cadrian.macchiato.interpreter.Field;
import net.cadrian.macchiato.interpreter.InterpreterException;
import net.cadrian.macchiato.interpreter.Method;
import net.cadrian.macchiato.interpreter.impl.Context;
import net.cadrian.macchiato.interpreter.impl.LocalContext;
import net.cadrian.macchiato.interpreter.objects.AbstractMethod;
import net.cadrian.macchiato.interpreter.objects.MacBoolean;
import net.cadrian.macchiato.interpreter.objects.MacCallable;
import net.cadrian.macchiato.interpreter.objects.MacNumber;
import net.cadrian.macchiato.interpreter.objects.MacObject;
import net.cadrian.macchiato.interpreter.objects.MacString;
import net.cadrian.macchiato.ruleset.ast.Ruleset;

public class MacDictionary implements MacContainer<MacString> {

	private static final ThreadLocal<Set<MacDictionary>> TO_STRING_GATE = new ThreadLocal<Set<MacDictionary>>() {
		@Override
		protected Set<MacDictionary> initialValue() {
			return new HashSet<>();
		}
	};

	private static class SizeMethod extends AbstractMethod<MacDictionary> {

		SizeMethod(final Ruleset ruleset) {
			super(ruleset);
		}

		@Override
		public String name() {
			return "size";
		}

		@SuppressWarnings("unchecked")
		@Override
		public Class<? extends MacObject>[] getArgTypes() {
			return new Class[0];
		}

		@Override
		public String[] getArgNames() {
			return new String[0];
		}

		@Override
		public Class<? extends MacObject> getResultType() {
			return MacNumber.class;
		}

		@Override
		public void run(final MacDictionary target, final Context context, final int position) {
			context.set("result", MacNumber.valueOf(target.size()));
		}

		@Override
		public Class<MacDictionary> getTargetType() {
			return MacDictionary.class;
		}

	};

	private static class HasMethod extends AbstractMethod<MacDictionary> {

		@SuppressWarnings("unchecked")
		private static final Class<? extends MacObject>[] ARG_TYPES = new Class[] { MacString.class };
		private static final String[] ARG_NAMES = { "index" };

		HasMethod(final Ruleset ruleset) {
			super(ruleset);
		}

		@Override
		public Class<MacDictionary> getTargetType() {
			return MacDictionary.class;
		}

		@Override
		public void run(final MacDictionary target, final Context context, final int position) {
			final MacString index = context.get("index");
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

	private static class ForEachMethod extends AbstractMethod<MacDictionary> {

		@SuppressWarnings("unchecked")
		private static final Class<? extends MacObject>[] ARG_TYPES = new Class[] { MacCallable.class };
		private static final String[] ARG_NAMES = { "callable" };

		protected ForEachMethod(final Ruleset ruleset) {
			super(ruleset);
		}

		@Override
		public Class<MacDictionary> getTargetType() {
			return MacDictionary.class;
		}

		@Override
		public void run(final MacDictionary target, final Context context, final int position) {
			final MacCallable callable = context.get("callable");
			final String[] argNames = callable.getArgNames();
			switch (argNames.length) {
			case 1:
				for (final MacObject value : target.dictionary.values()) {
					final LocalContext c = new LocalContext(context, getRuleset());
					c.declareLocal(argNames[0]);
					c.set(argNames[0], value);
					callable.invoke(c, position);
				}
				break;
			case 2:
				for (final Map.Entry<MacString, MacObject> entry : target.dictionary.entrySet()) {
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

	private static class MapMethod extends AbstractMethod<MacDictionary> {

		@SuppressWarnings("unchecked")
		private static final Class<? extends MacObject>[] ARG_TYPES = new Class[] { MacCallable.class,
				MacObject.class };
		private static final String[] ARG_NAMES = { "callable", "seed" };

		protected MapMethod(final Ruleset ruleset) {
			super(ruleset);
		}

		@Override
		public Class<MacDictionary> getTargetType() {
			return MacDictionary.class;
		}

		@Override
		public void run(final MacDictionary target, final Context context, final int position) {
			final MacCallable callable = context.get("callable");
			final String[] argNames = callable.getArgNames();
			MacObject result = context.get("seed");
			switch (argNames.length) {
			case 2:
				for (final MacObject value : target.dictionary.values()) {
					final LocalContext c = new LocalContext(context, getRuleset());
					c.declareLocal(argNames[0]);
					c.declareLocal(argNames[1]);
					c.set(argNames[0], value);
					c.set(argNames[1], result);
					callable.invoke(c, position);
					result = c.get("result");
				}
				break;
			case 3:
				for (final Map.Entry<MacString, MacObject> entry : target.dictionary.entrySet()) {
					final LocalContext c = new LocalContext(context, getRuleset());
					c.declareLocal(argNames[0]);
					c.declareLocal(argNames[1]);
					c.declareLocal(argNames[2]);
					c.set(argNames[0], entry.getKey());
					c.set(argNames[1], entry.getValue());
					c.set(argNames[2], result);
					callable.invoke(c, position);
					result = c.get("result");
				}
				break;
			default:
				throw new InterpreterException(
						"invalid 'map' function call: the function must have exactly one or two arguments", position);
			}
			context.set("result", result);
		}

		@Override
		public String name() {
			return "map";
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
			return MacObject.class;
		}

	};

	private final Map<MacString, MacObject> dictionary = new HashMap<>();

	@Override
	public MacObject set(final MacString index, final MacObject value) {
		return dictionary.put(index, value);
	}

	@Override
	public MacObject get(final MacString index) {
		return dictionary.get(index);
	}

	@Override
	public Iterator<MacString> keys() {
		return dictionary.keySet().iterator();
	}

	@Override
	public int size() {
		return dictionary.size();
	}

	@Override
	public <T extends MacObject, R extends MacObject> Field<T, R> getField(final Ruleset ruleset, final String name) {
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends MacObject> Method<T> getMethod(final Ruleset ruleset, final String name) {
		switch (name) {
		case "size":
			return (Method<T>) new SizeMethod(ruleset);
		case "has":
			return (Method<T>) new HasMethod(ruleset);
		case "forEach":
			return (Method<T>) new ForEachMethod(ruleset);
		case "map":
			return (Method<T>) new MapMethod(ruleset);
		}
		return null;
	}

	@Override
	public String toString() {
		final Set<MacDictionary> gate = TO_STRING_GATE.get();
		if (gate.contains(this)) {
			return "RECURSIVE DICTIONARY";
		}

		gate.add(this);
		try {
			final StringBuilder result = new StringBuilder();
			result.append('{');
			for (final Map.Entry<MacString, MacObject> entry : dictionary.entrySet()) {
				if (result.length() > 1) {
					result.append(", ");
				}
				result.append(entry.getKey()).append('=').append(entry.getValue());
			}
			result.append('}');
			return result.toString();
		} finally {
			gate.remove(this);
		}
	}

}
