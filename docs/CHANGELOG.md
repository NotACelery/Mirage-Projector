# Changelog

## 1.0.69 — Scanner finalization and 1.1.0 release reconciliation

- Holds the active Scanner arm straight forward in first-person and all player render views.
- Adds a one-second quiet cooldown after a successful capture so the duplicate-record message cannot immediately replace capture feedback.
- Reconciles the public 1.1.0 roadmap with tested End Resonance, Mirage Light, Scanner and Scan Codex behavior.

## 1.0.34 — Roadmap & UX/visual cleanup

- Restored the original presentation/Data-show **Mirage Wall Projector** identity and removed the accidental `mirage_wall_illuminator` experiment.
- Separated Table workspace navigation from source activation; only explicit `Use <mode> mode` actions change SourceMode.
- Replaced obsolete portable copied-profile empty-state messaging with `No projection has been configured.`
- Rebuilt Mirage Flashlight item/placed presentation around a coherent Crying-Obsidian/magenta-glass work-flashlight shape; held form is horizontal and Ambient emission/placed presentation point upward.
- Reworked Mirage Light Projector art: Crying Obsidian exterior, Iron internal structure, one-pixel-border front reflector, and four upper lateral Ambient lenses.
- Added `NEXT-WAVES-1.1.0.md` as the recovery-oriented remaining implementation order; visible Light Projector yaw/pitch aiming is intentionally last.
- Network protocol remains 43; ProjectionSettings format remains 4.

## 1.0.32 — Table dedicated runtime hotfix

- Rebuilt Mirage Table Projector client runtime behind a dedicated Table dispatcher/rules class while preserving the existing GUI 1:1.
- Table planar front/back now derives from the exact render quaternion chain instead of approximate viewer/yaw math.
- Table owns X/Y/Z anchor semantics, planar vs volumetric placement and an offset-aware render envelope.
- Removed the legacy shared `applyTableSurfaceOffsets` path from the canonical fixed-projector renderer.
- Main projector screen registration now uses an explicitly typed shared screen factory so NeoForge 1.21.1 can compile the dedicated Table/non-Table routing without generic type-inference ambiguity.
- Network protocol is 43 because main-menu opening data now carries authoritative chassis identity; ProjectionSettings format stays 4.

## 1.0.32 — Survival progression / Flashlight / wall illumination
- Fixed overlay-source cleanup so pre-rename Lantern Java files are tombstoned before compilation when 1.0.32 is copied over a 1.0.31 project directory; removed the fragile `CALL :delete_file` cleanup helper.

- Renamed Mirage Lantern runtime/public identity to Mirage Flashlight while preserving the legacy registry/NBT compatibility surface.
- Added committed Survival recipes for the rechargeable/portable/Shoulder/Charging/Codex/Table/Wall progression set.
- Added the exact five-Glow-Dust Light Battery recipe and common Glow Dust media tag.
- Reworked Flashlight and floor Light Projector visual language around Crying Obsidian, magenta glass and iron.
- Renamed the presentation chassis publicly/code-wise to Mirage Wall Projector while retaining its legacy registry ID.
- Added a separate true Mirage Wall Projector illumination chassis.
- Added temporary placed Flashlight form with battery/mode round-trip.
- All twelve Survival-progression recipes are standard shaped Crafting entries and are explicitly regression-gated for native JEI/EMI discovery; Light Battery exposes both vanilla Glowstone Dust and rechargeable Mirage Glow Dust through `mirage_projector:glow_dust_media`.
- Network protocol remains 42; ProjectionSettings format remains 4.


## 1.0.31 — Canonical workspace/runtime rebuild

- Replaces split client source-selection + workspace navigation with atomic server-authoritative Image/Item/Entity/Banner workspace opening.
- Main and source-workspace menus receive authoritative settings and projection-enabled snapshots on open.
- Table Projector stays on the canonical Mirage Projector / Mirage Display menu-screen path; only its horizontal anchor/render capabilities differ.
- Main Cancel restores the opening configuration and closes the interface.
- Adds padding to the projector header/source-workspace region so Wall controls do not overlap the source section.
- Restores the Scan Codex parchment/book canvas through one explicit render pass while preserving the no-blur/no-dim contract.
- Player scans are identity/skin-only: armor and held equipment are neither captured nor reloaded, including legacy Player scans; Entity Workspace can switch between Base Skin and All Layers.
- Table Placement adds X/Y/Z offsets and Tilt with independent resets; new Tables start with Rotation OFF and Table images rotate/tilt around their geometric center.
- Hand Projector removes Copy Target Projector, caps Compact scale at 10 px, stores a physical Projection Core separately from its rechargeable battery, and requires Core + energy + valid source.
- Portable Device UI is rebuilt around Source Workspaces, Geometry/Scale, Core + Energy and contextual War Banner controls; Image mode imports assets directly through the shared upload transport.
- Adds dedicated portable Scale and Image payloads.
- Network protocol 42; ProjectionSettings format 4.

## 1.0.30 — UX/runtime interaction wave

- Hand Projector: Image/Item/Entity/Banner source modes and virtual source capture.
- Shoulder panel: capture matching mouse release; no outside-drop extraction; remove redundant +3.
- Wall/Data-show: live Scale/X/Y preview; non-closing Apply/Cancel; aligned header.
- Presentation Remote: raw-window center + neutral dead-zone.
- Entity workspace: authoritative mode refresh and bounded Projected/Visibility controls.
- Scan Codex: single-pass book canvas, page-separated library controls, compact Lectern extensions.
- Entity Scan Card: dedicated 16×16 Mirage item texture.
- Network protocol 40; ProjectionSettings format 4.

## 1.0.29 — Runtime QA interaction fixes

- Creative Mirage Equipment appears only on the player inventory tab, with a larger gap from the vanilla frame.
- Mirage Equipment external slots consume their own mouse clicks and synchronize the visible Creative cursor stack instead of being treated as outside-inventory drops.
- Scan Codex renders its physical book/parchment canvas explicitly in handheld and lectern modes while retaining the no-blur/no-dim contract.
- Table projector source buttons now activate Image/Item/Entity/Banner before opening the matching workspace.
- Table installed Core rendering is smaller and centered inside its chamber to prevent clipping into glass/base geometry.
- Wall/Data-show pairing binds the actual held Presentation Remote as well as the docked copy; projector GUI can eject/invalidate the current pairing.
- Wall/Data-show outline and collision geometry now match its low-profile body, lens and top remote dock.
- Network protocol 39; `ProjectionSettings` format remains 4.

## 1.0.28 — Recipe synchronization hotfix

- Fixes world/server join disconnect with `Failed to encode packet clientbound/minecraft:update_recipes`.
- `ScanCardClearingRecipe` is now a canonical singleton shared by its JSON `MapCodec` and network `StreamCodec.unit(...)`.
- Prevents Minecraft 1.21.1 `StreamCodec.unit` equality validation from rejecting the independently decoded recipe instance.
- No gameplay, custom payload, registry-ID or `ProjectionSettings` change; protocol remains 38 and settings format remains 4.

## 1.0.26 — Presentation Deck automation + Data-show remote

## 1.0.27

- Build-stability hotfix over 1.0.26; no gameplay or wire-format change.
- Added missing `Direction` import to the Wall/Data-show renderer.
- Corrected Presentation Remote `ResourceKey` imports to `net.minecraft.resources.ResourceKey` for Minecraft 1.21.1.
- Restored `ProjectionSettings.withBackFaceMode(...)` as the canonical immutable copy helper used by chassis normalization.
- Cumulative pre-build cleanup now removes the obsolete `DuplicatingLecternBlock.java` left by older overlaid snapshots.


- Table and Wall/Data-show now share the persistent ordered nine-image Presentation Deck.
- Added optional Automatic Presentation playback with a 1–120 second interval slider.
- Manual GUI/remote slide changes preserve the automatic timer and do not disable automatic mode.
- Added crafted Presentation Remote plus a physical pairing dock on top of the Wall/Data-show projector.
- Holding RMB with a bound remote opens a lightweight `<-- / - / -->` controller; release commits one previous/next action.
- Pairing uses a persistent projector UUID and validates dimension/loaded target/link identity without force-loading chunks.
- Invalid Wall slides remain navigable and render a transparent red prohibition marker instead of silently disappearing.
- Network protocol 38; `ProjectionSettings` remains format 4.

## 1.0.25 — Table + Wall/Data-show presentation foundation

- Added Mirage Table Projector with horizontal Image/Banner and upright Item/Entity behavior plus packed pickup/support-loss state preservation.
- Added Mirage Wall Projector as a low-profile freestanding Data-show, not a wall-mounted emitter. It requires a complete flat support and faces N/E/S/W.
- Wall supports Image only and targets a real regular wall ahead. Surface validation uses only the actual aspect-correct image footprint after Scale + X/Y offsets; unused nominal-square area is ignored.
- Added automatic Wall scale reduction when the requested image cannot fit the regular wall or current PU budget.
- Added Wall distance PU surcharge (+1 PU per full block beyond the first block for current QA balance).
- Added persistent ordered nine-slide Image playlist with import/replace, clear, reorder, current slide and Previous/Next controls.
- Added signed Wall X/Y placement controls; `ProjectionSettings` format advances to 4.
- Network protocol 37; historical six chassis ordinals remain unchanged.

## 1.0.24 — Scan Codex library/card rework + portable UI lite

- Entity Scan Cards are passive one-snapshot containers; scanning belongs to the Scan Codex.
- Codex browser is scrollable, searchable and categorized, with exact entity preview/equipment/nameplate detail and preserved back-navigation state.
- Storage limit is 25 scans per entity type.
- Lectern duplication writes into a blank Entity Scan Card instead of consuming Paper.
- Filled Entity Scan Cards craft back into blank cards.
- Lectern import toggle is permanent; successful Mirage-card imports consume the source card.
- Optional Easy Mob Farm Mob Capture Cards import through the same panel and are consumed only on success.
- Hand Projector portable GUI is compacted and Banner/War Banner controls update dynamically.
- Network protocol 36; ProjectionSettings format remains 3.

## 1.0.23 — Vanilla Lectern Codex workflow

- Removed the temporary standalone `mirage_projector:duplicating_lectern` block/item introduced by the 1.0.22 foundation.
- Mirage Scan Codices can now be mounted directly on vanilla Lecterns using vanilla Lectern book/state/drop behavior.
- Right-clicking a Lectern-mounted Codex opens an explicit Lectern copy-mode browser with duplicate and Take Codex controls.
- Exact frozen-root duplication still consumes one Paper in Survival and preserves `ScanId`/source provenance/content unchanged.
- Restyled the Scan Codex browser as a two-page book-like interface while preserving the no-blur and non-pausing behavior.
- Advanced network protocol from 34 to 35 for position-aware Lectern Codex payloads.

# Changelog

## 1.0.22 — Duplicating Lectern foundation

- Adds `mirage_projector:duplicating_lectern` as the dedicated Scan Codex physical copy station.
- Using a Codex with a valid selected capture consumes one Paper and produces one physical Entity Scan Card.
- Copies the exact frozen capture root rather than rescanning or rebuilding fields, preserving ScanId/source provenance/entity data/equipment/player profile.
- Keeps favorite state in the Codex library only.
- Station is stateless and introduces no block entity, menu or network protocol change.
- Full-inventory output drops safely; Creative copy operations do not consume Paper.
- Survival recipe and final station art/UI remain intentionally unfrozen.

## 1.0.21 — Runtime QA render/invalidation corrections

- Corrects virtual-light terrain refresh after 1.0.20 made the field visibly render: static updates now invalidate a full neighboring render-section halo, while moving DYNAMIC_VISUAL emitters use boundary-aware pre/post light-byte comparison so only meshes that can sample changed face/edge/corner values are rebuilt. This targets blocks/walls that stayed dark until approached or stayed lit until a block update.
- Corrects Shoulder Device vertical placement by moving the mounted item toward shoulder height instead of mirroring it toward the player feet; shoulder Lantern light origin is aligned with the right-shoulder area while beam direction still follows look direction.
- Fixes Mirage Hand Projector non-War-Banner output. Portable profiles now render through a dedicated hologram-only path that bypasses the fixed projector Core gate after embedded-cell power validation.
- GUI projection toggling now also rejects profiles that exceed portable power instead of entering a visually inert ON state.
- Network protocol remains 34 and `ProjectionSettings` format remains 3. No gameplay IDs or wire formats changed.

## 1.0.20 — Sodium packed-light bridge hotfix

- Fixes a runtime crash while Sodium compiles chunk meshes after the 1.0.19 visual-light bridge.
- `LevelRendererMirageLightMixin` no longer calls `BlockAndTintGetter#getLightEngine()` because Sodium supplies a `LevelSlice` whose light-engine accessor intentionally throws `UnsupportedOperationException`.
- The client-only packed-light bridge now resolves Mirage virtual light from the active `ClientLevel`, while preserving the vanilla packed sky-light channel from the renderer query.
- Network protocol remains 34 and `ProjectionSettings` format remains 3. No gameplay IDs or wire formats changed.

## 1.0.19 — Runtime QA follow-up

- Corrected Mirage Lantern controls to normal RMB mode cycling and Shift+RMB GUI opening; battery service remains GUI-only.
- Corrected placed Mirage Light Projector controls to normal RMB mode cycling and Shift+RMB GUI opening.
- Added the packed-light LevelRenderer bridge required for virtual Mirage light to affect rendered terrain vertices, while preserving vanilla sky light.
- Added explicit item hover tooltips to Portable Device, Mirage Light Projector and Charging Station screens and compacted the Lantern screen to remove excess vertical space/title overlap.
- Enabled the Mirage Equipment panel in Creative inventory in addition to Survival inventory.
- Corrected Charging Station in-world input/output stack lane placement and added Jade active-cell name + charge percentage feedback.
- Scan Codex now overrides vanilla `renderBackground(...)` as a no-op, closing the runtime blur regression while remaining non-pausing.
- Kept the existing floor Mirage Light Projector as an accepted device and documented later visible yaw/pitch aiming, separate wall-light hardware and vanilla-lantern-like handheld ground placement as remaining 1.1.0 work.
- Network protocol remains 34; `ProjectionSettings` remains format 3.

## 1.0.18 — Massive stabilization

- Added real battery/configuration GUIs for Mirage Lantern, Mirage Hand Projector and Mirage Light Projector; legacy quick/opposite-hand battery service is removed.
- Lantern now defaults Off; normal RMB opens GUI and Shift+RMB cycles Off -> Focus -> Flood -> Ambient -> Off. Hand Projector RMB remains ON/OFF while Shift+RMB opens configuration.
- Suppressed battery-percentage held-item re-equip flicker and removed continuous portable-device actionbar refresh.
- Reworked directional-light cone acceptance to intersect voxel volume and moved held/shoulder/placed light origins outside their emitter/player body to address aim-angle/self-occlusion failures.
- Re-architected Mirage Equipment: the player attachment now stores one Shoulder Strap, while the Strap ItemStack owns its Shoulder Device, batteries and upgrades; legacy 1.0.13–1.0.17 layouts migrate automatically.
- Moved Mirage Equipment panel to the right, made empty state Strap-only, hid inactive expansion slots and fixed device swaps so the previous device stays on the cursor.
- Added Shift-hover packed Shoulder Strap contents preview and renamed the public Arm Strap name to Shoulder Strap while preserving the historical registry ID.
- Renamed Battery Pouch Expansion Patch to Shoulder Strap Slot Expansion and shortened upgrade/tool battery tooltips.
- Fully charged custom Glow Dust now returns to vanilla Glowstone Dust; partial/depleted custom dust remains outside vanilla crafting/brewing identity. Added five-dust average-charge inheritance hook for the future Light Battery recipe.
- Tightened Charging Station inputs to incomplete normal media, enlarged/reflowed its GUI and added in-world rendering for queued/charging/output stacks.
- Scan Codex no longer pauses singleplayer or invokes the blurred/dim vanilla background pass.
- Advanced network protocol 33 -> 34; `ProjectionSettings` remains format 3.
- Completed a 1.0.18 integrity/deprecation cleanup: active Java now uses Shoulder Strap terminology while the historical `arm_strap` / `battery_pouch_expansion_patch` registry IDs remain intentionally stable for save compatibility.
- Removed superseded `ShoulderDeviceScreen`, `ShoulderDeviceControlPayload`, the obsolete floating-battery Light Projector renderer, unused `LightProfileMath`, and the dead periodic Hand Projector HUD loop.
- Removed explicit deprecated `EventBusSubscriber.Bus` selectors; client MOD-bus registration now flows through a dist-scoped client mod entrypoint.
- Pruned obsolete direct-cell/debug/legacy-GUI localization and added a permanent integrity gate covering payload registration, mixin resolution, legacy-ID preservation and generated-file hygiene.

## 1.0.17 — Mirage Scan Codex foundation

- Added `mirage_projector:scan_codex`, a non-stackable physical key to a persistent server-side entity-scan library.
- Shift + right-clicking a living entity/player with the Codex stores a new independent frozen `EntityScanData` snapshot; repeated captures of the same species are never collapsed into a species unlock.
- The Codex ItemStack stores only its stable library UUID plus the currently selected scan UUID. Full entity snapshots live in Overworld `SavedData`, avoiding multi-megabyte inventory ItemStack synchronization as libraries grow.
- Added a metadata-only Codex browser with name/type search, All/Favorites/Players/Humanoids/Horses/Other filters, favorites, paging and exact capture selection.
- Browser summaries expose frozen name/type/category/nameplate/equipment-count metadata without sending the full entity snapshot to the client.
- Selection is server-authoritative and persists on the physical Codex item, providing the exact scan identity required by the future Duplicating Lectern.
- Added server-side lookup/copy APIs for exact stored scan roots; no physical Entity Scan Card duplication is implemented yet.
- Added two play payloads for Codex browsing/actions; network protocol advances 32→33. `ProjectionSettings` remains format 3.

## 1.0.16 — War Banner Presentation

- Added handheld Banner presentation state: `Forward Projection` or overhead `War Banner`.
- War Banner renders only the holographic banner cloth above the owning player; no physical pole/projector is required for an inventory-resident active device.
- Added `Directional` facing tied to player body yaw and `Always Face Viewer` horizontal billboard facing. Billboard remains upright and ignores camera pitch.
- Added portable-only War Banner size (45–80% of Forward Projection, 65% default) and height offset (0–12 px, +4 px default) controls through the mounted Shoulder Device screen.
- War Banner state persists on the Hand Projector ItemStack and rides the existing owner UUID + device UUID portable-state publication, so it continues rendering after the projector leaves the hand or Shoulder Slot.
- Network protocol advances from 31 to 32 because the existing Shoulder Device control payload gains new action ordinals. `ProjectionSettings` remains format 3.

## 1.0.15 — Dedicated Charging Station

- added a horizontal, glass-bodied Beacon Charging Station with a clearly marked player-facing output;
- added 4-slot rechargeable input queue, 1 single-cell active charger and 4 ordered outputs;
- completed cells move left-to-right and remain in the active slot when every output is blocked;
- added side-aware NeoForge item-handler exposure: five input faces and one output-only face;
- added hopper-like automatic front-face ejection at one item per eight ticks;
- generalized Beacon charging attenuation through `BeaconRechargeableCharger` so Core Booster and Charging Station share the same optics contract;
- kept Core Booster material relay behavior separate and unchanged;
- no custom network payload was added, so protocol remains 31.

## 1.0.14 — Shoulder Battery Pouch / upgrade foundation

- Expanded Mirage Equipment into a purpose-built Arm Strap battery-management system without consuming vanilla armor/offhand slots.
- Added 6 base Battery Pouch slots accepting only `RechargeableEnergyItem` stacks.
- Added two base generic shoulder-upgrade sockets with one-upgrade-per-family validation.
- Added `Auto Battery Swap Patch`: a depleted mounted-device cell is exchanged for a charged pouch cell only when the old cell can be returned safely.
- Added `Battery Pouch Expansion Patch`: expands pouch capacity 6→9 and unlocks a third generic upgrade socket.
- Blocked Expansion removal while expansion-only slots are occupied and blocked Arm Strap removal while any dependent device/battery/upgrade remains.
- Added owner-only Battery Pouch/upgrade state synchronization and an expanded leather-style Mirage Equipment inventory panel.
- Reserved the generic third upgrade socket for future families without implementing any 1.2 UV logic early.
- Advanced network protocol 30→31; `ProjectionSettings` remains format 3.
- Upgrade recipes remain intentionally unfrozen pending final 1.1 progression decisions.

## 1.0.13 — Mirage Equipment / Shoulder Slot foundation

- Added `mirage_projector:arm_strap` and a Mirage-owned two-slot player equipment attachment.
- Added a collapsible Arm Strap + Shoulder Slot extension beside the vanilla inventory without consuming armor/offhand slots or altering F swap-hands behavior.
- Shoulder Slot initially accepts Mirage Lantern and Mirage Hand Projector through the extensible `ShoulderMountableDevice` contract.
- Added server-authoritative cursor/slot insertion, extraction and swapping; Arm Strap removal is blocked while a Shoulder Device remains mounted.
- Added mounted-device ticking so Lantern light/drain and Hand Projector projection/drain continue with both vanilla hands free.
- Added compact mounted-device controls opened from the Shoulder Slot.
- Added synchronized shoulder-equipment state plus initial physical right-shoulder/upper-arm device rendering.
- Added right-shoulder reservation against vanilla shoulder riders while Mirage equipment occupies that side; the left shoulder remains available.
- Added save/relog and death/keepInventory handling for the real Arm Strap/device ItemStacks.
- Froze the fuller Shoulder Equipment and future bird/Parrot device-skin design in the 1.1.0 waitlist.
- Added three shoulder-equipment play payloads; network protocol advances to 30 while `ProjectionSettings` remains format 3.

## 1.0.12 — Persistent portable projector state / Creative Battery

- Added stable ItemStack-owned UUID identity for Mirage Hand Projectors.
- Active Hand Projectors now remain ON and continue normal battery drain while stored in the player's inventory instead of implicitly shutting down when no longer held.
- Added `PortableProjectorStatePayload` so other clients can see active inventory-stored portable projectors without access to the owner's arbitrary inventory slots.
- Kept player position/orientation on vanilla entity tracking; Mirage synchronizes device state rather than movement every tick.
- Added client portable-state cache with stale cleanup for dropped/deleted/lost active devices.
- Added `mirage_projector:creative_battery`, an infinite Creative/debug/admin rechargeable medium using the same `RechargeableEnergyItem` contract as normal cells.
- Creative Battery has no Survival recipe or loot path and is intended for QA and temporary minigame/PvP loadouts.
- Froze the final War Banner design in the 1.1.0 waitlist: pole-less smaller overhead hologram, Directional and Always-Face-Viewer/Billboard modes, portable Size/Height bounds and inventory-active multiplayer behavior.
- Added the first post-28 custom play payload; network protocol advances to 29 while `ProjectionSettings` remains format 3.

## 1.0.11 — Handheld Mirage Projector / portable copied-profile holograms

- Added `mirage_projector:mirage_hand_projector`, the first handheld hologram projector item.
- Added copied-profile portable projection storage: the item now stores one compact normalized snapshot of a placed Mirage Projector active source profile directly inside the ItemStack.
- Added sneak + right-click copy from placed Mirage Projectors. Copied portable profiles keep the active source family while normalizing to compact handheld limits.
- Added handheld ON/OFF behavior that toggles the hologram without deleting the stored profile.
- Added exact removable rechargeable-cell servicing through the opposite hand, reusing the same embedded-cell contract as the lantern.
- Added server-side held-only drain for active portable holograms using Projection Power-derived QA costs.
- Added client reconstruction/rendering of handheld portable holograms from vanilla tracked player transform + held ItemStack state, including reuse of the existing deferred entity-projection pass.
- Added portable-profile normalization rules: single-source image layout, reduced presentation ceilings and a 90% opacity cap so the handheld projector always remains at least slightly ghostly.
- Added local handheld-projector actionbar/HUD feedback, multilingual strings, Creative exposure and a 16x16 item sprite.
- No custom network payload was introduced; protocol remains 28 and ProjectionSettings remains format 3.

## 1.0.10 — Handheld Mirage Lantern / player-following DYNAMIC_VISUAL

- Added `mirage_projector:mirage_lantern`, the first handheld/player-following Mirage Light consumer.
- Lanterns store one exact rechargeable ItemStack internally, preserving Glow Dust/Light Battery type, partial charge and components through save/reload and extraction.
- Added opposite-hand battery servicing: sneak + right-click inserts a rechargeable cell from the other hand when empty or extracts the installed cell into an empty other hand.
- Normal right-click cycles the shared `Focus -> Flood -> Ambient -> Off -> Focus` sequence while powered.
- Added server-side held-only battery drain using the centralized 4/2/1/0 QA values; lanterns elsewhere in inventory do not drain.
- Added stable per-player-hand moving-light source identities and client reconstruction from vanilla tracked player position/look + held ItemStack state.
- Added persistent local hotbar/actionbar feedback with device name, mode and battery percentage; depleted/empty state reads `Discharged` / `Sin Cargar`.
- Added a pixel-perfect 16x16 lantern item texture, charge bar, tooltips, registry/Creative exposure and multilingual strings.
- No dedicated movement-light packet was added; protocol remains 28 and ProjectionSettings remains format 3.
- Survival recipe and final portable-light balance remain intentionally unresolved.

## 1.0.9 — Physical light projector / first DYNAMIC_VISUAL gameplay consumer

- Added `mirage_projector:mirage_light_projector`, a horizontally oriented reflector-style block with one real rechargeable-energy slot.
- Glow Dust and Light Battery insert through the shared `RechargeableEnergyItem` contract; Shift + right-click returns the exact cell through inventory merge semantics with safe overflow dropping.
- Added the exact mode cycle `Focus -> Flood -> Ambient -> Off -> Focus`, persisted and synchronized per block, with Focus as the default.
- Added server-side charge drain while emitting; current QA values are Focus 4/s, Flood 2/s, Ambient 1/s and Off 0/s.
- Connected placed synchronized emitters to the client-local `DYNAMIC_VISUAL` runtime using stable block source IDs.
- Focus/Flood now exercise directional-cone light solving in real gameplay; Ambient uses an omnidirectional local profile; depleted cells suspend emission without deleting the selected mode.
- Added a physical renderer for the inserted rechargeable medium.
- Added exact mode actionbar strings plus Jade mode/battery feedback.
- Kept profile and drain numbers centralized as tunable QA balance instead of baking them into the light engine.
- Added block/item/model/blockstate/loot/Creative exposure for testing; survival recipe remains intentionally unresolved.
- Protocol remains 28; ProjectionSettings remains format 3.

## 1.0.8 — Rechargeable energy polish / Light Battery foundation

- Generalized the 1.0.7 charging path through `RechargeableEnergyItem` while preserving existing Glow Dust charge data.
- Added Light Battery with 4000-unit capacity, a dedicated frame/charge-layer item sprite and charge-driven tint/bar/tooltip state.
- Set the current Light Battery Core-Booster charge rate to 8 units per 10 ticks: roughly 250 seconds from empty, about five times Glow Dust's 50-second baseline.
- Core Booster charging cradles now accept supported rechargeable media instead of only Glow Dust.
- Fixed sneak-right-click charging-item extraction to use normal inventory insertion, allowing exact item/component matches to merge instead of forcing the returned stack into the interaction hand.
- Reduced/recentered the charging-medium render so Glow Dust/Light Battery remain inside the Booster glass instead of colliding with the shell.
- Added Jade charging-state feedback with inserted medium + percentage or explicit empty state.
- Kept Beacon attenuation at 0.20 absolute transmission per actively charging cell and preserved the five-cell clear-column ceiling.
- Documented the future dedicated Charging Station queue (4 inputs, 1 active slot, 4–5 outputs) and the unresolved Light Battery recipe target (~5 Glow Dust plus additional materials).
- Did not guess projection PU, Lift efficiency or projection-light values that were not frozen in the recovered design state.
- Protocol remains 28 and ProjectionSettings remains format 3.

## 1.0.7 — Rechargeable Glow Dust foundation

- Added Glow Dust as a charge-bearing item with persistent partial charge, depleted naming, tooltip percentage/status and an inventory charge bar.
- Reused the vanilla Glowstone Dust silhouette with a charge-driven tint so depleted dust becomes visibly dull without requiring a second item ID.
- Added a dedicated Glow Dust charging cradle to Core Boosters without replacing their existing Core material socket.
- An active Beacon beam recharges one inserted Glow Dust per Core Booster; a clear vertical path supports at most five simultaneous charging cells.
- Each actively charging Glow Dust removes 20 percentage points from the outgoing Beacon beam, and the custom Beacon renderer shows the attenuation.
- Crying Obsidian optics now see the attenuated beam, so charging cells and crystal transmission compose instead of behaving as unrelated systems.
- Glow Dust charge state persists through save/reload, extraction and block-entity synchronization.
- No survival recipe or lantern consumer is committed yet; Glow Dust is currently exposed for Creative/QA while the 1.1 portable-device progression is still being built.
- Protocol remains 28 and ProjectionSettings remains format 3.

## 1.0.6 — Prism carousel compaction and four-tab UI

- Capped user-controlled Prism Distance at +160 px (10 blocks) beyond the collision-safe baseline.
- Reworked Prism collision spacing per adjacent face pair instead of using the single largest face for the whole carousel.
- At +0 px and outward Tilt, adjacent lower face edges can meet without overlapping, producing a compact carousel instead of mostly empty radial space.
- Inward Tilt increases only the collision-safe minimum actually required; unsupported inward angles are refused if they would need more than the +160 px spacing budget.
- Kept Prism Rotation as a true carousel around the machine center and retained the small Prism Distance PU surcharge.
- Merged Appearance controls into Geometry, reducing the fixed settings area to four tabs: Geometry, Placement, Rotation and Floating.
- Refined Prism clearance/culling reach so inward and outward Tilt are no longer treated as the same radial expansion.
- Preserved the 1.0.5 Dynamic Mirage Light foundation; protocol remains 28 and ProjectionSettings remains format 3.

## 1.0.5 — Dynamic Mirage Light foundation

- Activated the client-side `DYNAMIC_VISUAL` runtime for future moving Mirage light emitters.
- Added moving-source snapshots with update cadence, camera culling and stale-source cleanup.
- Added directional-cone solving for future Focus/Flood-style portable lighting.
- Kept dynamic fields completely separate from the server-authoritative STATIC_WORLD publication channel.
- Added reusable dynamic profile factories and stable entity/device source identities.
- Added automatic retry for dynamic solves clipped by temporarily unavailable chunks.
- No lantern, Glow Dust battery or portable projector gameplay is exposed yet.
- Protocol remains 28 and ProjectionSettings remains format 3.

## 1.0.4 — Fixed-tab UI and placement polish

- Consolidated vertical placement into Lift; the experimental signed Vertical Offset control is removed.
- Reworked Mirage Prism Distance so +0 px means the tight collision-safe four-face baseline, with Tilt increasing the minimum only when required.
- Removed the extra one-pixel Prism spacing margin so adjacent face boundaries may meet without overlapping.
- Reorganized Geometry, Placement, Rotation, Floating and Appearance into a fixed settings-tab area.
- Reduced the main projector panel height so 1920×1080 at GUI Scale 2 no longer enters responsive overflow.
- Kept Prism carousel rotation and the small radial-distance PU cost.
- Protocol remains 28 and ProjectionSettings remains format 3; positive legacy Vertical Offset values are absorbed into Lift and the legacy slot is sanitized to zero.

## 1.0.3 — Renderer compile hotfix

- Fixed a renderer compile regression introduced during the 1.0.2 Prism-placement pass.
- Restored the missing projection base-height local in multi-source image layouts.
- Removed a duplicate projection base-height declaration in the single-image renderer.
- No gameplay, save-format, placement, PU or protocol behavior changed from 1.0.2.

## 1.0.2 — Prism placement correction

- Removed independent Horizontal Offset from projector controls.
- Restricted Distance Offset to Mirage Prism Image/Banner projection, where it expands the four-face cross without scaling content.
- Added collision-safe automatic minimum Prism spacing based on projected face size and Tilt.
- Extended Tilt to the full ±90° range.
- Preserved Prism Rotation as a carousel around the projector center instead of rotating each face around its own displaced center.
- Added a light PU cost for Prism radial separation; required base collision spacing remains free while extra/manual or Tilt-driven separation costs modest additional power.
- Kept `ProjectionSettings` format 3 / protocol 28; the legacy horizontal-offset wire/NBT slot is retained but always sanitizes to zero.

## 1.0.1 — Advanced projection placement

- Introduced the first advanced-placement foundation with Vertical/Distance storage and quaternion-backed Tilt.
- Added initial Tilt support for high-mounted displays; its final range/collision behavior is refined in 1.0.2.
- Added Reset Position and Reset Tilt controls.
- Added placement-aware rendering, clearance and culling groundwork across existing projection sources.
- Updated projection clearance, front/back image selection and render culling to follow displaced/tilted projections.
- Bumped `ProjectionSettings` network format to 3 and protocol to 28; 1.0.0 saves default cleanly to zero offsets and identity tilt.
- Expanded the main projector panel while retaining the responsive scrollbar behavior introduced before the initial release.

## 1.0.0 — Stable fixed-projector release

First stable Mirage Projector release for Minecraft 1.21.1 / NeoForge 21.1.244+.

### Projectors

- Six canonical chassis: Mirage Projector, Mirage Display, Mirage Field Projector, Wide Mirage Projector, Tall Mirage Projector and Mirage Prism.
- State-preserving projector upgrade crafting.
- Final canonical models, VoxelShapes, inventory/held/dropped presentation and fixed projector ON/OFF UX.
- Active source and workspace navigation are independent; `TURN OFF` preserves configured state.

### Projection sources

- Image/GIF projection with server/client content-addressed asset synchronization.
- Item virtual snapshots.
- Banner virtual snapshots and Prism cardinal faces.
- Frozen Entity Scan Cards with Player/generic living/Humanoid/Horse equipment support, pose presets, nameplates and per-channel visibility.

### Presentation / power

- Scale, Lift, rotation, Float, tint, Ghost/opacity, Fullbright/world lighting and source-specific visual controls.
- Projection Cores with PU-based capacity and dynamic feasible slider limits.
- Overdrive behavior when sufficient PU is available.
- Core Booster with Glass/Quartz/Amethyst/Diamond/Netherite loaded states and ×1.50 Core amplification.

### Crying Obsidian / Mirage Light

- Crying Obsidian Shards and shard-based Crying Obsidian crafting.
- Renewable Small → Medium → Large → Cluster growth with lava above Crying Obsidian.
- Silk Touch stage preservation; fixed shard drops without Fortune multiplication.
- Material-specific Core Booster Beacon identities.
- Server-authoritative virtual Mirage Light Engine for energized Mature Clusters.
- Exact open-space half-decay, vanilla shape/opacity occlusion, obstacle-detour decay, overlap-by-maximum aggregation and revisioned chunk synchronization.

### Compatibility / extensibility

- Stable namespaced projection-source IDs and versioned settings serialization.
- Source content separated from `ProjectionTransform` presentation state.
- Source/content and client render provider registries.
- Quaternion-ready persisted orientation.
- Generic `ProjectionEnergySource` boundary for future non-Core devices.
- Legacy source-ID, Improved Core and physical Mirage-light migration paths retained where needed.

### Recipe viewers / packaging

- Optional EMI and JEI integrations.
- EMI crafting coverage for all projector upgrades and Crying Obsidian recipes.
- EMI icon-only Crying Obsidian World Interaction tutorial and age-ordered Block Drops.
- JEI projector crafting extension and ingredient guidance.
- Final mod logo/metadata integration.
- Public handbook/documentation cleanup and release audit.
- Responsive vertical scrolling for projector GUIs on constrained GUI heights.

Development-version chronology is preserved in `docs/history/development-notes/CHANGELOG-DEVELOPMENT.md`.

- Fixed Mirage Table Projector image visibility across rotation angles by using the correct horizontal-plane front normal; zero-tilt table rotation no longer flips front/back classification.
## 1.0.68

- Restricted Scanner interaction priority to living entities so the Scanner can be placed in item frames.

## 1.0.67

- Added Shift+click Codex insertion and extraction to the Scanner configuration menu.

## 1.0.66

- Removed redundant equipment labels and empty-state text from Codex scan details; populated equipment is shown only in slots.
- Extended UUID duplicate protection to same-equipment horses and other equipment-capable entities, while permitting captures when their equipment changed.

## 1.0.65

- Locked the Scanner's originating hotbar slot while its configuration menu is open, including pickup, drop, drag, and hotbar-number swaps.

## 1.0.64

- Prevented duplicate UUID scans for already-recorded non-player, non-equipment entities, while retaining player and equipment-capable rescans.
- Replaced the scanner HUD fill with magenta stained glass for a stable, readable progress surface.

## 1.0.63

- Made the Entity Scanner main-hand only and gave its scan interaction priority over all living-entity interactions.

## 1.0.62

- Scanner progress HUD is now additionally gated by the local held-RMB scanner state, preventing stale progress from remaining on-screen and keeping paused snapshots out of Jade.

## 1.0.61

- Reworked the Entity Scanner into a 1.5-second, four-block, server-authoritative sustained scan.
- Target loss or release pauses the current target progress; changing targets, target death, scan completion, and logout clear it.
- Active scanning halves movement speed, keeps the scanning arm neutral for all player render views, and shows a crying-obsidian 0–100% HUD meter.

## 1.0.57

- Added the handheld Mirage Entity Scanner with an inserted-Codex workflow for direct entity scans, including underwater use.
- Fish previews compensate for vanilla's out-of-water rotation so they render as swimming.
- New Codex captures display recorded health, movement speed, horse jump, llama storage/decoration, and stored variants when applicable.
