# Third-party notices

Mirage Projector is an independent Minecraft mod. The mod's own source remains under the license declared by the project metadata.

## webp-imageio

Mirage Projector `0.1.0-dev.9` embeds the following library in the distributable JAR to decode local WebP images:

- Artifact: `org.sejda.imageio:webp-imageio:0.1.6`
- Purpose in Mirage Projector: WebP decoding through Java ImageIO before the imported image is normalized to PNG.
- Upstream project: `sejda-pdf/webp-imageio`
- Upstream license: Apache License 2.0.

The dependency is used only for local decoding. Mirage Projector does not transmit the original WebP file or its filesystem path; the normalized PNG is the asset that is hashed, cached and synchronized.

When preparing a public release, verify that the final Jar-in-Jar output retains the dependency's own license/notice material as expected by the packaging task.
