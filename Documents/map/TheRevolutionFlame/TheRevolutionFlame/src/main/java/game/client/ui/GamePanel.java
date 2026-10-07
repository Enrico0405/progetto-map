/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package game.client.ui;
import game.common.dto.GameResponse;
import javax.swing.*;
import java.awt.*;

public class GamePanel extends JPanel {

    //label della stanza
    private JLabel lblSfondoStanza; 

    private JTextPane txtLogConsole; // uso il JTextPane per permettere di usare testi colorati
    private JTextField txtInputComando; //barra dove il player scriverà i comandi
    
    // componenti di stato
    private JLabel lblSalute;
    private JLabel lblArmaEquipaggiata;
    private JTextArea txtInventarioScorte;
    private MainFrame mainFrame;

    public GamePanel(MainFrame mainFrame) {
        // main layout per il pannello
        this.setLayout(new BorderLayout());
        
        this.mainFrame = mainFrame;

        // immagine di gioco centrale
        JPanel pnlVisual = new JPanel(new BorderLayout());
        pnlVisual.setBackground(Color.BLACK);
        
        lblSfondoStanza = new JLabel("", SwingConstants.CENTER);
        
        // Trucco visivo temporaneo per vedere l'area prima di caricare le PNG
        pnlVisual.setBorder(BorderFactory.createLineBorder(Color.YELLOW, 2));
        lblSfondoStanza.setText("QUI ANDRÀ L'IMMAGINE DI SFONDO (o la Mappa)");
        lblSfondoStanza.setForeground(Color.WHITE);
        
        pnlVisual.add(lblSfondoStanza, BorderLayout.CENTER);
        this.add(pnlVisual, BorderLayout.CENTER);

        // stato operativo a destra

        JPanel pnlStato = new JPanel();
        // BoxLayout dispone gli elementi in verticale, uno sotto l'altro
        pnlStato.setLayout(new BoxLayout(pnlStato, BoxLayout.Y_AXIS)); 
        pnlStato.setPreferredSize(new Dimension(200, 0)); // Fissa la larghezza a 200 pixel
        pnlStato.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY), "Stato Operativo"));
        pnlStato.setBackground(Color.DARK_GRAY);

        // label per salute e arma
        lblSalute = new JLabel("Salute: 100%");
        lblSalute.setForeground(Color.GREEN);
        lblSalute.setFont(new Font("Arial", Font.BOLD, 14));
        
        lblArmaEquipaggiata = new JLabel("Arma: NESSUNA");
        lblArmaEquipaggiata.setForeground(Color.WHITE);
        
        // inventario oggetti
        txtInventarioScorte = new JTextArea(10, 15);
        txtInventarioScorte.setEditable(false);
        txtInventarioScorte.setBackground(Color.DARK_GRAY);
        txtInventarioScorte.setForeground(Color.LIGHT_GRAY);
        txtInventarioScorte.setText("Scorte:\n- vuoto");

        // aggiunta di alcuni label alla barra destra, separandola da quella centrale dell'immagine con un RigidArea
        pnlStato.add(Box.createRigidArea(new Dimension(0, 10)));
        pnlStato.add(lblSalute);
        pnlStato.add(Box.createRigidArea(new Dimension(0, 10)));
        pnlStato.add(lblArmaEquipaggiata);
        pnlStato.add(Box.createRigidArea(new Dimension(0, 20)));
        pnlStato.add(new JScrollPane(txtInventarioScorte));
        
        this.add(pnlStato, BorderLayout.EAST);

        // input e comandi testuali al di sotto

        JPanel pnlTerminale = new JPanel(new BorderLayout());
        
        // console testuale (JTextPane per supportare piú colori)
        txtLogConsole = new JTextPane();
        txtLogConsole.setEditable(false);
        txtLogConsole.setBackground(Color.BLACK);
        txtLogConsole.setForeground(Color.WHITE);
        txtLogConsole.setPreferredSize(new Dimension(0, 150)); // altezza fissa per dare molto spazio al testo
        
        JScrollPane scrollConsole = new JScrollPane(txtLogConsole);
        
        // barra di input
        txtInputComando = new JTextField();
        txtInputComando.setBackground(Color.BLACK);
        txtInputComando.setForeground(Color.GREEN); // Stile "Hacker/Terminale retro"
        txtInputComando.setFont(new Font("Monospaced", Font.BOLD, 16));
        txtInputComando.setCaretColor(Color.GREEN); // Cursore lampeggiante verde
        txtInputComando.setBorder(BorderFactory.createLineBorder(Color.GREEN, 2)); // Crea un riquadro verde
        txtInputComando.setPreferredSize(new Dimension(0, 40));
        
        txtInputComando.addActionListener(e -> { 
            String testoInserito = txtInputComando.getText().trim();
            if(!testoInserito.isEmpty()){
                
                // 1. Caso speciale: il giocatore inserisce l'ID (es. "id:Enrico123")
                if (testoInserito.toLowerCase().startsWith("id:")) {
                    // Prende tutto ciò che c'è dopo i primi 3 caratteri ("id:")
                    String nomeId = testoInserito.substring(3).trim();
                    mainFrame.inviaAzioneAlServer("ID", nomeId);
                } 
                // 2. Tutti gli altri comandi di gioco normali
                else {
                    // Divide la frase in massimo 2 parti, tagliando SOLO al PRIMO SPAZIO
                    String[] parti = testoInserito.split(" ", 2); 
                    
                    // La prima parola diventa l'Azione (messa in maiuscolo per comodità)
                    String azione = parti[0].toUpperCase(); 
                    
                    // Tutto il resto della frase diventa l'Argomento (se esiste)
                    String argomento = (parti.length > 1) ? parti[1].trim() : ""; 
                    
                    mainFrame.inviaAzioneAlServer(azione, argomento);
                }
                
                txtInputComando.setText("");
            }
        });

        pnlTerminale.add(scrollConsole, BorderLayout.CENTER);
        pnlTerminale.add(txtInputComando, BorderLayout.SOUTH);

        this.add(pnlTerminale, BorderLayout.SOUTH);
    }
    public void renderizzaStato(GameResponse response) {
        
        // stampa messaggio in console
        if (response.getMessaggio() != null && !response.getMessaggio().isEmpty()) {
            aggiungiTestoAllaConsole(response.getMessaggio());
        }

        // aggiorna statistiche giocatore
        lblSalute.setText("Salute: " + response.getPlayerHp() + "/" + response.getPlayerMaxHp());
        
        // rende il colore della salute giallo se va al di sotto della metá circa
        if (response.getPlayerHp() <= 40) {
            lblSalute.setForeground(Color.YELLOW);
        } else {
            lblSalute.setForeground(Color.GREEN);
        }
        
        // rende il colore della salute rosso se bassa
        if (response.getPlayerHp() <= 20) {
            lblSalute.setForeground(Color.RED);
        } else {
            lblSalute.setForeground(Color.GREEN);
        }

        // aggiorna immagine centrale: preferisce la mappa allo sfondo
        String percorsoImmagine;
        if (response.getMappaPngPath() != null && !response.getMappaPngPath().isEmpty()) {
            percorsoImmagine = response.getMappaPngPath();
        } else {
            percorsoImmagine = response.getStanzaSfondoPath();
        }

        if (percorsoImmagine != null && !percorsoImmagine.isEmpty()) {
            /* togliere le spunte di commento quando imagecachemanager è pronto:
             * ImageIcon icona = ImageCacheManager.getIcona(percorsoImmagine);
             * lblSfondoStanza.setIcon(icona);
             * lblSfondoStanza.setText(""); // rimuove placeholder testuale
             */
             
             // placeholder temporaneo debug
             lblSfondoStanza.setText("[IMMAGINE CARICATA: " + percorsoImmagine + "]");
        }

        // gestione combattimento
        if (response.isInCombattimento()) {
            // stampa hp nemico
            aggiungiTestoAllaConsole(">> COMBATTIMENTO IN CORSO! HP Nemico: " + response.getEnemyHp());
        }

        // gestione fine partita
        if (response.isGameOver() || response.isVittoria()) {
            txtInputComando.setEnabled(false); // disabilita input
            txtInputComando.setText("PARTITA TERMINATA.");
            
            if (response.isVittoria()) {
                aggiungiTestoAllaConsole("\n*** HAI VINTO! ***");
            } else {
                aggiungiTestoAllaConsole("\n*** GAME OVER ***");
            }
        }
        
        // pulisce input
        txtInputComando.setText("");
    }


    /**
     * inserisce testo formattato nel jtextpane
     */
    private void aggiungiTestoAllaConsole(String testo) {
        try {
            javax.swing.text.Document doc = txtLogConsole.getDocument();
            // aggiunge testo a fine documento
            doc.insertString(doc.getLength(), testo + "\n\n", null);
            // autoscroll in basso
            txtLogConsole.setCaretPosition(doc.getLength());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void mostraSchermataIniziale(){
        aggiungiTestoAllaConsole("Avvio del terminale...");
        aggiungiTestoAllaConsole("Benvenuto su The Revolution Flame. Attesa connessione...");
        
        // ABILITA l'input testuale per permettere al giocatore di scrivere
        txtInputComando.setEnabled(true);
        
        // Mette il cursore lampeggiante in automatico sulla barra
        txtInputComando.requestFocusInWindow(); 
    }
}