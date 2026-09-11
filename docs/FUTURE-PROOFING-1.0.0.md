# 1.0.0 future-proofing contract for 1.1/1.2/addons

This is the practical checklist to apply before freezing 1.0.0 internals.

## 1. Projection source != GUI enum

A projection source should be addressed by a stable namespaced ID and provider/codec/renderer contract. Built-ins register themselves. Optional addons may register another source. Screens enumerate compatible registrations instead of assuming exactly Image/Item/Banner/Entity.

## 2. Chassis capability != class-name switch

Central capability data should answer questions such as:

- 2D / 3D support;
- fixed / mobile;
- supported projection-source IDs;
- slideshow/playlist support;
- dynamic-light support;
- direct-manipulation support;
- maximum intended transform envelope.

This allows 1.1 handheld to reject Blueprint while horizontal/wall projectors accept different Blueprint presentations.

## 3. Content != transform

`ProjectionContent` owns what is shown. `ProjectionTransform` owns where/how it is shown. Scale/Lift/orientation/float/rotation do not belong inside Entity/Image/Blueprint payloads.

Persist transform in a format that can grow to arbitrary quaternion orientation. 1.0 UI does not need to expose free quaternion editing.

## 4. Rendering != interaction

Renderer supplies bounds/hit envelope. Interaction controller performs grab/rotate/manipulate later. 1.2 should plug a controller into the same projected envelope rather than implement separate interaction per source.

## 5. Static light != moving visual light

`MirageLightSource/Profile` remains feature-independent. Static authoritative world light and future moving visual light use distinct backends/lifecycles. Never make lantern movement trigger static field rebuilds every frame.

## 6. Core PU != universal energy assumption

Fixed projectors can continue using Core/PU. Define device energy consumption at a boundary where 1.1 portable devices can be backed by Glow Dust charge. Do not force a fake Core slot into every portable device merely because 1.0 fixed projectors use one.

## 7. Presentation deck != image playlist

When slideshow arrives, each slide should point at a generic compatible source descriptor. Images are one source. An addon Blueprint slide can then participate without rewriting the deck format.

## 8. Preserve unknown registered data

State transfer/upgrades should preserve unknown namespaced source payloads where possible. Missing provider: fail closed/placeholder, not destructive delete. This is especially important for worlds opened temporarily without the optional Create bridge.

## 9. Version codecs/network explicitly

Projection-source payload and transform evolution must be versioned. Avoid relying on Java enum ordinal for persisted extensible types.

## 10. Do not over-implement future UI in 1.0

The goal is stable seams, not speculative menus. 1.0.0 should remain understandable and releasable even though its contracts are ready for 1.1/1.2.
