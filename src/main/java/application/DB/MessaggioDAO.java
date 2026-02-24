package application.DB;

import application.Classe.Messaggio;
import application.Classe.utente;
import application.messagistica.MessageEncryptionService;
import application.messagistica.MessageEncryptionService.EncryptedMessageContainer;

import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object per la gestione dei messaggi con crittografia AES-GCM completa
 *
 * <p><b>Caratteristiche:</b>
 * <ul>
 *   <li>Crittografia AES-256 GCM per tutti i messaggi</li>
 *   <li>IV univoco per ogni messaggio</li>
 *   <li>Backup plaintext rimosso (migliore sicurezza)</li>
 *   <li>Supporto conversazioni per annuncio</li>
 *   <li>Decifratura automatica alla lettura</li>
 * </ul>
 * </p>
 *
 * <p><b>Flusso di salvataggio messaggio:</b>
 * <pre>
 * Messaggio in chiaro
 *    ↓
 * MessageEncryptionService.encryptMessage()
 *    ↓
 * AES-256 GCM con IV random
 *    ↓
 * Salvataggio nel DB (testo_encrypted + iv)
 * </pre>
 * </p>
 *
 * <p><b>Flusso di lettura messaggio:</b>
 * <pre>
 * Lettura dal DB (testo_encrypted + iv)
 *    ↓
 * MessageEncryptionService.decryptMessage()
 *    ↓
 * AES-256 GCM decrypt
 *    ↓
 * Messaggio in chiaro
 * </pre>
 * </p>
 */
public class MessaggioDAO {
    private static final String TABLE_NAME = "messaggio";
    private final MessageEncryptionService encryptionService;

    public MessaggioDAO() {
        this.encryptionService = MessageEncryptionService.getInstance();
        creaTabellaSeMancante();
    }

    /**
     * Crea la tabella messaggi se non esiste
     * NOTA: Rimuoviamo testo_plaintext_backup per maggiore sicurezza
     */
    private void creaTabellaSeMancante() {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                "id SERIAL PRIMARY KEY, " +
                "mittente_id INTEGER NOT NULL REFERENCES utente(id) ON DELETE CASCADE, " +
                "destinatario_id INTEGER NOT NULL REFERENCES utente(id) ON DELETE CASCADE, " +
                "testo_encrypted BYTEA NOT NULL, " +
                "iv BYTEA NOT NULL, " +
                "data_invio TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "annuncio_id INTEGER REFERENCES annuncio(id) ON DELETE SET NULL, " +
                "algoritmo_encryption VARCHAR(30) NOT NULL DEFAULT 'AES/GCM/NoPadding', " +
                "key_id INTEGER REFERENCES encryption_keys(id) ON DELETE SET NULL" +
                ")";

        try (Connection conn = ConnessioneDB.getConnessione();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("✅ Tabella messaggi verificata/creata con AES-GCM encryption");
        } catch (SQLException e) {
            System.err.println("❌ Errore creazione tabella messaggio: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Invia un messaggio con crittografia AES-256 GCM
     *
     * @param msg Il messaggio da inviare
     * @return true se l'inserimento ha successo
     */
    public boolean inviaMessaggio(Messaggio msg) {
        String sql = "INSERT INTO " + TABLE_NAME +
                     " (mittente_id, destinatario_id, testo_encrypted, iv, data_invio, annuncio_id, algoritmo_encryption) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnessioneDB.getConnessione();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, msg.getMittenteId());
            stmt.setInt(2, msg.getDestinatarioId());

            // Crittografa il messaggio con AES-GCM
            String plaintext = msg.getTesto();
            EncryptedMessageContainer encrypted = encryptionService.encryptMessage(plaintext);

            // Salva i dati crittografati
            stmt.setBytes(3, encrypted.getEncryptedData());
            stmt.setBytes(4, encrypted.getIv());
            stmt.setTimestamp(5, Timestamp.valueOf(msg.getDataInvio()));

            if (msg.getAnnuncioId() != null) {
                stmt.setInt(6, msg.getAnnuncioId());
            } else {
                stmt.setNull(6, Types.INTEGER);
            }

            stmt.setString(7, encrypted.getAlgorithm());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ Messaggio crittografato e salvato con AES-GCM");
                return true;
            }

            return false;

        } catch (Exception e) {
            System.err.println("❌ Errore durante l'invio del messaggio: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Recupera gli ID degli interlocutori di un utente
     *
     * @param mioId ID dell'utente corrente
     * @return Lista di ID degli interlocutori
     */
    public List<Integer> getInterlocutori(int mioId) {
        List<Integer> interlocutori = new ArrayList<>();
        String query = "SELECT DISTINCT CASE " +
                       "WHEN mittente_id = ? THEN destinatario_id " +
                       "WHEN destinatario_id = ? THEN mittente_id " +
                       "END AS interlocutore " +
                       "FROM " + TABLE_NAME + " " +
                       "WHERE mittente_id = ? OR destinatario_id = ?";

        try (Connection conn = ConnessioneDB.getConnessione();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, mioId);
            stmt.setInt(2, mioId);
            stmt.setInt(3, mioId);
            stmt.setInt(4, mioId);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                interlocutori.add(rs.getInt("interlocutore"));
            }

        } catch (SQLException e) {
            System.err.println("❌ Errore recupero interlocutori: " + e.getMessage());
            e.printStackTrace();
        }

        return interlocutori;
    }

    /**
     * Recupera una conversazione completa tra due utenti con decifratura automatica
     *
     * @param utente1 ID del primo utente
     * @param utente2 ID del secondo utente
     * @return Lista di messaggi decifrati
     */
    public List<Messaggio> getConversazione(int utente1, int utente2) {
        List<Messaggio> messaggi = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE " +
                "(mittente_id = ? AND destinatario_id = ?) OR " +
                "(mittente_id = ? AND destinatario_id = ?) " +
                "ORDER BY data_invio ASC";

        try (Connection conn = ConnessioneDB.getConnessione();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, utente1);
            stmt.setInt(2, utente2);
            stmt.setInt(3, utente2);
            stmt.setInt(4, utente1);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Messaggio m = decrittografaMessaggioDaResultSet(rs);
                if (m != null) {
                    messaggi.add(m);
                }
            }

            System.out.println("✅ Recuperati " + messaggi.size() + " messaggi decifrati");

        } catch (SQLException e) {
            System.err.println("❌ Errore recupero conversazione: " + e.getMessage());
            e.printStackTrace();
        }

        return messaggi;
    }

    /**
     * Recupera gli utenti interlocutori con i dettagli completi
     *
     * @param mioId ID dell'utente corrente
     * @return Lista di utenti interlocutori
     */
    public List<utente> getInterlocutoriUtenti(int mioId) {
        List<utente> utenti = new ArrayList<>();

        String query = """
            SELECT DISTINCT u.*
            FROM utente u
            WHERE u.id IN (
                SELECT mittente_id FROM messaggio WHERE destinatario_id = ?
                UNION
                SELECT destinatario_id FROM messaggio WHERE mittente_id = ?
            )
            AND u.id <> ?
            """;

        try (Connection conn = ConnessioneDB.getConnessione();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, mioId);
            stmt.setInt(2, mioId);
            stmt.setInt(3, mioId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    utente u = new utente(
                        rs.getString("matricola"),
                        rs.getString("nome"),
                        rs.getString("cognome"),
                        rs.getString("email"),
                        ""
                    );
                    u.setId(rs.getInt("id"));
                    u.setFotoProfilo(rs.getString("foto_profilo"));
                    utenti.add(u);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Errore recupero interlocutori utenti: " + e.getMessage());
            e.printStackTrace();
        }
        return utenti;
    }

    /**
     * Recupera una conversazione filtrata per annuncio con decifratura automatica
     *
     * @param currentUserId ID dell'utente corrente
     * @param interlocutoreId ID dell'interlocutore
     * @param annuncioId ID dell'annuncio
     * @return Lista di messaggi decifrati per quell'annuncio
     */
    public List<Messaggio> getConversazionePerAnnuncio(int currentUserId, int interlocutoreId, int annuncioId) {
        List<Messaggio> messaggi = new ArrayList<>();

        System.out.println("🔍 Ricerca messaggi per annuncio:");
        System.out.println("   Utente corrente: " + currentUserId);
        System.out.println("   Interlocutore: " + interlocutoreId);
        System.out.println("   Annuncio ID: " + annuncioId);

        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE " +
                "((mittente_id = ? AND destinatario_id = ?) OR " +
                "(mittente_id = ? AND destinatario_id = ?)) " +
                "AND annuncio_id = ? " +
                "ORDER BY data_invio ASC";

        try (Connection conn = ConnessioneDB.getConnessione();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, currentUserId);
            stmt.setInt(2, interlocutoreId);
            stmt.setInt(3, interlocutoreId);
            stmt.setInt(4, currentUserId);
            stmt.setInt(5, annuncioId);

            System.out.println("📊 Esecuzione query...");

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Messaggio m = decrittografaMessaggioDaResultSet(rs);
                if (m != null) {
                    messaggi.add(m);

                    System.out.println("   📨 Messaggio: " + m.getTesto() +
                                     " (da: " + m.getMittenteId() + ")");
                }
            }

            System.out.println("✅ Trovati " + messaggi.size() + " messaggi decifrati per annuncio " + annuncioId);

        } catch (SQLException e) {
            System.err.println("❌ Errore recupero conversazione per annuncio: " + e.getMessage());
            e.printStackTrace();
        }

        return messaggi;
    }

    /**
     * Decrittografa un messaggio dal ResultSet
     *
     * @param rs Il ResultSet posizionato sul messaggio
     * @return Il messaggio decifrato o null se fallisce
     */
    private Messaggio decrittografaMessaggioDaResultSet(ResultSet rs) {
        try {
            int id = rs.getInt("id");
            int mittenteId = rs.getInt("mittente_id");
            int destinatarioId = rs.getInt("destinatario_id");
            Timestamp dataInvioTimestamp = rs.getTimestamp("data_invio");
            Integer annuncioId = rs.getObject("annuncio_id") != null ? rs.getInt("annuncio_id") : null;

            // Leggi i dati crittografati
            byte[] encryptedData = rs.getBytes("testo_encrypted");
            byte[] iv = rs.getBytes("iv");
            String algorithm = rs.getString("algoritmo_encryption");

            if (encryptedData == null || iv == null) {
                System.err.println("⚠️  Messaggio ID " + id + ": dati crittografati mancanti");
                return creaMessaggioErrore(id, mittenteId, destinatarioId, dataInvioTimestamp, annuncioId);
            }

            // Decrittografa il messaggio
            try {
                EncryptedMessageContainer container = EncryptedMessageContainer.fromBase64(
                    java.util.Base64.getEncoder().encodeToString(encryptedData),
                    java.util.Base64.getEncoder().encodeToString(iv),
                    algorithm != null ? algorithm : "AES/GCM/NoPadding",
                    128
                );

                String plaintext = encryptionService.decryptMessage(container);

                return new Messaggio(id, mittenteId, destinatarioId, plaintext,
                                    dataInvioTimestamp.toLocalDateTime(), annuncioId);

            } catch (Exception decryptError) {
                System.err.println("❌ Errore decifratura messaggio ID " + id + ": " + decryptError.getMessage());
                return creaMessaggioErrore(id, mittenteId, destinatarioId, dataInvioTimestamp, annuncioId);
            }

        } catch (SQLException e) {
            System.err.println("❌ Errore lettura messaggio dal ResultSet: " + e.getMessage());
            return null;
        }
    }

    /**
     * Crea un messaggio di errore quando la decifratura fallisce
     */
    private Messaggio creaMessaggioErrore(int id, int mittenteId, int destinatarioId,
                                          Timestamp dataInvio, Integer annuncioId) {
        return new Messaggio(
            id,
            mittenteId,
            destinatarioId,
            "[Messaggio non decifrabile - Errore chiave o corruzione dati]",
            dataInvio.toLocalDateTime(),
            annuncioId
        );
    }

    /**
     * Metodo di migrazione per convertire messaggi vecchi (UTF-8) in AES-GCM
     * Da eseguire una sola volta dopo l'aggiornamento
     *
     * @return Numero di messaggi migrati
     */
    public int migraMessaggiToAESGCM() {
        String selectSql = "SELECT id, mittente_id, destinatario_id, testo_encrypted, data_invio, annuncio_id " +
                          "FROM " + TABLE_NAME + " WHERE algoritmo_encryption = 'UTF-8_ENCODING'";

        String updateSql = "UPDATE " + TABLE_NAME + " SET " +
                          "testo_encrypted = ?, iv = ?, algoritmo_encryption = 'AES/GCM/NoPadding' " +
                          "WHERE id = ?";

        int migrati = 0;

        try (Connection conn = ConnessioneDB.getConnessione();
             PreparedStatement selectStmt = conn.prepareStatement(selectSql);
             PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {

            conn.setAutoCommit(false);

            ResultSet rs = selectStmt.executeQuery();

            while (rs.next()) {
                try {
                    int id = rs.getInt("id");
                    String vecchioTesto = new String(rs.getBytes("testo_encrypted"), StandardCharsets.UTF_8);

                    // Crittografa con AES-GCM
                    EncryptedMessageContainer encrypted = encryptionService.encryptMessage(vecchioTesto);

                    updateStmt.setBytes(1, encrypted.getEncryptedData());
                    updateStmt.setBytes(2, encrypted.getIv());
                    updateStmt.setInt(3, id);

                    updateStmt.executeUpdate();
                    migrati++;

                } catch (Exception e) {
                    System.err.println("⚠️  Errore migrazione messaggio ID " + rs.getInt("id") + ": " + e.getMessage());
                }
            }

            conn.commit();
            System.out.println("✅ Migrazione completata: " + migrati + " messaggi convertiti in AES-GCM");

        } catch (SQLException e) {
            System.err.println("❌ Errore migrazione messaggi: " + e.getMessage());
            e.printStackTrace();
        }

        return migrati;
    }

    /**
     * Verifica l'integrità dei messaggi nel database
     *
     * @return Numero di messaggi con integrità verificata
     */
    public int verificaIntegritaMessaggi() {
        String sql = "SELECT id, testo_encrypted, iv, algoritmo_encryption FROM " + TABLE_NAME;
        int verificati = 0;
        int corrotti = 0;

        try (Connection conn = ConnessioneDB.getConnessione();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                try {
                    byte[] encryptedData = rs.getBytes("testo_encrypted");
                    byte[] iv = rs.getBytes("iv");
                    String algorithm = rs.getString("algoritmo_encryption");

                    if (encryptedData != null && iv != null && "AES/GCM/NoPadding".equals(algorithm)) {
                        verificati++;
                    } else {
                        corrotti++;
                        System.err.println("⚠️  Messaggio ID " + rs.getInt("id") + " con dati non validi");
                    }
                } catch (Exception e) {
                    corrotti++;
                }
            }

            System.out.println("📊 Verifica integrità: " + verificati + " OK, " + corrotti + " corrotti");

        } catch (SQLException e) {
            System.err.println("❌ Errore verifica integrità: " + e.getMessage());
        }

        return verificati;
    }
}
