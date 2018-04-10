package net.cadrian.macchiato.midi;

import java.math.BigInteger;

import net.cadrian.macchiato.recipe.interpreter.AbstractEvent;

public interface Message {

	interface Visitor {
	}

	void accept(Visitor v);

	AbstractEvent toEvent(BigInteger tick);

}
