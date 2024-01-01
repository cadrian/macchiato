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
package net.cadrian.macchiato.midi;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import net.cadrian.macchiato.interpreter.Field;
import net.cadrian.macchiato.interpreter.Method;
import net.cadrian.macchiato.interpreter.objects.MacComparable;
import net.cadrian.macchiato.interpreter.objects.MacNumber;
import net.cadrian.macchiato.interpreter.objects.MacObject;
import net.cadrian.macchiato.interpreter.objects.MacString;
import net.cadrian.macchiato.ruleset.ast.Ruleset;
import net.cadrian.macchiato.ruleset.ast.expression.Identifier;

public enum ControlChange implements MacComparable<ControlChange> {
	// Coarse-grained controllers
	BANK(0), MODULATION_WHEEL(1), BREATH(2), FOOT(4), PORTAMENTO_TIME(5), CHANNEL_VOLUME(7), BALANCE(8), PAN(10),
	EXPRESSION(11), EFFECT_1(12), EFFECT_2(13), GENERAL_PURPOSE_1(16), GENERAL_PURPOSE_2(17), GENERAL_PURPOSE_3(18),
	GENERAL_PURPOSE_4(19),

	// Fine-grained controllers
	FINE_BANK(32), FINE_MODULATION_WHEEL(33), FINE_BREATH(34), FINE_FOOT(36), FINE_PORTAMENTO_TIME(37),
	FINE_CHANNEL_VOLUME(39), FINE_BALANCE(40), FINE_PAN(42), FINE_EXPRESSION(43), FINE_EFFECT_1(44), FINE_EFFECT_2(45),
	FINE_GENERAL_PURPOSE_1(48), FINE_GENERAL_PURPOSE_2(49), FINE_GENERAL_PURPOSE_3(50), FINE_GENERAL_PURPOSE_4(51),

	// Pedals
	DAMPER_PEDAL(64), PORTAMENTO(65), SOSTENUTO(66), SOFT_PEDAL(67), LEGATO_PEDAL(68),

	// Effects
	EFFECT_1_DEPTH(91), EFFECT_2_DEPTH(92), EFFECT_3_DEPTH(93), EFFECT_4_DEPTH(94), EFFECT_5_DEPTH(95),

	// Non-Registered and Registered Parameters
	NRPN_MSB(99), NRPN_LSB(98), RPN_MSB(101), RPN_LSB(100), PARAMETER_VALUE(6), FINE_PARAMETER_VALUE(38),

	// Global controllers
	ALL_SOUND_OFF(120), ALL_CONTROLLERS_OFF(121), LOCAL_CONTROL(122), ALL_NOTES_OFF(123), OMNI_MODE_OFF(124),
	OMNI_MODE_ON(125), MONO_OPERATION(126), POLY_OPERATION(127);

	private static final Map<Integer, ControlChange> MAP;
	static {
		final Map<Integer, ControlChange> m = new LinkedHashMap<>();
		for (final ControlChange mpc : values()) {
			m.put(mpc.code, mpc);
		}
		MAP = Collections.unmodifiableMap(m);
	}

	public final int code;

	private ControlChange(final int code) {
		this.code = code;
	}

	public static ControlChange at(final int id) {
		return MAP.get(id);
	}

	public String toString(final int data) {
		return Integer.toString(data);
	}

	public MacObject valueOf(final int midiValue) {
		return MacNumber.valueOf(midiValue);
	}

	public int midiValueOf(final MacObject value) {
		return ((MacNumber) value).getValue().intValueExact();
	}

	@Override
	public <T extends MacObject, R extends MacObject> Field<T, R> getField(final Ruleset ruleset,
			final Identifier name) {
		return null;
	}

	@Override
	public <T extends MacObject> Method<T> getMethod(final Ruleset ruleset, final Identifier name) {
		return null;
	}

	@Override
	public <T extends MacObject> T asIndexType(final Class<T> type) {
		if (type == getClass()) {
			return type.cast(this);
		}
		if (type == MacNumber.class) {
			return type.cast(MacNumber.valueOf(code));
		}
		if (type == MacString.class) {
			return type.cast(MacString.valueOf(name()));
		}
		return null;
	}

}
