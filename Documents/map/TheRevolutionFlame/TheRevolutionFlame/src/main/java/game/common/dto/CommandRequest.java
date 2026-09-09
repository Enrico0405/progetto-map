/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package game.common.dto;

public class CommandRequest {

    private int idGiocatore;
    private String azione;
    private String argomento;

    /*
    costruttore vuoto necessario per jackson
    */
    public CommandRequest() {
    }

    // costruttore completo
    public CommandRequest(int idGiocatore, String azione, String argomento) {
        this.idGiocatore = idGiocatore;
        this.azione = azione;
        this.argomento = argomento;
    }
/*
    getter e setter vari per gli attributi
    */
    public int getIdGiocatore() {
        return idGiocatore;
    }

    public void setIdGiocatore(int idGiocatore) {
        this.idGiocatore = idGiocatore;
    }

    public String getAzione() {
        return azione;
    }

    public void setAzione(String azione) {
        this.azione = azione;
    }

    public String getArgomento() {
        return argomento;
    }

    public void setArgomento(String argomento) {
        this.argomento = argomento;
    }
}
