# Third-Party Notices

## WebP ImageIO

- Library: `org.sejda.imageio:webp-imageio`
- Purpose: static WebP decoding through Java ImageIO before imported images are normalized for Mirage asset storage.
- Animated WebP is not supported as an animated playback format in 1.0.0.

The dependency is packaged through the project build configuration. Refer to the upstream project/license for its complete licensing terms.

## GIF decoding

Animated GIF playback uses Java/ImageIO-compatible GIF decoding logic in the Mirage client asset pipeline. GIF content is decoded into frame/timing data and rendered through the same content-addressed asset identity used by imported images.

## Optional recipe viewers

Mirage Projector contains optional integration code for:

- EMI
- JEI

Neither viewer is required for core mod startup/gameplay. Their own licenses/distribution terms are governed by their respective projects.
