package net.cadrian.macchiato.recipe.interpreter;

import net.cadrian.macchiato.recipe.ast.BoundFilter;
import net.cadrian.macchiato.recipe.ast.BoundFilter.Bound;
import net.cadrian.macchiato.recipe.ast.ConditionFilter;

class BoundFilterVisitor implements BoundFilter.Visitor, ConditionFilter.Visitor {

	private final GlobalContext context;
	private final Bound bound;

	public BoundFilterVisitor(final GlobalContext context, final Bound bound) {
		this.context = context;
		this.bound = bound;
	}

	@Override
	public void visit(final ConditionFilter conditionFilter) {
		// do nothing
	}

	@Override
	public void visit(final BoundFilter boundFilter) {
		if (boundFilter.getBound() == bound) {
			context.eval(boundFilter.getInstructions());
		}
	}

}