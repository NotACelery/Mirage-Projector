# Mirage Projector — next-chat handoff dev.67

Current source candidate: **0.1.0-dev.67**. Network protocol remains **18**.

## Live QA entering dev.67

- Create Netherite Backtank no longer crashes and now fades completely when Ghost transparency is enabled.
- Buds correctly intercept the Beacon beam, but continuation previously resumed too high.
- Relay/post-bud custom beam geometry could appear as an N/X because the custom side topology contained a diagonal connection.
- dev.65/66 powered lighting was not visibly extending because only Mature + modified relay could create nodes.
- Residual crystal rays were no longer visible because their pixel-2 origin lived inside the source crystal collider and the collision ray could immediately hit the source itself.

## dev.67 changes

- Core Booster optical effect plane: `8.5/16` inside its own block.
- Small/Medium/Large continuation: `4.5/16`, `6.5/16`, `8.5/16`.
- Mature vertical transmission stays 0.
- Custom beam quad topology matches vanilla.
- Residual collision skips only the source crystal volume while preserving the pixel-2 visual origin.
- Powered world light: Small/Medium/Large remain 0; only Mature emits 15 and drives the half-decay field.
- Only energized Mature uses the Mirage-owned half-decay field; baseline pattern along supported axes is approximately `5,5,4,4,3,3...`.
- Relay tier can extend the conceptual field but all actual nodes remain <= 15.
- dev.66 advanced light-profile placeholders remain disconnected.

## Required QA

1. Run `build.bat` on Windows/Java 21.
2. Compare a plain Beacon beam with a Booster beam from several rotations; ensure no new N/X diagonal face is visible.
3. Verify the Booster transition visibly occurs at the nested core plane, not the block bottom.
4. Verify Small/Medium/Large continuation heights against their actual textures.
5. Verify residual bursts visibly occur for all energized stages and still clip against later obstacles.
6. At night, measure source-light falloff along an unobstructed axis and confirm approximately repeated levels.
7. Remove the Beacon/crystal and confirm nodes self-clean.
8. Recheck the Create Backtank full-fade regression.

No build/cleaner tooling change is required in this version because no file was deprecated or moved.
