package game.server.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Rappresenta lo stato e le statistiche del giocatore attivo in RAM.
 */
public class Giocatore {
    public static final int CAPACITA_MASSIMA = 12; // 3 righe x 4 colonne

    private int id;
    private String nome;
    private int hp;
    private int maxHp;
    private String avatarPath;
    private List<Item> inventario;
    private Item armaEquipaggiata;
    private Item consumabileEquipaggiato;
    private Stanza stanzaCorrente;

    public Giocatore() {
        this.inventario = new ArrayList<>();
    }

    public Giocatore(int id, String nome, int hp, int maxHp, String avatarPath) {
        this.id = id;
        this.nome = nome;
        this.hp = hp;
        this.maxHp = maxHp;
        this.avatarPath = avatarPath;
        this.inventario = new ArrayList<>();
    }

    public void modificaHp(int delta) {
        this.hp = Math.min(maxHp, Math.max(0, this.hp + delta));
    }

    public boolean isVivo() {
        return this.hp > 0;
    }

    public boolean aggiungiItem(Item item) {
        if (this.inventario.size() >= CAPACITA_MASSIMA) {
            return false; // Inventario pieno
        }
        return this.inventario.add(item);
    }

    public boolean rimuoviItem(Item item) {
        return this.inventario.remove(item);
    }

    public Optional<Item> cercaItem(String nomeItem) {
        return inventario.stream()
                .filter(i -> i.getNome().equalsIgnoreCase(nomeItem))
                .findFirst();
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public int getHp() { return hp; }
    public void setHp(int hp) { this.hp = hp; }

    public int getMaxHp() { return maxHp; }
    public void setMaxHp(int maxHp) { this.maxHp = maxHp; }

    public String getAvatarPath() { return avatarPath; }
    public void setAvatarPath(String avatarPath) { this.avatarPath = avatarPath; }

    public List<Item> getInventario() { return inventario; }
    public void setInventario(List<Item> inventario) { this.inventario = inventario; }

    public Item getArmaEquipaggiata() { return armaEquipaggiata; }
    public void setArmaEquipaggiata(Item armaEquipaggiata) { this.armaEquipaggiata = armaEquipaggiata; }

    public Item getConsumabileEquipaggiato() { return consumabileEquipaggiato; }
    public void setConsumabileEquipaggiato(Item consumabileEquipaggiato) { this.consumabileEquipaggiato = consumabileEquipaggiato; }

    public Stanza getStanzaCorrente() { return stanzaCorrente; }
    public void setStanzaCorrente(Stanza stanzaCorrente) { this.stanzaCorrente = stanzaCorrente; }
}