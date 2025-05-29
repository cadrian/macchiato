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

import java.util.concurrent.atomic.AtomicInteger;

import net.cadrian.macchiato.interpreter.objects.MacObject;
import net.cadrian.macchiato.ruleset.ast.Expression;
import net.cadrian.macchiato.ruleset.ast.Node;
import net.cadrian.macchiato.ruleset.parser.Position;

public class Old implements Expression {

	private static final AtomicInteger COUNTER = new AtomicInteger();

	public interface Visitor extends Node.Visitor {
		void visitOld(Old old);
	}

	private final int id;
	private final Expression expression;
	private final Position position;

	public Old(final Position position, final Expression expression) {
		id = COUNTER.getAndIncrement();
		this.position = position;
		this.expression = expression;
	}

	public int getId() {
		return id;
	}

	public Expression getExpression() {
		return expression;
	}

	@Override
	public Position position() {
		return position;
	}

	@Override
	public void accept(final Node.Visitor v) {
		((Visitor) v).visitOld(this);
	}

	@Override
	public TypedExpression typed(final Class<? extends MacObject> type) {
		return new CheckedExpression(this, type);
	}

	@Override
	public Expression simplify() {
		final Expression simplifiedExpression = expression.simplify();
		if (simplifiedExpression == expression) {
			return this;
		}
		return new Old(position, simplifiedExpression);
	}

	@Override
	public boolean isStatic() {
		return expression.isStatic();
	}

	@Override
	public Expression getStaticValue() {
		return expression.getStaticValue();
	}

}
