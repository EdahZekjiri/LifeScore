#!/bin/sh
# Verankert die lokale Datenablage im entpackten Projektordner.
cd "$(dirname "$0")" || exit 1
# Startet die enthaltene Anwendung mit der installierten Java-Laufzeit.
exec java -jar LifeScore.jar
