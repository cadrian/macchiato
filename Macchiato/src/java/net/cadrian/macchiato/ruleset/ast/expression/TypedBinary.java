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

import net.cadrian.macchiato.ruleset.ast.Expression;
import net.cadrian.macchiato.ruleset.ast.Node;

public class TypedBinary extends Binary implements TypedExpression {

	private static final Logger LOGGER = LoggerFactory.getLogger(TypedBinary.class);

	public static interface Visitor extends Node.Visitor {
		void visitTypedBinary(TypedBinary typedBinary);
	}

	private final TypedExpression leftOperand;
	private final TypedExpression rightOperand;
	private final Class<?> resultType;

	public TypedBinary(final TypedExpression leftOperand, final Binary.Operator operator,
			final TypedExpression rightOperand, final Class<?> resultType) {
		super(operator);
		this.leftOperand = leftOperand;
		this.rightOperand = rightOperand;
		this.resultType = resultType;
	}

	public TypedExpression getLeftOperand() {
		return leftOperand;
	}

	public TypedExpression getRightOperand() {
		return rightOperand;
	}

	@Override
	public Class<?> getType() {
		return resultType;
	}

	@Override
	public int position() {
		return leftOperand.position();
	}

	@Override
	public TypedExpression typed(final Class<?> type) {
		if (type.isAssignableFrom(resultType)) {
			return this;
		}
		if (resultType.isAssignableFrom(type)) {
			return new CheckedExpression(this, type);
		}
		return null;
	}

	@Override
	public void accept(final Node.Visitor v) {
		((Visitor) v).visitTypedBinary(this);
	}

	@Override
	public TypedExpression simplify() {
		final TypedExpression simplifyLeftOperand = leftOperand.simplify();
		final TypedExpression simplifyRightOperand = rightOperand.simplify();
		final TypedBinary result;
		if (leftOperand == simplifyLeftOperand && rightOperand == simplifyRightOperand) {
			result = this;
		} else {
			result = new TypedBinary(simplifyLeftOperand, operator, simplifyRightOperand, resultType);
		}
		if (result.isStatic()) {
			LOGGER.debug("Simplify binary {}", operator);
			return result.getStaticValue().typed(resultType);
		}
		return result;
	}

	@Override
	public boolean isStatic() {
		return leftOperand.isStatic() && rightOperand.isStatic();
	}

	@Override
	public Expression getStaticValue() {
		final Expression left = leftOperand.getStaticValue();
		final Expression right = rightOperand.getStaticValue();
		switch (operator) {
		case AND: {
			final ManifestBoolean l = (ManifestBoolean) left;
			if (!l.getValue()) {
				return l;
			}
			final ManifestBoolean r = (ManifestBoolean) right;
			return r;
		}
		case OR: {
			final ManifestBoolean l = (ManifestBoolean) left;
			if (l.getValue()) {
				return l;
			}
			final ManifestBoolean r = (ManifestBoolean) right;
			return r;
		}
		case XOR: {
			final ManifestBoolean l = (ManifestBoolean) left;
			final ManifestBoolean r = (ManifestBoolean) right;
			return new ManifestBoolean(position(), l.getValue() != r.getValue());
		}
		case LT: {
			return new ManifestBoolean(position(), compare(left, right) < 0);
		}
		case LE: {
			return new ManifestBoolean(position(), compare(left, right) <= 0);
		}
		case EQ: {
			return new ManifestBoolean(position(), compare(left, right) == 0);
		}
		case NE: {
			return new ManifestBoolean(position(), compare(left, right) != 0);
		}
		case GE: {
			return new ManifestBoolean(position(), compare(left, right) >= 0);
		}
		case GT: {
			return new ManifestBoolean(position(), compare(left, right) > 0);
		}
		case MATCH: {
			final ManifestString l = (ManifestString) left;
			final ManifestRegex r = (ManifestRegex) right;
			return new ManifestBoolean(position(), r.getValue().matcher(l.getValue()).matches());
		}
		case ADD: {
			if (left instanceof ManifestString) {
				final ManifestString l = (ManifestString) left;
				final ManifestString r = (ManifestString) right;
				return new ManifestString(position(), l.getValue() + r.getValue());
			} else {
				final ManifestNumeric l = (ManifestNumeric) left;
				final ManifestNumeric r = (ManifestNumeric) right;
				return new ManifestNumeric(position(), l.getValue().add(r.getValue()));
			}
		}
		case SUBTRACT: {
			final ManifestNumeric l = (ManifestNumeric) left;
			final ManifestNumeric r = (ManifestNumeric) right;
			return new ManifestNumeric(position(), l.getValue().subtract(r.getValue()));
		}
		case MULTIPLY: {
			final ManifestNumeric l = (ManifestNumeric) left;
			final ManifestNumeric r = (ManifestNumeric) right;
			return new ManifestNumeric(position(), l.getValue().multiply(r.getValue()));
		}
		case DIVIDE: {
			final ManifestNumeric l = (ManifestNumeric) left;
			final ManifestNumeric r = (ManifestNumeric) right;
			return new ManifestNumeric(position(), l.getValue().divide(r.getValue()));
		}
		case REMAINDER: {
			final ManifestNumeric l = (ManifestNumeric) left;
			final ManifestNumeric r = (ManifestNumeric) right;
			return new ManifestNumeric(position(), l.getValue().remainder(r.getValue()));
		}
		case POWER: {
			final ManifestNumeric l = (ManifestNumeric) left;
			final ManifestNumeric r = (ManifestNumeric) right;
			return new ManifestNumeric(position(), l.getValue().pow(r.getValue().intValueExact()));
		}
		default:
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	private <T extends Comparable<T>> int compare(final Expression left, final Expression right) {
		final ManifestExpression<T> l = (ManifestExpression<T>) left;
		final ManifestExpression<T> r = (ManifestExpression<T>) right;
		return l.getValue().compareTo(r.getValue());
	}

	@Override
	public String toString() {
		return "{TypedBinary " + resultType.getSimpleName() + ": " + leftOperand + " " + getOperator() + " "
				+ rightOperand + "}";
	}

}
