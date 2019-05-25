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

import java.util.regex.Matcher;

import net.cadrian.macchiato.interpreter.Field;
import net.cadrian.macchiato.interpreter.Identifiers;
import net.cadrian.macchiato.interpreter.InterpreterException;
import net.cadrian.macchiato.interpreter.Method;
import net.cadrian.macchiato.interpreter.impl.Context;
import net.cadrian.macchiato.ruleset.ast.Ruleset;
import net.cadrian.macchiato.ruleset.ast.expression.Identifier;

public class MacMatcher implements MacObject {

	private static class MatchesMethod extends AbstractMethod<MacMatcher> {

		private static final Identifier NAME = new Identifier("Matches", 0);

		@SuppressWarnings("unchecked")
		private static final Class<? extends MacObject>[] ARG_TYPES = new Class[0];
		private static final Identifier[] ARG_NAMES = {};

		protected MatchesMethod(final Ruleset ruleset) {
			super(ruleset);
		}

		@Override
		public Class<MacMatcher> getTargetType() {
			return MacMatcher.class;
		}

		@Override
		public void run(final MacMatcher target, final Context context, final int position) {
			final MacBoolean result = MacBoolean.valueOf(target.value.matches());
			context.set(Identifiers.RESULT, result);
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
		public Class<MacBoolean> getResultType() {
			return MacBoolean.class;
		}

	};

	private static class GroupMethod extends AbstractMethod<MacMatcher> {

		private static final Identifier NAME = new Identifier("Group", 0);
		private static final Identifier ARG_GROUP = new Identifier("group", 0);

		@SuppressWarnings("unchecked")
		private static final Class<? extends MacObject>[] ARG_TYPES = new Class[] { MacComparable.class };
		private static final Identifier[] ARG_NAMES = { ARG_GROUP };

		protected GroupMethod(final Ruleset ruleset) {
			super(ruleset);
		}

		@Override
		public Class<MacMatcher> getTargetType() {
			return MacMatcher.class;
		}

		@Override
		public void run(final MacMatcher target, final Context context, final int position) {
			final MacString result;
			final MacComparable<?> group = context.get(ARG_GROUP);
			if (group == null) {
				throw new InterpreterException("group does not exist", position);
			}
			if (group instanceof MacNumber) {
				final MacNumber index = (MacNumber) group;
				result = MacString.valueOf(target.value.group(index.getValue().intValueExact()));
			} else if (group instanceof MacString) {
				final MacString name = (MacString) group;
				result = MacString.valueOf(target.value.group(name.getValue()));
			} else {
				throw new InterpreterException("invalid group value: must be a number or a string", position);
			}
			context.set(Identifiers.RESULT, result);
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
		public Class<MacString> getResultType() {
			return MacString.class;
		}

	};

	private final Matcher value;

	public MacMatcher(final Matcher value) {
		this.value = value;
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
		case "Matches":
			return (Method<T>) new MatchesMethod(ruleset);
		case "Group":
			return (Method<T>) new GroupMethod(ruleset);
		}
		return null;
	}

}
