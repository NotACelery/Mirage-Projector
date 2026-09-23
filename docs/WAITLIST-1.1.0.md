# WAITLIST — 1.1.0 Portable Illumination, Capture & Projection Expansion

This file contains only work that remains before 1.1.0. Delivered 1.0.x behavior belongs in `CURRENT-IMPLEMENTATION.md`, `ROADMAP.md` and `CHANGELOG.md`.

## Release blockers

### Dynamic Mirage Light

- Freeze final Focus/Flood/Ambient balance and charge rates.
- Invalidate stationary dynamic fields when nearby block geometry changes.
- Complete multiplayer and render-performance QA with many simultaneous moving emitters.
- Add visible yaw and pitch aiming to the floor Mirage Light Projector.

### Projection chassis and sources

- Freeze the formal anchor/pivot contract for portable, wall, table and any future ceiling chassis.
- Decide whether Table covers ceiling mounting or whether ceiling projection needs a dedicated chassis.
- Make source selection enumerate registered compatible sources and capabilities instead of assuming the four built-in families.
- Freeze the optional Create Blueprint bridge contract. Table is the preferred 3D host; Wall may expose a presentation source; Hand Projector must reject it.
- Complete final Table, Wall and Charging Station art polish.
- Decide whether slideshow transition effects are required for 1.1.0 or remain later polish.

### Portable energy and equipment

- Freeze rechargeable-cell-to-PU drain balance for portable holograms.
- Decide whether battery-backed holograms emit Mirage illumination; do not add it without explicit intensity, profile and synchronization rules.
- Complete migration, death/`keepInventory`, multiplayer and performance QA for packed Shoulder Straps.

### Scan Codex

- Complete dedicated Codex art polish.
- Run scale and performance QA with large persistent libraries.
- Keep reverse Mirage-to-Easy-Mob-Farm export deferred unless it receives a separate accepted design; any future cost is measured in experience levels, not raw XP.

### End Resonance release QA

- Test Dragon Egg insertion, removal, save/reload, chunk reload, restart, destruction, automation extraction and full-inventory fallback without loss or duplication.
- Test simultaneous multiplayer crossings, cooldowns, safe destinations and client cleanup after teleport or projector removal.
- Test players, mobs, items, projectiles, Primed TNT, falling blocks and only those vehicle/passenger combinations supported safely by vanilla transfer rules.
- Preserve the discovery-first changelog teaser: **Some projections may have an End after all...**

### Release integration

- Finish localization parity, public documentation, compatibility smoke tests and old-world migration QA.
- Add new Survival recipes only when remaining 1.1 work introduces a new Survival item.

## Optional 1.1 polish

- Shoulder-device cosmetic skin support, including the proposed dyeable bird disguise.
- Additional presentation transitions that do not alter the frozen deck contract.

## Explicitly deferred to 1.2.0

- Direct grab/free-rotate hologram interaction.
- UV Shoulder Light, Auto UV, UV combat behavior, UV Marks and UV authoring.
- Crying Obsidian UV reveal/resonance behavior.

The detailed 1.2 scope remains in `WAITLIST-1.2.0.md`. Cross-version maintenance remains in `WAITLIST-GENERAL.md`.
