package net.cadrian.macchiato.midi.message;

import net.cadrian.macchiato.midi.message.s.ChannelPressureMessage;
import net.cadrian.macchiato.midi.message.s.ControlChangeMessage;
import net.cadrian.macchiato.midi.message.s.NoteOffMessage;
import net.cadrian.macchiato.midi.message.s.NoteOnMessage;
import net.cadrian.macchiato.midi.message.s.PitchBendMessage;
import net.cadrian.macchiato.midi.message.s.PolyPressureMessage;
import net.cadrian.macchiato.midi.message.s.ProgramChangeMessage;

public interface ShortEventVisitor
		extends ChannelPressureMessage.Visitor, ControlChangeMessage.Visitor, NoteOffMessage.Visitor,
		NoteOnMessage.Visitor, PitchBendMessage.Visitor, PolyPressureMessage.Visitor, ProgramChangeMessage.Visitor {

}
