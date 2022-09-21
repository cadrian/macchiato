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
import net.cadrian.macchiato.interpreter.InterpreterException;
import net.cadrian.macchiato.interpreter.core.Context;
import net.cadrian.macchiato.ruleset.ast.Ruleset;
import net.cadrian.macchiato.ruleset.ast.expression.Identifier;
import net.cadrian.macchiato.ruleset.parser.Position;

class ReadOnlyField<T extends MacObject, R extends MacObject> implements Field<T, R> {

	private final Identifier name;
	private final Ruleset ruleset;
	private final Class<T> targetType;
	private final Class<R> resultType;

	private final R value;

	ReadOnlyField(final Identifier name, final Ruleset ruleset, final Class<T> targetType, final Class<R> resultType,
			final R initialValue) {
		this.name = name;
		this.ruleset = ruleset;
		this.targetType = targetType;
		this.resultType = resultType;
		this.value = initialValue;
	}

	@Override
	public Identifier name() {
		return name;
	}

	@Override
	public Class<R> getResultType() {
		return resultType;
	}

	@Override
	public Ruleset getRuleset() {
		return ruleset;
	}

	@Override
	public Class<T> getTargetType() {
		return targetType;
	}

	@Override
	public R get(final T target, final Context context, final Position position) {
		return value;
	}

	@Override
	public R set(final T target, final Context context, final Position position, final R newValue) {
		throw new InterpreterException("Field " + name + " cannot be set", position);
	}

}
