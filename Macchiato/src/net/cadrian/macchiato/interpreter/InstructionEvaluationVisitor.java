package net.cadrian.macchiato.interpreter;

import java.math.BigInteger;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.cadrian.macchiato.midi.Message;
import net.cadrian.macchiato.recipe.ast.Expression;
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

	private static final Logger LOGGER = LoggerFactory.getLogger(InstructionEvaluationVisitor.class);

	private final Context context;

	InstructionEvaluationVisitor(final Context context) {
		this.context = context;
	}

	@Override
	public void visitWhile(final While w) {
		LOGGER.debug("<-- {}", w);
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
		LOGGER.debug("--> {}", w);
	}

	@Override
	public void visitProcedureCall(final ProcedureCall procedureCall) {
		LOGGER.debug("<-- {}", procedureCall);
		final Function fn = context.getFunction(procedureCall.getName());
		final int position = procedureCall.position();
		if (fn == null) {
			throw new InterpreterException("unknown procedure " + procedureCall.getName(), position);
		}
		final LocalContext callContext = new LocalContext(context);
		final String[] argNames = fn.getArgNames();
		final Class<?>[] argTypes = fn.getArgTypes();
		final List<Expression> arguments = procedureCall.getArguments();
		if (argNames.length != arguments.size()) {
			throw new InterpreterException("invalid parameters", position);
		}
		for (int i = 0; i < argNames.length; i++) {
			final Expression argument = arguments.get(i);
			final Object value = context.eval(argument.typed(argTypes[i]));
			callContext.set(argNames[i], value);
		}
		fn.run(callContext, position);
		LOGGER.debug("--> {}", procedureCall);
	}

	@Override
	public void visitNext(final Next next) {
		LOGGER.debug("<-- {}", next);
		context.setNext(true);
		LOGGER.debug("--> {}", next);
	}

	@Override
	public void visitIf(final If i) {
		LOGGER.debug("<-- {}", i);
		final boolean value = (Boolean) context.eval(i.getCondition().typed(Boolean.class));
		if (value) {
			i.getInstruction().accept(this);
		} else {
			final Instruction otherwise = i.getOtherwise();
			if (otherwise != null) {
				otherwise.accept(this);
			}
		}
		LOGGER.debug("--> {}", i);
	}

	@Override
	public void visitEmit(final Emit emit) {
		LOGGER.debug("<-- {}", emit);
		final TypedExpression eventExpression = emit.getMessage();
		if (eventExpression == null) {
			context.emit();
		} else {
			final Message message = (Message) context.eval(eventExpression);
			final TypedExpression tickExpression = emit.getTick();
			LOGGER.debug("tickExpression={}", tickExpression);
			final BigInteger tick;
			if (tickExpression != null) {
				tick = (BigInteger) context.eval(tickExpression);
				LOGGER.debug("tick={}", tick);
			} else {
				tick = context.getEvent().getTick();
			}
			context.emit(message, tick);
		}
		LOGGER.debug("--> {}", emit);
	}

	@Override
	public void visitBlock(final Block block) {
		LOGGER.debug("<-- {}", block);
		for (final Instruction instruction : block.getInstructions()) {
			if (context.isNext()) {
				break;
			}
			instruction.accept(this);
		}
		LOGGER.debug("--> {}", block);
	}

	@Override
	public void visitAssignment(final Assignment assignment) {
		LOGGER.debug("<-- {}", assignment);
		final Object value = context.eval(assignment.getRightSide().typed(Object.class));
		new AssignmentVisitor(context).assign(assignment.getLeftSide(), value);
		LOGGER.debug("--> {}", assignment);
	}

}