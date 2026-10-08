package at.lifescore.repository; // Testet Speicherung getrennt von echten Nutzerdaten.

import at.lifescore.model.DailyAnswers; // Erstellt bekannte Testantworten.
import at.lifescore.model.DailyEntry; // Erstellt Testeinträge.
import at.lifescore.service.ScoreService; // Prüft die Wirkung einer Aktualisierung auf XP.
import java.nio.file.Files; // Erstellt isolierte Testdateien.
import java.nio.file.Path; // Beschreibt den temporären Speicherort.
import java.time.LocalDate; // Erstellt reproduzierbare Kalendertage.

public final class EntryRepositorySmokeTest { // Läuft ohne externe Testbibliothek.
    public static void main(String[] args) throws Exception { // Startet alle Repository-Prüfungen.
        Path directory = Files.createTempDirectory("life-score-test-"); // Verwendet niemals die echte CSV-Datei.
        Path file = directory.resolve("history.csv"); // Hält die Testdatei isoliert.
        EntryRepository repository = new EntryRepository(file); // Übergibt den Testpfad explizit.
        require(repository.loadAll().isEmpty(), "Neue Historie muss leer sein."); // Prüft den ersten App-Start.
        LocalDate day = LocalDate.of(2026, 10, 7); // Vermeidet Abhängigkeit vom aktuellen Testdatum.
        Files.writeString(file, "date,score,earnedXp\n2026-10-06,86,18\n"); // Simuliert eine vorhandene alte CSV-Datei.
        require(repository.loadAll().get(0).answers() == null, "Alte Antworten dürfen nicht erfunden werden."); // Prüft Abwärtskompatibilität.
        DailyAnswers answers = new DailyAnswers(8, 6, 7, 9, 5); // Verwendet ein bekanntes Score-Beispiel.
        repository.saveOrReplace(new DailyEntry(day, 72, 17, answers)); // Schreibt einen neuen vollständigen Eintrag.
        EntryRepository restarted = new EntryRepository(file); // Simuliert eine neu gestartete Anwendung.
        require(restarted.loadAll().size() == 2, "Alter Eintrag muss erhalten bleiben."); // Prüft datenerhaltende Migration.
        require(restarted.loadAll().get(1).answers().equals(answers), "Antworten müssen nach Neustart identisch sein."); // Prüft alle fünf persistierten Werte.
        require(restarted.loadAll().get(0).score() == 86 && restarted.loadAll().get(0).answers() == null, "Altbestand muss unverändert bleiben."); // Prüft fehlende Antwortfelder im erweiterten Format.
        restarted.saveOrReplace(new DailyEntry(day, 100, 20, new DailyAnswers(10, 10, 10, 10, 10))); // Aktualisiert denselben Kalendertag.
        require(restarted.loadAll().size() == 2, "Bearbeiten darf keinen zweiten Tag erzeugen."); // Verhindert doppelte Historieneinträge.
        require(new ScoreService().totalXp(restarted.loadAll()) == 38, "XP müssen ersetzt, nicht zusätzlich gesammelt werden."); // Prüft korrekten Fortschritt nach Bearbeitung.
        Files.writeString(file, "date,score,earnedXp\nkaputt\n"); // Simuliert eine beschädigte Historie.
        String before = Files.readString(file); // Merkt sich die beschädigten Daten vor dem Schreibversuch.
        try { // Erwartet, dass fehlerhafte Dateien vor jedem Schreiben abgewiesen werden.
            restarted.saveOrReplace(new DailyEntry(day, 50, 15)); // Versucht, die unlesbare Historie zu verändern.
            throw new AssertionError("Beschädigte Historie muss Speichern verhindern."); // Scheitert, falls der Schutz nicht greift.
        } catch (IllegalStateException expected) { // Erwartet den verständlichen Repository-Fehler.
            require(Files.readString(file).equals(before), "Beschädigte Originaldatei darf nicht überschrieben werden."); // Prüft explizit den Erhalt der Originaldatei.
        } // Beendet den Fehlertest.
        Files.delete(file); // Entfernt ausschließlich die selbst angelegte Testdatei.
        Files.delete(directory); // Entfernt den leeren Testordner.
        System.out.println("EntryRepository-Smoke-Test erfolgreich: Migration, Neustart, Antworten, Ersetzen, Fehlerschutz."); // Fasst die geprüften Fälle zusammen.
    } // Beendet den Testlauf.
    private static void require(boolean condition, String message) { // Liefert Assertions auch ohne JVM-Schalter -ea.
        if (!condition) { throw new AssertionError(message); } // Bricht beim ersten unerwarteten Ergebnis ab.
    } // Beendet die Prüfhilfe.
} // Beendet den Repository-Test.
