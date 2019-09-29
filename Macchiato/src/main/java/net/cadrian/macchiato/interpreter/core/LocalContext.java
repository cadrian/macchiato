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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.sound.midi.MidiMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.cadrian.macchiato.interpreter.Clazs;
import net.cadrian.macchiato.interpreter.ContractException;
import net.cadrian.macchiato.interpreter.Event;
import net.cadrian.macchiato.interpreter.Function;
import net.cadrian.macchiato.interpreter.objects.MacBoolean;
import net.cadrian.macchiato.interpreter.objects.MacNumber;
import net.cadrian.macchiato.interpreter.objects.MacObject;
import net.cadrian.macchiato.interpreter.objects.MacRuleset;
import net.cadrian.macchiato.interpreter.objects.MacString;
import net.cadrian.macchiato.interpreter.objects.container.MacArray;
import net.cadrian.macchiato.interpreter.objects.container.MacDictionary;
import net.cadrian.macchiato.midi.Message;
import net.cadrian.macchiato.ruleset.ast.Expression;
import net.cadrian.macchiato.ruleset.ast.Ruleset;
import net.cadrian.macchiato.ruleset.ast.expression.Identifier;
import net.cadrian.macchiato.ruleset.ast.expression.TypedExpression;

public class LocalContext extends Context {

	private static final Logger LOGGER = LoggerFactory.getLogger(LocalContext.class);

	protected final Ruleset ruleset;
	private final Context parent;
	private final Map<Identifier, MacObject> local = new LinkedHashMap<>();
	private final Map<Integer, MacObject> oldValues = new TreeMap<>();

	protected LocalContext(final Context parent, final Ruleset ruleset) {
		LOGGER.debug("parent {} for ruleset {}", parent, ruleset);
		this.parent = parent;
		this.ruleset = ruleset;
	}

	@Override
	Ruleset getRuleset() {
		return ruleset;
	}

	@Override
	boolean isNext() {
		return parent.isNext();
	}

	@Override
	void setNext(final boolean next) {
		parent.setNext(next);
	}

	@Override
	<M extends MidiMessage> Event<M> getEvent() {
		return parent.getEvent();
	}

	@Override
	<M extends MidiMessage> void emit(final Message<M> message, final MacNumber tick) {
		parent.emit(message, tick);
	}

	@Override
	protected Function getUncachedFunction(final Identifier name) {
		final Function result = super.getUncachedFunction(name);
		if (result == null) {
			return parent.getFunction(name);
		}
		return result;
	}

	@Override
	protected Clazs getUncachedClazs(final Ruleset ruleset, final Identifier name) {
		final Clazs result = super.getUncachedClazs(ruleset, name);
		if (result == null) {
			return parent.getClazs(name);
		}
		return result;
	}

	@Override
	public boolean has(final Identifier key) {
		if (local.containsKey(key)) {
			return true;
		}
		return parent.has(key);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends MacObject> T get(final Identifier key) {
		LOGGER.debug("<-- {}", key);
		final T result;
		if (local.containsKey(key)) {
			result = (T) local.get(key);
		} else {
			final Ruleset r = ruleset.getRuleset(key);
			if (r != null) {
				final MacRuleset newRuleset = new MacRuleset(r);
				local.put(key, newRuleset);
				result = (T) newRuleset;
			} else {
				result = getDefault(key);
			}
		}
		LOGGER.debug("--> {}", result);
		return result;
	}

	protected <T extends MacObject> T getDefault(final Identifier key) {
		return parent.get(key);
	}

	@Override
	public <T extends MacObject> T set(final Identifier key, final T value) {
		LOGGER.debug("<-- {} = {}", key, value);
		T result;
		if (local.containsKey(key)) {
			@SuppressWarnings("unchecked")
			final T old = (T) local.put(key, value);
			result = old;
		} else {
			result = setDefault(key, value);
		}
		LOGGER.debug("--> {}", result);
		return result;
	}

	protected <T extends MacObject> T setDefault(final Identifier key, final T value) {
		return parent.set(key, value);
	}

	@Override
	public void declareLocal(final Identifier name) {
		LOGGER.debug("<-- {}", name);
		if (!local.containsKey(name)) {
			local.put(name, null);
		}
		LOGGER.debug("-->");
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends MacObject> T eval(final TypedExpression expression) {
		final ExpressionEvaluationVisitor v = new ExpressionEvaluationVisitor(this, expression.getType(), oldValues);
		expression.accept(v);
		return (T) v.getLastValue();
	}

	@Override
	public void evaluateOldData(final Expression ensures) {
		if (ensures != null) {
			final OldExpressionEvaluationVisitor v = new OldExpressionEvaluationVisitor(this, oldValues);
			ensures.accept(v);
		}
	}

	@Override
	public boolean checkContract(final Expression contract, final String tag) throws ContractException {
		if (contract == null) {
			return false;
		}
		final MacObject contractValue = eval(contract.typed(MacObject.class));
		if (contractValue instanceof MacBoolean) {
			if (((MacBoolean) contractValue).isFalse()) {
				throw new ContractException(tag + " failed", contract.position());
			}
		} else if (contractValue instanceof MacArray) {
			final MacArray contractArray = (MacArray) contractValue;
			final Iterator<MacNumber> keys = contractArray.keys();
			while (keys.hasNext()) {
				final MacNumber key = keys.next();
				final MacObject value = contractArray.get(key);
				if (value instanceof MacBoolean) {
					if (((MacBoolean) contractValue).isFalse()) {
						throw new ContractException(tag + "[" + key + "] failed", contract.position());
					}
				} else {
					throw new ContractException(tag + "[" + key + "] is not a boolean", contract.position());
				}
			}
		} else if (contractValue instanceof MacDictionary) {
			final MacDictionary contractDictionary = (MacDictionary) contractValue;
			final Iterator<MacString> keys = contractDictionary.keys();
			while (keys.hasNext()) {
				final MacString key = keys.next();
				final MacObject value = contractDictionary.get(key);
				if (value instanceof MacBoolean) {
					if (((MacBoolean) contractValue).isFalse()) {
						throw new ContractException(tag + "[" + key + "] failed", contract.position());
					}
				} else {
					throw new ContractException(tag + "[" + key + "] is not a boolean", contract.position());
				}
			}
		} else {
			throw new ContractException(tag + " is not a boolean or a collection of booleans", contract.position());
		}
		return true;
	}

	@Override
	public String toString() {
		return "{LocalContext " + ruleset + " parent=" + parent + "}";
	}

}
