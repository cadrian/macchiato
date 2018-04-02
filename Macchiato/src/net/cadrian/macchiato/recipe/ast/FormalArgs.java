package net.cadrian.macchiato.recipe.ast;

import java.util.ArrayList;
import java.util.List;

public class FormalArgs {

	private final List<String> args = new ArrayList<>();

	public void add(final String arg) {
		args.add(arg);
	}

}
