package net.cadrian.macchiato.recipe.ast.expression;

import net.cadrian.macchiato.recipe.ast.Node;

public class TypedUnary extends Unary implements TypedExpression {

	public static interface Visitor extends Node.Visitor {
		void visitTypedUnary(TypedUnary typedUnary);
	}

	private final TypedExpression operand;
	private final Class<?> resultType;
	private final int position;

	public TypedUnary(final int position, final Unary.Operator operator, final TypedExpression operand,
			final Class<?> resultType) {
		super(operator);
		this.position = position;
		this.operand = operand;
		this.resultType = resultType;
	}

	public TypedExpression getOperand() {
		return operand;
	}

	@Override
	public Class<?> getType() {
		return resultType;
	}

	@Override
	public int position() {
		return position;
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
		((Visitor) v).visitTypedUnary(this);
	}

	@Override
	public String toString() {
		return "{TypedUnary " + resultType.getSimpleName() + ": " + getOperator() + " " + operand + "}#";
	}

}
