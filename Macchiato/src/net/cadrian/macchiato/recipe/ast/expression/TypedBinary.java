package net.cadrian.macchiato.recipe.ast.expression;

import net.cadrian.macchiato.recipe.ast.Node;

public class TypedBinary extends Binary implements TypedExpression {

	public static interface Visitor extends Node.Visitor {
		void visit(TypedBinary typedBinary);
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
		return null;
	}

	@Override
	public void accept(final Node.Visitor v) {
		((Visitor) v).visit(this);
	}

}
