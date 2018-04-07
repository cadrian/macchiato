package net.cadrian.macchiato.recipe.ast;

import java.util.ArrayList;
import java.util.List;

public class FormalArgs {

	private final List<String> args = new ArrayList<>();

	public void add(final String arg) {
		args.add(arg);
	}

	public int size() {
		return args.size();
	}

	public String get(final int i) {
		return args.get(i);
	}

}
