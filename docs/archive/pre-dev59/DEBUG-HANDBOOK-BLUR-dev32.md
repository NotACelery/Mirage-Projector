# Debug Handbook blur fix — dev.32

The temporary Debug Handbook must not use the generic blurred screen background.

In Minecraft 1.21.1 the world/first-person hand is already rendered before the Screen. Calling the generic background path can blur that image, including the handbook item that opened the screen, producing the visual impression that the book blurs itself.

dev.32 renders a local translucent black overlay instead. This keeps the world subdued and the handbook readable without invoking the global blur post-process.

No gameplay, network, NBT or projection-render changes are part of this cut.
