package net.cadrian.macchiato.midi.event;

import net.cadrian.macchiato.midi.event.metaev.CopyrightEvent;
import net.cadrian.macchiato.midi.event.metaev.CuePointEvent;
import net.cadrian.macchiato.midi.event.metaev.EndOfTrackEvent;
import net.cadrian.macchiato.midi.event.metaev.InstrumentNameEvent;
import net.cadrian.macchiato.midi.event.metaev.KeySignatureEvent;
import net.cadrian.macchiato.midi.event.metaev.LyricsEvent;
import net.cadrian.macchiato.midi.event.metaev.MarkerTextEvent;
import net.cadrian.macchiato.midi.event.metaev.SequenceNumberEvent;
import net.cadrian.macchiato.midi.event.metaev.TempoEvent;
import net.cadrian.macchiato.midi.event.metaev.TextEvent;
import net.cadrian.macchiato.midi.event.metaev.TimeSignatureEvent;
import net.cadrian.macchiato.midi.event.metaev.TrackNameEvent;

public interface MetaEventVisitor
		extends CopyrightEvent.Visitor, CuePointEvent.Visitor, EndOfTrackEvent.Visitor, InstrumentNameEvent.Visitor,
		KeySignatureEvent.Visitor, LyricsEvent.Visitor, MarkerTextEvent.Visitor, SequenceNumberEvent.Visitor,
		TempoEvent.Visitor, TextEvent.Visitor, TimeSignatureEvent.Visitor, TrackNameEvent.Visitor {

}
