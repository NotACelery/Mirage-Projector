# Mirage Projector — Image/GIF asset pipeline

## Supported import formats

Static:

- PNG;
- JPG/JPEG;
- WebP;
- BMP.

Animated:

- GIF.

Format detection uses file contents rather than filename extension.

Animated WebP and APNG are detected and rejected explicitly instead of silently flattening into a static image.

## Import limits

Current source rules:

- input file hard cap: 32 MiB;
- normalized/transferred asset cap: 8 MiB;
- static maximum dimension before normalization: 2048 px;
- static assets normalize to ARGB PNG;
- static images can be resized downward until they satisfy the normalized-size cap.

## GIF safety limits

- maximum stored asset: 8 MiB;
- maximum canvas dimension: 1024 px;
- maximum frames: 128;
- maximum decoded frame-pixel budget: 16,777,216 pixels;
- minimum effective frame delay: 20 ms;
- zero-delay fallback: 100 ms;
- maximum individual delay: 10 seconds;
- maximum loop duration: 5 minutes.

GIF disposal behavior is reconstructed into complete ARGB frames for rendering.

## Identity

Asset identity is lowercase SHA-256 over the normalized bytes for static images and the retained GIF bytes for animated GIFs.

A valid Mirage asset ID is exactly 64 lowercase hex characters.

## Local cache

Client cache location:

```text
<game directory>/mirage_projector/cache/<sha256>.asset
```

Old `.png` cache paths remain only where migration compatibility still requires them.

## Multiplayer transfer

Network chunk size: 32 KiB. Maximum chunk count is derived from the 8 MiB asset cap.

Upload/download sessions expire after 30 seconds of inactivity. Server upload safeguards currently include:

- maximum 4 active uploads per player;
- maximum 64 active uploads globally;
- per-chunk and total-byte validation;
- SHA-256 validation before accepting storage.

The server stores imported assets and other clients request missing hashes automatically.

## Playback

GIF frame selection is time-based from stored frame delays/loop duration. All projector chassis use the same content-addressed asset identity; layout/chassis changes do not create duplicate image files.

## Pending hardening

Before 0.1.0, run multiplayer latency and large-asset stress tests, repeated cache-delete/refetch tests and long-running animated GIF tests. Add server/admin limits only if actual performance QA shows they are needed.
