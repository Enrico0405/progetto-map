package game.client.util;

import javazoom.jl.player.Player;
import java.io.BufferedInputStream;
import java.io.InputStream;

public class SoundManager {

    private static Player musicPlayer;
    private static Thread musicThread;
    private static volatile boolean isLooping = false;

    /**
     * Riproduce una traccia MP3 di sottofondo in loop.
     * Esempio: SoundManager.playMusic("/colonne_sonore/menu/tema.mp3");
     */
    public static synchronized void playMusic(String resourcePath) {
        stopMusic();

        isLooping = true;
        musicThread = new Thread(() -> {
            while (isLooping) {
                try (InputStream is = SoundManager.class.getResourceAsStream(resourcePath)) {
                    if (is == null) {
                        System.err.println("File MP3 non trovato: " + resourcePath);
                        break;
                    }
                    BufferedInputStream bis = new BufferedInputStream(is);
                    musicPlayer = new Player(bis);
                    musicPlayer.play();
                } catch (Exception e) {
                    System.err.println("Errore riproduzione musica MP3: " + e.getMessage());
                    break;
                }
            }
        });
        musicThread.setDaemon(true); // Non blocca la chiusura dell'app
        musicThread.start();
    }

    /**
     * Ferma la musica di sottofondo.
     */
    public static synchronized void stopMusic() {
        isLooping = false;
        if (musicPlayer != null) {
            musicPlayer.close();
            musicPlayer = null;
        }
        if (musicThread != null) {
            musicThread.interrupt();
            musicThread = null;
        }
    }

    /**
     * Riproduce un effetto sonoro MP3 una singola volta.
     */
    public static void playEffect(String resourcePath) {
        new Thread(() -> {
            try (InputStream is = SoundManager.class.getResourceAsStream(resourcePath)) {
                if (is == null) {
                    System.err.println("Effetto sonoro MP3 non trovato: " + resourcePath);
                    return;
                }
                BufferedInputStream bis = new BufferedInputStream(is);
                Player effectPlayer = new Player(bis);
                effectPlayer.play();
            } catch (Exception e) {
                System.err.println("Errore effetto sonoro: " + e.getMessage());
            }
        }).start();
    }
}