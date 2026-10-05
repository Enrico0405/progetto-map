package game.server.model;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Rappresenta un nodo (stanza) nel grafo della mappa di gioco.
 * Gestisce direttamente la lista degli oggetti presenti al suo interno.
 */
public class Stanza {
    private int id;
    private int idLivello;
    private String nome;
    private String descrizione;
    private String immagineSfondoPath;
    private boolean bloccata;
    private boolean stanza_letale;
    private int stanza_letale_danno ;

    public int getStanza_letale_danno() {
        return stanza_letale_danno;
    }

    public void setStanza_letale_danno(int stanza_letale_danno) {
        this.stanza_letale_danno = stanza_letale_danno;
    }
    private Nemico nemicoPresente;
    private List<Item> oggettiNellaStanza;
    private List<Item> oggettiRichiestiSblocco;
    private final Map<String, Stanza> adiacenze;

    public Stanza() {
        this.adiacenze = new HashMap<>();
        this.oggettiNellaStanza = new ArrayList<>();
        this.oggettiRichiestiSblocco = new ArrayList<>();
    }

    public Stanza(int id, int idLivello, String nome, String descrizione, String immagineSfondoPath) {
        this.id = id;
        this.idLivello = idLivello;
        this.nome = nome;
        this.descrizione = descrizione;
        this.immagineSfondoPath = immagineSfondoPath;
        this.adiacenze = new HashMap<>();
        this.oggettiNellaStanza = new ArrayList<>();
        this.oggettiRichiestiSblocco = new ArrayList<>();
    }

    public Stanza getAdiacenza(String direzione) {
        return adiacenze.get(direzione.toLowerCase());
    }

    public void setAdiacenza(String direzione, Stanza stanza) {
        this.adiacenze.put(direzione.toLowerCase(), stanza);
    }

    public boolean haNemicoVivo() {
        return nemicoPresente != null && nemicoPresente.isVivo();
    }

    // Metodi di utilità per la gestione degli oggetti nella stanza
    public void aggiungiOggetto(Item item) {
        this.oggettiNellaStanza.add(item);
    }

    public boolean rimuoviOggetto(Item item) {
        return this.oggettiNellaStanza.remove(item);
    }

    public Optional<Item> cercaOggetto(String nomeItem) {
        return oggettiNellaStanza.stream()
                .filter(i -> i.getNome().equalsIgnoreCase(nomeItem))
                .findFirst();
    }

    public boolean haOggetti() {
        return !oggettiNellaStanza.isEmpty();
    }

    public void aggiungiOggettoRichiestoSblocco(Item item) {
        this.oggettiRichiestiSblocco.add(item);
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdLivello() { return idLivello; }
    public void setIdLivello(int idLivello) { this.idLivello = idLivello; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescrizione() { return descrizione; }
    public void setDescrizione(String descrizione) { this.descrizione = descrizione; }

    public String getImmagineSfondoPath() { return immagineSfondoPath; }
    public void setImmagineSfondoPath(String immagineSfondoPath) { this.immagineSfondoPath = immagineSfondoPath; }

    public boolean isBloccata() { return bloccata; }
    public void setBloccata(boolean bloccata) { this.bloccata = bloccata; }

    public boolean isPericolosa() { return stanza_letale; }
    public void setPericolosa(boolean stanza_letale) { this.stanza_letale = stanza_letale; }

    public Nemico getNemicoPresente() { return nemicoPresente; }
    public void setNemicoPresente(Nemico nemicoPresente) { this.nemicoPresente = nemicoPresente; }

    public List<Item> getOggettiNellaStanza() { return oggettiNellaStanza; }
    public void setOggettiNellaStanza(List<Item> oggettiNellaStanza) { this.oggettiNellaStanza = oggettiNellaStanza; }

    public List<Item> getOggettiRichiestiSblocco() { return oggettiRichiestiSblocco; }
    public void setOggettiRichiestiSblocco(List<Item> oggettiRichiestiSblocco) { this.oggettiRichiestiSblocco = oggettiRichiestiSblocco; }

    public Map<String, Stanza> getAdiacenze() { return adiacenze; }
}


