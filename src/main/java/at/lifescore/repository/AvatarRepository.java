package at.lifescore.repository; // Speichert das Avatar-Profil unabhängig von den Tagesantworten.

import at.lifescore.model.AvatarOption; // Liest stabile Optionsnamen statt übersetzter UI-Texte.
import at.lifescore.model.AvatarProfile; // Speichert Auswahl und dauerhaft erreichte Freischaltungen gemeinsam.
import java.io.IOException; // Behandelt Fehler beim Dateizugriff.
import java.io.Reader; // Liest die kleine Einstellungsdatei als Text.
import java.io.Writer; // Schreibt die Einstellungsdatei als Text.
import java.nio.charset.StandardCharsets; // Verwendet einheitlich UTF-8.
import java.nio.file.*; // Stellt lokale Pfade und sicheren Dateiaustausch bereit.
import java.util.Properties; // Nutzt ein einfaches, erklärbares Format ohne zusätzliche Bibliotheken.

public final class AvatarRepository { // Hält technische Dateizugriffe aus der Oberfläche heraus.
    private final Path file; // Liegt im selben Datenordner wie die zugehörige Historie.
    public AvatarRepository(Path file) { this.file = file.toAbsolutePath(); } // Erlaubt isolierte Pfade für Tests und Vorführungen.
    public AvatarProfile load() { // Lädt das gespeicherte Profil oder den vollständigen Standardavatar.
        if (!Files.exists(file)) { return AvatarProfile.initial(); } // Benötigt beim ersten Start keine vorbereitete Datei.
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) { // Schließt die Datei auch bei Fehlern zuverlässig.
            Properties data = new Properties(); // Hält die benannten Profilfelder.
            data.load(reader); // Liest die vorhandenen Einstellungen.
            if (!"1".equals(data.getProperty("version"))) { throw new IllegalArgumentException("Unbekannte Profilversion."); } // Verhindert stilles Umdeuten inkompatibler Daten.
            return new AvatarProfile(option(data, "character"), option(data, "background"), option(data, "frame"), option(data, "accessory"), Integer.parseInt(data.getProperty("highestLevel"))); // Validiert alle gespeicherten Werte über das Modell.
        } catch (IOException | RuntimeException exception) { // Erkennt sowohl fehlende Rechte als auch defekte Profildaten.
            throw new IllegalStateException("Das Avatar-Profil konnte nicht gelesen werden. Bitte data/avatar.properties prüfen; die Datei wurde nicht verändert.", exception); // Zeigt einen konkreten Hinweis ohne Daten zu ersetzen.
        } // Beendet die Fehlerbehandlung.
    } // Beendet das Laden.
    public void save(AvatarProfile profile) { // Speichert Auswahl und Freischaltungen zusammen.
        load(); // Verhindert das Überschreiben einer inzwischen beschädigten Bestandsdatei.
        Properties data = new Properties(); // Erstellt den neuen vollständigen Profilstand.
        data.setProperty("version", "1"); // Ermöglicht spätere kontrollierte Formatänderungen.
        data.setProperty("character", profile.character().name()); // Speichert die Figur mit stabilem Schlüssel.
        data.setProperty("background", profile.background().name()); // Speichert den Hintergrund.
        data.setProperty("frame", profile.frame().name()); // Speichert den Rahmen.
        data.setProperty("accessory", profile.accessory().name()); // Speichert das Accessoire.
        data.setProperty("highestLevel", Integer.toString(profile.highestLevel())); // Bewahrt alle bis dahin verdienten Extras auch nach Neustart.
        Path temporary = null; // Merkt sich die temporäre Datei für eine sichere Bereinigung.
        try { // Ersetzt die bestehende Datei erst nach einem vollständigen Schreibvorgang.
            Files.createDirectories(file.getParent()); // Erstellt bei Bedarf den lokalen Datenordner.
            temporary = Files.createTempFile(file.getParent(), "avatar-", ".tmp"); // Nutzt dasselbe Dateisystem für den anschließenden Austausch.
            try (Writer writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) { data.store(writer, "Life Score - Avatar und dauerhafte Freischaltungen"); } // Schließt den neuen Dateistand vor dem Austausch.
            try { // Bevorzugt einen atomaren Austausch.
                Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); // Ersetzt die Datei in einem Dateisystemschritt.
            } catch (AtomicMoveNotSupportedException exception) { // Unterstützt auch einfachere Dateisysteme.
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING); // Ersetzt dort die bereits vollständig geschriebene Datei regulär.
            } // Beendet den Dateiaustausch.
        } catch (IOException exception) { // Erkennt beispielsweise fehlende Schreibrechte.
            throw new IllegalStateException("Der Avatar konnte nicht gespeichert werden. Bitte Schreibrechte im Datenordner prüfen.", exception); // Bestätigt keine ungespeicherte Änderung.
        } finally { // Entfernt zurückgebliebene temporäre Dateien.
            if (temporary != null) { try { Files.deleteIfExists(temporary); } catch (IOException ignored) { /* Der ursprüngliche Fehler bleibt maßgeblich. */ } } // Verdeckt den eigentlichen Schreibfehler nicht.
        } // Beendet den Speichervorgang.
    } // Beendet das Speichern.
    private static AvatarOption option(Properties data, String key) { return AvatarOption.valueOf(data.getProperty(key, "")); } // Lässt unbekannte oder fehlende Werte bewusst an der Modellvalidierung scheitern.
} // Beendet das Avatar-Repository.
