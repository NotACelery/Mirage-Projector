# Core Booster and Projector Upgrades — current through 1.0.18

## Core Booster

There is one gameplay block/item:

```text
mirage_projector:core_booster
```

An empty Booster accepts one of:

- Glass
- Quartz
- Amethyst Shard
- Diamond
- Netherite Ingot

The loaded material is stored as block/item state. Shift + right-click returns the material. Correctly mined Boosters preserve their loaded material; empty and differently loaded Boosters do not silently merge into one state.

As a projector Core, a loaded Booster maps to the corresponding improved Core profile and provides ×1.50 amplification over the base material output.

### Rechargeable-media cradle

The Booster also has a separate one-cell charging cradle. It accepts supported `RechargeableEnergyItem` media and does not replace the material/Core socket. Current normal media are Glow Dust (1000 units) and Light Battery (4000 units). The historical Booster world interaction remains separate from the 1.0.18 portable-device GUI contract: portable Lantern/Hand Projector batteries are serviced only through their own screens.

A valid live Beacon column charges the inserted cell. An actively charging cell subtracts 0.20 absolute transmission from the outgoing beam; a full cell does not attenuate it. **At 100%, custom rechargeable Glow Dust normalizes back to vanilla `minecraft:glowstone_dust`** so it regains normal vanilla crafting/brewing identity; partial/depleted charge remains represented by the Mirage rechargeable item. The persisted block-entity key remains `ChargingGlowDust` solely for 1.0.7 save compatibility.

## Beacon / Crying Obsidian identities

The five materials remain behaviorally distinct:

- **Glass / Diffusion** — widens optical behavior and softens detour shadowing while trading straight-line static reach when unopposed.
- **Quartz / Radiance** — strongest pure static-reach amplifier and increases reflected brightness.
- **Amethyst / Resonance** — accelerates optical/residual activity without free static-world reach.
- **Diamond / Focus** — tightens/focuses the optical response and strengthens geometric shadowing; two effective Diamonds add one static reach tier.
- **Netherite / Inversion** — reverses optical rotation without granting free static reach.

At most four effective loaded Boosters participate in one Beacon relay calculation.

## Upgrade crafting

State-preserving projector progression:

```text
Mirage Projector
      ↓
Mirage Display
  ├─→ Wide Mirage Projector
  ├─→ Tall Mirage Projector
  ├─→ Mirage Prism
  └─→ Mirage Field Projector
```

The custom `projector_upgrade` recipe serializer transfers serialized projector state into the target chassis. Upgrade crafting must preserve relevant source content, presentation state and installed Core rather than returning a blank machine.

EMI and JEI receive explicit integration for these custom crafting recipes.

## Migration-only Improved Core IDs

Old `improved_*_core` block IDs remain registered only to load/migrate old development worlds. They have no BlockItems, recipes or Creative exposure and are not part of 1.0.0 gameplay progression.
