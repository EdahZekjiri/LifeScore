package at.lifescore; // Legt das Basispaket der Anwendung fest.

import at.lifescore.repository.EntryRepository; // Macht die lokale Datenspeicherung verfügbar.
import at.lifescore.service.ScoreService; // Macht die fachliche Score-Logik verfügbar.
import at.lifescore.ui.LifeScoreFrame; // Macht das Hauptfenster der Anwendung verfügbar.
import javax.swing.SwingUtilities; // Stellt den sicheren Start der Swing-Oberfläche bereit.

public final class Main { // Definiert den nicht vererbbaren Einstiegspunkt der Anwendung.
    private Main() { // Verhindert das unnötige Erzeugen eines Main-Objekts.
    } // Beendet den privaten Konstruktor.

    public static void main(String[] args) { // Startet die Anwendung über die Java-Laufzeit.
        SwingUtilities.invokeLater(() -> { // Erstellt die Oberfläche im dafür vorgesehenen Swing-Thread.
            ScoreService scoreService = new ScoreService(); // Erzeugt den Dienst für Score, XP und Level.
            EntryRepository repository = new EntryRepository(); // Erzeugt die lokale CSV-Datenablage.
            LifeScoreFrame frame = new LifeScoreFrame(scoreService, repository); // Baut das Hauptfenster mit seinen Abhängigkeiten auf.
            frame.setVisible(true); // Zeigt das fertig aufgebaute Hauptfenster an.
        }); // Beendet den verzögert ausgeführten Startblock.
    } // Beendet die main-Methode.
} // Beendet die Klasse Main.

