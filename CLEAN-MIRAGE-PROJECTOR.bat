@echo off
setlocal EnableExtensions EnableDelayedExpansion
cd /d "%~dp0"

set "FROM_BUILD=0"
if /I "%~1"=="--from-build" set "FROM_BUILD=1"
set "LOG=%CD%\cleanup-mirage-projector.log"
set /a REMOVED=0
set /a SKIPPED=0
set /a FAILED=0

>"%LOG%" echo Mirage Projector cumulative pre-build cleanup - 1.0.51
>>"%LOG%" echo Started: %DATE% %TIME%

if not exist "gradle.properties" goto :wrong_folder
findstr /b /c:"mod_id=mirage_projector" "gradle.properties" >nul 2>nul
if errorlevel 1 goto :wrong_folder

if "%FROM_BUILD%"=="0" (
    echo ============================================================
    echo      MIRAGE PROJECTOR - LIMPIEZA ACUMULATIVA 1.0.51
    echo ============================================================
    echo Proyecto: %CD%
    echo.
)

rem Runtime/source leftovers that can survive when a newer snapshot is copied
rem over an older Windows project directory. Keep this list cumulative.
for %%F in (
    "src\main\java\celerbi\mirageprojector\block\DuplicatingLecternBlock.java"
    "src\main\java\celerbi\mirageprojector\block\ImprovedCoreBlock.java"
    "src\main\java\celerbi\mirageprojector\blockentity\ImprovedCoreBlockEntity.java"
    "src\main\java\celerbi\mirageprojector\client\ImprovedCoreRenderer.java"
    "src\main\java\celerbi\mirageprojector\mixin\client\CryingObsidianGrowthMixin.java"
    "src\main\java\celerbi\mirageprojector\mixin\client\CryingObsidianRandomTickStateMixin.java"
    "src\main\java\celerbi\mirageprojector\mixin\client\RenderLayerMixin.java"
    "src\main\java\celerbi\mirageprojector\network\MirageLightSourceSyncPayload.java"
    "src\main\java\celerbi\mirageprojector\network\MirageLightSectionSyncPayload.java"
    "tools\verify_dev76a_legacy_payload_tombstone.py"
    "src\main\resources\assets\mirage_projector\models\item\improved_amethyst_core.json"
    "src\main\resources\assets\mirage_projector\models\item\improved_core_item_base.json"
    "src\main\resources\assets\mirage_projector\models\item\improved_diamond_core.json"
    "src\main\resources\assets\mirage_projector\models\item\improved_glass_core.json"
    "src\main\resources\assets\mirage_projector\models\item\improved_netherite_core.json"
    "src\main\resources\assets\mirage_projector\models\item\improved_quartz_core.json"
    "src\main\resources\data\mirage_projector\loot_table\blocks\improved_amethyst_core.json"
    "src\main\resources\data\mirage_projector\loot_table\blocks\improved_diamond_core.json"
    "src\main\resources\data\mirage_projector\loot_table\blocks\improved_glass_core.json"
    "src\main\resources\data\mirage_projector\loot_table\blocks\improved_netherite_core.json"
    "src\main\resources\data\mirage_projector\loot_table\blocks\improved_quartz_core.json"
    "src\main\resources\data\mirage_projector\recipe\improved_amethyst_core.json"
    "src\main\resources\data\mirage_projector\recipe\improved_diamond_core.json"
    "src\main\resources\data\mirage_projector\recipe\improved_glass_core.json"
    "src\main\resources\data\mirage_projector\recipe\improved_netherite_core.json"
    "src\main\resources\data\mirage_projector\recipe\improved_quartz_core.json"
    "src\main\java\celerbi\mirageprojector\client\ClientHeldLanterns.java"
    "src\main\java\celerbi\mirageprojector\item\MirageLanternItem.java"
    "src\main\java\celerbi\mirageprojector\block\MirageWallDisplayBlock.java"
    "src\main\java\celerbi\mirageprojector\block\MirageWallIlluminatorBlock.java"
    "src\main\resources\assets\mirage_projector\blockstates\mirage_wall_illuminator.json"
    "src\main\resources\assets\mirage_projector\models\block\mirage_wall_illuminator.json"
    "src\main\resources\assets\mirage_projector\models\item\mirage_wall_illuminator.json"
    "src\main\resources\data\mirage_projector\loot_table\blocks\mirage_wall_illuminator.json"
) do (
    if exist "%%~F" (
        >>"%LOG%" echo DELETE %%~F
        del /f /q "%%~F" >nul 2>nul
        if exist "%%~F" (
            >>"%LOG%" echo ERROR DELETE %%~F
            set /a FAILED+=1
        ) else (
            set /a REMOVED+=1
        )
    ) else (
        set /a SKIPPED+=1
    )
)

rem Build output is always regenerated and can retain deleted classes/resources.
if exist "build" (
    >>"%LOG%" echo DELETE DIR build
    rmdir /s /q "build" >nul 2>nul
    if exist "build" (
        >>"%LOG%" echo ERROR DELETE DIR build
        set /a FAILED+=1
    ) else (
        set /a REMOVED+=1
    )
)

rem Keep wrapper metadata recoverable without touching .gradle-dist, run or worlds.
if not exist "gradle\wrapper" mkdir "gradle\wrapper" >nul 2>nul
if not exist "gradle\wrapper\gradle-wrapper.properties" (
    >"gradle\wrapper\gradle-wrapper.properties" echo distributionBase=GRADLE_USER_HOME
    >>"gradle\wrapper\gradle-wrapper.properties" echo distributionPath=wrapper/dists
    >>"gradle\wrapper\gradle-wrapper.properties" echo distributionUrl=https\://services.gradle.org/distributions/gradle-9.2.1-bin.zip
    >>"gradle\wrapper\gradle-wrapper.properties" echo distributionSha256Sum=72f44c9f8ebcb1af43838f45ee5c4aa9c5444898b3468ab3f4af7b6076c5bc3f
    >>"gradle\wrapper\gradle-wrapper.properties" echo networkTimeout=10000
    >>"gradle\wrapper\gradle-wrapper.properties" echo validateDistributionUrl=true
    >>"gradle\wrapper\gradle-wrapper.properties" echo zipStoreBase=GRADLE_USER_HOME
    >>"gradle\wrapper\gradle-wrapper.properties" echo zipStorePath=wrapper/dists
    >>"%LOG%" echo CREATE gradle\wrapper\gradle-wrapper.properties
)

if !FAILED! GTR 0 goto :cleanup_failed

>>"%LOG%" echo Finished OK: %DATE% %TIME%
if "%FROM_BUILD%"=="0" (
    echo Limpieza terminada correctamente.
    echo Archivos/directorios eliminados: !REMOVED!
    echo Entradas ya ausentes: !SKIPPED!
    echo Log: %LOG%
    echo.
)
endlocal & exit /b 0

:wrong_folder
echo ERROR: CLEAN-MIRAGE-PROJECTOR.bat no esta en la raiz correcta de Mirage Projector.
>>"%LOG%" echo ERROR WRONG FOLDER
endlocal & exit /b 1

:cleanup_failed
echo ERROR: la limpieza acumulativa encontro !FAILED! fallo(s). Revisa cleanup-mirage-projector.log.
>>"%LOG%" echo FAILED COUNT !FAILED!
endlocal & exit /b 1
