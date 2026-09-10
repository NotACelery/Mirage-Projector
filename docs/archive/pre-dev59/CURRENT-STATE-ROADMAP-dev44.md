# Mirage Projector — Current State & Roadmap (dev.44)

## Baseline

- Current source line: `0.1.0-dev.44`
- Network protocol: 18
- Last explicitly confirmed in-game behavioral checkpoint before the post-dev.40 waves: dev.40 Piglin/Hoglin shaking fix.
- dev.41: consolidation/documentation closure.
- dev.42: Crying Obsidian Shard + physical chassis/Core Chamber.
- dev.43: renewable crystals + structure loot + Beacon optics.
- dev.44: Obsidian Spike trap.

`dev.44` must not be called build-clean until Windows `build.bat` and in-game regression QA pass. Because dev.44 is based on the unconfirmed dev.43 source candidate, any dev.43 build/render issue has priority over further feature work.

## Implemented current waves

### dev.42

- Crying Obsidian Shard recipes;
- composite chassis optical material language;
- universal Core Chamber;
- real installed Core ItemStack rendering.

### dev.43

- four Crying-Obsidian crystal ages;
- renewable downward growth;
- exact 1/2/3/4 shard economy + Silk stage recovery;
- structure shard loot;
- Beacon excitation/transmission;
- residual purple refraction beams.

### dev.44

- craftable Obsidian Spike;
- nine-spire half-block model;
- berry-bush-like living-entity slowdown;
- 2.0 damage per successful movement-triggered hurt event;
- Mirage-specific DamageType/death message;
- floor-support requirement;
- no networking/persistence changes.

## Next order

### dev.45 — state-preserving projector upgrade crafting

Build canonical upgrade-state transfer **before** adding the recipes.

Progression:

```text
Mirage Projector
      ↓
Mirage Display
   ├─ Wide
   ├─ Tall
   ├─ Prism
   └─ Field
```

Requirements:

1. consume an existing projector ItemStack as recipe input;
2. preserve its complete persisted projector state in the result;
3. preserve Core, imported Image/GIF references, Entity cards/snapshots/equipment, Item/Banner snapshots and presentation/Power settings;
4. normalize only values truly incompatible with the destination chassis;
5. never implement the upgrades as ordinary recipes that return a clean empty projector.

Frozen Display recipe:

```text
Q S A
S M S
A S Q
```

Frozen Prism recipe:

```text
S G S
G D G
S G S
```

Wide/Tall must have equal total cost and geometry-signaling layouts. Field must be substantially more expensive and prominently consume whole Crying Obsidian.

### dev.46 — Improved Cores

Exactly five material tiers. Same Base PU, target ~×1.50 amplification, decorative block/item with three nested dark-purple shells and a truly rotated middle shell.

### dev.47 — Improved-Core Beacon relay

Diffusion / Radiance / Resonance / Focus / Inversion. Resolve stacked modifiers first, then crystal response. Investigate optional extended-light provider before final balance.

### Freeze QA -> controlled refactor -> 0.1.0

Do not combine large source refactors with new gameplay.

### 1.1.0+

Mirage Scan Codex + searchable persistent scan library + Paper/card copy station.
