package at.lifescore.ui; 

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextArea; 
import javax.swing.SwingUtilities;

import static at.lifescore.model.AvatarOption.BEAR;
import static at.lifescore.model.AvatarOption.CAT;
import static at.lifescore.model.AvatarOption.GOLD;
import static at.lifescore.model.AvatarOption.LEAF;
import static at.lifescore.model.AvatarOption.SCARF;
import at.lifescore.model.AvatarProfile;
import at.lifescore.model.DailyEntry;
import at.lifescore.repository.AvatarRepository;
import at.lifescore.repository.EntryRepository;
import at.lifescore.service.ScoreService;

public final class AvatarUiSmokeTest { 
    public static void main(String[] args) throws Exception { 
        Path directory = Files.createTempDirectory("life-score-avatar-ui-"); 
        Path history = directory.resolve("history.csv"); 
        EntryRepository entries = new EntryRepository(history); 
        for (int i = 1; i <= 5; i++) { entries.saveOrReplace(new DailyEntry(LocalDate.now().minusDays(i), i == 5 ? 80 : 100, i == 5 ? 18 : 20)); } // Erzeugt realistische 98 XP direkt vor dem ersten Levelaufstieg.
        SwingUtilities.invokeAndWait(() -> { 
            LifeScoreFrame frame = null; LifeScoreFrame restarted = null; 
            try { 
                UiTheme.install(); 
                frame = new LifeScoreFrame(new ScoreService(), entries); 
                AvatarStudio studio = field(frame, "avatarStudio", AvatarStudio.class); 
                Map<?, ?> choices = field(studio, "choices", Map.class); 
                require(!((JButton) choices.get(SCARF)).isEnabled(), "Schal muss vor Level 2 sichtbar gesperrt sein."); // Prüft die anfängliche UI-Sperre.
                ((JButton) choices.get(CAT)).doClick(); 
                AvatarRepository profiles = new AvatarRepository(directory.resolve("avatar.properties")); 
                require(profiles.load().character() == CAT, "Figurenwahl muss sofort gespeichert werden."); 
                require(field(frame, "sidebarAvatar", AvatarView.class).getAccessibleContext().getAccessibleName().contains("Katze"), "Seitenleiste muss den angelegten Avatar übernehmen."); // Prüft die sichtbare Synchronisation zwischen Ansichten.
                field(frame, "saveButton", JButton.class).doClick(); 
                require(((JButton) choices.get(SCARF)).isEnabled() && ((JButton) choices.get(LEAF)).isEnabled(), "Echter Check-in muss beide Level-2-Extras freischalten."); // Prüft den vollständigen Datenfluss zum Inventar.
                require(!((JButton) choices.get(GOLD)).isEnabled(), "Höhere Extras müssen weiterhin gesperrt bleiben."); 
                require(field(frame, "saveStatus", JLabel.class).getText().contains("Neue Extras"), "Check-in muss neue Freischaltungen sichtbar melden."); // Prüft die unmittelbare Rückmeldung.
                ((JButton) choices.get(SCARF)).doClick(); ((JButton) choices.get(LEAF)).doClick(); 
                require(profiles.load().accessory() == SCARF && profiles.load().frame() == LEAF, "Rahmen und Accessoire müssen gemeinsam angelegt bleiben."); // Prüft echte kombinierbare Belohnungen.
                restarted = new LifeScoreFrame(new ScoreService(), new EntryRepository(history)); 
                AvatarStudio restoredStudio = field(restarted, "avatarStudio", AvatarStudio.class); 
                require(field(restoredStudio, "profile", AvatarProfile.class).equals(profiles.load()), "Neustart muss den vollständigen Look laden."); // Prüft Persistenz statt nur alten UI-Zustand.
                require(field(restarted, "sidebarAvatar", AvatarView.class).getAccessibleContext().getAccessibleName().contains("Lieblingsschal"), "Gespeichertes Extra muss nach Neustart sichtbar sein."); // Prüft die wiederhergestellte Darstellung.
                Files.writeString(directory.resolve("avatar.properties"), "version=kaputt\n"); 
                ((JButton) choices.get(BEAR)).doClick(); 
                require(field(studio, "profile", AvatarProfile.class).character() == CAT, "Speicherfehler darf keine ungespeicherte Figur als angelegt anzeigen."); // Prüft den Schutz vor falscher Erfolgsmeldung.
                require(field(studio, "status", JTextArea.class).getText().contains("nicht gelesen"), "Profilfehler muss verständlich angezeigt werden."); // Prüft die konkrete Fehlerrückmeldung.
            } catch (Exception exception) { throw new RuntimeException(exception); } finally { 
                if (frame != null) { frame.dispose(); } if (restarted != null) { restarted.dispose(); } 
            } 
        }); 
        Files.delete(history); Files.delete(directory.resolve("avatar.properties")); Files.delete(directory); 
        System.out.println("Avatar-UI-Smoke-Test erfolgreich: sichtbare Figur, Auswahl, Levelaufstieg, echte Extras, Neustart, Schreibfehler."); // Bestätigt den getesteten Umfang.
        System.exit(0); 
    } 
    private static <T> T field(Object object, String name, Class<T> type) throws Exception { 
        Field field = object.getClass().getDeclaredField(name); field.setAccessible(true); return type.cast(field.get(object)); // Liest das benannte Feld mit geprüftem Typ.
    } 
    private static void require(boolean condition, String message) { if (!condition) { throw new AssertionError(message); } } // Funktioniert ohne zusätzliche JVM-Schalter.
} 
