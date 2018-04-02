package net.cadrian.macchiato.recipe.ast;

import java.util.regex.Pattern;

public class RegexMatcher extends Binary {

	public static interface Visitor extends Node.Visitor {
		void visit(RegexMatcher regexMatcher);
	}

	private final TypedExpression<String> leftOperand;
	private final TypedExpression<Pattern> rightOperand;

	public RegexMatcher(final TypedExpression<String> leftOperand, final TypedExpression<Pattern> rightOperand) {
		super(Binary.Operator.MATCH);
		this.leftOperand = leftOperand;
		this.rightOperand = rightOperand;
	}

	public TypedExpression<String> getLeftOperand() {
		return leftOperand;
	}

	public TypedExpression<Pattern> getRightOperand() {
		return rightOperand;
	}

	@Override
	public int position() {
		return leftOperand.position();
	}

	@Override
	public <T> TypedExpression<T> typed(final Class<? extends T> type) {
		if (type.isAssignableFrom(Boolean.class)) {
			@SuppressWarnings("unchecked")
			final TypedExpression<T> result = (TypedExpression<T>) this;
			return result;
		}
		return null;
	}

	@Override
	public void accept(final Node.Visitor v) {
		((Visitor) v).visit(this);
	}

}
