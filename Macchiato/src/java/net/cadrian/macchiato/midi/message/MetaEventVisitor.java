package net.cadrian.macchiato.midi.message;

import net.cadrian.macchiato.midi.message.m.CopyrightMessage;
import net.cadrian.macchiato.midi.message.m.CuePointMessage;
import net.cadrian.macchiato.midi.message.m.EndOfTrackMessage;
import net.cadrian.macchiato.midi.message.m.InstrumentNameMessage;
import net.cadrian.macchiato.midi.message.m.KeySignatureMessage;
import net.cadrian.macchiato.midi.message.m.LyricsMessage;
import net.cadrian.macchiato.midi.message.m.MarkerTextMessage;
import net.cadrian.macchiato.midi.message.m.SequenceNumberMessage;
import net.cadrian.macchiato.midi.message.m.TempoMessage;
import net.cadrian.macchiato.midi.message.m.TextMessage;
import net.cadrian.macchiato.midi.message.m.TimeSignatureMessage;
import net.cadrian.macchiato.midi.message.m.TrackNameMessage;

public interface MetaEventVisitor extends CopyrightMessage.Visitor, CuePointMessage.Visitor, EndOfTrackMessage.Visitor,
		InstrumentNameMessage.Visitor, KeySignatureMessage.Visitor, LyricsMessage.Visitor, MarkerTextMessage.Visitor,
		SequenceNumberMessage.Visitor, TempoMessage.Visitor, TextMessage.Visitor, TimeSignatureMessage.Visitor,
		TrackNameMessage.Visitor {

}
