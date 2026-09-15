# PRE-1.0.14 ROADMAP / DOCUMENTATION AUDIT

Baseline audited: `1.0.13`
Minecraft: `1.21.1`
NeoForge: `21.1.244+`
Network protocol: `30`
ProjectionSettings format: `3`

## Technical baseline

`python tools/verify_current_line.py` completed successfully on the untouched 1.0.13 source snapshot supplied by the user:

```text
MIRAGE PROJECTOR 1.0.13 VERIFICATION PASS (30 gates)
```

The source tree is therefore considered a clean implementation baseline for the next wave.

## Documentation drift found

The main implementation documents were structurally sound, but roadmap state had drifted in three places:

1. `WAITLIST-1.1.0.md` mixed already-delivered 1.0.9–1.0.13 foundations into lists labeled as future work.
2. the new Shoulder Strap battery-pouch / upgrade design was not yet frozen despite depending directly on the delivered 1.0.13 shoulder attachment;
3. `WAITLIST-1.2.0.md` only described direct hologram manipulation and did not yet contain the UV ecosystem that was explicitly moved out of 1.1.0.

## Frozen release boundary after audit

### 1.1.0 remains about portable illumination / projection / capture

Delivered foundations already present in 1.0.x:

- Dynamic Mirage Light runtime;
- rechargeable Glow Dust and Light Battery;
- placed Mirage Light Projector;
- handheld Mirage Lantern;
- handheld Mirage Projector;
- persistent active portable projectors from inventory;
- Creative Battery;
- Mirage Equipment attachment;
- Arm Strap + Shoulder Slot foundation.

Still required for 1.1.0:

- final Focus/Flood/Ambient gameplay balance and many-emitter QA;
- final Glow Dust / Light Battery recipes and progression;
- Shoulder Strap Battery Pouch + upgrade sockets;
- Auto Battery Swap upgrade;
- Battery Pouch Expansion upgrade;
- final Shoulder equipment UI/layout/physical placement polish;
- War Banner presentation (`Directional` + `Always Face Viewer`);
- optional shoulder-device skin system / first bird disguise;
- Mirage Scan Codex;
- physical scan duplication station / Duplicating Lectern;
- horizontal/table portable projector family work;
- wall/data-show/presentation projector work;
- final source/chassis capability enumeration and anchor semantics;
- End Resonance / Dragon Egg Field+Prism behavior;
- final recipes/docs/QA/release polish.

### 1.2.0 owns UV and direct interaction

The following are explicitly **not 1.1.0 work**:

- UV Shoulder Light;
- UV mode and Auto UV upgrade;
- undead / Smite-vulnerability detection;
- UV damage/fire/slow behavior;
- solar suppression of Auto UV;
- UV Marks / hidden symbols / letters / arrows;
- UV marking material / authoring station;
- Crying Obsidian Cluster UV reveal/resonance behavior;
- direct grab/free-rotate hologram manipulation.

This keeps 1.1.0 focused and prevents the shoulder-upgrade architecture from accidentally implementing UV-specific assumptions early.

## Shoulder Strap inventory contract frozen before 1.0.14 coding

The Arm Strap is the owner of its battery-management subsystem.

Base Strap inventory:

- 1 Shoulder Device slot (already delivered in 1.0.13 player attachment);
- 6 Battery Pouch slots accepting only `RechargeableEnergyItem`;
- 2 upgrade sockets in the base strap panel.

Planned 1.1 upgrades:

1. **Auto Battery Swap Patch** — when the mounted device's installed rechargeable cell reaches 0, exchange it for a charged cell from the pouch and return the depleted cell to the pouch atomically. Never delete/drop a cell silently.
2. **Battery Pouch Expansion Patch** — expands battery storage from 6 to 9 slots and unlocks a third generic upgrade socket. This reserves enough architecture for a future 1.2 Auto UV upgrade without another attachment-format redesign.

Upgrade rules:

- each upgrade item declares its upgrade family/type;
- at most one upgrade of a given family may be installed;
- base sockets must not accept duplicate families;
- removing the Expansion Patch is blocked while any expansion-only battery slot or the third upgrade socket is occupied;
- battery pouch slots accept full/partial/depleted Glow Dust, Light Battery, Creative Battery and future `RechargeableEnergyItem` implementations;
- auto swap ignores 0%-charge candidates;
- auto swap is available only to the device mounted in the Shoulder Slot, not an arbitrary handheld/inventory device.

## Best next implementation wave

The cleanest next wave is the Shoulder Strap Battery Pouch + generic upgrade foundation because it:

- extends the just-delivered 1.0.13 attachment instead of starting an unrelated subsystem;
- directly supports long-duration lantern/projector use;
- gives 1.1 a concrete progression/utility feature;
- reserves one expansion upgrade socket for later 1.2 UV without implementing any UV logic now;
- creates the generic shoulder-upgrade contract required before richer shoulder-device behavior.
