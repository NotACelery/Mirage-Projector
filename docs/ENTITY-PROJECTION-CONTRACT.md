# Mirage Projector — Entity / Humanoid Entity Projection Contract

Status: **design frozen; dev.17 implements Empty Scan Template + Entity Workspace + client-only 3D preview + in-world Entity/Humanoid renderer, bodyless Humanoid mannequin, frozen Player skin property, projector-base nameplate and projection-local 3D Tint/Ghost handling. Pose presets, pose-aware clearance and final glint/custom-RenderType QA remain pending.**

This document is normative for the entity-projection subsystem. Later implementation must preserve these rules unless the project explicitly revises them.

---

## 1. Projection modes and ownership boundaries

Mirage has four distinct source semantics:

| Mode | Source geometry | What rotates | Source ownership |
|---|---|---|---|
| Image | 2D face(s) | plane/prism face geometry | imported asset snapshot |
| Item | one volumetric object | the complete 3D item/block | virtual ItemStack snapshot |
| Humanoid Entity | humanoid rig + optional base entity + equipment overlays | complete posed rig | entity snapshot + virtual equipment snapshots |
| Entity | one non-humanoid/general entity | complete entity model | frozen entity scan snapshot |

**Humanoid Entity Mode replaces the old working name Armor Mode.**

Armor is only one layer of the mode. The actual object is a humanoid display rig capable of showing:

- no body, only projected equipment on an invisible rig;
- a scanned Player;
- Zombie / Baby Zombie / Husk / Drowned;
- Skeleton / Stray / Wither Skeleton;
- Piglin-family humanoids;
- later compatible modded humanoids;
- four armor channels plus both hands;
- pose presets;
- complete-rig Scale, Lift, Rotation, Floating and Ghost Effect.

Entity Mode is for mobs whose primary projection is their own entity model rather than an editable humanoid equipment rig.

---

## 2. Entity Scan Card

### 2.1 Physical object

The empty player-facing item is **Empty Scan Template**. Internally the stable registry id may remain `entity_scan_card` for compatibility, but that internal id is not the UX name. The template is non-stackable and visually uses paper.

Recipe:

```text
N N N
N P N
N N N

N = Iron Nugget
P = Paper
```

Scanning requires **Shift + right-click** on one LivingEntity/Player. Normal right-click is intentionally passed through so ordinary entity interactions remain available. A populated scan may be deliberately replaced by repeating Shift + right-click on another valid target.

The card records a **frozen visual snapshot**. It is not a live tracker.

The stored source UUID is provenance/debug metadata only. Projection must continue if the original entity:

- dies;
- despawns;
- changes dimension;
- unloads;
- is a Player who logs out.

### 2.2 Scan-data foundation and dev.14 behavior

Dev.13 implements the first scan-card data layer:

- scan a single LivingEntity by using the card on it;
- reject entities that are currently passengers or vehicles, avoiding jockey/composite captures in v1;
- generate an independent scan UUID;
- preserve source UUID and entity type;
- preserve a bounded/sanitized visual NBT snapshot;
- split vanilla humanoid equipment into six explicit channels;
- split Horse saddle/body armor into explicit channels;
- keep generic non-editable visual state in the entity snapshot;
- require sneak-use to overwrite a populated card;
- cap entity snapshot NBT for safety.

Dev.14 **does consume/import populated scans** through the dedicated projector Entity Workspace. Inserting a populated scan copies the frozen entity body into projector-owned state and stages editable equipment as virtual Incoming data. The physical scan item is not the projection itself.

### 2.3 Composite-entity rule

Initial scanning does not support a visual composition made from multiple entities.

Rejected while mounted/composed:

- chicken jockey;
- skeleton jockey;
- rider + mount pairs;
- passenger stacks;
- any source entity that currently has passengers or is itself a passenger.

A future Composite Entity Display may deliberately support this, but it is not part of the base Entity Mode.

---

## 3. Effective GUI family

Entity editing uses a dedicated GUI family. It must not keep expanding the current Image/Item editor until one screen has unrelated controls everywhere.

The entity GUI has three layout profiles:

1. **Humanoid layout**
2. **Horse layout**
3. **Generic Entity layout**

The effective profile is selected from:

1. the card currently staged in the Entity Scan Card slot, when present;
2. otherwise the active imported entity snapshot/context;
3. otherwise the explicitly selected mode.

Humanoid is intentionally different from Horse/Generic: the six humanoid equipment channels are intrinsic to Humanoid Entity Mode and therefore do not disappear merely because the card slot becomes empty.

---

## 4. Shared GUI structure

The main entity editor is divided into three horizontal domains with the player inventory below them.

```text
┌──────────────────────────────────────────────────────────────────────────────┐
│ MIRAGE PROJECTOR — HUMANOID / ENTITY                                        │
├──────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  INCOMING / STAGING         ENTITY SOURCE         PROJECTED / ACTIVE         │
│                                                                              │
│  [Head]   [✓]                                    [Head]                      │
│  [Chest]  [✓]             [ Scan Card ]          [Chest]                     │
│  [Legs]   [✓]                                    [Legs]                      │
│  [Feet]   [✓]                                    [Feet]                      │
│  [Main]   [✓]                                    [Main]                      │
│  [Off]    [✓]                                    [Off]                       │
│                                                                              │
│  [Return inserted gear]                         [Pose / entity controls]     │
├──────────────────────────────────────────────────────────────────────────────┤
│ Player inventory + hotbar                                                   │
└──────────────────────────────────────────────────────────────────────────────┘
                                                     ┌───────────────────────┐
                                                     │ LIVE 3D PREVIEW       │
                                                     │                       │
                                                     │ entity / rig          │
                                                     │ fitted to viewport    │
                                                     │ follows cursor where  │
                                                     │ the model supports it │
                                                     └───────────────────────┘
```

The right-side preview is visually attached to the editor but may sit outside the main inventory rectangle, like an auxiliary inspection panel.

---

## 5. Humanoid Entity layout

### 5.1 Six incoming channels on the LEFT

Humanoid mode always owns six incoming/staging channels:

```text
HEAD
CHEST
LEGS
FEET
MAIN_HAND
OFF_HAND
```

This explicitly includes **both hands on the left**, not only the four armor pieces.

An incoming channel may be populated by two source classes:

- **Card snapshot**: virtual equipment extracted from a scanned humanoid entity;
- **Physical staging item**: a real item temporarily placed by the player so Mirage can capture a snapshot from it.

The GUI must visually distinguish those origins. A card-derived incoming stack is not obtainable because it was never a real projector inventory item.

### 5.2 Six projected channels on the RIGHT

The right side owns the six active projected snapshots:

```text
HEAD
CHEST
LEGS
FEET
MAIN_HAND
OFF_HAND
```

They are render snapshots, never physical storage.

They persist independently of the Entity Scan Card slot.

### 5.3 Card removal in Humanoid Entity Mode

Removing a Zombie/Player/Skeleton/etc. card must **not**:

- remove the six left/right slot positions;
- erase projected Head/Chest/Legs/Feet snapshots;
- erase projected Main/Off Hand snapshots;
- collapse the GUI back to another layout simply because the card slot is empty.

Humanoid equipment is mode state, not card-presence state.

This preserves the original equipment-display use case: Humanoid Entity Mode can still behave as an invisible virtual mannequin with no body card inserted.

The base scanned humanoid entity and the six projected equipment channels are separate data domains. Removing the physical Humanoid card clears the card-supplied **body** but leaves Humanoid Incoming/Projected equipment intact, so the workspace becomes a bodyless virtual mannequin instead of losing its configured loadout.

---

## 6. Incoming -> Projected conflict resolution

The incoming side is deliberately a **staging area**. Inserting/scanning a new humanoid must never silently overwrite the active projected loadout.

Every left channel has a dedicated **✓ Apply** control immediately beside it.

Example row:

```text
[ incoming helmet ] [✓]        [ projected helmet ]
```

### 6.1 Empty destination

If the projected slot is empty:

- clicking ✓ copies/captures the incoming visual snapshot to the right;
- no conflict prompt is necessary.

### 6.2 Occupied destination

If the projected slot already contains a different snapshot:

- show the row as a conflict;
- clicking ✓ asks for explicit replacement confirmation;
- only that one slot is replaced;
- all other armor/hand channels remain untouched.

Suggested confirmation:

```text
Replace projected Chest?

Current: Netherite Chestplate · Silence Trim
Incoming: Golden Chestplate

[ Replace ] [ Cancel ]
```

### 6.3 Same destination

If incoming and projected visual snapshots are equivalent, the row may show an already-applied state and ✓ becomes a no-op.

### 6.4 No global destructive overwrite by default

The old design idea of one blanket `overwrite equipment` operation is superseded as the primary workflow by **slot-by-slot conflict resolution**.

Bulk helpers may exist later, for example:

- Apply all empty slots;
- Replace all;
- Clear incoming;

but they must never remove the per-slot controls.

---

## 7. Physical staging items on the left

The left side also serves the direct equipment-snapshot workflow.

Example:

1. player places their real trimmed Netherite Helmet in incoming HEAD;
2. incoming HEAD visibly contains a **physical staged source**;
3. player clicks ✓;
4. Mirage creates a one-count virtual render snapshot on projected HEAD;
5. the real helmet is returned to the player inventory when possible.

A global button:

**Return inserted gear**

returns all **physical** staged inputs.

It does not delete card-derived virtual incoming snapshots because there is no physical item to return.

If the player's inventory is full, an item that cannot be safely returned remains in its staging slot. Mirage must never delete or drop a staged source merely to finish snapshot capture.

This staging inventory is distinct from the projected snapshot storage and must be clearly labelled.

---

## 8. Importing a scanned humanoid with existing equipment

Example: a scanned Baby Zombie wearing full Gold Armor.

When its card is staged:

- Baby Zombie base snapshot becomes the candidate body source;
- Head/Chest/Legs/Feet from the card appear on LEFT incoming rows;
- its held items appear in incoming Main/Off Hand;
- the right projected loadout remains unchanged;
- conflicts are resolved one row at a time with ✓.

This allows both workflows:

### Exact replica

Accept all six incoming channels.

Result: Baby Zombie exactly as scanned.

### Space-Marine-style customization

Import only the entity body and accept none/some of its equipment.

Then stage custom armor/weapons in the left side and apply those snapshots individually.

The scanned entity is therefore a **body preset + incoming loadout**, not a forced monolithic loadout.

---

## 9. Player / humanoid body behavior

A Player scan is a Humanoid Entity source.

Humanoid mode must ultimately support:

- scanned Player body/skin;
- Player with no armor;
- Player with scanned armor;
- Player with entirely custom projected armor;
- any mix of Main/Off Hand snapshots;
- pose changes without re-capturing equipment.

The equipment overlay is independent from the body source.

---

## 10. Horse layout

Horse is a dedicated dynamic Entity layout, not the six-slot humanoid layout.

When the active/staged card is a compatible Horse scan, show:

```text
INCOMING               PROJECTED
[Saddle]   [✓]          [Saddle]
[BodyArmor][✓]          [BodyArmor]
```

The same conflict semantics apply.

### 10.1 Horse context removal

Horse-only fields are not permanent general-purpose projector slots.

When Horse context is removed/replaced by a non-Horse Entity context:

- the card-supplied Horse body is cleared;
- Saddle controls disappear;
- Body Armor controls disappear;
- horse-specific staged virtual memory is cleared;
- horse-specific projected override memory is cleared unless deliberately committed into a future reusable horse preset format.

Physical Horse staging is never silently deleted: normal card removal stays blocked until the user returns those real items.

Humanoid mode is the exception because its six equipment channels are intrinsic to the mode itself.

---

## 11. Generic Entity layout

A generic Entity scan gets:

- central Entity Scan Card/source area;
- entity metadata/status;
- presentation controls;
- right-side live 3D preview;
- player inventory below.

It does **not** automatically expose six humanoid equipment slots.

If a mob visually contains armor/equipment that is not a normal editable equipment channel, that appearance remains part of the frozen entity snapshot.

Examples may include modded creatures whose armor is implemented as entity model state rather than an ItemStack equipment slot.

Mirage must not fake editability where the renderer/data model cannot actually support it.

---

## 12. Future compatibility slots — explicitly OUTSIDE base scope

Base Mirage Projector does not promise slots for every external equipment ecosystem.

Future optional compatibility may add adapters for systems such as:

- backpacks/back slots;
- Curios-like accessory slots;
- artifacts;
- trinkets;
- mod-specific equipment capabilities;
- cosmetic armor layers.

These integrations must be **additive adapters**, not hard dependencies and not part of the initial Humanoid Entity Mode completion criteria.

A compatibility adapter may contribute:

- extra incoming row(s);
- extra projected snapshot row(s);
- renderer hook(s);
- preview contribution;
- power cost contribution if needed.

The six vanilla humanoid channels remain the stable baseline.

---

## 13. Unified preview contract

Mirage GUIs need two preview levels.

### 13.1 Per-source thumbnails

Every 2D/multi-source projection channel must be visually identifiable:

- Front / Back images;
- future North/East/South/West images;
- banners;
- multi-display cells.

Do not rely on filenames such as `3j5hk4h6hj35k34h52h3i53hj4j3.png`.

Every source gets its own thumbnail/preview and role label.

### 13.2 Composed 3D preview panel

Item, Humanoid Entity and Entity modes receive a composed preview panel to the right of the main GUI.

It previews the **active projected result**, not merely a raw inventory icon.

Required examples:

- BlockItem -> actual 3D block;
- standalone armor -> equipped geometry when that renderer is complete;
- Humanoid Entity -> body + active projected armor + both hands + current pose;
- Horse -> horse + active Saddle/Body Armor overrides;
- generic mob -> complete scanned entity model;
- banners on multi-face projector -> real banner-like 3D surface preview where appropriate;
- Image/Prism -> simplified plane/prism preview plus individual face thumbnails.

---

## 14. Preview sizing and clipping

The preview must fit its viewport regardless of entity size.

A Chicken, Baby Zombie, Player, Horse and Ender Dragon cannot use one hardcoded GUI scale.

### Fit algorithm target

1. reserve a fixed preview viewport;
2. obtain model/entity bounding width and height;
3. compute a fit scale from available pixel width/height;
4. clamp to sensible UI minimum/maximum;
5. anchor the model to a stable baseline/center;
6. clip with a scissor rectangle so no renderer can draw across controls;
7. re-evaluate when entity type, baby/adult state or special model dimensions change.

Conceptually:

```text
scale = min(viewportWidth / entityWidth,
            viewportHeight / entityHeight) * calibration
```

Then clamp to a safe GUI range.

This replaces brittle category-only scaling, though per-entity calibration overrides may still be needed for unusual renderers.

### Mouse-following

Humanoid entities should mimic the vanilla inventory preview where head/face tracks the cursor when technically compatible.

For non-humanoid entities:

- allow whole-model yaw toward the cursor where safe;
- do not force head tracking on models that do not expose compatible head transforms.

Huge entities such as Ender Dragon must still remain contained inside the same preview panel.

---

## 15. Preview update rules

The composed preview updates immediately when:

- a scan card changes;
- a left incoming slot changes;
- a ✓ is accepted;
- a projected slot is cleared;
- pose changes;
- horse Saddle/Body Armor changes;
- entity/body source changes;
- Scale/Ghost/rotation presentation settings change where preview simulation supports them.

The default composed preview represents the **right/active projected state**.

Incoming/staged equipment should not silently appear as if already applied. A future hover/compare preview may ghost the candidate on top, but that is optional.

---

## 16. Entity-name display

Entity projections may show a base-aligned name label.

Planned choices:

- Off;
- Custom/scan display name;
- Entity type;
- Name + type.

Text is presentation metadata, not part of the entity model snapshot itself.

---

## 17. Animation contract

Entity Mode ultimately supports entity-native idle/base animation rather than treating the model as a static item.

Initial target:

- idle/base animation;
- freeze animation toggle;
- animation speed later if useful;
- whole-entity Mirage Rotation and Floating remain separate transforms.

Ender Dragon, Horse, Villager, etc. may therefore animate naturally while the overall hologram also rotates/floats.

Animation state must not require the original entity to remain loaded.

---

## 18. Scanner/catalog future idea

A future **Entity Catalog / Scan Binder** may store or index many scan cards in one collectible/research-style interface, inspired by bestiary/scanner catalog workflows.

This is an idea-level future feature only.

It is **not** part of the current implementation queue and must not delay:

- functional Entity Scan Card;
- projector Entity import;
- dynamic Entity GUI;
- Humanoid Entity equipment workflow;
- 3D preview;
- entity world renderer.

---

## 19. Implementation order after dev.16

### Foundation already present

- Item virtual snapshots;
- per-snapshot UUIDs;
- image thumbnails;
- 3D Item renderer foundation;
- Entity Scan Card item and first frozen scan-data format.

### Implemented in dev.14

1. projector-side entity snapshot state;
2. physical populated-scan staging slot;
3. Humanoid/Horse/Generic layout resolver;
4. Humanoid six incoming + six projected channels;
5. per-row ✓ conflict resolution;
6. physical staging-item return semantics;
7. Horse Saddle/BODY dynamic channels + safe removal guard;
8. dedicated Entity/Humanoid editor screen;
9. right-click clear of projected snapshots;
10. player/custom-name nameplate metadata.

### Implemented in dev.15

1. safe GUI entity reconstruction from frozen scan data without adding it to the level;
2. clipped auto-fitted 3D preview using vanilla inventory entity rendering;
3. Humanoid/Horse preview equipment sourced only from Projected/Active channels;
4. cursor-facing preview transforms where vanilla supports them;
5. responsive external preview width;
6. card/body teardown semantics separated from persistent Humanoid equipment state.

### Implemented in dev.16

1. world `SourceMode.ENTITY` renderer using client-only reconstructed LivingEntity objects;
2. under-projection selective nameplate renderer;
3. bodyless Humanoid mannequin that renders Projected armor/hands with no body card;
4. Player packed `textures` property frozen into scan data and reused by Mirage RemotePlayer;
5. standalone armor Item snapshots rendered as equipped geometry;
6. GUI Item preview follows the equipped-geometry rule;
7. visual-only render clock plus first safe Ender Dragon history/flap adapter.

### Next implementation block

1. Windows build/in-game QA of dev.16 renderer paths;
2. QA/refine Ghost Effect/Tint across glint and custom/modded RenderTypes after the dev.17 projection-local buffer pass;
3. Humanoid pose presets and hand transforms where vanilla defaults are insufficient;
4. pose/model-aware clearance;
5. additional entity-specific visual-animation adapters only where required by renderer internals.

### Then

- entity animation controls and special per-species presentation;
- modded humanoid/equipment compatibility adapters;
- optional backpack/accessory/artifact adapters;
- Banner/multi-source/Prism progression from the wider Mirage roadmap;
- Entity Catalog / Scan Binder only as a far-future catalog idea.

---

## 20. Non-negotiable invariants

1. Scanning never removes the source entity or its equipment.
2. A scan UUID is Mirage identity; source UUID is provenance only.
3. Projected equipment is snapshot data, never lootable physical inventory.
4. Humanoid Entity Mode always exposes four armor + two hand channels, even with no card inserted.
5. Scanned humanoid equipment enters on the LEFT and never silently overwrites the RIGHT.
6. Every incoming humanoid channel has its own ✓ Apply action.
7. Horse-specific channels are dynamic and must not leak into unrelated entity contexts.
8. Generic/non-editable entity armor stays part of the entity snapshot unless an explicit adapter exists.
9. Player inventory is below the entity editor.
10. Preview is clipped and auto-fitted so Dragon/Horse/Chicken scale differences cannot break the GUI.
11. External backpack/accessory/artifact slot systems are future optional integrations, not baseline scope.
12. Composite/jockey scans remain unsupported until deliberately implemented.


## 21. Empty Scan Template — exact gameplay contract

The scanner medium is intentionally cheap enough to catalog entities but non-stackable enough to keep every captured subject individually identifiable. One recipe yields one template from exactly eight Iron Nuggets + one Paper.

The scan action is **Shift + right-click**. This is not merely a convenience binding: it is a compatibility rule so Mirage does not consume normal right-click behaviors from tameables, villagers, mounts or modded interactable mobs.

A successful scan captures appearance only. It must never:

- damage the target;
- remove armor or hand items;
- copy items into an obtainable projector inventory;
- require the source to remain alive/loaded;
- treat the source UUID as the render source.

A populated template may be rescanned with the same explicit gesture. Replacement creates a fresh Mirage `ScanId`.

## 22. Projection nameplate policy

Nameplate text is a separate projector-owned presentation channel, not the entity's vanilla overhead name rendering. The captured entity snapshot strips vanilla custom-name rendering state to avoid duplicate labels.

Rules:

| Source | Nameplate |
|---|---|
| Player `Haku` | `Haku` |
| unnamed Horse | none |
| Horse custom-named `Aurelio` | `Aurelio` |
| unnamed Zombie | none |
| Zombie custom-named `Steve` | `Steve` |

The final world renderer places this label in the vertical gap between the lowest point of the projected model and the projector base. It follows projection transforms/visibility rules but is not a generic entity-type caption.

## 23. dev.14 implemented state machine

### 23.1 Scan card is physical; body lifetime follows the card

The center slot physically stores a populated scan template while it is staged. On insertion, Mirage copies the frozen scan into `EntityProjectionState` for rendering/sync rather than rendering directly out of an inventory slot. The card-supplied body is cleared when the card leaves the slot; virtual equipment has its own lifetime rules and never becomes lootable card contents.

### 23.2 Humanoid Incoming is persistent mode state

Humanoid mode always exposes six channels. Card-derived equipment is loaded into virtual Incoming. Physical user-supplied gear in the matching left staging slot temporarily takes precedence as the visible Incoming source for that row. Applying it captures a new virtual snapshot; it does not move the real item to Projected.

### 23.3 Projected/Active is authoritative render state

The right side is the only equipment set the preview/world renderer should eventually equip on the projected humanoid. Incoming data must never visually masquerade as already accepted.

Right-clicking a right-side virtual cell clears only that channel. This enables workflows such as importing a full-gold Baby Zombie, removing only its helmet hologram, then capturing/applying a Netherite helmet instead.

### 23.4 Horse context teardown

Horse Saddle/Body virtual overrides are context-specific. When Horse context is torn down, their virtual state is cleared. Real physical staging items are protected separately: normal card removal is blocked while such items remain, and `Return inserted gear` must be used first. No context switch may silently delete a real item.

### 23.5 Generic mobs

Generic mobs expose no fabricated equipment rows. If visible equipment cannot be represented through a supported vanilla equipment channel, it remains part of the frozen body snapshot until an explicit future compatibility adapter knows how to edit/render it safely.

## 24. 3D preview implementation in dev.15

The right-side panel now reconstructs and renders the active frozen LivingEntity client-side without adding that object to the level. It restores sanitized visual state, clears vanilla overhead names, applies only Projected/Active Humanoid/Horse equipment and uses the vanilla inventory renderer for clipping/cursor-facing behavior.

Auto-fit is derived from entity bounding width/height and the current preview viewport. The panel itself is responsive: a preferred width is used when possible and a compact width is accepted on narrower logical resolutions.

The preview cache invalidates when level, `ScanId` or Projected snapshot UUIDs change. Incoming/staging equipment remains absent until explicitly applied.

dev.16 closes the vanilla Player self-contained baseline by storing the packed GameProfile `textures` property and resolving the Mirage RemotePlayer skin from that frozen property. Live `PlayerInfo` is no longer the only source. Servers/mods using non-standard skin channels remain optional compatibility work.

The same body + Projected/Active composition remains the normative input for the future world renderer. An Ender Dragon must fit inside the same preview region in which a Chicken or Baby Zombie is still large enough to inspect.


## 25. dev.16 world-render and virtual-mannequin implementation

The world renderer now consumes exactly the same projector-owned state as the GUI preview. A reconstructed body is never added to the level and never executes entity AI. Removing a Humanoid card while keeping Projected equipment switches the render source to a client-only invisible humanoid rig rather than deleting the six equipment channels.

Standalone armor in Item Mode reuses that rig and therefore displays equipped geometry. Ordinary tools/blocks/items remain one full ItemRenderer object.

Player scans carry the standard packed GameProfile `textures` property where available. Mirage rebuilds a temporary GameProfile from the frozen property and uses the client SkinManager to resolve skin/cape/elytra resources; the source player does not need to remain online.

The renderer intentionally suppresses vanilla overhead name tags for Mirage Player clones. Projector `NameplateText` is the sole user-facing entity label and is drawn near the projector base according to the policy in section 22.

Fake projection entities must never call `tick()`/`aiStep()`. Special renderers needing history receive explicit visual-only adapters. Ender Dragon currently seeds its latency history and flap clock without invoking dragon phase/AI/world behavior.
