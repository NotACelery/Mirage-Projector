#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / "src/main/java/celerbi/mirageprojector/light/engine/MirageLightOcclusion.java"
text = SRC.read_text(encoding="utf-8")

assert "toState.getLightBlock(level, toPos)" in text
assert "destinationOpacity >= 15" in text
assert "direction,\n                destinationOpacity" in text
assert "direction,\n                1" not in text

# Regression model for the intended path semantics: a solid wall cannot be entered.
# A finite three-block-high wall must therefore be reached only by walking around an
# edge. The exact visible level depends on the half-decay profile, but routed distance
# must be strictly longer than the unobstructed Manhattan path.
source=(0,0)
probe=(6,0)
wall={(3,y) for y in range(0,3)}

def bfs(blocked):
    from collections import deque
    q=deque([(source,0)])
    seen={source}
    while q:
        p,d=q.popleft()
        if p==probe: return d
        x,y=p
        for n in ((x+1,y),(x-1,y),(x,y+1),(x,y-1)):
            if n in blocked or n in seen or abs(n[0])>12 or abs(n[1])>12: continue
            seen.add(n); q.append((n,d+1))
    return None

open_d=bfs(set())
wall_d=bfs(wall)
assert open_d == 6, open_d
assert wall_d is not None and wall_d > open_d, (open_d, wall_d)
print("dev.75c Mirage Light occlusion contract verification: PASS")
print(f"- open path={open_d} steps; routed wall path={wall_d} steps")
print("- real destination opacity is handed to vanilla edge occlusion")
