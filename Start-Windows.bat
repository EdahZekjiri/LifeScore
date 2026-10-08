@echo off
REM Verankert die Datenablage im Ordner dieses Startskripts.
cd /d "%~dp0"
REM Die enthaltene JAR benoetigt Java 17 oder neuer.
java -jar LifeScore.jar
if errorlevel 1 (
  echo Start fehlgeschlagen. Bitte Java 17 oder neuer installieren bzw. pruefen.
  pause
)
