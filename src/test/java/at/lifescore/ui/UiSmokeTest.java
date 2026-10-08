package at.lifescore.ui; // Prüft das Zusammenspiel von Oberfläche, Berechnung und Speicherung.

import at.lifescore.model.DailyEntry; // Ermöglicht die Prüfung gespeicherter Ergebnisse.
import at.lifescore.repository.EntryRepository; // Nutzt einen isolierten Testpfad.
import at.lifescore.service.ScoreService; // Verwendet die echte Berechnungslogik.
import java.lang.reflect.Field; // Greift ausschließlich im Test auf die privaten Formularfelder zu.
import java.lang.reflect.Method; // Öffnet Testdaten ohne native Maus- oder Tastatureingaben.
import java.nio.file.Files; // Legt einen temporären Testordner an.
import java.nio.file.Path; // Beschreibt den Testpfad.
import java.time.LocalDate; // Erzeugt heutige und zukünftige Testdaten.
import javax.swing.*; // Führt alle UI-Prüfungen im Swing-Thread aus.

public final class UiSmokeTest { // Benötigt einen grafischen Java-Betrieb, öffnet jedoch kein sichtbares Fenster.
    public static void main(String[] args) throws Exception { // Startet den unabhängigen Integrationstest.
        Path directory = Files.createTempDirectory("life-score-ui-test-"); // Verändert keine echte Historie.
        Path file = directory.resolve("history.csv"); // Reserviert den Speicherort der Testdaten.
        SwingUtilities.invokeAndWait(() -> { // Hält sämtliche Swing-Zugriffe auf dem vorgesehenen Thread.
            LifeScoreFrame frame = null; // Ermöglicht Aufräumen auch bei einer fehlgeschlagenen Prüfung.
            LifeScoreFrame restarted = null; // Hält die zum Neustartvergleich erzeugte zweite Instanz.
            try { // Verbindet funktionale Prüfungen mit garantierter Fensterbereinigung.
                UiTheme.install(); // Verwendet dieselbe Gestaltung wie die echte Anwendung.
                EntryRepository repository = new EntryRepository(file); // Nutzt ausschließlich die temporäre Datei.
                frame = new LifeScoreFrame(new ScoreService(), repository); // Startet mit einer leeren Historie.
                require(field(frame, "countLabel", JLabel.class).getText().equals("0"), "Leerer Start muss null Check-ins zeigen."); // Prüft den Dashboard-Leerzustand.
                JSlider[] ratings = field(frame, "ratings", JSlider[].class); // Liest die interaktiven Skalen des echten Formulars.
                int[] values = {8, 6, 7, 9, 5}; // Verwendet das dokumentierte Beispiel mit Score 72.
                for (int i = 0; i < values.length; i++) { ratings[i].setValue(values[i]); } // Simuliert Formularwerte auf Komponentenebene.
                require(field(frame, "previewLabel", JLabel.class).getText().contains("72 / 100"), "Vorschau muss auf Eingaben reagieren."); // Prüft die Verbindung zwischen Skalen und Service.
                field(frame, "saveButton", JButton.class).doClick(); // Löst dieselbe Speicheraktion wie die sichtbare Schaltfläche aus.
                DailyEntry saved = repository.loadAll().get(0); // Liest den tatsächlich gespeicherten Datensatz zurück.
                require(saved.score() == 72 && saved.answers().social() == 5, "Speichern muss Score und Antworten gemeinsam übernehmen."); // Prüft den vollständigen Datenfluss.
                require(field(frame, "saveStatus", JLabel.class).getText().startsWith("Gespeichert"), "Erfolg muss sichtbar bestätigt werden."); // Prüft die Rückmeldung.
                ratings[0].setValue(10); // Ändert die Schlafbewertung am selben Tag.
                field(frame, "saveButton", JButton.class).doClick(); // Aktualisiert den vorhandenen Eintrag.
                require(repository.loadAll().size() == 1 && repository.loadAll().get(0).score() == 77, "Bearbeiten muss denselben Tag ersetzen."); // Prüft Aktualisierung ohne doppelte Tage.
                restarted = new LifeScoreFrame(new ScoreService(), new EntryRepository(file)); // Simuliert ein erneutes Öffnen der Anwendung.
                require(field(restarted, "ratings", JSlider[].class)[0].getValue() == 10, "Neustart muss gespeicherte Antworten laden."); // Prüft den Formularzustand nach Neustart.
                Method open = LifeScoreFrame.class.getDeclaredMethod("openDate", LocalDate.class); // Öffnet gezielt einen Kalenderwert für die Datumsregeln.
                open.setAccessible(true); // Beschränkt den Zugriff auf diesen Integrationstest.
                open.invoke(restarted, LocalDate.now().plusDays(1)); // Wählt morgen als unerlaubten Check-in-Tag.
                require(!field(restarted, "saveButton", JButton.class).isEnabled(), "Zukünftige Einträge müssen gesperrt sein."); // Prüft die sichtbare Datumsschranke.
                open.invoke(restarted, LocalDate.now().minusDays(1)); // Wählt einen noch nicht gespeicherten vergangenen Tag.
                require(field(restarted, "saveButton", JButton.class).isEnabled(), "Nachträge müssen möglich sein."); // Prüft den rückwirkenden Check-in.
                require(field(restarted, "ratings", JSlider[].class)[0].getValue() == 5, "Neuer Tag darf keine fremden Antworten übernehmen."); // Prüft das Zurücksetzen zwischen Tagen.
            } catch (Exception exception) { // Macht Fehler auf dem Swing-Thread für den Teststarter sichtbar.
                throw new RuntimeException(exception); // Verhindert fälschlich erfolgreiche Tests.
            } finally { // Gibt native Fensterressourcen unabhängig vom Ergebnis frei.
                if (frame != null) { frame.dispose(); } // Schließt die erste unsichtbare Testinstanz.
                if (restarted != null) { restarted.dispose(); } // Schließt die Neustartinstanz.
            } // Beendet den UI-Testblock.
        }); // Wartet auf alle Prüfungen im Swing-Thread.
        Files.delete(file); // Entfernt ausschließlich selbst erstellte Testdaten.
        Files.delete(directory); // Entfernt den leeren Testordner.
        System.out.println("UI-Smoke-Test erfolgreich: Vorschau, Speichern, Bearbeiten, Neustart, Datumsregeln."); // Meldet den geprüften Umfang.
        System.exit(0); // Beendet auch plattformspezifische AWT-Hilfsthreads nach diesem eigenständigen Test.
    } // Beendet den Teststarter.
    private static <T> T field(Object object, String name, Class<T> type) throws Exception { // Vermeidet öffentliche Testzugriffe in der eigentlichen Anwendung.
        Field field = object.getClass().getDeclaredField(name); // Sucht das ausdrücklich benannte Testziel.
        field.setAccessible(true); // Erlaubt dem Integrationstest das Lesen der Komponente.
        return type.cast(field.get(object)); // Prüft den erwarteten Komponententyp.
    } // Beendet die Testzugriffshilfe.
    private static void require(boolean condition, String message) { // Funktioniert ohne besondere JVM-Assertion-Option.
        if (!condition) { throw new AssertionError(message); } // Bricht bei einem unerwarteten Verhalten ab.
    } // Beendet die Prüfhilfe.
} // Beendet den UI-Integrationstest.
