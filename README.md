# Introduction

Macchiato is a MIDI file filter.

A filter is a tool that takes an input, and produces an output of the
same kind. The UNIX/Linux command line uses a lot of filters: `grep`,
`sed`, `awk`, `perl`… Those tools work on text files: they take a text
file, apply some rules to transform it, and output the resulting text
file.

Macchiato works in the same way: it takes a MIDI file, transform it
using some rules, and outputs a MIDI file.

# Macchiato rules

The Macchiato rules resemble the `awk` rules: blocks that are run when
a condition is set.

Whereas the `awk` conditions are run for each text line, Macchiato
rules are run for each MIDI event, and at some special points:
beginning of sequence, beginning of each track, end of each track, end
of sequence.

Example: transform `NOTE_ON` events with a velocity equal to 0 into
`NOTE_OFF` events with the same velocity as the corresponding
`NOTE_ON` event.

```javascript
# Called at each beginning of track. Other special markers are BEGIN SEQUENCE, END SEQUENCE, and END TRACK.
BEGIN TRACK {
    # reset global variables at track beginning

    # velocity records the velocity of playing notes, per channel, per pitch
    velocity = [];

    # fill default values
    channel = 0;
    while (channel < 16) {
        velocity[channel] = [];
        channel = channel + 1;
    }
}

(event.type == NOTE_ON) and (event.velocity > 0) {
    velocity[event.channel][event.pitch] = event.velocity;
    emit;
    next;
}

(event.type == NOTE_ON) and (event.velocity == 0) {
    # emits a new event (instead of the analyzed one)
    emit NOTE_OFF(event.channel, event.pitch, velocity[event.channel][event.pitch]);
    next;
}

# the current event is emitted if there were no next before
```
