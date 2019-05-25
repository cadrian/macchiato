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
