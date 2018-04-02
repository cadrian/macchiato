package net.cadrian.macchiato.recipe.ast;

public class FunctionCall extends AbstractCall implements Expression {

	public static interface Visitor extends Node.Visitor {
		void visit(FunctionCall functionCall);
	}

	public FunctionCall(final int position, final String name) {
		super(position, name);
	}

	@Override
	public <T> TypedExpression<T> typed(final Class<? extends T> type) {
		return new CheckedExpression<T>(this, type);
	}

	@Override
	public void accept(final Node.Visitor v) {
		((Visitor) v).visit(this);
	}

}
