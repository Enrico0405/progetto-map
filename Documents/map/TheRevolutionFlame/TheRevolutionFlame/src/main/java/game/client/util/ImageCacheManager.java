/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package game.client.util;

import javax.swing.ImageIcon;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ImageCacheManager {

    // dizionario per memorizzare le immagini già caricate
    private static final Map<String, ImageIcon> cache = new HashMap<>();

    // costruttore
    private ImageCacheManager() {
    }

    /**
     * recupera un'immagine dalla cache o la carica dalle risorse
     */
    public static ImageIcon getIcona(String nomeFile) {
        if (nomeFile == null || nomeFile.trim().isEmpty()) {
            return null;
        }

        // restituisce l'immagine se già presente in memoria
        if (cache.containsKey(nomeFile)) {
            return cache.get(nomeFile);
        }

        // carica la risorsa dal classpath
        URL urlRisorsa = ImageCacheManager.class.getResource("/" + nomeFile);
        
        // fallback senza slash iniziale se non trovata
        if (urlRisorsa == null) {
            urlRisorsa = ImageCacheManager.class.getResource(nomeFile);
        }
        else{    
            ImageIcon icona = new ImageIcon(urlRisorsa);
            cache.put(nomeFile, icona);
            return icona;
        }

        // restituisce null e stampa warning se il file non esiste
        System.err.println("[ImageCacheManager] Risorsa non trovata: " + nomeFile);
        return null;
    }
}
