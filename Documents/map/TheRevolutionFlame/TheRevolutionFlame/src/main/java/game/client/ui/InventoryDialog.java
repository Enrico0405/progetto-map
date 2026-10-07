package game.client.ui;

import java.util.List;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.GridLayout;

public class InventoryDialog extends JDialog {

    // 1. Attributi per gli slot equipaggiamento
    private JLabel lblArmaEquipaggiata;
    private JLabel lblPozioneEquipaggiata;

    // 2. Attributi per le "scacchiere"
    private JTable tblInventarioGiocatore; // Prefisso G
    private JTable tblOggettiStanza;       // Prefisso S
    
    // 3. Attributo per l'esame degli oggetti
    private JTextArea txtDescrizione;

    public InventoryDialog(MainFrame parent) {
        super(parent, "Inventario e Stanza", false);
        this.setSize(700, 500);
        this.setLocationRelativeTo(parent);
        inizializzaComponenti();
    }

    public void inizializzaComponenti() {
        this.setLayout(new BorderLayout(10, 10));

        // --- PANNELLO ALTO: Slot Equipaggiamento ---
        JPanel pnlEquip = new JPanel(new GridLayout(1, 2, 10, 0));
        this.lblArmaEquipaggiata = new JLabel("Arma: Nessuna", JLabel.CENTER);
        this.lblPozioneEquipaggiata = new JLabel("Pozione: Nessuna", JLabel.CENTER);
        pnlEquip.add(lblArmaEquipaggiata);
        pnlEquip.add(lblPozioneEquipaggiata);
        this.add(pnlEquip, BorderLayout.NORTH);

        // --- PANNELLO CENTRALE: Le due scacchiere ---
        JPanel pnlScacchiere = new JPanel(new GridLayout(1, 2, 10, 0));
        
        // Creazione Scacchiera Giocatore (G)
        // Le colonne rappresentano le lettere (es. Ga, Gb, Gc)
        String[] colonneGiocatore = {"A", "B", "C", "D"};
        DefaultTableModel modelGiocatore = new DefaultTableModel(colonneGiocatore, 4); // 4 righe (numeri)
        this.tblInventarioGiocatore = new JTable(modelGiocatore);
        this.tblInventarioGiocatore.setEnabled(false); // Impedisce la modifica manuale delle celle
        pnlScacchiere.add(new JScrollPane(tblInventarioGiocatore));

        // Creazione Scacchiera Stanza (S)
        String[] colonneStanza = {"A", "B", "C", "D"};
        DefaultTableModel modelStanza = new DefaultTableModel(colonneStanza, 4);
        this.tblOggettiStanza = new JTable(modelStanza);
        this.tblOggettiStanza.setEnabled(false);
        pnlScacchiere.add(new JScrollPane(tblOggettiStanza));
        
        this.add(pnlScacchiere, BorderLayout.CENTER);

        // --- PANNELLO BASSO: Descrizione Oggetti ---
        this.txtDescrizione = new JTextArea(4, 20);
        this.txtDescrizione.setEditable(false);
        this.txtDescrizione.setText("Usa il comando 'esamina [cella]' per vedere i dettagli qui.");
        this.add(new JScrollPane(txtDescrizione), BorderLayout.SOUTH);
    }

    public void mostraSchermataIniziale() {
        this.txtDescrizione.setText("In attesa di dati dal server...");
        this.setVisible(true);
    }

    // Il metodo dovrà poi essere aggiornato per ricevere una matrice di dati o una struttura dati adatta
    public void aggiornaScacchiere(Object[][] datiGiocatore, Object[][] datiStanza, String arma, String pozione) {
        this.lblArmaEquipaggiata.setText("Arma: " + arma);
        this.lblPozioneEquipaggiata.setText("Pozione: " + pozione);
        
        // Qui andrà la logica per aggiornare i DefaultTableModel con le coordinate corrette
    }
}