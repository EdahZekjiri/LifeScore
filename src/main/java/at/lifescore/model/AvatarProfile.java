package at.lifescore.model; // Hält die persönliche Avatar-Auswahl und dauerhaft erreichte Freischaltungen.

import java.util.Arrays; // Filtert den zentralen Optionskatalog.
import java.util.Comparator; // Ordnet Belohnungen nach ihrer Freischaltschwelle.
import java.util.List; // Liefert alle verfügbaren Extras gemeinsam.
import static at.lifescore.model.AvatarOption.*; // Macht die vier Standardoptionen leicht lesbar.

public record AvatarProfile(AvatarOption character, AvatarOption background, AvatarOption frame, AvatarOption accessory, int highestLevel) { // Speichert nur die Auswahl und das höchste erreichte Level; Freischaltungen sind daraus eindeutig ableitbar.
    public AvatarProfile { // Validiert auch Profile, die aus einer Datei gelesen werden.
        if (highestLevel < 1) { throw new IllegalArgumentException("Das höchste erreichte Level muss mindestens 1 sein."); } // Verhindert ungültigen Fortschritt.
        validate(character, Slot.CHARACTER, highestLevel); // Prüft Figur und Freischaltung.
        validate(background, Slot.BACKGROUND, highestLevel); // Prüft Hintergrund und Freischaltung.
        validate(frame, Slot.FRAME, highestLevel); // Prüft Rahmen und Freischaltung.
        validate(accessory, Slot.ACCESSORY, highestLevel); // Prüft Accessoire und Freischaltung.
    } // Beendet die Profilvalidierung.
    public static AvatarProfile initial() { return new AvatarProfile(FOX, CREAM, PLAIN, NO_ACCESSORY, 1); } // Zeigt bereits beim ersten Start einen vollständigen Fuchs-Avatar.
    public boolean isUnlocked(AvatarOption option) { return option.requiredLevel() <= highestLevel; } // Prüft zentral, ob ein Teil angelegt werden darf.
    public AvatarProfile advanceTo(int level) { // Aktualisiert Freischaltungen nach einem Check-in oder beim Laden vorhandener XP.
        return new AvatarProfile(character, background, frame, accessory, Math.max(highestLevel, level)); // Bereits verdiente Extras bleiben auch nach einer ehrlichen Tageskorrektur erhalten.
    } // Beendet die Fortschrittsaktualisierung.
    public AvatarOption selected(Slot slot) { // Liefert die angelegte Option für eine bestimmte Kategorie.
        return switch (slot) { case CHARACTER -> character; case BACKGROUND -> background; case FRAME -> frame; case ACCESSORY -> accessory; }; // Nutzt eine vollständige Fallunterscheidung für alle Kategorien.
    } // Beendet die Auswahlabfrage.
    public AvatarProfile equip(AvatarOption option) { // Erzeugt eine neue Auswahl, ohne das bisher gespeicherte Profil zu verändern.
        if (!isUnlocked(option)) { throw new IllegalArgumentException(option.title() + " wird ab Level " + option.requiredLevel() + " freigeschaltet."); } // Sperrt Extras auch unabhängig von deaktivierten UI-Knöpfen.
        return new AvatarProfile(option.slot() == Slot.CHARACTER ? option : character, option.slot() == Slot.BACKGROUND ? option : background, option.slot() == Slot.FRAME ? option : frame, option.slot() == Slot.ACCESSORY ? option : accessory, highestLevel); // Ersetzt ausschließlich die passende Kategorie.
    } // Beendet das Anlegen eines Extras.
    public List<AvatarOption> unlockedRewards() { // Liefert alle verdienten Belohnungen statt nur die jeweils höchste.
        return Arrays.stream(values()).filter(option -> option.requiredLevel() > 1 && isUnlocked(option)).sorted(Comparator.comparingInt(AvatarOption::requiredLevel)).toList(); // Behält ältere Freischaltungen im Inventar.
    } // Beendet die Inventarabfrage.
    private static void validate(AvatarOption option, Slot slot, int highestLevel) { // Wendet dieselben Regeln auf jede Profilkategorie an.
        if (option == null || option.slot() != slot || option.requiredLevel() > highestLevel) { // Erkennt falsche Kategorien, fehlende Werte und gesperrte Auswahl.
            throw new IllegalArgumentException("Ungültige oder noch gesperrte Avatar-Auswahl: " + slot); // Verhindert fehlerhafte Profile aus unvollständigen Dateien.
        } // Beendet die Prüfung.
    } // Beendet die Validierungshilfe.
} // Beendet das Avatar-Profil.
