#!/bin/sh
# Wechselt in den entpackten Projektordner, damit Daten immer neben der App liegen.
cd "$(dirname "$0")" || exit 1
# Die beigefügte JAR benötigt nur Java 17 oder neuer, keinen Compiler und kein Maven.
if ! command -v java >/dev/null 2>&1; then
  echo "Bitte Java 17 oder neuer installieren. Danach dieses Startskript erneut öffnen."
  read -r answer
  exit 1
fi
java -jar LifeScore.jar
result=$?
if [ "$result" -ne 0 ]; then
  echo "Start nicht möglich. Bitte prüfen, ob Java 17 oder neuer installiert ist."
  read -r answer
fi
exit "$result"
