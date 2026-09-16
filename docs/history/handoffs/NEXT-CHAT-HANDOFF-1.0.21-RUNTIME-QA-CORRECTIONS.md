# Next Chat Handoff — Mirage Projector 1.0.21 Runtime QA Corrections

Baseline: **1.0.21**, Minecraft 1.21.1, NeoForge 21.1.244+, Java 21, network protocol 34, ProjectionSettings format 3.

1.0.20 is runtime-confirmed to start with Sodium and visibly illuminate terrain. QA exposed three follow-up regressions now corrected in 1.0.21:

1. Baked terrain/wall sections could remain stale because Mirage dirtied only sections owning changed light bytes. 1.0.21 adds full one-section halo invalidation for infrequent authoritative changes and boundary-aware pre/post aggregate-byte invalidation for frequent DYNAMIC_VISUAL sources.
2. Shoulder-mounted device rendering used `translate(-0.36, -1.32, 0.05)` and appeared near the feet. It now uses positive shoulder-height Y; the shoulder Lantern light origin is aligned to the right shoulder.
3. Hand Projector non-War-Banner output was silently rejected by the fixed-projector physical Core power gate. `MirageProjectorRenderer.renderPortableProjection(...)` now renders portable source content after `MirageHandProjectorItem.portablePowerAvailable(...)` validates the embedded battery/compact chassis.

Next runtime gate: build 1.0.21 and test stale-light convergence while walking/turning in caves, shoulder placement in third person, and Hand Projector Image/Item/Entity/Banner output from hand, inventory and Shoulder Device.
