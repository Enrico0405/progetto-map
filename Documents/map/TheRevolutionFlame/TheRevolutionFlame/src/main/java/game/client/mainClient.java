/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package game.client;

import game.client.ui.MainFrame;
import javax.swing.SwingUtilities;

public class mainClient {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.mostraSchermataIniziale();
            
            // Subito dopo aver mostrato la grafica, tenta la connessione al server
            frame.connettiAlServer();
        });
    }
}
