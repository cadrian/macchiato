package net.cadrian.macchiato.midi.event;

import net.cadrian.macchiato.midi.event.shortev.ChannelPressureEvent;
import net.cadrian.macchiato.midi.event.shortev.ControlChangeEvent;
import net.cadrian.macchiato.midi.event.shortev.NoteOffEvent;
import net.cadrian.macchiato.midi.event.shortev.NoteOnEvent;
import net.cadrian.macchiato.midi.event.shortev.PitchBendEvent;
import net.cadrian.macchiato.midi.event.shortev.PolyPressureEvent;
import net.cadrian.macchiato.midi.event.shortev.ProgramChangeEvent;

public interface ShortEventVisitor
		extends ChannelPressureEvent.Visitor, ControlChangeEvent.Visitor, NoteOffEvent.Visitor, NoteOnEvent.Visitor,
		PitchBendEvent.Visitor, PolyPressureEvent.Visitor, ProgramChangeEvent.Visitor {

}
