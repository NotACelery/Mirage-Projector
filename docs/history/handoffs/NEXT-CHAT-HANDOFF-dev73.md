# Mirage Projector — next-chat handoff dev.73

Current source candidate: **0.1.0-dev.73**. Network protocol: **19**.

## What changed

Live Simple Light Level QA on the dev.72 accumulation showed the Mature half-decay field was only doubling light levels consistently around 14–11. Level 15 and the 10–1 tail were not held for two positions. dev.73 replaces the sparse odd-distance axial relay lattice with an explicit relay at every accepted axial air cell.

Zero-Booster expected primary-axis sequence at distances 1–30 is exactly `15,15,14,14,...,2,2,1,1`. The old ~29-block wording is superseded by a 30-block explicit tail. Four effective conceptual Booster tiers can extend candidate reach to 38 blocks while real node levels remain capped at 15.

All dev.70 occlusion/same-tick invalidation behavior remains accumulated: an opaque blocker cuts the entire downstream branch, partial blockers attenuate, Glass has no long diagonal static nodes, and overlapping sources reconcile strongest surviving contributions.

## QA still required

1. Windows Java 21 `build.bat`.
2. Numeric block-light QA of the full 15→1 sequence, especially 15 and 10→1.
3. Opaque wall place/remove same-tick response with the denser lattice.
4. Quartz and Diamond range/persistence comparison.
5. Several/overlapping Mature fields for server hitching.
6. dev.71 equipment visibility and dev.72 Entity high-Lift/frustum/nameplate regression checks remain open.

## Next implementation work while lighting QA runs

Continue P2 renderer/entity hardening: frozen/offline Player skin behavior under reconnect/cache conditions, then translucent overlap/deferred depth stress. Do not add broad accessory compatibility adapters until a concrete mod fails the generic Ghost path.
