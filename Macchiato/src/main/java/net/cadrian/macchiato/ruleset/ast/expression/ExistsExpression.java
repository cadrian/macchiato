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

import net.cadrian.macchiato.interpreter.objects.MacBoolean;
import net.cadrian.macchiato.interpreter.objects.MacObject;
import net.cadrian.macchiato.ruleset.ast.Expression;
import net.cadrian.macchiato.ruleset.ast.Node;
import net.cadrian.macchiato.ruleset.parser.Position;

public class ExistsExpression implements TypedExpression {

	private final Expression expression;
	private final Position position;

	@SuppressWarnings("PMD.ImplicitFunctionalInterface")
	public interface Visitor extends Node.Visitor {
		void visitExistsExpression(ExistsExpression existsExpression);
	}

	public ExistsExpression(final Position position, final Expression expression) {
		this.position = position;
		this.expression = expression;
	}

	public Expression getExpression() {
		return expression;
	}

	@Override
	public TypedExpression typed(final Class<? extends MacObject> type) {
		if (type.isAssignableFrom(MacBoolean.class)) {
			return this;
		}
		return null;
	}

	@Override
	public boolean isStatic() {
		return false;
	}

	@Override
	public Expression getStaticValue() {
		return null;
	}

	@Override
	public Position position() {
		return position;
	}

	@Override
	public void accept(final Node.Visitor v) {
		((Visitor) v).visitExistsExpression(this);
	}

	@Override
	public Class<? extends MacObject> getType() {
		return MacBoolean.class;
	}

	@Override
	public TypedExpression simplify() {
		final Expression simplifyExpression = expression.simplify();
		if (simplifyExpression.equals(expression)) {
			return this;
		}
		return new ExistsExpression(position, simplifyExpression);
	}

	@Override
	public String toString() {
		return "{Exists:" + expression + "}";
	}

}
