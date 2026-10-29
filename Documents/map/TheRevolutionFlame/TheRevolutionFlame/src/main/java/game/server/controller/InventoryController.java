package game.server.controller;

import game.server.model.GameState;
import game.server.model.Giocatore;
import game.server.model.Item;
import game.server.model.Stanza;
import game.server.model.TipoItem;
import game.server.model.TargetEffetto;

import java.util.List;
import java.util.stream.Collectors;

public class InventoryController {

    // Impostiamo il numero di colonne per la griglia di Swing (es. 4 colonne per riga)
    private static final int COLS = 4;

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
            return "Coordinate slot non valide.";
        }

        Giocatore player = state.getPlayer();
        Stanza stanza = state.getStanzaAttuale();

        List<Item> listaOrigine = (tipoOrigine == 'G') ? player.getInventario() : stanza.getOggettiNellaStanza();
        List<Item> listaDestinazione = (tipoDestinazione == 'G') ? player.getInventario() : stanza.getOggettiNellaStanza();

        // Controllo validità indice di partenza e presenza effettiva dell'oggetto
        if (idxOrigine >= listaOrigine.size() || listaOrigine.get(idxOrigine) == null) {
            return "Nessun oggetto presente nello slot di partenza " + sorgente;
        }

        Item itemSpostato = listaOrigine.get(idxOrigine);

        // =====================================================================
        // CONTROLLO COMBATTIMENTO: Impedisce lo spostamento esterno (G <-> S)
        // =====================================================================
        if (state.isInCombattimento() && tipoOrigine != tipoDestinazione) {
            return "Non puoi raccogliere o lasciare oggetti mentre sei in combattimento!";
        }

        // Caso 1: Spostamento/Scambio nello stesso inventario
        if (tipoOrigine == tipoDestinazione) {
            gestisciSpostamentoInterno(listaOrigine, idxOrigine, idxDestinazione);
            return "Spostato " + itemSpostato.getNome() + " in " + destinazione;
        } 
        
        // Caso 2: Trasferimento tra inventari diversi (G -> S oppure S -> G)
        return gestisciTrasferimento(player, listaOrigine, listaDestinazione, idxOrigine, idxDestinazione, itemSpostato, tipoOrigine, destinazione);
    }

    private void gestisciSpostamentoInterno(List<Item> lista, int idxOrigine, int idxDestinazione) {
        // Riempie la lista con null se la destinazione supera la dimensione attuale
        while (lista.size() <= idxDestinazione) {
            lista.add(null);
        }

        Item itemDestinazioneEsistente = lista.get(idxDestinazione);
        
        // Effettua lo scambio (Swap) esatto tra lo slot di origine e quello di destinazione
        lista.set(idxDestinazione, lista.get(idxOrigine));
        lista.set(idxOrigine, itemDestinazioneEsistente);
        
        // Pulisce eventuali elementi null superflui in coda alla lista se possibile
        pulisciCodaNull(lista);
    }

    private String gestisciTrasferimento(Giocatore player, List<Item> sorgente, List<Item> destinazione, 
                                        int idxOrigine, int idxDestinazione, 
                                        Item itemSpostato, char tipoOrigine, String slotDest) {
        // Riempie la lista di destinazione con null se necessario
        while (destinazione.size() <= idxDestinazione) {
            destinazione.add(null);
        }

        Item itemRimpiazzato = destinazione.get(idxDestinazione);

        // L'oggetto che lascia l'inventario del giocatore è itemSpostato (se esce da 'G') 
        // oppure itemRimpiazzato (se un item da 'S' prende il suo posto in 'G')
        Item itemCheEsce = (tipoOrigine == 'G') ? itemSpostato : itemRimpiazzato;

        // CONTROLLO DISEQUIPAGGIAMENTO INLINE
        if (player != null && itemCheEsce != null) {
            Item arma = player.getArmaEquipaggiata();
            if (arma != null && (arma == itemCheEsce || 
                (arma.getId() != 0 && arma.getId() == itemCheEsce.getId()) || 
                (arma.getNome() != null && itemCheEsce.getNome() != null && arma.getNome().trim().equalsIgnoreCase(itemCheEsce.getNome().trim())))) {
                player.setArmaEquipaggiata(null);
            }

            Item cons = player.getConsumabileEquipaggiato();
            if (cons != null && (cons == itemCheEsce || 
                (cons.getId() != 0 && cons.getId() == itemCheEsce.getId()) || 
                (cons.getNome() != null && itemCheEsce.getNome() != null && cons.getNome().trim().equalsIgnoreCase(itemCheEsce.getNome().trim())))) {
                player.setConsumabileEquipaggiato(null);
            }
        }
        
        // Posiziona l'oggetto nella destinazione esatta
        destinazione.set(idxDestinazione, itemSpostato);

        if (itemRimpiazzato != null) {
            // Se c'era già un oggetto, lo rimette nello slot di origine esatto
            sorgente.set(idxOrigine, itemRimpiazzato);
            return "Scambiato " + itemSpostato.getNome() + " con " + itemRimpiazzato.getNome();
        } else {
            // Altrimenti libera lo slot di origine
            sorgente.set(idxOrigine, null);
            pulisciCodaNull(sorgente);
            String azione = (tipoOrigine == 'G') ? "Lasciato " : "Raccolto ";
            return azione + itemSpostato.getNome() + " nello slot " + slotDest;
        }
    }

    /**
     * Rimuove i null in eccesso alla fine della lista per mantenere la dimensione pulita.
     */
    private void pulisciCodaNull(List<Item> lista) {
        for (int i = lista.size() - 1; i >= 0; i--) {
            if (lista.get(i) == null) {
                lista.remove(i);
            } else {
                break;
            }
        }
    }

    /**
     * Converte "b2" o "2b" -> riga 'b' (1), colonna '2' (1) -> (1 * COLS) + 1 = 5
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

            if (riga < 0 || colonnaNum < 0 || colonnaNum >= COLS) {
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
                .filter(item -> item != null && item.getTipo() == tipo)
                .collect(Collectors.toList());
    }

    public List<Item> cercaInStanzaPerTipo(Stanza stanza, TipoItem tipo) {
        return stanza.getOggettiNellaStanza().stream()
                .filter(item -> item != null && item.getTipo() == tipo)
                .collect(Collectors.toList());
    }

    public List<Item> cercaInInventarioPerNome(Giocatore player, String nome) {
        return player.getInventario().stream()
                .filter(item -> item != null && item.getNome().toLowerCase().contains(nome.toLowerCase()))
                .collect(Collectors.toList());
    }

    // =========================================================================
    // METODI DI EQUIPAGGIAMENTO (CON VALIDAZIONE TIPO ITEM)
    // =========================================================================

    public boolean equipaggiaArma(Giocatore player, String coordinate) {
        if (player == null || coordinate == null) {
            return false;
        }
        int index = calcolaIndice(coordinate);
        List<Item> inventario = player.getInventario();

        if (index >= 0 && index < inventario.size() && inventario.get(index) != null) {
            Item item = inventario.get(index);
            if (item.getTipo() != TipoItem.ARMA) {
                return false; 
            }
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

        if (index >= 0 && index < inventario.size() && inventario.get(index) != null) {
            Item item = inventario.get(index);
            if (item.getTipo() != TipoItem.POZIONE) {
                return false; 
            }
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

        if (index < 0 || index >= player.getInventario().size() || player.getInventario().get(index) == null) {
            return "Nessun oggetto trovato nelle coordinate specificate.";
        }

        Item item = player.getInventario().get(index);

        if (tipoEquip == 'A') {
            if (item.getTipo() != TipoItem.ARMA) {
                return "Errore: " + item.getNome() + " non è un'arma!";
            }
            
            if (player.getArmaEquipaggiata() == item) {
                player.setArmaEquipaggiata(null);
                return "Arma disequipaggiata: " + item.getNome();
            } else {
                player.setArmaEquipaggiata(item);
                return "Equipaggiata arma: " + item.getNome();
            }
        } else {
            if (item.getTipo() != TipoItem.POZIONE) {
                return "Errore: " + item.getNome() + " non è una pozione!";
            }

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

        int valoreEffetto = consumabile.getValoreEffetto();
        String messaggioEsito;

        switch (consumabile.getTargetEffetto()) {
            case SALUTE_GIOCATORE -> {
                player.modificaHp(valoreEffetto);
                messaggioEsito = "Hai utilizzato " + consumabile.getNome() + "! Ripristinati " + valoreEffetto + " HP.";
            }
            case POTENZIAMENTO_ARMA -> {
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