# Mirage Projector 1.0.7 — Rechargeable Glow Dust Foundation

1.0.7 begins the portable-energy gameplay loop planned for 1.1.0 while keeping protocol 28 and `ProjectionSettings` format 3.

## Glow Dust

- `mirage_projector:glow_dust` stores 0–1000 persistent charge units.
- Fresh/default dust is fully charged; partial/depleted charge lives on the ItemStack.
- Tooltip/status, item bar and charge-driven tint make battery state visible.
- Depleted dust remains the same item and can be recharged.
- No survival recipe is committed in this snapshot; Creative exposes full and depleted QA variants.

## Core Booster charging cradle

- Every Core Booster can hold one Glow Dust cell independently of its existing Core-material socket.
- Right-click with Glow Dust inserts one cell.
- Sneak + right-click with an empty hand removes the charging cell first.
- Breaking the Booster preserves the inserted Glow Dust in Survival.
- A live Beacon beam recharges the cell while it remains below full charge.

## Beam attenuation

- Each actively charging Glow Dust subtracts 0.20 from the outgoing beam transmission.
- On a clear column, five cells can charge simultaneously; the fifth reduces outgoing transmission to zero.
- Crystal transmission composes with Glow Dust attenuation.
- The custom Beacon renderer applies the same attenuation visually.
- Crying Obsidian optics use the attenuated beam, so charging can affect energized crystal behavior above the chargers.

## Balance baseline

The current implementation uses 1000 charge units and restores 10 units every 10 ticks, equivalent to 50 seconds from empty to full on an uninterrupted clear charging path. These are balance values, not a compatibility contract, and may change before 1.1.0.
