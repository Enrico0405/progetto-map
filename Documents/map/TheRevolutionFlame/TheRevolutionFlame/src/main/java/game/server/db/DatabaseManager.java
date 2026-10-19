package game.server.db;

import game.server.model.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:h2:./data/game_db;DB_CLOSE_DELAY=-1";
    private static final String USER = "sa";
    private static final String PASS = "";

    private Connection connection;

    // ==========================================
    // 1. CONNESSIONE E SCRIPT CONFIGURAZIONE
    // ==========================================

    public void connetti() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
        }
    }

    public void disconnetti() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Esegue uno script SQL presente nei resources (es. "/db/schema.sql" o "/db/init_data.sql").
     */
    public void eseguiScriptConfigurazione(String resourcePath) throws SQLException {
        InputStream is = getClass().getResourceAsStream(resourcePath);
        if (is == null) {
            throw new IllegalArgumentException("File SQL non trovato nel classpath: " + resourcePath);
        }
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String sqlScript = reader.lines().collect(Collectors.joining("\n"));

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sqlScript);
        }
    }

    /**
     * Inizializza il database eseguendo in sequenza lo schema e i dati iniziali.
     */
    public void inizializzaDatabase() throws SQLException {
        eseguiScriptConfigurazione("/db/schema.sql");
        eseguiScriptConfigurazione("/db/init_data.sql");
    }

    // ==========================================
    // 2. CREAZIONE NUOVA PARTITA
    // ==========================================

    public GameState creaNuovaPartita(String nomeGiocatore, int idStanzaIniziale) throws SQLException {
        try {
            connection.setAutoCommit(false);

            eliminaGiocatoreSeEsiste(nomeGiocatore);

            int idGiocatore = creaGiocatore(nomeGiocatore, idStanzaIniziale);
            int idPartita = (int) (System.currentTimeMillis() / 1000);
            creaPartita(idPartita, idGiocatore);

            inizializzaNemiciPartita(idPartita);
            inizializzaStanzePartita(idPartita);

            connection.commit();

            return caricaGameState(idGiocatore);

        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    public GameState creaNuovaPartita(int idGiocatore) throws SQLException {
        try {
            connection.setAutoCommit(false);

            eliminaSalvataggio(idGiocatore);

            int idPartita = (int) (System.currentTimeMillis() / 1000);
            creaPartita(idPartita, idGiocatore);

            inizializzaNemiciPartita(idPartita);
            inizializzaStanzePartita(idPartita);

            String updateStanza = "UPDATE GIOCATORE SET id_stanza = 1, hp_attuali = 100 WHERE id_giocatore = ?";
            try (PreparedStatement pstm = connection.prepareStatement(updateStanza)) {
                pstm.setInt(1, idGiocatore);
                pstm.executeUpdate();
            }

            connection.commit();
            return caricaGameState(idGiocatore);

        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    private int creaGiocatore(String nomeGiocatore, int idStanzaIniziale) throws SQLException {
        String insertGiocatore = "INSERT INTO GIOCATORE (nome, hp_attuali, hp_massimi, id_stanza) VALUES (?, 100, 100, ?)";
        try (PreparedStatement pstm = connection.prepareStatement(insertGiocatore, Statement.RETURN_GENERATED_KEYS)) {
            pstm.setString(1, nomeGiocatore);
            pstm.setInt(2, idStanzaIniziale);
            pstm.executeUpdate();
            try (ResultSet rs = pstm.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private void creaPartita(int idPartita, int idGiocatore) throws SQLException {
        String insertPartita = "INSERT INTO PARTITA (id_partita, id_giocatore) VALUES (?, ?)";
        try (PreparedStatement pstm = connection.prepareStatement(insertPartita)) {
            pstm.setInt(1, idPartita);
            pstm.setInt(2, idGiocatore);
            pstm.executeUpdate();
        }
    }

    private void inizializzaNemiciPartita(int idPartita) throws SQLException {
        String query = "INSERT INTO STANZA_PARTITA_NEMICO (id_stanza, id_partita, id_nemico, hp_correnti) " +
                       "SELECT s.id_stanza, ?, n.id_nemico, n.max_hp " +
                       "FROM STANZA s JOIN NEMICO n ON s.id_nemico_presente = n.id_nemico";
        try (PreparedStatement pstm = connection.prepareStatement(query)) {
            pstm.setInt(1, idPartita);
            pstm.executeUpdate();
        }
    }

    private void inizializzaStanzePartita(int idPartita) throws SQLException {
        String queryStanze = "INSERT INTO STANZA_PARTITA (id_stanza, id_partita) " +
                             "SELECT id_stanza, ? FROM STANZA";
        try (PreparedStatement pstm = connection.prepareStatement(queryStanze)) {
            pstm.setInt(1, idPartita);
            pstm.executeUpdate();
        }

        String queryItem = "INSERT INTO STANZA_PARTITA_ITEM (id_stanza, id_partita, id_item) " +
                           "SELECT id_stanza, ?, id_item FROM STANZA_ITEM_BASE";
        try (PreparedStatement pstm = connection.prepareStatement(queryItem)) {
            pstm.setInt(1, idPartita);
            pstm.executeUpdate();
        }
    }

    // ==========================================
    // 3. CARICAMENTO GAMESTATE
    // ==========================================

    public GameState caricaGameState(int idGiocatore) throws SQLException {
        int idPartita = getIdPartitaDaGiocatore(idGiocatore);

        Giocatore giocatore = caricaDatiGiocatore(idGiocatore);

        Map<Integer, Stanza> mappaRAM = caricaStanzePartita(idPartita);
        caricaNemiciPartita(idPartita, mappaRAM);
        caricaAdiacenzeStanze(mappaRAM);

        int idStanzaCorrente = getIdStanzaCorrenteGiocatore(idGiocatore);
        Stanza stanzaAttuale = mappaRAM.get(idStanzaCorrente);
        giocatore.setStanzaCorrente(stanzaAttuale);

        giocatore.setInventario(caricaInventarioPartita(idPartita));

        return new GameState(idPartita, giocatore, stanzaAttuale, mappaRAM);
    }

    private Giocatore caricaDatiGiocatore(int idGiocatore) throws SQLException {
        String query = "SELECT g.*, img.percorso_file AS avatar_path " +
                       "FROM GIOCATORE g " +
                       "LEFT JOIN IMMAGINI img ON g.id_img = img.id_img " +
                       "WHERE g.id_giocatore = ?";
        try (PreparedStatement pstm = connection.prepareStatement(query)) {
            pstm.setInt(1, idGiocatore);
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) {
                    Giocatore g = new Giocatore();
                    g.setId(rs.getInt("id_giocatore"));
                    g.setNome(rs.getString("nome"));
                    g.setHp(rs.getInt("hp_attuali"));
                    g.setMaxHp(rs.getInt("hp_massimi"));
                    g.setAvatarPath(rs.getString("avatar_path"));
                    return g;
                }
            }
        }
        throw new SQLException("Giocatore non trovato per ID: " + idGiocatore);
    }

    private int getIdStanzaCorrenteGiocatore(int idGiocatore) throws SQLException {
        String query = "SELECT id_stanza FROM GIOCATORE WHERE id_giocatore = ?";
        try (PreparedStatement pstm = connection.prepareStatement(query)) {
            pstm.setInt(1, idGiocatore);
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_stanza");
                }
            }
        }
        return 1;
    }

    private Map<Integer, Stanza> caricaStanzePartita(int idPartita) throws SQLException {
        Map<Integer, Stanza> mappa = new HashMap<>();

        String queryStanze = "SELECT s.*, img.percorso_file AS bg_path FROM STANZA s " +
                             "LEFT JOIN IMMAGINI img ON s.id_img = img.id_img";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(queryStanze)) {
            while (rs.next()) {
                Stanza s = new Stanza();
                s.setId(rs.getInt("id_stanza"));
                s.setIdLivello(rs.getInt("id_livello"));
                s.setNome(rs.getString("nome"));
                s.setDescrizione(rs.getString("descrizione"));
                s.setImmagineSfondoPath(rs.getString("bg_path"));
                s.setBloccata(rs.getBoolean("bloccata"));
                s.setPericolosa(rs.getBoolean("stanza_letale"));
                s.setStanza_letale_danno(rs.getInt("stanza_letale_danno"));
                mappa.put(s.getId(), s);
            }
        }

        String queryItemStanza = "SELECT spi.id_stanza, i.*, img.percorso_file AS icon_path " +
                                 "FROM ITEM i " +
                                 "JOIN STANZA_PARTITA_ITEM spi ON i.id_item = spi.id_item " +
                                 "LEFT JOIN IMMAGINI img ON i.id_img = img.id_img " +
                                 "WHERE spi.id_partita = ?";
        try (PreparedStatement pstm = connection.prepareStatement(queryItemStanza)) {
            pstm.setInt(1, idPartita);
            try (ResultSet rs = pstm.executeQuery()) {
                while (rs.next()) {
                    int idStanza = rs.getInt("id_stanza");
                    Item item = mappaItem(rs);
                    if (mappa.containsKey(idStanza)) {
                        mappa.get(idStanza).aggiungiOggetto(item);
                    }
                }
            }
        }

        String queryItemRichiesti = "SELECT sir.id_stanza, i.*, img.percorso_file AS icon_path " +
                                    "FROM ITEM i " +
                                    "JOIN STANZA_ITEM_RICHIESTO sir ON i.id_item = sir.id_item " +
                                    "LEFT JOIN IMMAGINI img ON i.id_img = img.id_img";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(queryItemRichiesti)) {
            while (rs.next()) {
                int idStanza = rs.getInt("id_stanza");
                Item item = mappaItem(rs);
                if (mappa.containsKey(idStanza)) {
                    mappa.get(idStanza).aggiungiOggettoRichiestoSblocco(item);
                }
            }
        }

        return mappa;
    }

    private void caricaNemiciPartita(int idPartita, Map<Integer, Stanza> mappaRAM) throws SQLException {
        String query = "SELECT spn.id_stanza, spn.hp_correnti, n.*, img.percorso_file AS sprite_path " +
                       "FROM STANZA_PARTITA_NEMICO spn " +
                       "JOIN NEMICO n ON spn.id_nemico = n.id_nemico " +
                       "LEFT JOIN IMMAGINI img ON n.id_img = img.id_img " +
                       "WHERE spn.id_partita = ?";
        try (PreparedStatement pstm = connection.prepareStatement(query)) {
            pstm.setInt(1, idPartita);
            try (ResultSet rs = pstm.executeQuery()) {
                while (rs.next()) {
                    int idStanza = rs.getInt("id_stanza");
                    Nemico nemico = new Nemico();
                    nemico.setId(rs.getInt("id_nemico"));
                    nemico.setNome(rs.getString("nome"));
                    nemico.setHp(rs.getInt("hp_correnti"));
                    nemico.setMaxHp(rs.getInt("max_hp"));
                    nemico.setDannoAttacco(rs.getInt("danno_attacco"));
                    nemico.setPrecisione(rs.getDouble("precisione"));
                    nemico.setSpritePath(rs.getString("sprite_path"));
                    nemico.setMessaggioAgguato(rs.getString("messaggio_agguato"));
                    
                    String tipoStr = rs.getString("tipo");
                    if (tipoStr != null) {
                        nemico.setTipo(TipoNemico.valueOf(tipoStr.toUpperCase()));
                    }

                    if (mappaRAM.containsKey(idStanza)) {
                        mappaRAM.get(idStanza).setNemicoPresente(nemico);
                    }
                }
            }
        }
    }

    private void caricaAdiacenzeStanze(Map<Integer, Stanza> mappaRAM) throws SQLException {
        String query = "SELECT stanza_partenza, stanza_arrivo, verso_comando FROM ADIACENZE_STANZA";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                int idPartenza = rs.getInt("stanza_partenza");
                int idArrivo = rs.getInt("stanza_arrivo");
                String direzione = rs.getString("verso_comando");

                Stanza sPartenza = mappaRAM.get(idPartenza);
                Stanza sArrivo = mappaRAM.get(idArrivo);

                if (sPartenza != null && sArrivo != null) {
                    sPartenza.setAdiacenza(direzione, sArrivo);
                }
            }
        }
    }

    private List<Item> caricaInventarioPartita(int idPartita) throws SQLException {
        List<Item> inventario = new ArrayList<>();
        String query = "SELECT i.*, img.percorso_file AS icon_path FROM ITEM i " +
                       "JOIN ITEM_PARTITA ip ON i.id_item = ip.id_item " +
                       "LEFT JOIN IMMAGINI img ON i.id_img = img.id_img " +
                       "WHERE ip.id_partita = ?";
        try (PreparedStatement pstm = connection.prepareStatement(query)) {
            pstm.setInt(1, idPartita);
            try (ResultSet rs = pstm.executeQuery()) {
                while (rs.next()) {
                    inventario.add(mappaItem(rs));
                }
            }
        }
        return inventario;
    }

    // ==========================================
    // 4. SALVATAGGIO GAMESTATE
    // ==========================================

    public boolean salvaGameState(GameState state) {
        try {
            connection.setAutoCommit(false);

            int idGiocatore = state.getPlayer().getId();
            int idPartita = state.getIdGiocatore();

            salvaDatiGiocatore(idGiocatore, state);
            salvaInventarioPartita(idPartita, state.getPlayer().getInventario());
            salvaStanzePartita(idPartita, state.getMappaStanzeIstanziata());
            salvaNemiciPartita(idPartita, state.getMappaStanzeIstanziata());

            connection.commit();
            return true;

        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return false;
        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
        }
    }

    private void salvaDatiGiocatore(int idGiocatore, GameState state) throws SQLException {
        String updateGiocatore = "UPDATE GIOCATORE SET hp_attuali = ?, hp_massimi = ?, id_stanza = ? WHERE id_giocatore = ?";
        try (PreparedStatement pstm = connection.prepareStatement(updateGiocatore)) {
            pstm.setInt(1, state.getPlayer().getHp());
            pstm.setInt(2, state.getPlayer().getMaxHp());
            pstm.setInt(3, state.getStanzaAttuale().getId());
            pstm.setInt(4, idGiocatore);
            pstm.executeUpdate();
        }
    }

    private void salvaInventarioPartita(int idPartita, List<Item> inventario) throws SQLException {
        String deleteInv = "DELETE FROM ITEM_PARTITA WHERE id_partita = ?";
        try (PreparedStatement pstm = connection.prepareStatement(deleteInv)) {
            pstm.setInt(1, idPartita);
            pstm.executeUpdate();
        }

        String insertInv = "INSERT INTO ITEM_PARTITA (id_partita, id_item) VALUES (?, ?)";
        try (PreparedStatement pstm = connection.prepareStatement(insertInv)) {
            if (inventario != null) {
                for (Item item : inventario) {
                    pstm.setInt(1, idPartita);
                    pstm.setInt(2, item.getId());
                    pstm.executeUpdate();
                }
            }
        }
    }

    private void salvaStanzePartita(int idPartita, Map<Integer, Stanza> mappaStanze) throws SQLException {
        String deleteStanzeItems = "DELETE FROM STANZA_PARTITA_ITEM WHERE id_partita = ?";
        try (PreparedStatement pstm = connection.prepareStatement(deleteStanzeItems)) {
            pstm.setInt(1, idPartita);
            pstm.executeUpdate();
        }

        String insertStanzaItem = "INSERT INTO STANZA_PARTITA_ITEM (id_stanza, id_partita, id_item) VALUES (?, ?, ?)";
        try (PreparedStatement pstm = connection.prepareStatement(insertStanzaItem)) {
            if (mappaStanze != null) {
                for (Stanza stanza : mappaStanze.values()) {
                    if (stanza.getOggettiNellaStanza() != null) {
                        for (Item item : stanza.getOggettiNellaStanza()) {
                            pstm.setInt(1, stanza.getId());
                            pstm.setInt(2, idPartita);
                            pstm.setInt(3, item.getId());
                            pstm.executeUpdate();
                        }
                    }
                }
            }
        }
    }

    private void salvaNemiciPartita(int idPartita, Map<Integer, Stanza> mappaStanze) throws SQLException {
        String updateNemico = "MERGE INTO STANZA_PARTITA_NEMICO (id_stanza, id_partita, id_nemico, hp_correnti) " +
                              "KEY(id_stanza, id_partita, id_nemico) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstm = connection.prepareStatement(updateNemico)) {
            if (mappaStanze != null) {
                for (Stanza stanza : mappaStanze.values()) {
                    Nemico nemico = stanza.getNemicoPresente();
                    if (nemico != null) {
                        pstm.setInt(1, stanza.getId());
                        pstm.setInt(2, idPartita);
                        pstm.setInt(3, nemico.getId());
                        pstm.setInt(4, nemico.getHp());
                        pstm.executeUpdate();
                    }
                }
            }
        }
    }

    // ==========================================
    // 5. UTILITY E AUTENTICAZIONE
    // ==========================================

    
    /**
 * Recupera il percorso dell'immagine della mappa di un livello a partire dal suo id_livello.
 *
 * @param idLivello ID del livello (es. 1, 2, 3)
 * @return String percorso del file della mappa, oppure null se non trovato
 */
public String getPercorsoMappaLivello(int idLivello) {
    String query = "SELECT img.percorso_file " +
                   "FROM LIVELLO l " +
                   "JOIN IMMAGINI img ON l.id_img = img.id_img " +
                   "WHERE l.id_livello = ?";
    try (PreparedStatement pstm = connection.prepareStatement(query)) {
        pstm.setInt(1, idLivello);
        try (ResultSet rs = pstm.executeQuery()) {
            if (rs.next()) {
                return rs.getString("percorso_file");
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return null;
}
    public boolean esisteSalvataggio(int idGiocatore) {
        String query = "SELECT COUNT(*) FROM PARTITA WHERE id_giocatore = ?";
        try (PreparedStatement pstm = connection.prepareStatement(query)) {
            pstm.setInt(1, idGiocatore);
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int creaNuovoPlayer() {
        String nomeTemp = "Player_" + System.currentTimeMillis();
        String insert = "INSERT INTO GIOCATORE (nome, hp_attuali, hp_massimi, id_stanza) VALUES (?, 100, 100, 1)";
        try (PreparedStatement pstm = connection.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
            pstm.setString(1, nomeTemp);
            pstm.executeUpdate();
            try (ResultSet rs = pstm.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean eliminaSalvataggio(int idGiocatore) {
        String query = "DELETE FROM PARTITA WHERE id_giocatore = ?";
        try (PreparedStatement pstm = connection.prepareStatement(query)) {
            pstm.setInt(1, idGiocatore);
            return pstm.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int getIdPartitaDaGiocatore(int idGiocatore) throws SQLException {
        String query = "SELECT id_partita FROM PARTITA WHERE id_giocatore = ?";
        try (PreparedStatement pstm = connection.prepareStatement(query)) {
            pstm.setInt(1, idGiocatore);
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_partita");
                }
            }
        }
        throw new SQLException("Partita non trovata per id_giocatore: " + idGiocatore);
    }

    private void eliminaGiocatoreSeEsiste(String nomeGiocatore) throws SQLException {
        String query = "DELETE FROM GIOCATORE WHERE nome = ?";
        try (PreparedStatement pstm = connection.prepareStatement(query)) {
            pstm.setString(1, nomeGiocatore);
            pstm.executeUpdate();
        }
    }

    private Item mappaItem(ResultSet rs) throws SQLException {
        Item item = new Item();
        item.setId(rs.getInt("id_item"));
        item.setNome(rs.getString("nome"));
        item.setDescrizione(rs.getString("descrizione"));
        item.setIconaPath(rs.getString("icon_path"));
        item.setValoreEffetto(rs.getInt("valore_effetto"));
        item.setUtilizzabileInScontro(rs.getBoolean("utilizzabile_in_scontro"));

        String tipoStr = rs.getString("tipo");
        if (tipoStr != null) {
            item.setTipo(TipoItem.valueOf(tipoStr.toUpperCase()));
        }

        return item;
    }
}