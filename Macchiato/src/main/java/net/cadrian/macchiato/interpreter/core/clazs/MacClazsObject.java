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
package net.cadrian.macchiato.interpreter.core.clazs;

import java.util.LinkedHashMap;
import java.util.Map;

import net.cadrian.macchiato.interpreter.Clazs;
import net.cadrian.macchiato.interpreter.Field;
import net.cadrian.macchiato.interpreter.Method;
import net.cadrian.macchiato.interpreter.objects.MacObject;
import net.cadrian.macchiato.ruleset.ast.Ruleset;
import net.cadrian.macchiato.ruleset.ast.expression.Identifier;

public class MacClazsObject implements MacObject {

	private final Clazs clazs;
	private final Map<String, MacObject> fieldValues = new LinkedHashMap<>();

	public MacClazsObject(final Clazs clazs) {
		this.clazs = clazs;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends MacObject, R extends MacObject> Field<T, R> getField(final Ruleset ruleset,
			final Identifier name) {
		return (Field<T, R>) clazs.getField(ruleset, name);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends MacObject> Method<T> getMethod(final Ruleset ruleset, final Identifier name) {
		return (Method<T>) clazs.getMethod(ruleset, name);
	}

	@Override
	public <T extends MacObject> T asIndexType(final Class<T> type) {
		if (type == getClass()) {
			return type.cast(this);
		}
		return null;
	}

	MacObject getFieldValue(final String name) {
		return fieldValues.get(name);
	}

	MacObject setFieldValue(final String name, final MacObject newValue) {
		return fieldValues.put(name, newValue);
	}

}
