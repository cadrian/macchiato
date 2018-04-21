package net.cadrian.macchiato.interpreter;

public class InterpreterException extends RuntimeException {

	private static final long serialVersionUID = -695648464250538720L;

	private final int position;

	public InterpreterException(final String msg, final int position) {
		super(msg);
		this.position = position;
	}

	public InterpreterException(final String msg, final int position, final Throwable cause) {
		super(msg, cause);
		this.position = position;
	}

	public int getPosition() {
		return position;
	}

}
