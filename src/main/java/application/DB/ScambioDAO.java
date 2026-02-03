package application.DB;

import application.Classe.Annuncio;
import application.Classe.Scambio;
import application.Enum.StatoScambio;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO per la gestione degli scambi tra utenti.
 * Gestisce proposta, accettazione, rifiuto e completamento degli scambi.
 *
 * @author SwapUnina Development Team
 * @version 1.0
 */
public class ScambioDAO {

    private static final Logger LOGGER = Logger.getLogger(ScambioDAO.class.getName());
    private static final String TABLE_NAME = "scambio";

    /**
     * Crea la tabella scambio se non esiste
     */
    public void creaTabellaSeMancante() {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                "id SERIAL PRIMARY KEY, " +
                "richiedente_id INTEGER NOT NULL, " +
                "annuncio_richiesto_id INTEGER NOT NULL, " +
                "annuncio_offerto_id INTEGER, " +
                "stato VARCHAR(20) DEFAULT 'IN_ATTESA', " +
                "data_proposta TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "data_accettazione TIMESTAMP, " +
                "data_completamento TIMESTAMP, " +
                "messaggio TEXT, " +
                "FOREIGN KEY (richiedente_id) REFERENCES utente(id), " +
                "FOREIGN KEY (annuncio_richiesto_id) REFERENCES annuncio(id), " +
                "FOREIGN KEY (annuncio_offerto_id) REFERENCES annuncio(id)" +
                ")";

        try (Connection conn = ConnessioneDB.getConnessione();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            LOGGER.log(Level.INFO, "✅ Tabella scambio verificata/creata");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "❌ Errore creazione tabella scambio: {0}", e.getMessage());
        }
    }

    /**
     * Propone uno scambio
     *
     * @param richiedenteId ID dell'utente che propone lo scambio
     * @param annuncioRichiestoId ID dell'annuncio che vuole ottenere
     * @param annuncioOffertoId ID dell'annuncio che offre (null per scambio diretto senza contropartita)
     * @param messaggio Messaggio opzionale per il venditore
     * @return ID dello scambio creato, -1 se fallisce
     */
    public int proponiScambio(int richiedenteId, int annuncioRichiestoId, Integer annuncioOffertoId, String messaggio) {
        String sql = "INSERT INTO " + TABLE_NAME +
                " (richiedente_id, annuncio_richiesto_id, annuncio_offerto_id, stato, messaggio) " +
                "VALUES (?, ?, ?, 'IN_ATTESA', ?) RETURNING id";

        try (Connection conn = ConnessioneDB.getConnessione();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, richiedenteId);
            stmt.setInt(2, annuncioRichiestoId);

            if (annuncioOffertoId != null) {
                stmt.setInt(3, annuncioOffertoId);
            } else {
                stmt.setNull(3, Types.INTEGER);
            }

            if (messaggio != null && !messaggio.trim().isEmpty()) {
                stmt.setString(4, messaggio);
            } else {
                stmt.setNull(4, Types.VARCHAR);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int scambioId = rs.getInt("id");
                    LOGGER.log(Level.INFO, "✅ Scambio proposto: ID={0}, Richiedente={1}, Annunci={2}->{3}",
                            new Object[]{scambioId, richiedenteId, annuncioOffertoId, annuncioRichiestoId});
                    return scambioId;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "❌ Errore proposta scambio: {0}", e.getMessage());
        }
        return -1;
    }

    /**
     * Accetta uno scambio proposto
     *
     * @param scambioId ID dello scambio
     * @return true se l'accettazione ha successo
     */
    public boolean accettaScambio(int scambioId) {
        String sql = "UPDATE " + TABLE_NAME +
                " SET stato = 'ACCETTATO', data_accettazione = ? " +
                "WHERE id = ? AND stato = 'IN_ATTESA'";

        try (Connection conn = ConnessioneDB.getConnessione();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(2, scambioId);

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                LOGGER.log(Level.INFO, "✅ Scambio accettato: ID={0}", scambioId);
                return true;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "❌ Errore accettazione scambio {0}: {1}", new Object[]{scambioId, e.getMessage()});
        }
        return false;
    }

    /**
     * Rifiuta uno scambio proposto
     *
     * @param scambioId ID dello scambio
     * @return true se il rifiuto ha successo
     */
    public boolean rifiutaScambio(int scambioId) {
        String sql = "UPDATE " + TABLE_NAME +
                " SET stato = 'RIFIUTATO' " +
                "WHERE id = ? AND stato = 'IN_ATTESA'";

        try (Connection conn = ConnessioneDB.getConnessione();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, scambioId);

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                LOGGER.log(Level.INFO, "✅ Scambio rifiutato: ID={0}", scambioId);
                return true;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "❌ Errore rifiuto scambio {0}: {1}", new Object[]{scambioId, e.getMessage()});
        }
        return false;
    }

    /**
     * Segna lo scambio come completato
     *
     * @param scambioId ID dello scambio
     * @return true se il completamento ha successo
     */
    public boolean completaScambio(int scambioId) {
        String sql = "UPDATE " + TABLE_NAME +
                " SET stato = 'COMPLETATO', data_completamento = ? " +
                "WHERE id = ? AND stato = 'ACCETTATO'";

        try (Connection conn = ConnessioneDB.getConnessione();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(2, scambioId);

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                LOGGER.log(Level.INFO, "✅ Scambio completato: ID={0}", scambioId);

                // Aggiorna lo stato degli annunci coinvolti
                aggiornaStatoAnnunciScambio(scambioId);

                return true;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "❌ Errore completamento scambio {0}: {1}", new Object[]{scambioId, e.getMessage()});
        }
        return false;
    }

    /**
     * Annulla uno scambio (solo se non è ancora stato accettato)
     *
     * @param scambioId ID dello scambio
     * @return true se l'annullamento ha successo
     */
    public boolean annullaScambio(int scambioId) {
        String sql = "UPDATE " + TABLE_NAME +
                " SET stato = 'ANNULLATO' " +
                "WHERE id = ? AND stato = 'IN_ATTESA'";

        try (Connection conn = ConnessioneDB.getConnessione();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, scambioId);

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                LOGGER.log(Level.INFO, "✅ Scambio annullato: ID={0}", scambioId);
                return true;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "❌ Errore annullamento scambio {0}: {1}", new Object[]{scambioId, e.getMessage()});
        }
        return false;
    }

    /**
     * Recupera tutti gli scambi di un utente (come richiedente o come proprietario dell'annuncio richiesto)
     *
     * @param utenteId ID dell'utente
     * @return Lista degli scambi
     */
    public List<Scambio> getScambiPerUtente(int utenteId) {
        List<Scambio> scambi = new ArrayList<>();

        String sql = "SELECT s.*, " +
                "a1.titolo as titolo_richiesto, " +
                "a2.titolo as titolo_offerto, " +
                "u1.nome as nome_richiedente, " +
                "u2.nome as nome_proprietario " +
                "FROM " + TABLE_NAME + " s " +
                "LEFT JOIN annuncio a1 ON s.annuncio_richiesto_id = a1.id " +
                "LEFT JOIN annuncio a2 ON s.annuncio_offerto_id = a2.id " +
                "LEFT JOIN utente u1 ON s.richiedente_id = u1.id " +
                "LEFT JOIN utente u2 ON a1.venditore_id = u2.id " +
                "WHERE s.richiedente_id = ? OR a1.venditore_id = ? " +
                "ORDER BY s.data_proposta DESC";

        try (Connection conn = ConnessioneDB.getConnessione();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, utenteId);
            stmt.setInt(2, utenteId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    scambi.add(mapResultSetToScambio(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "❌ Errore recupero scambi utente {0}: {1}", new Object[]{utenteId, e.getMessage()});
        }
        return scambi;
    }

    /**
     * Recupera gli scambi in attesa per un annuncio specifico
     *
     * @param annuncioId ID dell'annuncio
     * @return Lista degli scambi in attesa
     */
    public List<Scambio> getScambiInAttesaPerAnnuncio(int annuncioId) {
        List<Scambio> scambi = new ArrayList<>();

        String sql = "SELECT s.*, " +
                "a1.titolo as titolo_richiesto, " +
                "a2.titolo as titolo_offerto, " +
                "u1.nome as nome_richiedente " +
                "FROM " + TABLE_NAME + " s " +
                "LEFT JOIN annuncio a1 ON s.annuncio_richiesto_id = a1.id " +
                "LEFT JOIN annuncio a2 ON s.annuncio_offerto_id = a2.id " +
                "LEFT JOIN utente u1 ON s.richiedente_id = u1.id " +
                "WHERE s.annuncio_richiesto_id = ? AND s.stato = 'IN_ATTESA' " +
                "ORDER BY s.data_proposta DESC";

        try (Connection conn = ConnessioneDB.getConnessione();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, annuncioId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    scambi.add(mapResultSetToScambio(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "❌ Errore recupero scambi in attesa per annuncio {0}: {1}",
                    new Object[]{annuncioId, e.getMessage()});
        }
        return scambi;
    }

    /**
     * Recupera uno scambio per ID
     *
     * @param scambioId ID dello scambio
     * @return Scambio o null se non trovato
     */
    public Scambio getScambioById(int scambioId) {
        String sql = "SELECT s.*, " +
                "a1.titolo as titolo_richiesto, " +
                "a2.titolo as titolo_offerto, " +
                "u1.nome as nome_richiedente, " +
                "u2.nome as nome_proprietario " +
                "FROM " + TABLE_NAME + " s " +
                "LEFT JOIN annuncio a1 ON s.annuncio_richiesto_id = a1.id " +
                "LEFT JOIN annuncio a2 ON s.annuncio_offerto_id = a2.id " +
                "LEFT JOIN utente u1 ON s.richiedente_id = u1.id " +
                "LEFT JOIN utente u2 ON a1.venditore_id = u2.id " +
                "WHERE s.id = ?";

        try (Connection conn = ConnessioneDB.getConnessione();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, scambioId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToScambio(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "❌ Errore recupero scambio {0}: {1}", new Object[]{scambioId, e.getMessage()});
        }
        return null;
    }

    /**
     * Verifica se l'utente può proporre uno scambio per un annuncio
     * (non può proporre scambio per i propri annunci)
     *
     * @param utenteId ID dell'utente
     * @param annuncioId ID dell'annuncio
     * @return true se può proporre lo scambio
     */
    public boolean puoProporreScambio(int utenteId, int annuncioId) {
        String sql = "SELECT venditore_id, stato FROM annuncio WHERE id = ?";

        try (Connection conn = ConnessioneDB.getConnessione();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, annuncioId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int venditoreId = rs.getInt("venditore_id");
                    String stato = rs.getString("stato");

                    // ✅ FIX: Controlla anche che l'annuncio sia attivo
                    return venditoreId != utenteId && "ATTIVO".equalsIgnoreCase(stato);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "❌ Errore verifica permessi scambio: {0}", e.getMessage());
        }
        return false;
    }

    /**
     * ✅ NUOVO METODO: Verifica se esiste già uno scambio attivo tra due utenti per gli stessi annunci
     * Previene proposte duplicate
     */
    public boolean esisteScambioAttivo(int richiedenteId, int annuncioRichiestoId, Integer annuncioOffertoId) {
        String sql = "SELECT COUNT(*) FROM " + TABLE_NAME +
                    " WHERE richiedente_id = ? " +
                    "AND annuncio_richiesto_id = ? " +
                    "AND stato IN ('IN_ATTESA', 'ACCETTATO')";

        try (Connection conn = ConnessioneDB.getConnessione();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, richiedenteId);
            stmt.setInt(2, annuncioRichiestoId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "❌ Errore verifica scambio esistente: {0}", e.getMessage());
        }
        return false;
    }

    /**
     * ✅ NUOVO METODO: Recupera gli scambi in attesa di gestione per un utente
     * (scambi dove l'utente è il venditore che deve accettare/rifiutare)
     */
    public List<Scambio> getScambiInAttesaDiGestione(int venditoreId) {
        List<Scambio> scambi = new ArrayList<>();

        String sql = "SELECT s.*, " +
                    "a1.titolo as titolo_richiesto, " +
                    "a2.titolo as titolo_offerto, " +
                    "u1.nome as nome_richiedente, u1.email as email_richiedente " +
                    "FROM " + TABLE_NAME + " s " +
                    "LEFT JOIN annuncio a1 ON s.annuncio_richiesto_id = a1.id " +
                    "LEFT JOIN annuncio a2 ON s.annuncio_offerto_id = a2.id " +
                    "LEFT JOIN utente u1 ON s.richiedente_id = u1.id " +
                    "WHERE a1.venditore_id = ? " +
                    "AND s.stato = 'IN_ATTESA' " +
                    "ORDER BY s.data_proposta ASC";

        try (Connection conn = ConnessioneDB.getConnessione();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, venditoreId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    scambi.add(mapResultSetToScambio(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "❌ Errore recupero scambi in attesa: {0}", e.getMessage());
        }
        return scambi;
    }

    /**
     * ✅ NUOVO METODO: Recupera lo storico scambi di un utente (completati, annullati, rifiutati)
     */
    public List<Scambio> getStoricoScambi(int utenteId) {
        List<Scambio> scambi = new ArrayList<>();

        String sql = "SELECT s.*, " +
                    "a1.titolo as titolo_richiesto, " +
                    "a2.titolo as titolo_offerto, " +
                    "u1.nome as nome_richiedente, " +
                    "u2.nome as nome_proprietario " +
                    "FROM " + TABLE_NAME + " s " +
                    "LEFT JOIN annuncio a1 ON s.annuncio_richiesto_id = a1.id " +
                    "LEFT JOIN annuncio a2 ON s.annuncio_offerto_id = a2.id " +
                    "LEFT JOIN utente u1 ON s.richiedente_id = u1.id " +
                    "LEFT JOIN utente u2 ON a1.venditore_id = u2.id " +
                    "WHERE (s.richiedente_id = ? OR a1.venditore_id = ?) " +
                    "AND s.stato IN ('COMPLETATO', 'ANNULLATO', 'RIFIUTATO') " +
                    "ORDER BY s.data_proposta DESC";

        try (Connection conn = ConnessioneDB.getConnessione();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, utenteId);
            stmt.setInt(2, utenteId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    scambi.add(mapResultSetToScambio(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "❌ Errore recupero storico scambi: {0}", e.getMessage());
        }
        return scambi;
    }

    /**
     * ✅ NUOVO METODO: Conta gli scambi per stato
     */
    public int contaScambiPerStato(int utenteId, String stato) {
        String sql = "SELECT COUNT(*) FROM " + TABLE_NAME + " s " +
                    "JOIN annuncio a ON s.annuncio_richiesto_id = a.id " +
                    "WHERE (s.richiedente_id = ? OR a.venditore_id = ?) " +
                    "AND s.stato = ?";

        try (Connection conn = ConnessioneDB.getConnessione();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, utenteId);
            stmt.setInt(2, utenteId);
            stmt.setString(3, stato);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "❌ Errore conteggio scambi: {0}", e.getMessage());
            return 0;
        }
    }

    /**
     * ✅ NUOVO METODO: Propone scambio con validazioni avanzate
     * Include controlli per evitare duplicati e verificare stati
     */
    public int proponiScambioConValidazione(int richiedenteId, int annuncioRichiestoId,
                                              Integer annuncioOffertoId, String messaggio) {
        // Validazione 1: Verifica permessi base
        if (!puoProporreScambio(richiedenteId, annuncioRichiestoId)) {
            LOGGER.log(Level.WARNING, "⚠️ Utente {0} non può proporre scambio per annuncio {1}",
                    new Object[]{richiedenteId, annuncioRichiestoId});
            return -1;
        }

        // Validazione 2: Verifica scambi duplicati
        if (esisteScambioAttivo(richiedenteId, annuncioRichiestoId, annuncioOffertoId)) {
            LOGGER.log(Level.WARNING, "⚠️ Esiste già uno scambio attivo per questi annunci");
            return -2;
        }

        // Validazione 3: Se offre un annuncio, verifica che sia suo e attivo
        if (annuncioOffertoId != null) {
            if (!verificaAnnuncioOfferto(richiedenteId, annuncioOffertoId)) {
                LOGGER.log(Level.WARNING, "⚠️ Annuncio offerto non valido");
                return -3;
            }
        }

        // Tutte le validazioni passate, proponi lo scambio
        return proponiScambio(richiedenteId, annuncioRichiestoId, annuncioOffertoId, messaggio);
    }

    /**
     * ✅ NUOVO METODO: Verifica che l'annuncio offerto sia dell'utente e attivo
     */
    private boolean verificaAnnuncioOfferto(int utenteId, int annuncioOffertoId) {
        String sql = "SELECT venditore_id, stato FROM annuncio WHERE id = ?";

        try (Connection conn = ConnessioneDB.getConnessione();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, annuncioOffertoId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int venditoreId = rs.getInt("venditore_id");
                    String stato = rs.getString("stato");
                    return venditoreId == utenteId && "ATTIVO".equalsIgnoreCase(stato);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "❌ Errore verifica annuncio offerto: {0}", e.getMessage());
        }
        return false;
    }

    // ========== METODI PRIVATI ==========

    /**
     * Mappa un ResultSet in un oggetto Scambio
     */
    private Scambio mapResultSetToScambio(ResultSet rs) throws SQLException {
        Scambio scambio = new Scambio();
        scambio.setId(rs.getInt("id"));
        scambio.setRichiedenteId(rs.getInt("richiedente_id"));
        scambio.setAnnuncioRichiestoId(rs.getInt("annuncio_richiesto_id"));

        int annuncioOffertoId = rs.getInt("annuncio_offerto_id");
        if (!rs.wasNull()) {
            scambio.setAnnuncioOffertoId(annuncioOffertoId);
        }

        scambio.setStato(StatoScambio.valueOf(rs.getString("stato")));
        scambio.setDataProposta(rs.getTimestamp("data_proposta").toLocalDateTime());

        Timestamp dataAccettazione = rs.getTimestamp("data_accettazione");
        if (dataAccettazione != null) {
            scambio.setDataAccettazione(dataAccettazione.toLocalDateTime());
        }

        Timestamp dataCompletamento = rs.getTimestamp("data_completamento");
        if (dataCompletamento != null) {
            scambio.setDataCompletamento(dataCompletamento.toLocalDateTime());
        }

        String messaggio = rs.getString("messaggio");
        if (messaggio != null) {
            scambio.setMessaggio(messaggio);
        }

        // Dati aggiuntivi per la visualizzazione
        try {
            scambio.setTitoloAnnuncioRichiesto(rs.getString("titolo_richiesto"));
            scambio.setTitoloAnnuncioOfferto(rs.getString("titolo_offerto"));
            scambio.setNomeRichiedente(rs.getString("nome_richiedente"));
            scambio.setNomeProprietario(rs.getString("nome_proprietario"));
        } catch (SQLException e) {
            // Questi campi potrebbero non essere sempre presenti
        }

        return scambio;
    }

    /**
     * Aggiorna lo stato degli annunci coinvolti in uno scambio completato
     */
    private void aggiornaStatoAnnunciScambio(int scambioId) {
        String sql = "UPDATE annuncio SET stato = 'VENDUTO' WHERE id IN " +
                "(SELECT annuncio_richiesto_id FROM scambio WHERE id = ?) " +
                "OR id IN (SELECT annuncio_offerto_id FROM scambio WHERE id = ? AND annuncio_offerto_id IS NOT NULL)";

        try (Connection conn = ConnessioneDB.getConnessione();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, scambioId);
            stmt.setInt(2, scambioId);
            stmt.executeUpdate();

            LOGGER.log(Level.INFO, "✅ Annunci aggiornati per scambio completato: ID={0}", scambioId);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "❌ Errore aggiornamento stati annunci: {0}", e.getMessage());
        }
    }
}
