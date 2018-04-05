package net.cadrian.macchiato.recipe.interpreter;

import net.cadrian.macchiato.recipe.ast.Instruction;
import net.cadrian.macchiato.recipe.ast.instruction.Assignment;
import net.cadrian.macchiato.recipe.ast.instruction.Block;
import net.cadrian.macchiato.recipe.ast.instruction.Emit;
import net.cadrian.macchiato.recipe.ast.instruction.If;
import net.cadrian.macchiato.recipe.ast.instruction.InstructionVisitor;
import net.cadrian.macchiato.recipe.ast.instruction.Next;
import net.cadrian.macchiato.recipe.ast.instruction.ProcedureCall;
import net.cadrian.macchiato.recipe.ast.instruction.While;

class InstructionEvaluationVisitor implements InstructionVisitor {

	private final Context context;

	InstructionEvaluationVisitor(final Context context) {
		this.context = context;
	}

	@Override
	public void visit(final While w) {
		boolean looped = false;
		while (true) {
			final boolean value = context.eval(w.getCondition().typed(Boolean.class));
			if (!value) {
				break;
			}
			looped = true;
			w.getInstruction().accept(this);
		}
		if (!looped) {
			final Instruction otherwise = w.getOtherwise();
			if (otherwise != null) {
				otherwise.accept(this);
			}
		}
	}

	@Override
	public void visit(final ProcedureCall procedureCall) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(final Next next) {
		context.setNext(true);
	}

	@Override
	public void visit(final If i) {
		final boolean value = context.eval(i.getCondition().typed(Boolean.class));
		if (value) {
			i.getInstruction().accept(this);
		} else {
			final Instruction otherwise = i.getOtherwise();
			if (otherwise != null) {
				otherwise.accept(this);
			}
		}
	}

	@Override
	public void visitEmit(final Emit emit) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(final Block block) {
		for (final Instruction instruction : block.getInstructions()) {
			if (context.isNext()) {
				break;
			}
			instruction.accept(this);
		}
	}

	@Override
	public void visit(final Assignment assignment) {
		final Object value = context.eval(assignment.getRightSide().typed(Object.class));
		new AssignmentVisitor(context).assign(assignment.getLeftSide(), value);
	}

}