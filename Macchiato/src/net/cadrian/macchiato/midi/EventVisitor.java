package net.cadrian.macchiato.midi;

import net.cadrian.macchiato.midi.message.MetaEventVisitor;
import net.cadrian.macchiato.midi.message.ShortEventVisitor;

public interface EventVisitor extends MetaEventVisitor, ShortEventVisitor {

}
