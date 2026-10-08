# Architektur und Erklärung

Das Projekt verwendet Java 17 als Sprachziel und ausschließlich die Java-Standardbibliothek. Swing stellt die Desktop-Oberfläche bereit. Es gibt keinen Webserver und keine notwendige Internetverbindung.

## Zuständigkeiten

| Datei | Aufgabe |
| --- | --- |
| `Main.java` | Startet im Swing-Thread, installiert das Design und verbindet Service, Repository und Fenster. |
| `ui/UiTheme.java` | Zentrale Farben, Schriftgrößen, Karten und Schaltflächen. Hier lässt sich das Design verändern. |
| `ui/ScoreVisuals.java` | Zeichnet Score-Ring und Verlauf. Berechnet keine fachlichen Scores; zeigt nur übergebene Daten. |
| `ui/LifeScoreFrame.java` | Baut fünf Ansichten auf, liest Formulareingaben und aktualisiert alle Anzeigen nach erfolgreichem Speichern. |
| `service/ScoreService.java` | Validiert Bewertungen und berechnet Score, XP und aktuelles Level. |
| `model/DailyAnswers.java` | Unveränderliche, validierte Antworten für die fünf Lebensbereiche. |
| `ui/AvatarView.java` | Zeichnet drei Figuren, Hintergründe, Rahmen und Accessoires skalierbar mit Java2D. |
| `ui/AvatarStudio.java` | Zeigt den gespeicherten Look, Auswahlkacheln, Sperren und Freischaltmeldungen. |
| `model/AvatarOption.java` | Zentraler Katalog mit Kategorie, Anzeigename und benötigtem Level. |
| `model/AvatarProfile.java` | Validiert die Auswahl und bewahrt dauerhaft erreichte Freischaltungen. |
| `repository/AvatarRepository.java` | Speichert den Look und den Level-Höchststand in `data/avatar.properties`. |
| `model/DailyEntry.java` | Datum, Score, XP und optional die ursprünglichen Antworten. |
| `repository/EntryRepository.java` | Liest und schreibt die lokale CSV; kennt keine Swing-Komponenten. |

## Datenfluss beim Speichern

1. Der Nutzer wählt einen Kalendertag und bewertet die fünf Bereiche.
2. Die Vorschau ruft dieselbe `ScoreService`-Formel wie die spätere Speicherung auf.
3. Beim Speichern wird zuerst die Datumseingabe bestätigt und geprüft. Zukunftstage sind nicht erlaubt.
4. `DailyAnswers` hält die fünf Bewertungen fest; der Service berechnet Score und XP.
5. Das Repository lädt die Historie, ersetzt ausschließlich denselben Kalendertag und schreibt den neuen Stand zuerst in eine temporäre Datei.
6. Die vollständige temporäre Datei ersetzt die bisherige CSV, bevorzugt atomar. Falls das Dateisystem dies nicht unterstützt, erfolgt ein normaler Dateiaustausch.
7. Erst nach erfolgreichem Speichern werden Dashboard, Verlauf und Speicherbestätigung aktualisiert.

Ein wichtiges Swing-Detail: Das Datumsfeld enthält intern auch eine Uhrzeit. Beim Bestätigen kann diese Uhrzeit wechseln, obwohl der Kalendertag gleich bleibt. `loadedDate` verhindert, dass dadurch gerade eingegebene Antworten zurückgesetzt werden. Nur ein echter Tageswechsel lädt andere Antworten.

## Dateiformat und Altbestand

Speicherort: `data/life-score-history.csv`, relativ zum Arbeitsverzeichnis beim Start.

```csv
date,score,earnedXp,sleep,movement,nutrition,productivity,social
2026-10-07,72,17,8,6,7,9,5
```

Auch die bisherigen drei Spalten `date,score,earnedXp` werden gelesen. Beim nächsten regulären Speichern wird das Format erweitert. Bei alten Einträgen bleiben die fünf Antwortfelder leer: Aus einem Gesamtscore lassen sich die damaligen Antworten nicht eindeutig zurückrechnen. Die Oberfläche weist darauf hin, statt Startwerte als historische Antworten auszugeben.

Die Datei wird nicht beim Öffnen migriert oder verändert. Beschädigte Zeilen oder doppelte Datumswerte verhindern das Schreiben. Dieses einfache Repository ist für eine lokale App-Instanz ausgelegt; paralleles Schreiben aus mehreren Instanzen ist nicht abgesichert. Die Datei ist nicht verschlüsselt.

## Darstellung und Verständlichkeit

Die Navigation trennt Überblick, Check-in, Verlauf, Avatar und Erklärung. Jede Kennzahl hat eine Beschriftung. Der Ring zeigt den letzten gespeicherten Score mit dessen Datum. Die Verlaufslinie nutzt tatsächliche Datumsabstände und maximal 14 Einträge; fehlende Tage werden nicht als null interpretiert. Die Tabelle enthält alle Einträge, neueste zuerst.

Standard-Swing-Komponenten bleiben für Felder und Aktionen erhalten. Slider sind mit Pfeiltasten bedienbar und haben zugängliche Namen. Das Diagramm hat eine tabellarische Alternative. Bei kleiner Fensterhöhe wird vertikal gescrollt; Seiten folgen der Fensterbreite.

Die deutschen Kommentare neben den Codezeilen bleiben erhalten. Zusätzlich erklären diese Dokumentation und die In-App-Erklärung die Zusammenhänge. Design, Zeichnung, Fachlogik und Speicherung bleiben getrennt, damit Änderungen nachvollziehbar sind.

## Kurzer Vorführablauf

1. **Überblick:** letzten Score samt Datum, Durchschnitt, Anzahl der Check-ins und XP zeigen.
2. **Check-in:** Skala 0–10 erklären und `8, 6, 7, 9, 5` einstellen. Die Vorschau zeigt 72.
3. **Speichern:** Bestätigung zeigen; im Verlauf erscheint genau ein Eintrag für diesen Tag.
4. **Bearbeiten:** Tag im Verlauf auswählen, Schlaf von 8 auf 10 ändern und aktualisieren. Der Score steigt auf 77; die Anzahl der Tage bleibt gleich.
5. **Erklärung:** Gewichtungen und Formel zeigen. Klar benennen, welche PDF-Funktionen noch fehlen.
6. **Neustart:** Anwendung schließen und aus demselben Arbeitsverzeichnis starten. Ergebnisse und neue Antworten sind weiterhin vorhanden.

Für eine Vorführung am besten einen separaten Projektordner bzw. eine Kopie der Datendatei verwenden, damit persönliche Einträge nicht durch Demonstrationswerte ersetzt werden.


## Avatar und echte Belohnungen

Der Avatar ist bereits beim ersten Start links sichtbar. `AvatarView` zeichnet Figur, Hintergrund, Rahmen und Accessoire mit Java2D. Es gibt keine externen Bilddateien, Downloads oder Bildbibliotheken. Dieselbe Zeichenroutine erzeugt auch die Vorschauen auf den Auswahlkacheln.

`AvatarOption` ist die einzige Quelle für die Freischaltschwellen. `AvatarProfile.equip` prüft die Freischaltung auch dann, wenn die Oberfläche umgangen würde. Eine Auswahl wird zuerst in `avatar.properties` gespeichert und erst danach als angelegt angezeigt. Dadurch bleiben Vorschau und tatsächlich gesicherte Daten konsistent.

Nach dem Speichern eines Check-ins berechnet `ScoreService` das aktuelle Level. `AvatarStudio.updateProgress` übernimmt neue Höchststände und speichert sie. Das Profil enthält vier Optionsschlüssel und `highestLevel`. Alle verfügbaren Extras lassen sich daraus eindeutig ableiten. So bleiben ältere Extras auch nach einem neuen Level oder einer späteren Score-Korrektur erhalten. Die Speicherung dieses Höchststands ist ein bewusstes Produktverhalten: Eine ehrliche Korrektur soll verdiente Gestaltungsmöglichkeiten nicht wieder entziehen.

Ein Fehler beim Profilspeichern macht einen bereits gespeicherten Check-in nicht rückgängig. Die Avatar-Seite erklärt den Fehler; neue Freischaltungen werden erst nach erfolgreicher Speicherung bestätigt. Das nächste Laden der Historie versucht die Fortschrittsübernahme erneut.

## Avatar vorführen

1. Links den sofort sichtbaren Fuchs zeigen; auf **Avatar** klicken.
2. Katze oder Bär auswählen. Große Vorschau und Seitenleiste wechseln gemeinsam.
3. Gesperrte Optionen samt benötigtem Level/XP zeigen. Die Schwellen sind auch im Code in `AvatarOption` verständlich nachzulesen.
4. Mit regulären Check-ins Level 2 erreichen und Schal/Salbeirahmen anlegen. Diese Extras verändern das gezeichnete Aussehen tatsächlich.
5. Anwendung neu starten: Figur und angelegte Extras bleiben unverändert.

Die automatisierten Tests erreichen Schwellen mit isolierten Testdaten. Das ausgelieferte ZIP startet ohne künstlich erhöhte XP und enthält keine persönlichen Einträge.
