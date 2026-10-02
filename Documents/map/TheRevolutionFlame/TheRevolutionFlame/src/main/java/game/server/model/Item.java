
package game.server.model;



/**
 * Rappresenta un oggetto generico nel mondo di gioco (arma, chiave, pozione, ecc.).
 */
public class Item {
    private int id;
    private String nome;
    private String descrizione;
    private String iconaPath;
    private int valoreEffetto;
    private boolean utilizzabileInScontro;
    private TipoItem tipo;
    private TargetEffetto targetEffetto;

    public TargetEffetto getTargetEffetto() {
        return targetEffetto;
    }

    public void setTargetEffetto(TargetEffetto targetEffetto) {
        this.targetEffetto = targetEffetto;
    }

    public Item() {}

    public Item(int id, String nome, String descrizione, String iconaPath, int valoreEffetto, boolean utilizzabileInScontro, TipoItem tipo) {
        this.id = id;
        this.nome = nome;
        this.descrizione = descrizione;
        this.iconaPath = iconaPath;
        this.valoreEffetto = valoreEffetto;
        this.utilizzabileInScontro = utilizzabileInScontro;
        this.tipo = tipo;
    }

    // Getters e Setters (Operano esclusivamente in RAM)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescrizione() { return descrizione; }
    public void setDescrizione(String descrizione) { this.descrizione = descrizione; }

    public String getIconaPath() { return iconaPath; }
    public void setIconaPath(String iconaPath) { this.iconaPath = iconaPath; }

    public int getValoreEffetto() { return valoreEffetto; }
    public void setValoreEffetto(int valoreEffetto) { this.valoreEffetto = valoreEffetto; }

    public boolean isUtilizzabileInScontro() { return utilizzabileInScontro; }
    public void setUtilizzabileInScontro(boolean utilizzabileInScontro) { this.utilizzabileInScontro = utilizzabileInScontro; }

    public TipoItem getTipo() { return tipo; }
    public void setTipo(TipoItem tipo) { this.tipo = tipo; }
}

