package game.server.controller;

import game.server.db.DatabaseManager;
import game.server.model.GameState;
import game.server.model.Item;
import game.server.model.Stanza;
import game.server.model.TipoItem;
import game.server.network.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GameEngine {

    // =========================================================================
    // STRUTTURE INTERNE PER IL REGISTRO DEI COMANDI
    // =========================================================================

    @FunctionalInterface
    public interface CommandHandler {
        String execute(int idSessione, GameState state, Matcher matcher);
    }

    public static class CommandRule {
        private final Pattern regex;
        private final CommandHandler handler;
        private final boolean richiedePartitaAttiva;

        public CommandRule(String regexPattern, CommandHandler handler, boolean richiedePartitaAttiva) {
            this.regex = Pattern.compile(regexPattern, Pattern.CASE_INSENSITIVE);
            this.handler = handler;
            this.richiedePartitaAttiva = richiedePartitaAttiva;
        }

        public Pattern getRegex() { return regex; }
        public CommandHandler getHandler() { return handler; }
        public boolean isRichiedePartitaAttiva() { return richiedePartitaAttiva; }
    }

    // =========================================================================
    // CAMPI E INIZIALIZZAZIONE
    // =========================================================================

    private final DatabaseManager dbManager;
    private final SessionManager sessionManager;

    private final EnvironmentController environmentController;
    private final combatController combatController;
    private final MovementController movementController;
    private final InventoryController inventoryController;

    private final List<CommandRule> registry = new ArrayList<>();

    public GameEngine(DatabaseManager dbManager, SessionManager sessionManager) {
        this.dbManager = dbManager;
        this.sessionManager = sessionManager;

        this.environmentController = new EnvironmentController();
        this.combatController = new combatController();
        this.movementController = new MovementController(environmentController, combatController);
        this.inventoryController = new InventoryController();

        registraComandi();
    }

    private void registraComandi() {

        // =========================================================================
        // 1. FLUSSO DI AUTENTICAZIONE, RETE E PARTITA (Pre-Game / Menu)
        // =========================================================================

        registry.add(new CommandRule("^si$", 
            (idSess, state, m) -> "INSERISCI IL TUO ID (Formato: id:<tuo_id>)", false));

        registry.add(new CommandRule("^id:\\s*(\\d+)$", 
            (idSess, state, m) -> gestisciIdentificazione(idSess, Integer.parseInt(m.group(1))), false));

        registry.add(new CommandRule("^no$", 
            (idSess, state, m) -> gestisciCreazioneNuovoPlayer(idSess), false));

        registry.add(new CommandRule("^esci\\s+dal\\s+gioch?o$", 
            (idSess, state, m) -> gestisciDisconnessioneGioco(idSess, state), false));

        registry.add(new CommandRule("^nuova\\s+partita$", 
            (idSess, state, m) -> gestisciNuovaPartita(idSess), false));

        registry.add(new CommandRule("^carica\\s+partita$", 
            (idSess, state, m) -> gestisciCaricaPartita(idSess), false));

        registry.add(new CommandRule("^elimina\\s+partita$", 
            (idSess, state, m) -> gestisciEliminaPartita(idSess), false));

        // =========================================================================
        // 2. COMANDI DI GIOCO E INVENTARIO (In-Game)
        // =========================================================================

        // Ricerca oggetti per TipoItem o per Nome
        registry.add(new CommandRule("^cerca\\s+(.+)$", 
            (idSess, state, m) -> gestisciCercaItem(state, m.group(1)), true));

        // Equipaggiamento rapido da inventario (es: GAa2 per arma, GCa2 per consumabile)
        registry.add(new CommandRule("^(GA|GC)[a-zA-Z0-9]+$", 
            (idSess, state, m) -> inventoryController.gestisciEquipaggiamentoDaComando(state, m.group(0)), true));

        // Esamina la stanza attuale e gli oggetti a terra (registrato prima di esamina cella)
        registry.add(new CommandRule("^(?:esamina|guarda)\\s+stanza$|^esamina$", 
            (idSess, state, m) -> gestisciEsaminaStanza(state), true));

        // Esamina dettagli singolo oggetto in uno slot
        registry.add(new CommandRule("^esamina\\s+([GSgs])([a-zA-Z0-9]+)$", 
            (idSess, state, m) -> gestisciEsaminaOggetto(state, m.group(1).toUpperCase().charAt(0), m.group(2)), true));

        // Sposta/scambia oggetti tra celle
        registry.add(new CommandRule("^sposta\\s+([a-zA-Z0-9]+)\\s+in\\s+([a-zA-Z0-9]+)$", 
            (idSess, state, m) -> inventoryController.spostaItem(state, m.group(1), m.group(2)), true));

        // Salvataggio ed Uscita
        registry.add(new CommandRule("^salva\\s+partita$", 
            (idSess, state, m) -> dbManager.salvaGameState(state) ? "Partita salvata con successo!" : "Errore durante il salvataggio nel database.", true));

        registry.add(new CommandRule("^esci\\s+partita$", 
            (idSess, state, m) -> gestisciUscitaPartita(idSess, state), true));

        // =========================================================================
        // 3. COMBATTIMENTO (Con Auto-Save per ricompense Boss)
        // =========================================================================

        registry.add(new CommandRule("^attacc[oa]$", 
            (idSess, state, m) -> gestisciAttaccoCombattimento(state), true));

        registry.add(new CommandRule("^usa\\s+consumabile$", 
            (idSess, state, m) -> gestisciUsoConsumabile(state), true));

        registry.add(new CommandRule("^fuga$", 
            (idSess, state, m) -> combatController.avviaTentativoFuga(state), true));

        // =========================================================================
        // 4. MOVIMENTO
        // =========================================================================

        registry.add(new CommandRule("^(?:vai\\s+a\\s+)?(nord|sud|est|ovest)$", 
            (idSess, state, m) -> movementController.eseguiSpostamento(state, m.group(1)), true));

        // =========================================================================
        // 5. HELPER / MANUALI
        // =========================================================================

        registry.add(new CommandRule("^help$|^aiuto$", 
            (idSess, state, m) -> generaManualeComandi(), false));

        // Visualizzazione Mappa del Livello Corrente
        registry.add(new CommandRule("^(?:vedi\\s+)?mappa$", 
            (idSess, state, m) -> gestisciVediMappa(state), true));
    }

    // =========================================================================
    // METODO PRINCIPALE DI PARSING
    // =========================================================================

    public String processaComando(int idSessione, String inputComando) {
        if (inputComando == null || inputComando.isBlank()) {
            return "Comando vuoto. Digita 'help' per consultare la guida ai comandi.";
        }

        String comando = inputComando.trim();
        GameState state = sessionManager.getSessione(idSessione);

        for (CommandRule rule : registry) {
            Matcher matcher = rule.getRegex().matcher(comando);
            if (matcher.matches()) {
                if (rule.isRichiedePartitaAttiva() && state == null) {
                    return "Devi prima effettuare l'accesso o avviare una partita per usare questo comando.";
                }
                return rule.getHandler().execute(idSessione, state, matcher);
            }
        }

        return "Comando non riconosciuto. Digita 'help' per la lista dei comandi disponibili.";
    }

    // =========================================================================
    // METODI AUSILIARI DI GESTIONE LOGICA
    // =========================================================================

    private String gestisciAttaccoCombattimento(GameState state) {
        String esito = combatController.eseguiAttacco(state);
        // Sincronizza immediatamente il DB dopo l'attacco (salvando la vittoria sul Boss o eventuali variazioni HP)
        dbManager.salvaGameState(state);
        return esito;
    }

    private String gestisciUsoConsumabile(GameState state) {
        String esito = inventoryController.usaConsumabileEquipaggiato(state);
        // Sincronizza il DB dopo l'uso del consumabile
        dbManager.salvaGameState(state);
        return esito;
    }

    private String gestisciEsaminaStanza(GameState state) {
        if (state == null || state.getStanzaAttuale() == null) {
            return "Nessuna stanza attiva da esaminare.";
        }

        Stanza stanza = state.getStanzaAttuale();
        StringBuilder sb = new StringBuilder();
        sb.append("--- STANZA: ").append(stanza.getNome().toUpperCase()).append(" ---\n");
        sb.append(stanza.getDescrizione()).append("\n\n");

        List<Item> oggetti = stanza.getOggettiNellaStanza();
        if (oggetti == null || oggetti.isEmpty()) {
            sb.append("Non ci sono oggetti a terra in questa stanza.");
        } else {
            sb.append("OGGETTI PRESENTI A TERRA:\n");
            for (int i = 0; i < oggetti.size(); i++) {
                Item item = oggetti.get(i);
                if (item != null) {
                    char riga = (char) ('a' + (i / 4));
                    int colonna = (i % 4) + 1;
                    String codSlot = "S" + riga + colonna;

                    sb.append("  - Slot [").append(codSlot).append("]: ").append(item.getNome())
                      .append(" (Tipo: ").append(item.getTipo())
                      .append(", Effetto: ").append(item.getValoreEffetto()).append(")\n");
                }
            }
            sb.append("\n(Usa 'sposta Sa1 in Ga1' per raccogliere l'oggetto nello slot desiderato)");
        }

        return sb.toString();
    }

    private String gestisciVediMappa(GameState state) {
        if (state == null || state.getPlayer() == null || state.getStanzaAttuale() == null) {
            return "Impossibile recuperare la mappa: nessuna partita o stanza attiva.";
        }

        int idLivelloCorrente = state.getStanzaAttuale().getIdLivello();
        String percorsoMappa = dbManager.getPercorsoMappaLivello(idLivelloCorrente);

        if (percorsoMappa == null || percorsoMappa.isBlank()) {
            return "Nessuna mappa trovata per il livello " + idLivelloCorrente + ".";
        }

        return "MAPPA_LIVELLO:" + percorsoMappa;
    }

    private String gestisciCercaItem(GameState state, String parametro) {
        if (state == null || state.getPlayer() == null) {
            return "Nessuna partita attiva.";
        }

        String query = parametro.trim().toUpperCase();
        TipoItem tipoTrovato = null;

        for (TipoItem t : TipoItem.values()) {
            if (t.name().equalsIgnoreCase(query) || 
                (query.startsWith("ARMA") && t == TipoItem.ARMA) ||
                (query.startsWith("POZION") && t == TipoItem.POZIONE) ||
                (query.startsWith("PERGAMEN") && t == TipoItem.PERGAMENA) ||
                (query.startsWith("COLLEZIONABIL") && t == TipoItem.COLLEZIONABILE)) {
                tipoTrovato = t;
                break;
            }
        }

        List<Item> risultati;
        if (tipoTrovato != null) {
            risultati = inventoryController.cercaInInventarioPerTipo(state.getPlayer(), tipoTrovato);
        } else {
            risultati = inventoryController.cercaInInventarioPerNome(state.getPlayer(), parametro);
        }

        if (risultati.isEmpty()) {
            return "Nessun oggetto trovato nell'inventario per: " + parametro;
        }

        StringBuilder sb = new StringBuilder("--- RISULTATI RICERCA INVENTARIO ---\n");
        for (Item item : risultati) {
            if (item != null) {
                sb.append("- ").append(item.getNome())
                  .append(" [Tipo: ").append(item.getTipo()).append("]")
                  .append(" (Effetto: ").append(item.getValoreEffetto()).append(")\n");
            }
        }
        return sb.toString().trim();
    }

    private String gestisciIdentificazione(int idSessione, int idGiocatore) {
        boolean haSalvataggio = dbManager.esisteSalvataggio(idGiocatore);
        sessionManager.associaIdGiocatore(idSessione, idGiocatore);

        StringBuilder sb = new StringBuilder();
        sb.append("Accesso effettuato con ID: ").append(idGiocatore).append("\n");
        if (haSalvataggio) {
            sb.append("Risulta presente una partita salvata.\nComandi disponibili: 'carica partita', 'elimina partita', 'nuova partita'");
        } else {
            sb.append("Nessun salvataggio trovato per questo ID.\nComandi disponibili: 'nuova partita'");
        }
        return sb.toString();
    }

    private String gestisciCreazioneNuovoPlayer(int idSessione) {
        int nuovoId = dbManager.creaNuovoPlayer();
        if (nuovoId <= 0) {
            return "Errore durante la creazione del nuovo profilo utente.";
        }

        try {
            GameState nuovoState = dbManager.creaNuovaPartita(nuovoId);
            sessionManager.registraSessione(idSessione, nuovoState);
            return "Nuovo profilo creato! Il tuo ID è: " + nuovoId + "\nPartita avviata con successo!";
        } catch (Exception e) {
            e.printStackTrace();
            return "Errore nella creazione della nuova partita per il nuovo profilo.";
        }
    }

    private String gestisciNuovaPartita(int idSessione) {
        int idGiocatore = sessionManager.getIdGiocatoreSessione(idSessione);
        if (idGiocatore <= 0) {
            return "Devi prima inserire il tuo ID utente (es. id:123).";
        }
        try {
            GameState nuovoState = dbManager.creaNuovaPartita(idGiocatore);
            sessionManager.registraSessione(idSessione, nuovoState);
            return "Nuova partita creata! Ti trovi in: " + nuovoState.getStanzaAttuale().getNome();
        } catch (Exception e) {
            e.printStackTrace();
            return "Errore durante l'avvio della nuova partita.";
        }
    }

    private String gestisciCaricaPartita(int idSessione) {
        int idGiocatore = sessionManager.getIdGiocatoreSessione(idSessione);
        if (idGiocatore <= 0) {
            return "Devi prima inserire il tuo ID utente (es. id:123).";
        }
        try {
            GameState stateCaricato = dbManager.caricaGameState(idGiocatore);
            sessionManager.registraSessione(idSessione, stateCaricato);
            return "Partita caricata con successo! Ti trovi in: " + stateCaricato.getStanzaAttuale().getNome();
        } catch (Exception e) {
            return "Nessun salvataggio valido trovato per l'ID " + idGiocatore;
        }
    }

    private String gestisciEliminaPartita(int idSessione) {
        int idGiocatore = sessionManager.getIdGiocatoreSessione(idSessione);
        if (idGiocatore <= 0) {
            return "Devi prima inserire il tuo ID utente (es. id:123).";
        }
        boolean eliminato = dbManager.eliminaSalvataggio(idGiocatore);
        if (eliminato) {
            sessionManager.rimuoviSessione(idSessione);
            return "Salvataggio eliminato. Puoi digitare 'nuova partita' per ricominciare.";
        }
        return "Nessuna partita da eliminare trovata per questo ID.";
    }

    private String gestisciDisconnessioneGioco(int idSessione, GameState state) {
        if (state != null) {
            dbManager.salvaGameState(state);
        }
        sessionManager.chiudiSessione(idSessione);
        return "Salvataggio automatico completato. Disconnessione dal server...";
    }

    private String gestisciUscitaPartita(int idSessione, GameState state) {
        dbManager.salvaGameState(state);
        sessionManager.rimuoviSessione(idSessione);
        return "Partita salvata nel database. Tornato al menu principale.\nInserisci 'si' o 'id:<tuo_id>' per continuare.";
    }

    private String gestisciEsaminaOggetto(GameState state, char origine, String coordinate) {
        Item item = inventoryController.ottieniItemDaCoordinata(state, origine, coordinate);
        if (item == null) {
            return "Nessun oggetto presente nella cella specificata (" + origine + coordinate + ").";
        }
        return "--- DETTAGLI OGGETTO ---" +
               "\nNome: " + item.getNome() +
               "\nTipo: " + item.getTipo() +
               "\nValore Effetto: " + item.getValoreEffetto() +
               "\nDescrizione: " + item.getDescrizione();
    }

    private String generaManualeComandi() {
        return """
               =================== GUIDA AI COMANDI ===================
               RETE E ACCESSO:
                 - si / no                   : Gestione iscrizione iniziale
                 - id:<tuo_id>               : Inserisci il tuo ID utente
                 - nuova partita             : Avvia una nuova avventura
                 - carica partita            : Ripristina il salvataggio DB
                 - elimina partita           : Rimuove la partita dal DB
                 - esci dal gioco            : Disconnessione dal server (con auto-save)
               
               PARTITA E INVENTARIO:
                 - cerca <tipo/nome>         : Filtra inventario (es: cerca arma, cerca pozione)
                 - GAa2 / GCa2               : Equipaggia/Disequipaggia arma (GA) o consumabile (GC)
                 - esamina Ga2 (o Sa1)       : Visualizza dettagli e caratteristiche
                 - sposta Ga2 in Sb3         : Sposta/Scambia oggetti tra scacchiere
                 - salva partita             : Aggiorna lo stato nel DB
                 - esci partita              : Salva ed esce al menu principale
               
               COMBATTIMENTO:
                 - attacco                   : Infligge danno con l'arma equipaggiata
                 - usa consumabile           : Utilizza il consumabile equipaggiato
                 - fuga                      : Tenta il disingaggio dalla stanza
               
               ESPLORAZIONE:
                 - vai a nord/sud/est/ovest  : Muove il personaggio nella direzione
                 - esamina stanza            : Mostra descrizione della stanza e oggetti a terra
                 - vedi mappa                : Visualizza la mappa del livello corrente
               =======================================================""";
    }
}