# Abgleich mit Life Score-2.pdf

Stand: 07.10.2026, nach der UI- und Avatar-Überarbeitung (Version 1.1). Quelle: die vier Seiten des bereitgestellten PDFs. Auch die grün markierten funktionalen und nichtfunktionalen Anforderungen wurden berücksichtigt. Redaktionelle Hinweise im PDF wurden als Dokumentinhalt behandelt, nicht als Arbeitsanweisung.

**Ergebnis: Das Projekt erfüllt noch nicht alle Anforderungen.** Von zehn funktionalen Anforderungen sind sechs im Umfang der lokalen Anwendung umgesetzt, eine teilweise umgesetzt und drei offen. Eine schönere Oberfläche ersetzt die fehlenden Funktionen nicht.

## Funktionale Anforderungen

| ID | Anforderung | Status | Beleg und verbleibende Lücke |
| --- | --- | --- | --- |
| FR-01 | Täglicher Fragebogen | Umgesetzt | Fünf verständlich beschriftete Bewertungen von 0 bis 10; Datum, Vorschau und lokale Speicherung. Neue Antworten werden nun gespeichert und wieder geladen. |
| FR-02 | Life-Score-Berechnung | Umgesetzt für den vorhandenen Fragebogen | `ScoreService` validiert die fünf Bewertungen, gewichtet sie und normalisiert auf 0–100. Keine zusätzliche Erfassung objektiver Aktivitätsmengen wie Stunden oder Schritte. |
| FR-03 | Score-Historie | Umgesetzt | CSV-Persistenz, Tabelle aller Tage und Diagramm der letzten 14 Einträge. Das Dashboard nennt das tatsächliche Datum des letzten Scores. |
| FR-04 | Nachträgliche Einträge für einen konfigurierbaren Zeitraum | Teilweise | Vergangene Tage können erfasst und bearbeitet werden; zukünftige Tage werden in der UI gesperrt. Der rückwirkende Zeitraum ist weiterhin unbeschränkt und nicht konfigurierbar. |
| FR-05 | Konto erstellen, anmelden, löschen | Offen | Keine Konten, Anmeldung, Kontolöschung oder Benutzertrennung vorhanden. Offline-Nutzung allein erfüllt diese separate Anforderung nicht. |
| FR-06 | Offline-Nutzung ohne Konto | Umgesetzt | Die Kernfunktionen benötigen nur Java und die lokale CSV-Datei. Keine externen Laufzeitbibliotheken, Server oder Netzwerkaufrufe. |
| FR-07 | Level und Belohnungen | Umgesetzt | Reale Check-ins vergeben XP. Sechs Avatar-Extras werden an festen Levelschwellen freigeschaltet und können angelegt werden. Das höchste erreichte Level wird gespeichert; verdiente Extras bleiben bei späteren Tageskorrekturen erhalten. |
| FR-08 | Avatar anpassen und Optionen freischalten | Umgesetzt | Sofort sichtbarer Avatar in der Seitenleiste; eigene Gestaltungsseite mit drei Figuren und separaten Hintergründen, Rahmen und Accessoires. Gesperrte Optionen zeigen Level/XP; angelegte Looks und Freischaltungen bleiben nach Neustart erhalten. |
| FR-09 | Deutsch/Englisch umschalten | Offen | Oberfläche und Fehlermeldungen sind nur deutsch; kein Sprachwechsel und keine gespeicherte Sprachpräferenz. |
| FR-10 | Autorisierte Fragenverwaltung | Offen | Fünf feste Fragen und Gewichtungen im Quellcode. Keine Admin-Anmeldung und keine Oberfläche zum Erstellen, Ändern oder Deaktivieren von Fragen. |

## Datenhaltung

| Daten aus dem PDF | Vor der Überarbeitung | Aktueller Stand |
| --- | --- | --- |
| Benutzername, Einstellungen, Sprache | Nicht vorhanden | Weiterhin offen. |
| Tägliche Antworten | Nicht gespeichert | Alle fünf Bewertungen werden für neue bzw. aktualisierte Einträge gespeichert. Alte Antworten sind nicht rekonstruierbar und werden ausdrücklich als fehlend markiert. |
| Score-Historie | Datum, Score und XP in CSV | Erhalten; zusätzlich grafischer Verlauf und Bearbeitungsmöglichkeit. |
| Aktivitätsdaten | Nur subjektive Bewertungen im Formular | Die Bewertungen werden nun gespeichert. Separate Mengenwerte wie Schlafdauer, Bewegungsminuten oder erledigte Aufgaben sind weiterhin nicht vorhanden. |
| Level und Erfahrungspunkte | XP gespeichert, Level berechnet | Unverändert nachvollziehbar; Tageskorrekturen ersetzen bisherige XP und erzeugen keine doppelten Punkte. |
| Freigeschaltete Belohnungen/Avatare | Nur berechneter Text | Auswahl in vier Kategorien und höchstes erreichtes Level werden in `avatar.properties` gespeichert. Der zentrale Katalog leitet daraus alle verfügbaren Extras ab. |
| Fragenkatalog und Gewichtungen | Fest im Java-Code | Weiterhin nicht als veränderbare Anwendungsdaten gespeichert. |
| Sprache, Benachrichtigungen, Darstellung | Nicht vorhanden | Keine Einstellungsverwaltung; die neue Gestaltung ist fest vorgegeben. |
| Lokaler Speicher / Datenbank | CSV-Datei | CSV bleibt bewusst erhalten. Für den einfachen lokalen Prototyp geeignet, aber ohne Mehrbenutzerverwaltung oder Zugriffsschutz. |
| Schnelle Updates über Datenbank | Nicht vorhanden | Kein Update-Mechanismus. Im PDF bleibt unklar, ob damit App-Versionen oder aktualisierte Fragen gemeint sind; vor Umsetzung konkretisieren. |

## Nichtfunktionale Anforderungen

| ID | Status | Bewertung |
| --- | --- | --- |
| NFR-01: Dashboard innerhalb von drei Sekunden | Nicht nachgewiesen | Kein belastbarer Kaltstart-Benchmark auf definierten Zielgeräten. CSV-Laden findet noch im Swing-Thread statt; große Historien müssen gesondert geprüft werden. |
| NFR-02: Fragebogen ohne Schulung bedienbar | Verbessert, Abnahme offen | Klare Fragen, erklärte Skala, sichtbare Werte, Tastaturbedienung der Slider, Vorschau und Speicherstatus. Ein Test mit unerfahrenen Nutzern steht aus. |
| NFR-03: Zielplattformen | Teilweise nachgewiesen | Java-Standardbibliothek und Kompilierung mit `--release 17`. Ausgeführt auf diesem Mac mit JDK 27; weder echter Java-17-Laufzeittest noch Windows-/Linux-Abnahme. Im PDF fehlt eine verbindliche Plattformliste. |
| NFR-04: Schutz persönlicher Daten | Nicht ausreichend erfüllt | Klartext-CSV und Avatar-Profildatei ohne eigene Zugangskontrolle oder Verschlüsselung. Offline ist keine Zugriffsschutzmaßnahme. Betriebssystemrechte allein wurden nicht als ausreichender Nachweis gewertet. |
| NFR-05: Erhalt gespeicherter Daten nach Neustart | Für vorhandene Einträge getestet | Score, XP, Antworten und Avatar-Auswahl einschließlich dauerhaft verdienter Extras werden wieder geladen. Alte CSV-Dateien bleiben kompatibel. Zuerst wird eine temporäre Datei geschrieben, danach die Zieldatei ersetzt; atomar, wenn das Dateisystem dies unterstützt. Keine Garantie gegen Stromausfall, gleichzeitige App-Instanzen oder Hardwarefehler. Für die noch fehlenden Datenarten gibt es noch keine Persistenz. |
| NFR-06: Offline-Verfügbarkeit | Für Kernfunktionen umgesetzt | Fragebogen, Berechnung, Verlauf, XP und Avatar greifen ausschließlich auf lokale Dateien zu. Kein gesonderter Test unter aktiv blockiertem Netzwerk durchgeführt; der Anwendungscode enthält keine Netzwerkzugriffe. |

## Algorithmus aus dem PDF

Die vorhandene Berechnung entspricht dem beschriebenen Prinzip für die fünf Bewertungswerte:

```text
Score = round((Schlaf × 0,25 + Bewegung × 0,20 + Ernährung × 0,20
             + Produktivität × 0,20 + Soziales × 0,15) × 10)
XP pro Tag = 10 + ganzzahlig(Score / 10)
Level = ganzzahlig(Summe aller gespeicherten XP / 100) + 1
```

Die Gewichtungen ergeben zusammen 1,0. Das Beispiel `8, 6, 7, 9, 5` ergibt 71,5 und damit gerundet **72 Punkte**. Jede Speicherung berechnet Score und XP neu. Das Repository ersetzt den Eintrag desselben Datums, statt XP zusätzlich anzuhäufen. Die Formel und die Schwellenwerte sind Implementierungsentscheidungen des bestehenden Prototyps; das PDF legt keine konkreten Zahlen fest.

Noch offen bleibt die Verwaltung veränderbarer Fragen und Gewichte. Avatar-Freischaltungen sind jetzt tatsächlich nutzbar: Level 2 Schal/Salbeirahmen, Level 3 Waldgrün, Level 4 Sternennacht, Level 5 Goldrahmen und Level 7 Krone. Bei späteren Änderungen am Katalog muss auch geklärt werden, ob historische Ergebnisse ihre damaligen Gewichtungen behalten.

## Änderungen in diesem Arbeitsschritt

- Neue grün-cremefarbene Swing-Oberfläche mit Seitenleiste, Karten, Score-Ring, Kennzahlen und Verlaufsgrafik.
- Check-in mit fünf erklärten Skalen, sichtbaren Gewichtungen, Live-Vorschau und Speicherbestätigung.
- Verlauf mit Datum, Score, XP, Antwortstatus und Aktion zum Bearbeiten eines ausgewählten Tages.
- Erklärungsseite mit Formel, Beispiel, XP-Regeln und ehrlichen Prototyp-Grenzen.
- Speicherung der Antworten mit rückwärtskompatiblem Lesen alter CSV-Dateien.
- Schreiben über eine temporäre Datei; unlesbare oder widersprüchliche Daten werden nicht überschrieben.
- Keine Darstellung alter Daten als heutiger Score; Zukunftseinträge im Formular gesperrt.
- Weiterhin deutsche Erklärungen direkt neben dem Java-Code; Architektur und Vorführablauf ergänzt.
- Sichtbarer Avatar in der Seitenleiste und eigene Avatar-Seite mit kombinierbaren Looks, Vorschauen und fachlich geprüften Sperren.
- Dauerhafte Freischaltungen und Auswahl in einer lokalen Profildatei; fehlerhafte Profile werden nicht überschrieben.
- Vollständiges ZIP-Paket mit Quellcode, Tests, Dokumentation, startbarer JAR und Startskripten.

Die tatsächliche Nutzerdatendatei wurde bei Entwicklung und Tests nicht geändert. Testdaten lagen in eigenen temporären Verzeichnissen.

## Verifikation

- Alle Produktions- und Testquellen mit `javac --release 17 -encoding UTF-8` kompiliert.
- `ScoreServiceSmokeTest`: Maximalwert, gemischtes Beispiel, Level-Schwelle.
- `EntryRepositorySmokeTest`: leerer Start, alte CSV, Erhalt alter Ergebnisse, neue Antworten, erneutes Laden, Tagesersetzung ohne doppelte XP, unveränderte Datei bei Lesefehlern.
- `AvatarProgressSmokeTest`: alle Freischaltschwellen, Sperren, kombinierbare Looks, dauerhafte Belohnungen trotz XP-Korrektur, Profil-Neustart und Fehlerschutz.
- `AvatarUiSmokeTest`: Levelaufstieg durch echten Check-in, Auswahl/Anlegen, synchroner sichtbarer Avatar, Neustart und unveränderte Auswahl bei Speicherfehler.
- `UiSmokeTest`: echte Swing-Komponenten mit temporärer Datenablage; Vorschau, Speichern, Bearbeiten, neue Fensterinstanz, Zukunftssperre und Rücksetzen bei einem neuen Datum.
- Swing-Ansichten als Bilder gerendert und visuell geprüft: leerer Start, befülltes Dashboard, Check-in, Verlauf, Avatar mit gesperrten/freien Extras und Erklärung sowie Mindestfenstergröße. Bei kleiner Höhe sind die Seiten vertikal scrollbar.
- Die native Computersteuerung konnte das laufende Java-Fenster nicht auswählen. Deshalb erfolgte die Layoutprüfung über das Rendern der echten Swing-Komponenten; ein vollständiger manueller Maus-/Tastaturdurchlauf bleibt als Abnahme sinnvoll.

## Sinnvolle Reihenfolge für die vollständige Umsetzung

1. Konfigurierbaren Nachtragszeitraum und persistente App-Einstellungen ergänzen.
2. Sprachumschaltung mit ausgelagerten Übersetzungen ergänzen.
3. Benutzer- und Sicherheitskonzept festlegen; anschließend optionale Konten und geschützte Datenhaltung umsetzen.
4. Datengetriebenen Fragenkatalog mit berechtigter Admin-Oberfläche und Versionskonzept ergänzen.
5. Zielplattformen, Lastfälle, Startzeitmessung und Usability-Abnahme verbindlich definieren und prüfen.
