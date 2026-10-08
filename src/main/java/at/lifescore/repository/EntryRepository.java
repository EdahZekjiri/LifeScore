package at.lifescore.repository; // Kapselt die lokale Datenspeicherung.

import at.lifescore.model.DailyAnswers; // Enthält die ursprünglichen Bewertungen.
import at.lifescore.model.DailyEntry; // Enthält den vollständigen Tagesdatensatz.
import java.io.IOException; // Beschreibt Fehler beim Dateizugriff.
import java.nio.charset.StandardCharsets; // Sichert einheitliche UTF-8-Dateien.
import java.nio.file.AtomicMoveNotSupportedException; // Erkennt Dateisysteme ohne atomaren Austausch.
import java.nio.file.Files; // Liest und schreibt lokale Dateien.
import java.nio.file.Path; // Beschreibt plattformunabhängige Pfade.
import java.nio.file.StandardCopyOption; // Steuert das sichere Ersetzen der Datendatei.
import java.time.LocalDate; // Liest Datumswerte ohne Zeitzonenverschiebung.
import java.util.ArrayList; // Sammelt und aktualisiert Datensätze.
import java.util.Comparator; // Sortiert die Historie nach Datum.
import java.util.List; // Beschreibt die zurückgegebenen Einträge.

public final class EntryRepository { // Trennt Dateiformat und Oberfläche voneinander.
    private final Path dataFile; // Ermöglicht auch Tests mit einer isolierten temporären Datei.

    public EntryRepository() { // Nutzt im normalen Betrieb den bisherigen Speicherort.
        this(Path.of("data", "life-score-history.csv")); // Bestehende Nutzerdaten bleiben am selben Ort.
    } // Beendet den Standardkonstruktor.

    public EntryRepository(Path dataFile) { // Erlaubt einen expliziten Dateipfad, insbesondere für Tests.
        this.dataFile = dataFile.toAbsolutePath(); // Sichert einen übergeordneten Ordner auch bei einfachen Dateinamen.
    } // Beendet die Initialisierung.

    public Path dataDirectory() { return dataFile.getParent(); } // Hält Avatar-Profil und Historie im selben lokalen bzw. isolierten Testordner.

    public List<DailyEntry> loadAll() { // Liest sowohl alte als auch neue CSV-Dateien.
        List<DailyEntry> entries = new ArrayList<>(); // Beginnt mit einer leeren Historie.
        if (!Files.exists(dataFile)) { // Bei der ersten Nutzung gibt es noch keine Datei.
            return entries; // Eine fehlende Datei ist kein Fehler.
        } // Beendet die Existenzprüfung.
        try { // Übersetzt technische Fehler in verständliche Rückmeldungen.
            for (String line : Files.readAllLines(dataFile, StandardCharsets.UTF_8)) { // Liest alle Zeilen als UTF-8.
                if (line.isBlank() || line.startsWith("date,")) { // Ignoriert Kopfzeile und Leerzeilen.
                    continue; // Wechselt zum nächsten Datensatz.
                } // Beendet die Filterung.
                String[] parts = line.split(",", -1); // Behält auch leere Antwortfelder alter Einträge.
                if (parts.length != 3 && parts.length != 8) { // Erkennt beschädigte Zeilen vor einem Schreibzugriff.
                    throw new IllegalArgumentException("Unerwartete Spaltenzahl."); // Verhindert stillen Datenverlust.
                } // Beendet die Formatprüfung.
                DailyAnswers answers = null; // Alte Scores besitzen keine rekonstruierbaren Antworten.
                if (parts.length == 8 && !String.join("", java.util.Arrays.copyOfRange(parts, 3, 8)).isEmpty()) { // Liest vorhandene Antworten und weist unvollständige Antworten zurück.
                    answers = new DailyAnswers(Integer.parseInt(parts[3]), Integer.parseInt(parts[4]), Integer.parseInt(parts[5]), Integer.parseInt(parts[6]), Integer.parseInt(parts[7])); // Validiert alle fünf Bewertungen.
                } // Beendet das Lesen der Antworten.
                DailyEntry entry = new DailyEntry(LocalDate.parse(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), answers); // Stellt den Tagesdatensatz wieder her.
                if (entries.stream().anyMatch(existing -> existing.date().equals(entry.date()))) { // Verhindert doppelte XP durch beschädigte Dateien.
                    throw new IllegalArgumentException("Doppeltes Datum."); // Meldet widersprüchliche Daten statt sie zu überschreiben.
                } // Beendet die Eindeutigkeitsprüfung.
                entries.add(entry); // Übernimmt den geprüften Eintrag.
            } // Beendet das Lesen der Datei.
        } catch (IOException | RuntimeException exception) { // Fängt Datei- und Formatfehler ab.
            throw new IllegalStateException("Die lokale Historie konnte nicht gelesen werden. Bitte die CSV-Datei prüfen; sie wurde nicht verändert.", exception); // Erklärt den nächsten Schritt.
        } // Beendet die Fehlerbehandlung.
        entries.sort(Comparator.comparing(DailyEntry::date)); // Liefert immer chronologisch sortierte Daten.
        return entries; // Gibt die vollständige Historie zurück.
    } // Beendet die Lademethode.

    public void saveOrReplace(DailyEntry newEntry) { // Ersetzt genau einen Kalendertag, ohne zusätzliche XP anzuhäufen.
        List<DailyEntry> entries = loadAll(); // Ein Lesefehler verhindert jede Änderung an der Datei.
        entries.removeIf(entry -> entry.date().equals(newEntry.date())); // Entfernt den bisherigen Stand dieses Tages.
        entries.add(newEntry); // Übernimmt die neuen Antworten und Ergebnisse.
        entries.sort(Comparator.comparing(DailyEntry::date)); // Hält die Datei lesbar und sortiert.
        List<String> lines = new ArrayList<>(); // Erstellt den vollständigen neuen Dateistand.
        lines.add("date,score,earnedXp,sleep,movement,nutrition,productivity,social"); // Dokumentiert die Spalten direkt in der Datei.
        for (DailyEntry entry : entries) { // Serialisiert jeden Tag.
            DailyAnswers a = entry.answers(); // Alte Einträge dürfen weiterhin ohne Antworten bestehen.
            String answers = a == null ? ",,,,," : "," + a.sleep() + "," + a.movement() + "," + a.nutrition() + "," + a.productivity() + "," + a.social(); // Schreibt fehlende Antworten als leere Felder.
            lines.add(entry.date() + "," + entry.score() + "," + entry.earnedXp() + answers); // Verbindet Ergebnisse mit ihren Eingabewerten.
        } // Beendet die Serialisierung.
        Path temporary = null; // Merkt sich die temporäre Datei für die anschließende Bereinigung.
        try { // Schreibt zunächst separat, damit ein Schreibfehler die alte Historie nicht abschneidet.
            Files.createDirectories(dataFile.getParent()); // Erstellt den lokalen Datenordner bei Bedarf.
            temporary = Files.createTempFile(dataFile.getParent(), "life-score-", ".tmp"); // Nutzt dasselbe Dateisystem für den Austausch.
            Files.write(temporary, lines, StandardCharsets.UTF_8); // Schreibt den neuen Stand vollständig vor dem Ersetzen.
            try { // Bevorzugt einen atomaren Austausch der Datei.
                Files.move(temporary, dataFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); // Tauscht die Historie in einem Dateisystemschritt aus.
            } catch (AtomicMoveNotSupportedException exception) { // Unterstützt auch Dateisysteme ohne atomare Umbenennung.
                Files.move(temporary, dataFile, StandardCopyOption.REPLACE_EXISTING); // Verwendet dort den normalen Austausch.
            } // Beendet die Auswahl des Schreibverfahrens.
        } catch (IOException exception) { // Meldet fehlenden Speicherplatz oder fehlende Schreibrechte.
            throw new IllegalStateException("Der Eintrag konnte nicht lokal gespeichert werden.", exception); // Zeigt keinen falschen Speichernachweis an.
        } finally { // Entfernt eine temporäre Datei auch nach einem Fehler.
            if (temporary != null) { // Prüft, ob überhaupt eine Datei angelegt wurde.
                try { Files.deleteIfExists(temporary); } catch (IOException ignored) { /* Der ursprüngliche Speicherfehler bleibt maßgeblich. */ } // Verhindert das Verdecken der eigentlichen Ursache.
            } // Beendet die temporäre Bereinigung.
        } // Beendet die Speicheroperation.
    } // Beendet das Speichern eines Tages.
} // Beendet das Repository.
