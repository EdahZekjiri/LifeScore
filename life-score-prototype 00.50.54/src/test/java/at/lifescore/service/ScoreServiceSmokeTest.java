package at.lifescore.service; // Ordnet den Smoke-Test demselben Service-Paket wie die getestete Klasse zu.

public final class ScoreServiceSmokeTest { // Definiert einen einfachen Test ohne externe Testbibliothek.
    private ScoreServiceSmokeTest() { // Verhindert das unnötige Erzeugen eines Testobjekts.
    } // Beendet den privaten Konstruktor.

    public static void main(String[] args) { // Startet den Test direkt über die Java-Laufzeit.
        ScoreService service = new ScoreService(); // Erzeugt die zu prüfende Berechnungslogik.
        int perfectScore = service.calculateScore(10, 10, 10, 10, 10); // Berechnet den erwarteten Maximalwert.
        if (perfectScore != 100) { // Prüft, ob die Normalisierung korrekt hundert ergibt.
            throw new AssertionError("Erwartet: 100, erhalten: " + perfectScore); // Bricht den Test mit verständlicher Abweichung ab.
        } // Beendet die Prüfung des Maximalwerts.
        int mixedScore = service.calculateScore(8, 6, 7, 9, 5); // Berechnet einen realistischen gemischten Tageswert.
        if (mixedScore != 72) { // Prüft die festgelegten Gewichtungen und die Rundung anhand eines konkreten Beispiels.
            throw new AssertionError("Erwartet: 72, erhalten: " + mixedScore); // Bricht bei einer falschen Gewichtung oder Rundung ab.
        } // Beendet die Prüfung des gemischten Werts.
        if (service.levelFor(200) != 3) { // Prüft den Levelaufstieg nach jeweils hundert XP.
            throw new AssertionError("200 XP müssen Level 3 ergeben."); // Meldet eine fehlerhafte Levelberechnung.
        } // Beendet die Prüfung der Levelberechnung.
        System.out.println("ScoreService-Smoke-Test erfolgreich."); // Bestätigt alle erfolgreich bestandenen Prüfungen.
    } // Beendet die Testmethode.
} // Beendet die Testklasse.
