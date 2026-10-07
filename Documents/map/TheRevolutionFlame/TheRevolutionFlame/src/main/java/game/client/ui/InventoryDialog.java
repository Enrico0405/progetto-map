package game.client.ui;

import game.common.dto.ItemDto;
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
        String[] colonneGiocatore = {"1", "2", "3", "4"};
        DefaultTableModel modelGiocatore = new DefaultTableModel(colonneGiocatore, 4); // 4 righe (numeri)
        this.tblInventarioGiocatore = new JTable(modelGiocatore);
        this.tblInventarioGiocatore.setEnabled(false); // Impedisce la modifica manuale delle celle
        pnlScacchiere.add(new JScrollPane(tblInventarioGiocatore));

        // Creazione Scacchiera Stanza (S)
        String[] colonneStanza = {"1", "2", "3", "4"};
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
    
    // --- METODI PER IL COLLEGAMENTO CON IL MAINFRAME E IL SERVER ---

    public void aggiornaGriglie(List<ItemDto> invGiocatore, List<ItemDto> invStanza) {
        DefaultTableModel modelGiocatore = (DefaultTableModel) this.tblInventarioGiocatore.getModel();
        DefaultTableModel modelStanza = (DefaultTableModel) this.tblOggettiStanza.getModel();

        // 1. Pulizia preventiva di entrambe le scacchiere
        for(int r = 0; r < modelGiocatore.getRowCount(); r++) {
            for(int c = 0; c < modelGiocatore.getColumnCount(); c++) {
                modelGiocatore.setValueAt("", r, c);
                modelStanza.setValueAt("", r, c);
            }
        }

        // 2. Popola la scacchiera del Giocatore (G)
        if (invGiocatore != null) {
            for (int i = 0; i < invGiocatore.size(); i++) {
                int riga = i / 4;
                int col = i % 4;
                // Prende il nome dal DTO
                if (riga < modelGiocatore.getRowCount() && invGiocatore.get(i) != null) {
                    modelGiocatore.setValueAt(invGiocatore.get(i).getNome(), riga, col);
                }
            }
        }

        // 3. Popola la scacchiera della Stanza (S)
        if (invStanza != null) {
            for (int i = 0; i < invStanza.size(); i++) {
                int riga = i / 4;
                int col = i % 4;
                // Prende il nome dal DTO
                if (riga < modelStanza.getRowCount() && invStanza.get(i) != null) {
                    modelStanza.setValueAt(invStanza.get(i).getNome(), riga, col);
                }
            }
        }
    }

    public void aggiornaEquipaggiamento(String arma, String pozione) {
        this.lblArmaEquipaggiata.setText("Arma: " + (arma != null && !arma.isBlank() ? arma : "Nessuna"));
        this.lblPozioneEquipaggiata.setText("Pozione: " + (pozione != null && !pozione.isBlank() ? pozione : "Nessuna"));
    }
}