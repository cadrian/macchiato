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
