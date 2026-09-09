# Banner Mode — dev.28

## Scope of this pass

`0.1.0-dev.28` adds Banner as a first-class projection source without turning the projector into physical banner storage.

### Virtual source contract

- The real Banner `ItemStack` never leaves the player's inventory.
- Clicking/shift-clicking a banner into a Mirage banner source captures a one-count virtual copy containing its base colour, `BANNER_PATTERNS` data and other visual components.
- Clearing a source destroys only the virtual copy.
- Changing active source does not erase Image, Item, Entity or Banner data.

### Plane

- One virtual source: **Front**.
- The world renderer draws the vanilla banner cloth only; the pole and crossbar are intentionally omitted.
- Scale is interpreted as cloth height and preserves the vanilla 20×40 (1:2) cloth proportion.

### Mirage Prism

- Four independent virtual sources: **North / East / South / West**.
- `Same source on all faces` copies North's virtual snapshot to East/South/West.
- The four cloth faces rotate, float and lift as one Prism assembly.
- Power uses the number of populated Banner faces, rather than charging for four faces unconditionally.

### Shared presentation

Banner uses the same global Projection Settings contract as the other source families:

- Scale / Projection Lift
- Rotation / direction / orientation
- Floating
- Lighting
- Tint
- Ghost Effect

The existing projection-local Ghost buffer also covers the banner pattern atlas, so translucent Banner projections follow Mirage's no-depth-write rule.

## dev.27 carried into this snapshot

This source is cumulative over dev.27. Main/Off Hand item rendering now normalizes raw block/item RenderTypes through Mirage's colour-only Ghost pass so swords, tools, blocks, shields and ordinary held items do not write depth over water.

## Build / QA targets

Windows `build.bat` remains authoritative. After compile success, priority live checks are:

1. Held sword/tool/block with water behind it at several Ghost values.
2. Plain and patterned banners on Plane.
3. Banner base colours + several loom patterns.
4. Prism with one, two and four different banners.
5. `Same source on all faces` and reload persistence.
6. Tint/Ghost/Fullbright/Rotation/Lift/Floating on Banner.
7. Clearance and Power with 1 vs 4 Prism faces.
8. Verify the real banner never leaves the player inventory.

## Still pending after this pass

- Exact entity/model-part bounds for Horse/Generic and special renderers.
- Multi-source layout banks for Wide/Tall/Field.
- Final chassis art/crafts after geometry and Power QA.
- More special/modded RenderType Ghost/Tint hardening.
- A richer cloth-model preview inside Banner Workspace; dev.28 uses the real banner item preview while the world uses the cloth-only model.
