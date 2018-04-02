package net.cadrian.macchiato.recipe.ast;

public class Identifier implements Expression {

	private final String name;
	private final int position;

	public Identifier(final int position, final String name) {
		this.name = name;
		this.position = position;
	}

	@Override
	public <T> TypedExpression<T> typed(final Class<? extends T> type) {
		return new CheckedExpression<T>(this, type);
	}

	public String getName() {
		return name;
	}

	@Override
	public int position() {
		return position;
	}

}
