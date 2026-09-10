# Mirage Prism adaptive aspect contract — dev.39

Status: **authoritative for Prism Image Mode.**

Prism remains a four-sided cardinal projector. It never becomes a Wide/Tall multi-source strip and never exposes a 4x1/1x4 stacking toggle.

- North: one image/GIF;
- East: one image/GIF;
- South: one image/GIF;
- West: one image/GIF;
- `Same source on all faces` remains an optional source-copy convenience.

Each face preserves its own aspect ratio and uses the same dominant-axis Scale contract as Wide/Tall SINGLE. To prevent Prism from being artificially limited to square artwork, its **nominal PU envelope is adaptive per face**:

- clearly horizontal source (aspect >= 1.20): Wide-like nominal 80x32;
- clearly vertical source (aspect <= 1/1.20): Tall-like nominal 32x80;
- near-square source: native Prism nominal 48x48.

These are nominal efficiency envelopes, not hard caps. Overdrive remains legal when Effective PU can pay it. Geometry PU/overdrive is calculated per Prism face, then summed; one extreme face does not multiply the geometry cost of the other three faces.

The physical result remains four lateral quads with no top or bottom. Different faces may use independent PNG/JPEG/WebP/BMP/GIF sources. Animated faces are sampled from one shared frame clock per Prism render so repeated copies of the same GIF do not visibly desynchronize across cardinal faces.
