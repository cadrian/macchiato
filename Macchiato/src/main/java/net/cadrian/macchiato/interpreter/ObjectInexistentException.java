package net.cadrian.macchiato.interpreter;

import net.cadrian.macchiato.ruleset.parser.Position;

public class ObjectInexistentException extends InterpreterException {

	private static final long serialVersionUID = 8581795177943315304L;

	public ObjectInexistentException(final String msg, final Position... position) {
		super(msg, position);
	}

	public ObjectInexistentException(final String msg, final Throwable cause, final Position... position) {
		super(msg, cause, position);
	}

	public ObjectInexistentException(final String msg, final InterpreterException e) {
		super(msg, e);
	}

}
