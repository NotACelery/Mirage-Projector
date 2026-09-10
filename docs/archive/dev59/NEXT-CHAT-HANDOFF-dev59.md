# Mirage Projector — next-chat handoff after dev.59

Current line: **0.1.0-dev.59**.

## Baseline

dev.59 is the full audit/rake based on dev.58. It does not intentionally change gameplay behavior except low-risk cleanup of compatibility structure and logging/formatting.

## Most important current state

- six projector chassis are active;
- Image/GIF, Item, Banner and Entity source families are implemented;
- Power/overdrive and state-preserving chassis upgrades are implemented;
- one configurable Core Booster replaces the old five user-facing Improved Core variants;
- Crying Obsidian renewable crystals, Beacon absorption/residual rays and Obsidian Spike are implemented;
- old five Improved Core block IDs remain migration-only;
- network protocol remains 18.

## Immediate unresolved issue

dev.58 still needs Windows build + in-game QA for semi-transparent Entity rendering at cloud altitude and modded armor such as Create's Netherite Backtank. This is P0 and should be fixed before another large gameplay branch if it still fails.

## Next feature branch after renderer stabilization

Core Booster Beacon relay:

- Glass / Diffusion;
- Quartz / Radiance;
- Amethyst / Resonance;
- Diamond / Focus;
- Netherite / Inversion;
- maximum four effective Boosters;
- additive/capped width, target cap around ×2 vanilla;
- preserve stained-glass hue;
- resolve effective beam first, then let downstream Crying Obsidian crystals react.

Parallel backlog: useful-range powered crystal lighting and per-equipment-channel visibility toggles.

## Documentation rule

Read `DOCUMENTATION-AUTHORITY.md` first. Do not promote anything from `docs/archive/pre-dev59/` back into active requirements without checking current source and `ROADMAP.md`.

## Snapshot rule

Every subsequent wave must be delivered as a recoverable source snapshot even if build/QA is incomplete. Exclude `.gradle`, `.gradle-dist`, `build`, `run`, IDE caches and downloaded/generated toolchain artifacts.

## dev.59 audit result

Final static audit before packaging reported zero findings for the current code-style/resource checks: 99 Java files, 94 runtime JSON resources and 423 localization keys per language were reconciled. The source remains **not build-clean until Windows `build.bat` succeeds**.
