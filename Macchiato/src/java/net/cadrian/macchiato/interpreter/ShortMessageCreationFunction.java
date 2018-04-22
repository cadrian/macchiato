package net.cadrian.macchiato.interpreter;

import net.cadrian.macchiato.midi.ShortMessageType;
import net.cadrian.macchiato.midi.message.ShortMessage;

public class ShortMessageCreationFunction implements Function {

	private final ShortMessageType type;

	public ShortMessageCreationFunction(final ShortMessageType type) {
		this.type = type;
	}

	@Override
	public String name() {
		return type.name();
	}

	@Override
	public Class<?>[] getArgTypes() {
		return type.getArgTypes();
	}

	@Override
	public String[] getArgNames() {
		return type.getArgNames();
	}

	@Override
	public Class<?> getResultType() {
		return ShortMessage.class;
	}

	@Override
	public void run(final Context context, final int position) {
		final String[] argNames = getArgNames();
		final Object[] args = new Object[argNames.length];
		for (int i = 0; i < argNames.length; i++) {
			args[i] = context.get(argNames[i]);
		}
		context.set("result", type.create(args));
	}

}
