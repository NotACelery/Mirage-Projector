# GIF / Animated Image — dev.39

GIF is part of Image Mode, not a separate SourceMode. The six current chassis accept it wherever a normal image source is accepted:

- Compact / Display / Field: one continuous animated Plane;
- Wide: SINGLE animated panorama or up to four mixed static/GIF cells in 4x1 MULTI;
- Tall: SINGLE animated vertical image or up to four mixed static/GIF cells in 1x4 MULTI;
- Prism: one independent static/GIF source per N/E/S/W face.

GIF bytes are content-addressed and preserved. Client decode happens once per cache lifetime. Mirage composes transparency, frame offsets and disposal into complete ARGB frames, registers DynamicTextures once and only selects the current texture during rendering. It never rereads/redecodes the GIF every render frame.

All ordinary Image presentation effects apply to the current animated frame: Scale, Lift, Float, Rotation, Ghost, Tint, Lighting, Scanlines, Flip and Plane back-face semantics.

Animation itself adds no PU surcharge: PU measures emitter geometry/presentation. Decoder/RAM abuse is controlled by the technical limits in `IMAGE-FORMAT-IMPORT-CONTRACT-dev39.md`.
