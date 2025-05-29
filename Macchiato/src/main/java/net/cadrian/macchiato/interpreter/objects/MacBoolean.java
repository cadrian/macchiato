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

import net.cadrian.macchiato.interpreter.Field;
import net.cadrian.macchiato.interpreter.Method;
import net.cadrian.macchiato.ruleset.ast.Ruleset;
import net.cadrian.macchiato.ruleset.ast.expression.Identifier;

public class MacBoolean implements MacComparable<MacBoolean> {

	public static final MacBoolean TRUE = new MacBoolean(true);
	public static final MacBoolean FALSE = new MacBoolean(false);

	private final boolean value;

	private MacBoolean(final boolean value) {
		this.value = value;
	}

	public boolean isTrue() {
		return value;
	}

	public boolean isFalse() {
		return !value;
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
		return Boolean.toString(value);
	}

	@Override
	public int compareTo(final MacBoolean other) {
		return value ? other.value ? 0 : 1 : other.value ? -1 : 0;
	}

	public static MacBoolean valueOf(final boolean b) {
		return b ? TRUE : FALSE;
	}

	@Override
	public int hashCode() {
		return value ? 13 : 0;
	}

	@Override
	public boolean equals(final Object obj) {
		return this == obj;
	}

	public MacBoolean and(final MacBoolean other) {
		return valueOf(value && other.value);
	}

	public MacBoolean or(final MacBoolean other) {
		return valueOf(value || other.value);
	}

	public MacBoolean xor(final MacBoolean other) {
		return valueOf(value != other.value);
	}

	public MacBoolean not() {
		return valueOf(!value);
	}

}
