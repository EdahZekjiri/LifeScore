package at.lifescore.repository; 

import java.io.IOException; 
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import at.lifescore.model.DailyAnswers;
import at.lifescore.model.DailyEntry; 

public final class EntryRepository { 
    private final Path dataFile; 

    public EntryRepository() { 
        this(Path.of("data", "life-score-history.csv")); 
    } 

    public EntryRepository(Path dataFile) { 
        this.dataFile = dataFile.toAbsolutePath(); 
    } 

    public Path dataDirectory() { return dataFile.getParent(); } 

    public List<DailyEntry> loadAll() { 
        List<DailyEntry> entries = new ArrayList<>(); 
        if (!Files.exists(dataFile)) { 
            return entries; 
        } 
        try { // Übersetzt technische Fehler in verständliche Rückmeldungen.
            for (String line : Files.readAllLines(dataFile, StandardCharsets.UTF_8)) { 
                if (line.isBlank() || line.startsWith("date,")) { 
                    continue; 
                } 
                String[] parts = line.split(",", -1); 
                if (parts.length != 3 && parts.length != 8) { 
                    throw new IllegalArgumentException("Unerwartete Spaltenzahl."); 
                } 
                DailyAnswers answers = null; // Alte Scores besitzen keine rekonstruierbaren Antworten.
                if (parts.length == 8 && !String.join("", java.util.Arrays.copyOfRange(parts, 3, 8)).isEmpty()) { 
                    answers = new DailyAnswers(Integer.parseInt(parts[3]), Integer.parseInt(parts[4]), Integer.parseInt(parts[5]), Integer.parseInt(parts[6]), Integer.parseInt(parts[7])); 
                } 
                DailyEntry entry = new DailyEntry(LocalDate.parse(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), answers); // Stellt den Tagesdatensatz wieder her.
                if (entries.stream().anyMatch(existing -> existing.date().equals(entry.date()))) { 
                    throw new IllegalArgumentException("Doppeltes Datum."); 
                } 
                entries.add(entry); 
            } 
        } catch (IOException | RuntimeException exception) { 
            throw new IllegalStateException("Die lokale Historie konnte nicht gelesen werden. Bitte die CSV-Datei prüfen; sie wurde nicht verändert.", exception); // Erklärt den nächsten Schritt.
        } 
        entries.sort(Comparator.comparing(DailyEntry::date)); 
        return entries; 
    } 

    public void saveOrReplace(DailyEntry newEntry) { 
        List<DailyEntry> entries = loadAll(); 
        entries.removeIf(entry -> entry.date().equals(newEntry.date())); 
        entries.add(newEntry); 
        entries.sort(Comparator.comparing(DailyEntry::date)); 
        List<String> lines = new ArrayList<>(); 
        lines.add("date,score,earnedXp,sleep,movement,nutrition,productivity,social"); 
        for (DailyEntry entry : entries) { 
            DailyAnswers a = entry.answers(); 
            String answers = a == null ? ",,,,," : "," + a.sleep() + "," + a.movement() + "," + a.nutrition() + "," + a.productivity() + "," + a.social(); // Schreibt fehlende Antworten als leere Felder.
            lines.add(entry.date() + "," + entry.score() + "," + entry.earnedXp() + answers); 
        } 
        Path temporary = null; 
        try { 
            Files.createDirectories(dataFile.getParent()); 
            temporary = Files.createTempFile(dataFile.getParent(), "life-score-", ".tmp"); 
            Files.write(temporary, lines, StandardCharsets.UTF_8); 
            try { 
                Files.move(temporary, dataFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); 
            } catch (AtomicMoveNotSupportedException exception) { 
                Files.move(temporary, dataFile, StandardCopyOption.REPLACE_EXISTING); 
            } 
        } catch (IOException exception) { 
            throw new IllegalStateException("Der Eintrag konnte nicht lokal gespeichert werden.", exception); 
        } finally { 
            if (temporary != null) { 
                try { Files.deleteIfExists(temporary); } catch (IOException ignored) { /* Der ursprüngliche Speicherfehler bleibt maßgeblich. */ } // Verhindert das Verdecken der eigentlichen Ursache.
            } 
        } 
    } 
} 
