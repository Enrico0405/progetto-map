package game.server.controller;

import game.server.model.GameState;
import game.server.model.Giocatore;
import game.server.model.Nemico;
import game.server.model.Stanza;
import game.server.model.Item;
import game.server.model.TipoItem;
import game.server.model.TargetEffetto;
import game.server.model.TipoNemico;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Gestisce la logica dei combattimenti e della fuga.
 * Mantiene lo stato temporaneo della fuga internamente senza alterare GameState.
 */
public class combatController {

    private final Random random = new Random();
    // Memorizza gli id delle sessioni che hanno superato il check di fuga e devono scegliere la stanza
    private final Set<Integer> sessioniInAttesaFuga = new HashSet<>();

    public String avviaScontro(GameState gameState) {
        if (gameState.getStanzaAttuale() == null) {
            return "Non sei in nessuna stanza.";
        }

        Nemico nemico = gameState.getStanzaAttuale().getNemicoPresente();
        if (nemico == null || !nemico.isVivo()) {
            return "Non c'è nessun nemico da affrontare in questa stanza.";
        }

        gameState.setInCombattimento(true);
        sessioniInAttesaFuga.remove(gameState.getIdGiocatore());

        String messaggio = (nemico.getMessaggioAgguato() != null && !nemico.getMessaggioAgguato().isBlank())
                ? nemico.getMessaggioAgguato()
                : "Inizia lo scontro con " + nemico.getNome() + "!";

        return messaggio + " (HP Nemico: " + nemico.getHp() + "/" + nemico.getMaxHp() + ")";
    }

    public String eseguiAttacco(GameState gameState) {
        if (!gameState.isInCombattimento()) {
            return "Non sei attualmente in combattimento.";
        }

        if (sessioniInAttesaFuga.contains(gameState.getIdGiocatore())) {
            return "Hai disingaggiato il nemico: devi indicare la direzione verso cui fuggire!";
        }

        Giocatore player = gameState.getPlayer();
        Nemico nemico = gameState.getNemicoInScontro();

        if (nemico == null || !nemico.isVivo()) {
            gameState.setInCombattimento(false);
            return "Il nemico è già stato sconfitto.";
        }

        StringBuilder esito = new StringBuilder();

        int danno = 5;
        Item arma = player.getArmaEquipaggiata();
        if (arma != null && arma.getTipo() == TipoItem.ARMA) {
            danno += arma.getValoreEffetto();
        }

        nemico.subisciDanno(danno);
        esito.append("Hai attaccato ").append(nemico.getNome())
             .append(" infliggendo ").append(danno).append(" danni. ");

        if (!nemico.isVivo()) {
            gameState.setInCombattimento(false);
            sessioniInAttesaFuga.remove(gameState.getIdGiocatore());
            if (nemico.getId() == 9){
             esito.append("\n[GIOCO_FINITO]\n\n")
             .append("HAI SCONFITTO IL RE STREGONE ANGMAR!\n")
             .append("La sua oscurità svanisce e il regno è finalmente salvo dall'influenza della Fiamma!");
            }
            else {
                esito.append("\nHai sconfitto ").append(nemico.getNome()).append("!");

                 // RICOMPENSA VITTORIA BOSS STANDARD
                gestisciRicompensaVittoria(player, nemico, esito);
            }

            return esito.toString();
        }
        esito.append("(HP Nemico: ").append(nemico.getHp()).append("/").append(nemico.getMaxHp()).append(")\n");
        esito.append(eseguiTurnoNemico(player, nemico, gameState));

        return esito.toString();
    }

    public String usaItemInScontro(GameState gameState, String nomeItem) {
        if (!gameState.isInCombattimento()) {
            return "Non sei in combattimento.";
        }

        if (sessioniInAttesaFuga.contains(gameState.getIdGiocatore())) {
            return "Hai già guadagnato la fuga! Scegli la direzione in cui scappare.";
        }

        Giocatore player = gameState.getPlayer();
        Nemico nemico = gameState.getNemicoInScontro();

        var optItem = player.cercaItem(nomeItem);
        if (optItem.isEmpty()) {
            return "Non possiedi l'oggetto: " + nomeItem;
        }

        Item item = optItem.get();
        if (!item.isUtilizzabileInScontro()) {
            return "Non puoi usare questo oggetto durante uno scontro!";
        }

        StringBuilder esito = new StringBuilder();
        TargetEffetto target = item.getTargetEffetto() != null ? item.getTargetEffetto() : TargetEffetto.NESSUNO;

        switch (target) {
            case SALUTE_GIOCATORE -> {
                player.modificaHp(item.getValoreEffetto());
                esito.append("Hai usato ").append(item.getNome())
                     .append(" recuperando ").append(item.getValoreEffetto()).append(" HP. ")
                     .append("(HP: ").append(player.getHp()).append("/").append(player.getMaxHp()).append(")\n");
            }
            case POTENZIAMENTO_ARMA -> {
                if (nemico != null && nemico.isVivo()) {
                    nemico.subisciDanno(item.getValoreEffetto());
                    esito.append("Hai usato ").append(item.getNome())
                         .append(" infliggendo ").append(item.getValoreEffetto()).append(" danni a ")
                         .append(nemico.getNome()).append("!\n");
                }
            }
            case NESSUNO -> esito.append("L'oggetto non ha avuto alcun effetto immediato.\n");
        }

        if (item.getTipo() == TipoItem.POZIONE || item.getTipo() == TipoItem.PERGAMENA) {
            player.rimuoviItem(item);
        }

        if (nemico != null && !nemico.isVivo()) {
            gameState.setInCombattimento(false);
            sessioniInAttesaFuga.remove(gameState.getIdGiocatore());
            esito.append("Hai sconfitto ").append(nemico.getNome()).append("!");

            // RICOMPENSA VITTORIA BOSS
            gestisciRicompensaVittoria(player, nemico, esito);

            return esito.toString();
        }

        if (nemico != null && nemico.isVivo()) {
            esito.append(eseguiTurnoNemico(player, nemico, gameState));
        }

        return esito.toString();
    }

    /**
     * Verifica se il nemico sconfitto è un Boss di livello.
     * In tal caso aumenta di +20 gli HP Massimi del giocatore e ripristina la salute al 100%.
     */
    private void gestisciRicompensaVittoria(Giocatore player, Nemico nemico, StringBuilder esito) {
        if (nemico != null && nemico.getTipo() == TipoNemico.BOSS) {
            int nuovoMaxHp = player.getMaxHp() + 20;
            player.setMaxHp(nuovoMaxHp);
            player.setHp(nuovoMaxHp); // Ripristino completo della salute al nuovo valore massimo
            
            esito.append("\n[VITTORIA SUL BOSS LOGRATA!] La tua tempra e la tua forza aumentano!")
                 .append("\nSalute Massima Aumentata: +20 HP.")
                 .append("\nSalute completamente ripristinata! (HP: ")
                 .append(player.getHp()).append("/").append(player.getMaxHp()).append(")");
        }
    }

    /**
     * Fase 1: Il giocatore richiede la fuga. Se ci sono vie libere, 
     * il disingaggio riesce sempre al 100%.
     */
    public String avviaTentativoFuga(GameState gameState) {
        if (!gameState.isInCombattimento()) {
            return "Non sei in combattimento.";
        }

        int id = gameState.getIdGiocatore();
        if (sessioniInAttesaFuga.contains(id)) {
            return "Hai già guadagnato una via di fuga! Scegli una direzione verso cui correre.";
        }

        Giocatore player = gameState.getPlayer();
        Nemico nemico = gameState.getNemicoInScontro();
        Stanza stanzaAttuale = gameState.getStanzaAttuale();

        if (stanzaAttuale == null) {
            return "Errore: stanza attuale non definita.";
        }

        List<String> vieAperte = new ArrayList<>();
        for (Map.Entry<String, Stanza> entry : stanzaAttuale.getAdiacenze().entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isBloccata()) {
                vieAperte.add(entry.getKey().toUpperCase());
            }
        }

        if (vieAperte.isEmpty()) {
            StringBuilder sb = new StringBuilder("Non ci sono vie di scampo aperte in questa stanza!\n");
            sb.append(eseguiTurnoNemico(player, nemico, gameState));
            return sb.toString();
        }

        sessioniInAttesaFuga.add(id);
        return "Sei riuscito a disingaggiarti! Dove vuoi fuggire? Vie disponibili: "
                + String.join(", ", vieAperte)
                + "\n(Usa il comando per muoverti nella direzione desiderata)";
    }

    public boolean isInAttesaFuga(int idGiocatore) {
        return sessioniInAttesaFuga.contains(idGiocatore);
    }

    /**
     * Fase 2: Chiamata quando l'utente specifica la direzione dopo una fuga riuscita.
     * Effettua lo spostamento e ingaggia automaticamente un eventuale nemico presente nella nuova stanza.
     */
    public String confermaDirezioneFuga(GameState gameState, String direzione) {
        int id = gameState.getIdGiocatore();
        if (!sessioniInAttesaFuga.contains(id)) {
            return "Nessuna fuga in sospeso da confermare.";
        }

        if (direzione == null || direzione.isBlank()) {
            return "Devi specificare una direzione verso cui scappare.";
        }

        Stanza stanzaAttuale = gameState.getStanzaAttuale();
        String dirNormalizzata = direzione.toLowerCase().trim();
        Stanza destinazione = stanzaAttuale != null ? stanzaAttuale.getAdiacenza(dirNormalizzata) : null;

        if (destinazione == null) {
            return "Non c'è alcun passaggio verso " + direzione.toUpperCase() + ". Scegli una direzione valida!";
        }

        if (destinazione.isBloccata()) {
            return "Il passaggio verso " + direzione.toUpperCase() + " è bloccato! Scegli un'altra via!";
        }

        Giocatore player = gameState.getPlayer();
        player.setStanzaCorrente(destinazione);
        gameState.setStanzaAttuale(destinazione);

        sessioniInAttesaFuga.remove(id);

        // Controllo se la nuova stanza contiene un nemico ancora vivo
        Nemico nemicoNuovo = destinazione.getNemicoPresente();
        if (nemicoNuovo != null && nemicoNuovo.isVivo()) {
            gameState.setInCombattimento(true);

            String msgAgguato = (nemicoNuovo.getMessaggioAgguato() != null && !nemicoNuovo.getMessaggioAgguato().isBlank())
                    ? nemicoNuovo.getMessaggioAgguato()
                    : "Inizia lo scontro con " + nemicoNuovo.getNome() + "!";

            return "Sei fuggito a rotta di collo verso " + dirNormalizzata.toUpperCase()
                    + "! Ti trovi ora in: " + destinazione.getNome() + ".\n\n[NUOVO PERICOLO!] "
                    + msgAgguato + " (HP Nemico: " + nemicoNuovo.getHp() + "/" + nemicoNuovo.getMaxHp() + ")";
        } else {
            gameState.setInCombattimento(false);
            return "Sei fuggito a rotta di collo verso " + dirNormalizzata.toUpperCase()
                    + "! Ti trovi ora in: " + destinazione.getNome() + ".";
        }
    }

    private String eseguiTurnoNemico(Giocatore player, Nemico nemico, GameState gameState) {
        StringBuilder sb = new StringBuilder();

        if (random.nextDouble() > nemico.getPrecisione()) {
            sb.append(nemico.getNome()).append(" sferra un colpo ma ti manca!");
            return sb.toString();
        }

        int danno = nemico.getDannoAttacco();

        if (nemico.getTipo() != null) {
            switch (nemico.getTipo()) {
                case ASSASSINO -> {
                    if (random.nextDouble() < 0.25) {
                        danno = (int) (danno * 1.5);
                        sb.append("[Colpo Critico!] ");
                    }
                }
                case BOSS -> {
                    if (nemico.getHp() < (nemico.getMaxHp() * 0.3)) {
                        danno += 5;
                        sb.append("[Furia del Boss!] ");
                    }
                }
                case INQUISITORE, SOLDATO, ACCOLITO -> {}
            }
        }

        player.modificaHp(-danno);
        sb.append(nemico.getNome()).append(" ti infligge ").append(danno).append(" danni! ")
          .append("(HP Tuoi: ").append(player.getHp()).append("/").append(player.getMaxHp()).append(")");

        if (!player.isVivo()) {
            gameState.setInCombattimento(false);
            sessioniInAttesaFuga.remove(gameState.getIdGiocatore());
            sb.append("\nSei caduto in battaglia... Game Over.");
        }

        return sb.toString();
    }
}