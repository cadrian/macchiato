package net.cadrian.macchiato.recipe.ast;

import net.cadrian.macchiato.recipe.ast.expression.TypedExpression;
import net.cadrian.macchiato.recipe.ast.instruction.Block;

public class ConditionFilter extends Filter {

	public static interface Visitor extends Node.Visitor {
		void visit(ConditionFilter conditionFilter);
	}

	private final TypedExpression condition;

	public ConditionFilter(final TypedExpression condition, final Block instructions) {
		super(instructions);
		assert (condition.getType() == Boolean.class);
		this.condition = condition;
	}

	public TypedExpression getCondition() {
		return condition;
	}

	@Override
	public int position() {
		return condition.position();
	}

	@Override
	public void accept(final Node.Visitor v) {
		((Visitor) v).visit(this);
	}

}