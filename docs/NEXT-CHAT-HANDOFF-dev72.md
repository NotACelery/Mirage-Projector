# Mirage Projector — next-chat handoff dev.72

Current source candidate: **0.1.0-dev.72**. Network protocol: **19**.

dev.72 is the first P2 renderer/entity hardening pass after dev.71 equipment visibility. It restores projected nameplates above the actual Lift/Float/species/pose/equipment-aware Entity envelope, makes them inherit Tint/Ghost opacity, adds conservative equipment extents to shared `EntityProjectionBounds`, unions the physical chassis with the entire projection/nameplate BER AABB, and enables vanilla frustum culling by returning false from `shouldRenderOffScreen`. No LOD/quality reduction is introduced.

Required QA before build-clean:

- Windows Java 21 build;
- nameplate height at low/high Scale, Lift and Floating;
- nameplate Tint/Ghost 100/90/50/10%;
- high-Lift case where only the projection is in frame;
- screen-edge culling with visible Humanoid hand/chest/head gear and Horse Body Armor/Rearing;
- Create Netherite Backtank Ghost regression;
- dev.71 visibility persistence/multiplayer QA;
- dev.70 wall/block/unblock/overlap light-field QA when the Simple Light Level backport is available.

Next implementation work: continue P2 with offline/frozen Player skin reconnect/cache hardening and overlap/deferred-render stress instrumentation. Add mod-specific accessories/backpack adapters only after a concrete incompatibility is reproduced.
