package net.cadrian.macchiato.interpreter.impl;

import net.cadrian.macchiato.interpreter.Function;
import net.cadrian.macchiato.interpreter.objects.MacClazsObject;
import net.cadrian.macchiato.ruleset.ast.Ruleset;
import net.cadrian.macchiato.ruleset.ast.expression.Identifier;

class ClazsContext extends LocalContext {

	private final MacClazsObject target;

	ClazsContext(final Context parent, final MacClazsObject target, final Ruleset ruleset) {
		super(parent, ruleset);
		this.target = target;
	}

	public MacClazsObject getTarget() {
		return target;
	}
	
	@Override
	protected Function getUncachedFunction(Identifier name) {
		// first: follow renames and try to find the right def
		
		// if not found:
		return super.getUncachedFunction(name);
	}

}
