# dev.39 closure QA

1. Run `build.bat`; do not mark build-clean until Windows succeeds.
2. Import PNG/JPEG/WebP/BMP/GIF under their normal extensions.
3. Rename a real GIF to `.png` and verify Mirage still detects/imports it as animated GIF.
4. Rename an animated WebP to `.png`, `.jpg` and `.gif`; every case must be explicitly rejected as Animated WebP, never flattened.
5. Repeat APNG renamed to `.png` if a fixture is available; reject explicitly.
6. GIF timing/transparency/disposal: test unequal delays, transparency and restore-to-background/restore-to-previous samples.
7. Test GIF on Compact, Display, Field; Wide SINGLE + four MULTI cells; Tall SINGLE + four MULTI cells; Prism N/E/S/W including Same Source on All Faces.
8. Prism: horizontal source should use Wide-like nominal envelope; vertical Tall-like; near-square 48x48; no 4x1/1x4 toggle exists for Prism.
9. Verify repeated same GIF on Prism/multi cells stays frame-synchronous.
10. Multiplayer: importing client uploads `.asset`; another client downloads and animates it. Old `<hash>.png` assets still resolve.
11. Re-run dev.38 regression QA: Field remains one Plane, Wide/Tall MULTI proportions, dynamic PU sliders, facing, idle books, Entity workspace cleanup and Entity/projector/water render-depth cases.
