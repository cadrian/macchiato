package net.cadrian.macchiato.recipe.ast;

public class Filter implements Node {

	private final TypedExpression<Boolean> condition;
	private final Block instructions;

	public Filter(final TypedExpression<Boolean> condition, final Block instructions) {
		this.condition = condition;
		this.instructions = instructions;
	}

	public TypedExpression<Boolean> getCondition() {
		return condition;
	}

	public Block getInstructions() {
		return instructions;
	}

	@Override
	public int position() {
		return condition.position();
	}

}
