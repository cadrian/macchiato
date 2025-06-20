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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import net.cadrian.macchiato.interpreter.Field;
import net.cadrian.macchiato.interpreter.Identifiers;
import net.cadrian.macchiato.interpreter.InterpreterException;
import net.cadrian.macchiato.interpreter.Method;
import net.cadrian.macchiato.interpreter.core.Context;
import net.cadrian.macchiato.interpreter.core.LocalContext;
import net.cadrian.macchiato.interpreter.objects.AbstractMethod;
import net.cadrian.macchiato.interpreter.objects.MacBoolean;
import net.cadrian.macchiato.interpreter.objects.MacCallable;
import net.cadrian.macchiato.interpreter.objects.MacNumber;
import net.cadrian.macchiato.interpreter.objects.MacObject;
import net.cadrian.macchiato.interpreter.objects.MacString;
import net.cadrian.macchiato.ruleset.ast.Ruleset;
import net.cadrian.macchiato.ruleset.ast.expression.Identifier;
import net.cadrian.macchiato.ruleset.parser.Position;

public class MacDictionary implements MacContainer<MacString> {

	private static final ThreadLocal<Set<MacDictionary>> TO_STRING_GATE = ThreadLocal.withInitial(HashSet::new);

	private final Map<MacString, MacObject> dictionary = new LinkedHashMap<>();

	private static class SizeMethod extends AbstractMethod<MacDictionary> {

		private static final Identifier NAME = new Identifier("Size", Position.NONE);

		@SuppressWarnings("unchecked")
		private static final Class<? extends MacObject>[] ARG_TYPES = new Class[0];
		private static final Identifier[] ARG_NAMES = {};

		SizeMethod(final Ruleset ruleset) {
			super(ruleset);
		}

		@Override
		public Identifier name() {
			return NAME;
		}

		@Override
		public Class<? extends MacObject>[] getArgTypes() {
			return ARG_TYPES.clone();
		}

		@Override
		public Identifier[] getArgNames() {
			return ARG_NAMES.clone();
		}

		@Override
		public Class<? extends MacObject> getResultType() {
			return MacNumber.class;
		}

		@Override
		public void run(final MacDictionary target, final Context context, final Position position) {
			context.set(Identifiers.RESULT, MacNumber.valueOf(target.size()));
		}

		@Override
		public Class<MacDictionary> getTargetType() {
			return MacDictionary.class;
		}

	}

	private static class HasMethod extends AbstractMethod<MacDictionary> {

		private static final Identifier NAME = new Identifier("Has", Position.NONE);
		private static final Identifier ARG_INDEX = new Identifier("index", Position.NONE);

		@SuppressWarnings("unchecked")
		private static final Class<? extends MacObject>[] ARG_TYPES = new Class[] { MacString.class };
		private static final Identifier[] ARG_NAMES = { ARG_INDEX };

		HasMethod(final Ruleset ruleset) {
			super(ruleset);
		}

		@Override
		public Class<MacDictionary> getTargetType() {
			return MacDictionary.class;
		}

		@Override
		public void run(final MacDictionary target, final Context context, final Position position) {
			final MacString index = context.get(ARG_INDEX);
			context.set(Identifiers.RESULT, MacBoolean.valueOf(target.get(index) != null));
		}

		@Override
		public Identifier name() {
			return NAME;
		}

		@Override
		public Class<? extends MacObject>[] getArgTypes() {
			return ARG_TYPES.clone();
		}

		@Override
		public Identifier[] getArgNames() {
			return ARG_NAMES.clone();
		}

		@Override
		public Class<MacObject> getResultType() {
			return MacObject.class;
		}

	}

	private static class ForEachMethod extends AbstractMethod<MacDictionary> {

		private static final Identifier NAME = new Identifier("ForEach", Position.NONE);
		private static final Identifier ARG_CALLABLE = new Identifier("callable", Position.NONE);

		@SuppressWarnings("unchecked")
		private static final Class<? extends MacObject>[] ARG_TYPES = new Class[] { MacCallable.class };
		private static final Identifier[] ARG_NAMES = { ARG_CALLABLE };

		protected ForEachMethod(final Ruleset ruleset) {
			super(ruleset);
		}

		@Override
		public Class<MacDictionary> getTargetType() {
			return MacDictionary.class;
		}

		@Override
		public void run(final MacDictionary target, final Context context, final Position position) {
			final MacCallable callable = context.get(ARG_CALLABLE);
			final Identifier[] argNames = callable.getArgNames();
			switch (argNames.length) {
			case 1:
				for (final MacObject value : target.dictionary.values()) {
					final LocalContext c = context.newLocalContext(getRuleset());
					c.declareLocal(argNames[0]);
					c.set(argNames[0], value);
					callable.invoke(c, position);
				}
				break;
			case 2:
				for (final Map.Entry<MacString, MacObject> entry : target.dictionary.entrySet()) {
					final LocalContext c = context.newLocalContext(getRuleset());
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
		public Identifier name() {
			return NAME;
		}

		@Override
		public Class<? extends MacObject>[] getArgTypes() {
			return ARG_TYPES.clone();
		}

		@Override
		public Identifier[] getArgNames() {
			return ARG_NAMES.clone();
		}

		@Override
		public Class<? extends MacObject> getResultType() {
			return null;
		}

	}

	private static class MapMethod extends AbstractMethod<MacDictionary> {

		private static final Identifier NAME = new Identifier("Map", Position.NONE);
		private static final Identifier ARG_CALLABLE = new Identifier("callable", Position.NONE);
		private static final Identifier ARG_SEED = new Identifier("seed", Position.NONE);

		@SuppressWarnings("unchecked")
		private static final Class<? extends MacObject>[] ARG_TYPES = new Class[] { MacCallable.class,
				MacObject.class };
		private static final Identifier[] ARG_NAMES = { ARG_CALLABLE, ARG_SEED };

		protected MapMethod(final Ruleset ruleset) {
			super(ruleset);
		}

		@Override
		public Class<MacDictionary> getTargetType() {
			return MacDictionary.class;
		}

		@Override
		public void run(final MacDictionary target, final Context context, final Position position) {
			final MacCallable callable = context.get(ARG_CALLABLE);
			final Identifier[] argNames = callable.getArgNames();
			MacObject result = context.get(ARG_SEED);
			switch (argNames.length) {
			case 2:
				for (final MacObject value : target.dictionary.values()) {
					final LocalContext c = context.newLocalContext(getRuleset());
					c.declareLocal(argNames[0]);
					c.declareLocal(argNames[1]);
					c.set(argNames[0], value);
					c.set(argNames[1], result);
					callable.invoke(c, position);
					result = c.get(Identifiers.RESULT);
				}
				break;
			case 3:
				for (final Map.Entry<MacString, MacObject> entry : target.dictionary.entrySet()) {
					final LocalContext c = context.newLocalContext(getRuleset());
					c.declareLocal(argNames[0]);
					c.declareLocal(argNames[1]);
					c.declareLocal(argNames[2]);
					c.set(argNames[0], entry.getKey());
					c.set(argNames[1], entry.getValue());
					c.set(argNames[2], result);
					callable.invoke(c, position);
					result = c.get(Identifiers.RESULT);
				}
				break;
			default:
				throw new InterpreterException(
						"invalid 'map' function call: the function must have exactly one or two arguments", position);
			}
			context.set(Identifiers.RESULT, result);
		}

		@Override
		public Identifier name() {
			return NAME;
		}

		@Override
		public Class<? extends MacObject>[] getArgTypes() {
			return ARG_TYPES.clone();
		}

		@Override
		public Identifier[] getArgNames() {
			return ARG_NAMES.clone();
		}

		@Override
		public Class<? extends MacObject> getResultType() {
			return MacObject.class;
		}

	}

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
	public <T extends MacObject, R extends MacObject> Field<T, R> getField(final Ruleset ruleset,
			final Identifier name) {
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends MacObject> Method<T> getMethod(final Ruleset ruleset, final Identifier name) {
		switch (name.getName()) {
		case "Size":
			return (Method<T>) new SizeMethod(ruleset);
		case "Has":
			return (Method<T>) new HasMethod(ruleset);
		case "ForEach":
			return (Method<T>) new ForEachMethod(ruleset);
		case "Map":
			return (Method<T>) new MapMethod(ruleset);
		default:
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
