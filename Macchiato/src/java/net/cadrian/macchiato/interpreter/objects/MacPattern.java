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

import net.cadrian.macchiato.interpreter.Method;
import net.cadrian.macchiato.interpreter.impl.Context;
import net.cadrian.macchiato.ruleset.ast.Ruleset;

public class MacPattern implements MacObject {

	private static final Map<Pattern, MacPattern> CACHE = new ConcurrentHashMap<>();

	private static class MatcherMethod extends AbstractMethod<MacPattern> {

		@SuppressWarnings("unchecked")
		private static final Class<? extends MacObject>[] ARG_TYPES = new Class[] { MacString.class };
		private static final String[] ARG_NAMES = { "string" };

		MatcherMethod(final Ruleset ruleset) {
			super(ruleset);
		}

		@Override
		public Class<MacPattern> getTargetType() {
			return MacPattern.class;
		}

		@Override
		public void run(final MacPattern target, final Context context, final int position) {
			final MacString string = context.get("string");
			final MacMatcher result = new MacMatcher(target.value.matcher(string.getValue()));
			context.set("result", result);
		}

		@Override
		public String name() {
			return "matcher";
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
	public Method<? extends MacObject> getMethod(final Ruleset ruleset, final String name) {
		switch (name) {
		case "matcher":
			return new MatcherMethod(ruleset);
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
