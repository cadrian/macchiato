package net.cadrian.macchiato.interpreter;

import net.cadrian.macchiato.interpreter.impl.Context;
import net.cadrian.macchiato.interpreter.objects.MacObject;

public interface Method<T extends MacObject> extends Callable {

	Class<T> getTargetType();

	void run(T target, final Context context, int position);

}
