# Life Score 1.1 – Java-Projekt mit Avatar und Level-Belohnungen

Eine lokale Desktop-Anwendung zum Reflektieren des Alltags. Fünf Bewertungen ergeben einen nachvollziehbaren Life Score. Dashboard, Verlauf, XP und ein anpassbarer Avatar machen den Fortschritt sichtbar.

**Direkt starten:** [START-HIER.md](START-HIER.md). Die vollständige ZIP-Ausgabe enthält auch die startbare `LifeScore.jar` und Startskripte für macOS, Windows und Linux. Erforderlich ist eine installierte Java-Laufzeit ab Version 17.

## Was funktioniert?

- Dashboard mit Score-Ring, tatsächlichem Eintragsdatum, Durchschnitt, Check-in-Zähler und Level-Fortschritt.
- Fragebogen mit fünf erklärten Skalen, Gewichtungen und Live-Vorschau.
- Speicherung von Datum, Score, XP und Antworten in einer lokalen CSV-Datei.
- Verlaufsgrafik, vollständige Tabelle und Bearbeiten vorhandener Tage ohne doppelte XP.
- Rückwirkende Einträge und Sperre für zukünftige Check-ins.
- **Sofort sichtbarer Avatar in der Seitenleiste:** Fuchs, Katze und Bär stehen kostenlos bereit.
- **Eigene Avatar-Seite:** Auswahl von Figur, Hintergrund, Rahmen und Accessoire mit visuellen Vorschauen.
- **Echte Level-Belohnungen:** Level 2 Schal/Salbeirahmen, Level 3 Waldgrün, Level 4 Sternennacht, Level 5 Goldrahmen, Level 7 Krone.
- Auswahl und dauerhaft erreichte Freischaltungen werden lokal gespeichert und nach Neustart wiederhergestellt.
- Erklärungsseite mit Score-Formel, XP-Regeln und transparenten Grenzen.

Die App läuft vollständig lokal und benötigt keine externen Laufzeitbibliotheken oder Netzwerkverbindung. Alle neuen Figuren werden mit Java-Zeichenbefehlen erzeugt; zusätzliche Bilder müssen weder geladen noch installiert werden.

## Starten und bauen

Fertig gebaute Anwendung im entpackten Projektordner:

```sh
java -jar LifeScore.jar
```

Quellcode in IntelliJ: Ordner oder `pom.xml` öffnen, JDK 17+ auswählen, Arbeitsverzeichnis auf den Projektordner setzen und `src/main/java/at/lifescore/Main.java` ausführen.

Neu bauen unter macOS/Linux:

```sh
sh build.sh
```

Unter Windows `build-windows.bat` ausführen. Zum Bauen wird ein JDK ab Version 17 benötigt; zum Starten der enthaltenen JAR genügt eine kompatible Java-Laufzeit. Die Skripte benötigen weder Maven noch zusätzliche Bibliotheken.

## Tests

```sh
sh test.sh
sh test.sh --ui
```

Der erste Aufruf baut das Projekt und prüft Score-Berechnung, Speicherung sowie Avatar-Regeln/Persistenz. `--ui` führt zusätzlich zwei Integrationstests mit echten Swing-Komponenten aus und benötigt eine grafische Sitzung.

Die fünf Testklassen lassen sich auch direkt in IntelliJ über ihre `main`-Methoden starten:

- `ScoreServiceSmokeTest`: Gewichtung und Levelberechnung.
- `EntryRepositorySmokeTest`: alte CSV-Dateien, Antworten, Neustart, Ersetzen und Schutz beschädigter Dateien.
- `AvatarProgressSmokeTest`: jede XP-Schwelle, gesperrte Extras, kombinierbare Looks, dauerhaftes Inventar und Profilspeicherung.
- `UiSmokeTest`: Check-in-Vorschau, Speichern, Bearbeiten, Neustart und Datumsregeln.
- `AvatarUiSmokeTest`: tatsächlicher Levelaufstieg durch Check-in, neue nutzbare Extras, synchroner Seitenleistenavatar, Neustart und Fehler beim Profilspeichern.

Die Tests verwenden temporäre Verzeichnisse. Sie verändern keine persönlichen Einträge. Sie sind eigenständige Java-Programme und werden **nicht automatisch durch `mvn test` gestartet**.

Geprüft: Kompilierung mit `--release 17` und Ausführung auf macOS mit JDK 27. Windows/Linux und eine echte Java-17-Laufzeit sind noch nicht separat getestet.

## Aufbau

```text
src/main/java/at/lifescore/
├── Main.java                         Startet und verbindet die Bausteine
├── model/
│   ├── DailyAnswers.java             Die fünf Antworten
│   ├── DailyEntry.java               Datum, Score, XP und Antworten
│   ├── AvatarOption.java             Zentraler Katalog aller Looks und Levelschwellen
│   └── AvatarProfile.java            Auswahl und dauerhaft erreichte Freischaltungen
├── repository/
│   ├── EntryRepository.java          CSV lesen und sicher ersetzen
│   └── AvatarRepository.java         Avatar-Profil lesen und sicher ersetzen
├── service/ScoreService.java         Score-, XP- und Level-Berechnung
└── ui/
    ├── LifeScoreFrame.java           Navigation und Verbindung der Ansichten
    ├── UiTheme.java                  Farben, Schrift, Karten und Knöpfe
    ├── ScoreVisuals.java             Score-Ring und Verlauf
    ├── AvatarView.java               Skalierbare Figuren und sichtbare Extras
    └── AvatarStudio.java             Auswahl, Vorschau, Sperren und Speicherfeedback
```

Deutsche Kommentare direkt neben dem Code erklären weiterhin Zweck und Wirkung. [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) beschreibt die Zusammenhänge und einen Vorführablauf.

## Daten und verbleibende Anforderungen

`data/life-score-history.csv` speichert die Tageswerte. `data/avatar.properties` speichert den Look und das höchste erreichte Level; daraus ergeben sich alle dauerhaft verdienten Extras. Alte CSV-Dateien bleiben lesbar, fehlende historische Antworten werden nicht erfunden. Beide Dateien werden beim Speichern zunächst vollständig temporär geschrieben und anschließend ersetzt, bevorzugt atomar.

Die ZIP-Datei enthält das vollständige Projekt ohne persönliche Laufzeitdaten, IDE-Caches oder Testdaten. Im ursprünglichen Projektordner bleiben vorhandene Einträge erhalten.

Die Dateien sind nicht verschlüsselt. Gleichzeitige Schreibzugriffe mehrerer App-Instanzen sind nicht abgesichert. Nicht alle Anforderungen des Gesamtkonzepts sind bereits erfüllt: Konten, Sprachwechsel, Admin-Fragenverwaltung und konfigurierbare Nachträge stehen weiterhin aus. Der aktualisierte [Anforderungsabgleich](docs/REQUIREMENTS_REVIEW.md) und [Ausbauplan](docs/NEXT_STEPS.md) benennen diese Grenzen ausdrücklich.

## Vollständige ZIP-Ausgabe selbst erzeugen

Nach dem Build mit Python 3 `python3 scripts/package.py` ausführen. Die geprüfte Datei liegt anschließend unter `delivery/Life-Score-Projekt.zip`. Das Skript übernimmt ausführbare Startskripte und schließt persönliche Daten sowie IDE-Caches aus.
