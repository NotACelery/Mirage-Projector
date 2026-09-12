@echo off
setlocal EnableExtensions EnableDelayedExpansion
cd /d "%~dp0"

set "GRADLE_VERSION=9.2.1"
set "DIST_ROOT=%CD%\.gradle-dist"
set "DIST_DIR=%DIST_ROOT%\gradle-%GRADLE_VERSION%"
set "DIST_ZIP=%DIST_ROOT%\gradle-%GRADLE_VERSION%-bin.zip"
set "JAVA_EXE="
set "BUILD_EXIT=1"

set "MOD_VERSION="
for /f "tokens=2 delims==" %%V in ('findstr /b /c:"mod_version=" "gradle.properties" 2^>nul') do set "MOD_VERSION=%%V"
if not defined MOD_VERSION set "MOD_VERSION=unknown"

title Mirage Projector - Build %MOD_VERSION%

echo ============================================================
echo          MIRAGE PROJECTOR - BUILD %MOD_VERSION%
echo ============================================================
echo Directorio: %CD%
echo.

if not exist "gradle.properties" goto :wrong_folder
findstr /b /c:"mod_id=mirage_projector" "gradle.properties" >nul 2>nul
if errorlevel 1 goto :wrong_folder

echo Ejecutando limpieza acumulativa previa al build...
if not exist "CLEAN-MIRAGE-PROJECTOR.bat" goto :cleanup_missing
call "%CD%\CLEAN-MIRAGE-PROJECTOR.bat" --from-build
set "CLEANUP_EXIT=%ERRORLEVEL%"
if not "%CLEANUP_EXIT%"=="0" goto :cleanup_failed
echo Limpieza previa terminada correctamente.
echo.

call :find_java21
if not defined JAVA_EXE goto :java_missing

for %%I in ("%JAVA_EXE%") do set "JAVA_BIN=%%~dpI"
for %%I in ("%JAVA_BIN%..") do set "JAVA_HOME=%%~fI"
set "PATH=%JAVA_HOME%\bin;%PATH%"

echo Java 21 encontrado:
echo   %JAVA_EXE%
"%JAVA_EXE%" -version
if errorlevel 1 goto :java_broken
echo.

if not exist "%DIST_DIR%\bin\gradle.bat" (
    echo Gradle %GRADLE_VERSION% no esta descargado.
    if not exist "%DIST_ROOT%" mkdir "%DIST_ROOT%"
    if errorlevel 1 goto :mkdir_failed

    if exist "%DIST_ZIP%" del /q "%DIST_ZIP%"

    echo Descargando Gradle %GRADLE_VERSION%...
    where curl.exe >nul 2>nul
    if not errorlevel 1 (
        curl.exe -L --fail --retry 3 --output "%DIST_ZIP%" "https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip"
    ) else (
        powershell -NoProfile -ExecutionPolicy Bypass -Command "$ProgressPreference='SilentlyContinue'; Invoke-WebRequest -Uri 'https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip' -OutFile '%DIST_ZIP%'"
    )
    if errorlevel 1 goto :download_failed
    if not exist "%DIST_ZIP%" goto :download_failed

    echo.
    echo Descomprimiendo Gradle...
    powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -LiteralPath '%DIST_ZIP%' -DestinationPath '%DIST_ROOT%' -Force"
    if errorlevel 1 goto :extract_failed
)

if not exist "%DIST_DIR%\bin\gradle.bat" goto :gradle_missing

echo.
echo Compilando Mirage Projector %MOD_VERSION%...
echo La primera compilacion puede descargar dependencias de NeoForge.
echo Este BAT ejecuta CLEAN-MIRAGE-PROJECTOR.bat antes de compilar.
echo.
echo Iniciando Gradle...
echo ------------------------------------------------------------

cmd /d /s /c ""%DIST_DIR%\bin\gradle.bat" --no-daemon clean build --stacktrace"
set "BUILD_EXIT=%ERRORLEVEL%"

echo ------------------------------------------------------------
echo Gradle termino con codigo: %BUILD_EXIT%
if not "%BUILD_EXIT%"=="0" goto :build_failed

set "JAR_FILE="
for /f "delims=" %%F in ('dir /b /a-d "build\libs\*.jar" 2^>nul') do if not defined JAR_FILE set "JAR_FILE=build\libs\%%F"
if not defined JAR_FILE goto :jar_missing

echo.
echo ============================================================
echo COMPILACION TERMINADA CORRECTAMENTE
echo JAR generado:
echo   %CD%\%JAR_FILE%
echo ============================================================
goto :success

:find_java21
if defined JAVA_HOME call :try_jdk "%JAVA_HOME%"
if defined JAVA_EXE exit /b 0
if defined JAVA_HOME_21_X64 call :try_jdk "%JAVA_HOME_21_X64%"
if defined JAVA_EXE exit /b 0
if defined JDK21_HOME call :try_jdk "%JDK21_HOME%"
if defined JAVA_EXE exit /b 0
for /d %%D in ("%ProgramFiles%\Eclipse Adoptium\jdk-21*") do if not defined JAVA_EXE call :try_jdk "%%~fD"
if defined JAVA_EXE exit /b 0
for /d %%D in ("%LOCALAPPDATA%\Programs\Eclipse Adoptium\jdk-21*") do if not defined JAVA_EXE call :try_jdk "%%~fD"
if defined JAVA_EXE exit /b 0
for /d %%D in ("%ProgramFiles%\Java\jdk-21*") do if not defined JAVA_EXE call :try_jdk "%%~fD"
if defined JAVA_EXE exit /b 0
for /d %%D in ("%ProgramFiles%\Microsoft\jdk-21*") do if not defined JAVA_EXE call :try_jdk "%%~fD"
if defined JAVA_EXE exit /b 0
for /d %%D in ("%ProgramFiles%\Amazon Corretto\jdk21*") do if not defined JAVA_EXE call :try_jdk "%%~fD"
if defined JAVA_EXE exit /b 0
for /d %%D in ("%USERPROFILE%\.jdks\*21*") do if not defined JAVA_EXE call :try_jdk "%%~fD"
if defined JAVA_EXE exit /b 0
for /f "usebackq delims=" %%J in (`powershell -NoProfile -ExecutionPolicy Bypass -Command "$roots=@($env:APPDATA+'\PrismLauncher\java',$env:LOCALAPPDATA+'\PrismLauncher\java',$env:LOCALAPPDATA+'\Programs\PrismLauncher\java',$env:ProgramFiles+'\PrismLauncher\java'); foreach($root in $roots){if(Test-Path -LiteralPath $root){Get-ChildItem -LiteralPath $root -Filter java.exe -File -Recurse -ErrorAction SilentlyContinue ^| Where-Object {$_.FullName -match '\\bin\\java\.exe$'} ^| ForEach-Object {$_.FullName}}}"`) do if not defined JAVA_EXE call :try_java_exe "%%J"
if defined JAVA_EXE exit /b 0
for /f "delims=" %%J in ('where java.exe 2^>nul') do if not defined JAVA_EXE call :try_java_exe "%%J"
exit /b 0

:try_jdk
if "%~1"=="" exit /b 0
if not exist "%~1\bin\java.exe" exit /b 0
if not exist "%~1\bin\javac.exe" exit /b 0
call :try_java_exe "%~1\bin\java.exe"
exit /b 0

:try_java_exe
if "%~1"=="" exit /b 0
if not exist "%~1" exit /b 0
set "JAVA_VERSION_FILE=%TEMP%\mirage-projector-java-version-%RANDOM%-%RANDOM%.txt"
"%~1" -version >"!JAVA_VERSION_FILE!" 2>&1
findstr /C:"21.0." "!JAVA_VERSION_FILE!" >nul 2>nul
set "JAVA_VERSION_MATCH=!ERRORLEVEL!"
del /q "!JAVA_VERSION_FILE!" >nul 2>nul
if "!JAVA_VERSION_MATCH!"=="0" (
    for %%I in ("%~1") do set "CANDIDATE_BIN=%%~dpI"
    if exist "!CANDIDATE_BIN!javac.exe" set "JAVA_EXE=%~1"
)
exit /b 0

:wrong_folder
echo.
echo ERROR: Este BAT debe estar en la raiz de Mirage Projector.
echo No encontre gradle.properties con mod_id=mirage_projector.
goto :failure

:java_missing
echo.
echo ERROR: No encontre un JDK 21 ejecutable.
echo Mirage Projector requiere Java 21.
echo Ubicacion habitual de Temurin 21:
echo   C:\Program Files\Eclipse Adoptium\jdk-21*\bin\java.exe
goto :failure

:java_broken
echo ERROR: La instalacion de Java 21 encontrada no puede ejecutarse.
goto :failure

:mkdir_failed
echo ERROR: No pude crear la carpeta "%DIST_ROOT%".
goto :failure

:download_failed
echo.
echo ERROR: No se pudo descargar Gradle %GRADLE_VERSION%.
echo Revisa internet, antivirus, proxy o firewall.
goto :failure

:extract_failed
echo ERROR: Gradle se descargo, pero no pudo descomprimirse.
echo Borra la carpeta .gradle-dist y vuelve a ejecutar build.bat.
goto :failure

:gradle_missing
echo ERROR: No existe "%DIST_DIR%\bin\gradle.bat" despues de descomprimir.
goto :failure

:build_failed
echo.
echo ============================================================
echo LA COMPILACION FALLO
echo Copia desde "FAILURE: Build failed" hasta el final y enviamelo.
echo Codigo de salida de Gradle: %BUILD_EXIT%
echo ============================================================
goto :failure

:jar_missing
echo ERROR: Gradle termino correctamente, pero no encontre ningun JAR en build\libs\.
goto :failure

:failure
echo.
echo La ventana queda abierta para poder leer o copiar el error.
echo Presiona una tecla solo cuando hayas terminado de revisarlo.
pause >nul
endlocal & exit /b 1

:success
echo.
echo El JAR esta listo para probar en la instancia de Minecraft.
echo Presiona una tecla para cerrar esta ventana.
pause >nul
endlocal & exit /b 0
