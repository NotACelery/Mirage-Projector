from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
client = (ROOT / 'src/main/java/celerbi/mirageprojector/client/ClientMirageLightSync.java').read_text(encoding='utf-8')
field = (ROOT / 'src/main/java/celerbi/mirageprojector/crying/CryingObsidianLightField.java').read_text(encoding='utf-8')
props = (ROOT / 'gradle.properties').read_text(encoding='utf-8')

assert 'mod_version=0.1.0-dev.75j' in props
assert 'SOURCE_QUIET_SETTLE_TICKS = 12' in client
assert 'SOURCE_LATE_VERIFY_TICKS = 80' in client
assert 'existing != null && existing.equals(source)' in client
assert 'scheduleSourceSettle(source.id())' in client
assert 'settle.resetQuietWindow()' in client
assert 'tickAndConsumeDue' in client
assert 'refreshInternal(level, sourcePos, level.getBlockState(sourcePos), true, false);' in field
assert 'refreshInternal(level, sourcePos, level.getBlockState(sourcePos), true, true);' in field
assert 'result.rebuilt() && (!sourceWasKnown || notifyClients)' in field
print('dev.75j accumulated client settle + descriptor sync separation verification: PASS')
