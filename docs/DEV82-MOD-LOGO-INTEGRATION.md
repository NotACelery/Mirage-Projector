# dev.82 — Mod logo integration

Version: **0.1.0-dev.82**  
Network protocol: **27**

## Goal

Package the chosen Mirage Projector promo render as the mod logo without reopening the dev.81 architectural work or changing runtime behavior.

## Implementation

- Added `src/main/resources/logo.png`.
- Added `logoFile="logo.png"` to `src/main/templates/META-INF/neoforge.mods.toml`.
- Added `logoBlur=false` so the logo is shown crisply in metadata-driven UIs.

## Scope

This pass does **not** change:

- network protocol;
- projection sources or registries;
- projector models, VoxelShapes, item transforms, or textures;
- Mirage Light behavior;
- recipes, registries, saves, or GUI logic.

## Verification

- `python tools/verify_dev82_mod_logo_integration.py`
- `python tools/verify_current_line.py`
- normal Windows Java 21 `build.bat` smoke pass
