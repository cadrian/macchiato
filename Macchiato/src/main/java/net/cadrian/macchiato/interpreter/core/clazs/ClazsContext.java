package net.cadrian.macchiato.interpreter.core.clazs;

import net.cadrian.macchiato.interpreter.ClazsMethod;
import net.cadrian.macchiato.interpreter.Field;
import net.cadrian.macchiato.interpreter.Function;
import net.cadrian.macchiato.interpreter.ObjectInexistentException;
import net.cadrian.macchiato.interpreter.core.Context;
import net.cadrian.macchiato.interpreter.core.LocalContext;
import net.cadrian.macchiato.interpreter.objects.MacObject;
import net.cadrian.macchiato.ruleset.ast.Ruleset;
import net.cadrian.macchiato.ruleset.ast.expression.Identifier;

class ClazsContext extends LocalContext {

	private final MacClazsObject target;

	ClazsContext(final Context parent, final MacClazsObject target, final Ruleset ruleset) {
		super(parent, ruleset);
		if (target == null) {
			throw new ObjectInexistentException("BUG: Method target does not exist");
		}
		this.target = target;
	}

	public MacClazsObject getTarget() {
		return target;
	}

	@Override
	protected <T extends MacObject> T getDefault(final Identifier key) {
		final Field<MacClazsObject, T> field = target.getField(ruleset, key);
		if (field != null) {
			return field.get(getTarget(), this, key.position());
		}
		return super.getDefault(key);
	}

	@Override
	protected <T extends MacObject> T setDefault(final Identifier key, final T value) {
		final Field<MacClazsObject, T> field = target.getField(ruleset, key);
		if (field != null) {
			return field.set(getTarget(), this, key.position(), value);
		}
		return super.setDefault(key, value);
	}

	@Override
	protected Function getUncachedFunction(final Identifier name) {
		final ClazsMethod method = (ClazsMethod) target.<MacClazsObject>getMethod(ruleset, name);
		if (method != null) {
			return new MethodFunction(method);
		}
		return super.getUncachedFunction(name);
	}

	@Override
	public LocalContext newLocalContext(final Ruleset ruleset) {
		return new ClazsContext(this, target, ruleset);
	}

}
