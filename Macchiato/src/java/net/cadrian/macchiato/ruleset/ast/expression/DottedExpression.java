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

import net.cadrian.macchiato.interpreter.objects.MacObject;
import net.cadrian.macchiato.ruleset.ast.Expression;
import net.cadrian.macchiato.ruleset.ast.Node;
import net.cadrian.macchiato.ruleset.parser.Position;

public class DottedExpression implements Expression {

	public static interface Visitor extends Node.Visitor {
		void visitDottedExpression(DottedExpression dottedExpression);
	}

	private final Expression target;
	private final Identifier selector;

	public DottedExpression(final Expression target, final Identifier selector) {
		this.target = target;
		this.selector = selector;
	}

	@Override
	public Position position() {
		return target.position();
	}

	public Identifier getSelector() {
		return selector;
	}

	public Expression getTarget() {
		return target;
	}

	@Override
	public TypedExpression typed(final Class<? extends MacObject> type) {
		return new CheckedExpression(this, type);
	}

	@Override
	public void accept(final Node.Visitor v) {
		((Visitor) v).visitDottedExpression(this);
	}

	@Override
	public Expression simplify() {
		final Expression simplifyTarget = target.simplify();
		if (simplifyTarget == target) {
			return this;
		}
		return new DottedExpression(simplifyTarget, selector);
	}

	@Override
	public boolean isStatic() {
		return false;
	}

	@Override
	public Expression getStaticValue() {
		return this;
	}

	@Override
	public String toString() {
		return "{DottedExpression " + target + " :: " + selector + "}";
	}

}
