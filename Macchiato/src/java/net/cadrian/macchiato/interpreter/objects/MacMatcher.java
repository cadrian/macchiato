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

import net.cadrian.macchiato.interpreter.InterpreterException;
import net.cadrian.macchiato.interpreter.Method;
import net.cadrian.macchiato.interpreter.impl.Context;
import net.cadrian.macchiato.ruleset.ast.Ruleset;

public class MacMatcher implements MacObject {

	private static class MatchesMethod extends AbstractMethod<MacMatcher> {

		@SuppressWarnings("unchecked")
		private static final Class<? extends MacObject>[] ARG_TYPES = new Class[0];
		private static final String[] ARG_NAMES = {};

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
			context.set("result", result);
		}

		@Override
		public String name() {
			return "matches";
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
		public Class<MacBoolean> getResultType() {
			return MacBoolean.class;
		}

	};

	private static class GroupMethod extends AbstractMethod<MacMatcher> {

		@SuppressWarnings("unchecked")
		private static final Class<? extends MacObject>[] ARG_TYPES = new Class[] { MacComparable.class };
		private static final String[] ARG_NAMES = { "group" };

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
			final MacComparable<?> group = context.get("group");
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
			context.set("result", result);
		}

		@Override
		public String name() {
			return "group";
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
		public Class<MacString> getResultType() {
			return MacString.class;
		}

	};

	private final Matcher value;

	public MacMatcher(final Matcher value) {
		this.value = value;
	}

	@Override
	public Method<? extends MacObject> getMethod(final Ruleset ruleset, final String name) {
		switch (name) {
		case "matches":
			return new MatchesMethod(ruleset);
		case "group":
			return new GroupMethod(ruleset);
		}
		return null;
	}

}
