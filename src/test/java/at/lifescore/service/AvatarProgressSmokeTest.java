package at.lifescore.service; 

import java.nio.file.Files;
import java.nio.file.Path;

import static at.lifescore.model.AvatarOption.BEAR;
import static at.lifescore.model.AvatarOption.CAT; 
import static at.lifescore.model.AvatarOption.CROWN;
import static at.lifescore.model.AvatarOption.FOREST;
import static at.lifescore.model.AvatarOption.FOX;
import static at.lifescore.model.AvatarOption.GOLD;
import static at.lifescore.model.AvatarOption.LEAF;
import static at.lifescore.model.AvatarOption.NIGHT;
import static at.lifescore.model.AvatarOption.SCARF;
import at.lifescore.model.AvatarProfile;
import at.lifescore.repository.AvatarRepository;

public final class AvatarProgressSmokeTest { 
    public static void main(String[] args) throws Exception { 
        ScoreService scores = new ScoreService(); 
        AvatarProfile profile = AvatarProfile.initial(); 
        require(profile.character() == FOX && profile.equip(CAT).character() == CAT, "Kostenlose Figuren müssen sofort wählbar sein."); 
        for (int xp : new int[]{0, 99, 100, 199, 200, 299, 300, 399, 400, 599, 600}) { 
            AvatarProfile state = profile.advanceTo(scores.levelFor(xp)); 
            require(state.isUnlocked(SCARF) == (xp >= 100), "Schal muss exakt ab 100 XP frei sein."); 
            require(state.isUnlocked(LEAF) == (xp >= 100), "Salbeirahmen muss exakt ab 100 XP frei sein."); 
            require(state.isUnlocked(FOREST) == (xp >= 200), "Waldgrün muss exakt ab 200 XP frei sein."); 
            require(state.isUnlocked(NIGHT) == (xp >= 300), "Sternennacht muss exakt ab 300 XP frei sein."); 
            require(state.isUnlocked(GOLD) == (xp >= 400), "Goldrahmen muss exakt ab 400 XP frei sein."); 
            require(state.isUnlocked(CROWN) == (xp >= 600), "Krone muss exakt ab 600 XP frei sein."); 
        } 
        try { profile.equip(GOLD); throw new AssertionError("Gesperrte Extras dürfen nicht angelegt werden."); } catch (IllegalArgumentException expected) { /* Die Sperre greift auch ohne UI. */ } 
        profile = profile.advanceTo(7).equip(BEAR).equip(NIGHT).equip(GOLD).equip(CROWN); 
        require(profile.unlockedRewards().size() == 6, "Alle sechs Extras müssen im Inventar erhalten bleiben."); 
        require(profile.advanceTo(2).equals(profile), "Tageskorrekturen dürfen verdiente Extras nicht wieder sperren."); 
        Path directory = Files.createTempDirectory("life-score-avatar-test-"); // Vermeidet jeden Zugriff auf persönliche Profile.
        Path file = directory.resolve("avatar.properties"); 
        AvatarRepository repository = new AvatarRepository(file); 
        require(repository.load().equals(AvatarProfile.initial()), "Fehlende Datei muss einen nutzbaren Standardavatar liefern."); 
        repository.save(profile); 
        require(new AvatarRepository(file).load().equals(profile), "Neustart muss Auswahl und Freischaltungen vollständig erhalten."); // Prüft alle Profilfelder gemeinsam.
        Files.writeString(file, "version=1\ncharacter=UNKNOWN\n"); 
        String broken = Files.readString(file); 
        try { repository.save(AvatarProfile.initial()); throw new AssertionError("Defektes Profil muss das Überschreiben verhindern."); } catch (IllegalStateException expected) { /* Das Repository schützt den Altbestand. */ } // Prüft den Fehlerschutz vor dem Schreiben.
        require(Files.readString(file).equals(broken), "Defektes Originalprofil darf nicht verändert werden."); 
        Files.delete(file); Files.delete(directory); 
        System.out.println("AvatarProgress-Smoke-Test erfolgreich: Schwellen, Sperren, Kombinationen, dauerhafte Extras, Neustart, Fehlerschutz."); // Nennt den geprüften Funktionsumfang.
    } 
    private static void require(boolean condition, String message) { if (!condition) { throw new AssertionError(message); } } // Prüft unabhängig vom JVM-Schalter -ea.
} 
