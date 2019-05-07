package net.cadrian.macchiato.interpreter.objects;

import java.util.HashMap;
import java.util.Map;

import net.cadrian.macchiato.interpreter.Field;
import net.cadrian.macchiato.interpreter.Method;
import net.cadrian.macchiato.midi.MetaMessageType;
import net.cadrian.macchiato.midi.ShortMessageType;
import net.cadrian.macchiato.ruleset.ast.Ruleset;

public class MacEvent implements MacObject {

	private final Ruleset ruleset;
	private final ReadWriteField<MacEvent, MacNumber> tick;
	private final ReadOnlyField<MacEvent, ? extends MacObject> type;

	private final Map<String, Field<? extends MacObject, ? extends MacObject>> fields = new HashMap<>();

	public MacEvent(final Ruleset ruleset, final MacNumber tick, final MetaMessageType type) {
		this.ruleset = ruleset;
		this.tick = new ReadWriteField<MacEvent, MacNumber>("tick", ruleset, MacEvent.class, MacNumber.class, tick);
		this.type = new ReadOnlyField<MacEvent, MetaMessageType>("type", ruleset, MacEvent.class, MetaMessageType.class,
				type);
	}

	public MacEvent(final Ruleset ruleset, final MacNumber tick, final ShortMessageType type) {
		this.ruleset = ruleset;
		this.tick = new ReadWriteField<MacEvent, MacNumber>("tick", ruleset, MacEvent.class, MacNumber.class, tick);
		this.type = new ReadOnlyField<MacEvent, ShortMessageType>("type", ruleset, MacEvent.class,
				ShortMessageType.class, type);
	}

	public <T extends MacObject, R extends MacObject> Field<T, R> addField(final String name, final R value) {
		@SuppressWarnings("unchecked")
		final Field<T, R> result = new ReadWriteField<>(name, ruleset, (Class<T>) MacEvent.class,
				(Class<R>) value.getClass(), value);
		fields.put(name, result);
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends MacObject, R extends MacObject> Field<T, R> getField(final Ruleset ruleset, final String name) {
		switch (name) {
		case "tick":
			return (Field<T, R>) tick;
		case "type":
			return (Field<T, R>) type;
		default:
			return (Field<T, R>) fields.get(name);
		}
	}

	@Override
	public <T extends MacObject> Method<T> getMethod(final Ruleset ruleset, final String name) {
		return null;
	}

}
