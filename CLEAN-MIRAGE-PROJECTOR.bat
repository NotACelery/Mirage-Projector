@echo off
setlocal EnableExtensions EnableDelayedExpansion
cd /d "%~dp0"
set "FROM_BUILD=0"
if /I "%~1"=="--from-build" set "FROM_BUILD=1"

set "LOG=%CD%\cleanup-mirage-projector.log"
set "ARCHIVE=%CD%\docs\archive\pre-dev59"
set "OVERLAY=%ARCHIVE%\overlay-leftovers"

>"%LOG%" echo Mirage Projector complete stale cleanup
>>"%LOG%" echo Started: %DATE% %TIME%

if not exist "gradle.properties" goto :wrong_folder
findstr /b /c:"mod_id=mirage_projector" "gradle.properties" >nul 2>nul
if errorlevel 1 goto :wrong_folder

if "%FROM_BUILD%"=="0" (
    echo ============================================================
    echo    MIRAGE PROJECTOR - LIMPIEZA COMPLETA DE ARCHIVOS OBSOLETOS
    echo ============================================================
    echo Proyecto: %CD%
    echo.
    echo Limpieza acumulativa y migracion documental vigente para dev.63.
    echo.
)

if not exist "%ARCHIVE%" mkdir "%ARCHIVE%" >nul 2>nul
if not exist "%OVERLAY%" mkdir "%OVERLAY%" >nul 2>nul

set /a REMOVED=0
set /a ARCHIVED=0
set /a SKIPPED=0

if "%FROM_BUILD%"=="0" echo [1/5] Reubicando documentacion que ya no pertenece a la raiz...
call :retire_root_doc "CHANGELOG.md" "docs\CHANGELOG.md"
call :retire_root_doc "DEVELOPMENT.md" "docs\DEVELOPMENT.md"
call :retire_root_doc "THIRD_PARTY_NOTICES.md" "docs\THIRD_PARTY_NOTICES.md"
call :retire_moved_doc "docs\NEXT-CHAT-HANDOFF-dev62.md" "docs\archive\post-dev59\NEXT-CHAT-HANDOFF-dev62.md"

if "%FROM_BUILD%"=="0" echo.
if "%FROM_BUILD%"=="0" echo [2/5] Eliminando source/resources runtime obsoletos...
call :delete_file "src\main\java\celerbi\mirageprojector\block\ImprovedCoreBlock.java"
call :delete_file "src\main\java\celerbi\mirageprojector\blockentity\ImprovedCoreBlockEntity.java"
call :delete_file "src\main\java\celerbi\mirageprojector\client\ImprovedCoreRenderer.java"
call :delete_file "src\main\java\celerbi\mirageprojector\mixin\client\CryingObsidianGrowthMixin.java"
call :delete_file "src\main\java\celerbi\mirageprojector\mixin\client\CryingObsidianRandomTickStateMixin.java"
call :delete_file "src\main\java\celerbi\mirageprojector\mixin\client\RenderLayerMixin.java"
call :delete_file "src\main\resources\assets\mirage_projector\models\item\improved_amethyst_core.json"
call :delete_file "src\main\resources\assets\mirage_projector\models\item\improved_core_item_base.json"
call :delete_file "src\main\resources\assets\mirage_projector\models\item\improved_diamond_core.json"
call :delete_file "src\main\resources\assets\mirage_projector\models\item\improved_glass_core.json"
call :delete_file "src\main\resources\assets\mirage_projector\models\item\improved_netherite_core.json"
call :delete_file "src\main\resources\assets\mirage_projector\models\item\improved_quartz_core.json"
call :delete_file "src\main\resources\data\mirage_projector\loot_table\blocks\improved_amethyst_core.json"
call :delete_file "src\main\resources\data\mirage_projector\loot_table\blocks\improved_diamond_core.json"
call :delete_file "src\main\resources\data\mirage_projector\loot_table\blocks\improved_glass_core.json"
call :delete_file "src\main\resources\data\mirage_projector\loot_table\blocks\improved_netherite_core.json"
call :delete_file "src\main\resources\data\mirage_projector\loot_table\blocks\improved_quartz_core.json"
call :delete_file "src\main\resources\data\mirage_projector\recipe\improved_amethyst_core.json"
call :delete_file "src\main\resources\data\mirage_projector\recipe\improved_diamond_core.json"
call :delete_file "src\main\resources\data\mirage_projector\recipe\improved_glass_core.json"
call :delete_file "src\main\resources\data\mirage_projector\recipe\improved_netherite_core.json"
call :delete_file "src\main\resources\data\mirage_projector\recipe\improved_quartz_core.json"

if "%FROM_BUILD%"=="0" echo.
if "%FROM_BUILD%"=="0" echo [3/5] Sacando documentacion historica duplicada de docs...
call :archive_doc "docs\BANNER-MODE-dev28.md"
call :archive_doc "docs\BUILD-FIX-dev18.md"
call :archive_doc "docs\BUILD-FIX-dev25.md"
call :archive_doc "docs\CHASSIS-IMAGE-LAYOUT-POWER-UX-dev38.md"
call :archive_doc "docs\CODE-QUALITY-AUDIT-dev41.md"
call :archive_doc "docs\CORES-AND-UPGRADES-dev41.md"
call :archive_doc "docs\CORES-AND-UPGRADES-dev42.md"
call :archive_doc "docs\CORES-AND-UPGRADES-dev43.md"
call :archive_doc "docs\CORES-AND-UPGRADES-dev44.md"
call :archive_doc "docs\CORES-AND-UPGRADES-dev45.md"
call :archive_doc "docs\CORES-AND-UPGRADES-dev46.md"
call :archive_doc "docs\CRYING-OBSIDIAN-ECOSYSTEM-dev41.md"
call :archive_doc "docs\CRYING-OBSIDIAN-ECOSYSTEM-dev42.md"
call :archive_doc "docs\CRYING-OBSIDIAN-ECOSYSTEM-dev43.md"
call :archive_doc "docs\CRYING-OBSIDIAN-ECOSYSTEM-dev44.md"
call :archive_doc "docs\CRYING-OBSIDIAN-ECOSYSTEM-dev45.md"
call :archive_doc "docs\CURRENT-IMPLEMENTATION-AUDIT-dev38.md"
call :archive_doc "docs\CURRENT-IMPLEMENTATION-AUDIT-dev39.md"
call :archive_doc "docs\CURRENT-IMPLEMENTATION-AUDIT-dev40.md"
call :archive_doc "docs\CURRENT-IMPLEMENTATION-AUDIT-dev41.md"
call :archive_doc "docs\CURRENT-IMPLEMENTATION-AUDIT-dev42.md"
call :archive_doc "docs\CURRENT-IMPLEMENTATION-AUDIT-dev43.md"
call :archive_doc "docs\CURRENT-IMPLEMENTATION-AUDIT-dev44.md"
call :archive_doc "docs\CURRENT-IMPLEMENTATION-AUDIT-dev45.md"
call :archive_doc "docs\CURRENT-IMPLEMENTATION-AUDIT-dev46.md"
call :archive_doc "docs\CURRENT-STATE-ROADMAP-dev41.md"
call :archive_doc "docs\CURRENT-STATE-ROADMAP-dev42.md"
call :archive_doc "docs\CURRENT-STATE-ROADMAP-dev43.md"
call :archive_doc "docs\CURRENT-STATE-ROADMAP-dev44.md"
call :archive_doc "docs\CURRENT-STATE-ROADMAP-dev45.md"
call :archive_doc "docs\CURRENT-STATE-ROADMAP-dev46.md"
call :archive_doc "docs\DEBUG-HANDBOOK-BLUR-dev32.md"
call :archive_doc "docs\DEV38-CLOSURE-QA.md"
call :archive_doc "docs\DEV39-GIF-QA.md"
call :archive_doc "docs\DEV40-UI-ENTITY-QA.md"
call :archive_doc "docs\DEV41-CONSOLIDATION-QA.md"
call :archive_doc "docs\DEV42-MATERIAL-CHAMBER-CHASSIS.md"
call :archive_doc "docs\DEV42-QA.md"
call :archive_doc "docs\DEV43-CRYSTAL-BEACON-QA.md"
call :archive_doc "docs\DEV44-OBSIDIAN-SPIKE-QA.md"
call :archive_doc "docs\DEV45-COMPILE-REPAIR.md"
call :archive_doc "docs\DEV45-PROJECTOR-UPGRADE-QA.md"
call :archive_doc "docs\DEV46-IMPROVED-CORES-QA.md"
call :archive_doc "docs\DEV46-STATIC-QA.md"
call :archive_doc "docs\DEV47-QA-POLISH-NOTES.md"
call :archive_doc "docs\DEV48-CRYING-OBSIDIAN-NUCLEATION-FIX.md"
call :archive_doc "docs\DEV49-RANDOM-TICK-DISPATCH-FIX.md"
call :archive_doc "docs\DEV50-RENDERLAYER-BOOT-CRASH-FIX.md"
call :archive_doc "docs\DEV51-QA-CORRECTIONS.md"
call :archive_doc "docs\DEV52-VISUAL-POLISH-NOTES.md"
call :archive_doc "docs\DEV53-CANONICAL-AMETHYST-ALIGNMENT.md"
call :archive_doc "docs\DEV54-CORE-BOOSTER-REDESIGN.md"
call :archive_doc "docs\DEV55-CORE-BOOSTER-CLEANUP.md"
call :archive_doc "docs\DEV56-BEACON-OPTICS-REWORK.md"
call :archive_doc "docs\DEV56-COMPILE-REPAIR.md"
call :archive_doc "docs\DEV56-RAY-ORIGIN-POLISH.md"
call :archive_doc "docs\DEV57-TRANSPARENCY-COMPOSITION-FIX.md"
call :archive_doc "docs\DOCUMENTATION-AUTHORITY-dev41.md"
call :archive_doc "docs\DOCUMENTATION-AUTHORITY-dev42.md"
call :archive_doc "docs\DOCUMENTATION-AUTHORITY-dev43.md"
call :archive_doc "docs\DOCUMENTATION-AUTHORITY-dev44.md"
call :archive_doc "docs\DOCUMENTATION-AUTHORITY-dev45.md"
call :archive_doc "docs\DOCUMENTATION-AUTHORITY-dev46.md"
call :archive_doc "docs\ENTITY-BOUNDS-dev30.md"
call :archive_doc "docs\ENTITY-DIMENSION-NORMALIZATION-dev40.md"
call :archive_doc "docs\ENTITY-FIDELITY-dev31.md"
call :archive_doc "docs\ENTITY-GHOST-LAYOUT-dev24.md"
call :archive_doc "docs\ENTITY-INTERACTION-LOADOUT-dev20.md"
call :archive_doc "docs\ENTITY-NAMEPLATE-DEPTH-ORDER-dev36.md"
call :archive_doc "docs\ENTITY-NAMEPLATE-HANDBOOK-dev34.md"
call :archive_doc "docs\ENTITY-NAMEPLATE-HANDBOOK-dev35.md"
call :archive_doc "docs\ENTITY-PREVIEW-RENDERER-dev15.md"
call :archive_doc "docs\ENTITY-PROJECTION-CONTRACT.md"
call :archive_doc "docs\ENTITY-RENDERER-dev16.md"
call :archive_doc "docs\ENTITY-SCAN-WORKSPACE-dev14.md"
call :archive_doc "docs\ENTITY-UX-RECOVERY-dev23.md"
call :archive_doc "docs\ENTITY-WORKSPACE-LIFETIME-dev38.md"
call :archive_doc "docs\GHOST-3D-RENDERER-dev17.md"
call :archive_doc "docs\GHOST-DEPTH-ARMOR-dev26.md"
call :archive_doc "docs\GHOST-HELD-ITEMS-dev27.md"
call :archive_doc "docs\GIF-ANIMATED-IMAGE-dev39.md"
call :archive_doc "docs\GUI-ARCHITECTURE-dev19.md"
call :archive_doc "docs\HUMANOID-POSES-dev22.md"
call :archive_doc "docs\IMAGE-FORMAT-IMPORT-CONTRACT-dev39.md"
call :archive_doc "docs\ITEM-ARMOR-SNAPSHOT-CONTRACT.md"
call :archive_doc "docs\MULTI-SOURCE-IMAGE-LAYOUTS-dev33.md"
call :archive_doc "docs\NEXT-CHAT-HANDOFF-dev41.md"
call :archive_doc "docs\NEXT-CHAT-HANDOFF-dev42.md"
call :archive_doc "docs\NEXT-CHAT-HANDOFF-dev43.md"
call :archive_doc "docs\NEXT-CHAT-HANDOFF-dev44.md"
call :archive_doc "docs\NEXT-CHAT-HANDOFF-dev45.md"
call :archive_doc "docs\NEXT-CHAT-HANDOFF-dev46.md"
call :archive_doc "docs\NEXT-CHAT-HANDOFF-dev47.md"
call :archive_doc "docs\NEXT-CHAT-HANDOFF-dev48.md"
call :archive_doc "docs\NEXT-CHAT-HANDOFF-dev49.md"
call :archive_doc "docs\NEXT-CHAT-HANDOFF-dev50.md"
call :archive_doc "docs\NEXT-CHAT-HANDOFF-dev51.md"
call :archive_doc "docs\NEXT-CHAT-HANDOFF-dev52.md"
call :archive_doc "docs\NEXT-CHAT-HANDOFF-dev53.md"
call :archive_doc "docs\NEXT-CHAT-HANDOFF-dev54.md"
call :archive_doc "docs\NEXT-CHAT-HANDOFF-dev55.md"
call :archive_doc "docs\NEXT-CHAT-HANDOFF-dev56.md"
call :archive_doc "docs\NEXT-CHAT-HANDOFF-dev57.md"
call :archive_doc "docs\NEXT-CHAT-HANDOFF-dev59.md"
call :archive_doc "docs\OBSIDIAN-SPIKE-dev44.md"
call :archive_doc "docs\POWER-POSE-UX-dev29.md"
call :archive_doc "docs\POWER-SYSTEM-REWORK-dev38.md"
call :archive_doc "docs\PRISM-ADAPTIVE-ASPECT-dev39.md"
call :archive_doc "docs\PRISM-FOUR-FACE-dev21.md"
call :archive_doc "docs\RENDER-ORDER-HANDBOOK-RECOVERY-dev37.md"
call :archive_doc "docs\WIDE-TALL-SINGLE-ASPECT-dev39.md"

if "%FROM_BUILD%"=="0" echo.
if "%FROM_BUILD%"=="0" echo [4/5] Restaurando metadata de Gradle Wrapper si falta...
call :ensure_wrapper_properties

if "%FROM_BUILD%"=="0" echo.
if "%FROM_BUILD%"=="0" echo [5/5] Limpiando salidas de compilacion regenerables...
if exist "build" (
    >>"%LOG%" echo DELETE DIR build
    rmdir /s /q "build"
)

if "%FROM_BUILD%"=="0" (
    echo.
    echo ============================================================
    echo LIMPIEZA TERMINADA
    echo Archivos runtime eliminados: !REMOVED!
    echo Documentos archivados/eliminados: !ARCHIVED!
    echo Entradas ya ausentes: !SKIPPED!
    echo Log:
    echo   %LOG%
    echo ============================================================
    echo.
    echo No se eliminan .gradle-dist, run, mundos ni configuraciones.
    echo Ya puedes ejecutar build.bat.
    echo.
    pause
)
endlocal & exit /b 0

:retire_root_doc
if not exist "%~1" (
    set /a SKIPPED+=1
    exit /b 0
)
if not exist "%~dp2" mkdir "%~dp2" >nul 2>nul
if not exist "%~2" (
    if "%FROM_BUILD%"=="0" echo   MOVE %~1 ^> %~2
    >>"%LOG%" echo MOVE ROOT DOC %~1 ^> %~2
    move /y "%~1" "%~2" >nul
    if exist "%~1" exit /b 1
    set /a ARCHIVED+=1
    exit /b 0
)
fc /b "%~1" "%~2" >nul 2>nul
if not errorlevel 1 (
    if "%FROM_BUILD%"=="0" echo   DELETE duplicate root doc %~1
    >>"%LOG%" echo DELETE DUP ROOT DOC %~1
    del /f /q "%~1" >nul 2>nul
    if exist "%~1" exit /b 1
    set /a REMOVED+=1
    exit /b 0
)
set "ROOT_FALLBACK=%OVERLAY%\root-pre-dev63-%~nx1"
if "%FROM_BUILD%"=="0" echo   ARCHIVE old root doc %~1
>>"%LOG%" echo ARCHIVE ROOT DOC %~1 ^> !ROOT_FALLBACK!
move /y "%~1" "!ROOT_FALLBACK!" >nul
if exist "%~1" exit /b 1
set /a ARCHIVED+=1
exit /b 0

:retire_moved_doc
if not exist "%~1" (
    set /a SKIPPED+=1
    exit /b 0
)
if not exist "%~dp2" mkdir "%~dp2" >nul 2>nul
if not exist "%~2" (
    if "%FROM_BUILD%"=="0" echo   MOVE %~1 ^> %~2
    >>"%LOG%" echo MOVE DOC %~1 ^> %~2
    move /y "%~1" "%~2" >nul
    if exist "%~1" exit /b 1
    set /a ARCHIVED+=1
    exit /b 0
)
fc /b "%~1" "%~2" >nul 2>nul
if not errorlevel 1 (
    if "%FROM_BUILD%"=="0" echo   DELETE duplicate moved doc %~1
    >>"%LOG%" echo DELETE DUP MOVED DOC %~1
    del /f /q "%~1" >nul 2>nul
    if exist "%~1" exit /b 1
    set /a REMOVED+=1
    exit /b 0
)
set "MOVED_FALLBACK=%OVERLAY%\moved-pre-dev63-%~nx1"
if "%FROM_BUILD%"=="0" echo   ARCHIVE differing moved doc %~1
>>"%LOG%" echo ARCHIVE MOVED DOC %~1 ^> !MOVED_FALLBACK!
move /y "%~1" "!MOVED_FALLBACK!" >nul
if exist "%~1" exit /b 1
set /a ARCHIVED+=1
exit /b 0

:delete_file
if exist "%~1" (
    if "%FROM_BUILD%"=="0" echo   DELETE %~1
    >>"%LOG%" echo DELETE %~1
    del /f /q "%~1" >nul 2>nul
    if exist "%~1" (
        if "%FROM_BUILD%"=="0" echo   ERROR: no pude eliminar %~1
        >>"%LOG%" echo ERROR DELETE %~1
    ) else (
        set /a REMOVED+=1
    )
) else (
    set /a SKIPPED+=1
)
exit /b 0

:archive_doc
if not exist "%~1" (
    set /a SKIPPED+=1
    exit /b 0
)
set "DOCNAME=%~nx1"
set "CANON=%ARCHIVE%\!DOCNAME!"
set "FALLBACK=%OVERLAY%\!DOCNAME!"
if exist "!CANON!" (
    fc /b "%~1" "!CANON!" >nul 2>nul
    if not errorlevel 1 (
        if "%FROM_BUILD%"=="0" echo   DELETE duplicate doc %~1
        >>"%LOG%" echo DELETE DUP DOC %~1
        del /f /q "%~1" >nul 2>nul
    ) else (
        if "%FROM_BUILD%"=="0" echo   ARCHIVE differing doc %~1
        >>"%LOG%" echo ARCHIVE DIFF DOC %~1 ^> !FALLBACK!
        move /y "%~1" "!FALLBACK!" >nul
    )
) else (
    if "%FROM_BUILD%"=="0" echo   ARCHIVE doc %~1
    >>"%LOG%" echo ARCHIVE DOC %~1 ^> !FALLBACK!
    move /y "%~1" "!FALLBACK!" >nul
)
if exist "%~1" (
    if "%FROM_BUILD%"=="0" echo   ERROR: no pude retirar %~1
    >>"%LOG%" echo ERROR ARCHIVE %~1
) else (
    set /a ARCHIVED+=1
)
exit /b 0

:ensure_wrapper_properties
if not exist "gradle\wrapper" mkdir "gradle\wrapper" >nul 2>nul
if exist "gradle\wrapper\gradle-wrapper.properties" (
    >>"%LOG%" echo KEEP gradle\wrapper\gradle-wrapper.properties
    exit /b 0
)
>"gradle\wrapper\gradle-wrapper.properties" echo distributionBase=GRADLE_USER_HOME
>>"gradle\wrapper\gradle-wrapper.properties" echo distributionPath=wrapper/dists
>>"gradle\wrapper\gradle-wrapper.properties" echo distributionUrl=https\://services.gradle.org/distributions/gradle-9.2.1-bin.zip
>>"gradle\wrapper\gradle-wrapper.properties" echo distributionSha256Sum=72f44c9f8ebcb1af43838f45ee5c4aa9c5444898b3468ab3f4af7b6076c5bc3f
>>"gradle\wrapper\gradle-wrapper.properties" echo networkTimeout=10000
>>"gradle\wrapper\gradle-wrapper.properties" echo validateDistributionUrl=true
>>"gradle\wrapper\gradle-wrapper.properties" echo zipStoreBase=GRADLE_USER_HOME
>>"gradle\wrapper\gradle-wrapper.properties" echo zipStorePath=wrapper/dists
>>"%LOG%" echo CREATE gradle\wrapper\gradle-wrapper.properties
if "%FROM_BUILD%"=="0" echo   Restaurado gradle\wrapper\gradle-wrapper.properties
exit /b 0

:wrong_folder
echo.
echo ERROR: Este BAT debe estar en la raiz de Mirage Projector.
echo No encontre gradle.properties con mod_id=mirage_projector.
echo No se elimino nada.
echo.
if "%FROM_BUILD%"=="0" pause
endlocal & exit /b 1
