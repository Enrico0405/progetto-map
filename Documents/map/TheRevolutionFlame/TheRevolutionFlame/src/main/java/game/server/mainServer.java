package game.server;

import game.server.controller.GameEngine;
import game.server.db.DatabaseManager;
import game.server.network.GameServer;
import game.server.network.SessionManager;

/**
 * Programma main del server 
 * Entry-point principale dell'applicazione Server.
 * Applica la strategia Fail-Fast: verifica la connessione al Database
 * prima di istanziare l'engine di gioco e avviare la porta di rete.
 */
public class mainServer {

    private static final int DEFAULT_PORT = 12345;

    public static void main(String[] args) {
        int porta = DEFAULT_PORT;

        // Consente facoltativamente di passare la porta da riga di comando
        if (args.length > 0) {
            try {
                porta = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("[AVVISO] Porta specificata non valida. Uso la porta predefinita: " + DEFAULT_PORT);
            }
        }

        System.out.println("[BOOT] Avvio sequenza di inizializzazione server...");

        // 1. Inizializzazione e verifica Fail-Fast del Database
        DatabaseManager dbManager = new DatabaseManager();
        try {
            dbManager.connetti();
            System.out.println("[BOOT] Connessione al Database stabilita con successo.");
        } catch (Exception e) {
            System.err.println("==================================================");
            System.err.println(" [ERRORE CRITICO BOOT] Impossibile connettersi al Database.");
            System.err.println(" Verificare che il DB sia attivo/configurato correttamente.");
            System.err.println("==================================================");
            e.printStackTrace();
            return; // Arresta l'esecuzione prima di aprire socket di rete
        }

        // 2. Inizializzazione della gestione sessioni e dell'engine di gioco
        SessionManager sessionManager = new SessionManager();
        GameEngine gameEngine = new GameEngine(dbManager, sessionManager);

        // 3. Istanziazione del server di rete
        GameServer server = new GameServer(porta, sessionManager, gameEngine);

        // 4. Registrazione dello Shutdown Hook per la pulizia delle risorse in caso di arresto
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n[BOOT] Intercettato segnale di arresto...");
            server.stop();
            dbManager.disconnetti();
            System.out.println("[BOOT] Shutdown completato.");
        }));

        // 5. Avvio del server di rete (ciclo bloccante)
        server.start();
    }
}