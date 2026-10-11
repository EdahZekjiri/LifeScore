package at.lifescore.service; // Ordnet den Smoke-Test demselben Service-Paket wie die getestete Klasse zu.

public final class ScoreServiceSmokeTest { 
    private ScoreServiceSmokeTest() { 
    } 

    public static void main(String[] args) { 
        ScoreService service = new ScoreService(); 
        int perfectScore = service.calculateScore(10, 10, 10, 10, 10); 
        if (perfectScore != 100) { 
            throw new AssertionError("Erwartet: 100, erhalten: " + perfectScore); 
        } 
        int mixedScore = service.calculateScore(8, 6, 7, 9, 5); 
        if (mixedScore != 72) { 
            throw new AssertionError("Erwartet: 72, erhalten: " + mixedScore); 
        } 
        if (service.levelFor(200) != 3) { 
            throw new AssertionError("200 XP müssen Level 3 ergeben."); 
        } 
        System.out.println("ScoreService-Smoke-Test erfolgreich."); 
    } 
} 
