package net.cadrian.macchiato.recipe.ast.expression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.cadrian.macchiato.recipe.ast.Expression;
import net.cadrian.macchiato.recipe.ast.Node;

public class ManifestArray implements TypedExpression {

	public static interface Visitor extends Node.Visitor {
		void visit(ManifestArray manifestArray);
	}

	private final List<Expression> expressions = new ArrayList<>();
	private final int position;

	public ManifestArray(final int position) {
		this.position = position;
	}

	@Override
	public TypedExpression typed(final Class<?> type) {
		if (type.isAssignableFrom(ManifestArray.class)) {
			return this;
		}
		return new CheckedExpression(this, type);
	}

	@Override
	public Class<?> getType() {
		return List.class;
	}

	@Override
	public int position() {
		return position;
	}

	public void add(final Expression expression) {
		expressions.add(expression);
	}

	@Override
	public void accept(final Node.Visitor v) {
		((Visitor) v).visit(this);
	}
	
	public List<Expression> getExpressions() {
		return Collections.unmodifiableList(expressions);
	}

}
