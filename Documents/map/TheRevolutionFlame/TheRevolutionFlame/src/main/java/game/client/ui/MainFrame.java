package game.client.ui;

import game.common.dto.GameResponse;
import game.common.dto.CommandRequest;
import game.client.network.NetworkClient;
import game.client.network.ResponseCallback;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;

public class MainFrame extends JFrame {

    private NetworkClient networkClient;
    private GamePanel gamePanel;
    private InventoryDialog inventoryDialog;
    private MapDialog mapDialog;
    private int idGiocatoreCorrente;

    public MainFrame() {
        super("TheRevolutionFlame");
        /*

        inizializzazione del client di rete con indirizzo e porta di default

        */
        this.networkClient = new NetworkClient("127.0.0.1", 12345);
        this.gamePanel = new GamePanel(this);
        this.inventoryDialog = new InventoryDialog(this);
        this.mapDialog = new MapDialog(this);
        this.idGiocatoreCorrente = -1;
        inizializzaComponenti();
    }

    public void inizializzaComponenti() {
        /*

        impostazione del layout manager e aggiunta del pannello centrale

        */
        this.setLayout(new BorderLayout());
        this.add(this.gamePanel, BorderLayout.CENTER);

        /*

        configurazione parametri base della finestra principale

        */
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(1024, 768);
        this.setLocationRelativeTo(null);
    }

    public void mostraSchermataIniziale() {
        /*

        delega al pannello la visualizzazione iniziale e rende visibile la finestra

        */
        this.gamePanel.mostraSchermataIniziale();
        this.setVisible(true);
        if (this.inventoryDialog != null) {
            this.inventoryDialog.setVisible(true);
        }
    }

    public void inviaAzioneAlServer(String azione, String argomento) {
        /*

        disabilita l'interazione sul pannello per bloccare l'invio di comandi a raffica

        */
        this.gamePanel.setEnabled(false);

        /*

        creazione del pacchetto dto da inoltrare

        */
        CommandRequest request = new CommandRequest(this.idGiocatoreCorrente, azione, argomento);

        /*

        inoltro asincrono al livello di rete

        */
        this.networkClient.inviaComandoAsincrono(request, new ResponseCallback() {

            @Override
            public void onResponse(GameResponse response) {
                /*

                aggiorna l'interfaccia e riabilita i controlli sull'event dispatch thread di swing

                */
                SwingUtilities.invokeLater(() -> {
                    aggiornaInterfaccia(response);
                    gamePanel.setEnabled(true);
                });
            }

            @Override
            public void onError(String errorMessage) {
                /*

                in caso di fallimento notifica l'errore e riabilita i controlli sull'event dispatch thread

                */
                SwingUtilities.invokeLater(() -> {
                    System.err.println("errore di comunicazione con il server: " + errorMessage);
                    gamePanel.setEnabled(true);
                });
            }
        });
    }

    public void aggiornaInterfaccia(GameResponse response) {
        // 1. Aggiorna il pannello principale (testo, salute, mappa)
        this.gamePanel.renderizzaStato(response);
        
        // 2. Aggiorna la scacchiera dell'inventario con le nuove matrici
        if (this.inventoryDialog != null) {
            this.inventoryDialog.aggiornaGriglia(response.getInventarioNomi());
            this.inventoryDialog.aggiornaArmaEquipaggiata(response.getCurrentWeapon());
        }
    }
    
    public void connettiAlServer() {
        boolean connesso = this.networkClient.connetti();
        if (connesso) {
            System.out.println("Connessione al server (porta 12345) stabilita con successo.");
        } else {
            System.err.println("ERRORE: Impossibile connettersi al server. Assicurati che il MainServer sia in esecuzione.");
        }
    }
}