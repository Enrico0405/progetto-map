package game.server.network;

import com.fasterxml.jackson.databind.ObjectMapper;
import game.common.dto.CommandRequest;
import game.common.dto.GameResponse;
import game.common.dto.ItemDto;
import game.server.controller.GameEngine;
import game.server.db.DatabaseManager;
import game.server.model.GameState;
import game.server.model.Item;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Gestore multithread della singola connessione socket con un client.
 * Ogni client connesso esegue in un proprio thread separato e isolato.
 */
public class ClientHandler implements Runnable {

    private final Socket socket;
    private final GameEngine gameEngine;
    private final SessionManager sessionManager;
    private final DatabaseManager dbManager;
    private final int idSessione;
    private final ObjectMapper objectMapper;

    public ClientHandler(Socket socket, GameEngine gameEngine, SessionManager sessionManager, DatabaseManager dbManager, int idSessione) {
        this.socket = socket;
        this.gameEngine = gameEngine;
        this.sessionManager = sessionManager;
        this.dbManager = dbManager;
        this.idSessione = idSessione;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void run() {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            String jsonInput;

            // Loop di ascolto comandi dal socket del client
            while ((jsonInput = in.readLine()) != null) {
                if (jsonInput.isBlank()) continue;

                // 1. Deserializzazione JSON -> CommandRequest
                CommandRequest request;
                try {
                    request = objectMapper.readValue(jsonInput, CommandRequest.class);
                } catch (Exception e) {
                    GameResponse errResp = new GameResponse("Formato comando JSON non valido.");
                    errResp.setSuccesso(false);
                    out.println(objectMapper.writeValueAsString(errResp));
                    continue;
                }

                String comandoStr = request.getComandoCompleto();

                // 2. Invocazione della logica di gioco nel GameEngine
                String messaggioRisposta = gameEngine.processaComando(idSessione, comandoStr);

                // 3. Recupero dello stato aggiornato in RAM per questa sessione
                GameState statoCorrente = sessionManager.getSessione(idSessione);

                // 4. Arricchimento della risposta con lo stato di salute, stanza, blocco, combattimento e INVENTARI
                GameResponse response = generaRispostaCompleta(messaggioRisposta, statoCorrente);

                // 5. Serializzazione GameResponse -> JSON e invio al client
                String jsonOutput = objectMapper.writeValueAsString(response);
                out.println(jsonOutput);

                // Gestione della disconnessione pulita impartita dal comando
                if ("esci dal gioco".equalsIgnoreCase(comandoStr)) {
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Connessione interrotta dal client (ID Sessione: " + idSessione + ")");
        } finally {
            // Chiusura pulita della sessione e salvataggio/pulizia risorse
            sessionManager.chiudiSessione(idSessione);
            chiudiSocket();
        }
    }

    /**
     * Mappa i dati dello stato di gioco (GameState) dentro il DTO GameResponse,
     * includendo anche la sincronizzazione degli oggetti dell'inventario e della stanza.
     */
    private GameResponse generaRispostaCompleta(String messaggio, GameState state) {
        GameResponse response = new GameResponse(messaggio);
        response.setIdGiocatore(sessionManager.getIdGiocatoreSessione(idSessione));

        if (state != null) {
            // Dati Giocatore e verifica Game Over
            if (state.getPlayer() != null) {
                response.setPlayerHp(state.getPlayer().getHp());
                response.setPlayerMaxHp(state.getPlayer().getMaxHp());
                if (state.getPlayer().getHp() <= 0) {
                    response.setGameOver(true);
                }

                // Sincronizzazione Inventario Giocatore
                if (state.getPlayer().getInventario() != null) {
                    List<ItemDto> invDto = state.getPlayer().getInventario().stream()
                            .map(this::mappaItemToDto)
                            .collect(Collectors.toList());
                    response.setInventarioGiocatore(invDto);
                }

                // Sincronizzazione Equipaggiamento
                if (state.getPlayer().getArmaEquipaggiata() != null) {
                    response.setCurrentWeapon(state.getPlayer().getArmaEquipaggiata().getNome());
                }
                if (state.getPlayer().getConsumabileEquipaggiato() != null) {
                    response.setCurrentConsumable(state.getPlayer().getConsumabileEquipaggiato().getNome());
                }
            }

            // Dati Stanza, Blocco, Mappa Livello e Oggetti a Terra
            if (state.getStanzaAttuale() != null) {
                response.setNomeStanza(state.getStanzaAttuale().getNome());
                response.setDescrizioneStanza(state.getStanzaAttuale().getDescrizione());
                response.setStanzaSfondoPath(state.getStanzaAttuale().getImmagineSfondoPath());
                response.setStanzaBloccata(state.getStanzaAttuale().isBloccata());

                int idLivello = state.getStanzaAttuale().getIdLivello();
                response.setMappaPngPath(dbManager.getPercorsoMappaLivello(idLivello));

                // Sincronizzazione Oggetti Presenti nella Stanza
                if (state.getStanzaAttuale().getOggettiNellaStanza() != null) {
                    List<ItemDto> stanzaDto = state.getStanzaAttuale().getOggettiNellaStanza().stream()
                            .map(this::mappaItemToDto)
                            .collect(Collectors.toList());
                    response.setOggettiStanza(stanzaDto);
                }
            }

            // Dati Combattimento e Nemico
            response.setInCombattimento(state.isInCombattimento());
            if (state.isInCombattimento() && state.getNemicoInScontro() != null) {
                response.setEnemyHp(state.getNemicoInScontro().getHp());
                response.setEnemyMaxHp(state.getNemicoInScontro().getMaxHp());
                response.setEnemySpritePath(state.getNemicoInScontro().getSpritePath());
            }
        }

        return response;
    }

    private ItemDto mappaItemToDto(Item item) {
        if (item == null) return null;
        ItemDto dto = new ItemDto();
        dto.setNome(item.getNome());
        dto.setImmaginePath(item.getIconaPath());
        return dto;
    }

    private void chiudiSocket() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}