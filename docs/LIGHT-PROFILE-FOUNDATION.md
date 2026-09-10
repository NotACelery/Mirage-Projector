# Mirage Projector — Light profile foundation (dev.69 authority)

## Runtime boundary

`VANILLA` and `EXTEND` are the only light modes currently allowed to mutate world lighting. `CONCENTRATE`, `DIRECTIONAL_SPOT` and `ROTATING_DIRECTIONAL_SPOT` remain reserved placeholders. They have profile/schema and pure math support where useful, but they do not place, remove or update world-light nodes.

## Mirage-owned node contract

The active Crying Obsidian field uses Mirage-owned `mirage_projector:crying_light_node` blocks rather than anonymous `minecraft:light` blocks. This preserves ownership, allows self-validation, avoids deleting operator/mod light blocks and lets overlapping Mirage sources resolve to the strongest currently valid requested level.

Nodes are internal only: no BlockItem, recipe, loot or Creative exposure. They may replace only air or another Mirage light node, are limited to loaded chunks/build height and remain capped to vanilla block-light level 15.

## Current Mature Cluster EXTEND field

An energized Mature Crying Obsidian Cluster always has the slow-decay baseline. No Core Booster is required. Six primary axial branches sample approximately one node per two blocks, giving the intended `...,5,5,4,4,3,3,2,2,1,1` persistence while each real node remains at level 15 or below. Base axial reach is approximately 29 blocks.

The candidate topology is deliberately bounded. Axial candidates extend only to the maximum conceptual reach supported by four effective Boosters. Glass/Diffusion can additionally activate twelve face-diagonal branch directions, but those secondary branches use a shorter/dimmer budget and are prebounded to 25 blocks maximum candidate distance. Body-diagonal/full-cube sampling is intentionally rejected.

## Occlusion and source causality

A Mirage relay node is not allowed to appear merely because its coordinate is in range. The source-to-candidate path is sampled first. Fully light-blocking geometry rejects the candidate completely; partially light-blocking geometry adds attenuation on top of the slow distance decay. Existing Mirage nodes on the path do not count as blockers.

This prevents a downstream node from teleporting a fresh source behind an opaque wall. Mirage does not attempt to encode a light direction per voxel or manually paint block faces: Minecraft's block-light propagation, corner wrap, ambient occlusion and renderer face shading remain responsible for the local result around accepted nodes.

Removing/de-energizing a Mature source immediately reconciles every candidate in its influence while excluding that source. Shared nodes survive or downgrade to the strongest remaining contribution; orphan nodes disappear immediately. Each node also keeps periodic self-validation as a recovery path.

## Core Booster reflection semantics

The incoming Beacon still keeps dev.60's width/brightness/rotation modifiers, but reflected Crying Obsidian light no longer derives static range from generic `widthScale`.

| Material | Reflected world-light consequence | Reflected visual consequence |
|---|---|---|
| Glass / Diffusion | activates bounded face-diagonal coverage; Diamond Focus cancels diffusion tier-for-tier | broader residual ray body and slightly longer/more frequent bursts |
| Quartz / Radiance | dominant conceptual-light/range reinforcement across reflected branches | brighter, longer and more frequent residual rays |
| Amethyst / Resonance | no free static range | faster residual cadence/rotation through the existing resonance speed |
| Diamond / Focus | smaller focused axial reach bonus; counteracts Glass diffusion | tighter residual beam with modest extra length |
| Netherite / Inversion | no free static range | reverses reflected ray rotation |

The four-effective-Booster cap remains authoritative. Quartz is intentionally stronger than Diamond as a world-light range amplifier; Focus trades breadth for axial persistence rather than becoming a second Radiance.

## Performance rule

Per-source refresh resolves the Beacon relay once, builds one immutable field specification and reuses it for every candidate. Candidate generation stays on fixed rays; full radius-cube scans are prohibited for ordinary or dynamic profiles.

Overlap revalidation can still inspect the bounded inverse candidate set because light nodes intentionally have no BlockEntity/owner payload. If future profiles multiply candidate counts significantly, introduce a stronger source-index/ownership structure before expanding topology.

## Reserved concentrated decay

A faster-than-vanilla profile cannot be achieved by adding weaker positive nodes around a source that still emits normally. Positive block-light contributions cannot subtract the source's existing propagation.

A future `CONCENTRATE` mode therefore requires source-emission suppression/replacement before its Mirage-owned field becomes authoritative. `LightProfileMath.concentratedLevel(...)` remains pure math only.

## Reserved directional spotlight

A future `DIRECTIONAL_SPOT` must likewise suppress the source's ordinary omnidirectional contribution and generate only a bounded cone/ray lattice. `LightProfileMath.insideDirectionalCone(...)` provides the dot-product predicate but performs no world mutation.

## Reserved rotating directional spotlight

`ROTATING_DIRECTIONAL_SPOT` records direction, refresh cadence and rotation period only. When implemented, server-side world-light updates must change only the delta between old/new bounded candidate sets. Smooth visual rotation may remain client-side and must not imply per-render-tick block mutation.
