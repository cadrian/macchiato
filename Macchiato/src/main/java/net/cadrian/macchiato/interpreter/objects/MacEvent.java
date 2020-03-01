package net.cadrian.macchiato.interpreter.objects;

import java.util.LinkedHashMap;
import java.util.Map;

import net.cadrian.macchiato.interpreter.Field;
import net.cadrian.macchiato.interpreter.Method;
import net.cadrian.macchiato.midi.MetaMessageType;
import net.cadrian.macchiato.midi.ShortMessageType;
import net.cadrian.macchiato.ruleset.ast.Ruleset;
import net.cadrian.macchiato.ruleset.ast.expression.Identifier;
import net.cadrian.macchiato.ruleset.parser.Position;

public class MacEvent implements MacObject {

	private static final Identifier FIELD_TICK = new Identifier("Tick", Position.NONE);
	private static final Identifier FIELD_TYPE = new Identifier("Type", Position.NONE);

	private final Ruleset ruleset;
	private final ReadWriteField<MacEvent, MacNumber> tick;
	private final ReadOnlyField<MacEvent, ? extends MacObject> type;

	private final Map<Identifier, Field<? extends MacObject, ? extends MacObject>> fields = new LinkedHashMap<>();

	public MacEvent(final Ruleset ruleset, final MacNumber tick, final MetaMessageType type) {
		this.ruleset = ruleset;
		this.tick = new ReadWriteField<MacEvent, MacNumber>(FIELD_TICK, ruleset, MacEvent.class, MacNumber.class, tick);
		this.type = new ReadOnlyField<MacEvent, MetaMessageType>(FIELD_TYPE, ruleset, MacEvent.class,
				MetaMessageType.class, type);
	}

	public MacEvent(final Ruleset ruleset, final MacNumber tick, final ShortMessageType type) {
		this.ruleset = ruleset;
		this.tick = new ReadWriteField<MacEvent, MacNumber>(FIELD_TICK, ruleset, MacEvent.class, MacNumber.class, tick);
		this.type = new ReadOnlyField<MacEvent, ShortMessageType>(FIELD_TYPE, ruleset, MacEvent.class,
				ShortMessageType.class, type);
	}

	public <T extends MacObject, R extends MacObject> Field<T, R> addField(final Identifier name, final R value) {
		@SuppressWarnings("unchecked")
		final Field<T, R> result = new ReadWriteField<>(name, ruleset, (Class<T>) MacEvent.class,
				(Class<R>) value.getClass(), value);
		fields.put(name, result);
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends MacObject, R extends MacObject> Field<T, R> getField(final Ruleset ruleset,
			final Identifier name) {
		switch (name.getName()) {
		case "Tick":
			return (Field<T, R>) tick;
		case "Type":
			return (Field<T, R>) type;
		default:
			return (Field<T, R>) fields.get(name);
		}
	}

	@Override
	public <T extends MacObject> Method<T> getMethod(final Ruleset ruleset, final Identifier name) {
		return null;
	}

	@Override
	public <T extends MacObject> T asIndexType(final Class<T> type) {
		if (type == getClass()) {
			return type.cast(this);
		}
		return null;
	}

}
