package at.lifescore.service; // Ordnet die Berechnungen dem Service-Paket zu.

import at.lifescore.model.DailyEntry; // Macht Tagesdatensätze für die XP-Auswertung verfügbar.
import java.time.LocalDate; // Stellt Kalendertage für die Serienberechnung bereit.
import java.util.HashSet; // Ermöglicht eine schnelle Prüfung, ob ein Tag einen Eintrag hat.
import java.util.List; // Stellt eine geordnete Sammlung von Einträgen bereit.
import java.util.Set; // Stellt den allgemeinen Mengentyp bereit.

public final class ScoreService { // Bündelt die fachlichen Berechnungen der Anwendung.
    public static final double[] WEIGHTS = {0.25, 0.20, 0.20, 0.20, 0.15}; // Legt die Gewichtung von Schlaf, Bewegung, Ernährung, Produktivität und Sozialem zentral fest.

    public int calculateScore(int sleep, int movement, int nutrition, int productivity, int social) { // Berechnet aus fünf Bewertungen einen gewichteten Gesamtscore.
        int[] ratings = {sleep, movement, nutrition, productivity, social}; // Fasst die Bewertungen in der Reihenfolge der Gewichte zusammen.
        double weighted = 0.0; // Startet die gewichtete Summe bei null.
        for (int i = 0; i < ratings.length; i++) { // Durchläuft alle fünf Bereiche nacheinander.
            validateRating(ratings[i]); // Prüft die jeweilige Bewertung auf den erlaubten Bereich.
            weighted += ratings[i] * WEIGHTS[i]; // Addiert die mit dem Bereichsgewicht multiplizierte Bewertung.
        } // Beendet die Summenbildung.
        return (int) Math.round(weighted * 10.0); // Normalisiert das Ergebnis auf die Skala von 0 bis 100.
    } // Beendet die Score-Berechnung.

    public String describe(int score) { // Übersetzt einen Score in eine verständliche Einordnung.
        if (score >= 85) { // Prüft auf einen herausragenden Tag.
            return "Ausgezeichnet"; // Beschreibt Werte ab 85.
        } // Beendet die Prüfung auf Werte ab 85.
        if (score >= 70) { // Prüft auf einen guten Tag.
            return "Sehr gut"; // Beschreibt Werte ab 70.
        } // Beendet die Prüfung auf Werte ab 70.
        if (score >= 50) { // Prüft auf einen durchschnittlichen Tag.
            return "Solide"; // Beschreibt Werte ab 50.
        } // Beendet die Prüfung auf Werte ab 50.
        if (score >= 30) { // Prüft auf einen schwächeren Tag.
            return "Ausbaufähig"; // Beschreibt Werte ab 30.
        } // Beendet die Prüfung auf Werte ab 30.
        return "Erholung nötig"; // Beschreibt alle niedrigeren Werte.
    } // Beendet die Einordnung.

    public int earnedXpFor(int score) { // Bestimmt die XP für einen abgeschlossenen Fragebogen.
        return 10 + score / 10; // Vergibt zehn Basis-XP plus bis zu zehn leistungsabhängige XP.
    } // Beendet die XP-Berechnung.

    public int totalXp(List<DailyEntry> entries) { // Addiert die bisher gespeicherten Erfahrungspunkte.
        return entries.stream().mapToInt(DailyEntry::earnedXp).sum(); // Summiert das XP-Feld aller Einträge.
    } // Beendet die XP-Summierung.

    public int levelFor(int totalXp) { // Ermittelt das Level anhand der gesamten XP.
        return totalXp / 100 + 1; // Startet bei Level eins und erhöht alle 100 XP das Level.
    } // Beendet die Level-Berechnung.

    public int streakFor(List<DailyEntry> entries, LocalDate today) { // Zählt die Tage in Folge mit einem Check-in.
        Set<LocalDate> days = new HashSet<>(); // Sammelt alle Tage, an denen ein Eintrag existiert.
        for (DailyEntry entry : entries) { // Durchläuft alle gespeicherten Einträge.
            days.add(entry.date()); // Merkt sich das Datum des Eintrags.
        } // Beendet das Sammeln der Tage.
        LocalDate day = days.contains(today) ? today : today.minusDays(1); // Lässt die Serie bestehen, solange heute noch eingecheckt werden kann.
        int streak = 0; // Startet mit einer Serie der Länge null.
        while (days.contains(day)) { // Läuft so lange rückwärts, wie lückenlos Einträge vorhanden sind.
            streak++; // Erhöht die Serie um einen Tag.
            day = day.minusDays(1); // Springt zum jeweils vorherigen Tag.
        } // Beendet das Rückwärtszählen.
        return streak; // Liefert die Länge der aktuellen Serie zurück.
    } // Beendet die Serienberechnung.

    public String rewardFor(int level) { // Liefert die zum Level passende einfache Belohnung.
        if (level >= 5) { // Prüft, ob die höchste Prototyp-Stufe erreicht ist.
            return "Goldener Fokus-Rahmen"; // Gibt die Belohnung ab Level fünf zurück.
        } // Beendet die Prüfung auf Level fünf.
        if (level >= 3) { // Prüft, ob die mittlere Prototyp-Stufe erreicht ist.
            return "Grüner Avatar-Hintergrund"; // Gibt die Belohnung ab Level drei zurück.
        } // Beendet die Prüfung auf Level drei.
        return "Nächste Belohnung ab Level 3"; // Zeigt neuen Nutzern das nächste Ziel an.
    } // Beendet die Belohnungsauswahl.

    private void validateRating(int rating) { // Prüft einen einzelnen Fragebogenwert.
        if (rating < 0 || rating > 10) { // Erkennt Werte außerhalb der vorgesehenen Skala.
            throw new IllegalArgumentException("Bewertungen müssen zwischen 0 und 10 liegen."); // Bricht bei einer ungültigen Bewertung ab.
        } // Beendet die Bereichsprüfung.
    } // Beendet die Hilfsmethode zur Validierung.
} // Beendet die Klasse ScoreService.
