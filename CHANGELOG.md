# Mirage Projector 0.1.0-dev.22

## Added

- Added eight persistent **Humanoid pose presets**: Standing, Guard, Hero, Combat, Raised Main Hand, Raised Off Hand, Dual Wield and Display.
- Added a translated pose-cycle control to the Humanoid Entity Workspace. Pose is projector state, independent from body scans and all equipment snapshot UUIDs.
- Added a client-only Humanoid model hook that applies Mirage-owned rotations after vanilla `HumanoidModel#setupAnim`; armor layers and hand-held items therefore inherit the same virtual rig pose.
- Added pose-aware Entity preview auto-fit, conservative clearance and world render bounding boxes.
- Added `docs/HUMANOID-POSES-dev22.md`.

## Changed

- Pose selection persists through save/reload and does not recapture, consume, replace or move any physical ItemStack.
- Non-Humanoid Entity scans and standalone Item-mode equipped armor remain outside the pose binding, preventing the Humanoid preset from leaking into unrelated render paths.
- Debug Handbook now documents functional Prism and the first Humanoid pose pass instead of listing both as future placeholders.
- Network protocol bumped from **9** to **10**.
- Development version bumped to `0.1.0-dev.22`.

## Validation

- Static source/resource validation performed in the assistant environment.
- Humanoid clearance is intentionally conservative, not exact per-model-part geometry. Exact species/entity bounds for Horse/Generic remain a separate pending pass.
- Windows NeoForge `build.bat` and in-game QA are still required before this snapshot can be called build-clean.
- Priority QA: Player/Zombie/Skeleton/bodyless rig, all eight poses, main-arm handedness, armor/trims/dye/glint, tools/shields in both hands, save/reload, multiplayer sync, Ghost/Tint and obstruction preview.

# Mirage Projector 0.1.0-dev.21

## Added

- Added the physical **Mirage Prism** block/item as a real shared projector chassis.
- Added persistent independent Image sources for **North / East / South / West**. Front maps to North and Back maps to South for compatibility; East/West are new persisted asset channels.
- Added four active Prism preview/import/clear cards to the Image Workspace.
- Added **Same Source on All Faces**, copying North's asset reference and dimensions across all four faces without duplicating image files.
- Added the first in-world Prism renderer: four lateral image quads around an open square, deliberately without top/bottom faces.
- Added `docs/PRISM-FOUR-FACE-dev21.md`.

## Changed

- Prism Image dimensions, Projection Power, clearance and render bounding boxes are now chassis-aware.
- Prism faces share the global Scale/Lift/Rotation/Floating/Lighting/Ghost/Tint/Scanlines presentation pipeline and rotate as one assembly.
- Plane Image Workspace remains Front/Back and retains Mirrored/Readable/Independent behavior.
- Network protocol bumped from **8** to **9**.
- Development version bumped to `0.1.0-dev.21`.

## Validation

- Static source/resource validation performed in the assistant environment.
- Windows NeoForge `build.bat` and in-game QA are still required before this snapshot can be called build-clean.
- Priority QA: 1/2/4 faces, mixed aspect ratios, Same Source, save/reload, multiplayer asset sync, rotation, Flip/Scanlines/Tint/Ghost and obstruction envelopes.

# Mirage Projector 0.1.0-dev.20

## Fixed

- Fixed Entity Scan priority for interactable LivingEntities such as Horses. `Shift + right-click` with the Scan Template is now intercepted at NeoForge's early `EntityInteractSpecific` stage before vanilla entity interaction can mount/open/use the target.
- Kept normal right-click behavior untouched and retained `Item#interactLivingEntity` as a compatibility fallback through the same shared scan implementation.

## Added

- Added **Capture Equipped Loadout** to Humanoid Entity Mode. It snapshots Head/Chest/Legs/Feet/Main Hand/Off Hand directly from the player into the six virtual Incoming channels without moving or consuming any real equipment.
- Empty equipped channels clear their matching virtual Incoming snapshot so each capture represents the complete current loadout.
- Capture is server-authoritative and only accepted while the effective Entity workspace is Humanoid (or bodyless Humanoid with no card).
- Added translated Capture/Return Gear labels, statuses and tooltips for English, Spanish and Chilean Spanish.
- Added `docs/ENTITY-INTERACTION-LOADOUT-dev20.md`.

## Changed

- `EntityWorkspaceActionPayload` adds `CAPTURE_EQUIPPED` at the end of the action enum.
- Network protocol bumped from **7** to **8**.
- Development version bumped to `0.1.0-dev.20`.

## Validation

- Source/JSON/static validation performed in the assistant environment.
- The runtime cannot currently resolve/download the Gradle 9.2.1 distribution, so the real NeoForge 21.1.244 compile remains pending through the included Windows `build.bat`.
- Priority in-game QA: Horse scan must no longer mount instead of scanning; also test Villager/tameable interactions, scan overwrite, composite rejection, and Capture Equipped Loadout with empty/partial/full equipment plus existing Projected conflicts.

# Mirage Projector 0.1.0-dev.19

## Added

- Dedicated **Image Workspace** with large face previews, front/back import/clear, Back Mirrored/Readable/Independent, Vertical Flip and Scanlines. Face labels and native picker titles are translated through the active Minecraft language.
- Dedicated **Item Snapshot Workspace** with virtual capture slot, 3D preview, player inventory and explicit source activation.
- Temporary **Mirage Debug Handbook** with runtime-translated pages (English / Spanish / Chilean Spanish resources included).
- Core-slot tooltip listing accepted Core tiers from weakest to strongest with Power/Scale/Lift/Float information.
- Dedicated network merge payload for image-only settings and new source-workspace navigation payloads.

## Changed

- Rebuilt the primary GUI as a global **Projection Settings** screen. Image import, image thumbnails and the Item virtual slot no longer compete with motion/appearance/Core controls.
- Primary GUI now groups Source Workspaces, Geometry, Rotation, Floating, Appearance and Projection Core as separate visual sections; the final dev.19 spacing pass removes the former Appearance/Core collision and keeps the player inventory below the Core section.
- Active source is read-only in the primary GUI; the corresponding workspace activates its source.
- Projection Core becomes a first-class visible section with the real removable slot, a separate material-block visual, compact capability summary, current PU bar and chassis/clearance state.
- Provisional Core power curve raised:
  - Glass 16 PU / 16px scale / 32px lift / 2px float.
  - Quartz 32 PU / 32px / 64px / 4px.
  - Amethyst 96 PU / 80px / 96px / 12px.
  - Diamond 192 PU / 128px / 128px / 24px.
  - Netherite 384 PU / 160px / 160px / 32px.
- Entity/Humanoid remains one dynamic workspace rather than duplicating state across separate menus.
- Network protocol bumped to **7**.

## Deferred / visible boundary

- Prism four-face layout is represented by North/East/South/West preview cards, but East/West persistent source storage/import remains deferred to the dedicated Prism source-bank pass. It is visibly marked rather than exposed as a fake working control.
- The debug handbook is a temporary development aid and has no frozen survival recipe yet.

## Validation

- Source/static validation only in the assistant environment. Windows NeoForge build/QA is still required before dev.19 can be marked build-clean.

# Mirage Projector changelog

## 0.1.0-dev.18 — NeoForge 1.21.1 equipment API compile repair

- Uses the first real Windows `build.bat` result from dev.17 as authoritative compile feedback. The failure reached `:compileJava` and exposed 13 errors grouped into three API mismatches.
- Restores the missing `net.minecraft.world.entity.player.Player` import in `MirageProjectorBlockEntity`, fixing the three unresolved `Player` signatures.
- Replaces invalid static calls to `LivingEntity.getEquipmentSlotForItem(ItemStack)` with a shared `EquipmentSnapshotRules` resolver. NeoForge 1.21.1 exposes that resolver as an **instance** method; the shared Mirage rule mirrors its stack override -> `Equipable` -> main-hand fallback without needing a live entity.
- Removes the invalid assumption that `EquipmentSlot.SADDLE` exists in Minecraft 1.21.1. Vanilla 1.21.1 has `MAINHAND`, `OFFHAND`, four humanoid armor slots and `BODY`; saddle is a horse-inventory concept, not an `EquipmentSlot`.
- Keeps Mirage's virtual `SADDLE` channel, but detaches it from `EquipmentSlot` and validates it explicitly with `Items.SADDLE`. Horse body armor continues to use `EquipmentSlot.BODY`.
- Replaces `HORSE_SLOTS` with `HORSE_CHANNELS` so Saddle and Body remain two independent Mirage rows without pretending both are vanilla equipment slots.
- Horse scan capture now extracts the saddle snapshot from vanilla `SaddleItem` NBT and Body Armor from the real `BODY` equipment slot before stripping both from the frozen base body.
- Client reconstruction applies Body through `setItemSlot(BODY, ...)` and routes Saddle through the horse's real equipment-inventory slot access (`AbstractHorse.EQUIPMENT_SLOT_OFFSET`). A projection-only `MirageProjectionHorse` makes `isSaddled()` read that slot so vanilla saddle geometry can be visible without a server-synced flag.
- Humanoid left-side staging still permits arbitrary Main/Off Hand items while armor rows enforce their actual equipment slot.
- No settings/wire field order changed; Mirage networking protocol remains `6`. Entity Scan data version remains `3`.
- Static source validation finds no Java parser errors and none of the 13 dev.17 target patterns remain. A real NeoForge Windows rebuild is still required before calling dev.18 build-clean.

## 0.1.0-dev.17 — projection-local 3D Ghost/Tint + true 3D Item preview

- Connects existing Tint and Ghost Effect/Transparency settings to the **volumetric Item / Entity / Humanoid render families** instead of leaving them Image-only.
- Adds `ProjectionRenderBuffers`, a projection-local `MultiBufferSource`/`VertexConsumer` wrapper. It multiplies RGB/alpha per emitted vertex and never leaves a global shader-colour mutation active across shared world batches.
- When Ghost Effect is active, common opaque/cutout textured entity and armor RenderTypes are rerouted through compatible translucent entity RenderTypes. Already-translucent and special layers keep their own RenderType so glint/emissive/modded behaviour is not blindly replaced.
- Adds fail-closed handling for unknown/custom RenderTypes: if Mirage cannot safely identify a normal textured opaque layer, it keeps the original RenderType and applies only the vertex-colour fallback.
- World Entity/Humanoid rendering now feeds body, Projected/Active armor and both hands through the local presentation buffer.
- World generic Item Mode now uses the same buffer, so blocks/tools/items receive Tint/Ghost Effect while retaining their complete 3D ItemRenderer model.
- The selective projector-base nameplate now follows the projection Tint/Ghost value as part of the same visual composition.
- Replaces the direct vanilla inventory helper in Entity preview with a local copy of the vanilla inventory transform so Mirage can safely inject its presentation buffer while retaining cursor-facing rotation, scissor and auto-fit.
- Upgrades ordinary Item Mode GUI preview from a flat inventory icon to a `FIXED` 3D model. Blocks therefore preview as blocks; standalone armor continues using equipped geometry on the invisible rig.
- Adds `docs/GHOST-3D-RENDERER-dev17.md` with the render-isolation contract, RenderType fallback policy and required glint/modded QA.
- Networking protocol remains `6`; no serialized settings field order changed in this wave.
- Keeps Minecraft 1.21.1 / NeoForge 21.1.244 / Java 21 and the single root `build.bat`.
- Real NeoForge compile and in-game QA remain pending; dev.17 must not be called build-clean yet.

## 0.1.0-dev.16 — world Entity/Humanoid renderer + bodyless equipment rig

- Adds `ENTITY` as a real projector source path and reuses frozen `EntityProjectionState` for in-world volumetric rendering.
- Renders scanned LivingEntity bodies client-side without spawning them into the world or ticking AI.
- Applies only Projected/Active Humanoid/Horse equipment to the rendered body; Incoming remains staging/conflict data.
- Adds the selective projector-base nameplate path: Players always use their player name; non-player entities only use captured CustomName.
- Implements the body-optional **invisible virtual Humanoid mannequin**. Removing a Humanoid card can now leave armor and both hand snapshots visibly projected without an Armor Stand/player body.
- Captures Player packed `textures` GameProfile property in Entity Scan data version 3 and resolves the render skin from that frozen property so Player projections can remain self-contained after the source leaves the session.
- Makes standalone Head/Chest/Legs/Feet Item Mode snapshots render through equipped geometry on the bodyless rig instead of relying on the inventory/hand sprite/model.
- Adds the same equipped-geometry behavior to the Item Mode GUI preview for wearable armor snapshots.
- Adds a visual-only animation clock that never calls fake-entity AI/world ticks. Ender Dragon receives a first side-effect-free latency/flap-history adapter so its renderer can animate without dragon AI.
- Keeps whole-model Scale/Lift/Rotation/Floating semantics for Entity/Humanoid sources.
- Keeps Ghost Effect/Tint for full 3D entity/equipment/glint layers explicitly pending until an alpha-safe buffer path is validated.
- Reasserts the permanent recovery invariant: WIP/source snapshots are delivered independently of compile/QA status.
- Keeps Minecraft 1.21.1 / NeoForge 21.1.244 / Java 21 and the single root `build.bat`.
- Real NeoForge compile and in-game QA remain pending.

## 0.1.0-dev.15 — live Entity GUI preview + context teardown cleanup

- Implements the first real **client-only 3D Entity preview** in the dedicated Mirage Entity Workspace.
- Reconstructs non-player LivingEntity previews from the frozen scan with `EntityType#create(level)` + sanitized entity data, without adding the preview entity to the world.
- Reconstructs Player previews as client-only `RemotePlayer` objects keyed to the scanned source UUID/name; exact frozen/offline skin capture remains a separate follow-up because vanilla 1.21.1 resolves `RemotePlayer` skins through current `PlayerInfo`.
- Applies only **Projected / Active** Humanoid or Horse equipment snapshots to the preview. Incoming/staging equipment does not masquerade as accepted equipment.
- Uses the vanilla inventory entity-render path, including scissor clipping and cursor-facing rotations where the entity renderer supports them.
- Adds bounding-box-derived auto-fit so Chicken/Baby Zombie/Player/Horse/large mobs share one bounded viewport instead of hardcoded per-mob scales.
- Makes the external preview panel responsive: preferred width is used when available and a narrower bounded preview is kept on smaller GUI widths instead of forcing the main workspace off-screen.
- Displays the frozen projection-nameplate policy in the preview footer: Player name always, non-player custom name only. No generic `Horse`/`Zombie` world label is invented.
- Corrects scan-card teardown semantics: removing any staged Entity card clears that card-supplied entity body; Humanoid Incoming/Projected six-channel equipment survives as mannequin state, while Horse-only Saddle/Body state is cleared with Horse context.
- Preserves the dev.14 Empty Scan Template recipe, Shift + right-click scan behavior, six Humanoid incoming/projected channels, per-slot conflict resolution and right-click projected clear.
- Reasserts the project safety invariant: a recoverable source snapshot is produced before/through every development wave even when compile/QA remains pending.
- Keeps Minecraft 1.21.1 / NeoForge 21.1.244 / Java 21 and the single root `build.bat` policy.
- Real NeoForge compile/Windows QA remains pending for this source snapshot.

## 0.1.0-dev.14 — Empty Scan Template + Entity workspace foundation

- Renames the empty scanner medium in player-facing UI to **Empty Scan Template** while keeping the internal registry id stable for world compatibility.
- Adds the shaped recipe: eight Iron Nuggets surrounding one Paper; output is one non-stackable Empty Scan Template.
- Scanning is now explicitly **Shift + right-click** on one LivingEntity/Player. Normal right-click remains available to the target entity.
- A scan stores an independent Mirage scan UUID, source UUID provenance, entity type, bounded visual entity data and frozen equipment snapshots without removing anything from the source.
- Scanned templates may be deliberately replaced by repeating Shift + right-click on another valid entity. Mounted/passenger composites remain rejected.
- Adds projector-owned Entity projection state, separated from the physical scan template. Removing a Humanoid template therefore cannot steal or erase active projected equipment.
- Adds a dedicated **Mirage Entity Workspace** reachable from the main projector GUI.
- Humanoid workspace now has six physical Incoming/Staging channels on the left (Head, Chest, Legs, Feet, Main Hand, Off Hand), six virtual Projected/Active channels on the right, and the scan-template slot in the center.
- Scanned Humanoid equipment enters the left virtual incoming state and never silently replaces the active right side. Each row has an explicit apply action; occupied destinations require slot-local replacement confirmation.
- Right-clicking a projected equipment cell clears only that virtual hologram snapshot.
- Adds `Return inserted gear`, which returns physical staging sources to the player when inventory space exists; virtual scan-derived snapshots are never treated as loot.
- Adds Horse-specific Saddle and Body Armor staging/projected channels, with a removal guard so a Horse scan cannot hide real staged gear. Leaving Horse context clears Horse-only virtual overrides.
- Generic entities do not fabricate editable armor channels; model-native/mod-private visible equipment remains part of the frozen entity body until an explicit compatibility adapter exists.
- Adds the final nameplate policy to scan data: Players always carry their player name; non-player entities carry text only when custom-renamed. Unnamed mobs/horses do not receive a generic type label.
- Reserves the external right-side 3D preview panel and exposes active entity/nameplate metadata there. Actual reconstructed entity rendering and auto-fit preview remain the next renderer task rather than being faked in this build.
- Bumps Mirage networking protocol to `5` for Entity workspace open/back/actions.
- Keeps Minecraft 1.21.1 / NeoForge 21.1.244 / Java 21 and the single root `build.bat` policy.
- Real NeoForge compile/Windows QA remains pending for this source snapshot.

## 0.1.0-dev.13 — Entity Scan foundation / Humanoid Entity contract

- Renames the future Armor/Effigy workflow to **Humanoid Entity Mode**.
- Adds the first non-stackable **Entity Scan Card** item.
- Entity Scan Card captures a frozen entity snapshot instead of relying on a live UUID lookup.
- Stores Mirage scan UUID + source UUID metadata + entity type + sanitized visual entity data.
- Splits known vanilla humanoid equipment into Head/Chest/Legs/Feet/Main/Off Hand scan channels.
- Splits Horse Saddle and Body Armor into dedicated scan channels.
- Rejects currently mounted/passenger/composite entity scans in the first implementation pass.
- Requires sneak-use to overwrite an already-populated scan card.
- Adds detailed `docs/ENTITY-PROJECTION-CONTRACT.md`.
- Freezes the Humanoid Entity GUI as six incoming/staging rows on the left and six projected/active rows on the right, with both hands on both sides.
- Every incoming row has an individual ✓ Apply action; occupied projected slots require explicit per-slot conflict resolution.
- Humanoid equipment rows and projected snapshots persist when the scan-card slot is emptied.
- Defines dynamic Horse Saddle/Body Armor rows and Generic Entity behavior.
- Defines player inventory placement below the editor and a clipped, auto-fitted 3D preview panel to the right.
- Explicitly keeps backpack/accessory/artifact integrations outside baseline scope.
- Entity projector import, dedicated Entity GUI and world Entity renderer remain pending after this foundation pass.


## 0.1.0-dev.12 — virtual item snapshots and armor/effigy ownership contract

- Replaced Item Mode's physical projected-item storage with a **Virtual Snapshot Slot**.
- Capturing an item now copies one ItemStack for render data without shrinking, moving or locking the source stack.
- Each capture receives a new Mirage-owned snapshot UUID; arbitrary Minecraft ItemStacks are not assumed to have their own universal UUID.
- The virtual snapshot cannot be picked up, cloned, dragged, hopper-extracted, crafted with or dropped as a real item.
- Clicking the virtual slot with an empty cursor clears only the snapshot. Shift-clicking a normal non-Core item captures it without consuming it. Number-key/offhand capture is also supported.
- Core Slot remains a real physical inventory slot.
- World renderer continues to consume the captured ItemStack copy, so existing 3D Item Mode behavior remains the visual baseline while ownership semantics change underneath it.
- Breaking a new dev.12 projector no longer drops a projected item snapshot.
- Added migration safety for dev.11 and older worlds: a formerly physical projected item becomes the render snapshot while the original real stack is retained as a one-time legacy return item and drops when that projector is broken.
- GUI now labels Item Mode as `Item Snapshot`, labels the virtual slot as `Snapshot`, displays the short snapshot UUID and explicitly states that the real item stays with the player.
- Frozen the future Armor/Effigy data model as six independent virtual snapshot channels: Head, Chest, Legs, Feet, Main Hand and Off Hand.
- Documented that pose is separate from equipment snapshots and that all six real items can be re-equipped immediately after capture.
- Added the future `Capture Equipped Loadout` contract: Armor/Effigy will snapshot the player's currently equipped four armor pieces plus both hands in one non-consuming operation.
- Added `docs/ITEM-ARMOR-SNAPSHOT-CONTRACT.md` as the canonical ownership/security/render contract for 3D sources.
- Kept standalone equipped-armor rendering, invisible humanoid rig, pose presets and alpha-safe 3D Ghost Effect in the waiting list because they still depend on the shared equipment renderer.
- Kept Minecraft 1.21.1 / NeoForge 21.1.244 / Java 21 fixed.
- Bumped development version to `0.1.0-dev.12`.

### Validation state

- dev.12 has static/source validation in this environment only.
- Full NeoForge compile and Windows in-game QA remain pending via `build.bat`.
- Priority QA is non-consuming capture, no snapshot drop/duplication, UUID persistence, legacy dev.11 return behavior and regression checks for Image Mode.

# Mirage Projector 0.1.0-dev.11

## Added

- Added simultaneous **Front / Back image thumbnails** to the projector GUI.
- Back thumbnail reflects the effective `Mirrored`, `Readable` or `Independent` face behavior.
- Added thumbnail hover details with face role, source resolution and short asset hash.
- Documented a permanent rendering contract separating Image/Banner face geometry, generic 3D Item rendering and Armor/Effigy rendering.
- Documented the full Armor/Effigy target: 4 armor slots, two hand slots, invisible humanoid rig, pose presets, global rotation/floating/scale/transparency and modded-equipment fallback rules.
- Added a dedicated 3D-render waitlist for equipped-piece armor rendering and alpha-safe Ghost Effect.

## Changed

- Image preview UX no longer depends on filenames or hashes to tell Front from Back; the actual source image is visible in its own card.
- Future Prism/multi-source UI is now required to provide one thumbnail per face/source.
- Clarified that a 3D Item source rotates as one complete model even inside a Prism-capable chassis; Prism face switching applies to 2D Image/Banner geometry, not to a sword/block/armor object.
- Clarified that standalone armor in Item Mode must ultimately use equipped-piece geometry instead of its inventory/hand representation.

## Deferred intentionally

- Armor Mode is not exposed yet: the generic ItemRenderer path is not enough for faithful equipped armor.
- Ghost Effect remains Image-only until a safe per-buffer/per-vertex alpha path exists for ItemRenderer, glint, armor layers and hand items.

## Baseline

- Minecraft 1.21.1
- NeoForge 21.1.244
- Java 21

# Changelog

## 0.1.0-dev.10 — Ghost Effect and first physical chassis family

- Kept the fixed project baseline at Minecraft 1.21.1 / NeoForge **21.1.244** / Java 21 and bumped the development version to `0.1.0-dev.10`.
- Reworked the user-facing alpha control into the requested **Ghost Effect / Transparency** semantics.
  - GUI range is now `Ghost effect: 0-90%`.
  - `0%` means fully visible; increasing the slider makes the Mirage more transparent/ghost-like.
  - `90%` is the safety maximum so a projection cannot become fully invisible by accident.
  - Existing dev.9 worlds remain compatible: NBT/network continue storing the legacy `OpacityPercent` value and the GUI maps `Transparency = 100 - Opacity`.
  - Plane/Image rendering continues using the same alpha channel internally, so Front/Back, Tint, Scanlines and existing transparent PNG pixels remain compatible.
  - Item Mode intentionally remains opaque until a safe ItemRenderer alpha path is implemented.
- Upgraded the GUI source preview so current **Tint + Ghost Effect** are applied to the DynamicTexture preview, not only to the world renderer.
  - Shader color is flushed/restored immediately around the preview draw to avoid leaking tint/alpha into other GUI elements.
  - Scanline simulation remains layered over the preview.
- Registered the first four additional physical Plane chassis using the shared architecture instead of per-tier subclasses:
  - `Mirage Display` — 16×16 px envelope, 48 px Lift, 6 px Float.
  - `Wide Mirage Projector` — 80×32 px / approximately 5×2 blocks, 64 px Lift, 8 px Float.
  - `Tall Mirage Projector` — 32×80 px / approximately 2×5 blocks, 96 px Lift, 12 px Float.
  - `Mirage Field Projector` — 80×80 px / approximately 5×5 blocks, 96 px Lift, 16 px Float.
  - Compact remains 10×10 px, 32 px Lift and 4 px Float.
- All five current chassis share one `MirageProjectorBlockEntity`, one menu, one renderer, one asset/network path and one Core/Power system.
  - The chassis is inferred from the placed block state.
  - The single BlockEntityType now supports all five registered projector blocks.
  - Creative tab and Functional Blocks expose all five development chassis.
- Reworked chassis validation from one scalar `maxScalePixels` into a real **width × height envelope**.
  - Projection dimensions are calculated after preserving the source aspect ratio.
  - Wide can reject a portrait that exceeds its 32 px vertical envelope even when the nominal Scale is <= 80 px.
  - Tall can reject a landscape that exceeds its 32 px horizontal envelope.
  - `Debug chassis` still bypasses only the chassis envelope; Core limits and Power remain enforced.
- Added chassis-specific physical emitter geometry metadata.
  - Each chassis owns `physicalTopPixels` plus dynamic Core dimensions/vertical placement.
  - Image, Item, book placeholder, missing-asset placeholder, render bounds and clearance now begin from the actual chassis emitter height instead of the Compact hardcoded 5 px top.
- Added distinct **provisional development models** and collision shapes for Display/Wide/Tall/Field.
  - All preserve the common obsidian + glass + visible dynamic Core language.
  - They are explicitly not final art and are intentionally not simple copies of the Compact model.
  - Collision is chassis-aware: the dynamic Core cuboid only contributes to the VoxelShape while a Core is actually installed, avoiding an invisible solid core when the socket is empty.
  - Recipes remain unfrozen until physical models and the Power curve are approved.
- New chassis start with an empty Core Socket so placing/breaking them cannot generate free core materials.
  - Compact keeps the Glass Core default/migration behavior required by the dev.7→dev.8 save transition.
- Updated clearance to accept the active chassis profile and use its physical top while scanning/rendering the editing envelope.
- Added chassis-aware GUI status showing the active chassis envelope and the current aspect-preserved projected dimensions.
- Updated language resources for all new block/container names and corrected the file-selection status to include WebP.
- Updated README/DEVELOPMENT to define Ghost Effect semantics, physical chassis contracts, provisional-model status and next steps.

### Validation state

- Source/JSON/static validation is performed in this environment before packaging, but the real NeoForge compile still requires the included Windows `build.bat` because this runtime does not have the dependency distribution cached.
- dev.9 has not been explicitly reported build-clean in this chat yet, so dev.10 must not be treated as release-ready until Windows build + in-game QA.
- Highest-risk live checks: new BlockEntityType multi-block registration, new models/collision shapes, dynamic Core alignment for each body, Wide/Tall aspect-envelope rejection, GUI shader-color restoration, and inventory/Core drop behavior on every chassis.

## 0.1.0-dev.9 — Image completeness, world preview and presentation pass

- Took the user-pushed dev.8 repository state as the baseline for this wave and kept Minecraft 1.21.1 / NeoForge **21.1.244** / Java 21 fixed.
- Added a permanent project metadata policy: **do not hardcode GitHub/repository URLs in mod descriptions**. Repository links belong in separate platform link fields when needed, not in `neoforge.mods.toml`/CurseForge/Modrinth descriptive copy.
- Added real **WebP import** without requiring a separate user-installed mod/library.
  - Bundles `org.sejda.imageio:webp-imageio:0.1.6` through ModDevGradle Jar-in-Jar.
  - Native file picker now accepts `.webp` alongside PNG/JPG/JPEG.
  - `ImageIO.scanForPlugins()` discovers the bundled provider before decode.
  - WebP still normalizes immediately to PNG; only normalized PNG bytes participate in SHA-256/cache/network/world storage.
- Added `THIRD_PARTY_NOTICES.md` documenting the embedded WebP decoder and its upstream license.
- Added first-pass **in-GUI source preview**.
  - Image preview uses the current DynamicTexture and preserves aspect ratio.
  - Item Mode previews the stored ItemStack.
  - Image preview preserves aspect ratio, simulates scanlines and shows the current Tint swatch; Tint/Opacity themselves are validated in-world.
- Added Image Mode **presentation settings**, persisted through NBT and synchronized over projector settings packets/menu data.
  - `Fullbright / World Light`.
  - Opacity from 10% to 100%.
  - Persistent RGB tint with White/Cyan/Amethyst/Rose/Amber/Green/Red GUI presets.
  - Optional scanlines.
- Implemented scanlines without requiring a shader.
  - Plane quads are split into visible horizontal strips separated by transparent gaps.
  - Scanline geometry is clamped to 8-64 segments per face to bound vertex growth on giant debug projections.
  - Scanlines cost +1 Projection Power and are OFF by default.
- Upgraded **projection clearance** from text-only warning to live world-space preview while the projector GUI is open.
  - Clearance result now carries the projection AABB and up to 128 blocking positions.
  - A stationary Plane uses its actual thin AABB rotated to the current orientation; rotating Planes and Item projections keep the conservative swept envelope.
  - Whole envelope is outlined in-world and intersecting blocks receive red outlines.
  - Preview updates while Scale/Lift/Float/source settings change and clears on screen close/logout.
- Added real **asset transfer feedback**.
  - New server-to-client `AssetUploadAckPayload` reports final accepted/rejected state plus a server message.
  - Client reports chunk-send percentage, waits for the server ACK before declaring success, and derives download progress from received chunks.
  - Local cache/hash/I/O upload failures are surfaced in the same status channel instead of only the log.
  - GUI adds a `↻` control to retry synchronizing the current Front/Back assets.
  - Server can explicitly acknowledge assets it already has, so dedup is visible as complete instead of silent.
- Added `ProjectionChassisProfile` as the canonical hard-envelope layer between physical projector bodies and Projection Core power.
  - Compact is the only registered chassis in dev.9.
  - Provisional contracts now exist for Display, Wide, Tall, Field, Prism, Effigy and Colossal.
  - `ProjectionPower` now accepts a chassis profile rather than hardcoding Compact limits.
- Centralized settings wire serialization in `ProjectionSettings` and extended it for all new presentation fields.
- Bumped payload protocol from `3` to `4` for the dev.9 settings/network layout.
- Grew the GUI to 416x518 and shifted player inventory down to keep presentation controls, preview/status and slots from overlapping.
- Fixed the duplicated `floatSlider.setTooltip(...)` source line that had slipped into the dev.8 screen source.
- Bumped development version to `0.1.0-dev.9`.

### Validation state

- Java source passed a syntax/parser-oriented `javac` pass in this environment; expected Minecraft/NeoForge symbol errors remain because the dependency classpath is unavailable here.
- JSON resources validate structurally.
- A full NeoForge build could not be executed in this environment because outbound DNS cannot download Gradle/dependencies. Run the included `build.bat` on Windows before treating dev.9 as build-clean.
- Highest-risk live checks: WebP Jar-in-Jar resolution, GUI size at low GUI Scale, GUI DynamicTexture preview, world-line rendering stage, upload ACK registration and scanline winding/transparency on both front/back faces.


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
