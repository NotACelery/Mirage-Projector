# WAITLIST — 1.1.0 Portable Illumination, Capture & Projection Expansion

1.1.0 is intentionally a large expansion. It grows Mirage from fixed projectors into a portable illumination/capture/projection ecosystem.

## 1.1.0 delivery map after the 1.0.19 QA follow-up

### Already delivered as 1.0.x foundations

- fixed-projector placement/Tilt/Prism Distance and source-registry foundations;
- client-local `DYNAMIC_VISUAL` runtime and directional-cone solver;
- Glow Dust + Light Battery rechargeable-item contract and Core Booster/Beacon recharge path;
- placed Mirage Light Projector;
- handheld Mirage Lantern;
- handheld Mirage Projector with portable copied profiles;
- persistent active portable projectors while stored in normal inventory;
- multiplayer portable-projector state publication and stable device UUIDs;
- Creative Battery for Creative/debug/admin/temporary game modes;
- Mirage Equipment player attachment;
- Shoulder Strap + Shoulder Slot foundation, mounted-device ticking/sync, compact mounted-device controls and right-shoulder reservation.

### Remaining 1.1.0 blockers / feature work

- final Focus/Flood/Ambient balance, geometry invalidation and simultaneous-emitter performance QA;
- final Glow Dust and Light Battery recipes/progression;
- **delivered in 1.0.14 foundation:** Shoulder Strap Battery Pouch + generic shoulder-upgrade sockets;
- **delivered in 1.0.14 foundation:** Auto Battery Swap Patch + Shoulder Strap Slot Expansion; final recipes/UI polish remain;
- final Mirage Equipment panel layout and physical shoulder-device positioning;
- **delivered in 1.0.16 foundation:** War Banner overhead presentation (`Directional` + `Always Face Viewer`); final in-game positioning/polish QA remains;
- optional shoulder-device cosmetic skin support / first bird disguise;
- **delivered in 1.0.17 foundation:** Mirage Scan Codex persistent library, search/filters/favorites and exact capture selection; final recipe/art/QA remain;
- **delivered in 1.0.18 stabilization:** device GUIs, packed Shoulder Strap inventory/migration, dynamic-light angle fix, Charging Station input/UI/render polish, Glow Dust full-charge normalization and non-pausing Codex UX;
- **delivered in 1.0.19 QA follow-up:** corrected Lantern/Light Projector gestures, device/station hover tooltips, compact Lantern GUI, Creative Mirage Equipment visibility, Charging Station lane/Jade progress, explicit Codex no-blur override and the final packed-light visual bridge for DYNAMIC_VISUAL terrain rendering;
- **delivered/corrected in 1.0.23:** physical scan-copy workflow through a vanilla Lectern hosting the Codex; no separate Mirage station remains;
- horizontal/table projector and wall/data-show projector families;
- final source/chassis capability enumeration and portable/wall/table anchor semantics;
- Dragon Egg / End Resonance Field+Prism behavior;
- final recipes, documentation, multiplayer/performance QA and release polish.

### Explicitly deferred to 1.2.0

Do not implement UV-specific behavior as part of 1.1.0. UV Shoulder Light, Auto UV, UV-vulnerability combat logic, UV Marks/authoring and Crying Obsidian UV reveal behavior now belong to `WAITLIST-1.2.0.md`. Direct grab/free-rotate hologram interaction also remains 1.2.0 scope.


## Pre-1.1 groundwork already delivered in 1.0.x

The fixed-projector placement foundation is no longer a 1.1.0 task. The 1.0.x line provides Lift as the single vertical axis, full ±90° quaternion-backed Tilt, reset controls, and clearance/culling support. Mirage Prism additionally owns collision-safe radial Distance for Image/Banner faces; +0 px is the tight safe carousel baseline and larger values expand the cross without changing content scale. This is a Prism chassis capability, not a universal translation contract. Every new 1.1.0 chassis should explicitly declare which placement capabilities it supports.

Remaining placement architecture for 1.1.0:

- freeze a formal **anchor/pivot semantic** for portable, wall, table and ceiling chassis;
- decide whether the table/horizontal chassis also covers ceiling mounting or whether a dedicated ceiling projector is required;
- make source selection UI enumerate compatible registered sources/capabilities instead of permanently hard-coding the four 1.0 built-ins;
- define the optional Blueprint/Create bridge capability contract explicitly: Table/Horizontal is the preferred 3D host, Wall may expose it as a presentation source where appropriate, and Handheld must reject it.

## A. Dynamic/Mobile Mirage Light Engine

Foundation delivered in **1.0.5**:

- operational client-only `DYNAMIC_VISUAL` fields that never publish into authoritative `STATIC_WORLD`;
- moving-source snapshots with stable identity, position, profile, solve cadence, camera culling and stale cleanup;
- UUID/key source identities suitable for future player/entity/device attachments;
- shared voxel-solver support for `DIRECTIONAL_CONE`;
- generic ambient/directional dynamic profile factories;
- render-section invalidation and retry of fields clipped by temporarily unavailable chunks.

Still required before 1.1.0:

- additional moving-device consumers/polish only where required by the remaining 1.1 feature set; the first placed and handheld consumers are already delivered;
- final Focus/Flood/Ambient profile balance and shape tuning;
- broader moving-device visibility/performance QA remains; lantern and portable-projector synchronization foundations are already delivered;
- geometry invalidation for nearby block changes while a stationary dynamic emitter remains active;
- performance QA under many simultaneous moving emitters remains.

## B. Rechargeable energy / Light Battery ecosystem

Foundation delivered through **1.0.8**:

- Glow Dust stores persistent partial charge and can be discharged/recharged without consuming the material.
- Glow Dust remains the 1000-unit baseline cell; default/full stacks are compatible with the 1.0.7 format.
- `RechargeableEnergyItem` generalizes item-owned charge so chargers do not hard-code one battery type.
- Light Battery is registered as a 4000-unit rechargeable medium: four times Glow Dust capacity.
- at the current 10-tick Core Booster pulse, Glow Dust gains 10 units and Light Battery gains 8, so an empty Light Battery takes about 250 seconds to refill versus about 50 seconds for Glow Dust — roughly five times longer.
- Core Boosters accept either rechargeable medium in the separate charging cradle while retaining the normal Core socket.
- charging-media extraction preserves the exact stack/components, merges through normal inventory insertion when possible, and drops safely only when the inventory cannot accept it.
- the charging render is compact/centered inside the Booster glass and Jade reports medium + percentage.
- an active Beacon column recharges the inserted medium; each actively charging cell subtracts 20 percentage points from outgoing beam transmission, with five active cells as the clear-column ceiling.
- Crying Obsidian optics and the custom Beacon renderer consume the same attenuated transmission.

1.0.9 now also consumes these rechargeable media in the placed Mirage Light Projector. Focus/Flood/Ambient/Off mode state is persistent/synchronized and server-side drain is functional with centralized QA balance values.

Still required before 1.1.0:

- final Glow Dust progression/recipe.
- final **Light Battery recipe**. The recovered design target is approximately **5 Glow Dust plus additional casing/electrical materials**; the other ingredients were not frozen, so 1.0.8 intentionally does not invent them.
- handheld discharge policy is implemented using shared 4/2/1/0 QA drain values; final charge-rate/profile balance remains open.
- portable hologram projection already consumes rechargeable cells through `ProjectionEnergySource`; final cell-to-PU/drain balance remains open. Fixed projector Cores remain a separate PU system and must not be silently replaced by battery semantics.
- decide whether any battery-backed hologram itself contributes Mirage illumination and freeze intensity/profile/synchronization semantics before adding that optional effect.

### Dedicated Charging Station — implemented in 1.0.15

The dedicated Charging Station removes the need to babysit one Core Booster charging cradle at a time. Its queue/output UX is intentionally inspired by the processing flow of Easy Farmer's Delight cutters, without coupling Mirage to that mod. It uses the same Beacon-powered `RechargeableEnergyItem` charging contract and the same active-cell beam attenuation as Core Booster charging. The existing material-relay Core Booster remains a separate system.

Frozen 1.0.15 contract:

- **4 input queue slots** accept only rechargeable Mirage energy media; automation may insert from every face except the machine output face.
- exactly **1 active charging slot**; it is player-accessible and hard-limited to one physical Glow Dust / battery at a time even when the queued source stack is larger.
- exactly **4 output slots**, ordered left-to-right. A completed cell moves to the leftmost output that can accept it.
- if every output is blocked, the 100% cell remains in the active charging slot and the queue stops advancing; items are never deleted or silently displaced.
- the block is horizontally directional. On placement its clearly marked output/front faces the placing player, like a furnace.
- the output face rejects automated insertion and exposes completed outputs for extraction. The other five faces expose the input queue for insertion and reject extraction.
- the station actively pushes **one completed item every eight ticks** through its output face into any adjacent inventory exposing NeoForge's standard item-handler capability (vanilla hoppers/chests/droppers/dispensers and compatible mod logistics).
- this side-aware capability contract is the compatibility surface for Create chutes, filtered hoppers and other logistics mods; Mirage does not hard-code individual transport mods.
- station charging consumes the same 20-percentage-point Beacon transmission while an incomplete cell is actively charging. Full/blocked cells no longer consume charging transmission.
- the station preserves exact ItemStack charge/components and uses `RechargeableEnergyItem`, so Glow Dust, Light Battery, Creative Battery and future compatible media share one path.

Still open before final 1.1.0 progression freeze: final Survival recipe and final block-model art pass. The first 1.0.15 model deliberately prioritizes readable glass body + unmistakable directional output for QA.

## C. Lantern family / modes

Portable hand-held lights toggle with right click and expose mode cycling without requiring placement.

Planned modes:

- **Focus** — long, concentrated forward beam; high central intensity, strong lateral falloff, higher battery draw.
- **Flood** — broader lower-intensity illumination; shorter/softer range and reasonable decay.
- **Ambient** — player-following torch-like field with enhanced useful reach; profile may repeat high levels less aggressively than Mature while remaining omnidirectional/local.
- **Off** — no light and no drain.

The placed Mirage Light Projector exercises these four modes in 1.0.9 and the handheld Mirage Lantern reuses them in 1.0.10. Current values live centrally in `PortableLightMode` as QA balance values (Focus 4/s, Flood 2/s, Ambient 1/s, Off 0/s) rather than hard-coded engine assumptions. Final tuning remains open.

## D. Mirage Scan Codex

Foundation delivered in **1.0.17**, reworked in **1.0.24**:

- `mirage_projector:scan_codex` is a physical, non-stackable Codex item. Shift + right-click scans a living entity/player; right-click in air opens the browser.
- each scan reuses the canonical `EntityScanData` frozen snapshot and receives its own scan UUID, so multiple independent captures of the same species/type remain distinct.
- the physical ItemStack stores only a stable Codex UUID and selected scan UUID; full snapshots live in Overworld `SavedData` so large libraries do not bloat ordinary inventory synchronization.
- browser sync is metadata-first and includes name/type, Player-vs-mob/category, frozen nameplate, equipment count and favorite state; the selected entry additionally receives its exact root for preview.
- the browser is scrollable and combines global text search with All/Favorites/Hostile/Passive/Farm/Nether/End/Water/Players/Other category tabs.
- opening an entry shows the frozen entity appearance/equipment/nameplate; Back preserves category, query and scroll position.
- storage is capped at 25 distinct captures per entity type, not 25 globally; deleting an entry frees that type's capacity.
- selected capture identity persists on the Codex and is the handoff point for vanilla-Lectern physical duplication.
- Entity Scan Cards are passive transport/projector containers; scanning belongs to the Codex.
- the library is not a binary "species unlocked" Pokédex.

Still required before final 1.1.0:

- final Survival recipe and dedicated Codex art/polish;
- in-game scale/performance QA with large libraries.

## E. Vanilla Lectern / Scan Codex physical-copy workflow

Flow in 1.0.24:

```text
Entity -> Scan Codex entry
Codex -> vanilla Lectern
Selected Codex entry + blank Entity Scan Card -> filled Entity Scan Card
Filled Mirage/compatible card -> Import extension -> Codex entry (source card consumed on success)
```

- selecting one stored capture chooses exactly which frozen snapshot is copied;
- duplication requires one blank Mirage Entity Scan Card in the detail-page extension; a filled card blocks the action;
- duplication fills that same card and preserves the Codex entry, so repeated copies still do not require finding/scanning the original entity again;
- no Paper cost remains in the current workflow;
- the permanent Import toggle appears in the Lectern library even without compatibility mods and accepts filled Mirage Entity Scan Cards shared by other players;
- a successfully imported source card is consumed as the price of adding the capture to the library; failed validation/import never consumes it;
- when Easy Mob Farm is installed, the same extension optionally accepts `easy_mob_farm:mob_capture_card` under the same consume-on-success rule;
- a filled Mirage Entity Scan Card placed by itself in either crafting grid clears back into a blank card;
- the existing physical Entity Scan Card remains the projector-facing interoperability format;
- no separate Mirage copy-station block exists after the 1.0.23 runtime-QA correction;
- the vanilla Lectern physically owns the mounted Codex ItemStack and retains vanilla occupied/drop behavior;
- right-clicking a Lectern-mounted Codex opens the position-bound library/copy/import browser instead of vanilla LecternScreen;
- Take Codex returns the same physical Codex with its library UUID and selected scan identity intact;
- full frozen snapshot data remains server-authoritative in `ScanCodexSavedData`;
- favorites remain Codex-library metadata and are not copied onto projector-facing cards.

Possible later interoperability, deliberately **not** implemented in 1.0.24: export a Mirage Codex capture into an Easy Mob Farm blank capture card. If accepted later, cloning cost is paid in **experience levels**, never raw XP points, and should scale with entity/card rarity plus meaningful equipment/enchantments so rare farm cards cannot be duplicated freely.

## F0. Physical anchor + presentation chassis foundation — delivered in 1.0.25 / presentation controls in 1.0.26

- Mirage Table Projector: supported tabletop/floor anchor, horizontal Image/Banner plane, upright Item/Entity volume, packed Shift+empty-hand pickup.
- Mirage Wall Projector is a low-profile **Data-show**, not a wall-mounted block: it stands on a complete flat support, faces N/E/S/W and projects Image content onto a real regular wall ahead.
- Wall validates only the real aspect-correct image footprint after Scale + X/Y offsets. Irregular unused space outside that footprint does not matter; irregular/obstructed cells touched by the image cancel the projection.
- Wall distance has a PU surcharge and Scale may resolve downward until both the wall footprint and Core budget are valid.
- Wall Image Workspace owns an ordered nine-image presentation playlist with reorder/current/Previous/Next controls.
- Wall supports Image only; Item/Entity/Banner remain hologram-oriented sources for other chassis.
- anchor/capability/source metadata is shared by rendering, wall validation, Projection Power and settings UI.
- **delivered in 1.0.26:** Table/Wall automatic slideshow timing, persistent manual current slide and Data-show Presentation Remote pairing/control; invalid wall slides remain traversable and show a prohibition marker.
- still pending: final Survival recipes, Table/final Wall art polish, optional slideshow transition effects, optional Create Blueprint bridge and any later ceiling-specific family decision.

## F. Portable projector family

### Handheld projector

- 1.0.11 delivers the first foundation: copied-profile moving projection with reduced range/size compared with fixed projectors;
- visually descended from the lantern with a larger lens;
- Image/Item/Entity/Map-style content as supported by the final source registry;
- **no Blueprint source**;
- substantially higher Glow Dust charge consumption than a lantern;
- default minimum Ghost of about 10% to communicate "portable/lite" instability and reduce deceptive PvP readability;
- works while the player moves/aims;
- **delivered in 1.0.12:** once explicitly turned ON, the portable projector remains active when moved out of the player's hands and stored anywhere in normal player inventory; it remains active until explicitly turned OFF, while normal battery drain continues;
- **delivered in 1.0.12 foundation:** multiplayer visibility for inventory-stored active projectors uses Mirage-owned synchronized portable-device state rather than assuming remote clients can inspect another player's full inventory;
- **delivered in 1.0.12:** each portable projector owns a stable device UUID so moving it between hotbar/inventory slots does not create a new logical projector.

#### Mirage Equipment / Shoulder Strap / Shoulder Slot

Portable Mirage devices must not compete with vanilla chest armor or the normal offhand. Exploration use should never require sacrificing the player's defensive chestplate merely to carry a light/projector. The base mod therefore owns a small dedicated **Mirage Equipment** panel attached to the vanilla player inventory rather than using an armor/offhand slot.

Equipment contract:

- the panel is compact and collapsible/expandable so the normal inventory remains visually close to vanilla when Mirage equipment is not being managed;
- a dedicated **Shoulder Strap** slot stores the enabling harness as a real ItemStack;
- equipping a Shoulder Strap unlocks a neighboring **Shoulder Slot**;
- neither slot replaces helmet/chest/legs/boots, the vanilla offhand, nor the normal `F` swap-hands behavior;
- the leather/strap state should be communicated visually with a brown/leather frame/accent inspired by the offhand-adjacent mockup rather than recoloring the actual vanilla offhand slot into a fake equipment slot;
- Shoulder Slot accepts only items implementing Mirage's shoulder-mountable-device contract; initial accepted devices are **Mirage Lantern** and **Mirage Hand Projector**;
- the Shoulder Strap cannot be removed while the Shoulder Slot is occupied. The player must remove the mounted device first;
- left-click inventory interaction moves the real strap/device ItemStacks through the server-authoritative equipment inventory; no boolean-only fake equipment state;
- right-clicking an occupied Shoulder Slot opens the mounted device configuration surface. The first foundation may expose compact device controls while richer per-device GUIs are added later;
- a mounted device remains operational/ticking: Lantern continues to emit/drain according to its selected light mode and Mirage Hand Projector continues to run/synchronize an explicitly active projection;
- equipment survives save/relog/dimension changes as player equipment data and follows normal death/`keepInventory` semantics;
- the initial Mirage mount reserves the player's **right shoulder** while occupied. Vanilla shoulder riders may still use the free left shoulder, but another Parrot/shoulder entity must not claim the reserved Mirage side until the device is removed;
- the physical device may render on/near the shoulder/upper arm for presentation, but remote clients only need the synchronized mounted-device state and its visible result; they do not need access to the player's arbitrary normal inventory;
- the system should remain native to Mirage Projector. Optional future Curios/Accessories-style bridges may expose equivalent integration, but the base feature must not require another equipment-slot mod.

**Foundation target/delivery line:** the 1.0.13 implementation wave established the player attachment, Shoulder Strap + Shoulder Slot inventory, inventory-panel controls, mounted-device ticking/sync, initial physical shoulder render and right-shoulder vanilla-rider reservation. Final visual positioning and richer per-device configuration remain QA/polish work toward 1.1.0.

##### Shoulder Strap Battery Pouch / upgrade system

**Foundation delivered in 1.0.14 and rearchitected in 1.0.18:** the Shoulder Strap ItemStack itself owns the shoulder-device battery-management inventory. This belongs to 1.1.0 because it directly supports long-duration portable lighting/projection; UV-specific upgrades remain deferred to 1.2.0.

Base strap panel contract:

- **6 Battery Pouch slots** accepting only `RechargeableEnergyItem` stacks (full, partial or depleted Glow Dust / Light Battery / Creative Battery and future compatible media);
- **2 generic upgrade sockets** in the base strap panel;
- battery slots behave as a small purpose-built pouch, not a general inventory;
- shoulder device remains the one mounted `ShoulderMountableDevice`;
- battery/upgrades are real persisted ItemStacks and follow save/relog/death/`keepInventory` rules with the rest of Mirage equipment.

1.1 upgrade set:

1. **Auto Battery Swap Patch** — only affects the device installed in Shoulder Slot. When its installed rechargeable cell reaches 0%, atomically return the depleted cell to the pouch and install a charged compatible cell from the pouch. Ignore 0%-charge candidates. If the depleted cell cannot be safely returned, do not perform the swap.
2. **Shoulder Strap Slot Expansion** — expands the pouch from **6 to 9 battery slots** and unlocks a **third generic upgrade socket**. Removal is blocked while any expansion-only battery slot or that third socket is occupied.

Upgrade rules:

- at most one installed upgrade per upgrade family/type;
- duplicate Auto Swap or duplicate Expansion patches are rejected even if another socket exists;
- sockets are generic enough for later upgrade families, but 1.1 code must not contain UV-specific logic;
- the third socket is deliberately unlocked by Expansion so later releases can add another upgrade family without migrating the player attachment again.

The compact inventory panel should use clear leather/strap styling and labels/outlines for `Shoulder`, battery pouch and upgrade sockets rather than pretending any of them are vanilla offhand/armor slots.

##### Future Shoulder Device skins

A future cosmetic slot inside compatible portable devices may accept a **device skin** without replacing/crafting away the configured projector/lantern itself. The first proposed skin is a dyeable vanilla-bird/Parrot-like disguise so a shoulder-mounted Mirage device can visually resemble a small bird perched on the player while still functioning as the original device.

- the cosmetic must not destroy or rewrite the device's battery, source/profile or UUID;
- bird skin is cosmetic presentation, not a second projector registry item;
- while a Mirage device occupies the reserved shoulder, vanilla Parrots cannot also occupy that same shoulder;
- exact skin recipe/materials and dye model remain intentionally unfrozen.

#### War Banner presentation mode

**Foundation delivered in 1.0.16:** the Hand Projector now stores portable Banner presentation/facing/size/height state, renders War Banner directly above the tracked owner using the existing portable-projector state publication, and exposes the first configuration controls through the Shoulder Device screen. Final visual tuning remains QA/polish.

Banner remains a meaningful handheld source because it can act as a personal/team identifier in battles and PvP rather than only as a decorative forward projection.

The handheld Banner source must support at least two top-level presentations:

1. **Forward Projection** — normal handheld behavior in front of the player/device.
2. **War Banner** — a smaller floating holographic banner positioned a short distance above the player's head and following that player while the portable projector remains active, including while the projector is stored in inventory.

War Banner requirements:

- no physical banner pole/staff near the player's back or body;
- smaller scale than the normal forward-projected Banner so it remains readable without becoming a large obstruction;
- float slightly above the head with enough separation to read during group combat while remaining visually attached to that player;
- remain a hologram and preserve the portable projector's minimum Ghost/readability rule;
- support a **Directional** facing option where the banner orientation remains physically tied to the player;
- support an optional/default PvP-oriented **Always Face Viewer / Omnidirectional Billboard** facing option where each observing client rotates the banner horizontally toward its own camera, allowing team identity to remain readable from the side/rear;
- Billboard mode should normally use observer-camera yaw only while keeping the banner's vertical axis upright rather than pitching toward observers above/below;
- Size and vertical Height/offset should be portable-specific presentation controls, bounded so the banner cannot become a giant combat obstruction;
- future optional team-color/outline presentation may add a subtle holographic team cue without recoloring/destructively modifying the actual banner design.

The War Banner feature is intended for battle standards, team identification, temporary PvP modes and similar multiplayer scenarios. It must not require the player to keep the handheld projector selected in the hotbar.

#### Creative / debug battery

**Foundation delivered in 1.0.12:** `mirage_projector:creative_battery` is the Mirage rechargeable medium intended only for Creative/debug/temporary game-mode use:

- infinite/no-depletion energy while installed;
- no survival recipe, world loot or normal progression path;
- available through Creative/debug/admin distribution and commands;
- compatible with portable Mirage devices through the same rechargeable-energy contract rather than special-casing every device;
- suitable for temporary PvP/minigame loadouts so a projector/War Banner can remain active for an entire match without recharge management;
- servers/minigame controllers may grant it at match start and remove it when the temporary mode/session ends;
- it is a QA/admin convenience, not the balance baseline for normal Survival batteries.

### Horizontal/table projector

- intended for floor/table placement;
- Shift + empty-hand interaction may pick it back up, following the portable-placement usability model;
- projects a liftable horizontal plane for maps, planning surfaces, tablecloth-like images and decorative ceiling projection when Lift is raised high enough;
- this chassis is the preferred future host for 3D Blueprint content from the Create bridge.

### Wall / data-show projector

- presentation-oriented wall projection;
- ordered multi-slide image presentation;
- source-agnostic slide architecture so optional registered content can appear between image slides later;
- eventual Create bridge may insert an interactive Blueprint slide without teaching the base mod about Create.

## G. Presentation/source architecture requirements

- slides reference generic projection-source descriptors, not image-only filenames;
- compatible chassis decide which source types can be selected;
- handheld projectors explicitly reject Blueprint capability even if the addon is installed;
- portable/fixed power models share presentation/source contracts without pretending Core PU and battery charge are identical resources.

## H. Dragon Egg / End Resonance Easter Egg

### Product intent

`End Resonance` is a deliberately hidden 1.1.0 Easter egg that gives the vanilla Dragon Egg a rare, world-significant use without turning it into a normal progression material.

The Dragon Egg acts as a **special resonance Core**, not as a normal PU Core and not as a consumable ingredient. Because a survival world normally exposes only one naturally obtained Dragon Egg, the scarcity of the item naturally makes an End Resonance installation feel unique/community-significant without Mirage enforcing an artificial one-per-world rule.

This feature is **not a 1.0.0 blocker**. It belongs to 1.1.0 after the generalized energy/source abstractions are established.

### Compatible chassis

Only these fixed chassis accept a Dragon Egg as an End Resonance Core:

- **Mirage Field Projector** — creates a planar End aperture.
- **Mirage Prism** — creates a volumetric End anomaly.

The following must reject Dragon Egg resonance mode:

- Mirage Projector / Compact;
- Mirage Display;
- Wide Mirage Projector;
- Tall Mirage Projector;
- handheld/portable projector family;
- any future chassis that does not explicitly expose the `END_RESONANCE` capability.

### Field behavior — planar aperture

When a Dragon Egg is inserted into a Mirage Field Projector:

- suspend the normal projection source;
- render a fixed **2 blocks wide × 3 blocks tall** vertical End aperture;
- the aperture is a 2D plane, not a volumetric box;
- orientation follows the projector/chassis facing chosen at activation time;
- no obsidian frame is created and no vanilla End Portal blocks are placed in the world;
- the visual language should reuse/recreate the characteristic depth, star-field/parallax and mystical appearance of the vanilla End portal while remaining a Mirage-rendered effect;
- crossing the active plane transfers compatible entities through the aperture.

Design identity: **Field = doorway**. It should read as a deliberate, practical dimensional gate created by Mirage technology.

### Prism behavior — volumetric anomaly

When a Dragon Egg is inserted into a Mirage Prism:

- suspend the normal Prism projection;
- render a fixed **2 × 3 × 2 block** End Resonance volume;
- the volume should visually read as a portal cube/chamber rather than a flat portal repeated four times;
- exterior faces should use the End-portal visual language so walking around it preserves the impossible/deep spatial effect;
- no physical vanilla End Portal blocks are placed;
- any compatible entity whose bounding box meaningfully enters the active resonance volume may trigger dimensional transfer.

Design identity: **Prism = anomaly**. It should feel more mysterious and spatially impossible than the practical Field doorway, as though the Prism is forcing open a small three-dimensional region connected to the End.

### Dragon Egg Core semantics

The Dragon Egg is a special resonance catalyst:

- it is **never consumed**;
- it does not convert into PU and does not participate in normal Core-capacity arithmetic;
- it should not be accepted as a generic recipe material for this feature;
- it should render visibly as the installed Core/resonance object where practical;
- Prism/Field emitters may use a special intensified violet/resonance visual state while the egg is installed;
- automation that inserts/removes the egg must execute the same activation/restoration contract as direct player interaction;
- state-copy/upgrade/crafting flows must never duplicate the physical Dragon Egg.

Architecturally this belongs under a generalized special-energy/resonance contract rather than an `if dragon_egg` exception scattered through projector logic. Conceptual future-facing shape:

```text
Energy / activation providers
├── PROJECTOR_CORE        -> normal PU
├── GLOW_DUST             -> rechargeable portable energy
└── SPECIAL_RESONANCE
    └── DRAGON_EGG        -> END_RESONANCE
```

Exact class/interface names are implementation details; the separation of semantics is the requirement.

### ProjectionState snapshot and restoration

Activation must be completely non-destructive to the projector's previous configuration.

When the Dragon Egg is inserted:

1. serialize/copy the complete current projector-facing state into an `End Resonance restore snapshot`;
2. preserve all source-specific state necessary to reproduce the previous projection exactly;
3. enter `END_RESONANCE` mode;
4. suspend the normal projection without mutating its saved settings.

The restore snapshot should include, where applicable:

- selected source family and source identity/data;
- scale;
- lift;
- float mode/amplitude/phase-relevant configuration;
- rotation/orientation settings;
- tint/colour state;
- Ghost/opacity state;
- image workspace configuration and selected banks/faces;
- Item/Banner state;
- Entity snapshot/card selection, pose, equipment visibility and related projector-facing settings;
- any future generic `ProjectionTransform` or source descriptor fields introduced before 1.1.0.

While End Resonance is active, changing ordinary projection controls must **not** mutate either the portal or the stored restore snapshot.

When the Dragon Egg is removed:

1. immediately remove the End Resonance portal/anomaly;
2. return/preserve the exact Dragon Egg item;
3. restore the complete saved projector state;
4. resume the previous source as though End Resonance had never modified it.

The restore snapshot must persist in BlockEntity save data while the egg is installed. World save/reload, chunk unload/reload, server restart or client relog must not lose the suspended projector configuration.

### UI lock/suspension contract

While `END_RESONANCE` is active:

- Scale is locked;
- Lift is locked;
- Float is locked;
- rotation controls are locked;
- Tint/Ghost controls are locked;
- Overdrive is unavailable;
- normal source replacement/import actions are unavailable;
- Entity tabs/workspaces are blocked;
- controls that could mutate the suspended source are disabled/read-only rather than silently editing hidden state.
- the normal projector ON/OFF state is overridden by End Resonance for as long as the Dragon Egg remains installed;
- `TURN OFF` remains visible/attemptable as deliberate feedback, but **must not** disable End Resonance, stop the aperture/anomaly, alter the restore snapshot or change the suspended source;
- pressing `TURN OFF` while the Dragon Egg is installed produces the event message exactly: `This doesn't seem to work...`;
- normal shutdown/source activation controls regain authority only after the Dragon Egg is physically removed and the suspended ProjectionState is restored.

The GUI should visibly switch to a resonance-specific status panel rather than looking broken. Suggested information hierarchy:

```text
END RESONANCE
Projection parameters suspended.

Dragon Egg resonance detected.
```

Chassis-specific status can expose:

```text
FIELD
Aperture: PLANAR
Dimensions: 2 × 3
```

or:

```text
PRISM
Aperture: VOLUMETRIC
Dimensions: 2 × 3 × 2
```

The text is UX guidance, not frozen final localization copy.

### Dimensional transfer behavior

End Resonance is intended to be a **functional portal**, not only a shader/decorative projection.

Baseline destination semantics should follow the intuitive vanilla End-portal loop where practical:

- outside The End -> transfer to The End;
- from The End -> return toward the Overworld/appropriate vanilla-style return destination.

The implementation must use Mirage's own portal collision/transfer envelope; do not place hidden vanilla `end_portal` blocks merely to obtain teleportation.

Compatible transferable entities should intentionally include more than players:

- players;
- living mobs/animals;
- dropped item entities;
- compatible projectiles where vanilla dimensional transfer rules permit them;
- boats/minecarts or other vehicles when their transfer semantics are safe and well-defined;
- **Primed TNT**, preserving its active fuse state through transfer where vanilla entity serialization/teleport semantics allow it;
- **FallingBlockEntity / gravity-block entities**, enabling technical transport setups involving sand, red sand, gravel, concrete powder, anvils and other gravity-driven blocks where their normal entity form is transferable.

This is intentionally useful for multiplayer infrastructure and technical play. A shared End Resonance installation can become a central world transport point, mob/item routing endpoint or component in technical contraptions.

Mirage should guarantee **normal compatible entity transfer**, not a specific item-duplication exploit. If a particular Minecraft version permits a technical farm/duplication setup through vanilla falling-block or portal behavior, that remains version-dependent behavior rather than a Mirage API guarantee.

### Safety / edge cases to QA

Before 1.1.0 release, explicitly test:

- survival Dragon Egg insertion/removal without item loss or duplication;
- world save/reload while resonance is active;
- chunk unload/reload and server restart;
- projector break while the egg is installed;
- explosion/destruction/automation extraction paths;
- inventory-full extraction fallback/drop behavior;
- Prism/Field upgrade/state-transfer interactions while resonance is active;
- multiple End Resonance portals created through Creative/admin duplication;
- multiplayer simultaneous crossing;
- player, mob, item, projectile, Primed TNT and FallingBlockEntity transfer;
- entity cooldown/anti-loop behavior so an entity does not ping-pong dimensions every tick;
- vehicles/passengers only if their vanilla transfer contract is safe;
- destination collision/safe placement;
- client render cleanup after teleport, dimension change, projector removal and egg extraction;
- no Dragon Egg duplication through state copies, recipes, drops or projector cloning;
- no hidden mutation of the suspended ProjectionState while UI controls are locked.
- `TURN OFF` repeatedly returns `This doesn't seem to work...` and never changes End Resonance state while the Dragon Egg remains installed.

### Visual/FX direction

- Reuse the visual language of vanilla End Portal depth/star/parallax effects where technically appropriate, but render through Mirage so arbitrary vertical/volumetric geometry is possible.
- Do not build a Nether-style obsidian frame.
- The projector remains visibly responsible for sustaining the aperture/anomaly.
- Violet Mirage emitters/resonance accents should visually connect the Dragon Egg to the active portal.
- Field should remain readable as a clean planar gateway.
- Prism should emphasize the uncanny 3D chamber/cube effect and reward walking around it as decoration even when nobody is actively using the portal.

### Discovery philosophy

This should remain an Easter egg rather than a tutorialized progression feature:

- do not expose a normal recipe/tutorial that explicitly tells the player `Dragon Egg -> End portal`;
- avoid spelling the mechanic out in the public changelog;
- subtle handbook/lore hints may be considered later, but should preserve discovery;
- once discovered, the GUI can clearly explain the active `END RESONANCE` state and how to safely remove the egg.

### 1.1.0 changelog teaser

Preferred final-line teaser for the public 1.1.0 changelog:

> **Some projections may have an End after all...**

Use `End` capitalized exactly as written. It hints at the dimension and the projector connection without naming the Dragon Egg, Prism/Field behavior, portal geometry or teleport capability.

Fallback candidate if the preferred line feels too explicit during final release editing:

> **There might be an End to this after all...**

Do **not** add a normal changelog bullet explicitly describing End Resonance unless the feature is intentionally declassified later.


### Physical illumination projector follow-up

- Keep the existing floor-standing `Mirage Light Projector`; it was not part of the original lantern-on-ground request, but is now an accepted device.
- Its lamp head should later gain visible horizontal rotation + vertical tilt so players can aim the beam at a chosen point/direction.
- A true wall-mounted light projector remains a separate pending 1.1.0 chassis/device; the floor model does not replace it.
- The handheld Mirage Lantern should still eventually support being placed temporarily as a vanilla-lantern-like portable beacon without turning into the wall projector.
