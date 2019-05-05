/*
 * This file is part of Macchiato.
 *
 * Macchiato is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * Macchiato is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Macchiato.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package net.cadrian.macchiato.interpreter.impl;

import java.util.Iterator;
import java.util.List;

import javax.sound.midi.MidiMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.cadrian.macchiato.interpreter.Function;
import net.cadrian.macchiato.interpreter.InterpreterException;
import net.cadrian.macchiato.interpreter.objects.MacBoolean;
import net.cadrian.macchiato.interpreter.objects.MacNumber;
import net.cadrian.macchiato.interpreter.objects.MacObject;
import net.cadrian.macchiato.interpreter.objects.container.MacContainer;
import net.cadrian.macchiato.midi.Message;
import net.cadrian.macchiato.ruleset.ast.Expression;
import net.cadrian.macchiato.ruleset.ast.Instruction;
import net.cadrian.macchiato.ruleset.ast.expression.Identifier;
import net.cadrian.macchiato.ruleset.ast.expression.TypedExpression;
import net.cadrian.macchiato.ruleset.ast.instruction.Abort;
import net.cadrian.macchiato.ruleset.ast.instruction.Assignment;
import net.cadrian.macchiato.ruleset.ast.instruction.Block;
import net.cadrian.macchiato.ruleset.ast.instruction.Emit;
import net.cadrian.macchiato.ruleset.ast.instruction.For;
import net.cadrian.macchiato.ruleset.ast.instruction.If;
import net.cadrian.macchiato.ruleset.ast.instruction.InstructionVisitor;
import net.cadrian.macchiato.ruleset.ast.instruction.Local;
import net.cadrian.macchiato.ruleset.ast.instruction.Next;
import net.cadrian.macchiato.ruleset.ast.instruction.ProcedureCall;
import net.cadrian.macchiato.ruleset.ast.instruction.While;

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
			final MacBoolean value = (MacBoolean) context.eval(w.getCondition().typed(MacBoolean.class));
			if (value.isFalse()) {
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
		final LocalContext callContext = new LocalContext(context, fn.getRuleset());
		final String[] argNames = fn.getArgNames();
		final Class<? extends MacObject>[] argTypes = fn.getArgTypes();
		final List<Expression> arguments = procedureCall.getArguments();
		if (argNames.length != arguments.size()) {
			throw new InterpreterException("invalid parameters", position);
		}
		for (int i = 0; i < argNames.length; i++) {
			final Expression argument = arguments.get(i);
			final MacObject value = context.eval(argument.typed(argTypes[i]));
			callContext.declareLocal(argNames[i]);
			callContext.set(argNames[i], value);
		}
		if (fn.getResultType() != null) {
			callContext.declareLocal("result");
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
	public void visitLocal(final Local local) {
		LOGGER.debug("<-- {}", local);
		final Identifier id = local.getLocal();
		final String name = id.getName();
		context.declareLocal(name);
		final Expression initializer = local.getInitializer();
		if (initializer != null) {
			final MacObject value = context.eval(initializer.typed(MacObject.class));
			new AssignmentVisitor(context).assign(id, value);
		}
		LOGGER.debug("--> {}", local);
	}

	@Override
	public void visitIf(final If i) {
		LOGGER.debug("<-- {}", i);
		final MacBoolean value = (MacBoolean) context.eval(i.getCondition().typed(MacBoolean.class));
		if (value.isTrue()) {
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
	public void visitFor(final For f) {
		LOGGER.debug("<-- {}", f);
		@SuppressWarnings("unchecked")
		final MacContainer<? extends MacObject> loop = (MacContainer<? extends MacObject>) context
				.eval(f.getLoop().typed(MacContainer.class));
		if (loop != null) {
			runLoop(loop, f.getName1(), f.getName2(), f.getInstruction());
		}
		LOGGER.debug("--> {}", f);
	}

	private <I extends MacObject> void runLoop(final MacContainer<I> loop, final Expression name1,
			final Expression name2, final Instruction instruction) {
		final AssignmentVisitor assignmentVisitor = new AssignmentVisitor(context);
		final Iterator<I> keys = loop.keys();
		if (name2 == null) {
			while (keys.hasNext()) {
				final MacObject value = loop.get(keys.next());
				assignmentVisitor.assign(name1, value);
			}
			instruction.accept(this);
		} else {
			while (keys.hasNext()) {
				final I key = keys.next();
				final MacObject value = loop.get(key);
				assignmentVisitor.assign(name1, key);
				assignmentVisitor.assign(name2, value);
			}
			instruction.accept(this);
		}
	}

	@Override
	public void visitEmit(final Emit emit) {
		LOGGER.debug("<-- {}", emit);
		final TypedExpression eventExpression = emit.getMessage();
		if (eventExpression == null) {
			context.emit();
		} else {
			@SuppressWarnings("unchecked")
			final Message<MidiMessage> message = (Message<MidiMessage>) context.eval(eventExpression);
			final TypedExpression tickExpression = emit.getTick();
			LOGGER.debug("tickExpression={}", tickExpression);
			final MacNumber tick;
			if (tickExpression != null) {
				tick = (MacNumber) context.eval(tickExpression);
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
		final MacObject value = context.eval(assignment.getRightSide().typed(MacObject.class));
		new AssignmentVisitor(context).assign(assignment.getLeftSide(), value);
		LOGGER.debug("--> {}", assignment);
	}

	@Override
	public void visitAbort(final Abort abort) {
		throw new InterpreterException("ABORTING: " + abort.getMessage(), abort.position());
	}

}
