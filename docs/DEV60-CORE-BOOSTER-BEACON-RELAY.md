# dev.60 — Core Booster Beacon relay

## Scope

dev.60 implements the first complete loaded-Core-Booster Beacon relay pass on top of the dev.59 audited baseline. It deliberately does not modify the unresolved P0 semi-transparent Entity renderer.

## Relay accumulation

Relay state is evaluated from the Beacon upward. Only loaded `core_booster` blocks contribute. Empty Boosters are ignored. The first four loaded Boosters are effective; later Boosters remain physical but do not add modifiers. Stained-glass Beacon section color remains authoritative.

## Material identities

- Glass / Diffusion: +35 percentage points width and a broader outer beam.
- Quartz / Radiance: +25 percentage points width and increased luminance.
- Amethyst / Resonance: +25 percentage points width and ×1.25 rotation speed per effective Amethyst, capped near ×2.
- Diamond / Focus: +25 percentage points width and a tighter inner beam relative to the outer beam.
- Netherite / Inversion: +25 percentage points width and reversed outgoing rotation. Multiple Netherite Boosters do not toggle the direction back.

Width is additive and capped near 200% vanilla.

## Crying Obsidian coupling

Crystal attenuation still applies after the relay modifiers that exist below that crystal have been accumulated. Residual rays receive a bounded portion of upstream relay width/radiance/rotation state. Extended useful-range block lighting is intentionally not part of dev.60.

## QA

1. Build on Windows with Java 21.
2. Test one Booster of each material in a clear active Beacon column.
3. Verify stained glass still controls hue before and after Booster blocks.
4. Stack 1–5 Boosters and verify the fifth adds no further modifier.
5. Verify Glass is visibly widest, Quartz brighter, Amethyst faster, Diamond more focused and Netherite reversed.
6. Put Crying Obsidian buds/clusters above modified beams and verify attenuation/cut behavior still works.
7. Re-test direct Mature-on-Beacon suppression.
8. Re-test the unresolved cloud/ghost renderer independently because dev.60 does not change it.
