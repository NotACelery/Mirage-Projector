# dev.63 build.bat reliability fix

The normal build script no longer invokes `CLEAN-MIRAGE-PROJECTOR.bat` automatically. Cleanup is a migration tool and is only meant to be run when a snapshot explicitly removes or relocates project files.

The dev.63 cleaner remains available because dev.63 relocates `CHANGELOG.md`, `DEVELOPMENT.md`, and `THIRD_PARTY_NOTICES.md` into `/docs`. Its line endings were normalized to CRLF for Windows `cmd.exe`.

`build.bat` now launches Gradle inside a child `cmd /c`, captures the exit code, prints it, and always reaches a visible pause on success or failure. This prevents a child batch script from closing the build console before the error can be read.
