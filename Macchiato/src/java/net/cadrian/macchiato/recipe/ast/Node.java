package net.cadrian.macchiato.recipe.ast;

public interface Node {

	interface Visitor {
	}

	int position();

	void accept(Visitor v);

}
