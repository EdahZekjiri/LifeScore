package at.lifescore; 

import javax.swing.SwingUtilities;

import at.lifescore.repository.EntryRepository; 
import at.lifescore.service.ScoreService;
import at.lifescore.ui.LifeScoreFrame;
import at.lifescore.ui.UiTheme; 

public final class Main { 
    private Main() { 
    } 

    public static void main(String[] args) { 
        SwingUtilities.invokeLater(() -> { 
            UiTheme.install(); 
            ScoreService scoreService = new ScoreService(); 
            EntryRepository repository = new EntryRepository(); 
            LifeScoreFrame frame = new LifeScoreFrame(scoreService, repository); 
            frame.setVisible(true); 
        }); 
    } 
} 
