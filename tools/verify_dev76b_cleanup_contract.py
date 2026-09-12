from pathlib import Path

root = Path(__file__).resolve().parents[1]
cleanup = (root / "CLEAN-MIRAGE-PROJECTOR.bat").read_text(encoding="utf-8")
build = (root / "build.bat").read_text(encoding="utf-8")
legacy = root / "src/main/java/celerbi/mirageprojector/network/MirageLightSourceSyncPayload.java"

assert not legacy.exists(), "legacy source payload must not exist in canonical dev.76b source"
assert 'MirageLightSourceSyncPayload.java' in cleanup
assert 'verify_dev76a_legacy_payload_tombstone.py' in cleanup
assert 'call "%CD%\\CLEAN-MIRAGE-PROJECTOR.bat" --from-build' in build
assert 'if not "%CLEANUP_EXIT%"=="0" goto :cleanup_failed' in build
assert "MirageLightChunkSnapshotPayload.TYPE" in (root / "src/main/java/celerbi/mirageprojector/network/ModNetworking.java").read_text(encoding="utf-8")
assert "MirageLightSourceSyncPayload.TYPE" not in (root / "src/main/java/celerbi/mirageprojector/network/ModNetworking.java").read_text(encoding="utf-8")
print("dev.76b automatic cleanup/build contract verification: PASS")
