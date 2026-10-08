#!/bin/sh
# Führt unabhängige Tests mit temporären Daten aus; persönliche Einträge bleiben unverändert.
set -eu
cd "$(dirname "$0")"
sh build.sh
mkdir -p target/test-classes
find src/test/java -name '*.java' -print > target/test-sources.txt
javac --release 17 -encoding UTF-8 -cp target/classes -d target/test-classes @target/test-sources.txt
java -cp target/classes:target/test-classes at.lifescore.service.ScoreServiceSmokeTest
java -cp target/classes:target/test-classes at.lifescore.repository.EntryRepositorySmokeTest
java -cp target/classes:target/test-classes at.lifescore.service.AvatarProgressSmokeTest
# Die beiden Swing-Tests benötigen eine grafische Sitzung; ohne --ui werden nur die drei obigen Tests ausgeführt.
if [ "${1:-}" = "--ui" ]; then
  java -cp target/classes:target/test-classes at.lifescore.ui.UiSmokeTest
  java -cp target/classes:target/test-classes at.lifescore.ui.AvatarUiSmokeTest
fi
