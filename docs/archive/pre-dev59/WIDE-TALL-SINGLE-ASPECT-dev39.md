# Wide / Tall SINGLE aspect contract — dev.39

SINGLE never crops, stretches or distorts a source. `Scale` controls the image's dominant axis.

- horizontal source: `Scale = rendered width`, height follows aspect ratio;
- vertical source: `Scale = rendered height`, width follows aspect ratio;
- square: both dimensions equal Scale.

Wide is naturally efficient for horizontal content (nominal 80x32). A vertical source is still legal, but it reaches Wide's nominal height/overdrive earlier.

Tall is naturally efficient for vertical content (nominal 32x80). A horizontal source is still legal, but it reaches Tall's nominal width/overdrive earlier.

Examples at Scale 80:

- 800x320 -> 80x32;
- 1920x1080 -> 80x45;
- 320x800 -> 32x80;
- 1080x1920 -> 45x80.

Renderer and `ProjectionPower` both use `ProjectionImageSizing`; what is drawn is what geometry PU charges.

MULTI remains separate: four equivalent square cells. At Scale 80, Wide = 80x20 (4x 20x20), Tall = 20x80 (4x 20x20). Field never uses MULTI.
