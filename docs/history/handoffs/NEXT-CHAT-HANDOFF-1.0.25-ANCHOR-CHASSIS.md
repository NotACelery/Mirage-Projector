# Mirage Projector 1.0.25 — Anchor Chassis Handoff

Baseline: **1.0.25**  
Minecraft: **1.21.1**  
NeoForge: **21.1.244+**  
Network protocol: **37**  
ProjectionSettings format: **3**

## Delivered

- `mirage_projector:mirage_table_projector` and `mirage_projector:mirage_wall_projector`.
- `ProjectionChassisProfile` anchor + placement-capability contract.
- Table: sturdy support below, horizontal Image/Banner plane, upright Item/Entity volume, packed Shift+empty-hand pickup, packed support-loss drop.
- Wall: horizontal wall-only placement, outward anchor, Lift/Tilt only, packed support-loss drop.
- Renderer, clearance, Projection Power and settings UI consume the same chassis capability contract.
- First-pass block/item models, loot, localization and Creative QA exposure.
- No Survival recipes frozen yet.

## QA pending

Windows `build.bat` and in-game QA remain authoritative. Check all N/E/S/W wall facings, support loss, packed state round trips, Table plane/volume orientation, GUI tab gating and regression of the six historical chassis.

## Next implementation candidates

1. Wall/Data-show ordered generic-source slide deck.
2. Optional Create Blueprint source bridge once source capability enumeration is used by the UI.
3. End Resonance Field/Prism behavior.
4. Final Table/Wall art and Survival recipes after runtime QA.
