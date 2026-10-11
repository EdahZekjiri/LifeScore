package at.lifescore.repository; 

import java.nio.file.Files;
import java.nio.file.Path; 
import java.time.LocalDate;

import at.lifescore.model.DailyAnswers;
import at.lifescore.model.DailyEntry;
import at.lifescore.service.ScoreService; 

public final class EntryRepositorySmokeTest { 
    public static void main(String[] args) throws Exception { 
        Path directory = Files.createTempDirectory("life-score-test-"); 
        Path file = directory.resolve("history.csv"); 
        EntryRepository repository = new EntryRepository(file); 
        require(repository.loadAll().isEmpty(), "Neue Historie muss leer sein."); 
        LocalDate day = LocalDate.of(2026, 10, 7); 
        Files.writeString(file, "date,score,earnedXp\n2026-10-06,86,18\n"); 
        require(repository.loadAll().get(0).answers() == null, "Alte Antworten dürfen nicht erfunden werden."); 
        DailyAnswers answers = new DailyAnswers(8, 6, 7, 9, 5); 
        repository.saveOrReplace(new DailyEntry(day, 72, 17, answers)); 
        EntryRepository restarted = new EntryRepository(file); 
        require(restarted.loadAll().size() == 2, "Alter Eintrag muss erhalten bleiben."); 
        require(restarted.loadAll().get(1).answers().equals(answers), "Antworten müssen nach Neustart identisch sein."); 
        require(restarted.loadAll().get(0).score() == 86 && restarted.loadAll().get(0).answers() == null, "Altbestand muss unverändert bleiben."); 
        restarted.saveOrReplace(new DailyEntry(day, 100, 20, new DailyAnswers(10, 10, 10, 10, 10))); 
        require(restarted.loadAll().size() == 2, "Bearbeiten darf keinen zweiten Tag erzeugen."); 
        require(new ScoreService().totalXp(restarted.loadAll()) == 38, "XP müssen ersetzt, nicht zusätzlich gesammelt werden."); 
        Files.writeString(file, "date,score,earnedXp\nkaputt\n"); 
        String before = Files.readString(file); 
        try { 
            restarted.saveOrReplace(new DailyEntry(day, 50, 15)); 
            throw new AssertionError("Beschädigte Historie muss Speichern verhindern."); 
        } catch (IllegalStateException expected) { 
            require(Files.readString(file).equals(before), "Beschädigte Originaldatei darf nicht überschrieben werden."); 
        } 
        Files.delete(file); 
        Files.delete(directory); 
        System.out.println("EntryRepository-Smoke-Test erfolgreich: Migration, Neustart, Antworten, Ersetzen, Fehlerschutz."); 
    } 
    private static void require(boolean condition, String message) { 
        if (!condition) { throw new AssertionError(message); } 
    } 
} 
