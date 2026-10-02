/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package game.common.dto;

public class GameResponse {

    private String messaggio;
    private boolean successo;
    private int playerHp;
    private int playerMaxHp;
    private int enemyHp;
    private int enemyMaxHp;
    private String stanzaSfondoPath;
    private String enemySpritePath;
    private String mappaPngPath;
    private boolean inCombattimento;
    private boolean gameOver;
    private boolean vittoria;

    /*
    costruttore vuoto obbligatorio per jackson
    */
    public GameResponse() {
    }

    public String getMessaggio() {
        return messaggio;
    }

    public void setMessaggio(String messaggio) {
        this.messaggio = messaggio;
    }

    public boolean isSuccesso() {
        return successo;
    }

    public void setSuccesso(boolean successo) {
        this.successo = successo;
    }

    public int getPlayerHp() {
        return playerHp;
    }

    public void setPlayerHp(int playerHp) {
        this.playerHp = playerHp;
    }

    public int getPlayerMaxHp() {
        return playerMaxHp;
    }

    public void setPlayerMaxHp(int playerMaxHp) {
        this.playerMaxHp = playerMaxHp;
    }

    public int getEnemyHp() {
        return enemyHp;
    }

    public void setEnemyHp(int enemyHp) {
        this.enemyHp = enemyHp;
    }

    public int getEnemyMaxHp() {
        return enemyMaxHp;
    }

    public void setEnemyMaxHp(int enemyMaxHp) {
        this.enemyMaxHp = enemyMaxHp;
    }

    public String getStanzaSfondoPath() {
        return stanzaSfondoPath;
    }

    public void setStanzaSfondoPath(String stanzaSfondoPath) {
        this.stanzaSfondoPath = stanzaSfondoPath;
    }

    public String getEnemySpritePath() {
        return enemySpritePath;
    }

    public void setEnemySpritePath(String enemySpritePath) {
        this.enemySpritePath = enemySpritePath;
    }

    public String getMappaPngPath() {
        return mappaPngPath;
    }

    public void setMappaPngPath(String mappaPngPath) {
        this.mappaPngPath = mappaPngPath;
    }

    public boolean isInCombattimento() {
        return inCombattimento;
    }

    public void setInCombattimento(boolean inCombattimento) {
        this.inCombattimento = inCombattimento;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    public boolean isVittoria() {
        return vittoria;
    }

    public void setVittoria(boolean vittoria) {
        this.vittoria = vittoria;
    }
}