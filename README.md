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
