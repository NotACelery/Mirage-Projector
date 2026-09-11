# Mirage Projector — Scope consolidation handoff

Baseline code: **0.1.0-dev.75d**, protocol **21**. This handoff is documentation-only; no runtime code changed.

## Current immediate gate

Run dev.75d Windows/in-game acceptance. Static light architecture should not be reopened unless concrete measurements fail.

## Before stable 1.0.0

Work from `WAITLIST-1.0.0.md`. The critical architectural objective is not to implement 1.1 features early; it is to remove hard-coded assumptions that would make them expensive:

- extensible projection source registration;
- central chassis capabilities;
- source content separated from transform;
- quaternion-ready transform persistence;
- renderer and future interaction provider boundaries;
- static vs dynamic Mirage Light backends;
- energy boundary not tied exclusively to Core slots;
- safe unknown/addon source persistence.

## 1.1.0 headline

Portable Illumination, Capture & Projection Expansion:

- dynamic/mobile light;
- Focus/Flood/Ambient/Off lantern modes;
- rechargeable Glow Dust, depleted state, Beacon/Core-Booster charging;
- Scan Codex with multiple distinct snapshots of the same entity type;
- Duplicating Lectern / Paper -> Entity Scan Copy/Card;
- handheld portable projector (no Blueprint);
- horizontal/table/ceiling-capable projection;
- wall/data-show presentations and generic slide sources.

## 1.2.0 headline

Interactive Holograms: hold RMB to grab and freely rotate projected content using a generic transform/manipulation system.

## Optional Create bridge

A separate addon registers Blueprint as a projection source. Horizontal/table projectors are the primary full-3D schematic viewer. Wall/data-show projectors can present 2D-face Blueprint slides with rotate + zoom + layer removal. Handheld projectors do not support Blueprint.
