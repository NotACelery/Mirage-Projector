# dev.68 — Core extraction guard and immediate light teardown

## Scope

This snapshot does not change Beacon optics geometry or the dev.67 residual-ray work. It addresses two interaction/runtime issues discovered during QA.

## Core Booster extraction

A loaded Core Booster extracts only on Shift + right-click when the interacting hand is empty or is holding the same item type represented by the installed core material. Different held items leave Mirage's extraction path untouched and uncancelled.

Empty-hand extraction places the core directly into the interacting hand. Matching stacks merge if their components match and stack space exists; otherwise the extracted item uses the inventory and existing drop fallback.

## Mature Cluster light-field baseline

The slow-decay field does not require a Core Booster. Relay tier 0 still uses the Mature Cluster base conceptual light of 15 and advances one decay step for every two blocks. Core Boosters can add up to four conceptual relay tiers and therefore reinforce/extend the field.

## Immediate teardown

The previous removal path asked light nodes to validate on a later scheduled tick. dev.68 adds source-aware immediate reconciliation. On Mature Cluster removal, each node in that source's influence is recalculated while explicitly excluding the removed source. Nodes with no surviving contribution are removed immediately; nodes shared with another Mature source are immediately kept or adjusted to the surviving desired level. The light engine is checked for every changed node and the source position.

The same path is invoked when an energized Mature Cluster transitions to de-energized.

## Tooling

No source/resource/document was moved or deprecated in this snapshot. `build.bat` and `CLEAN-MIRAGE-PROJECTOR.bat` are intentionally unchanged from dev.67.
