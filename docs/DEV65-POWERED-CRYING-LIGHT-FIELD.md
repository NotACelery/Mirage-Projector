# Mirage Projector — dev.65 powered Crying Obsidian light field

## Goal

Increase the useful illumination radius of an energized Mature Crying Obsidian Cluster without claiming impossible block-light values above Minecraft's normal level-15 range and without replacing the global light engine.

## Relay-to-light mapping

The source reads the effective Core Booster relay between the active Beacon and the Mature Cluster. The same four-effective-Booster cap used by Beacon rendering applies.

Field tier is derived from two inputs:

- relay width gain contributes up to two tiers;
- each effective Quartz/Radiance Booster contributes one additional tier;
- final tier is clamped to 0–4.

This intentionally gives any wider relay some lighting consequence while making Radiance the dominant world-light modifier.

## Auxiliary field

The field uses an internal block ID, `mirage_projector:crying_light_node`. It is not a gameplay item. No BlockItem, recipe, loot table or Creative entry exists.

Three Manhattan-distance shells are sampled around the Mature source. Each shell uses six axis points and twelve face-diagonal points, for at most 54 candidate nodes around one source.

| Tier | Distance 8 | Distance 16 | Distance 24 | Target reach |
|---:|---:|---:|---:|---:|
| 0 | 0 | 0 | 0 | normal Mature light |
| 1 | 12 | 0 | 0 | ~19 blocks |
| 2 | 14 | 7 | 0 | ~22 blocks |
| 3 | 15 | 10 | 4 | ~27 blocks |
| 4 | 15 | 13 | 7 | ~30 blocks |

All values remain ordinary vanilla block-light levels. Longer range comes from distributed sources rather than values above 15.

## Safety rules

A node can only replace air. It has no collision or selection shape, is invisible, replaceable and drops nothing. A sampled route that crosses a fully light-blocking block is rejected, so the field does not blindly light through opaque walls.

Every node revalidates itself every 40 ticks. It checks whether any possible energized Mature source still requests that exact position and strength. If not, it removes itself. The Mature source also refreshes its field during the existing 20-tick optics cycle, so stronger relay changes expand quickly and weaker relay changes are corrected by node validation.

Overlapping fields are resolved by taking the strongest currently valid requested light at a shared node location.

## Crystal presentation

Small, Medium and Large buds remain zero-block-light stages. Every energized crystal stage now uses emissive rendering, so Beacon excitation is visually readable before maturity.

Residual-ray frequency now receives a bounded excitation multiplier from relay width and Radiance, complementing the existing relay-driven ray width, alpha and rotation changes.

## Non-goals

- no global custom light engine;
- no block-light value above 15;
- no mandatory external lighting mod;
- no automatic replacement of solid terrain or water;
- no growth-speed changes;
- no change to the dev.64 Entity/Create Ghost branch.
