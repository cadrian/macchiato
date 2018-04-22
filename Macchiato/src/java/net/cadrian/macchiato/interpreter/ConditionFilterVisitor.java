package net.cadrian.macchiato.interpreter;

import net.cadrian.macchiato.recipe.ast.BoundFilter;
import net.cadrian.macchiato.recipe.ast.ConditionFilter;

class ConditionFilterVisitor implements BoundFilter.Visitor, ConditionFilter.Visitor {

	private final GlobalContext context;

	public ConditionFilterVisitor(final GlobalContext context) {
		this.context = context;
	}

	@Override
	public void visit(final ConditionFilter conditionFilter) {
		final boolean condition = (Boolean) context.eval(conditionFilter.getCondition());
		if (condition) {
			context.eval(conditionFilter.getInstructions());
		}
	}

	@Override
	public void visit(final BoundFilter boundFilter) {
		// do nothing
	}

}