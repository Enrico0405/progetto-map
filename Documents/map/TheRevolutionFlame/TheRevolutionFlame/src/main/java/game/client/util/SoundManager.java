package game.client.util;

import javazoom.jl.player.Player;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;

public class SoundManager {

    private static Player musicPlayer;
    private static Thread musicThread;
    private static volatile boolean isLooping = false;

    /**
     * Tenta il caricamento flessibile del file audio da diverse sorgenti
     */
    private static InputStream caricaStreamAudio(String resourcePath) {
        if (resourcePath == null || resourcePath.isBlank()) return null;

        String pathConSlash = resourcePath.startsWith("/") ? resourcePath : "/" + resourcePath;
        String pathSenzaSlash = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;

        // 1. Prova tramite ClassLoader di sistema
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(pathSenzaSlash);
        if (is != null) return is;

        // 2. Prova tramite il ClassLoader della classe SoundManager
        is = SoundManager.class.getResourceAsStream(pathConSlash);
        if (is != null) return is;

        // 3. Prova come file fisico nel File System (es. cartella data/ o risorse nella radice del progetto)
        try {
            File fileFisico = new File(pathSenzaSlash);
            if (fileFisico.exists()) {
                return new FileInputStream(fileFisico);
            }
        } catch (Exception ignored) {}

        return null;
    }

    /**
     * Riproduce una traccia MP3 di sottofondo in loop.
     * Esempio: SoundManager.playMusic("colonne_sonore/menu/tema.mp3");
     */
    public static synchronized void playMusic(String resourcePath) {
        stopMusic();

        isLooping = true;
        musicThread = new Thread(() -> {
            while (isLooping) {
                try (InputStream is = caricaStreamAudio(resourcePath)) {
                    if (is == null) {
                        System.err.println("[SoundManager] Impossibile trovare il file MP3: " + resourcePath);
                        break;
                    }
                    BufferedInputStream bis = new BufferedInputStream(is);
                    musicPlayer = new Player(bis);
                    musicPlayer.play();
                } catch (Exception e) {
                    if (isLooping) {
                        System.err.println("[SoundManager] Errore riproduzione musica: " + e.getMessage());
                    }
                    break;
                }
            }
        });
        musicThread.setDaemon(true);
        musicThread.start();
    }

    /**
     * Ferma la musica di sottofondo.
     */
    public static synchronized void stopMusic() {
        isLooping = false;
        if (musicPlayer != null) {
            try {
                musicPlayer.close();
            } catch (Exception ignored) {}
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
            try (InputStream is = caricaStreamAudio(resourcePath)) {
                if (is == null) {
                    System.err.println("[SoundManager] Effetto sonoro non trovato: " + resourcePath);
                    return;
                }
                BufferedInputStream bis = new BufferedInputStream(is);
                Player effectPlayer = new Player(bis);
                effectPlayer.play();
            } catch (Exception e) {
                System.err.println("[SoundManager] Errore effetto sonoro: " + e.getMessage());
            }
        }).start();
    }
}