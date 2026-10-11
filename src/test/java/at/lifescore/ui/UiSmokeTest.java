package at.lifescore.ui; // Prüft das Zusammenspiel von Oberfläche, Berechnung und Speicherung.

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSlider; 
import javax.swing.SwingUtilities;

import at.lifescore.model.DailyEntry;
import at.lifescore.repository.EntryRepository;
import at.lifescore.service.ScoreService;

public final class UiSmokeTest { 
    public static void main(String[] args) throws Exception { 
        Path directory = Files.createTempDirectory("life-score-ui-test-"); 
        Path file = directory.resolve("history.csv"); 
        SwingUtilities.invokeAndWait(() -> { 
            LifeScoreFrame frame = null; 
            LifeScoreFrame restarted = null; 
            try { 
                UiTheme.install(); 
                EntryRepository repository = new EntryRepository(file); 
                frame = new LifeScoreFrame(new ScoreService(), repository); 
                require(field(frame, "countLabel", JLabel.class).getText().equals("0"), "Leerer Start muss null Check-ins zeigen."); // Prüft den Dashboard-Leerzustand.
                JSlider[] ratings = field(frame, "ratings", JSlider[].class); 
                int[] values = {8, 6, 7, 9, 5}; 
                for (int i = 0; i < values.length; i++) { ratings[i].setValue(values[i]); } 
                require(field(frame, "previewLabel", JLabel.class).getText().contains("72 / 100"), "Vorschau muss auf Eingaben reagieren."); // Prüft die Verbindung zwischen Skalen und Service.
                field(frame, "saveButton", JButton.class).doClick(); 
                DailyEntry saved = repository.loadAll().get(0); 
                require(saved.score() == 72 && saved.answers().social() == 5, "Speichern muss Score und Antworten gemeinsam übernehmen."); // Prüft den vollständigen Datenfluss.
                require(field(frame, "saveStatus", JLabel.class).getText().startsWith("Gespeichert"), "Erfolg muss sichtbar bestätigt werden."); // Prüft die Rückmeldung.
                ratings[0].setValue(10); 
                field(frame, "saveButton", JButton.class).doClick(); 
                require(repository.loadAll().size() == 1 && repository.loadAll().get(0).score() == 77, "Bearbeiten muss denselben Tag ersetzen."); // Prüft Aktualisierung ohne doppelte Tage.
                restarted = new LifeScoreFrame(new ScoreService(), new EntryRepository(file)); 
                require(field(restarted, "ratings", JSlider[].class)[0].getValue() == 10, "Neustart muss gespeicherte Antworten laden."); // Prüft den Formularzustand nach Neustart.
                Method open = LifeScoreFrame.class.getDeclaredMethod("openDate", LocalDate.class); 
                open.setAccessible(true); 
                open.invoke(restarted, LocalDate.now().plusDays(1)); 
                require(!field(restarted, "saveButton", JButton.class).isEnabled(), "Zukünftige Einträge müssen gesperrt sein."); // Prüft die sichtbare Datumsschranke.
                open.invoke(restarted, LocalDate.now().minusDays(1)); 
                require(field(restarted, "saveButton", JButton.class).isEnabled(), "Nachträge müssen möglich sein."); // Prüft den rückwirkenden Check-in.
                require(field(restarted, "ratings", JSlider[].class)[0].getValue() == 5, "Neuer Tag darf keine fremden Antworten übernehmen."); // Prüft das Zurücksetzen zwischen Tagen.
            } catch (Exception exception) { 
                throw new RuntimeException(exception); 
            } finally { 
                if (frame != null) { frame.dispose(); } 
                if (restarted != null) { restarted.dispose(); } 
            } 
        }); 
        Files.delete(file); 
        Files.delete(directory); 
        System.out.println("UI-Smoke-Test erfolgreich: Vorschau, Speichern, Bearbeiten, Neustart, Datumsregeln."); // Meldet den geprüften Umfang.
        System.exit(0); 
    } 
    private static <T> T field(Object object, String name, Class<T> type) throws Exception { 
        Field field = object.getClass().getDeclaredField(name); 
        field.setAccessible(true); 
        return type.cast(field.get(object)); 
    } 
    private static void require(boolean condition, String message) { 
        if (!condition) { throw new AssertionError(message); } 
    } 
} 
