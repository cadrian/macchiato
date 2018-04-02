package net.cadrian.macchiato.recipe.parser;

public class ParserException extends RuntimeException {

	private static final long serialVersionUID = 4088421127991758010L;

	public ParserException(final String message, final Throwable e) {
		super(message, e);
	}

	public ParserException(final String message) {
		super(message);
	}

}
