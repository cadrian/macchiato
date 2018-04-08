package net.cadrian.macchiato.recipe.interpreter;

import java.util.List;

import net.cadrian.macchiato.recipe.ast.Def;
import net.cadrian.macchiato.recipe.ast.Expression;
import net.cadrian.macchiato.recipe.ast.FormalArgs;
import net.cadrian.macchiato.recipe.ast.Instruction;
import net.cadrian.macchiato.recipe.ast.expression.TypedExpression;
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
			final boolean value = (Boolean) context.eval(w.getCondition().typed(Boolean.class));
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
		final Def def = context.getInterpreter().recipe.getDef(procedureCall.getName());
		final LocalContext callContext = new LocalContext(context);
		final FormalArgs args = def.getArgs();
		final List<Expression> arguments = procedureCall.getArguments();
		if (args.size() != arguments.size()) {
			throw new InterpreterException("invalid parameters");
		}
		for (int i = 0; i < args.size(); i++) {
			final Expression argument = arguments.get(i);
			final Object value = context.eval(argument.typed(Object.class));
			callContext.set(args.get(i), value);
		}
		def.getInstruction().accept(new InstructionEvaluationVisitor(callContext));
	}

	@Override
	public void visit(final Next next) {
		context.setNext(true);
	}

	@Override
	public void visit(final If i) {
		final boolean value = (Boolean) context.eval(i.getCondition().typed(Boolean.class));
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
		final TypedExpression expression = emit.getExpression();
		if (expression == null) {
			context.emit();
		} else {
			final AbstractEvent event = (AbstractEvent) context.eval(expression);
			context.emit(event);
		}
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