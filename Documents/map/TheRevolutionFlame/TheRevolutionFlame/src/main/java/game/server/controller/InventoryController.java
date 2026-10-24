package game.server.controller;

import game.server.model.GameState;
import game.server.model.Giocatore;
import game.server.model.Item;
import game.server.model.Stanza;
import game.server.model.TargetEffetto;
import game.server.model.TipoItem;

import java.util.List;
import java.util.stream.Collectors;

public class InventoryController {

    // Griglia 3 righe x 4 colonne = 12 slot totali
    private static final int COLS = 4;
    private static final int ROWS = 3;
    private static final int CAPACITA_MASSIMA = COLS * ROWS; // 12

    /**
     * Esegue lo spostamento di un oggetto tra griglie (Giocatore/Stanza).
     * @param state Stato attuale del gioco
     * @param sorgente Es: "Gb2" (Giocatore, riga b, colonna 2) o "Sb2" (Stanza)
     * @param destinazione Es: "Gc3" o "Sc3"
     * @return Messaggio di risposta da restituire al client
     */
    public String spostaItem(GameState state, String sorgente, String destinazione) {
        if (sorgente == null || destinazione == null || sorgente.length() < 3 || destinazione.length() < 3) {
            return "Formato comando non valido. Es: sposta Gb2 in Sc3";
        }

        sorgente = sorgente.trim();
        destinazione = destinazione.trim();

        char tipoOrigine = Character.toUpperCase(sorgente.charAt(0));
        char tipoDestinazione = Character.toUpperCase(destinazione.charAt(0));

        int idxOrigine = calcolaIndice(sorgente.substring(1));
        int idxDestinazione = calcolaIndice(destinazione.substring(1));

        if (idxOrigine < 0 || idxDestinazione < 0) {
            return "Coordinate slot non valide (usare righe a-c e colonne 1-4).";
        }

        Giocatore player = state.getPlayer();
        Stanza stanza = state.getStanzaAttuale();

        List<Item> listaOrigine = (tipoOrigine == 'G') ? player.getInventario() : stanza.getOggettiNellaStanza();
        List<Item> listaDestinazione = (tipoDestinazione == 'G') ? player.getInventario() : stanza.getOggettiNellaStanza();

        // Controllo validità indice di partenza
        if (idxOrigine >= listaOrigine.size()) {
            return "Nessun oggetto presente nello slot di partenza " + sorgente;
        }

        Item itemSpostato = listaOrigine.get(idxOrigine);

        // Caso 1: Spostamento/Scambio nello stesso inventario (es. Gb2 -> Gc3 o Sb2 -> Sc3)
        if (tipoOrigine == tipoDestinazione) {
            gestisciSpostamentoInterno(listaOrigine, idxOrigine, idxDestinazione);
            return "Spostato " + itemSpostato.getNome() + " in " + destinazione;
        } 
        
        // Caso 2: Trasferimento tra inventari diversi (G -> S oppure S -> G)
        return gestisciTrasferimento(listaOrigine, listaDestinazione, idxOrigine, idxDestinazione, itemSpostato, tipoOrigine, destinazione);
    }

    private void gestisciSpostamentoInterno(List<Item> lista, int idxOrigine, int idxDestinazione) {
        // Se la destinazione supera la dimensione attuale della lista, aggiungiamo in coda
        if (idxDestinazione >= lista.size()) {
            Item item = lista.remove(idxOrigine);
            lista.add(item);
        } else {
            // Scambio di due oggetti (Swap)
            Item temp = lista.get(idxOrigine);
            lista.set(idxOrigine, lista.get(idxDestinazione));
            lista.set(idxDestinazione, temp);
        }
    }

    private String gestisciTrasferimento(List<Item> sorgente, List<Item> destinazione, 
                                        int idxOrigine, int idxDestinazione, 
                                        Item itemSpostato, char tipoOrigine, String slotDest) {
        if (idxDestinazione < destinazione.size()) {
            // Se lo slot contiene già un oggetto, avviene uno scambio
            sorgente.remove(idxOrigine);
            Item itemRimpiazzato = destinazione.set(idxDestinazione, itemSpostato);
            sorgente.add(idxOrigine, itemRimpiazzato);
            return "Scambiato " + itemSpostato.getNome() + " con " + itemRimpiazzato.getNome();
        } else {
            // Verifica capienza massima prima dell'inserimento
            if (destinazione.size() >= CAPACITA_MASSIMA) {
                String bersaglio = (tipoOrigine == 'G') ? "della stanza" : "del giocatore";
                return "Impossibile inserire: l'inventario " + bersaglio + " è pieno (max " + CAPACITA_MASSIMA + " oggetti).";
            }
            sorgente.remove(idxOrigine);
            destinazione.add(itemSpostato);
            String azione = (tipoOrigine == 'G') ? "Lasciato " : "Raccolto ";
            return azione + itemSpostato.getNome() + " nello slot " + slotDest;
        }
    }

    /**
     * Converte "b2" o "2b" -> riga 'b' (1), colonna '2' (1) -> (1 * COLS) + 1 = 5
     * Limita le righe a 'a', 'b', 'c' (0, 1, 2) per un totale di 12 slot.
     */
    private int calcolaIndice(String coord) {
        try {
            coord = coord.trim();
            char rigaChar;
            int colonnaNum;

            if (Character.isLetter(coord.charAt(0))) {
                rigaChar = Character.toLowerCase(coord.charAt(0));
                colonnaNum = Integer.parseInt(coord.substring(1)) - 1;
            } else {
                rigaChar = Character.toLowerCase(coord.charAt(coord.length() - 1));
                colonnaNum = Integer.parseInt(coord.substring(0, coord.length() - 1)) - 1;
            }

            int riga = rigaChar - 'a'; // 'a' -> 0, 'b' -> 1, 'c' -> 2

            // Controllo strictly entro la griglia 3x4 (righe 0..2, colonne 0..3)
            if (riga < 0 || riga >= ROWS || colonnaNum < 0 || colonnaNum >= COLS) {
                return -1;
            }

            return (riga * COLS) + colonnaNum;
        } catch (Exception e) {
            return -1;
        }
    }

    // =========================================================================
    // METODI DI RICERCA CON LAMBDA EXPRESSIONS & STREAM API
    // =========================================================================

    public List<Item> cercaInInventarioPerTipo(Giocatore player, TipoItem tipo) {
        return player.getInventario().stream()
                .filter(item -> item.getTipo() == tipo)
                .collect(Collectors.toList());
    }

    public List<Item> cercaInStanzaPerTipo(Stanza stanza, TipoItem tipo) {
        return stanza.getOggettiNellaStanza().stream()
                .filter(item -> item.getTipo() == tipo)
                .collect(Collectors.toList());
    }

    public List<Item> cercaInInventarioPerNome(Giocatore player, String nome) {
        return player.getInventario().stream()
                .filter(item -> item.getNome().toLowerCase().contains(nome.toLowerCase()))
                .collect(Collectors.toList());
    }

    // =========================================================================
    // METODI DI EQUIPAGGIAMENTO
    // =========================================================================

    public boolean equipaggiaArma(Giocatore player, String coordinate) {
        if (player == null || coordinate == null) {
            return false;
        }
        int index = calcolaIndice(coordinate);
        List<Item> inventario = player.getInventario();

        if (index >= 0 && index < inventario.size()) {
            Item item = inventario.get(index);
            player.setArmaEquipaggiata(item);
            return true;
        }
        return false;
    }

    public boolean equipaggiaConsumabile(Giocatore player, String coordinate) {
        if (player == null || coordinate == null) {
            return false;
        }
        int index = calcolaIndice(coordinate);
        List<Item> inventario = player.getInventario();

        if (index >= 0 && index < inventario.size()) {
            Item item = inventario.get(index);
            player.setConsumabileEquipaggiato(item);
            return true;
        }
        return false;
    }

    public void disequipaggiaArma(Giocatore player) {
        if (player != null) {
            player.setArmaEquipaggiata(null);
        }
    }

    public void disequipaggiaConsumabile(Giocatore player) {
        if (player != null) {
            player.setConsumabileEquipaggiato(null);
        }
    }

    public String gestisciEquipaggiamentoDaComando(GameState state, String comando) {
        if (state == null || state.getPlayer() == null || comando == null || comando.length() < 3) {
            return "Comando di equipaggiamento non valido.";
        }

        comando = comando.trim();
        char sorgente = Character.toUpperCase(comando.charAt(0));
        char tipoEquip = Character.toUpperCase(comando.charAt(1));

        if (sorgente != 'G' || (tipoEquip != 'A' && tipoEquip != 'C')) {
            return "Formato non valido. Usa 'GA' per arma o 'GC' per consumabile (es: GAa2, GCa2).";
        }

        Giocatore player = state.getPlayer();
        String coord = comando.substring(2);
        int index = calcolaIndice(coord);

        if (index < 0 || index >= player.getInventario().size()) {
            return "Nessun oggetto trovato nelle coordinate specificate.";
        }

        Item item = player.getInventario().get(index);

        if (tipoEquip == 'A') {
            if (player.getArmaEquipaggiata() == item) {
                player.setArmaEquipaggiata(null);
                return "Arma disequipaggiata: " + item.getNome();
            } else {
                player.setArmaEquipaggiata(item);
                return "Equipaggiata arma: " + item.getNome();
            }
        } else {
            if (player.getConsumabileEquipaggiato() == item) {
                player.setConsumabileEquipaggiato(null);
                return "Consumabile disequipaggiato: " + item.getNome();
            } else {
                player.setConsumabileEquipaggiato(item);
                return "Equipaggiato consumabile: " + item.getNome();
            }
        }
    }

    // =========================================================================
    // METODO PER USO CONSUMABILE EQUIPAGGIATO
    // =========================================================================

    public String usaConsumabileEquipaggiato(GameState state) {
        if (state == null || state.getPlayer() == null) {
            return "Stato di gioco o giocatore non valido.";
        }

        Giocatore player = state.getPlayer();
        Item consumabile = player.getConsumabileEquipaggiato();

        if (consumabile == null) {
            return "Nessun consumabile equipaggiato!";
        }

        if (consumabile.getTipo() != TipoItem.POZIONE) {
            return "L'oggetto equipaggiato non è una pozione utilizzabile!";
        }

        TargetEffetto target = consumabile.getTargetEffetto();
        if (target == null) {
            return "Questo consumabile non ha un effetto valido impostato.";
        }

        int valoreEffetto = consumabile.getValoreEffetto();
        String messaggioEsito;

        switch (target) {
            case SALUTE_GIOCATORE -> {
                player.modificaHp(valoreEffetto);
                messaggioEsito = "Hai utilizzato " + consumabile.getNome() + "! Ripristinati " + valoreEffetto + " HP.";
            }
            case DANNO_ARMA -> {
                Item arma = player.getArmaEquipaggiata();
                if (arma == null) {
                    return "Non hai un'arma equipaggiata da potenziare!";
                }
                int nuovoDanno = arma.getValoreEffetto() + valoreEffetto;
                arma.setValoreEffetto(nuovoDanno);
                messaggioEsito = "Hai utilizzato " + consumabile.getNome() + "! Il danno di " + arma.getNome() 
                        + " è aumentato di +" + valoreEffetto + " (Danno totale: " + nuovoDanno + ").";
            }
            default -> {
                return "Questo oggetto non produce alcun effetto applicabile.";
            }
        }

        player.setConsumabileEquipaggiato(null);
        player.rimuoviItem(consumabile);

        return messaggioEsito;
    }

    public Item ottieniItemDaCoordinata(GameState state, char origine, String coordinate) {
        if (state == null || coordinate == null) {
            return null;
        }

        int index = calcolaIndice(coordinate);
        if (index < 0) {
            return null;
        }

        List<Item> listaTarget;
        if (Character.toUpperCase(origine) == 'G' && state.getPlayer() != null) {
            listaTarget = state.getPlayer().getInventario();
        } else if (Character.toUpperCase(origine) == 'S' && state.getStanzaAttuale() != null) {
            listaTarget = state.getStanzaAttuale().getOggettiNellaStanza();
        } else {
            return null;
        }

        if (index < listaTarget.size()) {
            return listaTarget.get(index);
        }

        return null;
    }

}