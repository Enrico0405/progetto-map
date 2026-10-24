package game.client.util;

import javazoom.jl.player.Player;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;

public class SoundManager {

    private static Player musicPlayer;
    private static Thread musicThread;
    private static String correnteTracciaPath = "";

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

        // 3. Prova come file fisico nel File System
        try {
            File fileFisico = new File(pathSenzaSlash);
            if (fileFisico.exists()) {
                return new FileInputStream(fileFisico);
            }
        } catch (Exception ignored) {}

        return null;
    }

    /**
     * Riproduce una traccia MP3 di sottofondo in loop continuo.
     * Termina tassativamente la traccia precedente ed evita riavvii se la traccia è già attiva.
     */
    public static synchronized void playMusic(String resourcePath) {
        if (resourcePath == null || resourcePath.isBlank()) {
            stopMusic();
            return;
        }

        // Se la traccia richiesta è già in riproduzione nel thread attivo, non fare nulla
        if (resourcePath.equals(correnteTracciaPath) && musicThread != null && musicThread.isAlive()) {
            return;
        }

        // 1. Ferma e cancella il riferimento al thread e al player precedente
        stopMusic();

        correnteTracciaPath = resourcePath;

        // 2. Crea il nuovo thread
        musicThread = new Thread(() -> {
            Thread threadMio = Thread.currentThread();

            // Il ciclo continua SOLO finché 'musicThread' punta a questo specifico thread
            while (musicThread == threadMio && !threadMio.isInterrupted()) {
                try (InputStream is = caricaStreamAudio(resourcePath)) {
                    if (is == null) {
                        System.err.println("[SoundManager] Impossibile trovare il file MP3: " + resourcePath);
                        break;
                    }
                    BufferedInputStream bis = new BufferedInputStream(is);

                    synchronized (SoundManager.class) {
                        // Controllo di sicurezza se il thread è stato cambiato durante il caricamento dello stream
                        if (musicThread != threadMio) break;
                        musicPlayer = new Player(bis);
                    }

                    musicPlayer.play(); // Chiamata bloccante: si sblocca appena viene chiamato musicPlayer.close()

                } catch (Exception e) {
                    break;
                }
            }
        });

        musicThread.setDaemon(true);
        musicThread.start();
    }

    /**
     * Ferma la musica di sottofondo e distrugge il player e il thread attivi.
     */
    public static synchronized void stopMusic() {
        correnteTracciaPath = "";

        if (musicPlayer != null) {
            try {
                musicPlayer.close(); // Sblocca la play() nel thread
            } catch (Exception ignored) {}
            musicPlayer = null;
        }

        if (musicThread != null) {
            Thread t = musicThread;
            musicThread = null; // Annulla il riferimento: il vecchio thread uscirà dal ciclo immediatamente
            t.interrupt();
        }
    }

    /**
     * Riproduce un effetto sonoro MP3 una singola volta senza interrompere la musica di sottofondo.
     */
    public static void playEffect(String resourcePath) {
        new Thread(() -> {
            try (InputStream is = caricaStreamAudio(resourcePath)) {
                if (is == null) return;
                BufferedInputStream bis = new BufferedInputStream(is);
                Player effectPlayer = new Player(bis);
                effectPlayer.play();
            } catch (Exception ignored) {}
        }).start();
    }
}