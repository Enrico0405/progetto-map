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

        if (player == null || stanza == null) {
            return "Stato di gioco non valido per lo spostamento.";
        }

        List<Item> listaOrigine = (tipoOrigine == 'G') ? player.getInventario() : stanza.getOggettiNellaStanza();
        List<Item> listaDestinazione = (tipoDestinazione == 'G') ? player.getInventario() : stanza.getOggettiNellaStanza();

        if (idxOrigine >= listaOrigine.size()) {
            return "Nessun oggetto presente nello slot di partenza " + sorgente;
        }

        Item itemSpostato = listaOrigine.get(idxOrigine);

        if (tipoOrigine == tipoDestinazione) {
            gestisciSpostamentoInterno(listaOrigine, idxOrigine, idxDestinazione);
            return "Spostato " + itemSpostato.getNome() + " in " + destinazione;
        } 
        
        return gestisciTrasferimento(listaOrigine, listaDestinazione, idxOrigine, idxDestinazione, itemSpostato, tipoOrigine, destinazione);
    }

    private void gestisciSpostamentoInterno(List<Item> lista, int idxOrigine, int idxDestinazione) {
        if (idxDestinazione >= lista.size()) {
            Item item = lista.remove(idxOrigine);
            lista.add(item);
        } else {
            Item temp = lista.get(idxOrigine);
            lista.set(idxOrigine, lista.get(idxDestinazione));
            lista.set(idxDestinazione, temp);
        }
    }

    private String gestisciTrasferimento(List<Item> sorgente, List<Item> destinazione, 
                                        int idxOrigine, int idxDestinazione, 
                                        Item itemSpostato, char tipoOrigine, String slotDest) {
        if (idxDestinazione < destinazione.size()) {
            sorgente.remove(idxOrigine);
            Item itemRimpiazzato = destinazione.set(idxDestinazione, itemSpostato);
            sorgente.add(idxOrigine, itemRimpiazzato);
            return "Scambiato " + itemSpostato.getNome() + " con " + itemRimpiazzato.getNome();
        } else {
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

            int riga = rigaChar - 'a';

            if (riga < 0 || riga >= ROWS || colonnaNum < 0 || colonnaNum >= COLS) {
                return -1;
            }

            return (riga * COLS) + colonnaNum;
        } catch (Exception e) {
            return -1;
        }
    }

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
                .filter(item -> item != null && item.getNome() != null && item.getNome().toLowerCase().contains(nome.toLowerCase()))
                .collect(Collectors.toList());
    }

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
            if (item.getTipo() != TipoItem.ARMA) {
                return "Impossibile equipaggiare: '" + item.getNome() + "' non è un'arma!";
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
                return "Impossibile equipaggiare: '" + item.getNome() + "' non è un consumabile!";
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

        TargetEffetto target = consumabile.getTargetEffetto();
        // FALLBACK: Se il target letto dal DB è null, imposta di default SALUTE_GIOCATORE
        if (target == null) {
            target = TargetEffetto.SALUTE_GIOCATORE;
        }

        int valoreEffetto = consumabile.getValoreEffetto();
        String messaggioEsito;

        if (target == TargetEffetto.SALUTE_GIOCATORE) {
            int hpPrima = player.getHp();
            player.modificaHp(valoreEffetto);
            int hpDopo = player.getHp();
            int effettivaCura = hpDopo - hpPrima;

            messaggioEsito = "Hai utilizzato " + consumabile.getNome() + "! Ripristinati +" + effettivaCura + " HP (Salute: " + hpDopo + " HP).";
        } else if (target == TargetEffetto.DANNO_ARMA) {
            Item arma = player.getArmaEquipaggiata();
            if (arma == null) {
                return "Non hai un'arma equipaggiata da potenziare!";
            }
            int nuovoDanno = arma.getValoreEffetto() + valoreEffetto;
            arma.setValoreEffetto(nuovoDanno);
            messaggioEsito = "Hai utilizzato " + consumabile.getNome() + "! Il danno di " + arma.getNome() 
                    + " è aumentato di +" + valoreEffetto + " (Danno totale: " + nuovoDanno + ").";
        } else {
            return "Questo oggetto non produce alcun effetto applicabile.";
        }

        player.setConsumabileEquipaggiato(null);

        if (player.getInventario() != null) {
            player.getInventario().remove(consumabile);
        }

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