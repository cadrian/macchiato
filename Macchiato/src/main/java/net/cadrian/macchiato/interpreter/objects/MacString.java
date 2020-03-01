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

import net.cadrian.macchiato.interpreter.Field;
import net.cadrian.macchiato.interpreter.Method;
import net.cadrian.macchiato.ruleset.ast.Ruleset;
import net.cadrian.macchiato.ruleset.ast.expression.Identifier;

public class MacString implements MacComparable<MacString> {

	private static final Map<String, MacString> CACHE = new ConcurrentHashMap<>();

	private final String value;

	private MacString(final String value) {
		final String v = value.intern();
		this.value = v;
	}

	public String getValue() {
		return value;
	}

	@Override
	public <T extends MacObject, R extends MacObject> Field<T, R> getField(final Ruleset ruleset,
			final Identifier name) {
		return null;
	}

	@Override
	public <T extends MacObject> Method<T> getMethod(final Ruleset ruleset, final Identifier name) {
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
		return value;
	}

	@Override
	public int compareTo(final MacString other) {
		return value.compareTo(other.value);
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		return this == obj;
	}

	public static MacString valueOf(final String value) {
		return CACHE.computeIfAbsent(value, MacString::new);
	}

	public MacString concat(final MacString other) {
		return valueOf(value.concat(other.value));
	}

}
