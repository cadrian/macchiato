package net.cadrian.macchiato.recipe.ast;

public class FunctionCall extends AbstractCall implements Expression {

	public FunctionCall(final int position, final String name) {
		super(position, name);
	}

	@Override
	public <T> TypedExpression<T> typed(final Class<? extends T> type) {
		return new CheckedExpression<T>(this, type);
	}

}
