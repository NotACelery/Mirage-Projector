# Image format import contract — dev.39

Status: **authoritative for 0.1.0-dev.39+.**

## Core rule: content wins over filename

Mirage never selects an image decoder from the file extension. The importer reads the file bytes first and identifies its real container/signature. Renaming `animation.gif` to `animation.png` therefore does not flatten or misdecode it: it is still imported as GIF. Likewise, a static WebP renamed `.jpg` is still handled as WebP.

Supported in dev.39:

- static PNG;
- static JPEG/JPG;
- static WebP (bundled `webp-imageio` decoder);
- static BMP (Java ImageIO);
- animated GIF.

Static formats are normalized to ARGB PNG before hashing/storage. GIF is preserved byte-for-byte after bounded validation because flattening to PNG would destroy frame timing/disposal.

## Temporary animated-format failsafe

Animated WebP and APNG are deliberately detected but **rejected**, even if their extension has been changed to `.png`, `.jpg`, `.gif`, `.webp`, or anything else. Mirage never silently imports frame 1 as a static image. The UI reports the real detected format and asks the user to convert it to GIF or a supported static format.

Unknown formats are rejected. The native file picker includes a catch-all entry so a renamed image can still be selected; acceptance is decided by content sniffing, not the picker filter.

### Decision table

| Real bytes | Extension | dev.39 action |
|---|---|---|
| GIF87a/GIF89a | any | validate + preserve animated GIF |
| static WebP RIFF/WEBP | any | decode + normalize PNG |
| animated WebP (`VP8X` animation bit / `ANIM` / `ANMF`) | any | reject explicitly |
| static PNG | any | decode + normalize PNG |
| APNG (`acTL`) | any | reject explicitly |
| JPEG | any | decode + normalize PNG |
| BMP | any | decode + normalize PNG |
| unknown | any | reject |

## Transport/storage

New content-addressed assets use `<sha256>.asset` locally and in the world. The bytes are either normalized PNG or validated GIF. Historical `<sha256>.png` files from dev.1-dev.38 remain readable and uploadable. Protocol 18 is the compatibility boundary for generic asset transport.

## GIF safety limits

- source/persisted GIF <= 8 MiB;
- canvas <= 1024x1024;
- <= 128 frames;
- <= 16,777,216 decoded frame-pixels total;
- frame delay clamped to 20 ms minimum (50 FPS maximum), with zero-delay frames interpreted as 100 ms;
- per-frame delay <= 10 s;
- complete loop <= 5 minutes;
- frame rectangles must remain inside the declared logical canvas before pixel decode.

A malicious/invalid GIF cannot bypass these rules by changing its extension.
