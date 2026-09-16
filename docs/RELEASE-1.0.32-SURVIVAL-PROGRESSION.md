# Mirage Projector 1.0.32 — Survival Progression & Illumination Identity

The original 1.0.32 Survival-progression wave shipped on network protocol **42** with `ProjectionSettings` serialization format **4**. The later dedicated Table runtime hotfix keeps format **4** but advances the current 1.0.32 artifact to protocol **43** so the main menu carries authoritative chassis identity.

## Mirage Flashlight

- The former Mirage Lantern is publicly and internally renamed **Mirage Flashlight**.
- Java symbols use `MirageFlashlightItem`, `MIRAGE_FLASHLIGHT` and `ClientHeldFlashlights`.
- The stable item registry ID remains `mirage_projector:mirage_lantern` and the existing `MirageLantern...` ItemStack data keys remain unchanged so old configured devices migrate without remapping or losing their cell/mode.
- The item presentation is rebuilt as a compact 3D flashlight using Crying Obsidian, Magenta Stained Glass and small metal/copper accents.
- Sneak + right-click on the top of a sturdy block places the Flashlight temporarily. The placed form owns the same mode/cell and breaking it returns exactly one Flashlight with that state.

## Illumination chassis

- The floor **Mirage Light Projector** now uses Iron Block as its dominant casing language, Crying Obsidian bands on multiple sides and a magenta lens.
- The presentation/Data-show chassis historically registered as `mirage_projector:mirage_wall_projector` is now publicly/code-named **Mirage Wall Display**; its registry ID remains unchanged.
- A separate true wall-mounted illumination chassis is introduced as `mirage_projector:mirage_wall_illuminator`, public name **Mirage Wall Projector**.
- Mirage Wall Projector mounts only to a sturdy vertical face, emits perpendicular to that wall and reuses the existing rechargeable-cell + Focus/Flood/Ambient/Off block-entity/menu/runtime contract.
- Visible yaw/pitch aiming for the floor Mirage Light Projector remains intentionally deferred.

## Survival recipes

Committed recipes now exist for:

- Light Battery;
- Mirage Flashlight;
- Mirage Light Projector;
- Mirage Wall Projector;
- Shoulder Strap;
- Auto Battery Swap Patch;
- Shoulder Strap Slot Expansion;
- Charging Station;
- Mirage Hand Projector;
- Mirage Scan Codex;
- Mirage Table Projector;
- Mirage Wall Display.

Light Battery uses the frozen layout `GCG / IGI / GRG`: five Glow Dust media in an X, Copper above, Redstone below and Iron on both sides. `mirage_projector:glow_dust_media` accepts either full vanilla Glowstone Dust or Mirage's rechargeable Glow Dust. The existing ItemCrafted charge hook therefore preserves the average charge of the five media. Glow Dust itself intentionally has no crafting recipe because its progression is derived from vanilla Glowstone Dust and the recharge loop.

## Compatibility

No existing registry ID is renamed. No network payload changes are introduced. The public rename and Java cleanup are separated from persistent identifiers on purpose.
## JEI / EMI

All twelve 1.0.32 Survival-progression recipes are standard `minecraft:crafting_shaped` RecipeManager entries. JEI and EMI therefore expose them natively under Crafting; Mirage does not register duplicate wrappers for these recipes. The Light Battery entry uses `mirage_projector:glow_dust_media`, so its five Glow Dust positions expose both vanilla Glowstone Dust and rechargeable Mirage Glow Dust in compatible viewers. Custom viewer wrappers remain limited to recipe types that actually need them, such as projector chassis upgrades and EMI's Crying Obsidian presentation.
## Overlay/build hotfix

The cumulative pre-build cleanup now explicitly tombstones the pre-rename `ClientHeldLanterns.java` and `MirageLanternItem.java` sources. This is required when a 1.0.32 source snapshot is copied over an older 1.0.31 Windows project directory, because archive extraction cannot delete files that disappeared from the newer archive. The cleanup no longer relies on the old `CALL :delete_file` helper and performs its cumulative tombstones inline before Gradle compilation.


- Fixed Mirage Table Projector image visibility across rotation angles by using the correct horizontal-plane front normal; zero-tilt table rotation no longer flips front/back classification.
