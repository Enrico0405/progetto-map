package game.common.dto;

import java.util.List;

public class GameResponse {

    private String messaggio;
    private boolean successo = true;

    // Dati Sessione e Giocatore
    private int idGiocatore;
    private int playerHp;
    private int playerMaxHp;
    
    // EQUIPAGGIAMENTO E INVENTARIO A LISTA (Aggiornati per la nuova logica del Server)
    private String currentWeapon; // Nome dell'arma attualmente impugnata
    private List<String> inventarioNomi;  // Lista piatta con i nomi degli oggetti
    private List<String> inventarioPaths; // Lista piatta con i percorsi delle icone png

    // Dati Nemico
    private int enemyHp;
    private int enemyMaxHp;

    // Dati Stanza e Stato Ambientale
    private String nomeStanza;
    private String descrizioneStanza;
    private String stanzaSfondoPath;
    private boolean stanzaBloccata;

    // Asset Grafici per la GUI
    private String enemySpritePath;
    private String mappaPngPath;

    // Flag di Stato Partita
    private boolean inCombattimento;
    private boolean gameOver;
    private boolean vittoria;

    public GameResponse() {
    }

    public GameResponse(String messaggio) {
        this.messaggio = messaggio;
    }

    public String getMessaggio() { return messaggio; }
    public void setMessaggio(String messaggio) { this.messaggio = messaggio; }

    public boolean isSuccesso() { return successo; }
    public void setSuccesso(boolean successo) { this.successo = successo; }

    public int getIdGiocatore() { return idGiocatore; }
    public void setIdGiocatore(int idGiocatore) { this.idGiocatore = idGiocatore; }

    public int getPlayerHp() { return playerHp; }
    public void setPlayerHp(int playerHp) { this.playerHp = playerHp; }

    public int getPlayerMaxHp() { return playerMaxHp; }
    public void setPlayerMaxHp(int playerMaxHp) { this.playerMaxHp = playerMaxHp; }

    // --- NUOVI GETTER E SETTER PER INVENTARIO ED EQUIPAGGIAMENTO ---
    public String getCurrentWeapon() { return currentWeapon; }
    public void setCurrentWeapon(String currentWeapon) { this.currentWeapon = currentWeapon; }

    public List<String> getInventarioNomi() { return inventarioNomi; }
    public void setInventarioNomi(List<String> inventarioNomi) { this.inventarioNomi = inventarioNomi; }

    public List<String> getInventarioPaths() { return inventarioPaths; }
    public void setInventarioPaths(List<String> inventarioPaths) { this.inventarioPaths = inventarioPaths; }
    // ---------------------------------------------------------------

    public int getEnemyHp() { return enemyHp; }
    public void setEnemyHp(int enemyHp) { this.enemyHp = enemyHp; }

    public int getEnemyMaxHp() { return enemyMaxHp; }
    public void setEnemyMaxHp(int enemyMaxHp) { this.enemyMaxHp = enemyMaxHp; }

    public String getNomeStanza() { return nomeStanza; }
    public void setNomeStanza(String nomeStanza) { this.nomeStanza = nomeStanza; }

    public String getDescrizioneStanza() { return descrizioneStanza; }
    public void setDescrizioneStanza(String descrizioneStanza) { this.descrizioneStanza = descrizioneStanza; }

    public String getStanzaSfondoPath() { return stanzaSfondoPath; }
    public void setStanzaSfondoPath(String stanzaSfondoPath) { this.stanzaSfondoPath = stanzaSfondoPath; }

    public boolean isStanzaBloccata() { return stanzaBloccata; }
    public void setStanzaBloccata(boolean stanzaBloccata) { this.stanzaBloccata = stanzaBloccata; }

    public String getEnemySpritePath() { return enemySpritePath; }
    public void setEnemySpritePath(String enemySpritePath) { this.enemySpritePath = enemySpritePath; }

    public String getMappaPngPath() { return mappaPngPath; }
    public void setMappaPngPath(String mappaPngPath) { this.mappaPngPath = mappaPngPath; }

    public boolean isInCombattimento() { return inCombattimento; }
    public void setInCombattimento(boolean inCombattimento) { this.inCombattimento = inCombattimento; }

    public boolean isGameOver() { return gameOver; }
    public void setGameOver(boolean gameOver) { this.gameOver = gameOver; }

    public boolean isVittoria() { return vittoria; }
    public void setVittoria(boolean vittoria) { this.vittoria = vittoria; }
}