/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package game.client.util;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import java.awt.image.BufferedImage;
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

        if (urlRisorsa != null) {
            try {
                BufferedImage imgOriginale = ImageIO.read(urlRisorsa);
                if (imgOriginale != null) {
                    // Applica il filtro per rimuovere la finta scacchiera di grigi e bianchi
                    BufferedImage imgTrasparente = rimuoviFintaScacchiera(imgOriginale);
                    
                    ImageIcon icona = new ImageIcon(imgTrasparente);
                    cache.put(nomeFile, icona);
                    return icona;
                }
            } catch (Exception e) {
                System.err.println("[ImageCacheManager] Errore nella lettura dell'immagine: " + nomeFile);
                e.printStackTrace();
            }
        }

        // restituisce null e stampa warning se il file non esiste
        System.err.println("[ImageCacheManager] Risorsa non trovata: " + nomeFile);
        return null;
    }

    /**
     * Rileva i pixel bianchi e grigio chiaro neutri della finta scacchiera e li rende trasparenti.
     */
    private static BufferedImage rimuoviFintaScacchiera(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();

        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = img.getRGB(x, y);

                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                // Calcola la variazione tra i canali di colore (saturazione)
                int max = Math.max(r, Math.max(g, b));
                int min = Math.min(r, Math.min(g, b));
                int diff = max - min;

                // Un pixel appartiene alla finta scacchiera se:
                // 1. Ha luminosità alta (grigio chiaro > 185 o bianco)
                // 2. Ha variazione di colore quasi nulla (è un grigio/bianco neutro)
                boolean isFintaScacchiera = (r > 185 && diff < 15);

                if (isFintaScacchiera) {
                    // Pixel completamente trasparente (Alpha = 0)
                    output.setRGB(x, y, 0x00000000);
                } else {
                    // Mantiene il pixel originale
                    output.setRGB(x, y, rgb);
                }
            }
        }
        return output;
    }
}