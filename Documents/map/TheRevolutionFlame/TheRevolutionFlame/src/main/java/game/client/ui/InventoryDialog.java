/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package game.client.ui;

import java.util.List;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextArea;
import javax.swing.DefaultListModel;

public class InventoryDialog extends JDialog {

    // 1. Attributi privati (i componenti visivi dell'inventario richiesti dall'UML)
    private JList<String> lstOggetti;
    private JLabel lblIconaOggetto;
    private JTextArea txtDescrizione;
    private JButton btnEquipaggia;
    private JButton btnUsa;
    private JButton btnScarta;

    // 2. Costruttore
    public InventoryDialog() {
        super(); // Richiama il costruttore base di JDialog
        this.setTitle("Inventario");
        
        // Impostando false, la finestra NON è modale.
        // Significa che il giocatore può tenere aperto l'inventario 
        // in un angolo dello schermo e continuare a muoversi nel gioco.
        this.setModal(false); 
        
        // Crea fisicamente i pulsanti e le aree di testo in memoria
        inizializzaComponenti();
    }

    // 3. Metodi della classe
    public void inizializzaComponenti() {
        // Istanziazione dei componenti di Swing
        this.lstOggetti = new JList<>();
        this.lblIconaOggetto = new JLabel("Nessuna Icona");
        
        // Crea un'area di testo di 5 righe e 20 colonne
        this.txtDescrizione = new JTextArea(5, 20); 
        // Impedisce all'utente di scriverci dentro (deve solo leggere la descrizione)
        this.txtDescrizione.setEditable(false); 
        
        this.btnEquipaggia = new JButton("Equipaggia");
        this.btnUsa = new JButton("Usa");
        this.btnScarta = new JButton("Scarta");
        
        // Imposta le dimensioni della finestrella dell'inventario
        this.setSize(400, 300);
        
        // NOTA: Proprio come nel MainFrame, qui in futuro dovrai usare this.setLayout(...) 
        // e this.add(...) per posizionare i bottoni e la lista dove preferisci.
    }

    public void mostraSchermataIniziale() {
        // Reset della finestra prima di mostrarla: 
        // svuota la descrizione e toglie eventuali selezioni vecchie
        this.txtDescrizione.setText("");
        this.lstOggetti.clearSelection();
        
        // Rende visibile la dialog a schermo
        this.setVisible(true);
    }

    public void popolaInventario(List<String> items) {
        // In Java Swing, non puoi semplicemente passare un array alla JList.
        // Devi usare un "Modello" per aggiornare i dati dinamicamente.
        DefaultListModel<String> model = new DefaultListModel<>();
        
        // Cicla tutti gli oggetti ricevuti dal server e li aggiunge al modello
        for (String item : items) {
            model.addElement(item);
        }
        
        // Applica il modello riempito alla lista grafica, che si aggiornerà in automatico
        this.lstOggetti.setModel(model);
    }
}
