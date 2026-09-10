# Mirage Prism — four-face static-image pass (dev.21)

## Scope

Dev.21 turns Mirage Prism from a reserved GUI/chassis contract into a functional static-image projector. It is still deliberately limited to Image sources in this pass; Banner and animated sources remain separate future work.

## Face contract

Prism owns four independent lateral faces:

- North
- East
- South
- West

There is no top face and no bottom face. The projection must remain four quads around an open square rather than becoming a translucent cube.

For save/network compatibility with the existing Plane model:

- existing Front asset storage becomes Prism North;
- existing Back asset storage becomes Prism South;
- East and West use new persistent asset fields.

Older saves therefore migrate naturally with East/West empty. Plane semantics are unchanged: Front/Back plus Mirrored/Readable/Independent remain Plane-only controls.

## Image Workspace

When the active chassis is Prism, Image Workspace exposes four preview cards and four independent import/clear actions. `Same Source on All Faces` copies North's SHA-256 asset ID and dimensions into East/South/West. It does not duplicate normalized PNG bytes and it does not create four independent physical files.

Vertical Flip and Scanlines remain image-presentation controls shared by the whole Prism. Scale, Lift, Rotation, Floating, Lighting, Ghost and Tint remain global Projection Settings.

## World geometry

Each configured face renders its own aspect-ratio-preserving quad at the shared Prism radius. All faces are transformed by the same global Y rotation and bob/lift transform, so the four-face assembly moves as one object.

Missing/unconfigured faces remain open. A configured face whose asset is not yet available locally continues to participate in normal asset request/sync behavior.

## Power and clearance

Prism power is provisional but geometry-aware:

1. calculate the pixel area cost of every configured face;
2. sum those face costs;
3. add a small fixed four-face geometry overhead;
4. add the same Lift/Rotation/Floating/Fullbright/Scanlines costs used elsewhere.

The chassis envelope uses the largest configured face width/height for capability validation. Clearance treats Prism as a square lateral volume. When rotating, it reserves the swept square radius rather than only the current Plane-like thickness.

## QA matrix

Before marking dev.21 build-clean, test at least:

1. North only; East only; South only; West only.
2. Two opposite faces and two adjacent faces.
3. All four faces with different images.
4. All four faces with very different aspect ratios.
5. `Same Source on All Faces`, then replace/clear one face independently.
6. Save world, reload world, verify all four IDs/dimensions persist.
7. Multiplayer: import on one client and verify other clients request/render every face.
8. Rotation disabled and enabled; verify physical face order stays N→E→S→W as one assembly.
9. Lift/Floating plus clearance near walls/ceilings.
10. Vertical Flip, Scanlines, World Light/Fullbright, Tint and Ghost Effect.
11. Plane after Prism changes: Front/Back picker names and Back Mirrored/Readable/Independent must remain unchanged.

## Explicitly deferred

- Banner source/editor/renderer.
- Banner per Prism face.
- GIF/animated image transport and playback.
- Multi-source layouts for Wide/Tall/Field.
- Final Prism block model/textures and survival recipe.
