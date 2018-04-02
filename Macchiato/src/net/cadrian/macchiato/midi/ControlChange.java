package net.cadrian.macchiato.midi;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum ControlChange {
	BANK(0),
	MODULATION_WHEEL(1),
	BREATH(2),
	FOOT(4),
	PORTAMENTO_TIME(5),
	CHANNEL_VOLUME(7),
	BALANCE(8),
	PAN(10),
	EXPRESSION(11),
	EFFECT_1(12),
	EFFECT_2(13),
	GENERAL_PURPOSE_1(16),
	GENERAL_PURPOSE_2(17),
	GENERAL_PURPOSE_3(18),
	GENERAL_PURPOSE_4(19),

	FINE_BANK(32),
	FINE_MODULATION_WHEEL(33),
	FINE_BREATH(34),
	FINE_FOOT(36),
	FINE_PORTAMENTO_TIME(37),
	FINE_CHANNEL_VOLUME(39),
	FINE_BALANCE(40),
	FINE_PAN(42),
	FINE_EXPRESSION(43),
	FINE_EFFECT_1(44),
	FINE_EFFECT_2(45),
	FINE_GENERAL_PURPOSE_1(48),
	FINE_GENERAL_PURPOSE_2(49),
	FINE_GENERAL_PURPOSE_3(50),
	FINE_GENERAL_PURPOSE_4(51),

	DAMPER_PEDAL(64),
	PORTAMENTO(65),
	SOSTENUTO(66),
	SOFT_PEDAL(67),
	LEGATO_PEDAL(68);

	private static final Map<Integer, ControlChange> MAP;
	static {
		final Map<Integer, ControlChange> m = new HashMap<>();
		for (final ControlChange mpc : values()) {
			m.put(mpc.id, mpc);
		}
		MAP = Collections.unmodifiableMap(m);
	}

	private final int id;
	private final boolean flag;

	private ControlChange(final int id) {
		this.id = id;
		this.flag = id > 63;
	}

	public static ControlChange at(final int id) {
		return MAP.get(id);
	}

	public String toString(final int data) {
		if (flag) {
			return (data > 63) ? "on" : "off";
		}
		return Integer.toString(data);
	}

}
