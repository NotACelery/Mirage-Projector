# dev.75d documentation recovery — versioned roadmap consolidation

- No runtime/version/protocol change.
- Split the monolithic waitlist into General, 1.0.0, 1.1.0 and 1.2.0 authorities.
- Froze 1.1.0 as Portable Illumination, Capture & Projection Expansion: dynamic light, rechargeable Glow Dust, lanterns, Scan Codex, copy lectern and portable/presentation projectors.
- Froze 1.2.0 direction around direct hologram grab/free rotation.
- Added optional Create Blueprint/schematic bridge roadmap and explicit chassis compatibility.
- Added a 1.0.0 future-proofing contract so projection source types, transforms, light backends, energy and future interactions do not remain closed/hard-coded.

# 0.1.0-dev.75d — Obstacle-detour decay and static-light consolidation

- Added `detourExtraCostUnits` to `MirageLightProfile`, separating open-space decay from obstacle-forced route decay.
- Current half-decay EXTEND profile uses substeps 2, air cost 1 and detour extra 1: direct/open travel remains 1/2 visible level per block, while each extra path block caused by obstacle routing costs one full visible level overall.
- `MirageLightSolver` detects detour increment without storing complete paths: an edge moving back toward the source reduces Manhattan distance while path length still grows, representing two accumulated detour steps and receiving `2 * detourExtraCostUnits` additional fixed-point cost.
- Open no-Booster `15,15,14,14,...,1,1` sequence remains unchanged.
- No abrupt shadow-region mode switch is introduced; finite-wall/L-corner light forms a gradual gradient from the cheapest actual causal route.
- Synchronized the new profile field in `MirageLightSourceSyncPayload`; network protocol intentionally moves **20 -> 21**.
- `/miragelight probe` now reports weighted/direct/extra nearest-source cost for obstacle QA.
- Added `verify_mirage_light_detour_penalty.py` regression fixture: open probe level 13 vs routed finite-wall level 11 while unaffected monotonic positions remain unchanged.
- Added full static-light authority/consolidation docs (`MIRAGE-LIGHT-ENGINE.md`, dev.75c/dev.75d notes, refreshed CURRENT/ARCHITECTURE/ROADMAP/QA/DEVELOPMENT/registry/handbook) and moves the next implementation target to dev.76 dynamic/mobile light foundation.
- No projector NBT change. Physical Crying Light Nodes remain migration-only.
- Source candidate pending Windows Java 21 build and in-game 3-D wall/L-corner/partial-block/chunk/multiplayer/performance QA.

# 0.1.0-dev.75c — Vanilla opacity handoff fix

- Fixed authoritative Mirage-light wall occlusion: `MirageLightOcclusion` now passes the destination block's real `getLightBlock(...)` opacity into vanilla `LightEngine.getLightBlockInto(...)` instead of the erroneous hard-coded value `1`.
- Fully opaque destinations (`>=15`) are rejected before propagation; face-shape occlusion remains delegated to vanilla for slabs/stairs/modded states.
- The causal flood solver is unchanged: walls must now be routed around voxel-by-voxel, so every extra surface/path step consumes Mirage fixed-point energy rather than allowing light to cross the wall.
- Network protocol remains 20; no NBT or payload schema change.

# 0.1.0-dev.75b — Authoritative Mirage-light lifecycle

- Completed the dev.75 authority handoff after dev.74 in-game solver QA confirmed the causal fixed-point field.
- Scoped Mirage source descriptors to watched chunks instead of dimension-wide fanout; UnWatch/source removal retract stale client fields.
- Preserved watched-chunk state across same-level respawn so CLEAR/resync does not depend on vanilla retransmitting already watched chunks.
- Added coalesced server chunk-load discovery, orphan legacy-node cleanup and re-solves for sources whose radius intersects newly loaded geometry; source-origin unload removes the authoritative source.
- Added coalesced client chunk load/unload geometry rebuilds plus old/new render-section dirtying.
- Extended terrain invalidation to fluid placement, crop/feature growth, piston paths and explosions in addition to player place/break, while keeping one rebuild per impacted source per tick.
- Optimized source replacement so aggregate sections rebuild once across the union of old/new touched sections instead of remove+add double work.
- Physical `mirage_projector:crying_light_node` creation remains disabled; loaded chunks palette-scan and remove orphan legacy nodes only.
- Network protocol remains 20 from dev.75a source-descriptor sync. No new projector NBT contract.
- Source candidate pending Windows Java 21 build and in-game wall/chunk/relog/multiplayer/performance QA.

# 0.1.0-dev.74 — Mirage Light Engine foundation

- Added a feature-independent `light.engine` subsystem after live dev.70–73 QA proved physical auxiliary emitters cannot preserve source causality once vanilla begins propagating from each relay.
- Added immutable `MirageLightSource` / `MirageLightProfile` contracts with conceptual power, fixed-point substeps, shape/direction data, runtime intent and reserved RGB metadata.
- Added a six-neighbour causal voxel solver. Every accepted cell has a traversable predecessor chain to the source; complete barriers can disconnect a field while finite-wall corner routes consume extra distance.
- Reused vanilla `LightEngine.getLightBlockInto(...)` edge semantics for full/partial block-light obstruction instead of maintaining block-specific slab/stair rules.
- Added exact fixed-point Mature half-decay math: no-Booster outward distances 1–30 solve to `15,15,14,14,...,2,2,1,1`; conceptual Booster power above 15 extends the saturated plateau without producing visible values above 15.
- Added primitive packed-position + energy-bucket solver queues and sparse 16³ per-source section storage.
- Added per-Level source ownership and an aggregate max virtual-light layer; aggregate sections reference solved source sections instead of duplicating one visible 4096-byte array per source/section.
- Wired energized Mature Clusters into the virtual solver in **shadow mode** while intentionally retaining the dev.73 physical `crying_light_node` backend as visible/gameplay authority for this foundation snapshot.
- Terrain invalidation forces affected shadow fields to rebuild in the existing dev.70 end-of-tick coalesced pass; Core Booster material changes also trigger immediate nearby source refresh.
- Added `/miragelight stats`, `/miragelight probe`, `/miragelight axis <direction>` and `/miragelight rebuild` diagnostics plus pure-model curve/causality verification scripts.
- Added Level-unload cleanup for virtual-light caches.
- Network protocol remains 19; no projector NBT/payload schema changes.
- dev.75 is the planned authority handoff: client/static virtual-light integration, automatic chunk-load invalidation and safe retirement/migration of active physical Mature relays.
- Source candidate pending Windows Java 21 build and in-game shadow-solver/performance QA.

# 0.1.0-dev.73 — Explicit full-range Mature half-decay relays

- Corrected the Mature Cluster slow-decay lattice after live Simple Light Level QA showed that the sparse dev.67–72 relay pattern only visibly doubled a middle section of the light curve.
- Replaced odd-distance-only axial sampling with an explicit Mirage relay on every accepted axial air cell.
- No-Booster target is now directly represented as `15,15,14,14,...,2,2,1,1` across 30 unobstructed blocks.
- Reflected conceptual power above 15 now extends the saturated level-15 plateau and total tail; real relay emission remains clamped to vanilla 15.
- Preserved dev.70 source-causal opaque-wall cutoff, partial-block attenuation, same-tick terrain invalidation, overlap reconciliation and removal of long Glass diagonal static relays.
- Network protocol remains 19; no NBT or payload schema changes.
- Requires Windows Java 21 build and measured in-game level QA before build-clean acceptance.

# 0.1.0-dev.72 — Entity envelope, nameplate and frustum hardening

- Restored projected Entity nameplates above the actual projection using `Lift + Float + EntityProjectionBounds` instead of the regressed chassis-base anchor.
- Nameplates now inherit projection Tint and Ghost/opacity while retaining the two-pass vanilla-like readable text path.
- Extended generic Entity bounds conservatively for visible Humanoid Main/Off Hand, Chest, Head and Horse Body Armor snapshots so preview, clearance and culling better cover renderer geometry outside the vanilla body AABB.
- Expanded the BER AABB to cover both the physical projector and the displaced projection/nameplate envelope, including high Lift and downward Floating travel.
- Stopped forcing `shouldRenderOffScreen=true`; vanilla frustum culling can now reject giant projectors whose complete Mirage envelope is outside the camera without adding a quality-reducing LOD path.
- Network protocol remains 19; no saved/network payload changes.
- Source candidate pending Windows Java 21 build plus nameplate/high-Lift/screen-edge/Create regression QA. dev.70 light-field QA remains open in parallel.

# 0.1.0-dev.71 — Per-channel Entity equipment visibility

- Added persistent render-only visibility state for Humanoid Head, Chest, Legs, Feet, Main Hand and Off Hand plus Horse Saddle and Body Armor.
- Added a VIS. control beside every supported Projected equipment row. Hidden snapshots remain visible in the workspace as stored virtual content while their world/preview renderer contribution is suppressed.
- Re-enabling a hidden channel restores the exact already stored snapshot; toggling visibility never returns, deletes, replaces or recaptures equipment.
- Added visibility state to Entity projection NBT with backward-compatible default-visible loading. Clearing an individual projected snapshot resets only that channel to visible; incompatible family cleanup resets that family's visibility flags.
- Made bodyless Humanoid creation visibility-aware so hiding the final visible channel produces no invisible empty mannequin render while keeping source state stored.
- Included visibility in the equipment cache fingerprint so world and preview projection entities rebuild as soon as the synced toggle changes.
- Appended `TOGGLE_VISIBILITY` to `EntityWorkspaceActionPayload.Action` and bumped Mirage network protocol from 18 to 19 to prevent mixed-version ordinal ambiguity.
- Updated the in-game handbook Entity page and active documentation. dev.70 light-field QA remains open in parallel.
- Source candidate pending Windows Java 21 build and in-game visibility/save/reload/upgrade QA.

# 0.1.0-dev.70 — Instant occlusion rebuild and strict axial relays

- Fixed dev.69 terrain-response latency by coalescing block placement/break changes and rebuilding impacted active Mature fields from `LevelTickEvent.Post` in the same server tick.
- Reconciles stale downstream Mirage Light Nodes immediately during source refresh instead of scheduling a later node tick.
- Makes opaque states hard-stop the remainder of an axial relay branch, including blockers sitting exactly on a sampled node position.
- Removed dev.69's long Glass-driven face-diagonal static-light branches after in-game QA showed that their omnidirectional vanilla emission could leak onto the dark side of walls.
- Keeps Glass/Diffusion in reflected residual-ray rendering; Quartz/Radiance and Diamond/Focus retain static axial range roles, while Amethyst/Netherite remain dynamic reflection effects.
- Added one-time reconciliation of legacy dev.69 diagonal Mirage nodes when an affected Mature source first refreshes/removes.
- Preserved the no-Booster ~29-block axial half-decay baseline, overlap-aware strongest-source reconciliation and protocol 18.
- Source candidate pending Windows Java 21 build plus dev.70 in-game wall/unblock/overlap QA.

# 0.1.0-dev.69 — Occlusion-aware reflected light and Booster identity

- Reworked Mature Cluster node path validation so fully opaque blockers terminate downstream reflected candidates while partially light-blocking states add attenuation instead of behaving like perfect air.
- Kept local light spreading, corner wrap, ambient occlusion and block-face shading in Minecraft's vanilla light/render pipeline; Mirage does not introduce a per-face lighting engine.
- Removed generic `widthScale -> reflected field tier` coupling. Quartz/Radiance is now the dominant static range amplifier; Diamond/Focus adds a smaller axial bonus; Glass/Diffusion broadens coverage; Amethyst/Resonance and Netherite/Inversion remain dynamic reflected-ray effects.
- Added bounded Glass-driven face-diagonal reflected branches with shorter/dimmer coverage than the six axial baseline branches.
- Added dedicated reflected relay helpers for residual-ray brightness, radius, length and excitation.
- Optimized Mature source refresh so the Beacon relay is resolved once per source refresh instead of once per candidate node.
- Preserved dev.68 immediate overlap-aware node teardown and the zero-Booster half-decay baseline.
- Network protocol remains 18.

# 0.1.0-dev.68 — Safe Core Booster extraction and instant light teardown

- Restricted loaded Core Booster extraction to Shift + right-click while the interacting hand is empty or holds the same stored core-material item.
- Left incompatible held-item interactions uncancelled so tools and other item interactions do not accidentally eject the stored core.
- Empty-hand extraction now returns the core directly to the interacting hand; compatible same-item stacks merge when possible and otherwise fall back to inventory/drop behavior.
- Confirmed the Mature Cluster half-decay light field is baseline Beacon-energized behavior and does not depend on a Core Booster. Relay tiers only reinforce/extend it.
- Added immediate source teardown for Mature Cluster light nodes. Breaking the cluster now removes or downgrades its contributed nodes in the same server tick while preserving overlapping contributions from other Mature sources.
- Applied the same immediate teardown path when an energized Mature Cluster transitions to de-energized.
- Network protocol remains 18.

# 0.1.0-dev.67 — Beacon optics stabilization and half-decay field

- Live QA confirms the Create Netherite Backtank synthetic chest can now fade completely under Ghost.
- Moved Core Booster relay activation from the block bottom to the physical internal-core plane at `8.5/16`.
- Added stage-specific post-bud vertical continuation heights: Small `4.5/16`, Medium `6.5/16`, Large `8.5/16`; Mature remains a full vertical stop.
- Corrected custom Beacon side-quad ordering to match vanilla topology and remove the accidental diagonal face responsible for N/X-shaped relay/continuation beams.
- Fixed residual-ray collision clipping so the source crystal's own collider no longer suppresses the burst while the visible origin remains at pixel 2.
- Clarified powered-light scope: Small/Medium/Large buds remain zero-block-light stages; only an energized Mature Cluster drives the Mirage-owned half-decay auxiliary field.
- Reworked the energized Mature Cluster light field to target repeated falloff levels (`5,5,4,4,3,3...`) along its supported axes while keeping every node at or below level 15.
- Kept dev.66 advanced light modes as placeholders only.
- Did not change `build.bat` or the cleaner because no project file was deprecated, removed or moved.
- Network protocol remains 18.
- Source candidate pending Windows build and in-game Beacon/light QA.

# 0.1.0-dev.66 — Advanced light-profile foundation

- Added reusable `LightProfile`, `LightDecayMode` and pure `LightProfileMath` contracts without changing the dev.65 Crying Obsidian node runtime.
- Mapped the current Beacon-relay lighting result to `VANILLA` / `EXTEND` profiles through `CryingObsidianLightField.profileForRelay(...)`.
- Reserved `CONCENTRATE`, `DIRECTIONAL_SPOT` and `ROTATING_DIRECTIONAL_SPOT` as explicit non-runtime placeholders for later lighting features.
- Encoded that concentrated/directional world-light modes require suppression of ordinary omnidirectional source emission before they can be activated correctly.
- Added floating-point concentrated-falloff and dot-product cone math as pure helpers only; neither helper places or removes blocks.
- Kept Mirage-owned auxiliary nodes, source revalidation, strongest-overlap resolution, loaded-chunk guards and obstacle checks as the required runtime safety model.
- Did not change `build.bat` or the cleaner because no existing file was deprecated, removed or moved.
- Network protocol remains 18.
- Source candidate pending Windows build; dev.65 lighting QA and dev.64 Create Ghost QA remain open.

# 0.1.0-dev.65 — Powered Crying Obsidian extended light field

- Added an internal auxiliary-light field for energized Mature Crying Obsidian Clusters without raising vanilla light levels above 15.
- Added three invisible node rings at Manhattan distances 8, 16 and 24 with four relay-driven strength tiers.
- Beam width contributes a smaller tier increase; effective Quartz/Radiance Boosters contribute the dominant tier increase.
- Strongest supported relay targets roughly 30 blocks of useful light reach.
- Internal nodes have no item/Creative exposure, no collision or drops, are replaceable, only occupy air and self-validate every 40 ticks.
- Solid light-blocking paths prevent auxiliary nodes from appearing beyond the obstruction.
- All energized Crying Obsidian stages now use emissive rendering while buds remain zero-block-light stages.
- Relay amplification now increases residual-ray event presence in addition to existing width/brightness/rotation effects.
- Preserved dev.64 Create synthetic-chest Ghost candidate unchanged for parallel QA.
- Network protocol remains 18.

# 0.1.0-dev.64 — Create synthetic chest single-surface Ghost

- Live dev.63 QA confirmed the Create Netherite Backtank tank itself followed Ghost opacity, while the synthetic diving chest remained too opaque and visually unstable.
- Replaced dev.63 pair-normalized alpha with a Ghost-only single-surface rule for `create:netherite_backtank`.
- The synthetic `netherite_diving_layer_2` underlay is discarded while transparency is active; the outer `netherite_diving_layer_1` surface renders once at the requested opacity with late Entity depth stabilization.
- Suppresses the discarded inner layer's immediately associated armor glint when present, while preserving the outer layer/glint path.
- Added an item-aware deterministic fallback for 1.21.1 RenderType cases where the texture cannot be recovered from the RenderType description.
- 100% opacity remains native Create behavior with both synthetic diving layers.
- No file was deprecated or moved, so the project cleaner/build tooling is unchanged from dev.63.
- Network protocol remains 18.
- Source candidate pending Windows build and in-game regression QA.

- Build tooling hotfix: the normal `build.bat` no longer runs the migration cleaner automatically; Windows batch files are CRLF-normalized and Gradle runs in a child command process so the console remains readable on failure.

# 0.1.0-dev.63 — Create layered Backtank Ghost normalization

- Audited Create 6.0.10's Netherite Backtank rendering path instead of treating the visible chestpiece as a normal single armor layer.
- Confirmed the equipped `netherite_backtank` is a `BacktankItem.Layered`: Create intercepts Humanoid armor rendering and emits both `netherite_diving_layer_2` and `netherite_diving_layer_1`, while `BacktankArmorLayer` separately renders the actual tank block geometry.
- Added a no-hard-dependency compatibility rule keyed only to those two Create armor textures.
- During semi-transparent late Entity rendering, the two synthetic chest layers now use colour-only Ghost passes so they no longer compete with each other through late depth writes.
- Normalized each synthetic layer's alpha with `1 - sqrt(1 - requestedOpacity)` so the pair composes to approximately one ordinary armor layer at the requested opacity instead of applying the full Ghost alpha twice.
- Kept the physical Backtank geometry on the standard dev.62 late depth-stable Ghost path and retained the dev.61 simultaneous-buffer crash fix.
- Moved `CHANGELOG.md`, `DEVELOPMENT.md` and `THIRD_PARTY_NOTICES.md` under `docs/`; `README.md` remains the only primary documentation file at project root.
- Network protocol remains 18.
- Source candidate pending Windows build and in-game Create Backtank regression QA.

# 0.1.0-dev.62 — Late Entity transform + depth stabilization

- Confirmed dev.61 removes the Create Backtank `Not building!` crash.
- Fixed the dev.58+ late Ghost visibility regression: NeoForge's `AFTER_LEVEL` stage provides an identity `PoseStack` after Minecraft has already popped the world model-view matrix, so semi-transparent Entity projections lost the camera transform. dev.62 restores the event model-view matrix only around Mirage's late flush.
- Added Entity-only late Ghost RenderTypes that write colour and depth after level composition is already complete. This gives projected body/armor/modded equipment a stable self-depth surface without reintroducing the historical water depth-hole problem in Image/Banner/Item projection paths.
- Disabled translucent quad sorting for the late depth-stable Ghost RenderTypes because depth now resolves their local visibility and camera-dependent sorting could cause unstable coplanar ordering.
- Reordered the projection-owned fixed buffers so base surfaces are flushed before glint overlays and mirrored the important vanilla fixed-buffer sheets used by custom equipment renderers.
- Deferred buffers are flushed after each projected Entity instead of after the whole queue, isolating one hologram's fixed layers from the next.
- Network protocol remains 18.

# 0.1.0-dev.61 — Deferred Ghost multi-buffer safety

- Fixed the Create Backtank entity-projection crash caused by the late Ghost pass reusing one shared byte buffer for simultaneous render types.
- The deferred entity renderer now reserves independent buffers for vanilla glint variants and the known Mirage block-atlas, shield, banner and armor-trim Ghost render types.
- Custom/modded base render types still use the shared fallback, but can now coexist safely with foil/glint consumers such as Create's Backtank renderer.
- Preserved Ghost alpha/tint processing for the Backtank base model instead of disabling the custom armor layer.
- Kept dev.60 Core Booster Beacon relay behavior unchanged.
- Network protocol remains 18.
- Source candidate pending Windows build and in-game regression QA.

# 0.1.0-dev.60 — Core Booster Beacon relay

- Added loaded Core Booster relay handling to active Beacon columns without resurrecting the five legacy Improved Core products.
- Added a centralized cumulative relay state with a maximum of four effective loaded Boosters.
- Glass / Diffusion contributes +35 percentage points of width and broadens the outer beam.
- Quartz / Radiance contributes +25 percentage points of width and increases beam luminance.
- Amethyst / Resonance contributes +25 percentage points of width and multiplies outgoing rotation speed by ×1.25 per effective Amethyst, capped near ×2.
- Diamond / Focus contributes +25 percentage points of width while tightening the inner beam relative to the outer beam.
- Netherite / Inversion contributes +25 percentage points of width and reverses outgoing beam rotation without parity toggling.
- Total outgoing beam width is capped near ×2 vanilla and stained-glass section color remains authoritative.
- Crying Obsidian crystal attenuation/residual response now receives the accumulated upstream relay state before rendering.
- Core Booster tooltip and Jade output now identify the loaded Beacon relay effect.
- P0 cloud/modded-armor Ghost stabilization remains unchanged and still requires QA.

# 0.1.0-dev.59 — Full project audit / rake operation

- Performed a project-wide source/resource/documentation audit without intentionally adding a new gameplay feature branch.
- Removed Java/Javadoc source comments and Gradle explanatory comments under the current cleanup rule.
- Normalized 4-space formatting, tabs/trailing whitespace, import ordering, wildcard imports, compact multi-statement formatting, empty catches and active TODO/FIXME/HACK markers.
- Renamed the five old Improved Core compatibility classes/symbols to `LegacyImprovedCore*` / `LEGACY_*` while preserving serialized registry IDs for old development-world migration.
- Removed the obsolete legacy Improved Core renderer and moved the common Crying Obsidian random-tick/light Mixin out of the client-only Java package.
- Rebuilt documentation authority: current implementation, architecture, registry inventory, Power/chassis, Core Booster/upgrades, Crying Obsidian, Entity/snapshots, asset pipeline, roadmap and regression QA now have one current dev.59 set.
- Moved 112 pre-dev.59 wave documents to `docs/archive/pre-dev59/` as historical/migration evidence rather than active planning authority.
- Preserved the user-supplied Core Booster geometry and canonical vanilla Amethyst references under `docs/reference/`.
- Reconciled the active waitlist: dev.58 ghost/cloud/modded-armor acceptance, Core Booster Beacon relay, useful-range crystal lighting, per-equipment-channel visibility, renderer/entity hardening, mounted/composite design, release hardening and 1.1.0+ Scan Codex.
- Removed stale active promises for Effigy/Colossal and stale future entries for already-implemented systems.
- Updated README, DEVELOPMENT, in-game handbook text and metadata description to current behavior.
- Network protocol remains 18.
- Source candidate pending Windows `build.bat` and regression QA.

# 0.1.0-dev.58 — Late ghost composition + modded armor coverage

- Semi-transparent ENTITY projections now stay queued until NeoForge `AFTER_LEVEL`, after Minecraft has finished compositing clouds/weather/transparency targets.
- Fully opaque ENTITY projections remain on the established `AFTER_TRIPWIRE_BLOCKS` pass so vanilla opaque RenderTypes still execute in their normal world-render phase.
- Mirage ghost RenderTypes return to `MAIN_TARGET`; the late timing, rather than a framebuffer swap, is now responsible for keeping clouds behind the hologram.
- Projection buffer remapping now catches raw `solid/cutout/translucent` block-atlas layers used by custom armor renderers.
- Any non-effect custom RenderType with a recoverable texture is routed through Mirage's blended ghost pass even if its RenderType name is mod-specific.
- This specifically targets custom-rendered equipment such as Create Backtanks, which bypass vanilla `HumanoidArmorLayer.renderModel`.
- Effect-only passes (eyes, glint, beams, outlines, text, shadows, water mask, etc.) remain native to avoid breaking their specialized shaders.
- Protocol remains 18.

# 0.1.0-dev.57 — Transparency composition experiment

- Moved Mirage ghost entity/item/armor output from `MAIN_TARGET` to `ITEM_ENTITY_TARGET` in an attempt to stop vanilla clouds compositing over semi-transparent projections.
- Preserved color-only writes to avoid reintroducing the historical water/depth-hole regression.
- In-game QA showed the cloud ordering issue still occurred, so this framebuffer-only approach was superseded by dev.58 late-stage composition.
- Protocol remains 18.

# 0.1.0-dev.56 — Beacon optics rework

- Residual Crying Obsidian rays now originate at exact centered X/Z with the visual origin lowered to approximately model pixel Y=2 instead of inheriting a rotated +0.5 X/Z offset.
- Residual rays are 50% narrower and approximately 66% shorter than the old mature-cluster implementation.
- Bursts now appear instantly at full length, hold for 20 ticks, then retract while fading for 20 ticks.
- Residual width/length/frequency now scale by stage: MATURE 100%, LARGE 75%, MEDIUM 50%, SMALL 25%.
- Vanilla Beacon beam rendering stops at pixel 0 of every Crying Obsidian crystal block and resumes only above buds, with stage-based attenuation.
- Bud stages no longer emit block light when energized; only MATURE cluster does.
- Direct energized MATURE cluster above a Beacon suppresses the Beacon vertical beam and requests dynamic suppression of the Beacon block's own light while the cluster becomes the light source.
- Energized mature-cluster texture now uses a stronger saturated purple/lavender palette.
- Core Booster behavior from dev.55 is unchanged.
- Same-version compile repair corrected `CoreBoosterBlockEntity#isEmpty()` to call the material accessor rather than an out-of-scope variable.
- Protocol remains 18.

# 0.1.0-dev.55 — Core Booster cleanup and visual-state sync

- Removed the five legacy Improved Core BlockItems from the item registry, eliminating their Creative/Search/JEI-style exposure as independent obtainable blocks.
- Retained only the old block IDs as hidden compatibility shims; placed legacy Improved Core blocks schedule a one-tick migration into the single Core Booster with the matching stored material.
- Added authoritative `material` BlockState values (`empty`, `glass`, `quartz`, `amethyst`, `diamond`, `netherite`) to Core Booster.
- Core Booster renderer now resolves its center item from the synchronized blockstate instead of a potentially stale client BlockEntity field.
- Shift-right-click extraction therefore transitions the block to `material=empty`, immediately clearing the hologram instead of leaving a ghost core behind.
- dev.54 BlockEntity-only material NBT is migrated into the new blockstate on world/chunk load.
- Loaded Booster ItemStacks still use `BLOCK_ENTITY_DATA`, preserving same-material-only stacking and restoring their material on placement.
- Protocol remains 18.

# 0.1.0-dev.54 — Core Booster redesign

- Replaced five user-facing Improved Core variants with one stateful **Core Booster**.
- Added manual material socket: right-click inserts Glass / Quartz / Amethyst Shard / Diamond / Netherite Ingot; Shift + right-click returns it.
- Empty Booster is not a valid projector Core; loaded Booster maps to the matching ×1.50 improved profile.
- Material persists in Booster ItemStack block-entity data, giving same-material-only stacking automatically.
- Correct pickaxe mining returns one loaded Booster item without requiring Silk Touch.
- Adopted the user-supplied four-cube nested geometry as the canonical Booster model.
- Added synchronized block-entity material state so the center hologram is available immediately on chunk/world load.
- Added optional Jade material/amplification readout.
- Removed the five old Improved Core crafting recipes and hid their creative entries; legacy registry IDs remain for QA-world compatibility.

# 0.1.0-dev.53 — Canonical Amethyst visual alignment

- Adopted the user-supplied vanilla Amethyst 16×16 textures and item JSON transforms as the canonical visual law for Crying Obsidian Small/Medium/Large buds and Mature Cluster.
- Recolored the exact vanilla silhouettes/shading layout into the Crying Obsidian palette instead of approximating a crystal shape.
- Preserved current growth timing after QA reported the lifecycle was acceptably close to Amethyst overall.
- Further tightened the then-current Improved Core nested middle frame for visual containment.
- Protocol remains 18.

# 0.1.0-dev.52 — Crystal/Core visual polish experiment

- Reworked the then-current Improved Core middle shell into an inset rotated wireframe in response to shell geometry escaping the outer block.
- Repainted Crying Obsidian growth textures toward an Amethyst-like silhouette while keeping the current growth logic.
- This approximate texture pass was superseded by dev.53, which uses the user-provided vanilla Amethyst assets as the canonical source.
- Protocol remains 18.

# 0.1.0-dev.51 — Crying growth + asset/core QA correction

- Crying Obsidian nucleation now checks `FluidTags.LAVA`, so both lava source and flowing lava directly above the generator are valid. This matches the visible lava-drip setup instead of silently accepting only source blocks.
- Initial Small-Bud nucleation is raised from 1/32 to 1/5 per eligible Crying-Obsidian random tick; later stage denominators remain unchanged for this QA wave.
- Crying Obsidian Small/Medium/Large Bud and Cluster world models now use the same vanilla `minecraft:block/cross` model structure as amethyst buds/clusters, with Mirage textures only.
- Crying Obsidian Shard now uses the vanilla prismarine-shard silhouette reconstructed from the in-game reference while retaining the approved purple gradient, centered in the 16x16 texture.
- Obsidian Spike inventory presentation is scaled down so its tips no longer touch the slot border; world geometry is unchanged.
- Improved Core outer shell now spans the full 16x16x16 block volume instead of floating inset from every face.
- Removed the three frozen orthogonal material planes from placed Improved Cores. Added a dedicated block entity renderer that draws one actual Glass/Quartz/Amethyst/Diamond/Netherite item at the center, full-bright, tilted and rotating clockwise like the projector Core hologram.
- Improved Glass and Quartz therefore use their real item/block presentation in-world instead of depending on flat texture planes that could disappear.
- Protocol remains 18.

# 0.1.0-dev.50 — Client mixin boot-crash hotfix

- Removed the dev.47 `RenderLayerMixin` redirect after real pack QA showed that its target does not exist in the production-mapped `RenderLayer` class.
- This fixes the startup `InjectionError` (`0/1 succeeded; Scanned 0 target(s)`) that cascaded into unrelated mods failing construction.
- The dev.49 Crying Obsidian `BlockStateBase` random-tick dispatch fix remains unchanged and is still the growth-system candidate to test once the game boots.
- Cat-collar/secondary-layer ordering is explicitly left pending instead of keeping a crash-prone generic redirect.

# 0.1.0-dev.49 — Random-tick dispatch fix

- Fixes the dev.48 startup crash caused by targeting `Block#randomTick`, which is not declared on `Block` in this target.
- Moves Crying Obsidian nucleation to `BlockBehaviour.BlockStateBase#randomTick`, the state-level random-tick dispatch point.
- Keeps the existing `BlockStateBase#isRandomlyTicking` override for vanilla Crying Obsidian.
- Removes the broken standalone `CryingObsidianGrowthMixin`.
- Carries forward all dev.47 visual/UI fixes.

# 0.1.0-dev.48 — Crying Obsidian nucleation hook experiment

- Attempted to retarget Crying Obsidian nucleation from `BlockBehaviour#randomTick` to `Block#randomTick`.
- Real startup testing showed that target is not declared on `Block` for this mapped target, causing a Mixin bootstrap failure.
- The approach was immediately superseded by dev.49's `BlockBehaviour.BlockStateBase` random-tick dispatch hook.
- Protocol remains 18.

# 0.1.0-dev.47 — QA visual/UI polish experiment

- Changed projector Core presentation away from a camera-facing billboard toward a world-like clockwise holographic spin.
- Increased Core Chamber shell visibility and cleaned overlapping Image/Item/Banner/Entity workspace helper text.
- Removed redundant Banner and Entity-preview helper lines.
- Experimented with secondary entity-layer ghost normalization and revised Crying Obsidian bud/cluster/shard visuals.
- The generic `RenderLayer` redirect introduced here was later proven invalid in production mappings and removed in dev.50; the approximate crystal visuals were later replaced by canonical Amethyst-derived assets.
- Protocol remains 18.

# 0.1.0-dev.46 — Improved Projection Cores

- Added five placeable/installable Improved Cores: Glass, Quartz, Amethyst, Diamond and Netherite.
- Improved Cores retain their standard material Base PU and use the initial x1.50 amplification target.
- Added original three-shell translucent models with an actually rotated middle shell and a centered material/item visual.
- Added the shared preferred `GSG / SCS / GSG` crafting family using Glass + Crying Obsidian Shards + the matching base Core material.
- Core socket, shift-click handling and Power calculations accept Improved Cores without a second power system.
- Empty Core tooltip now orders standard/improved entries by effective material output.
- Metadata author is now explicitly `Celerbi`; mod metadata/description contains no repository URL.
- Beacon relay/stacking/material effects remain deliberately deferred to the next wave.
- Network protocol remains 18.

# 0.1.0-dev.45

## Compile repair

- Repairs the first Windows dev.45 `:compileJava` failure without advancing the development version.
- Moves `ClipContext` to its Minecraft 1.21.1 package, `net.minecraft.world.level.ClipContext`.
- Imports `net.minecraft.world.level.block.Block` for the stateful Survival drop path using `Block.popResource`.
- The existing eight `EventBusSubscriber.Bus` messages remain deprecation warnings only.
- Rebuild on Windows is still required before dev.45 can be marked build-clean.

## Stateful projector items

- Ordinary Survival mining now packs the complete `MirageProjectorBlockEntity` into the dropped projector ItemStack through vanilla `BlockEntity.saveToItem` / `minecraft:block_entity_data`.
- Packed mining suppresses duplicate Core, Entity Scan card and staging-item ejection because those physical stacks are already inside the projector state.
- Replacing the packed item restores state through vanilla BlockItem BlockEntity-data loading.
- Middle-click clone preserves loaded projector state when a real Level is available.
- Non-player destruction keeps the older physical-safety fallback; full virtual-state preservation for explosions/pistons is not promised yet.

## Canonical upgrade transfer

- Adds `ProjectorStateTransfer`; recipe logic no longer knows individual persistent fields.
- Uses `ItemStack.transmuteCopy` so source item component patches/custom names survive chassis changes.
- Loads the entire source BlockEntity payload into a temporary target-chassis BlockEntity, allowing the target's existing migration/sanitization rules to normalize incompatible state.
- Clean Compact items materialize their implicit default Glass Core before Compact -> Display upgrades.
- Scale/Lift/Float and all presentation/source state remain user-selected; upgrades do not auto-inflate projections.

## Crafting progression

- Adds the from-scratch Mirage Projector recipe: 5 Crying Obsidian Shards + Glass Block + 3 Obsidian.
- Adds custom exact-pattern `mirage_projector:projector_upgrade` recipes.
- Mirage -> Display uses 2 Quartz + 2 Amethyst Shards + 4 Crying Obsidian Shards.
- Display -> Wide and Display -> Tall each use 6 shards + 2 Glass with horizontal/vertical pattern geometry.
- Display -> Prism uses 4 shards + 4 Glass.
- Display -> Field uses 4 whole Crying Obsidian + 4 shards; this exact material cost remains balance-provisional.
- There are no direct Compact -> Wide/Tall/Prism/Field recipes.

## Documentation / status

- Adds dev.45 authority, implementation audit, roadmap, QA and next-chat handoff.
- Promotes Cores/Upgrades and Crying-Obsidian ecosystem contracts to dev.45 current copies.
- Protocol remains 18.
- Source candidate only until Windows `build.bat` and in-game state-transfer QA pass.

---

# 0.1.0-dev.44

## Obsidian Spike

- Adds `Obsidian Spike`, crafted from exactly 3 Crying Obsidian Shards, 2 String and 1 Stick.
- Adds a true nine-spire ~half-block model; the center point is thinner/taller and surrounding points have varied height/lean.
- Uses a dedicated 16x16 Crying-Obsidian texture rather than reusing Amethyst artwork.
- Trap has no solid collision body, so living entities enter the spike volume instead of standing safely on a half-block.
- Applies berry-bush-like movement drag `(0.8, 0.75, 0.8)` to living entities.
- Requests 2.0 damage points (1 heart) on each movement-triggered server-side contact; normal Minecraft hurt immunity controls successful repeated events, so this is **not** 2 damage every game tick.
- Counts vertical movement as well as horizontal movement, allowing a straight fall onto the spikes to hurt.
- Adds a Mirage-specific `obsidian_spike` DamageType/death message; items/projectiles/non-living entities are ignored.
- Requires floor support and returns itself from its block loot table.
- Adds Debug Handbook guidance, dev.44 authority/audit/QA/handoff and promotes the Crying-Obsidian/Core contracts to dev.44 current copies.
- Protocol remains **18**; no projector NBT/payload schema changed.

## Status

- Source candidate only until Windows `build.bat` succeeds. dev.44 inherits the still-unconfirmed dev.43 source candidate, so inherited crystal/Beacon build/render issues take priority.

---

# 0.1.0-dev.43

## Renewable Crying Obsidian crystal / Beacon ecosystem

- Adds four placeable Crying-Obsidian crystal stages: Small Bud, Medium Bud, Large Bud and mature Crying Obsidian Cluster, with Amethyst-like facing/waterlogging and original normal/energized pixel-art textures.
- Makes vanilla Crying Obsidian a renewable crystal generator only when a Lava **source** is directly above it and the growth space directly below is free/water. Natural growth is downward only. Initial nucleation is deliberately the slowest step; later stages progressively accelerate.
- Adds exact harvesting economy: without Silk Touch stages drop 1/2/3/4 Crying Obsidian Shards; Silk Touch preserves the stage; Fortune is intentionally not applied.
- Adds uncommon data-driven shard loot to Ruined Portals, Abandoned Mineshafts and village armorer/toolsmith/weaponsmith chests.
- Adds Beacon excitation state to crystals. Vanilla fallback powered block light is 14/15/15/15 by age; optional >15 lighting remains a future compatibility integration rather than a mandatory dependency.
- Adds custom Beacon rendering when a crystal lies in the active column. Stained-glass section colors are preserved while crystals visually transmit approximately 75/50/25/0% of the vertical beam. Mature visually terminates the beam without disabling Beacon gameplay/effects.
- Adds deterministic Crying-Obsidian residual refraction beams: one leak per energized crystal, roughly half vanilla Beacon-inner width, stage-scaled length, solid-block ray clipping, ~1 s extension, ~1 s full-length hold, then ~1 s retract/fade followed by stage-specific cooldown.
- Keeps stacked-crystal energy semantics: lower crystals attenuate the input reaching higher crystals, and a Mature Cluster prevents higher crystals from becoming Beacon-energized by the same column.
- Adds dev.43 implementation audit, authority/handoff, ecosystem contract and QA checklist.
- Protocol remains **18**; this wave adds blocks/resources/render hooks but does not change Mirage packet schemas.

## Status

- Source candidate only until Windows `build.bat` succeeds and in-game growth, loot, light and Beacon-render QA pass.
- dev.42 physical chassis/Core-Chamber behavior remains part of the baseline and should be regression-tested alongside dev.43.

---

# 0.1.0-dev.42

## Material / Core Chamber / chassis rework

- Implements `Crying Obsidian Shard` as the final shared shard item name. Adds a 16x16 sprite, creative-tab registration, `1 Crying Obsidian -> 4 shards` Stonecutter recipe, and both `8 shards + Fire Charge` / `8 shards + Magma Cream -> 1 Crying Obsidian` re-form recipes.
- Replaces all six broad Glass/Glass-Pane chassis surfaces with a composite material language: solid Obsidian structure, translucent Crying-Obsidian optical/emitter geometry, and a small central Glass Core Chamber.
- Converts all six block models to `neoforge:composite` with independently rendered solid/emitter/chamber children so the structural body no longer lives in the translucent render layer.
- Gives Field a visibly Crying-Obsidian structural base, matching the planned expensive whole-Crying-Obsidian upgrade identity.
- Removes the active fake Core material-block renderer (`Netherite Ingot -> Netherite Block`, etc.). The BER now renders the actual installed Core ItemStack in the chamber using uniform scale, camera-facing GUI-model rotation, subtle bob and fullbright lighting.
- Removes legacy per-chassis Core width/height/depth sizing from `ProjectionChassisProfile`; the chamber is universally 4x4x4 model pixels and chassis only choose its center Y.
- Keeps Compact's historical default Glass Core behavior, all Power formulas, saved state, Image/GIF/Entity behavior and network protocol 18 unchanged.
- Adds dev.42 documentation authority, implementation audit, QA checklist and updated Crying-Obsidian/Core contracts.
- Freezes future Improved-Core -> Beacon -> powered-crystal coupling: wider relay beams must visibly excite crystals more, radiance must increase useful crystal lighting where the lighting backend allows, and stacked Core modifiers resolve to one effective beam before crystal response is calculated.
- Extended-light provider selection remains future work; do not rewrite Minecraft's global light engine merely to exceed vanilla level 15.

## Status

- Source candidate only until Windows `build.bat` and in-game physical/Core regression QA succeed.
- Protocol remains **18**.

---

# 0.1.0-dev.41

## Final design-closure addendum (same dev.41 line)

- Keeps the version at dev.41 while closing the material/progression roadmap before the next implementation chat. No new gameplay code or protocol changes are introduced by this addendum.
- Adds `docs/CRYING-OBSIDIAN-ECOSYSTEM-dev41.md` as the authoritative replacement for the earlier normal-Obsidian dripstone/cauldron conversion plan. Renewable Crying Obsidian now starts from a genuine Crying Obsidian source with Lava above it and grows downward through Small/Medium/Large/Mature crystal stages.
- Renames the planned shared material from **Cut Obsidian Shard** to **Crying Obsidian Shard**, because shards can now come from Stonecutting or natural crystal harvest. Stonecutter remains `1 Crying Obsidian -> 4 shards`; `8 shards + Fire Charge` or `8 shards + Magma Cream -> 1 Crying Obsidian` remains the re-form loop.
- Freezes no-Silk crystal drops at 1/2/3/4 shards by age, Silk Touch stage recovery, no initial Fortune bonus, slowest first nucleation and progressively faster later growth.
- Defines age-dependent Beacon absorption: approximate visual continuation 75/50/25/0% from Small to Mature, while Beacon gameplay remains active. Mature Cluster visually terminates the vertical beam.
- Defines powered crystal target light curve 14/18/23/28 when an optional extended-light provider exists, with vanilla-compatible fallback capped at 15. Purple world-light tint is not guaranteed without colored-light integration.
- Defines residual escape/refraction beams: Crying-Obsidian purple, about half Beacon-inner-beam width, one at a time, mature target 3-4 blocks, breakthrough/extension -> ~1 s full-length hold -> simultaneous retraction/fade, collision-limited and pseudo-random direction.
- Adds `docs/CURRENT-STATE-ROADMAP-dev41.md` as the next-chat handoff and implementation-order authority.
- Rewrites `CORES-AND-UPGRADES-dev41.md`, README, DEVELOPMENT, documentation authority and current audit to remove the retired Obsidian-conversion/Jade-intermediate plan and to reference the new crystal ecosystem.
- Records Glowstone only as an uncommitted brainstorm with **no current role**; future chats must not infer a Core/recipe replacement from it.
- Preserves the optional future Unrefined Crying Crystal Core concept as backlog only: intentionally distorted/refracted projection signature, stats/recipe TBD after the standard/Improved Core pipeline is stable.

## Consolidation / documentation authority

- Adds the dev.41 progression documentation set; its final authority is split between `CRYING-OBSIDIAN-ECOSYSTEM-dev41.md`, `CORES-AND-UPGRADES-dev41.md` and `CURRENT-STATE-ROADMAP-dev41.md`.
- Adds `docs/DOCUMENTATION-AUTHORITY-dev41.md` so historical dev notes cannot silently override current contracts.
- Adds `docs/CURRENT-IMPLEMENTATION-AUDIT-dev41.md`, `docs/CODE-QUALITY-AUDIT-dev41.md` and `docs/DEV41-CONSOLIDATION-QA.md`.
- Rewrites README and DEVELOPMENT as current-state documents instead of embedding full duplicate histories; chronology remains in CHANGELOG and historical focused docs.
- Records user QA that dev.40 ran in-game and the Piglin dimension-shake correction is confirmed.

## Code/deprecation cleanup

- Removes unused `EFFIGY` and `COLOSSAL` placeholder values from the active `ProjectionChassisProfile`; the enum now represents exactly the six registered gameplay chassis. The six existing ordinals remain unchanged.
- Splits ImageSourceBank terminology into `ACTIVE_MULTI_SLOTS = 4` and `PERSISTED_COMPAT_SLOTS = 9`, preserving old wire/NBT data while making it explicit that Field does not have nine active sources.
- Removes the unused `ProjectionChassisProfile#sourceCapacity` field; active source capacity is already expressed by real Wide/Tall layout methods and Prism face rules.
- Removes the unused raw-English `ProjectionCoreProfile.displayName`; Core UI already uses translated components.
- Removes the unused `PLANNED_IMPROVED_AMPLIFICATION` code constant; x1.50 remains a design/balance target in docs until Improved Core gameplay is implemented.
- Renames the temporary Core material-block visual helper to `legacyBlockVisualStack()` and the old per-chassis Core sizing accessors to `legacyCore...`; the agreed universal Core Chamber + real ItemStack renderer supersedes both as final design.
- Centralizes network protocol `18` as `MirageProjector.NETWORK_PROTOCOL`; payload shape/protocol is otherwise unchanged.

## Early dev.41 progression draft — superseded within dev.41

The first dev.41 planning draft still used `Cut Obsidian Shard` terminology and a normal-Obsidian dripstone/cauldron conversion. Those definitions were replaced before dev.41 documentation closure. The final same-version design is recorded in the **Final design-closure addendum** above and the current authoritative dev.41 docs.

## Status

- dev.41 intentionally changes no Power formula, Image/GIF pipeline, Entity persistence or payload schema.
- Windows build + regression QA required before build-clean.

---

# 0.1.0-dev.40

## Power / Capacity GUI repair

- Reserves a dedicated left column for the physical Projection Core slot so Core/Power text can no longer paint over it.
- Restores a visible capacity/load progress bar directly below the Effective capacity / Load line.
- Removes the dev.38-dev.39 section-wide Power hover trap: moving the cursor through POWER / CAPACITY no longer opens a giant formula tooltip.
- Detailed PU breakdown remains available deliberately through a small `?` hotspot in the Power header.
- Keeps the exact dev.38 Power formula, Overdrive, dynamic slider bounds and Ghost rebate unchanged; this is a presentation/UX correction, not a rebalance.

## Dimension-neutral projection entities

- Fixes Piglin previews and world projections shaking continuously in the Overworld/End even though Mirage entities never tick or convert.
- Root cause: the vanilla Piglin renderer uses the live `AbstractPiglin#isConverting()` state for conversion shaking; reconstructed Mirage clones inhabit the current client dimension, so an ordinary Nether Piglin reports a zombification conversion when viewed in an unsafe dimension.
- `EntityProjectionClientEntityFactory` now normalizes temporary `AbstractPiglin` clones with zombification immunity after frozen NBT load.
- Applies the same projection-only immunity to Hoglin clones to prevent the equivalent Zoglin conversion state.
- The real mob, frozen scan-card NBT and source identity are never modified. Preview and world rendering share the same normalization path.
- Mirage still never calls `tick()` / `aiStep()` on projection-only entities.

## Documentation / status

- Adds `docs/ENTITY-DIMENSION-NORMALIZATION-dev40.md`.
- Adds `docs/DEV40-UI-ENTITY-QA.md`.
- Adds `docs/CURRENT-IMPLEMENTATION-AUDIT-dev40.md` as the new recovery authority while retaining dev.39 GIF/import documentation unchanged.
- Network protocol remains **18**; dev.40 changes no payload or persisted schema.
- Improved Core items/recipes/variant specializations remain the next design pass, not part of dev.40.
- dev.39 was successfully executed in-game; dev.40 remains a SOURCE candidate until Windows build + in-game QA.

---

# 0.1.0-dev.39

- Content-sniffed import failsafe: extension is ignored for decoder selection; renamed GIF stays animated, animated WebP/APNG are detected and rejected explicitly instead of silently flattening.
- Static import support is PNG/JPEG/WebP/BMP; GIF is the supported animated format. New assets use generic `.asset`, with dev.1-dev.38 `.png` compatibility retained.
- Prism Image faces gain adaptive Wide-like/Tall-like/Square-like nominal envelopes (80x32 / 32x80 / 48x48) while remaining four N/E/S/W faces with no stacking mode. Geometry overdrive is charged per face.

## GIF / Animated Image implementation

- Promotes GIF from future scope to a real **Image Workspace source** on all six current projector chassis.
- GIF is not a new SourceMode: every existing Image control continues to apply to the current animation frame (Scale, Lift, Rotation, Floating, Lighting, Ghost, Tint, Scanlines, Vertical Flip and Plane Front/Back semantics).
- Compact, Display and Field support one continuous static/GIF Plane.
- Wide/Tall support GIF in both `SINGLE` and `MULTI`; each of the four 4x1/1x4 cells may independently contain a static image or GIF.
- Prism North/East/South/West may each contain independent GIFs. `Same Source on All Faces` reuses one content-addressed animated asset and one decoded frame cache, keeping copied faces synchronized without duplicating bytes.
- Mirrored/Readable rear faces reuse the same current Front frame rather than creating a second animation instance; Independent Front/Back may use separate animations.

## GIF decoder / timing / safety

- Adds `GifAssetDecoder`, which decodes each asset once into full logical-screen RGBA frames and respects ImageDescriptor offsets plus `none/doNotDispose`, `restoreToBackground` and `restoreToPrevious` transitions.
- Frame timing is read from GIF centiseconds. Zero delay falls back to 100 ms; non-zero delay is clamped to at least 20 ms (50 FPS ceiling); one frame may wait at most 10 seconds; one loop may total at most 5 minutes.
- Mirage intentionally loops projected GIFs continuously; finite source loop counts are not used as a projection lifetime.
- Playback is selected from a shared monotonic client clock. Reusing the same GIF asset on one client therefore resolves to the same frame at the same moment. Wide/Tall MULTI and Prism sample that clock once per layout draw so duplicated source ids cannot split across an animation-frame boundary inside one projector render.
- GIF technical limits: 8 MiB preserved source asset, 1024 px maximum logical canvas axis, 128 frames and 16,777,216 decoded frame-pixels (`canvasWidth * canvasHeight * frameCount`). These are memory/network safety rails, not gameplay Scale limits.
- Validates each encoded GIF frame rectangle against the logical canvas **before** asking ImageIO to allocate/decode frame pixels, closing the compressed-descriptor memory-bomb path.
- Static images keep the 2048 px normalization dimension and now share the generic 8 MiB persisted asset ceiling; they are still normalized to PNG.
- GIF has **no additional PU surcharge**. PU models emitter geometry/presentation; animation decode cost is bounded by technical limits instead of hidden client-only gameplay power.

## Generic asset transport/cache

- Static PNG and animated GIF now share one content-addressed transport envelope. The server accepts only supported PNG/GIF signatures within their type limit and verifies SHA-256 before storing.
- Transport maximum becomes 8 MiB while retaining 32 KiB chunks.
- New local/world assets use neutral `<hash>.asset` filenames because stored bytes may be PNG or GIF.
- dev.1-dev.38 `<hash>.png` cache/world assets remain readable and uploadable/downloadable; SHA-256 identity is unchanged and no existing image NBT is rewritten.
- `ProjectionTextureCache` now caches either one static DynamicTexture or all validated GIF frame textures. `invalidate`/session clear releases the complete frame set.
- Network protocol increases **17 -> 18** so dev.38 and dev.39 do not silently mix asset-validation semantics.

## Wide/Tall SINGLE aspect rules frozen

- Adds `ProjectionImageSizing` as the shared renderer/power authority for continuous Image Plane dimensions.
- SINGLE images/GIFs are never cropped or stretched.
- Wide + landscape/square: Scale controls projected width; height follows aspect ratio.
- Wide + portrait: portrait height remains the dominant Scale axis; the source is legal but reaches Wide vertical overdrive sooner.
- Tall + portrait/square: Scale controls projected height; width follows aspect ratio.
- Tall + landscape: landscape width remains the dominant Scale axis; the source is legal but reaches Tall horizontal overdrive sooner.
- Power and renderer now consume the same sizing helper, preventing a future mismatch where PU is calculated for different dimensions than the quad actually rendered.
- MULTI remains the dev.38 square-cell contract: Scale 80 = Wide 80x20 / Tall 20x80 with four equivalent 20x20 cells.
- The Image layout button now explains the Wide/Tall SINGLE orientation rule in its tooltip.
- Wide/Tall SINGLE workspace also surfaces source dimensions, current Scale-derived projected W×H, and highlights the cross-orientation case (portrait in Wide / landscape in Tall) so the rule is visible without opening documentation.

## Documentation

- Adds `docs/GIF-ANIMATED-IMAGE-dev39.md` as the authoritative animation/import/transport/playback/safety contract.
- Adds `docs/WIDE-TALL-SINGLE-ASPECT-dev39.md` as the authoritative aspect/Scale/Overdrive contract for one continuous Wide/Tall image.
- Adds `docs/CURRENT-IMPLEMENTATION-AUDIT-dev39.md` and `docs/DEV39-GIF-QA.md` for recovery/build/in-game validation.
- Debug Handbook General pages now document GIF support/limits; Wide/Tall tabs explicitly document SINGLE aspect behavior.
- Older README/DEVELOPMENT statements that call GIF future-only are superseded by the dev.39 authority section and docs above.

## Validation status

- Source-level implementation is complete for the dev.39 candidate, but Windows `build.bat` and in-game QA remain authoritative before marking it build-clean.
- Priority live QA: transparent/disposal-heavy GIF, Compact/Display/Field, Wide/Tall SINGLE and four-source MULTI, Prism four independent GIFs, Front/Mirrored/Readable/Independent, all Image effects, save/reload, second-client download, old `.png` migration and technical-limit rejection.

---

# 0.1.0-dev.38

## Projection Power System Rework

- **Deprecates the dev.8/dev.19 double-hard-cap model.** Projection Cores no longer define independent Scale/Lift/Float maxima and chassis geometry/Lift/Float values are no longer absolute gameplay walls.
- Standard Core base-output curve is now Glass 32 PU, Quartz 48 PU, Amethyst 64 PU, Diamond 96 PU and Netherite 128 PU.
- Adds chassis PU multipliers: Compact ×1.00, Display ×1.50, Wide ×2.00, Tall ×2.00, Field ×4.00 and Prism ×2.00.
- Effective capacity is now `floor(Core base PU × chassis multiplier × Core amplification)`.
- Standard raw-material Cores use amplification ×1.00. `ProjectionCoreProfile` reserves the amplification term for future purpose-built Improved Cores; the current design target is roughly ×1.50 while retaining the material's same base PU. Improved Core items/recipes/textures are not introduced yet.
- Chassis W×H/Lift/Float values become **nominal efficiency targets**. Going beyond nominal is legal Overdrive when sufficient PU exists.
- Overdrive applies a quadratic penalty independently to Geometry, Lift and Float: a component at ratio `r=current/nominal` costs `r²` once `r>1`.
- Removes runtime `CORE_SCALE_LIMIT`, `CORE_LIFT_LIMIT`, `CORE_FLOAT_LIMIT`, `CHASSIS_ENVELOPE_LIMIT`, `CHASSIS_LIFT_LIMIT` and `CHASSIS_FLOAT_LIMIT` failure paths. Power validation is now Core present + physical Float/Lift safety + Effective PU budget.
- Keeps the physical invariant `Float amplitude <= Lift` while Floating is enabled.

## Exact PU breakdown + Ghost efficiency

- Every non-empty projection pays a 2 PU emitter/stability base.
- Geometry base cost is `ceil(projected area / 256)` PU before Geometry Overdrive.
- Lift base cost is `ceil(Lift / 16)` PU before Lift Overdrive.
- Float base cost is `ceil(Float / 2)` PU while Floating is active, before Float Overdrive.
- Explicit source-complexity surcharges: Independent Plane +1; populated Wide/Tall MULTI beyond one source +1; Prism Image +2; Item +2; Entity +4; Banner Plane +1; Banner Prism +2.
- Explicit presentation costs: Rotation +1, rotation-synced Floating +1, Fullbright +1, Image Scanlines +1; Tint/Flip remain free.
- Ghost now supplies a deliberately tiny PU rebate: `floor((grossPU - 2) × Ghost% / 3000)`. At the 90% Ghost safety maximum it cannot exceed 3% of non-base load and rounds down, preventing low-cost abuse.
- Adds `ProjectionPower.Breakdown` and a Power-section hover tooltip showing the complete capacity formula and every load component.

## Dynamic slider/Power UX rework

- Scale, Lift and Float slider endpoints are generated from the largest currently feasible value under the installed Core, chassis multiplier, active settings and current Effective PU.
- The player no longer intentionally drags through invalid/orange ranges to find a pixel threshold.
- If a Core/feature change makes an already-loaded configuration invalid, recovery prefers reducing Float, then Lift, then Scale only as a final fallback.
- Power panel now prioritizes Core base output, chassis multiplier, Core amplification, Effective capacity/load, actual dimensions vs nominal and current/effective maximum for Scale/Lift/Float.
- `Remaining PU` is no longer the primary limit explanation; exact PU components are available on hover.
- Creative Debug chassis now removes Overdrive penalties for stress tests but still requires a Core, still obeys Effective PU and still obeys `Float <= Lift`.
- Internal Scale/Lift/Float technical ceilings are safety/search rails only and are no longer documented as chassis gameplay limits.

## Chassis/Image contract recovery

- Rolls back the accidental dev.33 mandatory multi-source interpretation. **Field is restored as one continuous large Plane**; the 3×3/9-image UI/render contract is removed.
- Current Field nominal target becomes 128×128 px / Lift 144 / Float 24 with chassis multiplier ×4.00.
- Wide and Tall gain an explicit Image layout toggle: one continuous image (`SINGLE`) or four independent images (`MULTI`) arranged 4×1 / 1×4.
- MULTI uses four equal square cells. Scale 80 resolves to Wide 80×20 or Tall 20×80 (four 20×20 cells), fixing the Tall-vs-Wide source magnification mismatch.
- Current nominal targets: Compact 10×10 / 32 / 4; Display 32×32 / 48 / 12; Wide 80×32 / 64 / 12; Tall 32×80 / 96 / 16; Field 128×128 / 144 / 24; Prism 48×48 / 96 / 12.
- Legacy `ImageSourceBank` still serializes nine slots only to preserve dev.33-dev.37 data. Field never consumes/displays the old grid; slot 0 can be recovered as continuous Front on migration. Wide/Tall use slots 0-3 in MULTI.
- Adds/persists `ImageLayoutMode`; network protocol remains **17** for the dev.38 source line.

## Placement / GUI / Entity-lifetime / render-pipeline fixes carried in the same wave

- Adds furnace-style horizontal `FACING` to all current projector blocks. Plane rendering follows the placed chassis orientation.
- Prism Image/Banner sources remain true world-cardinal North/East/South/West and are not renamed by physical block facing.
- Every registered chassis now renders the same floating vanilla-book idle marker when no renderable source exists, even when its Core socket is empty. This removes the Compact-only idle feedback caused by Compact historically starting with Glass while the other five chassis start with empty Core sockets.
- Fixes Entity workspace kind transitions: inserting a card whose kind exposes a different equipment-row family clears virtual Incoming/Projected snapshots belonging to rows that disappear. Humanoid -> Horse/Generic clears all six Humanoid armor/hand snapshots; Horse -> Humanoid/Generic clears Horse Saddle/Body snapshots; Generic exposes no editable equipment rows.
- Removing a Humanoid card by itself still preserves the bodyless Humanoid mannequin use case. The new cleanup occurs when a different card kind becomes active and the GUI actually changes slot family, preventing hidden NBT/memory from resurfacing later.
- Incompatible virtual workspace cleanup also resets that workspace's pose preset to its default. Physical staging items are never silently deleted; normal menu-close return/drop safety remains authoritative.
- Adds a dev.38 load-time migration guard: saved projectors with an active Generic/Horse/Humanoid body prune virtual equipment from incompatible hidden families, while true no-body mannequin saves are left untouched.
- Fixes Item Snapshot Workspace heading/preview collision: the Screen owns the label and the preview renderer only renders model content.
- Keeps the dev.37 Entity render-order recovery: deferred Entity holograms use Mirage's private BufferSource at `AFTER_TRIPWIRE_BLOCKS`; Mirage must not flush Minecraft's global block/entity buffer again.

## Documentation sweep

- Adds `docs/POWER-SYSTEM-REWORK-dev38.md` as the authoritative mathematical/power contract.
- Rewrites `docs/CHASSIS-IMAGE-LAYOUT-POWER-UX-dev38.md` as the authoritative chassis/image/facing/GUI contract.
- `docs/MULTI-SOURCE-IMAGE-LAYOUTS-dev33.md` remains historical and explicitly SUPERSEDED.
- README and DEVELOPMENT mark the dev.8/dev.19 hard-cap model and dev.33 Field grid as historical/superseded wherever they could otherwise be mistaken for current rules.
- Adds `docs/ENTITY-WORKSPACE-LIFETIME-dev38.md` as the authoritative card-kind/virtual-equipment lifetime contract and updates the long-lived Entity contract with the dev.38 transition rule.
- Marks GIF/animated-image import as the first feature priority after dev.38 stabilization; Improved Core items/recipes/models remain a subsequent balance/art wave rather than being mixed into this stabilization build.

## Validation

- Static source/data consistency and documentation audits are required before packaging.
- Windows `build.bat` and in-game QA remain authoritative before dev.38 can become build-clean.
- Priority QA: Field continuous Plane; Wide/Tall SINGLE/MULTI symmetry; core/chassis effective capacity and Overdrive; dynamic slider endpoints; Ghost rebate; N/E/S/W block placement; floating idle book on all six chassis; Entity Humanoid/Horse/Generic card-kind cleanup; Item Workspace layout; and dev.37 Entity/projector/water depth-order regression tests.

---

# 0.1.0-dev.37

## Render-order recovery + tabbed Debug Handbook

- Moves the deferred Entity hologram pass from NeoForge `AFTER_BLOCK_ENTITIES` to `AFTER_TRIPWIRE_BLOCKS`, after translucent world geometry has established its depth. Water behind a hologram can therefore remain behind it, while nearer water still occludes it normally.
- Deferred Entity projections now use a Mirage-owned `MultiBufferSource.BufferSource`. The pass no longer calls `endBatch()` on Minecraft's shared entity/block-entity buffer source.
- This isolates Entity batching from Image/Banner/other BlockEntity rendering and specifically repairs the dev.36 pipeline regression that disturbed previously-correct Image projections. Image layout/source semantics themselves are unchanged from dev.33.
- Physical Mirage projectors are rendered before the late Entity pass and remain in the depth buffer. A projector behind an Entity hologram can no longer win merely because its BER was submitted later; a physically nearer projector still occludes the hologram correctly.
- Debug Handbook keeps vanilla `BookViewScreen` page rendering but is now dynamically enlarged and vertically/horizontally centred.
- Replaces the long linear handbook with seven side tabs: General, Compact, Display, Wide, Tall, Field and Prism. Each chassis tab explicitly explains its intended use and hard chassis limits. General covers source modes, Projection Cores/power, independent Core/chassis limits, virtual snapshots, global presentation controls, clearance and entity scanning.
- Removes the oversized vanilla Done button from the handbook; Esc remains the close gesture. The local non-blurred dim overlay and narrow first-person hand suppression remain unchanged.
- No network or persistent-data change; protocol remains 15.

## Validation

- Source/static validation performed in the recovery environment. Gradle/NeoForge compilation cannot be executed there because the wrapper JAR/distribution and dependency cache are not available offline. Windows `build.bat` remains authoritative before dev.37 is build-clean.
- Priority in-game QA: water in front/behind a Ghost Entity; multiple physical projectors crossing a giant Entity; Image projections on Compact/Display/Wide/Tall/Field/Prism; handbook at several GUI scales and all seven tabs.

---

# 0.1.0-dev.36

## Entity name/base label and deferred projection ordering

- Named mob scans now recover a projection label from `DisplayName` whenever it differs from the vanilla EntityType name, regardless of scan data version. Explicit frozen `CustomNameText` still has priority.
- Projected entity names are rendered at the projector base instead of above the scaled entity. The label therefore stays visible and stable even with very large Scale/Lift values.
- Entity holograms are no longer rendered inside each projector BER immediately. The physical projector/core is submitted first and the entity projection is deferred to NeoForge `AFTER_BLOCK_ENTITIES`.
- Deferred entity projections are sorted far-to-near, rendered after all physical block entities, and flushed before translucent world blocks. This removes other Mirage Projectors being painted on top of a projection while preserving the no-depth-write Ghost/water fix.
- No network protocol change; protocol remains 15.
- dev.36 was later compiled/run in-game by the user, but failed QA: Entity depth ordering remained incorrect against physical projectors/water and Image Mode regressed. dev.35 remains the last build-clean visual baseline.

# 0.1.0-dev.35

## Vanilla-book Debug Handbook + projected custom-name labels

- Replaces the custom DebugHandbookScreen panel with Minecraft 1.21.1 `BookViewScreen`, so the handbook uses the same in-game book rendering/background path as vanilla books instead of a generic menu blur stratum.
- Handbook content remains fully translatable; each handbook paragraph becomes one physical vanilla page to avoid clipping in the narrower book text area.
- Keeps first-person Debug Handbook hand suppression while its BookViewScreen is open as an additional fail-safe.
- Projection nameplates now resolve from the explicitly frozen mob `CustomName` first instead of trusting only the legacy `NameplateText` field.
- Reconstructed named mobs receive their frozen CustomName again (with vanilla nameplate visibility disabled to avoid duplication), keeping the temporary render entity semantically faithful.
- Custom-name labels move from the chassis/feet region to above the projected entity using species/pose-aware projected bounds, matching vanilla nametag expectations.
- Name labels use a two-pass vanilla-like font render for readability with Ghost while retaining normal opaque-world depth behaviour.
- Network protocol remains `15`; no new saved/network fields are introduced.

## Validation

- Source/static validation performed; Windows `build.bat` and in-game handbook/nametag QA remain authoritative before dev.35 is build-clean.

---

# 0.1.0-dev.34

## Entity custom-name fidelity + Debug Handbook hand suppression

- Entity Scan data version bumped `4 -> 5` and now freezes mob `CustomName` explicitly (`HadCustomName` + `CustomNameText`) instead of relying only on the generic display label.
- Player nameplate behavior remains explicit and unchanged: a scanned Player uses the frozen player/display name.
- Legacy pre-v5 mob scans attempt a conservative recovery when their stored DisplayName differs from the vanilla EntityType description.
- Entity Workspace preview footer now shows `name · entity type` whenever a projection nameplate exists, making nametag capture immediately verifiable.
- Projected custom-name labels keep a minimum readable offset above the chassis when Lift is near zero.
- Debug Handbook now cancels first-person rendering of the handbook item itself through NeoForge `RenderHandEvent` while the handbook screen is open. The dev.32 non-blurred dark overlay remains in place.
- Network protocol remains `15`; only the frozen Entity Scan card payload version changes.

## Validation

- Source/static validation performed; Windows `build.bat` and in-game nametag/handbook QA remain authoritative before dev.34 is build-clean.

---

# 0.1.0-dev.33

## Wide / Tall / Field multi-source Image layouts

- Adds a persistent `ImageSourceBank` separate from global ProjectionSettings.
- Wide uses four independent source cells in a 4×1 horizontal strip.
- Tall uses four independent source cells in a 1×4 vertical strip.
- Field uses nine independent source cells in a 3×3 matrix.
- Image Workspace now exposes a physical layout map, selectable source cells, a dedicated large preview, per-cell import/replace/clear and `Copy to empty slots`.
- Multi-source Plane rear rendering supports global Mirrored or Readable behavior. Independent rear banks are intentionally deferred instead of pretending one legacy Back asset applies to every cell.
- Global Scale controls the complete layout footprint; individual images preserve aspect ratio and are centered within their cells.
- Power charges populated cells while clearance/render bounds reserve the complete active layout envelope.
- Existing pre-dev.33 Front images on Wide/Tall/Field migrate into source slot 1 when no source bank exists.
- Network protocol bumped `14 -> 15` because Image Workspace menu/payload now carries the source bank.
- Includes the dev.32 Debug Handbook no-blur hotfix.

## Validation

- Source/static validation performed; Windows `build.bat` remains authoritative before dev.33 is build-clean.

---

# 0.1.0-dev.32

## Debug Handbook blur hotfix

- Debug Handbook no longer calls the generic blurred screen background path.
- The handbook now renders over a local dark overlay, so opening it cannot blur the same first-person book that remains visible behind the GUI.
- No networking or saved-data format changes; protocol remains `14`.

## Validation

- Source/static validation performed; Windows `build.bat` remains authoritative before dev.32 is build-clean.

---

# 0.1.0-dev.31

## Entity reconstruction fidelity

- Player scans now freeze the visual metadata that vanilla does not persist in ordinary entity NBT: skin model geometry (`slim`/`wide`), enabled player model parts (hat/jacket/sleeves/pants/cape) and dominant arm.
- The packed GameProfile `textures` property remains the self-contained skin/cape source; dev.31 additionally decodes its skin-model metadata and forces the reconstructed `RemotePlayer` to the captured model.
- Player model-part flags are restored after NBT load so the temporary projection matches the source player's visible second layers instead of inheriting RemotePlayer defaults.
- Fixes Generic pose cache invalidation: Cat/Wolf/Parrot `Idle/Sitting` now participates in the client entity fingerprint and recreates the frozen render entity immediately when the pose changes.
- Entity Scan data version bumped `3 -> 4`; old cards remain readable with vanilla-compatible defaults, but rescanning a Player is required to capture the new visual metadata.
- Network protocol remains `14`; the card's internal frozen scan payload is extended without changing packet layout.
- Added `docs/ENTITY-FIDELITY-dev31.md`.

## Validation

- Source/static validation performed; Windows `build.bat` and in-game Player/Generic QA remain authoritative before dev.31 is build-clean.

---

# 0.1.0-dev.30

## Species-aware Entity bounds

- Entity clearance and BlockEntity render bounds now derive from the scanned EntityType native width/height instead of treating every entity as a square Scale×Scale projection.
- Scale continues to target the largest native dimension, matching the world renderer.
- Humanoid pose expansion remains pose-aware; Horse Rearing receives a taller conservative envelope.
- Generic entities now reserve their own aspect ratio, reducing false obstruction checks around narrow/small mobs.
- Network protocol remains `14`; no new payload shape beyond dev.29.
- Added `docs/ENTITY-BOUNDS-dev30.md`.

## Validation

- Source/static validation performed; Windows `build.bat` remains authoritative before dev.30 is build-clean.

---

# 0.1.0-dev.29 — Power/limits UX + contextual sitting pose

## Power/limits UX + contextual sitting pose

- Adds persistent `Idle / Sitting` projection poses for vanilla Cat, Wolf and Parrot scans.
- `Return gear` is now contextual and only appears for Entity families with editable physical gear staging (Humanoid/Horse).
- Projection Settings replaces the ambiguous Used/Available display with explicit Projection cost, Core capacity and Remaining power.
- Core limits and chassis physical limits are shown separately with exact Scale/Lift/Float, width/height and source-capacity values.
- Clearance failures now report blocked block count and the required projection envelope instead of silently failing.
- Fixes an accidental duplicate Lift PU charge.
- Fixes a duplicate Main Hand row in the Humanoid GUI channel list.
- Network protocol bumped `13 -> 14` for the new persisted Generic pose.
- Added `docs/POWER-POSE-UX-dev29.md`.

## Validation

- Source/static validation performed; Windows `build.bat` remains authoritative before dev.29 is build-clean.

---

# 0.1.0-dev.28 — Held-item Ghost completion + Banner Mode

## Held-item Ghost completion + Banner Mode

- Carries forward dev.27's projection-only `ItemInHandRenderer` normalization so Main/Off Hand swords, tools, blocks and ordinary items use Mirage's colour-only Ghost pass instead of depth-writing chunk layers.
- Adds **Banner** as a first-class source mode while preserving the existing Image/Item/Entity ordinals for save compatibility.
- Adds a dedicated Banner Workspace with virtual, non-consuming banner snapshots; the real banner never leaves the player inventory.
- Plane chassis expose one Front banner source. Mirage Prism exposes independent North/East/South/West sources plus `Same source on all faces`.
- Adds cloth-only world rendering through vanilla `BannerRenderer.renderPatterns`: base dye + loom patterns are preserved while pole/crossbar are omitted.
- Banner Scale preserves the vanilla 20×40 cloth proportion; Lift/Rotation/Floating/Lighting/Tint/Ghost reuse global Projection Settings.
- Prism Banner Power counts only populated faces and clearance/render bounds understand Banner as Prism face geometry.
- Debug Handbook and source-workspace navigation document Banner Mode.
- Network protocol bumped `12 -> 13`.
- Added `docs/BANNER-MODE-dev28.md`.
- Development version bumped to `0.1.0-dev.28`.

## Validation

- Source/resource/static validation performed in the development environment.
- Windows `build.bat` remained authoritative before dev.28 could be considered build-clean.
- Priority QA: held-item/water regression, patterned Banner cloth, Plane/Prism face persistence, Ghost/Tint and 1-vs-4-face Power.

---

# 0.1.0-dev.27

## Held-item Ghost depth repair

- Adds a projection-only `ItemInHandRenderer` mixin so Main Hand / Off Hand contents use Mirage's Ghost buffer contract explicitly.
- Held swords, tools, blocks and ordinary items now normalize raw chunk-style `solid`, `cutout`, `cutoutMipped`, `translucent` and `translucentMovingBlock` layers onto the colour-only Mirage item pass.
- Keeps the existing entity-wide projection buffer for tint/alpha, avoiding a second alpha/tint multiplication when the held-item wrapper is nested.
- The fix is inert outside `ProjectionRenderContext`; normal player/mob item rendering is untouched.
- Network protocol remains `12`: this wave adds no persisted/network state.
- Windows build and in-game water regression QA remain authoritative.

# 0.1.0-dev.26

## Ghost depth / armor repair

- Replaced vanilla `entityTranslucent` as Mirage's Ghost body pass with custom projection-owned RenderTypes that depth-test against the world but write **colour only**, preventing Ghost entities/items from masking water behind them.
- Added a projection-scoped `HumanoidArmorLayer` hook so vanilla humanoid armor uses the same Ghost alpha path as the body instead of remaining opaque.
- Normalized held item/block atlas rendering, shield/banner atlases and armor trim sheets to the Mirage no-depth-write Ghost pass.
- Image/Prism Ghost faces use the same colour-only pass, keeping the Ghost contract consistent across 2D and 3D projections.
- Compatible entity/item translucent layers are normalized as well; glint, eyes, beams, shadows, text, outlines, masks and unknown special/modded RenderTypes remain native/fail-closed.
- Network protocol remains `12`; no persistence or server payload changed.
- Added `docs/GHOST-DEPTH-ARMOR-dev26.md`.
- Development version bumped to `0.1.0-dev.26`.

## Validation

- Source/static validation performed in the development environment.
- Windows `build.bat` and water/armor in-game QA remain authoritative before dev.26 is considered build-clean.

---

# 0.1.0-dev.25

## Build fix

- Fixed the Entity Workspace 3D preview call to `EntityRenderDispatcher#render` for Minecraft 1.21.1 / NeoForge 21.1.244. dev.24 accidentally passed one extra `double` argument, causing `compileJava` to fail.
- No functional Ghost, layout, staging, Horse pose, power, or persistence behavior from dev.24 was changed.
- Development version bumped to `0.1.0-dev.25`.

## Validation

- The fix restores the exact dispatcher argument shape already used by the user-confirmed build-clean dev.22 and by the world Entity renderer.
- Windows `build.bat` remains the authoritative compile/QA check.

---

# 0.1.0-dev.24

## Entity GUI / staging / Ghost repair

- Rebuilt Entity Workspace around a larger centered vanilla-style 9x3 inventory + hotbar footprint; Source, Equipment, Actions/Status, Preview and Inventory no longer share text/button space.
- Reflowed Item Snapshot Workspace so its title and Projection Settings button no longer overlap and its player inventory is centered.
- Incoming is now strictly an actionable queue: accepted snapshots disappear from the left rail, saved duplicates matching Projected are pruned, and Capture Equipped Loadout skips already-projected matches.
- Horse pose is now persisted and selectable (`Idle` / `Rearing`) instead of forcing every Horse projection to rear.
- Replaced Ghost's base-body RenderType guessing with a projection-local `LivingEntityRenderer#getRenderType` hook. This covers normal LivingEntity bodies such as Skeleton consistently without changing normal world entities.
- Held block/item models now use `Sheets.translucentItemSheet()` under Ghost instead of the translucent block sheet, preventing the dev.23 water/depth artefact.
- Common armor/entity cutout layers and shield/banner atlas layers receive projection-local translucent routes; special effect passes keep their native RenderTypes.
- Added `docs/ENTITY-GHOST-LAYOUT-dev24.md`.
- Network protocol bumped `11 -> 12`.
- Development version bumped to `0.1.0-dev.24`.

## Validation

- Source/static validation performed in the development environment.
- Windows `build.bat` / in-game QA are still required before dev.24 is considered build-clean.

---

# 0.1.0-dev.23

## Fixed

- Physical Entity staging items are no longer left inside the projector after acceptance. Successful Apply/Replace returns the real source immediately; the accepted virtual Incoming snapshot is consumed into Projected.
- Closing/leaving the Entity workspace now returns every remaining physical staging stack server-side. Inventory overflow drops at the player instead of remaining trapped in hidden BlockEntity slots.
- Fixed Entity Ghost Effect for common opaque/cutout body/equipment layers by correctly parsing the `Optional[resource]` texture shard and rerouting those layers through translucent entity RenderTypes.
- Horse projections now force the full vanilla rearing animation (`getStandAnim = 1.0`) instead of remaining on four legs.
- Empty source workspaces no longer report phantom Projection Power usage from presentation settings alone.
- Empty Item/Entity modes no longer render the legacy generic book placeholder, keeping the visible world state consistent with 0 PU source usage.
- Removed the duplicate decorative Core item from Projection Settings; the real Core slot remains the single physical/visual source.

## Changed

- Rebuilt Entity Workspace as one centred 520 px composition with dedicated Source, Equipment, Actions/Status, Inventory and integrated tall 3D Preview regions. Menu slots moved with the layout.
- Incoming snapshots now behave as staging, not a second persistent copy: accepting an already-identical Projected snapshot also clears Incoming.
- Projection Power UI now shows a compact non-overlapping Used / Available-remaining summary beside the Core Capacity.
- `Item Snapshot` remains purely virtual: its real source item never leaves the player's inventory, while Entity physical staging gains the explicit close-time recovery path above.
- Network protocol bumped from **10** to **11**.
- Development version bumped to `0.1.0-dev.23`.
- Added `docs/ENTITY-UX-RECOVERY-dev23.md`.

## Validation

- Built from the user-confirmed **dev.22 build-clean Windows baseline**.
- Static validation in the assistant environment: 32 JSON resources parse, 60 Java sources pass delimiter/state checks, and translation keys used by the changed screens are present in en_us/es_es/es_cl.
- Gradle/NeoForge compile could not be run in this environment because the Gradle 9.2.1 distribution download is unavailable here. Windows `build.bat` remains the authoritative compile/QA step for dev.23.
- Priority QA is documented in `docs/ENTITY-UX-RECOVERY-dev23.md`.

# 0.1.0-dev.22

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
- Windows NeoForge `build.bat` was later confirmed build-clean by the user; in-game QA remained ongoing.
- Priority QA: Player/Zombie/Skeleton/bodyless rig, all eight poses, main-arm handedness, armor/trims/dye/glint, tools/shields in both hands, save/reload, multiplayer sync, Ghost/Tint and obstruction preview.

# 0.1.0-dev.21

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
- Windows NeoForge `build.bat` was later confirmed build-clean by the user; in-game QA remained ongoing.
- Priority QA: 1/2/4 faces, mixed aspect ratios, Same Source, save/reload, multiplayer asset sync, rotation, Flip/Scanlines/Tint/Ghost and obstruction envelopes.

# 0.1.0-dev.20

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

# 0.1.0-dev.19

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

# 0.1.0-dev.11

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
- Added a permanent project metadata policy: **do not hardcode repository URLs in mod descriptions**. Repository links belong in separate platform link fields when needed, not in `neoforge.mods.toml`/CurseForge/Modrinth descriptive copy.
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
