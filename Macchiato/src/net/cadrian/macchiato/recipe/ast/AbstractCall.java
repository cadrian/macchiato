package net.cadrian.macchiato.recipe.ast;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractCall {

	private final String name;
	private final int position;
	private final List<Expression> expressions = new ArrayList<>();

	public AbstractCall(final int position, final String name) {
		this.name = name;
		this.position = position;
	}

	public String getName() {
		return name;
	}

	public void add(final Expression exp) {
		expressions.add(exp);
	}

	public int position() {
		return position;
	}

}
