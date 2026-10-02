
package game.server.model;

import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

/**
 * Incapsula l'intero stato di una sessione di gioco attiva per un giocatore.
 */
public class GameState {
    private final int idGameState;
    private Giocatore player;
    private Stanza stanzaAttuale;
    private boolean inCombattimento;
    private Map<Integer, Stanza> mappaStanzeIstanziata;
    
    // Gestione danni ambientali asincroni
    private ScheduledExecutorService schedulerAmbientale;
    private ScheduledFuture<?> dannoTaskAttivo;

    public GameState(int idGameState, Giocatore player, Stanza stanzaAttuale, Map<Integer, Stanza> mappaStanzeIstanziata) {
        this.idGameState = idGameState;
        this.player = player;
        this.stanzaAttuale = stanzaAttuale;
        this.mappaStanzeIstanziata = mappaStanzeIstanziata;
        this.inCombattimento = false;
    }

   
    public Nemico getNemicoInScontro() {
        if (inCombattimento && stanzaAttuale != null) {
            return stanzaAttuale.getNemicoPresente();
        }
        return null;
    }

    // Getters e Setters
    public int getIdGiocatore() { return idGameState; }

    public Giocatore getPlayer() { return player; }
    public void setPlayer(Giocatore player) { this.player = player; }

    public Stanza getStanzaAttuale() { return stanzaAttuale; }
    public void setStanzaAttuale(Stanza stanzaAttuale) { this.stanzaAttuale = stanzaAttuale; }

    public boolean isInCombattimento() { return inCombattimento; }
    public void setInCombattimento(boolean inCombattimento) { this.inCombattimento = inCombattimento; }

    public Map<Integer, Stanza> getMappaStanzeIstanziata() { return mappaStanzeIstanziata; }
    public void setMappaStanzeIstanziata(Map<Integer, Stanza> mappaStanzeIstanziata) { this.mappaStanzeIstanziata = mappaStanzeIstanziata; }

    public ScheduledExecutorService getSchedulerAmbientale() { return schedulerAmbientale; }
    public void setSchedulerAmbientale(ScheduledExecutorService schedulerAmbientale) { this.schedulerAmbientale = schedulerAmbientale; }

    public ScheduledFuture<?> getDannoTaskAttivo() { return dannoTaskAttivo; }
    public void setDannoTaskAttivo(ScheduledFuture<?> dannoTaskAttivo) { this.dannoTaskAttivo = dannoTaskAttivo; }
}

