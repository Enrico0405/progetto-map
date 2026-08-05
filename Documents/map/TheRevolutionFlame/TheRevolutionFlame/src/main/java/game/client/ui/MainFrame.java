/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package game.client.ui;

/**
 *
 * @author Utente
 */

import game.common.dto.GameResponse;
import game.common.dto.CommandRequest;
import game.client.network.NetworkClient;
import javax.swing.JFrame;
import java.awt.BorderLayout;
import game.client.network.ResponseCallback;

public class MainFrame extends JFrame 
{
    private NetworkClient networkClient ;
    private GamePanel gamePanel ;
    private InventoryDialog inventoryDialog ;
    private MapDialog mapDialog ;
    private int idGiocatoreCorrente ;
    
    public MainFrame()
    {
        super("TheRevolutionFlame"); //serve per dare il titolo alla finestra
        this.networkClient=new NetworkClient();
        this.gamePanel = new GamePanel(this);
        this.inventoryDialog=new InventoryDialog();
        this.mapDialog=new MapDialog(this);
        this.idGiocatoreCorrente=-1;
        inizializzaComponenti();
    }
    
    public void inizializzaComponenti()
    {
        //Imposta il gestore del layout per organizzare gli spazi
        this.setLayout(new BorderLayout());
        
        //Aggiunge il pannello di gioco principale al centro della finestra
        this.add(this.gamePanel, BorderLayout.CENTER);
        
        //Garantisce che il processo Java termini quando si preme la "X"
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        //Imposta le dimensioni di default della finestra (larghezza, altezza)
        this.setSize(1024, 768);
        
        // Centra automaticamente la finestra nello schermo al suo avvio
        this.setLocationRelativeTo(null);
    }
    
    public void mostraSchermataIniziale()
    {
        // Delega al pannello interno la preparazione dei suoi elementi grafici
        this.gamePanel.mostraSchermataIniziale();
        
        // Rende visibile sul monitor la finestra dell'applicazione
        this.setVisible(true);
    }
    
    public void inviaAzioneAlServer(String azione,String argomento)
    {
        // Creazione del pacchetto dati (DTO) da spedire al server
        CommandRequest request = new CommandRequest(this.idGiocatoreCorrente, azione, argomento);

        // Inoltro al livello di rete con gestione asincrona della risposta
        this.networkClient.inviaComandoAsincrono(request, new ResponseCallback() {
            
            @Override
            public void onResponse(GameResponse response) {
                // Quando il server risponde con successo, si passa il DTO alla grafica
                aggiornaInterfaccia(response);
            }

            @Override
            public void onErrorErrorMessage(String errorMessage) {
                // Gestione di un eventuale errore (es. server disconnesso o pacchetto perso)
                System.err.println("Errore di comunicazione con il server: " + errorMessage);
            }
        });
    }
    
    public void aggiornaInterfaccia(GameResponse response)
    {
        // Delega l'aggiornamento visivo di tutti i componenti al GamePanel
        this.gamePanel.renderizzaStato(response);
    }
}
