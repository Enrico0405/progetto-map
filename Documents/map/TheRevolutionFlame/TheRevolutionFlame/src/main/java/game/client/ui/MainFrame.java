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
import game.client.network.NetworkClient;
import javax.swing.JFrame;
public class MainFrame extends JFrame 
{
    private NetworkClient networkClient ;
    private GamePanel gamePanel ;
    private InventoryDialog inventoryDialog ;
    private MapDialog mapDialog ;
    private int idGiocatoreCorrente ;
    
    public MainFrame()
    {
        super("TheRevolutionFlame"); "serve per dare il titolo alla finestra"
        this.networkClient=new NetworkClient();
        this.gamePanel=new GamePanel();
        this.inventoryDialog=new InventoryDialog();
        this.mapDialog=new MapDialog();
        this.idGiocatoreCorrente=-1;
        inizializzaComponenti();
    }
    
    public void inizializzaComponenti()
    {
        
    }
    
    public void mostraSchermataIniziale()
    {
        
    }
    
    public void inviaAzioneAlServer(String azione,String argomento)
    {
        
    }
    
    public void aggiornaInterfaccia(GameResponse response)
    {
        
    }
}
