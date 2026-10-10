package at.lifescore.model; 

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
 
import static at.lifescore.model.AvatarOption.CREAM;
import static at.lifescore.model.AvatarOption.FOX;
import static at.lifescore.model.AvatarOption.NO_ACCESSORY;
import static at.lifescore.model.AvatarOption.PLAIN;
import at.lifescore.model.AvatarOption.Slot;
import static at.lifescore.model.AvatarOption.values;

public record AvatarProfile(AvatarOption character, AvatarOption background, AvatarOption frame, AvatarOption accessory, int highestLevel) { 
    public AvatarProfile { 
        if (highestLevel < 1) { throw new IllegalArgumentException("Das höchste erreichte Level muss mindestens 1 sein."); } 
        validate(character, Slot.CHARACTER, highestLevel); 
        validate(background, Slot.BACKGROUND, highestLevel); 
        validate(frame, Slot.FRAME, highestLevel); 
        validate(accessory, Slot.ACCESSORY, highestLevel); 
    } 
    public static AvatarProfile initial() { return new AvatarProfile(FOX, CREAM, PLAIN, NO_ACCESSORY, 1); } 
    public boolean isUnlocked(AvatarOption option) { return option.requiredLevel() <= highestLevel; } 
    public AvatarProfile advanceTo(int level) { 
        return new AvatarProfile(character, background, frame, accessory, Math.max(highestLevel, level)); 
    } 
    public AvatarOption selected(Slot slot) { 
        return switch (slot) { case CHARACTER -> character; case BACKGROUND -> background; case FRAME -> frame; case ACCESSORY -> accessory; }; 
    } 
    public AvatarProfile equip(AvatarOption option) { 
        if (!isUnlocked(option)) { throw new IllegalArgumentException(option.title() + " wird ab Level " + option.requiredLevel() + " freigeschaltet."); } 
        return new AvatarProfile(option.slot() == Slot.CHARACTER ? option : character, option.slot() == Slot.BACKGROUND ? option : background, option.slot() == Slot.FRAME ? option : frame, option.slot() == Slot.ACCESSORY ? option : accessory, highestLevel); 
    } 
    public List<AvatarOption> unlockedRewards() { 
        return Arrays.stream(values()).filter(option -> option.requiredLevel() > 1 && isUnlocked(option)).sorted(Comparator.comparingInt(AvatarOption::requiredLevel)).toList(); 
    } 
    private static void validate(AvatarOption option, Slot slot, int highestLevel) { 
        if (option == null || option.slot() != slot || option.requiredLevel() > highestLevel) { 
            throw new IllegalArgumentException("Ungültige oder noch gesperrte Avatar-Auswahl: " + slot); 
        } 
    } 
} 
