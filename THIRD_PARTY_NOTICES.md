# Third-party notices

Mirage Projector is an independent Minecraft mod. The mod's own source remains under the license declared by the project metadata.

## webp-imageio

Mirage Projector embeds the following library in the distributable JAR to decode local WebP images:

- Artifact: `org.sejda.imageio:webp-imageio:0.1.6`
- Purpose in Mirage Projector: Static WebP decoding through Java ImageIO before the imported image is normalized to PNG. Animated WebP is content-detected and rejected in dev.39 before decode/playback.
- Upstream project: `sejda-pdf/webp-imageio`
- Upstream license: Apache License 2.0.

The dependency is used only for local decoding. Mirage Projector does not transmit the original WebP file or its filesystem path; the normalized PNG is the asset that is hashed, cached and synchronized.

When preparing a public release, verify that the final Jar-in-Jar output retains the dependency's own license/notice material as expected by the packaging task.

## GIF decoding in dev.39

GIF / Animated Image support adds **no new third-party decoder dependency**. Mirage uses the GIF reader provided by Java ImageIO (`java.desktop`) and Mirage-owned frame composition/timing/safety code in `GifAssetDecoder`.

Validated GIF bytes are preserved as the content-addressed projection asset because converting them to PNG would destroy animation timing/disposal metadata. This does not change the `webp-imageio` notice above; WebP/JPEG/PNG imports remain normalized to PNG bytes before storage/transport.
