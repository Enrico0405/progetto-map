package game.client.network;

import com.fasterxml.jackson.databind.ObjectMapper;
import game.common.dto.CommandRequest;
import game.common.dto.GameResponse;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NetworkClient {

    private String serverAddress;
    private int serverPort;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private ObjectMapper mapper;
    private boolean connesso;

    /*

    executor a thread singolo per gestire la coda delle richieste di rete in modo sequenziale
    evita il sovraccarico dovuto alla continua creazione di thread e previene collisioni sul socket tcp

    */
    private final ExecutorService networkQueue = Executors.newSingleThreadExecutor();

    public NetworkClient(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.mapper = new ObjectMapper();
        this.connesso = false;
    }

    public boolean connetti() {
        try {
            this.socket = new Socket(serverAddress, serverPort);
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.connesso = true;
            return true;
        } catch (Exception e) {
            this.connesso = false;
            return false;
        }
    }

    public void disconnetti() {
        try {
            connesso = false;
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
            
            /*

            arresta immediatamente l'executor liberando il thread in background alla chiusura

            */
            networkQueue.shutdownNow();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void inviaComandoAsincrono(CommandRequest request, ResponseCallback callback) {
        networkQueue.submit(() -> {
            try {
                if (!connesso || socket == null || socket.isClosed()) {
                    if (callback != null) {
                        callback.onError("Client non connesso al server.");
                    }
                    return;
                }

                /*

                serializzazione dell'oggetto di richiesta in stringa formato json
                e invio sullo stream di output

                */
                String jsonRichiesta = mapper.writeValueAsString(request);
                out.println(jsonRichiesta);

                /*

                attesa e lettura della linea di risposta inviata dal server

                */
                String jsonRisposta = in.readLine();
                if (jsonRisposta != null) {
                    /*

                    deserializzazione della stringa json nell'oggetto gameresponse
                    e notifica della callback

                    */
                    GameResponse response = mapper.readValue(jsonRisposta, GameResponse.class);
                    if (callback != null) {
                        callback.onResponse(response);
                    }
                } else {
                    if (callback != null) {
                        callback.onError("Connessione interrotta dal server.");
                    }
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError("Errore di comunicazione: " + e.getMessage());
                }
            }
        });
    }

    public boolean isConnesso() {
        return this.connesso;
    }
}