# Changelog

## 0.1.0-dev.8 — Projection Core / Power foundation

- Added the first real **Projection Core Socket** to the Compact Mirage Projector GUI and BlockEntity.
  - Core slot accepts Glass, Quartz, Amethyst, Diamond and Netherite material families.
  - Accepted raw forms: Glass block, Quartz, Amethyst Shard, Diamond and Netherite Ingot.
  - Corresponding material blocks are also accepted for convenience.
  - Core inventory is server-authoritative, registry-aware NBT persisted and synchronized through the BlockEntity update tag.
  - Breaking/replacing the projector drops both the projected ItemStack and the installed Core exactly once.
  - dev.7 worlds without Core NBT migrate to the intended Compact starter state with a Glass Core.
- Added provisional **Projection Core material profiles**:
  - Glass: 8 Power, 10 px scale, 16 px lift, 1 px float.
  - Quartz: 16 Power, 16 px scale, 32 px lift, 2 px float.
  - Amethyst: 48 Power, 48 px scale, 64 px lift, 8 px float.
  - Diamond: 96 Power, 80 px scale, 96 px lift, 16 px float.
  - Netherite: 192 Power, 160 px development scale/lift, 32 px float.
  - Source-capacity fields are already present for the future multi-source chassis pass.
- Added the first reusable **Projection Power budget** calculator.
  - Power cost currently accounts for Plane/Item scale, 3D Item surcharge, lift, rotation, floating, rotation-synced floating and Independent Back image usage.
  - Core hard limits and total Power are validated independently.
  - Settings are never destroyed when a weaker Core is installed: an invalid Mirage simply becomes inactive and comes back when power/limits are satisfied again.
- Added the first real **double-limit chassis/core rule** for Compact.
  - Normal Compact envelope: max 10 px scale, 32 px lift and 4 px float.
  - A development-only `Debug chassis` toggle can bypass the Compact chassis envelope in Creative Mode while still respecting the installed Core.
  - Server rejects the debug override for non-Creative players.
  - Float amplitude can never exceed Projection Lift because bobbing moves downward and may not pass through the physical pedestal.
- Added **dynamic Core rendering** to the pedestal.
  - Removed the hardcoded diamond cuboid from the block model.
  - The 2x3x2 px center column is now rendered from the installed Core material (Glass/Quartz/Amethyst/Diamond/Netherite).
  - Removing the Core visually removes the center material and disables projection output without deleting settings.
- Expanded the GUI with:
  - dedicated Core slot;
  - current Core name and material limits;
  - live `used / available` Projection Power bar;
  - explicit inactive reason when a Core/chassis/physical limit is exceeded;
  - labels for Core vs projected Item slots;
  - Creative-only `Debug chassis` toggle.
- Added explanatory hover tooltips to Scale, Lift, 360° rotation period, Float amplitude, Float cycle and Float-leg/rotation-sync sliders.
- Bumped the Mirage payload registrar protocol from `2` to `3` because the projector settings payload gained the debug-chassis field.
- Kept Minecraft 1.21.1 / NeoForge **21.1.244** / Java 21 fixed.
- Bumped development version to `0.1.0-dev.8`.

### Validation state

- dev.6 remains the last explicitly reported renderer baseline in this conversation; dev.7 was reported by the user as progressing very well.
- dev.8 has JSON/static structural validation in this environment; Windows `build.bat` + in-game Core/Power QA remains required.
- Highest-risk live checks: dynamic Core item rendering scale/alignment, menu slot indices/shift-click, Core removal/reinsert restoring a previously invalid Mirage, and Creative-only debug override behavior.


## 0.1.0-dev.7 — multiplayer asset transport, Item Mode and clearance pass

- Confirmed `0.1.0-dev.6` as the current in-game Plane renderer baseline after the large-angle/culling and transparent Front/Back fixes were exercised successfully.
- Added the first complete multiplayer image-asset transport path.
  - Normalized PNG assets are uploaded from the client in 32 KiB chunks.
  - Uploads are reassembled server-side and rejected unless the asset id is a valid SHA-256, size is within the 4 MiB normalized cap, PNG signature is present and the recomputed SHA-256 matches.
  - Valid assets are stored world-locally under `<world>/mirage_projector/assets/<sha256>.png`.
  - Server storage deduplicates by SHA-256.
  - Clients that do not have an asset request it automatically when the DynamicTexture cache misses.
  - Server -> client downloads are also chunked, reassembled and SHA-256 verified before the local cache is written.
  - Added retry throttling, stale session cleanup and active-upload limits.
  - Invalid/non-SHA asset ids are now sanitized out before becoming projector state.
  - Completed client downloads are written through a temporary file and atomically moved into cache when supported, avoiding partially-written cache entries.
  - Added logout cleanup for runtime transfer state and registered DynamicTextures.
  - Cached assets opportunistically seed the server when first loaded, helping migrate pre-dev.7 worlds to the new server asset store.
- Added first-pass **Item Mode**.
  - Mirage Projector BlockEntity now owns a real one-slot `ItemStackHandler`.
  - Added a dedicated projected-item slot to the GUI plus normal player inventory/hotbar slots.
  - Added `Projection source: Image / Item` toggle.
  - Item projection uses the vanilla `ItemRenderer` / `ItemDisplayContext.FIXED` path with fullbright lighting.
  - Item Mode shares Scale, Projection Lift, rotation and floating controls with Image Mode.
  - Stored ItemStack persists through registry-aware NBT and synchronizes through the BlockEntity update tag.
  - Breaking/replacing the projector extracts and drops the stored ItemStack once.
- Added first-pass **projection clearance** feedback.
  - GUI scans a conservative projection envelope roughly every 10 ticks.
  - Scan accounts for Image vs Item source, image aspect ratio/item scale, Projection Lift and float amplitude.
  - GUI reports either `Clearance: clear` or the number of intersecting non-air blocks.
- Added small `×` controls beside Front and Back import buttons so either image reference can be cleared without replacing it.
- Updated the GUI layout to accommodate Item Mode and the player inventory.
- Kept Minecraft 1.21.1 / NeoForge `21.1.244` / Java 21 as the fixed baseline.
- Bumped development version to `0.1.0-dev.7`.

### Validation state

- dev.6 is the last confirmed build/run/visual baseline.
- dev.7 has syntax/static source validation only in this environment because Gradle/NeoForge dependencies cannot be downloaded here.
- The next Windows pass should run `build.bat`, then test Image regressions, two-client asset synchronization, Item Mode/glint/drop behavior and clearance warnings.


## 0.1.0-dev.6 — projection visibility and true two-sided face isolation

- Confirmed `0.1.0-dev.5` builds, launches, opens the native image picker and successfully projects imported images in-game on Minecraft 1.21.1 + NeoForge 21.1.244.
- Fixed large/elevated projections disappearing at some close camera angles, especially while looking diagonally upward with the physical pedestal outside the normal frustum.
  - The BlockEntityRenderer now opts into off-screen rendering inside its normal view-distance limit while the projection system is still in the debug/stress-test phase.
  - The projection-aware AABB remains in place for future optimized visibility/LOD work.
- Fixed translucent front/back face bleed-through.
  - dev.5 rendered both textured quads simultaneously; transparent pixels could therefore expose the reverse face and create doubled/ghosted silhouettes.
  - dev.6 classifies the camera against the rotating projection plane every frame and renders **only the physically visible side**.
  - Front, Mirrored back, Readable back and Independent back modes all obey the same one-visible-face rule.
- Removed the tiny back-face Z epsilon because only one side is rendered at a time; both sides now occupy the exact same 2D plane.
- Kept the project baseline fixed at NeoForge `21.1.244`.
- Bumped development version to `0.1.0-dev.6`.

### Validation state

- `dev.5` is confirmed build/run-functional and successfully reaches the first real imported-image projection milestone.
- `dev.6` source is prepared for Windows `build.bat` validation and targeted visual QA of the two reported renderer defects.

## 0.1.0-dev.5 — first in-game visual/input fixes

- Confirmed `0.1.0-dev.4` builds and launches correctly on the project baseline: Minecraft 1.21.1 + NeoForge 21.1.244 + Java 21.
- Replaced the AWT `FileDialog` image picker with LWJGL `TinyFileDialogs`, matching Minecraft's native-window stack so the Import buttons can open a real OS file chooser from the game.
- Kept PNG/JPG/JPEG filters and the existing asynchronous normalization/hash/cache pipeline after selection.
- Added a dedicated **Mirage Projector** Creative Mode tab while keeping the projector available in Functional Blocks.
- Corrected the compact projector block model against the Blockbench reference:
  - center column now uses the vanilla diamond-block texture;
  - the glass deck now uses the full vanilla glass top UV instead of cropping away its visible border;
  - glass-deck side faces now use `glass_pane_top`, matching the intended panel edge from the Blockbench model.
- Removed the extra 20° X tilt from the empty-state book so it renders upright instead of leaning as if it were lying toward the ground.
- Bumped development version to `0.1.0-dev.5`.

### In-game findings that motivated this patch

- dev.4 GUI opened correctly from right click.
- Empty-state book rendered, rotated and bobbed correctly.
- Import buttons did not surface a file picker.
- The physical block rendered mostly as obsidian because the glass top UV omitted the visible border and the core was incorrectly textured as glass.
- No dedicated Mirage Projector creative tab existed.

### Validation state

- dev.4 is the last confirmed build/run-clean baseline on NeoForge 21.1.244.
- dev.5 source is prepared for the next Windows `build.bat` validation; the artifact environment could not fetch the Gradle distribution for a local full compile.

## 0.1.0-dev.4 — restore project NeoForge baseline

- Corrected the Minecraft 1.21.1 NeoForge target from `21.1.249` to the project-standard `21.1.244`.
- Bumped development version to `0.1.0-dev.4`.
- Kept the single `build.bat` workflow and all projection functionality from dev.3 unchanged.
- Documented NeoForge `21.1.244` as a fixed project baseline: future MDK updates must not silently change it.
- Recorded that dev.3 built successfully on Windows, but against the wrong NeoForge revision; dev.4 therefore requires a fresh validation build.

### Functional state

- No projection behavior was intentionally changed in this patch.
- The next functional milestone remains real multiplayer image-asset transport after dev.4 is confirmed build-clean on NeoForge 21.1.244.

## 0.1.0-dev.3 — single-script Windows build bootstrap

- Replaced the dev.2 three-script Windows toolchain with one `build.bat`.
- Removed `INSTALL-GRADLE.bat` and `RUN-CLIENT.bat`.
- `build.bat` now owns the complete workflow: Java 21 discovery, local Gradle bootstrap and JAR compilation.
- Reused the proven build-helper pattern from the project's other Minecraft mods instead of maintaining a separate installer.
- Added robust Java 21 discovery across `JAVA_HOME`, explicit JDK 21 variables, Temurin/Adoptium, Microsoft JDK, Corretto, IntelliJ `.jdks`, Prism Launcher and finally PATH.
- Java versions other than 21 are ignored rather than silently selected.
- Gradle 9.2.1 is downloaded only when missing and stored under `.gradle-dist/gradle-9.2.1/`.
- Gradle download uses `curl.exe` when present with a PowerShell fallback.
- The build runs `--no-daemon clean build --stacktrace`.
- Both failure and success paths explicitly `pause`, so double-click execution cannot hide compiler/download errors.
- Updated `.gitignore`, README MASTER and development notes for the single-script invariant.
- Bumped development version to `0.1.0-dev.3`.

### Functional state

- No projection functionality from dev.2 was removed or intentionally changed in this patch.
- The next functional milestone remains the real multiplayer image-asset transport.

## 0.1.0-dev.2 — independent faces, render bounds and one-click toolchain

- Added a real second image asset to projection settings for `Independent` back-face mode.
- Added separate front/back image import buttons in the projector GUI.
- Added independent SHA-256 asset IDs and dimensions for both faces.
- Added NBT persistence, menu synchronization and serverbound settings serialization for the second face.
- Updated the renderer so `Independent` can use a different texture on the reverse side.
- Independent faces preserve their own aspect ratio. Both use the same configured `scalePixels` as their maximum dimension and share the same lower anchor/rotation axis.
- If Independent mode has no back asset, the front asset remains a readable fallback instead of rendering nothing.
- Added a dynamic BlockEntity render bounding box based on projection scale, lift and float amplitude. This prepares the renderer for large/elevated debug projections without relying on rendering every projector off-screen.
- Fixed client screen/BlockEntity renderer registration to explicitly subscribe on NeoForge's MOD event bus.
- Removed unconditional `shouldRenderOffScreen=true`; frustum culling can now use the enlarged projection-aware AABB.
- Added `INSTALL-GRADLE.bat`.
  - Installs Gradle 9.2.1 locally under `.tools/`.
  - Verifies the official Gradle 9.2.1 binary archive SHA-256 before extraction.
  - Does not modify the global Windows PATH.
  - Attempts to generate/update the project Gradle Wrapper after installation.
- Added `BUILD.bat`.
  - Double-click build entry point.
  - Validates Java 21.
  - Automatically runs the Gradle installer when necessary.
  - Runs a clean build and prints the generated JAR name/location.
- Added `RUN-CLIENT.bat` for one-click `runClient` development launches.
- Added `.tools/`, Gradle, build and run directories to `.gitignore`.
- Added the Gradle 9.2.1 distribution checksum to `gradle-wrapper.properties`.
- Bumped development version to `0.1.0-dev.2`.

### Known dev.2 limitations

- Image bytes are still local-client assets. The importing client can render them, but the permanent client → server → other clients asset protocol is not implemented yet.
- WebP is still planned but not enabled; current importer is PNG/JPG/JPEG.
- `Independent` now supports two real images, but both are still local-client cached until multiplayer asset transfer lands.
- Item, Banner, Prism, Effigy, Core/Power and larger chassis systems remain roadmap work.
- The Gradle bootstrap scripts are prepared for Windows testing. This snapshot has not been able to download the Gradle/NeoForge toolchain inside the artifact container, so the first real compile remains to be run with `BUILD.bat` on the user's Windows environment.

## 0.1.0-dev.1 — initial implementation snapshot

- Added the compact Mirage Projector block and block entity architecture.
- Added the first configurable flat-image projection state.
- Added dynamic BlockEntityRenderer path for imported images.
- Added empty-state floating vanilla book renderer.
- Added rotation controls and clockwise/counter-clockwise direction.
- Added time-based and rotation-synced floating modes.
- Added projection lift, amplitude, scale and rotation offset settings.
- Added back-face modes: Mirrored, Readable and Independent (second independent image import was reserved for a later dev build).
- Added client image importer and normalized PNG cache for PNG/JPG/JPEG sources.
- Added SHA-256 image IDs and aspect-ratio metadata.
- Added configuration GUI and server-authoritative settings payload.
- Added NBT persistence and block-update sync for projection settings.
- Added development-scale limits far above the compact projector's eventual production envelope so the renderer can be stress-tested before other chassis exist.
- Added the Blockbench reference model under `docs/reference/`.
- Added exhaustive master README documenting the complete intended system and future roadmap.
