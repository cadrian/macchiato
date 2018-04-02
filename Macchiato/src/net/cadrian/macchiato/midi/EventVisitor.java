package net.cadrian.macchiato.midi;

import net.cadrian.macchiato.midi.event.MetaEventVisitor;
import net.cadrian.macchiato.midi.event.ShortEventVisitor;

public interface EventVisitor extends MetaEventVisitor, ShortEventVisitor {

}
