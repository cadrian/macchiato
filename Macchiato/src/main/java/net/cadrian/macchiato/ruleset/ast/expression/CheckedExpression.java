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
package net.cadrian.macchiato.ruleset.ast.expression;

import net.cadrian.macchiato.interpreter.InterpreterException;
import net.cadrian.macchiato.interpreter.objects.MacObject;
import net.cadrian.macchiato.ruleset.ast.Expression;
import net.cadrian.macchiato.ruleset.ast.Node;
import net.cadrian.macchiato.ruleset.parser.Position;

public class CheckedExpression implements TypedExpression {

	public interface Visitor extends Node.Visitor {
		void visitCheckedExpression(CheckedExpression e);
	}

	private final Class<? extends MacObject> type;
	private final Expression toCheck;

	public CheckedExpression(final Expression toCheck, final Class<? extends MacObject> type) {
		this.toCheck = toCheck;
		this.type = type;
	}

	@Override
	public TypedExpression typed(final Class<? extends MacObject> type) {
		return new CheckedExpression(toCheck, type);
	}

	@Override
	public Position position() {
		return toCheck.position();
	}

	@Override
	public Class<? extends MacObject> getType() {
		return type;
	}

	public Expression getToCheck() {
		return toCheck;
	}

	@Override
	public void accept(final Node.Visitor v) {
		((Visitor) v).visitCheckedExpression(this);
	}

	@Override
	public TypedExpression simplify() {
		final TypedExpression result;
		final Expression simplifyToCheck = toCheck.simplify();
		if (simplifyToCheck == toCheck) {
			result = this;
		} else {
			result = simplifyToCheck.typed(type);
			if (result == null) {
				throw new InterpreterException("invalid expression type: expected " + type.getSimpleName(), position());
			}
		}
		return result;
	}

	@Override
	public boolean isStatic() {
		return toCheck.isStatic();
	}

	@Override
	public Expression getStaticValue() {
		return toCheck.getStaticValue();
	}

	@Override
	public String toString() {
		return "{CheckedExpression " + type.getSimpleName() + ": " + toCheck + "}";
	}

}
