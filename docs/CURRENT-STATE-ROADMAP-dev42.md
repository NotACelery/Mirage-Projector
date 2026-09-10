# Mirage Projector — Current State & Roadmap (dev.42)

## Baseline

- Version: `0.1.0-dev.42` source candidate.
- Protocol: 18.
- dev.40: last confirmed in-game behavioral baseline before dev.41 documentation closure.
- dev.41: documentation/code-cleanup consolidation.
- dev.42: first material/visual implementation wave after that closure.

## dev.42 scope status

Implemented in source:

1. Crying Obsidian Shard item.
2. Stonecutter 1 Crying Obsidian -> 4 shards.
3. Re-form recipe with Fire Charge or Magma Cream.
4. Crying-Obsidian optical emitter material.
5. six-chassis physical model rework.
6. universal Glass Core Chamber.
7. actual installed Core item floating/rotating inside chamber.
8. old fake block Core renderer removed.

Pending before dev.42 can become stable/build-clean:

- Windows Gradle build;
- in-game physical-model QA;
- Core visibility from all angles;
- all source-mode regression checks.

## Next implementation order

### dev.43 — renewable Crying Obsidian crystal ecosystem

Implement in this dependency order:

1. register four age blocks: Small Bud, Medium Bud, Large Bud, Mature Cluster;
2. use Amethyst Bud/Cluster geometry language with original Crying-Obsidian textures, not copied copyrighted mod art;
3. only Crying Obsidian with a Lava source directly above can nucleate a downward Small Bud;
4. initial empty->Small transition is slowest;
5. Small->Medium faster; Medium->Large faster; Large->Mature fastest;
6. no-Silk drop exactly 1/2/3/4 Crying Obsidian Shards by age;
7. Silk Touch returns the actual age block;
8. no Fortune bonus initially;
9. add reviewed shard loot to Ruined Portals, Mineshafts, smith/blacksmith-relevant structure pools, and other justified Obsidian/Crying-Obsidian sources;
10. add normal and energized textures/models for all ages;
11. detect active Beacon beam interception without disabling Beacon gameplay;
12. visual upward transmission target by age: ~75/50/25/0%;
13. mechanical powered-light behavior scales by age;
14. Mature crystal residual side beam: about half Beacon inner-beam width, Crying-Obsidian purple, one active at a time, max ~3–4 blocks;
15. side beam grows strongly from cluster, reaches full length, holds roughly one second, then retracts while fading;
16. raycast/collision prevents residual light from rendering through solid walls;
17. investigate a maintained NeoForge 1.21.1 extended-light integration before finalizing >15 behavior.

### Improved-Core / crystal coupling rule

When dev.47 eventually modifies a Beacon before it reaches a crystal:

- wider beam -> visibly stronger crystal excitation/residual refraction;
- higher-radiance beam -> greater useful crystal world lighting;
- stacked Core effects resolve to one effective beam first, then crystal response is derived once;
- stained glass remains hue authority;
- if >15 light is unavailable, prefer a safe larger illumination radius/slower effective decay over pretending the source level itself can exceed vanilla limits.

### dev.44 — Obsidian Spike

- 3 shards + 2 String + 1 Stick;
- half-block-ish, 9 tips, central tip thinner/higher;
- bush-like slowdown;
- 2 damage points per successful hurt event.

### dev.45 — state-preserving projector upgrades

First implement one canonical state-transfer path. Then add:

```text
Mirage -> Display -> Wide / Tall / Prism / Field
```

No recipe is allowed to erase Core, image/GIF references, cards, virtual equipment, banners, item snapshots or projector presentation settings.

### dev.46 — Improved Cores

- Glass, Quartz, Amethyst, Diamond, Netherite;
- same Base PU as standard material;
- initial amplification target ~x1.50;
- decorative block + installable Core item;
- three nested dark-purple shells;
- middle shell genuinely rotated geometrically;
- central material visible.

### dev.47 — Improved-Core Beacon relay

- beam relay starts at block Y+0.5;
- wider output common to all;
- Glass Diffusion;
- Quartz Radiance;
- Amethyst Resonance;
- Diamond Focus;
- Netherite Inversion;
- max four effective relay Cores;
- global beam-width cap around 2x vanilla;
- feed final beam characteristics into powered crystal logic.

### feature freeze / QA

After dev.47, stop adding major systems until deep render/gameplay QA passes.

### refactor

Only after behavior freeze, split large renderer/BlockEntity/Power/Screen classes one recoverable refactor at a time.

### 0.1.0

Release preparation after recipes/balance/QA/refactor are stable.

### 1.1.0+

Mirage Scan Codex and paper-copy station remain future UX work.

## Brainstorm only

Glowstone currently has no assigned role. Do not replace the Glass Core or add Glowstone recipes without a new explicit design decision.

