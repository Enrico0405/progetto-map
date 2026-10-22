package game.client.ui;

import javax.swing.*;
import java.awt.*;

public class MapDialog extends JDialog {
    private JLabel lblImmagineMappa;

    /**
     * Costruttore: crea lo scheletro della finestra della mappa
     */
    public MapDialog(JFrame parent) {
        super(parent, "Mappa del Regno", false); 
        
        // Impostazioni della finestra
        this.setSize(800, 560); // Dimensione proporzionata per la mappa
        this.setLocationRelativeTo(parent); // La centra rispetto al gioco
        this.setLayout(new BorderLayout());
        this.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE); 

        // Inizializza JLabel per contenere l'immagine
        lblImmagineMappa = new JLabel("", SwingConstants.CENTER);
        lblImmagineMappa.setBackground(Color.BLACK);
        lblImmagineMappa.setOpaque(true);
        
        this.add(lblImmagineMappa, BorderLayout.CENTER);
    }

    /**
     * Carica e ridimensiona la mappa per adattarla alla finestra
     */
    public void mostraMappa(String immaginePath) {
        if (immaginePath != null && !immaginePath.isEmpty()) {
            ImageIcon icona = game.client.util.ImageCacheManager.getIcona(immaginePath);
            
            if (icona != null && icona.getImage() != null) {
                // Calcola le dimensioni interne disponibili
                int targetWidth = 780;
                int targetHeight = 520;

                // Ridimensiona l'immagine mantenendo l'alta qualità
                Image imgOriginale = icona.getImage();
                Image imgScalata = imgOriginale.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);

                lblImmagineMappa.setIcon(new ImageIcon(imgScalata));
                lblImmagineMappa.setText(""); 
            } else {
                lblImmagineMappa.setIcon(null);
                lblImmagineMappa.setText("[ MAPPA CARICATA: " + immaginePath + " ]");
                lblImmagineMappa.setForeground(Color.WHITE);
            }
        }
        
        // Mostra la finestra centrata
        this.setVisible(true);
    }
}