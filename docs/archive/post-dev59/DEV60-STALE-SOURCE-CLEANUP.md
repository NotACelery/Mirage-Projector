# dev.60 compile repair — stale source cleanup

The second Windows compile exposed stale Java files left behind when a newer source snapshot was extracted over an older working directory.

The clean dev.60 snapshot no longer contains `ImprovedCoreBlock.java`, `ImprovedCoreBlockEntity.java`, `ImprovedCoreRenderer.java`, or the old client-package `CryingObsidianRandomTickStateMixin.java`. Their replacements are the `LegacyImprovedCore*` compatibility classes and the common-package Crying Obsidian mixin.

`build.bat` now removes those four obsolete source paths before invoking Gradle. This makes overlay extraction of newer snapshots safe for these known dev.59 renames and prevents stale Java files from being compiled against the cleaned registry.

No gameplay, Beacon Relay, Core Booster, rendering, protocol, or resource behavior changed in this repair.
