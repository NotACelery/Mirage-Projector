# dev.69 — Occlusion-aware reflected light and Core Booster identity

## Scope

dev.69 keeps the dev.68 Mature Cluster half-decay baseline and immediate teardown, but changes how reflected light is accepted through geometry and how Core Booster materials influence Crying Obsidian after the Beacon reaches it.

Network protocol, projector state formats, recipes and migration IDs are unchanged.

## Vanilla-like geometry contract

Minecraft block light stores scalar light levels rather than a per-voxel source direction. Mirage therefore does **not** attempt to mark or manually shade individual block faces.

Instead, Mirage controls where auxiliary sources may exist:

- a fully light-blocking state between the Mature source and a candidate node rejects that candidate;
- partially light-blocking states add extra attenuation instead of being treated as air;
- a rejected branch cannot create a new full-strength light source behind the blocker;
- after candidate placement, Minecraft's own block-light propagation, corner wrap, ambient occlusion and face shading produce the local result.

This means a wall can cast a real dark side while light is still allowed to reach around its edges naturally, rather than Mirage making an artificial hard-black volume.

## Reflected field topology

The no-Booster baseline remains six axial branches with half-speed falloff and approximately 29-block unobstructed reach.

Glass/Diffusion can activate twelve face-diagonal secondary directions. These are deliberately shorter/dimmer than the primary axes and are prebounded to a 25-block candidate distance. No body-diagonal or full cube scan is used.

Every actual node remains level 1–15, invisible, internal-only, replaceable, non-item and no-loot.

## Reflected Core Booster semantics

Incoming Beacon rendering still uses the dev.60 relay values. Reflected Crying Obsidian behavior now maps the material explicitly instead of using generic Beam `widthScale` as a light-power tier.

- **Glass / Diffusion**: broadens reflected coverage through face-diagonal branches and broadens residual ray geometry. Each effective Diamond Focus cancels one reflected Glass diffusion tier.
- **Quartz / Radiance**: dominant static reflected-range amplifier; also increases residual ray brightness, length and excitation.
- **Amethyst / Resonance**: increases reflected residual activity/rotation through the existing resonance multiplier; it grants no free static world-light range.
- **Diamond / Focus**: adds a smaller focused axial reach bonus, counteracts Glass diffusion and tightens residual ray geometry. Its static range contribution is intentionally weaker than Quartz.
- **Netherite / Inversion**: preserves reflected rotation reversal and grants no free static world-light range.

Only the first four loaded Boosters remain effective.

## Axial conceptual reach

The Mature source itself remains vanilla level 15. Auxiliary nodes emulate conceptual levels above 15 only by holding high vanilla values farther from the source.

The reflected axial tier is:

```text
Quartz count + ceil(Diamond count / 2)
```

clamped to 0–4.

Representative unobstructed axial maxima:

| Effective mix | Reflected tier | Approx. max axial candidate distance |
|---|---:|---:|
| none / Glass / Amethyst / Netherite | 0 | 29 |
| 1 Quartz | 1 | 31 |
| 2 Quartz | 2 | 33 |
| 4 Quartz | 4 | 37 |
| 1–2 Diamond | 1 | 31 |
| 3–4 Diamond | 2 | 33 |
| 3 Quartz + 1 Diamond | 4 | 37 |

No real block-light value exceeds 15.

## Source refresh optimization

`CryingObsidianLightField.refresh(...)` now resolves the Beacon relay once into a per-source field specification and reuses it across all candidate positions. dev.68 effectively re-scanned downward toward the Beacon for every valid Mature candidate during source refresh.

Inverse node self-validation remains bounded by the fixed candidate topology because light nodes intentionally carry no owner BlockEntity/NBT payload.

## Immediate teardown and overlap

dev.68 semantics are preserved. Removing/de-energizing a Mature source immediately recalculates affected Mirage nodes while excluding that source. Orphan nodes disappear immediately; shared nodes remain or downgrade according to surviving sources. The expanded Glass diffusion candidate set participates in the same reconciliation.

## QA

See `QA-REGRESSION.md`, section **dev.69 occlusion-aware reflected light**. Windows Java 21 compilation and in-game QA are still required before this source candidate can be called build-clean/runtime-clean.
