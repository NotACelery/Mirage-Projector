# dev.44 — Obsidian Spike QA

## Build gate

1. Run Windows `build.bat`.
2. Any inherited dev.43 compile/Mixin/resource failure takes priority.
3. Do not call dev.44 build-clean until the build succeeds.

## Recipe/model

- recipe consumes exactly 3 Crying Obsidian Shards + 2 String + 1 Stick;
- result count = 1;
- inventory model is recognizable and not oversized/broken;
- placed model visibly contains nine tips;
- center tip is taller/thinner;
- total visual height stays around half a block;
- no z-fighting or translucent ghost layers.

## Placement

- place on ordinary solid floor successfully;
- cannot remain floating when support underneath is removed;
- selection outline roughly matches half-block trap volume;
- it does not act as a solid half slab: creatures can enter the points.

## Damage/slow behavior

Test Survival player and several mobs:

- walking through visibly slows movement similarly to a berry bush;
- successful hit removes 2.0 damage points / 1 heart before armor/modifiers;
- damage repeats according to normal hurt timing, not every raw game tick;
- standing perfectly still inside does not receive artificial constant damage;
- moving again can trigger another hit;
- dropping vertically onto it can trigger damage;
- items and projectiles are not slowed/damaged;
- Creative/invulnerable player behavior remains vanilla-consistent.

## Death message

Kill a player/mob with the trap and verify Mirage's Obsidian Spike message is used rather than cactus/berry-bush wording.

## Regression

Because dev.44 inherits unvalidated dev.43, also spot-check:

- crystal growth registration still loads;
- Beacon renderer/Mixins still initialize;
- projector blocks still render;
- languages/resources load without missing-model purple-black boxes.
