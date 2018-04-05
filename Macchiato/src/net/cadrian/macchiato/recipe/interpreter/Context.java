package net.cadrian.macchiato.recipe.interpreter;

import java.util.HashMap;
import java.util.Map;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.ShortMessage;

import net.cadrian.macchiato.midi.MetaMessageType;
import net.cadrian.macchiato.midi.ShortMessageType;
import net.cadrian.macchiato.recipe.ast.Instruction;
import net.cadrian.macchiato.recipe.ast.expression.TypedExpression;

class Context {

	private final Interpreter interpreter;
	final Map<String, Object> global = new HashMap<>();
	private Track track;
	private AbstractEvent event;
	private boolean next;

	public Context(final Interpreter interpreter) {
		this.interpreter = interpreter;
	}

	public Interpreter getInterpreter() {
		return interpreter;
	}

	void setTrack(final int trackIndex, final javax.sound.midi.Track track) {
		this.track = new Track(trackIndex, track);
	}

	void setEvent(final int eventIndex, final long tick, final MetaMessageType type, final MetaMessage message) {
		this.event = new MetaEvent(eventIndex, tick, type, message);
	}

	public void setEvent(final int eventIndex, final long tick, final ShortMessageType type,
			final ShortMessage message) {
		this.event = new ShortEvent(eventIndex, tick, type, message);
	}

	public boolean isNext() {
		return next;
	}

	public void setNext(final boolean next) {
		this.next = next;
	}

	public Track getTrack() {
		return track;
	}

	public AbstractEvent getEvent() {
		return event;
	}

	void eval(final Instruction instruction) {
		// TODO Auto-generated method stub

	}

	<T> T eval(final TypedExpression expression) {
		// TODO
		return null;
	}

}
