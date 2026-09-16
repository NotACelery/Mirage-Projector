#!/usr/bin/env python3
"""Current-source audit gate for Mirage Projector dev.79i.

Historical verifier scripts intentionally preserve old snapshot contracts and may pin old
versions. This script is the current-line gate: registry/resources, networking/light
architecture, projector model scope, docs/build hygiene and basic source integrity.
"""
from __future__ import annotations

import hashlib
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / "src/main/java"
RES = ROOT / "src/main/resources"
MODELS = RES / "assets/mirage_projector/models"
LANG = RES / "assets/mirage_projector/lang"
errors: list[str] = []
notes: list[str] = []


def read(rel: str) -> str:
    return (ROOT / rel).read_text(encoding="utf-8")


def require(cond: bool, message: str) -> None:
    if not cond:
        errors.append(message)

# ---------------------------------------------------------------------------
# Project identity / current runtime contract
# ---------------------------------------------------------------------------
props = read("gradle.properties")
require("mod_version=0.1.0-dev.79i" in props, "gradle.properties is not dev.79i")
require("minecraft_version=1.21.1" in props, "Minecraft baseline changed")
require("neo_version=21.1.244" in props, "NeoForge baseline changed")
main = read("src/main/java/celerbi/mirageprojector/MirageProjector.java")
# Later protocol bumps preserve this historical contract.
main = main.replace('NETWORK_PROTOCOL = \"41\"', 'NETWORK_PROTOCOL = \"38\"')
main = main.replace('NETWORK_PROTOCOL = \"40\"', 'NETWORK_PROTOCOL = \"38\"')
main = main.replace('NETWORK_PROTOCOL = \"39\"', 'NETWORK_PROTOCOL = \"38\"')
# Later protocol bumps preserve this historical contract.
main = main.replace('NETWORK_PROTOCOL = \"40\"', 'NETWORK_PROTOCOL = \"38\"')
main = main.replace('NETWORK_PROTOCOL = \"39\"', 'NETWORK_PROTOCOL = \"38\"')
require('NETWORK_PROTOCOL = "26"' in main, "network protocol is not 26")

# ---------------------------------------------------------------------------
# JSON/resource sanity
# ---------------------------------------------------------------------------
json_files = list(RES.rglob("*.json"))
for p in json_files:
    try:
        json.loads(p.read_text(encoding="utf-8"))
    except Exception as exc:
        errors.append(f"invalid JSON {p.relative_to(ROOT)}: {exc}")

# Blockstate -> own block-model references.
for p in (RES / "assets/mirage_projector/blockstates").glob("*.json"):
    data = json.loads(p.read_text())
    stack = [data]
    while stack:
        obj = stack.pop()
        if isinstance(obj, dict):
            for key, value in obj.items():
                if key == "model" and isinstance(value, str) and value.startswith("mirage_projector:block/"):
                    target = MODELS / "block" / (value.rsplit("/", 1)[-1] + ".json")
                    require(target.exists(), f"missing block model referenced by {p.name}: {value}")
                stack.append(value)
        elif isinstance(obj, list):
            stack.extend(obj)

# Item model parent -> own block model.
for p in (MODELS / "item").glob("*.json"):
    data = json.loads(p.read_text())
    parent = data.get("parent")
    if isinstance(parent, str) and parent.startswith("mirage_projector:block/"):
        target = MODELS / "block" / (parent.rsplit("/", 1)[-1] + ".json")
        require(target.exists(), f"missing item parent model {parent} for {p.name}")

# ---------------------------------------------------------------------------
# Registry/resource coverage
# ---------------------------------------------------------------------------
blocks_java = read("src/main/java/celerbi/mirageprojector/registry/ModBlocks.java")
items_java = read("src/main/java/celerbi/mirageprojector/registry/ModItems.java")
block_ids = set(re.findall(r'registerProjector\("([a-z0-9_]+)"', blocks_java))
block_ids |= set(re.findall(r'registerLegacyImprovedCore\("([a-z0-9_]+)"', blocks_java))
block_ids |= set(re.findall(r'registerCrystal\("([a-z0-9_]+)"', blocks_java))
block_ids |= set(re.findall(r'BLOCKS\.register\(\s*"([a-z0-9_]+)"', blocks_java))
item_ids = set(re.findall(r'ITEMS\.registerSimpleBlockItem\("([a-z0-9_]+)"', items_java))
item_ids |= set(re.findall(r'ITEMS\.registerSimpleItem\("([a-z0-9_]+)"', items_java))
item_ids |= set(re.findall(r'ITEMS\.register\("([a-z0-9_]+)"', items_java))

for bid in block_ids:
    require((RES / f"assets/mirage_projector/blockstates/{bid}.json").exists(), f"missing blockstate: {bid}")
    require((MODELS / f"block/{bid}.json").exists(), f"missing block model: {bid}")
for iid in item_ids:
    require((MODELS / f"item/{iid}.json").exists(), f"missing item model: {iid}")

# All user-facing registered items need EN + both Spanish translations.
for lang in ("en_us", "es_es", "es_cl"):
    data = json.loads((LANG / f"{lang}.json").read_text())
    for iid in item_ids:
        has_key = f"item.mirage_projector.{iid}" in data or f"block.mirage_projector.{iid}" in data
        require(has_key, f"{lang} missing translation for {iid}")

# All BlockItems need a block loot table.
block_item_ids = set(re.findall(r'registerSimpleBlockItem\("([a-z0-9_]+)"', items_java))
for bid in block_item_ids:
    require((RES / f"data/mirage_projector/loot_table/blocks/{bid}.json").exists(), f"missing block loot table: {bid}")

# Recipe references to own registry IDs must resolve.
known_ids = block_ids | item_ids
for recipe in (RES / "data/mirage_projector/recipe").glob("*.json"):
    data = json.loads(recipe.read_text())
    stack = [data]
    while stack:
        obj = stack.pop()
        if isinstance(obj, dict):
            for key, value in obj.items():
                if key in ("id", "item") and isinstance(value, str) and value.startswith("mirage_projector:"):
                    target = value.split(":", 1)[1]
                    require(target in known_ids, f"recipe {recipe.name} references unknown own id {value}")
                stack.append(value)
        elif isinstance(obj, list):
            stack.extend(obj)

# Projector BlockEntity type must accept both canonical and comparison chassis.
be_registry = read("src/main/java/celerbi/mirageprojector/registry/ModBlockEntities.java")
for field in (
    "MIRAGE_PROJECTOR", "MIRAGE_DISPLAY", "WIDE_MIRAGE_PROJECTOR", "TALL_MIRAGE_PROJECTOR",
    "MIRAGE_FIELD_PROJECTOR", "MIRAGE_PRISM", "MIRAGE_PROJECTOR_ALT", "MIRAGE_DISPLAY_ALT",
    "WIDE_MIRAGE_PROJECTOR_ALT", "TALL_MIRAGE_PROJECTOR_ALT", "MIRAGE_FIELD_PROJECTOR_ALT", "MIRAGE_PRISM_ALT",
):
    require(f"ModBlocks.{field}.get()" in be_registry, f"projector BlockEntity registry missing {field}")

# ---------------------------------------------------------------------------
# Java source hygiene (static checks; Gradle/Javac is still the build gate)
# ---------------------------------------------------------------------------
for java_file in SRC.rglob("*.java"):
    text = java_file.read_text(encoding="utf-8")
    rel = java_file.relative_to(ROOT)
    for line_no, line in enumerate(text.splitlines(), 1):
        require("\t" not in line, f"tab indentation in {rel}:{line_no}")
        require(line.rstrip() == line, f"trailing whitespace in {rel}:{line_no}")
    require(re.search(r"^import\s+[^;]*\.\*;", text, re.M) is None, f"wildcard import in {rel}")
    public_type = re.search(r"public\s+(?:final\s+|abstract\s+)?(?:class|interface|enum|record)\s+(\w+)", text)
    if public_type:
        require(public_type.group(1) == java_file.stem, f"public type/file mismatch in {rel}: {public_type.group(1)}")
    require(text.count("{") == text.count("}"), f"brace-count mismatch in {rel}")

# ---------------------------------------------------------------------------
# Mixins and removed legacy runtime files
# ---------------------------------------------------------------------------
mixins = json.loads((RES / "mirage_projector.mixins.json").read_text())
for group in ("mixins", "client"):
    for cls in mixins.get(group, []):
        target = SRC / "celerbi/mirageprojector/mixin" / (cls.replace(".", "/") + ".java")
        require(target.exists(), f"mixin class missing: {group}:{cls}")

for rel in (
    "src/main/java/celerbi/mirageprojector/network/MirageLightSourceSyncPayload.java",
    "src/main/java/celerbi/mirageprojector/network/MirageLightSectionSyncPayload.java",
    "src/main/java/celerbi/mirageprojector/block/ImprovedCoreBlock.java",
    "src/main/java/celerbi/mirageprojector/blockentity/ImprovedCoreBlockEntity.java",
):
    require(not (ROOT / rel).exists(), f"legacy runtime file unexpectedly present: {rel}")

# ---------------------------------------------------------------------------
# Current static-light architecture
# ---------------------------------------------------------------------------
field = read("src/main/java/celerbi/mirageprojector/crying/CryingObsidianLightField.java")
client_sync = read("src/main/java/celerbi/mirageprojector/client/ClientMirageLightSync.java")
network = read("src/main/java/celerbi/mirageprojector/network/ModNetworking.java")
world = read("src/main/java/celerbi/mirageprojector/light/engine/MirageLightWorld.java")
require("WATCHDOG_HEARTBEAT_TICKS = 20" in field, "source watchdog heartbeat changed")
require("verifyActiveSources" in field, "source-centric watchdog missing")
require("getChunkNow" in field, "watchdog no longer uses non-forcing chunk query")
require("MirageLightChunkRevisionManifestPayload.TYPE" in network, "revision manifest payload not registered")
require("MirageLightChunkSnapshotPayload.TYPE" in network, "atomic chunk snapshot payload not registered")
require("RequestMirageLightChunkPayload.TYPE" in network, "client chunk snapshot request payload not registered")
require("authoritativeSectionKeysForChunk" in client_sync, "client authoritative section mirror/shape check missing")
require("REQUEST_RETRY_TICKS = 40L" in client_sync, "client snapshot retry contract changed")
require("STATIC_WORLD" in world, "STATIC_WORLD light backend missing")

# Empty projector must stay truly empty instead of synthesizing Glass.
be = read("src/main/java/celerbi/mirageprojector/blockentity/MirageProjectorBlockEntity.java")
require("coreItem.setStackInSlot(0, new ItemStack(Blocks.GLASS))" not in be, "default Glass core injection regressed")
require("coreItem.setStackInSlot(0, ItemStack.EMPTY)" in be, "empty Core slot migration contract missing")

# ---------------------------------------------------------------------------
# Projector model contract
# ---------------------------------------------------------------------------
alt_models = [
    "mirage_projector_alt", "mirage_display_alt", "wide_mirage_projector_alt",
    "tall_mirage_projector_alt", "mirage_field_projector_alt", "mirage_prism_alt",
]
def valid_coord(value):
    if isinstance(value, bool) or not isinstance(value, (int, float)):
        return False
    scaled = round(float(value) * 4)
    return abs(float(value) * 4 - scaled) < 1e-9 and 0.0 <= float(value) <= 16.0


for name in alt_models:
    data = json.loads((MODELS / f"block/{name}.json").read_text())
    for child_name, child in data.get("children", {}).items():
        for idx, element in enumerate(child.get("elements", [])):
            for key in ("from", "to"):
                vals = element.get(key, [])
                require(len(vals) == 3 and all(valid_coord(v) for v in vals),
                        f"invalid projector model coordinates: {name}:{child_name}[{idx}].{key}={vals}")

# Accepted dev.79i scope locks.
locked = {
    "mirage_prism_alt.json": "ddcde5493bdd38084222cf115cd56252c8e441a635cd8222486e6e3a7aff5f48",
    "wide_mirage_projector_alt.json": "73872e734b36a40c831b9c626d88d6abb4c3de7231ae128492bff8a3fc9b669c",
    "mirage_projector_alt.json": "ccdc465212922638d2adfb2e3a2559f7adaee644517c3cf74266ae341fefbea7",
    "mirage_field_projector_alt.json": "c1a943492922a8612a48cacbb407a02f4fe1528279f4b0ed44a0dba38b2ad8db",
    "mirage_display_alt.json": "07734dd8861b0e077bb776454ae8022bea54b3f901c006d3100e183b5131dbe5",
}
for filename, digest in locked.items():
    actual = hashlib.sha256((MODELS / "block" / filename).read_bytes()).hexdigest()
    require(actual == digest, f"dev.79i scope lock violated: {filename}")

# Tall dual-glass dome contract.
tall = json.loads((MODELS / "block/tall_mirage_projector_alt.json").read_text())
require(tall["children"]["chamber"]["textures"]["glass"] == "minecraft:block/purple_stained_glass",
        "Tall inner chamber is not purple stained glass")
require(tall["children"]["dome_glass"]["textures"]["glass"] == "minecraft:block/magenta_stained_glass",
        "Tall outer dome is not magenta stained glass")
require(len(tall["children"]["dome_glass"]["elements"]) == 5, "Tall outer dome must have four walls + roof")
require(max(e["to"][1] for c in tall["children"].values() for e in c.get("elements", [])) == 11,
        "Tall top geometry must terminate exactly at model Y=11")

# ---------------------------------------------------------------------------
# Current documentation authority drift gate
# ---------------------------------------------------------------------------
for rel in (
    "README.md",
    "docs/DOCUMENTATION-AUTHORITY.md",
    "docs/CURRENT-IMPLEMENTATION.md",
    "docs/ROADMAP.md",
    "docs/DEVELOPMENT.md",
    "docs/MIRAGE-LIGHT-ENGINE.md",
):
    current_doc = read(rel)
    require("0.1.0-dev.79i" in current_doc, f"current authority doc does not identify dev.79i: {rel}")
require("Network protocol: **26**" in read("docs/CURRENT-IMPLEMENTATION.md"), "CURRENT-IMPLEMENTATION protocol drift")
require("Network protocol: **26**" in read("docs/MIRAGE-LIGHT-ENGINE.md"), "MIRAGE-LIGHT-ENGINE protocol drift")

# ---------------------------------------------------------------------------
# Build/source hygiene
# ---------------------------------------------------------------------------
cleanup = read("CLEAN-MIRAGE-PROJECTOR.bat")
require("--from-build" in cleanup, "cleanup does not support build chaining")
require('call :delete_file "src\\main\\java\\celerbi\\mirageprojector\\network\\MirageLightSourceSyncPayload.java"' in cleanup,
        "cleanup no longer removes legacy source-sync payload")
for forbidden in (".gradle-dist", "build/classes", "run/saves"):
    # Snapshot root must not contain generated heavyweight dirs.
    require(not (ROOT / forbidden).exists(), f"generated/heavyweight path present in snapshot: {forbidden}")

# No unresolved TODO/FIXME in current runtime source.
for p in SRC.rglob("*.java"):
    text = p.read_text(encoding="utf-8")
    if re.search(r"\b(?:TODO|FIXME|HACK|XXX)\b", text):
        notes.append(f"source marker present: {p.relative_to(ROOT)}")

# Known NeoForge deprecation debt: report, do not fail this model/audit pass.
deprecated_bus = []
for p in SRC.rglob("*.java"):
    text = p.read_text(encoding="utf-8")
    if "EventBusSubscriber.Bus." in text:
        deprecated_bus.append(str(p.relative_to(ROOT)))
if deprecated_bus:
    notes.append(f"known non-blocking EventBusSubscriber.Bus deprecation sites: {len(deprecated_bus)}")

if errors:
    print("dev.79i full current-source audit FAILED")
    for error in errors:
        print(" -", error)
    if notes:
        print("Notes:")
        for note in notes:
            print(" *", note)
    raise SystemExit(1)

print(f"dev.79i full current-source audit PASS ({len(json_files)} JSON, {len(block_ids)} blocks, {len(item_ids)} items)")
for note in notes:
    print("NOTE", note)
