# Mirage Projector 1.0.30 — UX / Runtime Interaction Wave

1.0.30 is the second broad runtime-QA pass after the 1.0.24–1.0.29 feature wave. It advances the custom payload protocol to **40** because the Mirage Hand Projector gains explicit portable source-mode actions. `ProjectionSettings` remains format **4**.

## Mirage Hand Projector

The portable GUI is now a real compact source workspace rather than a battery shell around Copy Target. Hand Projector exposes Image, Item, Entity and Banner modes. Item, filled Entity Scan Card and Banner sources use a non-consuming virtual source well stored inside the portable projector profile; Image remains sourced through Copy Target Projector. Stored source families survive mode changes. The battery/source wells and labels are compacted and aligned, while War Banner presentation/facing/size/height controls remain conditional.

## Mirage Equipment / Shoulder Strap

The external Shoulder panel now captures both the press and matching release for its pseudo-slots, preventing vanilla `AbstractContainerScreen` outside-click release handling from dropping extracted equipment on the ground. The toggle is visually docked to the inventory frame and the expansion no longer displays a redundant `+3` label; the extra slots themselves communicate the upgrade.

## Wall/Data-show live placement

Wall Scale, X Offset and Y Offset publish a live server preview while the GUI stays open. Apply commits the current preview as the new baseline without closing the screen. Cancel restores that baseline and also stays open. Turn Off and Unpair Remote share the header row, Active Source has its own non-overlapping line, and the Presentation Order grid starts below its label.

The Presentation Remote now centers the native mouse cursor using raw window-pixel dimensions instead of GUI-scaled coordinates and uses a wider neutral dead-zone, so an accidental RMB does not immediately choose Previous on non-1 GUI scales.

## Entity workspace / Table projector

Use Entity Mode now reopens the Entity workspace after the server-authoritative source change so Table and other chassis rebuild the client menu from the updated projector state. Projected/Visibility columns and buttons are pulled inside the workspace bounds.

## Scan Codex and Entity Scan Card

The Codex book canvas renders exactly once from the container background pass while the vanilla blur/dim path remains disabled. Library filters/search occupy the left page and the scrollable capture list occupies the right page, keeping controls away from the book spine. Lectern Import/Duplicate extensions use compact labels and padded slot frames. Successful destructive import behavior is unchanged.

Entity Scan Card now has a dedicated 16×16 Mirage pixel-art texture instead of inheriting vanilla Paper presentation.

## Validation

Static gates must preserve language parity, the no-blur Codex contract, portable source persistence, the Shoulder press/release capture, Data-show live preview, Entity mode refresh and the custom Entity Scan Card asset. Windows NeoForge `build.bat` remains the authoritative build-clean gate.
