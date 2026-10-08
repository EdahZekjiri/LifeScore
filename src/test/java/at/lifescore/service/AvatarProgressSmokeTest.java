package at.lifescore.service; // Prüft Belohnungsregeln unabhängig von der Oberfläche.

import at.lifescore.model.AvatarProfile; // Prüft Freischaltungen und Ausrüstung über das echte Datenmodell.
import at.lifescore.repository.AvatarRepository; // Prüft den Erhalt von Profil und Belohnungen nach Neustart.
import java.nio.file.Files; // Erstellt ausschließlich temporäre Testdateien.
import java.nio.file.Path; // Beschreibt die isolierte Testablage.
import static at.lifescore.model.AvatarOption.*; // Macht die geprüften Belohnungen leicht lesbar.

public final class AvatarProgressSmokeTest { // Läuft ohne Grafikzugriff und externe Testbibliothek.
    public static void main(String[] args) throws Exception { // Startet die fachlichen und Persistenzprüfungen.
        ScoreService scores = new ScoreService(); // Verwendet die echte XP-zu-Level-Formel.
        AvatarProfile profile = AvatarProfile.initial(); // Beginnt mit dem sofort sichtbaren Fuchs.
        require(profile.character() == FOX && profile.equip(CAT).character() == CAT, "Kostenlose Figuren müssen sofort wählbar sein."); // Prüft Anpassung ohne Check-in.
        for (int xp : new int[]{0, 99, 100, 199, 200, 299, 300, 399, 400, 599, 600}) { // Prüft jede Freischaltung direkt vor und an ihrer XP-Schwelle.
            AvatarProfile state = profile.advanceTo(scores.levelFor(xp)); // Nutzt den tatsächlichen Levelwert.
            require(state.isUnlocked(SCARF) == (xp >= 100), "Schal muss exakt ab 100 XP frei sein."); // Prüft Level 2.
            require(state.isUnlocked(LEAF) == (xp >= 100), "Salbeirahmen muss exakt ab 100 XP frei sein."); // Prüft mehrere gleichzeitige Belohnungen.
            require(state.isUnlocked(FOREST) == (xp >= 200), "Waldgrün muss exakt ab 200 XP frei sein."); // Prüft Level 3.
            require(state.isUnlocked(NIGHT) == (xp >= 300), "Sternennacht muss exakt ab 300 XP frei sein."); // Prüft Level 4.
            require(state.isUnlocked(GOLD) == (xp >= 400), "Goldrahmen muss exakt ab 400 XP frei sein."); // Prüft Level 5.
            require(state.isUnlocked(CROWN) == (xp >= 600), "Krone muss exakt ab 600 XP frei sein."); // Prüft Level 7.
        } // Beendet die Grenzwertprüfungen.
        try { profile.equip(GOLD); throw new AssertionError("Gesperrte Extras dürfen nicht angelegt werden."); } catch (IllegalArgumentException expected) { /* Die Sperre greift auch ohne UI. */ } // Prüft die fachliche Sperre.
        profile = profile.advanceTo(7).equip(BEAR).equip(NIGHT).equip(GOLD).equip(CROWN); // Kombiniert alle vier Kategorien in einem freigeschalteten Look.
        require(profile.unlockedRewards().size() == 6, "Alle sechs Extras müssen im Inventar erhalten bleiben."); // Verhindert das Verdrängen früherer Belohnungen.
        require(profile.advanceTo(2).equals(profile), "Tageskorrekturen dürfen verdiente Extras nicht wieder sperren."); // Prüft dauerhafte Freischaltungen trotz sinkender aktueller XP.
        Path directory = Files.createTempDirectory("life-score-avatar-test-"); // Vermeidet jeden Zugriff auf persönliche Profile.
        Path file = directory.resolve("avatar.properties"); // Verwendet eine isolierte Profildatei.
        AvatarRepository repository = new AvatarRepository(file); // Verbindet den Test mit dem echten Speicherformat.
        require(repository.load().equals(AvatarProfile.initial()), "Fehlende Datei muss einen nutzbaren Standardavatar liefern."); // Prüft den ersten Start.
        repository.save(profile); // Speichert den vollständig angelegten Look.
        require(new AvatarRepository(file).load().equals(profile), "Neustart muss Auswahl und Freischaltungen vollständig erhalten."); // Prüft alle Profilfelder gemeinsam.
        Files.writeString(file, "version=1\ncharacter=UNKNOWN\n"); // Simuliert eine beschädigte Profildatei.
        String broken = Files.readString(file); // Merkt sich den Originalinhalt.
        try { repository.save(AvatarProfile.initial()); throw new AssertionError("Defektes Profil muss das Überschreiben verhindern."); } catch (IllegalStateException expected) { /* Das Repository schützt den Altbestand. */ } // Prüft den Fehlerschutz vor dem Schreiben.
        require(Files.readString(file).equals(broken), "Defektes Originalprofil darf nicht verändert werden."); // Prüft, dass tatsächlich keine Daten überschrieben wurden.
        Files.delete(file); Files.delete(directory); // Entfernt nur selbst erzeugte Testdateien.
        System.out.println("AvatarProgress-Smoke-Test erfolgreich: Schwellen, Sperren, Kombinationen, dauerhafte Extras, Neustart, Fehlerschutz."); // Nennt den geprüften Funktionsumfang.
    } // Beendet den Testlauf.
    private static void require(boolean condition, String message) { if (!condition) { throw new AssertionError(message); } } // Prüft unabhängig vom JVM-Schalter -ea.
} // Beendet den Avatar-Regeltest.
