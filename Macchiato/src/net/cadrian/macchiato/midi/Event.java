package net.cadrian.macchiato.midi;

public interface Event {

	interface Visitor {
	}

	void accept(Visitor v);

}
