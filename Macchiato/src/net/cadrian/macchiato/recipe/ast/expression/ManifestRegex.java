package net.cadrian.macchiato.recipe.ast.expression;

import java.util.regex.Pattern;

import net.cadrian.macchiato.recipe.ast.Node;

public class ManifestRegex implements TypedExpression {

	public static interface Visitor extends Node.Visitor {
		void visit(ManifestRegex manifestRegex);
	}

	private final Pattern value;
	private final int position;

	public ManifestRegex(final int position, final Pattern value) {
		this.value = value;
		this.position = position;
	}

	@Override
	public TypedExpression typed(final Class<?> type) {
		if (type.isAssignableFrom(Pattern.class)) {
			return this;
		}
		return null;
	}

	@Override
	public Class<?> getType() {
		return Pattern.class;
	}

	public Pattern getValue() {
		return value;
	}

	@Override
	public int position() {
		return position;
	}

	@Override
	public void accept(final Node.Visitor v) {
		((Visitor) v).visit(this);
	}

	@Override
	public String toString() {
		return "{ManifestRegex /" + value + "/}";
	}

}
