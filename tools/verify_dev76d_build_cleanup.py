from pathlib import Path
root = Path(__file__).resolve().parents[1]
lifecycle = (root / "src/main/java/celerbi/mirageprojector/event/MirageLightLifecycleEvents.java").read_text(encoding="utf-8")
cleanup = (root / "CLEAN-MIRAGE-PROJECTOR.bat").read_text(encoding="utf-8")
build = (root / "build.bat").read_text(encoding="utf-8")
assert "import java.util.Map;" in lifecycle
assert "Map<ServerLevel, ChunkLoadBatch>" in lifecycle
assert "call :archive_doc" not in cleanup
assert "pause" not in cleanup.lower()
assert "CLEAN-MIRAGE-PROJECTOR.bat" in build
assert "--from-build" in build
print("dev.76d build/cleanup stabilization contract: PASS")
