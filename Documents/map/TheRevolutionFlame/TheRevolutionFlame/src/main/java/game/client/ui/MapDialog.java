/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package game.client.ui;

import javax.swing.*;
import java.awt.*;

public class MapDialog extends JDialog {
    private JLabel lblImmagineMappa;

    /**
     * costruttore: crea lo scheletro della finestra della mappa
     */
    public MapDialog(JFrame parent) {
        // richiama il costruttore di JDialog: 
        // (finestra padre, titolo della finestra, modale)
        super(parent, "Mappa di Gioco", false); 
        
        //impostazioni base della finestra
        this.setSize(600, 400); // grandezza iniziale
        this.setLocationRelativeTo(parent); // la centra rispetto al gioco principale
        this.setLayout(new BorderLayout());
       
        // chisura della mappa
        this.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE); 

        // inizializzo JLabel che contiene l'immagine
        lblImmagineMappa = new JLabel("", SwingConstants.CENTER);
        lblImmagineMappa.setBackground(Color.BLACK);
        lblImmagineMappa.setOpaque(true); // Serve per far vedere lo sfondo nero
        
        // Aggiunta con barra di scorrimento
        this.add(new JScrollPane(lblImmagineMappa), BorderLayout.CENTER);
    }

    /**
     * Carica e mostra mappa
     */
    public void mostraMappa(String immaginePath) {
        if (immaginePath != null && !immaginePath.isEmpty()) {
            ImageIcon icona = game.client.util.ImageCacheManager.getIcona(immaginePath);
            if (icona != null && icona.getImage() != null) {
                lblImmagineMappa.setIcon(icona);
                lblImmagineMappa.setText(""); // Rimuove il testo segnaposto
            } else {
                lblImmagineMappa.setText("[ MAPPA CARICATA: " + immaginePath + " ]");
                lblImmagineMappa.setForeground(Color.WHITE);
            }
        }
        
        // Rende visibile la finestrella in sovrimpressione
        this.setVisible(true);
    }
}
