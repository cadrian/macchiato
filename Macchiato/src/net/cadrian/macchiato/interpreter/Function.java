package net.cadrian.macchiato.interpreter;

public interface Function {

	String name();

	Class<?>[] getArgTypes();

	String[] getArgNames();

	Class<?> getResultType();

	void run(final Context context);

}
