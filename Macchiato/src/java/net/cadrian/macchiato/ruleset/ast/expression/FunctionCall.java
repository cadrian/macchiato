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
import net.cadrian.macchiato.ruleset.ast.AbstractCall;
import net.cadrian.macchiato.ruleset.ast.Expression;
import net.cadrian.macchiato.ruleset.ast.Node;
import net.cadrian.macchiato.ruleset.parser.Position;

public class FunctionCall extends AbstractCall implements Expression {

	public static interface Visitor extends Node.Visitor {
		void visitFunctionCall(FunctionCall functionCall);
	}

	public FunctionCall(final Position position, final Expression target, final Identifier name) {
		super(position, target, name);
	}

	@Override
	public TypedExpression typed(final Class<? extends MacObject> type) {
		return new CheckedExpression(this, type);
	}

	@Override
	public void accept(final Node.Visitor v) {
		((Visitor) v).visitFunctionCall(this);
	}

	@Override
	public Expression simplify() {
		final Expression simplifyTarget = target == null ? null : target.simplify();
		boolean changed = simplifyTarget != target;
		final FunctionCall result = new FunctionCall(position, simplifyTarget, name);
		for (final Expression arg : arguments) {
			final Expression simplifyArg = arg.simplify();
			result.add(simplifyArg);
			changed |= simplifyArg != arg;
		}
		return changed ? result : this;
	}

	@Override
	public boolean isStatic() {
		return false;
	}

	@Override
	public Expression getStaticValue() {
		return null;
	}

}
