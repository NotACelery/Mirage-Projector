# Mirage Projector — next-chat handoff after dev.54

Current source candidate: **0.1.0-dev.54**.

## Main change

Five user-facing Improved Core blocks have been consolidated into one stateful **Core Booster**.

### Core Booster
- empty shell recipe: `GSG / S S / GSG`;
- accepts only Glass, Quartz, Amethyst Shard, Diamond, Netherite Ingot;
- right-click inserts one material;
- Shift + right-click extracts it;
- material persists through placement/mining;
- loaded stacks only merge with Boosters carrying the same material;
- no Silk Touch required; correct pickaxe required for the Booster itself to drop;
- loaded Booster maps to the existing x1.50 Improved Projection Core profile;
- empty Booster is not a valid projector Core.

### Visuals
- exact `anidado.json` four-cube geometry is used;
- all shells use the existing crying-obsidian glass palette;
- center material is a single spinning hologram inside the smallest cube;
- persistent BE sync is intended to fix the old center-item-not-visible-on-world-load bug.

### Compatibility
- five old Improved Core registry IDs remain hidden for QA-world safety only;
- old recipes were removed;
- new creative exposure uses only the Core Booster ID, with empty + five loaded same-ID variants.

### Jade
- optional Jade component shows material + x1.50 when Jade is installed.

## QA targets
1. Build on Windows.
2. Place a loaded Booster, quit/re-enter world: center item must already be visible immediately.
3. Insert/extract all five materials.
4. Mine loaded Booster with pickaxe: one Booster item retaining material.
5. Verify stack merging only for identical materials.
6. Put each loaded Booster in every projector Core socket and verify correct Power profile.
7. Check Jade material line with Jade installed.
