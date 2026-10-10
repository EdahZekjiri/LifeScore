package at.lifescore.model; 

public enum AvatarOption { 
    FOX(Slot.CHARACTER, "Fuchs", 1), 
    CAT(Slot.CHARACTER, "Katze", 1), 
    BEAR(Slot.CHARACTER, "Bär", 1), 
    CREAM(Slot.BACKGROUND, "Morgenlicht", 1), 
    FOREST(Slot.BACKGROUND, "Waldgrün", 3), 
    NIGHT(Slot.BACKGROUND, "Sternennacht", 4), 
    PLAIN(Slot.FRAME, "Ohne Rahmen", 1), 
    LEAF(Slot.FRAME, "Salbeirahmen", 2), 
    GOLD(Slot.FRAME, "Goldener Fokus", 5), 
    NO_ACCESSORY(Slot.ACCESSORY, "Ohne Accessoire", 1), 
    SCARF(Slot.ACCESSORY, "Lieblingsschal", 2), 
    CROWN(Slot.ACCESSORY, "Kleine Krone", 7); 

    public enum Slot { CHARACTER, BACKGROUND, FRAME, ACCESSORY } 
    private final Slot slot; 
    private final String title; 
    private final int requiredLevel; 

    AvatarOption(Slot slot, String title, int requiredLevel) { 
        this.slot = slot; 
        this.title = title; 
        this.requiredLevel = requiredLevel; 
    } 
    public Slot slot() { return slot; } 
    public String title() { return title; } 
    public int requiredLevel() { return requiredLevel; } 
    public int requiredXp() { return (requiredLevel - 1) * 100; } 
} 
