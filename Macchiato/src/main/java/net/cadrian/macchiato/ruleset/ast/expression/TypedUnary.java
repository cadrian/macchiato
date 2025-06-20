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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.cadrian.macchiato.interpreter.objects.MacObject;
import net.cadrian.macchiato.ruleset.ast.Expression;
import net.cadrian.macchiato.ruleset.ast.Node;
import net.cadrian.macchiato.ruleset.parser.Position;

public class TypedUnary extends Unary implements TypedExpression {

	private static final Logger LOGGER = LoggerFactory.getLogger(TypedUnary.class);

	private final TypedExpression operand;
	private final Class<? extends MacObject> resultType;
	private final Position position;

	@SuppressWarnings("PMD.ImplicitFunctionalInterface")
	public interface Visitor extends Node.Visitor {
		void visitTypedUnary(TypedUnary typedUnary);
	}

	public TypedUnary(final Position position, final Unary.Operator operator, final TypedExpression operand,
			final Class<? extends MacObject> resultType) {
		super(operator);
		this.position = position;
		this.operand = operand;
		this.resultType = resultType;
	}

	public TypedExpression getOperand() {
		return operand;
	}

	@Override
	public Class<? extends MacObject> getType() {
		return resultType;
	}

	@Override
	public Position position() {
		return position;
	}

	@Override
	public TypedExpression typed(final Class<? extends MacObject> type) {
		if (type.isAssignableFrom(resultType)) {
			return this;
		}
		if (resultType.isAssignableFrom(type)) {
			return new CheckedExpression(this, type);
		}
		return null;
	}

	@Override
	public TypedExpression simplify() {
		final TypedExpression simplifyOperand = operand.simplify();
		final TypedUnary result;
		if (operand.equals(simplifyOperand)) {
			result = this;
		} else {
			result = new TypedUnary(position(), operator, simplifyOperand, resultType);
		}
		if (result.isStatic()) {
			LOGGER.debug("Simplify unary {}", operator);
			return result.getStaticValue().typed(resultType);
		}
		return result;
	}

	@Override
	public boolean isStatic() {
		return operand.isStatic();
	}

	@Override
	public Expression getStaticValue() {
		final Expression op = operand.getStaticValue();
		switch (operator) {
		case NOT:
			return getStaticNot(op);
		case MINUS:
			return getStaticMinus(op);
		}
		// never reached
		return null;
	}

	private Expression getStaticNot(final Expression op) {
		final ManifestBoolean o = (ManifestBoolean) op;
		return new ManifestBoolean(position(), !o.getValue());
	}

	private Expression getStaticMinus(final Expression op) {
		final ManifestNumeric o = (ManifestNumeric) op;
		return new ManifestNumeric(position(), o.getValue().negate());
	}

	@Override
	public void accept(final Node.Visitor v) {
		((Visitor) v).visitTypedUnary(this);
	}

	@Override
	public String toString() {
		return "{TypedUnary " + resultType.getSimpleName() + ": " + getOperator() + " " + operand + "}#";
	}

}
