package game.server.controller;

import game.server.model.GameState;
import game.server.model.Item;
import game.server.model.Stanza;

import java.util.List;

/**
 * Gestisce la logica di navigazione tra le stanze, integrando i controlli di:
 * - Validazione direzioni e adiacenze
 * - Sblocco automatico delle porte tramite lista di oggetti dell'inventario
 * - Disingaggio / Fuga da scontro (via CombatController)
 * - Attivazione/Disattivazione danni ambientali (via EnvironmentController)
 * - Innesco di imboscate/combattimenti all'ingresso nelle stanze
 */
public class MovementController {

    private final EnvironmentController environmentController;
    private final combatController combatController;

    public MovementController(EnvironmentController environmentController, combatController combatController) {
        this.environmentController = environmentController;
        this.combatController = combatController;
    }

    /**
     * Esegue lo spostamento del giocatore verso una direzione specificata.
     *
     * @param state     Lo stato di gioco attivo della sessione.
     * @param direzione Direzione inviata dal comando client (es. "nord", "sud", "est", "ovest").
     * @return Testo di risposta formattato da inviare al client.
     */
    public String eseguiSpostamento(GameState state, String direzione) {
        if (state == null || state.getPlayer() == null) {
            return "Errore: Stato di gioco non valido.";
        }

        if (direzione == null || direzione.isBlank()) {
            return "Specificare una direzione verso cui muoversi (es. nord, sud, est, ovest).";
        }

        String dir = direzione.toLowerCase().trim();

        // 1. GESTIONE STATO DI COMBATTIMENTO E FUGA
        if (state.isInCombattimento()) {
            if (combatController != null && combatController.isInAttesaFuga(state.getIdGiocatore())) {
                // Interruzione del danno ambientale della stanza corrente prima di completare la fuga
                if (environmentController != null) {
                    environmentController.interrompiEffettiAmbientali();
                }

                // Esecuzione dello spostamento di fuga tramite CombatController
                String esitoFuga = combatController.confermaDirezioneFuga(state, dir);

                // Se la fuga si è conclusa con successo (non è più in combattimento), applica eventuali effetti della nuova stanza
                if (!state.isInCombattimento() && environmentController != null) {
                    environmentController.applicaDannoPeriodico(state);
                }
                return esitoFuga;
            } else {
                return "Non puoi muoverti liberamente mentre sei in combattimento! Tenta prima la fuga.";
            }
        }

        // 2. VALIDAZIONE STANZA E DIREZIONE
        Stanza stanzaAttuale = state.getStanzaAttuale();
        if (stanzaAttuale == null) {
            return "Errore: Stanza attuale non definita.";
        }

        Stanza destinazione = stanzaAttuale.getAdiacenza(dir);
        if (destinazione == null) {
            return "Non c'è alcun passaggio verso " + dir.toUpperCase() + ".";
        }

        StringBuilder rispostaPrefix = new StringBuilder();

        // 3. GESTIONE PORTE BLOCCATE E SBLOCCO TRAMITE LISTA DI OGGETTI NELL'INVENTARIO
        if (destinazione.isBloccata()) {
            List<Item> richiesti = destinazione.getOggettiRichiestiSblocco();

            if (richiesti != null && !richiesti.isEmpty()) {
                List<Item> inventarioGiocatore = state.getPlayer().getInventario();

                // Individua gli oggetti richiesti che NON sono presenti nell'inventario del giocatore
                List<Item> mancanti = richiesti.stream()
                        .filter(req -> req != null && (inventarioGiocatore == null || inventarioGiocatore.stream().noneMatch(inv -> 
                            inv != null && (
                                (inv.getId() != 0 && inv.getId() == req.getId()) ||
                                (inv.getNome() != null && req.getNome() != null && inv.getNome().trim().equalsIgnoreCase(req.getNome().trim()))
                            )
                        )))
                        .toList();

                if (mancanti.isEmpty()) {
                    // Sblocca la stanza
                    destinazione.setBloccata(false);

                    // Consuma/Rimuove gli oggetti usati dall'inventario del giocatore
                    for (Item req : richiesti) {
                        state.getPlayer().rimuoviItem(req);
                    }

                    rispostaPrefix.append("[SBLOCCO] Hai usato gli oggetti necessari per sbloccare il passaggio verso ")
                                  .append(dir.toUpperCase()).append("!\n\n");
                } else {
                    List<String> nomiMancanti = mancanti.stream().map(Item::getNome).toList();
                    return "La via verso " + dir.toUpperCase() + " è bloccata! Ti mancano i seguenti oggetti: " 
                            + String.join(", ", nomiMancanti) + ".";
                }
            } else {
                return "La via verso " + dir.toUpperCase() + " è bloccata! Devi trovare un modo per sbloccarla.";
            }
        }

        // 4. INTERRUZIONE DANNI AMBIENTALI STANZA PRECEDENTE
        if (environmentController != null) {
            environmentController.interrompiEffettiAmbientali();
        }

        // 5. AGGIORNAMENTO POSIZIONE GIOCATORE
        state.setStanzaAttuale(destinazione);

        StringBuilder risposta = new StringBuilder();
        risposta.append(rispostaPrefix)
                .append("Ti sposti verso ").append(dir.toUpperCase()).append(".\n")
                .append("Sei entrato in: ").append(destinazione.getNome()).append("\n")
                .append(destinazione.getDescrizione());

        // 6. VERIFICA PRESENZA NEMICI O DANNO AMBIENTALE NELLA NUOVA STANZA
        if (destinazione.haNemicoVivo()) {
            if (combatController != null) {
                String avvioScontroMsg = combatController.avviaScontro(state);
                risposta.append("\n\n").append(avvioScontroMsg);
            } else {
                state.setInCombattimento(true);
                risposta.append("\n\nUn nemico presidia la stanza e ti attacca!");
            }
        } else {
            // Se la stanza è libera da nemici ed è pericolosa, attiva il timer di danno ambientale
            if (environmentController != null) {
                environmentController.applicaDannoPeriodico(state);
            }
        }

        return risposta.toString();
    }
}