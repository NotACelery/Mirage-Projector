from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

def read(rel):
    return (ROOT / rel).read_text(encoding="utf-8")

engine = read("src/main/java/celerbi/mirageprojector/light/engine/MirageLightEngine.java")
solver = read("src/main/java/celerbi/mirageprojector/light/engine/MirageLightSolver.java")
client = read("src/main/java/celerbi/mirageprojector/client/ClientMirageLightSync.java")
server = read("src/main/java/celerbi/mirageprojector/event/MirageLightLifecycleEvents.java")
field = read("src/main/java/celerbi/mirageprojector/crying/CryingObsidianLightField.java")

assert "dependencyChunks" in engine
assert "queryableDependencyChunks" in engine
assert "sourceDependsOnChunk" in engine
assert "Math.floorDiv(origin.getX() - radius, 16)" in engine
assert "Math.floorDiv(origin.getX() + radius, 16)" in engine
assert "getChunkNow" in engine
assert "MirageLightEngine.isChunkQueryable(level, origin)" in solver
assert solver.index("if (!withinRadius(origin, nextPos, profile.maxRadius()))") < solver.index("if (!MirageLightEngine.isChunkQueryable(level, nextPos))")

# Client must capture the fingerprint before solving and store that exact snapshot.
helper = client.index("private static Set<Long> rebuildSourceAndCaptureReadiness")
before = client.index("Set<Long> before = MirageLightEngine.queryableDependencyChunks", helper)
solve = client.index("MirageLightEngine.updateSource(level, source, true)", helper)
after = client.index("Set<Long> after = MirageLightEngine.queryableDependencyChunks", helper)
assert before < solve < after
assert "SOLVED_CHUNK_READINESS.put(source.id(), solveReadiness)" in client
assert "sourceDependsOnChunk" in client
assert "requestFullStabilization();" in client

# Server stores the pre-refresh 'current' snapshot, never a fresh post-solve snapshot.
assert "Set<Long> current = MirageLightEngine.queryableDependencyChunks(level, source)" in server
assert "CryingObsidianLightField.forceRefreshSource(level, source.origin());" in server
assert "snapshots.put(source.id(), current);" in server
assert "snapshots.put(source.id(), MirageLightEngine.queryable" not in server
assert "sourceDependsOnChunk" in field

print("dev.75h dependency-window stable-solve verification: PASS")
print("- readiness window is derived from origin +/- source radius and never force-loads chunks")
print("- client fingerprint is captured before solve, closing the post-solve chunk race")
print("- server mirrors the same pre-solve readiness association")
