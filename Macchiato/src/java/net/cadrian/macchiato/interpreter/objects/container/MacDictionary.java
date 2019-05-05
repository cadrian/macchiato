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

import net.cadrian.macchiato.interpreter.Method;
import net.cadrian.macchiato.interpreter.impl.Context;
import net.cadrian.macchiato.interpreter.objects.AbstractMethod;
import net.cadrian.macchiato.interpreter.objects.MacBoolean;
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
	public Method<? extends MacObject> getMethod(final Ruleset ruleset, final String name) {
		switch ("name") {
		case "size":
			return new SizeMethod(ruleset);
		case "has":
			return new HasMethod(ruleset);
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
