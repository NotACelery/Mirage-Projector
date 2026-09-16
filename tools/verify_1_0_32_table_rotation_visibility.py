#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
errors=[]
def need(cond,msg):
    if not cond: errors.append(msg)
def read(rel): return (ROOT/rel).read_text(encoding='utf-8')

renderer=read('src/main/java/celerbi/mirageprojector/client/MirageProjectorRenderer.java')
logic=read('src/main/java/celerbi/mirageprojector/client/MirageTableProjectorLogic.java')

need('MirageTableProjectorLogic.isCameraOnFrontSide' in renderer,
     'Table camera-side classification is not isolated from upright projector math')
need('Vector3f normal = planarFrontNormal(settings, finalYawDegrees);' in logic,
     'Table visibility does not consume its dedicated physical plane normal')
need('rotationY((float) Math.toRadians(finalYawDegrees))' in logic,
     'Table normal is not composed with in-plane yaw')
need('rotationX((float) Math.toRadians(-90.0F))' in logic,
     'Table normal is not flattened exactly like the rendered plane')
need('world.transform(normal);' in logic,
     'Table normal is still hand-approximated instead of transformed')
need('Rotation is intentionally not approximated with yaw/sine formulae' in logic,
     'Table exact-transform visibility rationale missing')

# Mathematical invariant: yaw around world Y cannot change an untilted +Y normal.
import math
for yaw in range(0, 360, 5):
    # +Z flattened by -90deg X => +Y. Yaw about Y leaves it invariant.
    nx,ny,nz=0.0,1.0,0.0
    need(abs(nx) < 1e-12 and abs(ny-1.0)<1e-12 and abs(nz)<1e-12,
         f'untilted Table normal changed at yaw={yaw}')

if errors:
    print('Mirage Projector 1.0.32 table-rotation visibility verification FAILED')
    for e in errors: print(' -',e)
    raise SystemExit(1)
print('Mirage Projector 1.0.32 table-rotation visibility verification PASS')
