# Mirage Projector — Current State & Roadmap (dev.43)

## Baseline

- Current source line: `0.1.0-dev.43`
- Network protocol: 18
- Last explicitly confirmed in-game behavioral checkpoint before the post-dev.40 waves: dev.40 Piglin/Hoglin shaking fix.
- dev.41: documentation/code consolidation.
- dev.42: Shard + physical chassis/Core-Chamber source wave.
- dev.43: renewable crystal/Beacon ecosystem source wave.

`dev.43` must not be called build-clean until Windows `build.bat` and in-game QA pass.

## Completed source waves

### dev.42 — material / optical chassis foundation

Implemented:

- Crying Obsidian Shard;
- Stonecutter/re-form recipes;
- broad Glass-sheet retirement;
- composite Obsidian/Crying-Obsidian/Glass chassis language;
- universal Core Chamber;
- real installed Core ItemStack visual.

### dev.43 — renewable Crying Obsidian crystal ecosystem

Implemented in source candidate:

- Small/Medium/Large/Mature Crying Obsidian crystals;
- downward renewable growth under Crying Obsidian + Lava source;
- deliberately slow first nucleation and progressively easier later growth;
- exact 1/2/3/4 shard harvest economy;
- Silk Touch stage recovery;
- no Fortune bonus;
- Ruined Portal / Mineshaft / village smith shard loot;
- age-dependent Beacon excitation and fallback block light;
- age-dependent vertical visual transmission;
- Mature visual beam termination without disabling Beacon gameplay;
- narrow purple residual refraction leaks;
- stacked-crystal attenuation.

## Next order

### dev.44 — Obsidian Spike

Implement the already-defined shard trap:

- 3 Crying Obsidian Shards;
- 2 String;
- 1 Stick;
- approximately half-block height;
- nine tips, central tip thinner/taller;
- bush-like movement hindrance;
- 2.0 damage points per successful hurt event.

Do not mix projector-upgrade persistence into this wave.

### dev.45 — state-preserving projector upgrade crafting

Build the infrastructure before recipes:

1. identify canonical persistent projector state;
2. consume one projector as recipe input;
3. copy its complete state to the upgraded chassis ItemStack;
4. normalize only truly chassis-incompatible data;
5. prove Image/GIF/Entity/Banner/Item/Power settings survive.

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

Display recipe contract already defined:

```text
Q S A
S M S
A S Q
```

Prism recipe contract:

```text
S G S
G D G
S G S
```

Wide/Tall must have equal total cost but layout-signaling recipe shapes. Field must be substantially more expensive and use whole Crying Obsidian prominently.

### dev.46 — Improved Cores

Exactly five material cores:

- Glass
- Quartz
- Amethyst
- Diamond
- Netherite

Current target:

- same Base PU as standard material;
- ~×1.50 Core amplification;
- three nested dark-purple shells;
- middle shell physically rotated, not texture-faked;
- material visible at center;
- usable as projector Core and decorative block.

Balance amplification only after dev.42–45 gameplay costs are observable in-game.

### dev.47 — Improved-Core Beacon relay

Implement:

- relay transition at block midpoint Y+0.5;
- wider outgoing beam;
- max four effective Core modifiers;
- stained glass keeps hue control;
- Glass Diffusion;
- Quartz Radiance;
- Amethyst Resonance;
- Diamond Focus;
- Netherite Inversion.

Resolve all stacked Core modifiers into one effective beam first, then evaluate Crying-Obsidian crystal response.

A width amplifier must make the crystal visibly more excited. A radiance amplifier must increase useful crystal lighting where the lighting backend supports it.

### Optional extended-light integration investigation

Do this before final dev.47 balance:

- verify a maintained NeoForge 1.21.1 provider;
- prefer optional integration, not mandatory dependency;
- target richer crystal curve around 14/18/23/28 if safe;
- base mod must retain vanilla fallback;
- do not own a global light-engine rewrite.

### Feature/render freeze QA

Before release:

- Entity/projector depth ordering;
- Ghost/water;
- translucent entities;
- eyes/glint/special RenderTypes;
- GIF stress/multiplayer asset transfer;
- all Image chassis modes;
- crystal Beacon render under graphics modes/shaders;
- state-preserving upgrades;
- all Core tiers.

### Controlled refactor

Only after behavior freeze, split large hotspots such as:

- `MirageProjectorRenderer`;
- `MirageProjectorBlockEntity`;
- `ImageProjectorScreen`;
- `ProjectionPower`.

Do not combine a giant refactor with new gameplay.

### 0.1.0 release preparation

After feature freeze, QA and refactor stabilization.

### 1.1.0+

Mirage Scan Codex / searchable scan library + Paper/card copy station.

## Brainstorm / deliberately not scheduled

- unrefined Crying-Obsidian Cluster Core with intentionally refracted/distorted projections;
- Glowstone has no assigned role;
- no automatic assumption that every brainstorm becomes a Core variant.
