# Mirage Projector — handoff after dev.49

Current source candidate: **0.1.0-dev.49**.

- dev.48 was rejected: startup crash from `CryingObsidianGrowthMixin` targeting nonexistent `Block#randomTick` declaration.
- dev.49 removes that mixin and moves nucleation into the existing `BlockBehaviour.BlockStateBase` mixin.
- `isRandomlyTicking` and `randomTick` are now handled at the same state-dispatch layer.
- All dev.47 visual/UI fixes remain carried forward.

Pending: user-side Windows build/startup and in-game nucleation QA.
