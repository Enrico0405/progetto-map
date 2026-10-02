package game.server.controller;

import game.server.model.GameState;
import game.server.model.Giocatore;
import game.server.model.Stanza;

/**
 * Gestisce la logica degli effetti ambientali (danno da gas/stanza letale)
 * avviando e interrompendo thread temporizzati per ciascuna sessione di gioco.
 */
public class EnviromentController {

    private Thread threadDannoAttivo;

    /**
     * Valuta lo stato della stanza e del giocatore. Se la stanza è pericolosa,
     * non ci sono combattimenti in corso e il giocatore è vivo, avvia un thread
     * separato che sottrae HP ogni due secondi.
     *
     * @param state Lo stato di gioco della sessione corrente.
     */
    public synchronized void applicaDannoPeriodico(GameState state) {
        // Interrompe prima un eventuale timer già in esecuzione
        interrompiEffettiAmbientali();

        Stanza stanza = state.getStanzaAttuale();
        Giocatore player = state.getPlayer();

        // Condizioni per l'avvio del timer: stanza letale, non in combattimento e player vivo
        if (stanza != null && stanza.isPericolosa() && !state.isInCombattimento() && player.isVivo()) {
            
            Runnable taskDanno = new Runnable() {
                @Override
                public void run() {
                    while (!Thread.currentThread().isInterrupted()) {
                        try {
                            // Attende 2 secondi (2000 ms) prima di infliggere il danno[cite: 1]
                            Thread.sleep(2000);

                            synchronized (state) {
                                Stanza stanzaCorrente = state.getStanzaAttuale();
                                Giocatore giocatore = state.getPlayer();

                                // Condizioni di arresto automatico del timer:
                                // 1. Il giocatore non si trova più in una stanza pericolosa[cite: 2]
                                // 2. È iniziato un combattimento[cite: 6]
                                // 3. Il giocatore non è più in vita[cite: 5]
                                if (stanzaCorrente == null || !stanzaCorrente.isPericolosa() 
                                        || state.isInCombattimento() || !giocatore.isVivo()) {
                                    break;
                                }

                                // Sottrarre gli HP al giocatore[cite: 2, 5]
                                int danno = stanzaCorrente.getStanza_letale_danno();
                                giocatore.modificaHp(-danno);

                                if (!giocatore.isVivo()) {
                                    break;
                                }
                            }

                        } catch (InterruptedException e) {
                            // Gestione dell'interruzione pulita del thread senza stampe a console[cite: 1]
                            Thread.currentThread().interrupt(); // Ripristina lo stato di interruzione
                            break;
                        }
                    }
                }
            };

            // Istanziamento ed avvio del thread separato per il timer[cite: 1]
            threadDannoAttivo = new Thread(taskDanno, "TimerDannoAmbientale-Player-" + state.getIdGiocatore());
            threadDannoAttivo.start();
        }
    }

    /**
     * Interrompe ed annulla il thread di danno se attualmente in esecuzione.
     * Da invocare quando il giocatore si sposta, entra in combattimento o disconnette la sessione.
     */
    public synchronized void interrompiEffettiAmbientali() {
        if (threadDannoAttivo != null && threadDannoAttivo.isAlive()) {
            threadDannoAttivo.interrupt(); // Invia il segnale di interruzione al thread[cite: 1]
            threadDannoAttivo = null;
        }
    }
}