package net.cadrian.macchiato.recipe.ast;

public class ConditionFilter extends Filter {

	private final TypedExpression<Boolean> condition;

	public ConditionFilter(final TypedExpression<Boolean> condition, final Block instructions) {
		super(instructions);
		this.condition = condition;
	}

	public TypedExpression<Boolean> getCondition() {
		return condition;
	}

	@Override
	public int position() {
		return condition.position();
	}

}
