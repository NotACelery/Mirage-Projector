# Obsidian Spike — implementation contract (dev.44)

> **Status:** implemented in the `0.1.0-dev.44` source candidate. Requires Windows build + in-game QA before build-clean.

## Purpose

The Obsidian Spike gives Crying Obsidian Shards an immediate practical use before projector-upgrade crafting and Improved Cores arrive. It is a reusable floor trap: visually a compact Crying-Obsidian crystal cluster, mechanically closer to a sharper Sweet Berry Bush.

## Recipe

```text
 S 
S S
TKT
```

- S = 3 Crying Obsidian Shards total
- T = 2 String total
- K = 1 Stick
- output = 1 Obsidian Spike

The shape intentionally reads as three sharp shards above a tied/supporting base rather than as a generic 3×3 material compression recipe.

## Model

- ~8 px / half-block visual height;
- 9 physical spires, not a flat painted icon;
- one tall, narrow central tip;
- eight shorter surrounding tips with varied height/lean;
- each tip uses a base + narrower upper segment so the silhouette tapers rather than ending in nine blunt columns;
- Crying-Obsidian black/deep-purple texture with arcane violet veins;
- collision body is intentionally absent; selection/outline shape is ~14×8×14 px.

## Entity interaction

Only `LivingEntity` instances participate.

While inside:

1. apply berry-bush-like movement multiplier `(0.8, 0.75, 0.8)`;
2. on server, compare current position with previous-tick X/Y/Z;
3. if movement on any axis exceeds `0.003`, request 2.0 damage;
4. use Mirage's own `obsidian_spike` DamageType.

Vertical movement is included deliberately: falling straight onto the trap must hurt even when X/Z are unchanged.

There is no separate high-frequency damage timer. Normal LivingEntity hurt/invulnerability handling controls successful repeated hits, preventing the intended `2.0 per hurt event` from turning into 2.0 every game tick.

Items, projectiles and other non-living entities are unaffected.

## Placement/support

- floor-oriented only;
- requires the block below to support the center of its top face;
- loses support -> becomes air/drops according to normal block removal behavior;
- no wall/ceiling orientation in dev.44.

## Compatibility

- no network protocol change;
- no projector NBT changes;
- no interaction with Beacon/crystal optics;
- no Fortune/Silk behavior special-case;
- standard block loot returns the Obsidian Spike itself.
