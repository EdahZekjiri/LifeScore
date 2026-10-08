# Life Score 1.1 – direkt starten

Dieses Paket enthält das **vollständige Projekt**, die fertig gebaute Anwendung und den kommentierten Quellcode.

## 1. ZIP vollständig entpacken

Den ganzen Ordner `Life-Score` an einen beschreibbaren Ort entpacken. Nicht direkt aus der ZIP-Datei starten.

## 2. Anwendung öffnen

Voraussetzung: **Java 17 oder neuer**. Die Java-Laufzeit ist nicht im ZIP enthalten.

- **macOS:** `Start-macOS.command` öffnen. Alternativ im Terminal im entpackten Ordner: `sh Start-macOS.command`.
- **Windows:** `Start-Windows.bat` öffnen.
- **Linux:** im entpackten Ordner `sh start-linux.sh` ausführen.
- **Alle Plattformen:** im entpackten Ordner `java -jar LifeScore.jar` ausführen.

Die Startskripte setzen das Arbeitsverzeichnis auf ihren eigenen Ordner. Dadurch werden deine Einträge zuverlässig in dessen Unterordner `data` gespeichert.

## 3. Wo ist der Avatar?

Dein Fuchs ist **sofort links in der Seitenleiste sichtbar**. Über **04 Avatar** oder **Avatar gestalten** auf dem Dashboard öffnest du die vollständige Gestaltung.

Fuchs, Katze und Bär sind von Anfang an frei wählbar. Wähle eine verfügbare Kachel: Die Figur aktualisiert sich sofort und die Auswahl wird lokal gespeichert. Für Figur, Hintergrund, Rahmen und Accessoire gibt es jeweils eine eigene Kategorie. Unterhalb der sichtbaren Kacheln findest du durch Scrollen die weiteren Kategorien.

| Ab Level | Benötigte Gesamt-XP | Neue, tatsächlich anlegbare Extras |
| --- | ---: | --- |
| 2 | 100 | Lieblingsschal und Salbeirahmen |
| 3 | 200 | Waldgrüner Hintergrund |
| 4 | 300 | Sternennacht-Hintergrund |
| 5 | 400 | Goldener Fokus-Rahmen |
| 7 | 600 | Kleine Krone |

Ein Check-in gibt 10 bis 20 XP. Die App zeigt gesperrte Looks mit ihrer Freischaltschwelle. Nach dem Levelaufstieg werden die Kacheln freigegeben. Verdiente Extras bleiben erhalten, auch wenn du einen früheren Check-in korrigierst und die aktuelle XP-Summe sinkt. Rahmen, Hintergrund und ein Accessoire können miteinander kombiniert werden; Schal und Krone gehören zur selben Kategorie und werden alternativ getragen.

## Was steckt im Paket?

- `LifeScore.jar`: fertig gebaute, startbare Anwendung.
- `src/main/java`: vollständiger Quellcode mit deutschen Erklärungen direkt neben dem Code.
- `src/test/java`: fünf ausführbare Tests, darunter echte Check-in- und Avatar-Integrationstests.
- `pom.xml`: Projektimport in IntelliJ oder Maven.
- `build.sh` / `build-windows.bat`: JAR selbst aus dem Quellcode neu bauen (JDK 17+ erforderlich).
- `test.sh`: fachliche Tests; mit `--ui` zusätzlich beide Swing-Tests in einer grafischen Sitzung.
- `README.md` und `docs`: Bedienung, Architektur, Anforderungsabgleich und noch offene Funktionen.
- `data`: zunächst leerer Ordner für deine lokalen Einträge und dein Avatar-Profil.

Die ZIP-Ausgabe startet bewusst ohne persönliche Tagesdaten, ohne künstliche XP und ohne vorab freigeschaltete Extras. Deine bisherigen Daten im ursprünglichen Projektordner bleiben unverändert. Wenn du sie übernehmen möchtest, kopiere bei geschlossener App `data/life-score-history.csv` und – falls vorhanden – `data/avatar.properties` aus dem bisherigen Projekt in den entpackten `data`-Ordner.

## Quellcode bearbeiten

Den gesamten entpackten Ordner in IntelliJ öffnen, Java 17 oder neuer auswählen und `at.lifescore.Main` starten. Nach Quellcodeänderungen die JAR mit dem Build-Skript neu erzeugen; andernfalls startet die JAR weiterhin den zuvor gebauten Stand.

Noch offen aus dem ursprünglichen Gesamtkonzept: optionale Konten, Sprachumschaltung, autorisierte Fragenverwaltung, ein konfigurierbarer Nachtragszeitraum und weitergehender Schutz persönlicher Daten. Die neue Avatar- und Belohnungsfunktion ist vollständig nutzbar; sie benötigt weder Konto noch Internet.
