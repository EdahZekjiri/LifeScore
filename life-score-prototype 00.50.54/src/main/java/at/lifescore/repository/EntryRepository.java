package at.lifescore.repository; // Ordnet die Speicherung dem Repository-Paket zu.

import at.lifescore.model.DailyEntry; // Macht das zu speichernde Datenmodell verfügbar.
import java.io.IOException; // Stellt Fehler bei Dateioperationen dar.
import java.nio.charset.StandardCharsets; // Legt UTF-8 als einheitliche Zeichenkodierung fest.
import java.nio.file.Files; // Stellt komfortable Dateioperationen bereit.
import java.nio.file.Path; // Stellt plattformunabhängige Dateipfade bereit.
import java.nio.file.StandardOpenOption; // Legt das Schreibverhalten beim Speichern fest.
import java.time.LocalDate; // Wandelt gespeicherte Datumswerte zurück in Datumsobjekte.
import java.util.ArrayList; // Stellt eine veränderbare Liste für geladene Einträge bereit.
import java.util.Comparator; // Ermöglicht die Sortierung nach Datum.
import java.util.List; // Stellt den allgemeinen Listentyp bereit.

public final class EntryRepository { // Kapselt die lokale Offline-Speicherung der Historie.
    private final Path dataFile = Path.of("data", "life-score-history.csv"); // Bestimmt die CSV-Datei relativ zum Projektordner.

    public List<DailyEntry> loadAll() { // Lädt alle vorhandenen Einträge aus der CSV-Datei.
        List<DailyEntry> entries = new ArrayList<>(); // Erstellt eine zunächst leere Ergebnisliste.
        if (!Files.exists(dataFile)) { // Prüft, ob bereits eine Datendatei existiert.
            return entries; // Liefert bei der ersten Nutzung eine leere Historie zurück.
        } // Beendet die Existenzprüfung.
        try { // Startet den kontrollierten Dateizugriff.
            for (String line : Files.readAllLines(dataFile, StandardCharsets.UTF_8)) { // Liest und durchläuft alle CSV-Zeilen als UTF-8.
                if (line.isBlank() || line.startsWith("date,")) { // Überspringt Leerzeilen und die Kopfzeile.
                    continue; // Wechselt ohne Verarbeitung zur nächsten Zeile.
                } // Beendet die Prüfung auf nicht relevante Zeilen.
                String[] parts = line.split(","); // Zerlegt eine Datenzeile in ihre drei Spalten.
                entries.add(new DailyEntry(LocalDate.parse(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]))); // Erstellt aus den Spalten einen Tagesdatensatz.
            } // Beendet das Durchlaufen der CSV-Zeilen.
        } catch (IOException | RuntimeException exception) { // Fängt Datei- und Formatfehler gemeinsam ab.
            throw new IllegalStateException("Die lokale Historie konnte nicht gelesen werden.", exception); // Meldet einen verständlichen Anwendungsfehler mit Ursache.
        } // Beendet die Fehlerbehandlung beim Laden.
        entries.sort(Comparator.comparing(DailyEntry::date)); // Sortiert die geladenen Einträge chronologisch.
        return entries; // Liefert die vollständig geladene Historie zurück.
    } // Beendet die Lademethode.

    public void saveOrReplace(DailyEntry newEntry) { // Speichert einen Eintrag oder ersetzt denselben Kalendertag.
        List<DailyEntry> entries = loadAll(); // Lädt den aktuellen Stand der Historie.
        entries.removeIf(entry -> entry.date().equals(newEntry.date())); // Entfernt einen eventuell vorhandenen Eintrag für dieses Datum.
        entries.add(newEntry); // Fügt den neuen oder aktualisierten Eintrag hinzu.
        entries.sort(Comparator.comparing(DailyEntry::date)); // Hält die Datei chronologisch sortiert.
        List<String> lines = new ArrayList<>(); // Erstellt die auszugebenden CSV-Zeilen.
        lines.add("date,score,earnedXp"); // Fügt eine verständliche Kopfzeile ein.
        for (DailyEntry entry : entries) { // Durchläuft alle zu speichernden Tagesdatensätze.
            lines.add(entry.date() + "," + entry.score() + "," + entry.earnedXp()); // Wandelt jeden Eintrag in eine CSV-Zeile um.
        } // Beendet die Umwandlung der Einträge.
        try { // Startet den kontrollierten Schreibzugriff.
            Files.createDirectories(dataFile.getParent()); // Erstellt den Datenordner, falls er noch fehlt.
            Files.write(dataFile, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING); // Schreibt den vollständigen aktuellen Stand atomar aus Sicht der Anwendung neu.
        } catch (IOException exception) { // Fängt Fehler beim Erstellen oder Schreiben der Datei ab.
            throw new IllegalStateException("Der Eintrag konnte nicht lokal gespeichert werden.", exception); // Wandelt den technischen Fehler in eine verständliche Anwendungsmeldung um.
        } // Beendet die Fehlerbehandlung beim Speichern.
    } // Beendet die Speichermethode.
} // Beendet die Klasse EntryRepository.

