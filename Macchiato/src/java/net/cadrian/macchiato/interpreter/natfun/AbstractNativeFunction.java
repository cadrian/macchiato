package net.cadrian.macchiato.interpreter.natfun;

import net.cadrian.macchiato.interpreter.Function;
import net.cadrian.macchiato.ruleset.ast.Ruleset;

abstract class AbstractNativeFunction implements Function {

	protected final Ruleset ruleset;

	AbstractNativeFunction(final Ruleset ruleset) {
		this.ruleset = ruleset;
	}

	@Override
	public Ruleset getRuleset() {
		return ruleset;
	}

}
