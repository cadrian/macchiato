package net.cadrian.macchiato.interpreter;

public interface Container<I> {

	Object set(I index, final Object value);

	Object get(final I index);

}
