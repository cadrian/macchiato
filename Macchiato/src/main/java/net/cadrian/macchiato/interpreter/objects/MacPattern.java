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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import net.cadrian.macchiato.interpreter.Field;
import net.cadrian.macchiato.interpreter.Identifiers;
import net.cadrian.macchiato.interpreter.Method;
import net.cadrian.macchiato.interpreter.core.Context;
import net.cadrian.macchiato.ruleset.ast.Ruleset;
import net.cadrian.macchiato.ruleset.ast.expression.Identifier;
import net.cadrian.macchiato.ruleset.parser.Position;

public class MacPattern implements MacObject {

	private static final Map<Pattern, MacPattern> CACHE = new ConcurrentHashMap<>();

	private static class MatcherMethod extends AbstractMethod<MacPattern> {

		private static final Identifier NAME = new Identifier("Matcher", Position.NONE);
		private static final Identifier ARG_STRING = new Identifier("string", Position.NONE);

		@SuppressWarnings("unchecked")
		private static final Class<? extends MacObject>[] ARG_TYPES = new Class[] { MacString.class };
		private static final Identifier[] ARG_NAMES = { ARG_STRING };

		MatcherMethod(final Ruleset ruleset) {
			super(ruleset);
		}

		@Override
		public Class<MacPattern> getTargetType() {
			return MacPattern.class;
		}

		@Override
		public void run(final MacPattern target, final Context context, final Position position) {
			final MacString string = context.get(ARG_STRING);
			final MacMatcher result = new MacMatcher(target.value.matcher(string.getValue()));
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
		public Class<MacMatcher> getResultType() {
			return MacMatcher.class;
		}

	};

	private final Pattern value;

	private MacPattern(final Pattern value) {
		this.value = value;
	}

	public Pattern getValue() {
		return value;
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
		case "Matcher":
			return (Method<T>) new MatcherMethod(ruleset);
		}
		return null;
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
		return value.toString();
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		return this == obj;
	}

	public static MacPattern valueOf(final Pattern value) {
		return CACHE.computeIfAbsent(value, MacPattern::new);
	}

	public MacBoolean matches(final MacString string) {
		return MacBoolean.valueOf(value.matcher(string.getValue()).matches());
	}

}
