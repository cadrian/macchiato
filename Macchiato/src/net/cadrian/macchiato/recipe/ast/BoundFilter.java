package net.cadrian.macchiato.recipe.ast;

public class BoundFilter extends Filter {

	public static enum Bound {
		BEGIN_SEQUENCE,
		END_SEQUENCE,
		BEGIN_TRACK,
		END_TRACK;
	}

	public static interface Visitor extends Node.Visitor {
		void visit(BoundFilter boundFilter);
	}

	private final int position;
	private final Bound bound;

	public BoundFilter(final int position, final Bound bound, final Block instructions) {
		super(instructions);
		this.position = position;
		this.bound = bound;
	}

	public Bound getBound() {
		return bound;
	}

	@Override
	public int position() {
		return position;
	}

	@Override
	public void accept(final Node.Visitor v) {
		((Visitor) v).visit(this);
	}

}
