package at.lifescore.ui; // Prüft den vollständigen Weg vom Check-in bis zum angelegten Avatar-Extra.

import at.lifescore.model.*; // Nutzt echte Tagesdaten und Avatar-Optionen.
import at.lifescore.repository.*; // Verwendet die echten Repositories mit isolierten Dateipfaden.
import at.lifescore.service.ScoreService; // Berechnet denselben Fortschritt wie die Anwendung.
import java.lang.reflect.Field; // Liest die echten Swing-Komponenten ausschließlich im Test.
import java.nio.file.*; // Hält Testdaten von der persönlichen Historie getrennt.
import java.time.LocalDate; // Erstellt vergangene Testeinträge.
import java.util.Map; // Beschreibt die Zuordnung zwischen Avatar-Optionen und Schaltflächen.
import javax.swing.*; // Führt die Prüfung im Swing-Thread aus.
import static at.lifescore.model.AvatarOption.*; // Benennt die geprüften Looks direkt.

public final class AvatarUiSmokeTest { // Benötigt Grafikzugriff, aber keine sichtbare Maussteuerung.
    public static void main(String[] args) throws Exception { // Startet einen isolierten Integrationstest.
        Path directory = Files.createTempDirectory("life-score-avatar-ui-"); // Legt einen eigenen Testdatenordner an.
        Path history = directory.resolve("history.csv"); // Trennt die Historie von der echten Anwendung.
        EntryRepository entries = new EntryRepository(history); // Nutzt dasselbe Repository wie die App.
        for (int i = 1; i <= 5; i++) { entries.saveOrReplace(new DailyEntry(LocalDate.now().minusDays(i), i == 5 ? 80 : 100, i == 5 ? 18 : 20)); } // Erzeugt realistische 98 XP direkt vor dem ersten Levelaufstieg.
        SwingUtilities.invokeAndWait(() -> { // Hält alle Swing-Zugriffe im UI-Thread.
            LifeScoreFrame frame = null; LifeScoreFrame restarted = null; // Ermöglicht sicheres Aufräumen aller Testfenster.
            try { // Prüft Vorschau, Sperren, echte Freischaltung und Neustart gemeinsam.
                UiTheme.install(); // Nutzt die normale App-Gestaltung.
                frame = new LifeScoreFrame(new ScoreService(), entries); // Lädt den Fortschritt knapp unter Level 2.
                AvatarStudio studio = field(frame, "avatarStudio", AvatarStudio.class); // Greift auf die tatsächlich eingebundene Avatar-Seite zu.
                Map<?, ?> choices = field(studio, "choices", Map.class); // Liest die echten Auswahlknöpfe.
                require(!((JButton) choices.get(SCARF)).isEnabled(), "Schal muss vor Level 2 sichtbar gesperrt sein."); // Prüft die anfängliche UI-Sperre.
                ((JButton) choices.get(CAT)).doClick(); // Legt eine kostenlose Figur an.
                AvatarRepository profiles = new AvatarRepository(directory.resolve("avatar.properties")); // Liest den tatsächlich gespeicherten Look.
                require(profiles.load().character() == CAT, "Figurenwahl muss sofort gespeichert werden."); // Prüft automatische Speicherung.
                require(field(frame, "sidebarAvatar", AvatarView.class).getAccessibleContext().getAccessibleName().contains("Katze"), "Seitenleiste muss den angelegten Avatar übernehmen."); // Prüft die sichtbare Synchronisation zwischen Ansichten.
                field(frame, "saveButton", JButton.class).doClick(); // Verdient mit dem heutigen Standard-Check-in weitere 15 XP.
                require(((JButton) choices.get(SCARF)).isEnabled() && ((JButton) choices.get(LEAF)).isEnabled(), "Echter Check-in muss beide Level-2-Extras freischalten."); // Prüft den vollständigen Datenfluss zum Inventar.
                require(!((JButton) choices.get(GOLD)).isEnabled(), "Höhere Extras müssen weiterhin gesperrt bleiben."); // Prüft, dass der Aufstieg nicht versehentlich alle Looks öffnet.
                require(field(frame, "saveStatus", JLabel.class).getText().contains("Neue Extras"), "Check-in muss neue Freischaltungen sichtbar melden."); // Prüft die unmittelbare Rückmeldung.
                ((JButton) choices.get(SCARF)).doClick(); ((JButton) choices.get(LEAF)).doClick(); // Kombiniert zwei freigeschaltete Kategorien.
                require(profiles.load().accessory() == SCARF && profiles.load().frame() == LEAF, "Rahmen und Accessoire müssen gemeinsam angelegt bleiben."); // Prüft echte kombinierbare Belohnungen.
                restarted = new LifeScoreFrame(new ScoreService(), new EntryRepository(history)); // Öffnet eine neue App-Instanz mit denselben Daten.
                AvatarStudio restoredStudio = field(restarted, "avatarStudio", AvatarStudio.class); // Prüft das frisch geladene Profil.
                require(field(restoredStudio, "profile", AvatarProfile.class).equals(profiles.load()), "Neustart muss den vollständigen Look laden."); // Prüft Persistenz statt nur alten UI-Zustand.
                require(field(restarted, "sidebarAvatar", AvatarView.class).getAccessibleContext().getAccessibleName().contains("Lieblingsschal"), "Gespeichertes Extra muss nach Neustart sichtbar sein."); // Prüft die wiederhergestellte Darstellung.
                Files.writeString(directory.resolve("avatar.properties"), "version=kaputt\n"); // Simuliert einen später auftretenden Profilfehler.
                ((JButton) choices.get(BEAR)).doClick(); // Versucht eine Änderung nach Beschädigung der Datei.
                require(field(studio, "profile", AvatarProfile.class).character() == CAT, "Speicherfehler darf keine ungespeicherte Figur als angelegt anzeigen."); // Prüft den Schutz vor falscher Erfolgsmeldung.
                require(field(studio, "status", JTextArea.class).getText().contains("nicht gelesen"), "Profilfehler muss verständlich angezeigt werden."); // Prüft die konkrete Fehlerrückmeldung.
            } catch (Exception exception) { throw new RuntimeException(exception); } finally { // Meldet Fehler und gibt dennoch alle Fensterressourcen frei.
                if (frame != null) { frame.dispose(); } if (restarted != null) { restarted.dispose(); } // Schließt ausschließlich die eigenen unsichtbaren Testinstanzen.
            } // Beendet den UI-Testblock.
        }); // Wartet auf den vollständigen Swing-Test.
        Files.delete(history); Files.delete(directory.resolve("avatar.properties")); Files.delete(directory); // Bereinigt die selbst angelegten Testdateien.
        System.out.println("Avatar-UI-Smoke-Test erfolgreich: sichtbare Figur, Auswahl, Levelaufstieg, echte Extras, Neustart, Schreibfehler."); // Bestätigt den getesteten Umfang.
        System.exit(0); // Beendet plattformspezifische AWT-Hilfsthreads nach dem eigenständigen Test.
    } // Beendet den Teststarter.
    private static <T> T field(Object object, String name, Class<T> type) throws Exception { // Vermeidet zusätzliche öffentliche Testmethoden in Produktionsklassen.
        Field field = object.getClass().getDeclaredField(name); field.setAccessible(true); return type.cast(field.get(object)); // Liest das benannte Feld mit geprüftem Typ.
    } // Beendet die Testzugriffshilfe.
    private static void require(boolean condition, String message) { if (!condition) { throw new AssertionError(message); } } // Funktioniert ohne zusätzliche JVM-Schalter.
} // Beendet den Avatar-Integrationstest.
