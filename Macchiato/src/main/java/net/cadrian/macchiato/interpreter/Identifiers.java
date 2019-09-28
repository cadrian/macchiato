package net.cadrian.macchiato.interpreter;

import net.cadrian.macchiato.ruleset.ast.expression.Identifier;
import net.cadrian.macchiato.ruleset.parser.Position;

public final class Identifiers {

	public static final Identifier RESULT = new Identifier("Result", Position.NONE);
	public static final Identifier EVENT = new Identifier("Event", Position.NONE);

	private Identifiers() {
		// no instance
	}

}
