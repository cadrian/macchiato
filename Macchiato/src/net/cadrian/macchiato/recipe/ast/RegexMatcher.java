package net.cadrian.macchiato.recipe.ast;

import net.cadrian.macchiato.recipe.ast.expression.Binary;
import net.cadrian.macchiato.recipe.ast.expression.TypedExpression;

public class RegexMatcher extends Binary implements TypedExpression {

	public static interface Visitor extends Node.Visitor {
		void visit(RegexMatcher regexMatcher);
	}

	private final TypedExpression leftOperand;
	private final TypedExpression rightOperand;

	public RegexMatcher(final TypedExpression leftOperand, final TypedExpression rightOperand) {
		super(Binary.Operator.MATCH);
		this.leftOperand = leftOperand;
		this.rightOperand = rightOperand;
	}

	public TypedExpression getLeftOperand() {
		return leftOperand;
	}

	public TypedExpression getRightOperand() {
		return rightOperand;
	}

	@Override
	public int position() {
		return leftOperand.position();
	}

	@Override
	public TypedExpression typed(final Class<?> type) {
		if (type.isAssignableFrom(Boolean.class)) {
			return this;
		}
		return null;
	}

	@Override
	public Class<?> getType() {
		return Boolean.class;
	}

	@Override
	public void accept(final Node.Visitor v) {
		((Visitor) v).visit(this);
	}

}
