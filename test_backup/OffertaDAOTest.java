package application.DB;

import application.Classe.OffertaNegoziazione;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test unitari per OffertaDAO
 * Verifica il sistema di negoziazione e contro-offerte
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OffertaDAOTest {

    private OffertaDAO offertaDAO;
    private Connection connection;

    private static final String H2_DB_URL = "jdbc:h2:mem:testdb_offerte;DB_CLOSE_DELAY=-1";
    private static final String H2_USER = "sa";
    private static final String H2_PASSWORD = "";

    @BeforeAll
    void setUpDatabase() throws SQLException {
        connection = DriverManager.getConnection(H2_DB_URL, H2_USER, H2_PASSWORD);

        try (Statement stmt = connection.createStatement()) {
            // Tabella utente
            stmt.execute("""
                CREATE TABLE utente (
                    id SERIAL PRIMARY KEY,
                    matricola VARCHAR(50),
                    nome VARCHAR(100),
                    cognome VARCHAR(100),
                    email VARCHAR(100)
                )
            """);

            // Tabella annuncio
            stmt.execute("""
                CREATE TABLE annuncio (
                    id SERIAL PRIMARY KEY,
                    titolo VARCHAR(200),
                    venditore_id INTEGER,
                    prezzo NUMERIC(10,2)
                )
            """);

            // Tabella offerta
            stmt.execute("""
                CREATE TABLE offerta (
                    id SERIAL PRIMARY KEY,
                    annuncio_id INTEGER NOT NULL REFERENCES annuncio(id) ON DELETE CASCADE,
                    offerente_id INTEGER NOT NULL REFERENCES utente(id) ON DELETE CASCADE,
                    venditore_id INTEGER NOT NULL REFERENCES utente(id) ON DELETE CASCADE,
                    importo NUMERIC(10, 2) NOT NULL,
                    messaggio TEXT,
                    stato VARCHAR(20) NOT NULL DEFAULT 'IN_ATTESA',
                    tipo VARCHAR(20) NOT NULL DEFAULT 'INIZIALE',
                    data_creazione TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    data_risposta TIMESTAMP,
                    offerta_padre_id INTEGER REFERENCES offerta(id) ON DELETE SET NULL,
                    notificata BOOLEAN DEFAULT FALSE,
                    CHECK (importo > 0)
                )
            """);

            // Inserisci dati di test
            stmt.execute("INSERT INTO utente (matricola, nome, cognome, email) VALUES " +
                       "('N1', 'Mario', 'Rossi', 'mario@test.com')");
            stmt.execute("INSERT INTO utente (matricola, nome, cognome, email) VALUES " +
                       "('N2', 'Luigi', 'Verdi', 'luigi@test.com')");

            stmt.execute("INSERT INTO annuncio (titolo, venditore_id, prezzo) VALUES " +
                       "('Libro Programmazione', 1, 50.00)");
        }

        System.out.println("📊 Database H2 per offerte inizializzato");
    }

    @BeforeEach
    void setUp() {
        offertaDAO = new OffertaDAO();
    }

    @Test
    @DisplayName("Test creazione offerta iniziale")
    void testCreaOffertaIniziale() {
        // Arrange
        OffertaNegoziazione offerta = new OffertaNegoziazione();
        offerta.setAnnuncioId(1);
        offerta.setOfferenteId(2);
        offerta.setVenditoreId(1);
        offerta.setImporto(40.00);
        offerta.setMessaggio("Posso offrire 40 euro?");
        offerta.setStato(OffertaNegoziazione.StatoOfferta.IN_ATTESA);
        offerta.setTipo(OffertaNegoziazione.TipoOfferta.INIZIALE);

        // Act
        int offertaId = offertaDAO.creaOfferta(offerta);

        // Assert
        assertTrue(offertaId > 0, "L'ID offerta dovrebbe essere positivo");
        assertEquals(offertaId, offerta.getId(), "L'ID dovrebbe essere impostato nell'oggetto");
    }

    @Test
    @DisplayName("Test accettazione offerta")
    void testAccettaOfferta() {
        // Arrange
        OffertaNegoziazione offerta = new OffertaNegoziazione();
        offerta.setAnnuncioId(1);
        offerta.setOfferenteId(2);
        offerta.setVenditoreId(1);
        offerta.setImporto(45.00);
        offerta.setMessaggio("Ti offro 45 euro");
        offertaDAO.creaOfferta(offerta);

        // Act
        boolean result = offertaDAO.accettaOfferta(offerta.getId());

        // Assert
        assertTrue(result, "L'accettazione dovrebbe avere successo");

        OffertaNegoziazione aggiornata = offertaDAO.getOffertaNegoziazioneById(offerta.getId());
        assertEquals(OffertaNegoziazione.StatoOfferta.ACCETTATA, aggiornata.getStato(),
                    "Lo stato dovrebbe essere ACCETTATA");
    }

    @Test
    @DisplayName("Test rifiuto offerta")
    void testRifiutaOfferta() {
        // Arrange
        OffertaNegoziazione offerta = new OffertaNegoziazione();
        offerta.setAnnuncioId(1);
        offerta.setOfferenteId(2);
        offerta.setVenditoreId(1);
        offerta.setImporto(30.00);
        offertaDAO.creaOfferta(offerta);

        // Act
        boolean result = offertaDAO.rifiutaOfferta(offerta.getId());

        // Assert
        assertTrue(result);

        OffertaNegoziazione aggiornata = offertaDAO.getOffertaNegoziazioneById(offerta.getId());
        assertEquals(OffertaNegoziazione.StatoOfferta.RIFIUTATA, aggiornata.getStato());
    }

    @Test
    @DisplayName("Test creazione contro-offerta")
    void testCreaControfferta() {
        // Arrange - Crea offerta iniziale
        OffertaNegoziazione offertaIniziale = new OffertaNegoziazione();
        offertaIniziale.setAnnuncioId(1);
        offertaIniziale.setOfferenteId(2);
        offertaIniziale.setVenditoreId(1);
        offertaIniziale.setImporto(35.00);
        offertaIniziale.setMessaggio("Offro 35 euro");
        offertaDAO.creaOfferta(offertaIniziale);

        // Act - Crea contro-offerta
        int controffertaId = offertaDAO.creaControfferta(
            offertaIniziale.getId(),
            42.00,
            "Non posso scendere sotto i 42 euro"
        );

        // Assert
        assertTrue(controffertaId > 0, "La contro-offerta dovrebbe essere creata");

        OffertaNegoziazione padre = offertaDAO.getOffertaNegoziazioneById(offertaIniziale.getId());
        assertEquals(OffertaNegoziazione.StatoOfferta.CONTROFFERTA, padre.getStato(),
                    "L'offerta padre dovrebbe avere stato CONTROFFERTA");

        OffertaNegoziazione controfferta = offertaDAO.getOffertaNegoziazioneById(controffertaId);
        assertEquals(OffertaNegoziazione.TipoOfferta.CONTRO_OFFERTA, controfferta.getTipo());
        assertEquals(offertaIniziale.getId(), controfferta.getOffertaPadreId());
        assertEquals(42.00, controfferta.getImporto());
    }

    @Test
    @DisplayName("Test recupero offerte ricevute")
    void testGetOfferteRicevute() {
        // Arrange
        OffertaNegoziazione offerta1 = new OffertaNegoziazione();
        offerta1.setAnnuncioId(1);
        offerta1.setOfferenteId(2);
        offerta1.setVenditoreId(1);
        offerta1.setImporto(40.00);
        offerta1.setStato(OffertaNegoziazione.StatoOfferta.IN_ATTESA);
        offertaDAO.creaOfferta(offerta1);

        // Act
        List<OffertaNegoziazione> offerte = offertaDAO.getOfferteRicevute(1);

        // Assert
        assertFalse(offerte.isEmpty(), "Dovrebbe esserci almeno un'offerta ricevuta");
        assertTrue(offerte.stream().anyMatch(o -> o.getImporto() == 40.00),
                  "Dovrebbe trovare l'offerta di 40 euro");
    }

    @Test
    @DisplayName("Test verifica offerta attiva")
    void testHaOffertaAttiva() {
        // Arrange
        OffertaNegoziazione offerta = new OffertaNegoziazione();
        offerta.setAnnuncioId(1);
        offerta.setOfferenteId(2);
        offerta.setVenditoreId(1);
        offerta.setImporto(40.00);
        offerta.setStato(OffertaNegoziazione.StatoOfferta.IN_ATTESA);
        offertaDAO.creaOfferta(offerta);

        // Act
        boolean haOfferta = offertaDAO.haOffertaAttiva(1, 2);

        // Assert
        assertTrue(haOfferta, "L'utente dovrebbe avere un'offerta attiva");
    }

    @Test
    @DisplayName("Test storico negoziazione")
    void testGetStoricoNegoziazione() {
        // Arrange - Crea una serie di offerte e contro-offerte
        OffertaNegoziazione o1 = new OffertaNegoziazione();
        o1.setAnnuncioId(1);
        o1.setOfferenteId(2);
        o1.setVenditoreId(1);
        o1.setImporto(35.00);
        o1.setStato(OffertaNegoziazione.StatoOfferta.IN_ATTESA);
        int o1Id = offertaDAO.creaOfferta(o1);

        // Contro-offerta
        int o2Id = offertaDAO.creaControfferta(o1Id, 42.00, "Non meno di 42");

        // Act
        List<OffertaNegoziazione> storico = offertaDAO.getStoricoNegoziazione(1, 1, 2);

        // Assert
        assertEquals(2, storico.size(), "Dovrebbero esserci 2 messaggi nello storico");
    }

    @AfterAll
    void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
        System.out.println("✅ Database test offerte chiuso");
    }
}
