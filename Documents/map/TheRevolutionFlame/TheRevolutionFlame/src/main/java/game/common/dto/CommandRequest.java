package game.common.dto;

public class CommandRequest {

    private int idGiocatore;
    private String azione;
    private String argomento;

    // Costruttore vuoto per Jackson
    public CommandRequest() {
    }

    public CommandRequest(int idGiocatore, String azione, String argomento) {
        this.idGiocatore = idGiocatore;
        this.azione = azione;
        this.argomento = argomento;
    }

    /**
     * Ricostruisce la stringa di comando completa per la regex del GameEngine.
     */
    public String getComandoCompleto() {
        if (azione == null || azione.isBlank()) {
            return (argomento != null) ? argomento.trim() : "";
        }
        if (argomento == null || argomento.isBlank()) {
            return azione.trim();
        }
        return (azione.trim() + " " + argomento.trim()).trim();
    }

    public int getIdGiocatore() { return idGiocatore; }
    public void setIdGiocatore(int idGiocatore) { this.idGiocatore = idGiocatore; }

    public String getAzione() { return azione; }
    public void setAzione(String azione) { this.azione = azione; }

    public String getArgomento() { return argomento; }
    public void setArgomento(String argomento) { this.argomento = argomento; }
}