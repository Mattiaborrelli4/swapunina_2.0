package application.DB;

import application.Classe.Offerta;
import application.Classe.OffertaNegoziazione;
import application.notifications.NotificationManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO per la gestione delle offerte e negoziazioni
 * Supporta offerte iniziali, contro-offerte e gestione stati
 */
public class OffertaDAO {
    private static final String TABLE_NAME = "offerta";
    private final NotificationManager notificationManager;

    public OffertaDAO() {
        this.notificationManager = NotificationManager.getInstance();
        creaTabellaSeMancante();
    }

    /**
     * Crea la tabella delle offerte se non esiste
     */
    private void creaTabellaSeMancante() {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                "id SERIAL PRIMARY KEY, " +
                "annuncio_id INTEGER NOT NULL REFERENCES annuncio(id) ON DELETE CASCADE, " +
                "offerente_id INTEGER NOT NULL REFERENCES utente(id) ON DELETE CASCADE, " +
                "venditore_id INTEGER NOT NULL REFERENCES utente(id) ON DELETE CASCADE, " +
                "importo NUMERIC(10, 2) NOT NULL, " +
                "messaggio TEXT, " +
                "stato VARCHAR(20) NOT NULL DEFAULT 'IN_ATTESA', " +
                "tipo VARCHAR(20) NOT NULL DEFAULT 'INIZIALE', " +
                "data_creazione TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "data_risposta TIMESTAMP, " +
                "offerta_padre_id INTEGER REFERENCES " + TABLE_NAME + "(id) ON DELETE SET NULL, " +
                "notificata BOOLEAN DEFAULT FALSE, " +
                "CHECK (importo > 0), " +
                "CHECK (stato IN ('IN_ATTESA', 'ACCETTATA', 'RIFIUTATA', 'RITIRATA', 'SCADUTA', 'CONTROFFERTA')), " +
                "CHECK (tipo IN ('INIZIALE', 'CONTRO_OFFERTA'))" +
                ")";

        try (Connection conn = ConnessioneDB.getConnessione();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("✅ Tabella offerta creata/verificata");
        } catch (SQLException e) {
            System.err.println("❌ Errore creazione tabella offerta: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Crea una nuova offerta di negoziazione
     */
    public int creaOfferta(OffertaNegoziazione offerta) {
        String sql = "INSERT INTO " + TABLE_NAME +
                     " (annuncio_id, offerente_id, venditore_id, importo, messaggio, stato, tipo, data_creazione, offerta_padre_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                     "RETURNING id";

        try (Connection conn = ConnessioneDB.getConnessione();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, offerta.getAnnuncioId());
            stmt.setInt(2, offerta.getOfferenteId());
            stmt.setInt(3, offerta.getVenditoreId());
            stmt.setDouble(4, offerta.getImporto());
            stmt.setString(5, offerta.getMessaggio());
            stmt.setString(6, offerta.getStato().name());
            stmt.setString(7, offerta.getTipo().name());
            stmt.setTimestamp(8, Timestamp.valueOf(offerta.getDataCreazione()));

            if (offerta.getOffertaPadreId() != null) {
                stmt.setInt(9, offerta.getOffertaPadreId());
            } else {
                stmt.setNull(9, Types.INTEGER);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int offertaId = rs.getInt("id");
                    offerta.setId(offertaId);

                    // Notifica il venditore della nuova offerta
                    notificationManager.notificaNuovaOfferta(
                            "Offerta ricevuta",
                            "Nuova offerta di " + offerta.getImportoFormattato(),
                            offerta.getImporto()
                    );

                    System.out.println("✅ OffertaNegoziazione creata con ID: " + offertaId);
                    return offertaId;
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Errore creazione offerta: " + e.getMessage());
            e.printStackTrace();
        }

        return -1;
    }

    /**
     * Crea un'offerta semplice (per compatibilità con il codice esistente)
     */
    public int creaOfferta(Offerta offerta, int venditoreId) {
        OffertaNegoziazione negoziazione = OffertaNegoziazione.fromOfferta(offerta);
        negoziazione.setVenditoreId(venditoreId);
        return creaOfferta(negoziazione);
    }

    /**
     * Accetta un'offerta
     */
    public boolean accettaOfferta(int offertaId) {
        return aggiornaStatoOfferta(offertaId, OffertaNegoziazione.StatoOfferta.ACCETTATA);
    }

    /**
     * Rifiuta un'offerta
     */
    public boolean rifiutaOfferta(int offertaId) {
        return aggiornaStatoOfferta(offertaId, OffertaNegoziazione.StatoOfferta.RIFIUTATA);
    }

    /**
     * Ritira un'offerta (solo l'offerente può farlo)
     */
    public boolean ritiraOfferta(int offertaId) {
        return aggiornaStatoOfferta(offertaId, OffertaNegoziazione.StatoOfferta.RITIRATA);
    }

    /**
     * Aggiorna lo stato di un'offerta
     */
    private boolean aggiornaStatoOfferta(int offertaId, OffertaNegoziazione.StatoOfferta nuovoStato) {
        String sql = "UPDATE " + TABLE_NAME +
                     " SET stato = ?, data_risposta = CURRENT_TIMESTAMP " +
                     "WHERE id = ?";

        try (Connection conn = ConnessioneDB.getConnessione();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nuovoStato.name());
            stmt.setInt(2, offertaId);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ Offerta " + offertaId + " aggiornata a: " + nuovoStato);

                // Notifica dell'aggiornamento
                notificationManager.notificaAggiornamentoTransazione(
                        nuovoStato.name(),
                        "La tua offerta è stata " + nuovoStato.name().toLowerCase()
                );

                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Errore aggiornamento offerta: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Crea una contro-offerta
     */
    public int creaControfferta(int offertaPadreId, double nuovoImporto, String messaggio) {
        // Prima recupera l'offerta padre
        OffertaNegoziazione offertaPadre = getOffertaNegoziazioneById(offertaPadreId);
        if (offertaPadre == null) {
            System.err.println("⚠️ Offerta padre non trovata: " + offertaPadreId);
            return -1;
        }

        // Aggiorna lo stato dell'offerta padre
        aggiornaStatoOfferta(offertaPadreId, OffertaNegoziazione.StatoOfferta.CONTROFFERTA);

        // Crea la contro-offerta
        OffertaNegoziazione controfferta = offertaPadre.creaControfferta(nuovoImporto, messaggio);
        return creaOfferta(controfferta);
    }

    /**
     * Recupera un'offerta di negoziazione per ID
     */
    public OffertaNegoziazione getOffertaNegoziazioneById(int offertaId) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE id = ?";

        try (Connection conn = ConnessioneDB.getConnessione();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, offertaId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToOffertaNegoziazione(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Errore recupero offerta: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Recupera tutte le offerte per un annuncio
     */
    public List<OffertaNegoziazione> getOffertePerAnnuncio(int annuncioId) {
        List<OffertaNegoziazione> offerte = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE annuncio_id = ? ORDER BY data_creazione DESC";

        try (Connection conn = ConnessioneDB.getConnessione();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, annuncioId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    offerte.add(mapResultSetToOffertaNegoziazione(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Errore recupero offerte annuncio: " + e.getMessage());
            e.printStackTrace();
        }

        return offerte;
    }

    /**
     * Recupera le offerte ricevute da un venditore
     */
    public List<OffertaNegoziazione> getOfferteRicevute(int venditoreId) {
        List<OffertaNegoziazione> offerte = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_NAME +
                     " WHERE venditore_id = ? AND stato = 'IN_ATTESA' " +
                     "ORDER BY data_creazione DESC";

        try (Connection conn = ConnessioneDB.getConnessione();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, venditoreId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    offerte.add(mapResultSetToOffertaNegoziazione(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Errore recupero offerte ricevute: " + e.getMessage());
            e.printStackTrace();
        }

        return offerte;
    }

    /**
     * Recupera le offerte fatte da un utente
     */
    public List<OffertaNegoziazione> getOfferteFatte(int offerenteId) {
        List<OffertaNegoziazione> offerte = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_NAME +
                     " WHERE offerente_id = ? " +
                     "ORDER BY data_creazione DESC";

        try (Connection conn = ConnessioneDB.getConnessione();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, offerenteId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    offerte.add(mapResultSetToOffertaNegoziazione(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Errore recupero offerte fatte: " + e.getMessage());
            e.printStackTrace();
        }

        return offerte;
    }

    /**
     * Recupera lo storico negoziazioni per un annuncio (incluse contro-offerte)
     */
    public List<OffertaNegoziazione> getStoricoNegoziazione(int annuncioId, int utente1Id, int utente2Id) {
        List<OffertaNegoziazione> offerte = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_NAME +
                     " WHERE annuncio_id = ? " +
                     "AND ((offerente_id = ? AND venditore_id = ?) " +
                     "OR (offerente_id = ? AND venditore_id = ?)) " +
                     "ORDER BY data_creazione ASC";

        try (Connection conn = ConnessioneDB.getConnessione();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, annuncioId);
            stmt.setInt(2, utente1Id);
            stmt.setInt(3, utente2Id);
            stmt.setInt(4, utente2Id);
            stmt.setInt(5, utente1Id);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    offerte.add(mapResultSetToOffertaNegoziazione(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Errore recupero storico negoziazione: " + e.getMessage());
            e.printStackTrace();
        }

        return offerte;
    }

    /**
     * Segna le offerte scadute (più di 7 giorni in attesa)
     */
    public int segnaOfferteScadute() {
        String sql = "UPDATE " + TABLE_NAME +
                     " SET stato = 'SCADUTA' " +
                     "WHERE stato = 'IN_ATTESA' " +
                     "AND data_creazione < CURRENT_TIMESTAMP - INTERVAL '7 days'";

        try (Connection conn = ConnessioneDB.getConnessione();
             Statement stmt = conn.createStatement()) {

            int rowsAffected = stmt.executeUpdate(sql);

            if (rowsAffected > 0) {
                System.out.println("✅ Segnate come scadute " + rowsAffected + " offerte");
            }

            return rowsAffected;

        } catch (SQLException e) {
            System.err.println("❌ Errore segnalazione scadenze: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Verifica se un utente ha un'offerta attiva su un annuncio
     */
    public boolean haOffertaAttiva(int annuncioId, int offerenteId) {
        String sql = "SELECT COUNT(*) FROM " + TABLE_NAME +
                     " WHERE annuncio_id = ? AND offerente_id = ? " +
                     "AND stato IN ('IN_ATTESA', 'CONTROFFERTA')";

        try (Connection conn = ConnessioneDB.getConnessione();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, annuncioId);
            stmt.setInt(2, offerenteId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Errore verifica offerta attiva: " + e.getMessage());
        }

        return false;
    }

    /**
     * Mappa un ResultSet a un oggetto OffertaNegoziazione
     */
    private OffertaNegoziazione mapResultSetToOffertaNegoziazione(ResultSet rs) throws SQLException {
        OffertaNegoziazione offerta = new OffertaNegoziazione();
        offerta.setId(rs.getInt("id"));
        offerta.setAnnuncioId(rs.getInt("annuncio_id"));
        offerta.setOfferenteId(rs.getInt("offerente_id"));
        offerta.setVenditoreId(rs.getInt("venditore_id"));
        offerta.setImporto(rs.getDouble("importo"));
        offerta.setMessaggio(rs.getString("messaggio"));

        String statoStr = rs.getString("stato");
        offerta.setStato(OffertaNegoziazione.StatoOfferta.valueOf(statoStr));

        String tipoStr = rs.getString("tipo");
        offerta.setTipo(OffertaNegoziazione.TipoOfferta.valueOf(tipoStr));

        offerta.setDataCreazione(rs.getTimestamp("data_creazione").toLocalDateTime());

        Timestamp dataRisposta = rs.getTimestamp("data_risposta");
        if (dataRisposta != null) {
            offerta.setDataRisposta(dataRisposta.toLocalDateTime());
        }

        int offertaPadreId = rs.getInt("offerta_padre_id");
        if (!rs.wasNull()) {
            offerta.setOffertaPadreId(offertaPadreId);
        }

        offerta.setNotificata(rs.getBoolean("notificata"));

        return offerta;
    }
}
