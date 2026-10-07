package game.server.network;

import game.server.controller.GameEngine;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Server di rete multithread.
 * Gestisce l'ascolto delle connessioni TCP in ingresso e delega
 * la gestione di ciascun client a un thread dedicato tramite ClientHandler.
 */
public class GameServer {

    private final int port;
    private ServerSocket serverSocket;
    private final ExecutorService threadPool;
    private volatile boolean running;

    private final SessionManager sessionManager;
    private final GameEngine gameEngine;

    // Contatore thread-safe per la generazione progressiva degli ID sessione
    private final AtomicInteger sessionCounter = new AtomicInteger(1);

    /**
     * Costruttore principale con dependency injection dei componenti già inizializzati.
     */
    public GameServer(int port, SessionManager sessionManager, GameEngine gameEngine) {
        this.port = port;
        this.sessionManager = sessionManager;
        this.gameEngine = gameEngine;
        this.threadPool = Executors.newCachedThreadPool();
    }

    /**
     * Avvia il ciclo di ascolto della ServerSocket.
     */
    public void start() {
        running = true;
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("==================================================");
            System.out.println("  Game Server avviato con successo sulla porta: " + port);
            System.out.println("  In attesa di connessioni dai client...");
            System.out.println("==================================================");

            while (running) {
                try {
                    // Chiamata bloccante: in attesa di un nuovo client
                    Socket clientSocket = serverSocket.accept();

                    // Assegna un idSessione univoco al client connesso
                    int idSessione = sessionCounter.getAndIncrement();

                    System.out.println("[RETE] Client connesso da " 
                            + clientSocket.getRemoteSocketAddress() 
                            + " -> Assegnato ID Sessione: " + idSessione);

                    // Crea il gestore multithread per la connessione del client
                    ClientHandler clientHandler = new ClientHandler(clientSocket, gameEngine, sessionManager, idSessione);

                    // Esegue il ClientHandler in un thread dedicato del ThreadPool
                    threadPool.execute(clientHandler);

                } catch (IOException e) {
                    if (!running) {
                        System.out.println("[RETE] ServerSocket chiusa. Arresto accettazione connessioni.");
                        break;
                    }
                    System.err.println("[ERRORE RETE] Impossibile accettare la connessione del client: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("[ERRORE CRITICO] Impossibile aprire la ServerSocket sulla porta " + port + ": " + e.getMessage());
        } finally {
            stop();
        }
    }

    /**
     * Arresta in modo pulito il server, la ServerSocket e il ThreadPool.
     */
    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("[ERRORE] Chiusura ServerSocket fallita: " + e.getMessage());
        }

        if (threadPool != null && !threadPool.isShutdown()) {
            threadPool.shutdown();
        }
        System.out.println("[SERVER] GameServer arrestato e risorse liberate.");
    }
}