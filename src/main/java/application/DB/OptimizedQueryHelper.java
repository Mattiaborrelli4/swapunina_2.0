package application.DB;

import java.sql.*;
import java.util.*;

/**
 * Helper per query ottimizzate che evitano problemi N+1
 *
 * <p><b>Problema N+1:</b>
 * Quando recuperi N entità e poi fai N query aggiuntive per recuperare
 * le relazioni, invece di fare una singola query con JOIN.
 * </p>
 *
 * <p><b>Soluzione:</b>
 * Usare JOIN, subquery, o fetch con una sola query per recuperare
 * tutti i dati necessari.
 * </p>
 */
public class OptimizedQueryHelper {

    /**
     * Recupera annunci con caratteristiche in una singola query
     * Invece di: 1 query per annunci + N query per caratteristiche
     * Usa: 1 query con JOIN che include le caratteristiche
     */
    public static List<Map<String, Object>> getAnnunciConCaratteristiche(Connection conn, int limit, int offset) throws SQLException {
        String sql = """
            SELECT DISTINCT
                a.id AS annuncio_id,
                a.titolo,
                a.prezzo,
                a.stato,
                a.venditore_id,
                a.data_pubblicazione,
                o.nome AS oggetto_nome,
                o.categoria_id,
                u.nome AS venditore_nome,
                u.cognome AS venditore_cognome,
                COALESCE(
                    json_agg(
                        CASE WHEN ac.caratteristica IS NOT NULL
                            THEN json_build_object('caratteristica', ac.caratteristica)
                            ELSE NULL
                        END
                    ) FILTER (WHERE ac.caratteristica IS NOT NULL), '[]'
                ) AS caratteristiche
            FROM annuncio a
            JOIN oggetto o ON a.oggetto_id = o.id
            JOIN utente u ON a.venditore_id = u.id
            LEFT JOIN annuncio_caratteristica ac ON a.id = ac.annuncio_id
            WHERE a.stato = 'ATTIVO'
            GROUP BY a.id, a.titolo, a.prezzo, a.stato, a.venditore_id,
                     a.data_pubblicazione, o.nome, o.categoria_id,
                     u.nome, u.cognome
            ORDER BY a.data_pubblicazione DESC
            LIMIT ? OFFSET ?
        """;

        List<Map<String, Object>> results = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            stmt.setInt(2, offset);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("annuncio_id", rs.getInt("annuncio_id"));
                    row.put("titolo", rs.getString("titolo"));
                    row.put("prezzo", rs.getDouble("prezzo"));
                    row.put("stato", rs.getString("stato"));
                    row.put("venditore_id", rs.getInt("venditore_id"));
                    row.put("data_pubblicazione", rs.getTimestamp("data_pubblicazione"));
                    row.put("oggetto_nome", rs.getString("oggetto_nome"));
                    row.put("categoria_id", rs.getInt("categoria_id"));
                    row.put("venditore_nome", rs.getString("venditore_nome"));
                    row.put("venditore_cognome", rs.getString("venditore_cognome"));
                    // Le caratteristiche sono già incluse come array JSON
                    row.put("caratteristiche", rs.getString("caratteristiche"));
                    results.add(row);
                }
            }
        }

        return results;
    }

    /**
     * Recupera il carrello con tutti i dettagli in una query
     * Evita N query per recuperare i dettagli di ogni annuncio
     */
    public static List<Map<String, Object>> getCarrelloCompleto(Connection conn, int utenteId) throws SQLException {
        String sql = """
            SELECT
                c.id AS carrello_id,
                c.quantita,
                c.data_aggiunta,
                a.id AS annuncio_id,
                a.titolo,
                a.prezzo,
                a.stato AS annuncio_stato,
                a.descrizione,
                o.nome AS oggetto_nome,
                o.image_url AS oggetto_image,
                cat.nome AS categoria_nome,
                u.id AS venditore_id,
                u.nome AS venditore_nome,
                u.cognome AS venditore_cognome,
                u.email AS venditore_email
            FROM carrello c
            JOIN annuncio a ON c.annuncio_id = a.id
            JOIN oggetto o ON a.oggetto_id = o.id
            JOIN categoria cat ON o.categoria_id = cat.id
            JOIN utente u ON a.venditore_id = u.id
            WHERE c.utente_id = ?
            ORDER BY c.data_aggiunta DESC
        """;

        List<Map<String, Object>> results = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, utenteId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("carrello_id", rs.getInt("carrello_id"));
                    row.put("quantita", rs.getInt("quantita"));
                    row.put("data_aggiunta", rs.getTimestamp("data_aggiunta"));
                    row.put("annuncio_id", rs.getInt("annuncio_id"));
                    row.put("titolo", rs.getString("titolo"));
                    row.put("prezzo", rs.getDouble("prezzo"));
                    row.put("annuncio_stato", rs.getString("annuncio_stato"));
                    row.put("descrizione", rs.getString("descrizione"));
                    row.put("oggetto_nome", rs.getString("oggetto_nome"));
                    row.put("oggetto_image", rs.getString("oggetto_image"));
                    row.put("categoria_nome", rs.getString("categoria_nome"));
                    row.put("venditore_id", rs.getInt("venditore_id"));
                    row.put("venditore_nome", rs.getString("venditore_nome"));
                    row.put("venditore_cognome", rs.getString("venditore_cognome"));
                    row.put("venditore_email", rs.getString("venditore_email"));
                    results.add(row);
                }
            }
        }

        return results;
    }

    /**
     * Recupera le recensioni con dettagli utente in una query
     */
    public static List<Map<String, Object>> getRecensioniConDettagliUtente(Connection conn, int destinatarioId) throws SQLException {
        String sql = """
            SELECT
                r.id AS recensione_id,
                r.voto,
                r.commento,
                r.data_recensione,
                r.tipo_recensione,
                u.id AS recensore_id,
                u.nome AS recensore_nome,
                u.cognome AS recensore_cognome,
                u.foto_profilo AS recensore_foto,
                a.id AS annuncio_id,
                a.titolo AS annuncio_titolo
            FROM recensione r
            JOIN utente u ON r.recensore_id = u.id
            LEFT JOIN annuncio a ON r.annuncio_id = a.id
            WHERE r.destinatario_id = ?
            ORDER BY r.data_recensione DESC
        """;

        List<Map<String, Object>> results = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, destinatarioId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("recensione_id", rs.getInt("recensione_id"));
                    row.put("voto", rs.getInt("voto"));
                    row.put("commento", rs.getString("commento"));
                    row.put("data_recensione", rs.getTimestamp("data_recensione"));
                    row.put("tipo_recensione", rs.getString("tipo_recensione"));
                    row.put("recensore_id", rs.getInt("recensore_id"));
                    row.put("recensore_nome", rs.getString("recensore_nome"));
                    row.put("recensore_cognome", rs.getString("recensore_cognome"));
                    row.put("recensore_foto", rs.getString("recensore_foto"));
                    row.put("annuncio_id", rs.getObject("annuncio_id", Integer.class));
                    row.put("annuncio_titolo", rs.getString("annuncio_titolo"));
                    results.add(row);
                }
            }
        }

        return results;
    }

    /**
     * Recupera messaggi con info mittente in una query
     */
    public static List<Map<String, Object>> getMessaggiConMittente(Connection conn, int utenteId, int interlocutoreId) throws SQLException {
        String sql = """
            SELECT
                m.id AS messaggio_id,
                m.testo_encrypted,
                m.iv,
                m.data_invio,
                m.annuncio_id,
                m.mittente_id,
                m.destinatario_id,
                u.nome AS mittente_nome,
                u.cognome AS mittente_cognome,
                u.foto_profilo AS mittente_foto
            FROM messaggio m
            JOIN utente u ON m.mittente_id = u.id
            WHERE (m.mittente_id = ? AND m.destinatario_id = ?)
               OR (m.mittente_id = ? AND m.destinatario_id = ?)
            ORDER BY m.data_invio ASC
        """;

        List<Map<String, Object>> results = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, utenteId);
            stmt.setInt(2, interlocutoreId);
            stmt.setInt(3, interlocutoreId);
            stmt.setInt(4, utenteId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("messaggio_id", rs.getInt("messaggio_id"));
                    row.put("testo_encrypted", rs.getBytes("testo_encrypted"));
                    row.put("iv", rs.getBytes("iv"));
                    row.put("data_invio", rs.getTimestamp("data_invio"));
                    row.put("annuncio_id", rs.getObject("annuncio_id", Integer.class));
                    row.put("mittente_id", rs.getInt("mittente_id"));
                    row.put("destinatario_id", rs.getInt("destinatario_id"));
                    row.put("mittente_nome", rs.getString("mittente_nome"));
                    row.put("mittente_cognome", rs.getString("mittente_cognome"));
                    row.put("mittente_foto", rs.getString("mittente_foto"));
                    results.add(row);
                }
            }
        }

        return results;
    }

    /**
     * Recupera statistiche utente con aggregazioni in una query
     * Evita query separate per contare annunci, recensioni, etc.
     */
    public static Map<String, Object> getStatisticheUtente(Connection conn, int utenteId) throws SQLException {
        String sql = """
            SELECT
                u.id AS utente_id,
                u.nome,
                u.cognome,
                u.email,
                u.data_registrazione,
                COUNT(DISTINCT a.id) FILTER (WHERE a.stato = 'ATTIVO') AS annunci_attivi,
                COUNT(DISTINCT a.id) FILTER (WHERE a.stato = 'VENDUTO') AS annunci_venduti,
                COUNT(DISTINCT c.id) AS oggetti_carrello,
                COUNT(DISTINCT CASE WHEN m.destinatario_id = u.id THEN m.id END) AS messaggi_ricevuti,
                COALESCE(AVG(r.voto), 0) AS media_voto,
                COUNT(DISTINCT r.id) AS totale_recensioni
            FROM utente u
            LEFT JOIN annuncio a ON u.id = a.venditore_id
            LEFT JOIN carrello c ON a.id = c.annuncio_id
            LEFT JOIN messaggio m ON (u.id = m.mittente_id OR u.id = m.destinatario_id)
            LEFT JOIN recensione r ON u.id = r.destinatario_id
            WHERE u.id = ?
            GROUP BY u.id, u.nome, u.cognome, u.email, u.data_registrazione
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, utenteId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> stats = new HashMap<>();
                    stats.put("utente_id", rs.getInt("utente_id"));
                    stats.put("nome", rs.getString("nome"));
                    stats.put("cognome", rs.getString("cognome"));
                    stats.put("email", rs.getString("email"));
                    stats.put("data_registrazione", rs.getTimestamp("data_registrazione"));
                    stats.put("annunci_attivi", rs.getInt("annunci_attivi"));
                    stats.put("annunci_venduti", rs.getInt("annunci_venduti"));
                    stats.put("oggetti_carrello", rs.getInt("oggetti_carrello"));
                    stats.put("messaggi_ricevuti", rs.getInt("messaggi_ricevuti"));
                    stats.put("media_voto", rs.getDouble("media_voto"));
                    stats.put("totale_recensioni", rs.getInt("totale_recensioni"));
                    return stats;
                }
            }
        }

        return Collections.emptyMap();
    }

    /**
     * Recupera annunci con conteggio recensioni in una query
     */
    public static List<Map<String, Object>> getAnnunciConRecensioni(Connection conn, int venditoreId) throws SQLException {
        String sql = """
            SELECT
                a.id AS annuncio_id,
                a.titolo,
                a.prezzo,
                a.stato,
                a.data_pubblicazione,
                o.nome AS oggetto_nome,
                o.image_url AS oggetto_image,
                COUNT(DISTINCT r.id) AS numero_recensioni,
                COALESCE(AVG(r.voto), 0) AS media_recensioni,
                COUNT(DISTINCT c.id) AS volte_carrello
            FROM annuncio a
            JOIN oggetto o ON a.oggetto_id = o.id
            LEFT JOIN recensione r ON a.id = r.annuncio_id
            LEFT JOIN carrello c ON a.id = c.annuncio_id
            WHERE a.venditore_id = ?
            GROUP BY a.id, a.titolo, a.prezzo, a.stato, a.data_pubblicazione,
                     o.nome, o.image_url
            ORDER BY a.data_pubblicazione DESC
        """;

        List<Map<String, Object>> results = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, venditoreId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("annuncio_id", rs.getInt("annuncio_id"));
                    row.put("titolo", rs.getString("titolo"));
                    row.put("prezzo", rs.getDouble("prezzo"));
                    row.put("stato", rs.getString("stato"));
                    row.put("data_pubblicazione", rs.getTimestamp("data_pubblicazione"));
                    row.put("oggetto_nome", rs.getString("oggetto_nome"));
                    row.put("oggetto_image", rs.getString("oggetto_image"));
                    row.put("numero_recensioni", rs.getInt("numero_recensioni"));
                    row.put("media_recensioni", rs.getDouble("media_recensioni"));
                    row.put("volte_carrello", rs.getInt("volte_carrello"));
                    results.add(row);
                }
            }
        }

        return results;
    }

    /**
     * Metodo di utilità per eseguire query batch
     * Utile quando devi eseguire più query indipendenti
     */
    public static List<ResultSet> eseguiBatchQuery(Connection conn, List<String> queries, List<Object[]> params) throws SQLException {
        List<ResultSet> results = new ArrayList<>();

        for (int i = 0; i < queries.size(); i++) {
            String sql = queries.get(i);
            Object[] queryParams = params.get(i);

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (int j = 0; j < queryParams.length; j++) {
                    stmt.setObject(j + 1, queryParams[j]);
                }
                results.add(stmt.executeQuery());
            }
        }

        return results;
    }

    /**
     * Crea una query con subquery per contare relazioni
     * Evita query separate per i conteggi
     */
    public static List<Map<String, Object>> getAnnunciConConteggi(Connection conn) throws SQLException {
        String sql = """
            SELECT
                a.id,
                a.titolo,
                a.prezzo,
                a.stato,
                (SELECT COUNT(*) FROM carrello WHERE annuncio_id = a.id) AS volte_in_carrello,
                (SELECT COUNT(*) FROM messaggio WHERE annuncio_id = a.id) AS numero_messaggi,
                (SELECT COUNT(*) FROM recensione WHERE annuncio_id = a.id) AS numero_recensioni,
                (SELECT COALESCE(AVG(voto), 0) FROM recensione WHERE annuncio_id = a.id) AS media_voti
            FROM annuncio a
            WHERE a.stato = 'ATTIVO'
            ORDER BY a.data_pubblicazione DESC
        """;

        List<Map<String, Object>> results = new ArrayList<>();

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("id", rs.getInt("id"));
                row.put("titolo", rs.getString("titolo"));
                row.put("prezzo", rs.getDouble("prezzo"));
                row.put("stato", rs.getString("stato"));
                row.put("volte_in_carrello", rs.getInt("volte_in_carrello"));
                row.put("numero_messaggi", rs.getInt("numero_messaggi"));
                row.put("numero_recensioni", rs.getInt("numero_recensioni"));
                row.put("media_voti", rs.getDouble("media_voti"));
                results.add(row);
            }
        }

        return results;
    }

    /**
     * Verifica le performance di una query
     */
    public static void analizzaPerformanceQuery(Connection conn, String sql, Object[] params) {
        long startTime = System.currentTimeMillis();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                int rowCount = 0;
                while (rs.next()) {
                    rowCount++;
                }

                long duration = System.currentTimeMillis() - startTime;
                System.out.println("📊 Performance Query:");
                System.out.println("   Tempo: " + duration + "ms");
                System.out.println("   Righe: " + rowCount);
                System.out.println("   Media per riga: " + (duration / Math.max(1, rowCount)) + "ms");
            }
        } catch (SQLException e) {
            System.err.println("❌ Errore analisi performance: " + e.getMessage());
        }
    }
}
