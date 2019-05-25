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
package net.cadrian.macchiato.interpreter.core;

import java.util.HashMap;
import java.util.Map;

import javax.sound.midi.MidiMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.cadrian.macchiato.interpreter.Clazs;
import net.cadrian.macchiato.interpreter.Event;
import net.cadrian.macchiato.interpreter.Function;
import net.cadrian.macchiato.interpreter.core.clazs.ClazzClazs;
import net.cadrian.macchiato.interpreter.objects.MacNumber;
import net.cadrian.macchiato.interpreter.objects.MacObject;
import net.cadrian.macchiato.midi.Message;
import net.cadrian.macchiato.ruleset.ast.Instruction;
import net.cadrian.macchiato.ruleset.ast.Ruleset;
import net.cadrian.macchiato.ruleset.ast.Ruleset.LocalizedClazz;
import net.cadrian.macchiato.ruleset.ast.Ruleset.LocalizedDef;
import net.cadrian.macchiato.ruleset.ast.expression.Identifier;
import net.cadrian.macchiato.ruleset.ast.expression.TypedExpression;

public abstract class Context {

	private static final Logger LOGGER = LoggerFactory.getLogger(Context.class);

	private final Map<Identifier, Function> functions = new HashMap<>();
	private final Map<Identifier, Clazs> clazses = new HashMap<>();

	abstract Ruleset getRuleset();

	abstract <M extends MidiMessage> Event<M> getEvent();

	<M extends MidiMessage> void emit() {
		final Event<M> event = getEvent();
		emit(event.createMessage(), event.getTick());
	}

	abstract <M extends MidiMessage> void emit(Message<M> message, MacNumber tick);

	abstract boolean isNext();

	abstract void setNext(boolean next);

	public void eval(final Instruction instruction) {
		instruction.accept(new InstructionEvaluationVisitor(this));
	}

	@SuppressWarnings("unchecked")
	<T extends MacObject> T eval(final TypedExpression expression) {
		final ExpressionEvaluationVisitor v = new ExpressionEvaluationVisitor(this, expression.getType());
		expression.accept(v);
		return (T) v.getLastValue();
	}

	final Function getFunction(final Identifier name) {
		LOGGER.debug("<-- {}", name);
		final Function result;
		final Function fn = functions.get(name);
		if (fn != null) {
			result = fn;
		} else {
			result = getUncachedFunction(name);
		}
		functions.put(name, result);
		LOGGER.debug("--> {}", result);
		return result;
	}

	final Clazs getClazs(final Identifier name) {
		LOGGER.debug("<-- {}", name);
		final Clazs result;
		final Clazs c = clazses.get(name);
		if (c != null) {
			result = c;
		} else {
			result = getUncachedClazs(name);
		}
		clazses.put(name, result);
		LOGGER.debug("--> {}", result);
		return result;
	}

	protected Function getUncachedFunction(final Identifier name) {
		LOGGER.debug("<-- {}", name);
		final Function result;
		final LocalizedDef def = getRuleset().getDef(name);
		if (def == null) {
			final Clazs clazs = getClazs(name);
			if (clazs != null) {
				result = clazs.getConstructor();
			} else {
				result = null;
			}
		} else {
			result = new DefFunction(def);
		}
		LOGGER.debug("--> {}", result);
		return result;
	}

	protected Clazs getUncachedClazs(final Identifier name) {
		LOGGER.debug("<-- {}", name);
		final Clazs result;
		final LocalizedClazz clazz = getRuleset().getClazz(name);
		if (clazz == null) {
			result = null;
		} else {
			result = new ClazzClazs(clazz);
		}
		LOGGER.debug("--> {}", result);
		return result;
	}

	public abstract boolean has(Identifier key);

	public abstract <T extends MacObject> T get(Identifier key);

	public abstract <T extends MacObject> T set(Identifier key, T value);

	abstract void declareLocal(Identifier name);

}
