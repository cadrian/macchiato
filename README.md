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

## Language grammar

The language is derived from C, with stricter conventions (notably,
blocks are mandatory in some cases).

```
Recipe ::= (Def | Filter)*

Def ::= "def" Identifier FormalArgs Block

Identifier ::= /[A-Za-z_][0-9A-Za-z]*/

FormalArgs ::= "(" (Identifier ("," Identifier)*)? ")"

Filter ::= Condition Block

Condition ::= "BEGIN" "SEQUENCE"
           | "BEGIN" "TRACK"
           | "END" "SEQUENCE"
           | "END" "TRACK"
           | Expression

Instruction ::= If
             |  While
             |  Emit
             |  Next
             |  Assignment
             |  Call
             |  Block

Block ::= "{" (Instruction)* "}"

Assignment ::= (Identifier | "result") IdentifierSuffix "=" Expression ";"

Call ::= Identifier "(" (Expression ("," Expression)*)? ")" ";"

If ::= "if" Block ("else" (If | Block))?

While ::= "while" Block ("else" Block)?

Emit ::= "emit" (Expression ("at" Expression)?)? ";"

Next ::= "next" ";"

Expression ::= OrLeft (OrRight)*

OrLeft ::= AndLeft (AndRight)*

OrRight ::= ("or" | "xor") OrLeft

AndLeft ::= ComparatorLeft (ComparatorRight)*

AndRight ::= "and" AndLeft

ComparatorLeft ::= AdditionLeft (AdditionRight)*

ComparatorRight ::= ("~" | "=" | "!=" | "<" | "<=" | ">" | ">=") ComparatorLeft

AdditionLeft ::= MultiplicationLeft (MultiplicationRight)*

AdditionRight ::= ("+" | "-") AdditionLeft

MultiplicationLeft ::= PowerLeft (PowerRight)*

MultiplicationRight ::= ("*" | "/" | "\") MultiplicationLeft

PowerLeft ::= Unary

PowerRight ::= "^" PowerLeft

Unary ::= ("not" | "+" | "-") Unary
       | AtomicExpressionWithSuffix

AtomicExpressionWithSuffix ::= AtomicExpression IdentifierSuffix

AtomicExpression ::= ManifestString
                  |  ManifestRegex
                  |  ManifestArray
                  |  ManifestDictionary
                  |  ManifestNumber
                  |  Call
                  |  Identifier
                  |  "result"
                  |  "(" Expression ")"

IdentifierSuffix ::= ("[" Expression "]" | "." Identifier)*

ManifestString ::= /"([^"]|\\.)*"/

ManifestRegex ::= /\/([^/]|\\.)*\//

ManifestArray ::= "[" (Expression ("," Expression)*)? "]"

ManifestDictionary ::= "{" (Expression ":" Expression ("," Expression ":" Expression)*)? "}"

ManifestNumber ::= /[0-9]+/
```

Note: the `result` reserved identifier is used to assign a value that
will be returned from a `def` function.

## Native values and functions

Some functions and values are provided natively by the interpreter.

### event

The `event` variable contains all the information necessary to
identify the event currently being treated. It contains the following
information:

Identifier | Content
---------- | --------
`event.tick` | The MIDI tick at which the event happens
`event.type` | The type of the event, either a meta message event: `SEQUENCE_NUMBER`, `TEXT`, `COPYRIGHT`, `TRACK_NAME`, `INSTRUMENT_NAME`, `LYRICS`, `MARKER_TEXT`, `CUE_POINT`, `CHANNEL_PREFIX`, `END_OF_TRACK`, `TEMPO`, `TIME_SIGNATURE`, `KEY_SIGNATURE`; or a short message event: `NOTE_OFF`, `NOTE_ON`, `POLY_PRESSURE`, `CONTROL_CHANGE`, `PROGRAM_CHANGE`, `CHANNEL_PRESSURE`, `PITCH_BEND`.
`event.sequence` | Only for `SEQUENCE_NUMBER` events: the sequence number
`event.text` | Only for text meta events (i.e. `TEXT`, `COPYRIGHT`, `TRACK_NAME`, `INSTRUMENT_NAME`, `LYRICS`, `MARKER_TEXT`, `CUE_POINT`): the text string
`event.bpm` | Only for `TEMPO` events: the number of beats per minute
`event.numerator` | Only for `TIME_SIGNATURE` events: the signature numerator
`event.denominator` | Only for `TIME_SIGNATURE` events: the signature denominator
`event.metronome` | Only for `TIME_SIGNATURE` events: the signature metronome, i.e. the number of MIDI ticks per metronome beat (usually 24)
`event.ticks` | Only for `TIME_SIGNATURE` events: the signature ticks, i.e. the number of 32ths that happen per quarter note (usually 8)
`event.keysig` | Only for `KEY_SIGNATURE`events: the key signature, between -7 and 7 (Cb to C# major, or Ab to A# minor)
`event.mode` | Only for `KEY_SIGNATURE`events: the key mode, 0 for major and 1 for minor
`event.channel` | Only for short message events (i.e. `NOTE_OFF`, `NOTE_ON`, `POLY_PRESSURE`, `CONTROL_CHANGE`, `PROGRAM_CHANGE`, `CHANNEL_PRESSURE`, `PITCH_BEND`): the channel to which the event applies
`event.velocity` | Only for `NOTE_ON` and `NOTE_OFF` events: the velocity of the note being resp. pushed or released
`event.pitch` | Only for `NOTE_ON` and `NOTE_OFF` events: the pitch of the note being resp. pushed or released
`event.pressure` | Only for `POLY_PRESSURE` and `CHANNEL_PRESSURE` events: the pressure to apply
`event.mpc` | Only for `CONTROL_CHANGE` events: the type of Multi-Point Controller: either sliding controllers: `BANK`, `MODULATION_WHEEL`, `BREATH`, `FOOT`, `PORTAMENTO_TIME`, `CHANNEL_VOLUME`, `BALANCE`, `PAN`, `EXPRESSION`, `EFFECT_1`, `EFFECT_2`, `GENERAL_PURPOSE_1`, `GENERAL_PURPOSE_2`, `GENERAL_PURPOSE_3`, `GENERAL_PURPOSE_4`, `FINE_BANK`, `FINE_MODULATION_WHEEL`, `FINE_BREATH`, `FINE_FOOT`, `FINE_PORTAMENTO_TIME`, `FINE_CHANNEL_VOLUME`, `FINE_BALANCE`, `FINE_PAN`, `FINE_EXPRESSION`, `FINE_EFFECT_1`, `FINE_EFFECT_2`, `FINE_GENERAL_PURPOSE_1`, `FINE_GENERAL_PURPOSE_2`, `FINE_GENERAL_PURPOSE_3`, `FINE_GENERAL_PURPOSE_4`; or switch controllers: `DAMPER_PEDAL`, `PORTAMENTO`, `SOSTENUTO`, `SOFT_PEDAL`, `LEGATO_PEDAL`
`event.value` | For `CONTROL_CHANGE` events: the value of the Multi-Point Controller (either a number between 0 and 127for sliding controllers; or a boolean for switch controllers); for `PITCH_BEND` events: the value of the pitch bend (between -8192 and 8191)
`event.patch` | Only for `PROGRAM_CHANGE` events: the patch number

### Event functions

New events can be emitted. Those events are built using the following functions:

Function name | Arguments
------------- | ---------
`SEQUENCE_NUMBER` | `sequence`: number between 0 and 127
`TEXT` | `text`: the text string
`COPYRIGHT` | `text`: the text string
`TRACK_NAME` | `text`: the text string
`INSTRUMENT_NAME` | `text`: the text string
`LYRICS` | `text`: the text string
`MARKER_TEXT` | `text`: the text string
`CUE_POINT` | `text`: the text string
`CHANNEL_PREFIX` | not yet implemented
`END_OF_TRACK` | no arguments
`TEMPO` | `bpm`: number of beats per minute
`TIME_SIGNATURE` | `numerator`: the signature numerator; `denominator`: the signature denominator; `metronome`: the number of MIDI ticks per metronome beat; `ticks`: the number of 32ths that happen per quarter note
`KEY_SIGNATURE` | `keysig`: the key signature, between -7 and 7 (Cb to C# major, or Ab to A# minor); `mode`: 0 for major or 1 for minor
`NOTE_OFF` | `channel` the event channel; `velocity`: the velocity between 0 and 127; `pitch` the note pitch netween 0 and 127
`NOTE_ON` | `channel` the event channel; `velocity`: the velocity between 0 and 127; `pitch` the note pitch netween 0 and 127
`POLY_PRESSURE` | `channel` the event channel; `pressure`: the pressure between 0 and 127
`CONTROL_CHANGE` | `channel` the event channel; `mpc`: the Multi-Point Controller (either sliding controllers: `BANK`, `MODULATION_WHEEL`, `BREATH`, `FOOT`, `PORTAMENTO_TIME`, `CHANNEL_VOLUME`, `BALANCE`, `PAN`, `EXPRESSION`, `EFFECT_1`, `EFFECT_2`, `GENERAL_PURPOSE_1`, `GENERAL_PURPOSE_2`, `GENERAL_PURPOSE_3`, `GENERAL_PURPOSE_4`, `FINE_BANK`, `FINE_MODULATION_WHEEL`, `FINE_BREATH`, `FINE_FOOT`, `FINE_PORTAMENTO_TIME`, `FINE_CHANNEL_VOLUME`, `FINE_BALANCE`, `FINE_PAN`, `FINE_EXPRESSION`, `FINE_EFFECT_1`, `FINE_EFFECT_2`, `FINE_GENERAL_PURPOSE_1`, `FINE_GENERAL_PURPOSE_2`, `FINE_GENERAL_PURPOSE_3`, `FINE_GENERAL_PURPOSE_4`; or switch controllers: `DAMPER_PEDAL`, `PORTAMENTO`, `SOSTENUTO`, `SOFT_PEDAL`, `LEGATO_PEDAL`); `value`: either a number between 0 and 127 (for sliding controllers) or a boolean (for switch controllers)
`PROGRAM_CHANGE` | `channel` the event channel; `patch`: the patch number between 0 and 127
`CHANNEL_PRESSURE` | `channel` the event channel; `pressure`: the pressure between 0 and 127
`PITCH_BEND` | `channel` the event channel; `value`: the pitch bend value between -8192 and 8191

### Other functions

Other functions are natively defined by the interpreter:

Function name | Arguments
------------- | ---------
`random`      | `max`: the upper bound of the random to draw. The value actually returned will be between 0 and `max`-1 inclusive.
