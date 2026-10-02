
package game.server.model;

/**
 * Rappresenta un avversario/nemico in una stanza.
 */
public class Nemico {
    private int id;
    private String nome;
    private int hp;
    private int maxHp;
    private int dannoAttacco;
    private double precisione;
    private String spritePath;
    private TipoNemico tipo;
    private String messaggioAgguato;
    

    public Nemico() {}

    public Nemico(int id, String nome, int hp, int maxHp, int dannoAttacco,double precisione, String spritePath, TipoNemico tipo, String messaggioAgguato) {
        this.id = id;
        this.nome = nome;
        this.hp = hp;
        this.maxHp = maxHp;
        this.dannoAttacco = dannoAttacco;
        this.precisione=precisione; 
        this.spritePath = spritePath;
        this.tipo = tipo;
        this.messaggioAgguato = messaggioAgguato;
        this.precisione = 0.8;
    }

    public void subisciDanno(int danno) {
        this.hp = Math.max(0, this.hp - danno);
    }

    public boolean isVivo() {
        return this.hp > 0;
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

    public int getDannoAttacco() { return dannoAttacco; }
    public void setDannoAttacco(int dannoAttacco) { this.dannoAttacco = dannoAttacco; }

    public String getSpritePath() { return spritePath; }
    public void setSpritePath(String spritePath) { this.spritePath = spritePath; }

    public TipoNemico getTipo() { return tipo; }
    public void setTipo(TipoNemico tipo) { this.tipo = tipo; }

    public String getMessaggioAgguato() { return messaggioAgguato; }
    public void setMessaggioAgguato(String messaggioAgguato) { this.messaggioAgguato = messaggioAgguato; }

   

    public double getPrecisione() {
        return precisione;
    }

    public void setPrecisione(double precisione) {
        this.precisione = precisione;
    }
    
}

