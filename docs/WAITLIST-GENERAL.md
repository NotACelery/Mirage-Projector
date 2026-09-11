# WAITLIST — General / cross-version

This file contains work that is architectural, compatibility-oriented or cross-version. Release-specific work belongs in `WAITLIST-1.0.0.md`, `WAITLIST-1.1.0.md` or `WAITLIST-1.2.0.md`.

## Extension architecture

- Replace closed assumptions about projection content with a **registered projection-source contract** identified by stable namespaced IDs. Current Image/Item/Banner/Entity become built-in registrations, not the only legal enum forever.
- Make workspace/tab availability derive from **projector capabilities + registered source types**, so an addon can add `BLUEPRINT` without patching every screen.
- Keep **content data** separate from **presentation transform**. Image bytes, Entity snapshot and future Blueprint structure data must not own Scale/Lift/orientation/ghost/tint state.
- Keep a generic transform representation that is **quaternion-ready** even if 1.0.0 UI exposes only the existing orientation controls. 1.2.0 free manipulation must not require rewriting every serialized projection format.
- Separate projection rendering from input/manipulation. A future interaction controller should be able to grab an already-rendered projection without each source implementing mouse math independently.
- Define chassis capabilities centrally: fixed/mobile, 2D/3D, supported source families, slideshow support, dynamic-light support, interaction support. Avoid scattered chassis-name conditionals.
- Keep Mirage Light Engine source/profile ownership independent of Crying Obsidian. Static Mature Cluster remains one consumer; moving lanterns/projectors are later consumers.
- Keep energy semantics extensible: fixed projectors use Core/PU; later portable devices may consume rechargeable batteries. Do not make "has a Core slot" synonymous with "all Mirage devices obtain energy this way".
- Preserve state-transfer/versioning contracts so future chassis upgrades or addon content do not silently discard unknown projection-source data.

## Renderer / compatibility backlog

- Target compatibility adapters only where generic Entity/Ghost rendering cannot safely cover a mod.
- Continue species/renderer-specific bounds refinements only from concrete QA failures.
- Stress overlapping translucent projections, water/cloud/deferred rendering and glint/modded armor.
- Verify frozen/offline Player skin behavior through cache/reconnect scenarios.
- Mounted/passenger composite projections remain a separate design problem; do not silently permit them until root/passenger relative transforms, equipment ownership, bounds and ordering are defined.

## Quality / maintenance

- Keep reusable regression suites instead of one-off bug tests.
- Multiplayer asset/GIF stress, save/reload, migration, render-performance and old-world compatibility remain recurring release gates.
- Preserve source snapshots after meaningful implementation waves.
- Historical docs may describe superseded behavior; active authorities must explicitly say when an architecture is migration-only or historical.
