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

@SuppressWarnings("PMD.GodClass")
public class TypedBinary extends Binary implements TypedExpression {

	private static final Logger LOGGER = LoggerFactory.getLogger(TypedBinary.class);

	private final TypedExpression leftOperand;
	private final TypedExpression rightOperand;
	private final Class<? extends MacObject> resultType;

	@SuppressWarnings("PMD.ImplicitFunctionalInterface")
	public interface Visitor extends Node.Visitor {
		void visitTypedBinary(TypedBinary typedBinary);
	}

	public TypedBinary(final TypedExpression leftOperand, final Binary.Operator operator,
			final TypedExpression rightOperand, final Class<? extends MacObject> resultType) {
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
	public Class<? extends MacObject> getType() {
		return resultType;
	}

	@Override
	public Position position() {
		return leftOperand.position();
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
	public void accept(final Node.Visitor v) {
		((Visitor) v).visitTypedBinary(this);
	}

	@Override
	public TypedExpression simplify() {
		final TypedExpression simplifyLeftOperand = leftOperand.simplify();
		final TypedExpression simplifyRightOperand = rightOperand.simplify();
		final TypedBinary result;
		if (leftOperand.equals(simplifyLeftOperand) && rightOperand.equals(simplifyRightOperand)) {
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
	@SuppressWarnings("PMD.CyclomaticComplexity")
	public Expression getStaticValue() {
		final Expression left = leftOperand.getStaticValue();
		final Expression right = rightOperand.getStaticValue();
		switch (operator) {
		case AND:
			return getStaticAnd(left, right);
		case OR:
			return getStaticOr(left, right);
		case XOR:
			return getStaticXor(left, right);
		case LT:
			return getStaticLessThan(left, right);
		case LE:
			return getStaticLessOrEqual(left, right);
		case EQ:
			return getStaticEqual(left, right);
		case NE:
			return getStaticNotEqual(left, right);
		case GE:
			return getStaticGreaterOrEqual(left, right);
		case GT:
			return getStaticGreaterThan(left, right);
		case MATCH:
			return getStaticMatch(left, right);
		case ADD:
			return getStaticAdd(left, right);
		case SUBTRACT:
			return getStaticSubtract(left, right);
		case MULTIPLY:
			return getStaticMultiply(left, right);
		case DIVIDE:
			return getStaticDivide(left, right);
		case REMAINDER:
			return getStaticRemainder(left, right);
		case POWER:
			return getStaticPower(left, right);
		}
		// never reached
		return null;
	}

	private Expression getStaticAnd(final Expression left, final Expression right) {
		final ManifestBoolean l = (ManifestBoolean) left;
		if (!l.getValue()) {
			return l;
		}
		return right;
	}

	private Expression getStaticOr(final Expression left, final Expression right) {
		final ManifestBoolean l = (ManifestBoolean) left;
		if (l.getValue()) {
			return l;
		}
		return right;
	}

	private Expression getStaticXor(final Expression left, final Expression right) {
		final ManifestBoolean l = (ManifestBoolean) left;
		final ManifestBoolean r = (ManifestBoolean) right;
		return new ManifestBoolean(position(), !l.getValue().equals(r.getValue()));
	}

	private Expression getStaticLessThan(final Expression left, final Expression right) {
		return new ManifestBoolean(position(), compare(left, right) < 0);
	}

	private Expression getStaticLessOrEqual(final Expression left, final Expression right) {
		return new ManifestBoolean(position(), compare(left, right) <= 0);
	}

	private Expression getStaticEqual(final Expression left, final Expression right) {
		return new ManifestBoolean(position(), compare(left, right) == 0);
	}

	private Expression getStaticNotEqual(final Expression left, final Expression right) {
		return new ManifestBoolean(position(), compare(left, right) != 0);
	}

	private Expression getStaticGreaterOrEqual(final Expression left, final Expression right) {
		return new ManifestBoolean(position(), compare(left, right) >= 0);
	}

	private Expression getStaticGreaterThan(final Expression left, final Expression right) {
		return new ManifestBoolean(position(), compare(left, right) > 0);
	}

	private Expression getStaticMatch(final Expression left, final Expression right) {
		final ManifestString l = (ManifestString) left;
		final ManifestRegex r = (ManifestRegex) right;
		return new ManifestBoolean(position(), r.getValue().matcher(l.getValue()).matches());
	}

	private Expression getStaticAdd(final Expression left, final Expression right) {
		if (left instanceof ManifestString) {
			final ManifestString l = (ManifestString) left;
			final ManifestString r = (ManifestString) right;
			return new ManifestString(position(), l.getValue() + r.getValue());
		}
		final ManifestNumeric l = (ManifestNumeric) left;
		final ManifestNumeric r = (ManifestNumeric) right;
		return new ManifestNumeric(position(), l.getValue().add(r.getValue()));
	}

	private Expression getStaticSubtract(final Expression left, final Expression right) {
		final ManifestNumeric l = (ManifestNumeric) left;
		final ManifestNumeric r = (ManifestNumeric) right;
		return new ManifestNumeric(position(), l.getValue().subtract(r.getValue()));
	}

	private Expression getStaticMultiply(final Expression left, final Expression right) {
		final ManifestNumeric l = (ManifestNumeric) left;
		final ManifestNumeric r = (ManifestNumeric) right;
		return new ManifestNumeric(position(), l.getValue().multiply(r.getValue()));
	}

	private Expression getStaticDivide(final Expression left, final Expression right) {
		final ManifestNumeric l = (ManifestNumeric) left;
		final ManifestNumeric r = (ManifestNumeric) right;
		return new ManifestNumeric(position(), l.getValue().divide(r.getValue()));
	}

	private Expression getStaticRemainder(final Expression left, final Expression right) {
		final ManifestNumeric l = (ManifestNumeric) left;
		final ManifestNumeric r = (ManifestNumeric) right;
		return new ManifestNumeric(position(), l.getValue().remainder(r.getValue()));
	}

	private Expression getStaticPower(final Expression left, final Expression right) {
		final ManifestNumeric l = (ManifestNumeric) left;
		final ManifestNumeric r = (ManifestNumeric) right;
		return new ManifestNumeric(position(), l.getValue().pow(r.getValue().intValueExact()));
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
