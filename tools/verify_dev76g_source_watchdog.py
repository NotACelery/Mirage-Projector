from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
checks = []

def has(rel, text):
    data = (ROOT / rel).read_text(encoding="utf-8")
    checks.append((rel + " :: " + text, text in data))

has("gradle.properties", "mod_version=0.1.0-dev.76h")
has("src/main/java/celerbi/mirageprojector/MirageProjector.java", 'NETWORK_PROTOCOL = "25"')
has("src/main/java/celerbi/mirageprojector/crying/CryingObsidianLightField.java", "verifyActiveSources")
has("src/main/java/celerbi/mirageprojector/crying/CryingObsidianLightField.java", "Math.max(2, (source.profile().maxRadius() + 15) / 16)")
has("src/main/java/celerbi/mirageprojector/crying/CryingObsidianLightField.java", "WATCHDOG_HEARTBEAT_TICKS = 20")
has("src/main/java/celerbi/mirageprojector/crying/CryingObsidianLightField.java", "refreshMirageSolverField(level, sourcePos, true, true)")
has("src/main/java/celerbi/mirageprojector/event/MirageLightLifecycleEvents.java", "CryingObsidianLightField.verifyActiveSources(level)")
has("src/main/java/celerbi/mirageprojector/event/CryingObsidianLightInvalidationEvents.java", "CryingObsidianLightField.noteBlockChanged(level, pos)")
has("src/main/java/celerbi/mirageprojector/network/ModNetworking.java", "MirageLightChunkRevisionManifestPayload.TYPE")
has("src/main/java/celerbi/mirageprojector/client/ClientMirageLightSync.java", "shapeMismatch")
has("src/main/java/celerbi/mirageprojector/client/ClientMirageLightSync.java", "REQUEST_RETRY_TICKS = 40L")

# The watchdog must probe loaded chunks only; never introduce force-load APIs/tickets.
watch = (ROOT / "src/main/java/celerbi/mirageprojector/crying/CryingObsidianLightField.java").read_text(encoding="utf-8")
checks.append(("watchdog uses getChunkNow", "getChunkNow" in watch))
checks.append(("no forced chunk ticket in watchdog", "addRegionTicket" not in watch and "setChunkForced" not in watch))

failed = [name for name, ok in checks if not ok]
for name, ok in checks:
    print(("PASS " if ok else "FAIL ") + name)
if failed:
    raise SystemExit(1)
print(f"dev.76g watchdog retained in dev.76h: {len(checks)} checks passed")
