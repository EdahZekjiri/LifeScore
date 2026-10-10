package at.lifescore.repository; 

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption; 
import java.util.Properties;

import at.lifescore.model.AvatarOption;
import at.lifescore.model.AvatarProfile;

public final class AvatarRepository { 
    private final Path file; 
    public AvatarRepository(Path file) { this.file = file.toAbsolutePath(); } 
    public AvatarProfile load() { 
        if (!Files.exists(file)) { return AvatarProfile.initial(); } 
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) { 
            Properties data = new Properties(); 
            data.load(reader); 
            if (!"1".equals(data.getProperty("version"))) { throw new IllegalArgumentException("Unbekannte Profilversion."); } 
            return new AvatarProfile(option(data, "character"), option(data, "background"), option(data, "frame"), option(data, "accessory"), Integer.parseInt(data.getProperty("highestLevel"))); 
        } catch (IOException | RuntimeException exception) { 
            throw new IllegalStateException("Das Avatar-Profil konnte nicht gelesen werden. Bitte data/avatar.properties prüfen; die Datei wurde nicht verändert.", exception); 
        } 
    } 
    public void save(AvatarProfile profile) { 
        load(); 
        Properties data = new Properties(); 
        data.setProperty("version", "1"); 
        data.setProperty("character", profile.character().name()); 
        data.setProperty("background", profile.background().name()); 
        data.setProperty("frame", profile.frame().name()); 
        data.setProperty("accessory", profile.accessory().name()); 
        data.setProperty("highestLevel", Integer.toString(profile.highestLevel())); 
        Path temporary = null; 
        try { 
            Files.createDirectories(file.getParent()); 
            temporary = Files.createTempFile(file.getParent(), "avatar-", ".tmp"); 
            try (Writer writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) { data.store(writer, "Life Score - Avatar und dauerhafte Freischaltungen"); } // Schließt den neuen Dateistand vor dem Austausch.
            try { 
                Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); 
            } catch (AtomicMoveNotSupportedException exception) { 
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING); 
            } 
        } catch (IOException exception) { 
            throw new IllegalStateException("Der Avatar konnte nicht gespeichert werden. Bitte Schreibrechte im Datenordner prüfen.", exception); // Bestätigt keine ungespeicherte Änderung.
        } finally { 
            if (temporary != null) { try { Files.deleteIfExists(temporary); } catch (IOException ignored) { /* Der ursprüngliche Fehler bleibt maßgeblich. */ } } // Verdeckt den eigentlichen Schreibfehler nicht.
        } 
    } 
    private static AvatarOption option(Properties data, String key) { return AvatarOption.valueOf(data.getProperty(key, "")); } // Lässt unbekannte oder fehlende Werte bewusst an der Modellvalidierung scheitern.
} 
