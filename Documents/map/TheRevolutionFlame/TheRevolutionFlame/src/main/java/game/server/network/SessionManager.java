package game.server.network;

import game.server.model.GameState;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gestore centralizzato e thread-safe delle sessioni di gioco attive in RAM.
 * Mantiene l'associazione tra l'ID della sessione socket (idSessione),
 * l'ID univoco del giocatore (idGiocatore) e il GameState corrente.
 */
public class SessionManager {

    // Mappa: idSessione -> GameState (Partita attiva in RAM)
    private final Map<Integer, GameState> sessioniAttive = new ConcurrentHashMap<>();

    // Mappa: idSessione -> idGiocatore (Associazione utente identificato)
    private final Map<Integer, Integer> sessioneGiocatoreMap = new ConcurrentHashMap<>();

    /**
     * Recupera lo stato di gioco (GameState) associato a una sessione.
     *
     * @param idSessione L'ID della sessione socket.
     * @return L'oggetto GameState se c'è una partita attiva, altrimenti null.
     */
    public GameState getSessione(int idSessione) {
        return sessioniAttive.get(idSessione);
    }

    /**
     * Associa l'ID del giocatore autenticato/identificato alla sessione corrente.
     *
     * @param idSessione   L'ID della sessione socket.
     * @param idGiocatore L'ID dell'utente nel database.
     */
    public void associaIdGiocatore(int idSessione, int idGiocatore) {
        sessioneGiocatoreMap.put(idSessione, idGiocatore);
    }

    /**
     * Recupera l'ID del giocatore associato a una sessione.
     *
     * @param idSessione L'ID della sessione socket.
     * @return L'ID del giocatore oppure -1 se l'utente non ha ancora inserito il suo ID.
     */
    public int getIdGiocatoreSessione(int idSessione) {
        return sessioneGiocatoreMap.getOrDefault(idSessione, -1);
    }

    /**
     * Registra/Avvia una partita attiva in RAM per la sessione specificata.
     *
     * @param idSessione L'ID della sessione socket.
     * @param state      Il GameState da associare.
     */
    public void registraSessione(int idSessione, GameState state) {
        if (state != null) {
            sessioniAttive.put(idSessione, state);
            // Assicura la sincronizzazione anche con l'ID del giocatore presente nel GameState
            sessioneGiocatoreMap.put(idSessione, state.getIdGiocatore());
        }
    }

    /**
     * Rimuove solo la partita attiva (GameState) dalla RAM per la sessione specificata
     * (es. quando il giocatore torna al menu principale senza disconnettersi).
     *
     * @param idSessione L'ID della sessione socket.
     */
    public void rimuoviSessione(int idSessione) {
        sessioniAttive.remove(idSessione);
    }

    /**
     * Pulisce completamente tutti i dati di sessione (GameState e idGiocatore)
     * quando un client si disconnette dal server.
     *
     * @param idSessione L'ID della sessione socket.
     */
    public void chiudiSessione(int idSessione) {
        sessioniAttive.remove(idSessione);
        sessioneGiocatoreMap.remove(idSessione);
    }

    /**
     * Verifica se una sessione ha una partita attiva in corso.
     *
     * @param idSessione L'ID della sessione socket.
     * @return true se c'è un GameState attivo in RAM, false altrimenti.
     */
    public boolean haSessioneAttiva(int idSessione) {
        return sessioniAttive.containsKey(idSessione);
    }
}