# Next Chat Handoff — Mirage Projector 1.0.18 Massive Stabilization

## Baseline

- version: `1.0.18`
- Minecraft: `1.21.1`
- NeoForge: `21.1.244+`
- Java: `21`
- network protocol: `34`
- `ProjectionSettings` format: `3`

1.0.18 is a QA-driven stabilization wave. It does not open a new feature family.

## Major delivered changes

### Portable devices

- Shared `PortableDeviceMenu` / `PortableDeviceScreen` for Mirage Lantern and Mirage Hand Projector.
- Real one-cell GUI battery slot; old opposite-hand battery servicing removed.
- Lantern: RMB opens GUI; Shift+RMB cycles `Off -> Focus -> Flood -> Ambient -> Off`; default is Off.
- Hand Projector: RMB toggles projection; Shift+RMB opens GUI; GUI owns battery, target-copy and War Banner controls.
- `shouldCauseReequipAnimation` suppresses battery-percentage re-equip flicker.
- continuous local actionbar refresh removed from runtime loop.

### Mirage Light Projector

- real block menu/screen and server-authoritative mode action;
- RMB opens GUI;
- battery service is GUI-only;
- old floating-battery renderer is no longer registered.

### Dynamic light

- directional cone uses voxel-volume intersection margin rather than voxel-center-only test;
- held Lantern source moves forward from player eye;
- Shoulder Lantern source moves forward from player;
- placed Light Projector source begins just outside projector chassis.

### Shoulder Strap rearchitecture

- display name is Shoulder Strap; registry ID remains `arm_strap`;
- player attachment now contains only the equipped Strap;
- Strap ItemStack owns device + 9 battery capacity + 3 upgrade capacity via `DataComponents.CONTAINER`;
- legacy 14-slot attachments migrate into packed Strap automatically;
- inventory extension appears on right;
- empty panel exposes only Strap slot;
- base Strap dynamically exposes device + 6 cells + 2 upgrades;
- Expansion dynamically exposes +3 cells + third upgrade; inactive positions do not render;
- device swaps keep previous device on cursor rather than dropping it;
- Shift-hover on a stored Strap lists packed contents.

### Rechargeable media

- completed custom Glow Dust normalizes into vanilla Glowstone Dust;
- vanilla Glowstone Dust is a full rechargeable source when inserted into Mirage devices;
- partial/depleted custom Glow Dust remains non-vanilla and therefore cannot satisfy vanilla crafting/brewing IDs;
- Glow Dust tooltip no longer has a full-charge line;
- Light Battery tooltip no longer exposes capacity/recharge explanation;
- Light Battery crafting hook averages exactly five Glow Dust charge fractions when the final recipe exists.

### Charging Station

- input whitelist: incomplete Glow Dust / Light Battery only; full media and Creative Battery rejected;
- enlarged screen/layout;
- physical inventory renderer for 4 input + active + 4 output positions;
- completed Glow Dust returns as vanilla Glowstone Dust.

### Scan Codex

- does not pause singleplayer;
- no vanilla blurred/dim background pass.

### Integrity/deprecated-code cleanup

- Java/runtime naming migrated to Shoulder Strap terminology while legacy registry IDs remain stable for world/save compatibility.
- removed dead `ShoulderDeviceScreen`, `ShoulderDeviceControlPayload`, `MirageLightProjectorRenderer`, `LightProfileMath` and periodic Hand Projector HUD-refresh code;
- removed explicit `EventBusSubscriber.Bus` selectors and registered client MOD-bus listeners from `MirageProjectorClient`;
- pruned known dead localization left by direct-cell gestures/debug prose/old GUI paths;
- added `verify_1_0_18_integrity_cleanup.py` and `docs/history/audits/1.0.18-INTEGRITY-CLEANUP-AUDIT.md`;
- War Banner presentation/facing buttons in the portable GUI now display their current selected values.

## QA priorities on Windows

1. Run `build.bat` under Java 21 before trusting the snapshot.
2. Confirm Lantern/Hand Projector/Light Projector menus open and battery slots synchronize both directions.
3. Confirm Lantern Shift+RMB sequence and absence of once-per-percent hand re-equip animation.
4. Confirm Focus/Flood/Ambient actually create stable dynamic light over arbitrary yaw/pitch, not only special angles.
5. Open inventory with/without Shoulder Strap, install/remove Expansion and confirm slots appear/disappear dynamically on the right.
6. Pack a Strap with batteries/upgrades, remove it, inspect Shift tooltip, store/relog and re-equip it; contents must remain.
7. Upgrade an existing 1.0.17 player with populated shoulder attachment and verify migration does not lose device/cells/upgrades.
8. Swap Shoulder Devices with another device on cursor; old device must remain on cursor.
9. Charging Station must reject full/Creative cells, render its three inventory regions and keep logistics behavior from 1.0.15.
10. Charge custom Glow Dust to completion; output must become vanilla Glowstone Dust and regain normal vanilla crafting/brewing behavior.
11. Scan Codex must leave the world running and unblurred behind the panel.

## Intentional deferrals

- Final Light Battery survival recipe materials are still not frozen beyond the five-Glow-Dust charge-preservation requirement.
- Duplicating Lectern remains the next Scan Codex consumer rather than part of 1.0.18.
- UV Shoulder Light / Auto UV / UV Marks remain wholly reserved for 1.2.0.
- Bird/Parrot Shoulder Device skins remain later 1.1.0 polish.

## Verification

Run:

```text
python tools/verify_current_line.py
```

Expected static closeout: `MIRAGE PROJECTOR 1.0.18 VERIFICATION PASS (42 gates)` plus the 1.0.18 release audit at 113 JSON / 20 blocks / 26 items / 577 language keys.

No real NeoForge compile was available in the implementation environment. Static verification must not be represented as a successful game build.
