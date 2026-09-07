package game.client.network;

import com.fasterxml.jackson.databind.ObjectMapper;
import game.common.dto.CommandRequest;
import game.common.dto.GameResponse;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class NetworkClient {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private ObjectMapper mapper;

    public NetworkClient() {
        this.mapper = new ObjectMapper();
    }

    public boolean connetti(String ip, int porta) {
        try {
            this.socket = new Socket(ip, porta);
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public GameResponse inviaComando(CommandRequest richiesta) {
        try {
            // serializza l'oggetto richiesta in stringa json
            String jsonRichiesta = mapper.writeValueAsString(richiesta);
            out.println(jsonRichiesta);

            // legge la risposta json inviata dal server
            String jsonRisposta = in.readLine();
            if (jsonRisposta != null) {
                // deserializza la stringa json in oggetto GameResponse
                return mapper.readValue(jsonRisposta, GameResponse.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void disconnetti() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}