#!/bin/sh
# Baut die Anwendung ohne externe Bibliotheken. Dafür wird ein JDK ab Version 17 benötigt.
set -eu
cd "$(dirname "$0")"
mkdir -p target/classes
# Die Dateiliste hält den Compileraufruf auch bei vielen Klassen übersichtlich.
find src/main/java -name '*.java' -print > target/sources.txt
javac --release 17 -encoding UTF-8 -d target/classes @target/sources.txt
# Der Manifest-Eintrag macht die Datei direkt mit java -jar startbar.
jar --create --file LifeScore.jar --main-class at.lifescore.Main -C target/classes .
echo "Fertig: LifeScore.jar"
