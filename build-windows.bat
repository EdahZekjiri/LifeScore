@echo off
REM Baut das Projekt ohne Maven mit einem JDK ab Version 17.
cd /d "%~dp0"
if not exist target\classes mkdir target\classes
REM Relative Pfade funktionieren auch in einem Projektordner mit Leerzeichen.
(for /r src\main\java %%f in (*.java) do @echo "%%f") > target\sources.txt
javac --release 17 -encoding UTF-8 -d target\classes @target\sources.txt
if errorlevel 1 exit /b 1
jar --create --file LifeScore.jar --main-class at.lifescore.Main -C target\classes .
if errorlevel 1 exit /b 1
echo Fertig: LifeScore.jar
