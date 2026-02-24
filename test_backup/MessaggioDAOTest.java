package application.DB;

import application.Classe.Messaggio;
import application.messagistica.MessageEncryptionService;
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
 * Test unitari per MessaggioDAO
 * Verifica la crittografia AES-GCM e le operazioni CRUD
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MessaggioDAOTest {

    private MessaggioDAO messaggioDAO;
    private Connection connection;
    private MessageEncryptionService encryptionService;

    private static final String H2_DB_URL = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1";
    private static final String H2_USER = "sa";
    private static final String H2_PASSWORD = "";

    @BeforeAll
    void setUpDatabase() throws SQLException {
        // Crea connessione H2 in memoria per i test
        connection = DriverManager.getConnection(H2_DB_URL, H2_USER, H2_PASSWORD);

        // Crea le tabelle necessarie
        try (Statement stmt = connection.createStatement()) {
            // Tabella utente
            stmt.execute("""
                CREATE TABLE utente (
                    id SERIAL PRIMARY KEY,
                    matricola VARCHAR(50),
                    nome VARCHAR(100),
                    cognome VARCHAR(100),
                    email VARCHAR(100),
                    password_hash VARCHAR(255),
                    foto_profilo VARCHAR(500)
                )
            """);

            // Tabella messaggio
            stmt.execute("""
                CREATE TABLE messaggio (
                    id SERIAL PRIMARY KEY,
                    mittente_id INTEGER NOT NULL REFERENCES utente(id) ON DELETE CASCADE,
                    destinatario_id INTEGER NOT NULL REFERENCES utente(id) ON DELETE CASCADE,
                    testo_encrypted BYTEA NOT NULL,
                    iv BYTEA NOT NULL,
                    data_invio TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    annuncio_id INTEGER,
                    algoritmo_encryption VARCHAR(30) NOT NULL DEFAULT 'AES/GCM/NoPadding',
                    key_id INTEGER
                )
            """);

            // Inserisci utenti di test
            stmt.execute("INSERT INTO utente (matricola, nome, cognome, email, password_hash) VALUES " +
                       "('N123456', 'Mario', 'Rossi', 'mario@studenti.unina.it', 'hash1')");
            stmt.execute("INSERT INTO utente (matricola, nome, cognome, email, password_hash) VALUES " +
                       "('N123457', 'Luigi', 'Verdi', 'luigi@studenti.unina.it', 'hash2')");
        }

        encryptionService = MessageEncryptionService.getInstance();

        // Sostituisci ConnessioneDB per usare H2
        System.out.println("📊 Database H2 inizializzato per i test");
    }

    @BeforeEach
    void setUp() {
        messaggioDAO = new MessaggioDAO();
    }

    @Test
    @DisplayName("Test invio messaggio con crittografia AES-GCM")
    void testInviaMessaggioConCrittografia() {
        // Arrange
        Messaggio msg = new Messaggio();
        msg.setMittenteId(1);
        msg.setDestinatarioId(2);
        msg.setTesto("Questo è un messaggio di test");
        msg.setDataInvio(LocalDateTime.now());

        // Act
        boolean result = messaggioDAO.inviaMessaggio(msg);

        // Assert
        assertTrue(result, "L'inserimento del messaggio dovrebbe avere successo");
        assertNotEquals(0, msg.getId(), "L'ID del messaggio dovrebbe essere generato");
    }

    @Test
    @DisplayName("Test recupero conversazione con decifratura")
    void testRecuperoConversazioneConDecifratura() {
        // Arrange
        Messaggio msg1 = new Messaggio();
        msg1.setMittenteId(1);
        msg1.setDestinatarioId(2);
        msg1.setTesto("Messaggio 1");
        msg1.setDataInvio(LocalDateTime.now().minusMinutes(5));
        messaggioDAO.inviaMessaggio(msg1);

        Messaggio msg2 = new Messaggio();
        msg2.setMittenteId(2);
        msg2.setDestinatarioId(1);
        msg2.setTesto("Risposta 1");
        msg2.setDataInvio(LocalDateTime.now());
        messaggioDAO.inviaMessaggio(msg2);

        // Act
        List<Messaggio> conversazione = messaggioDAO.getConversazione(1, 2);

        // Assert
        assertNotNull(conversazione, "La conversazione non dovrebbe essere null");
        assertEquals(2, conversazione.size(), "Dovrebbero esserci 2 messaggi");
        assertEquals("Messaggio 1", conversazione.get(0).getTesto(), "Il primo messaggio dovrebbe essere decifrato correttamente");
        assertEquals("Risposta 1", conversazione.get(1).getTesto(), "Il secondo messaggio dovrebbe essere decifrato correttamente");
    }

    @Test
    @DisplayName("Test recupero interlocutori")
    void testGetInterlocutori() {
        // Arrange
        Messaggio msg = new Messaggio();
        msg.setMittenteId(1);
        msg.setDestinatarioId(2);
        msg.setTesto("Test");
        msg.setDataInvio(LocalDateTime.now());
        messaggioDAO.inviaMessaggio(msg);

        // Act
        List<Integer> interlocutori = messaggioDAO.getInterlocutori(1);

        // Assert
        assertNotNull(interlocutori);
        assertTrue(interlocutori.contains(2), "L'interlocutore 2 dovrebbe essere nella lista");
    }

    @Test
    @DisplayName("Test messaggio con annuncio associato")
    void testMessaggioConAnnuncio() {
        // Arrange
        Messaggio msg = new Messaggio();
        msg.setMittenteId(1);
        msg.setDestinatarioId(2);
        msg.setTesto("Sono interessato a questo annuncio");
        msg.setDataInvio(LocalDateTime.now());
        msg.setAnnuncioId(123);

        // Act
        boolean result = messaggioDAO.inviaMessaggio(msg);

        // Assert
        assertTrue(result);
        List<Messaggio> conversazione = messaggioDAO.getConversazionePerAnnuncio(1, 2, 123);
        assertFalse(conversazione.isEmpty(), "Dovrebbe esserci almeno un messaggio per questo annuncio");
        assertEquals(123, conversazione.get(0).getAnnuncioId(), "L'ID annuncio dovrebbe essere preservato");
    }

    @Test
    @DisplayName("Test conversazione vuota")
    void testConversazioneVuota() {
        // Act
        List<Messaggio> conversazione = messaggioDAO.getConversazione(1, 2);

        // Assert
        assertNotNull(conversazione, "La conversazione non dovrebbe essere null");
        assertTrue(conversazione.isEmpty(), "La conversazione dovrebbe essere vuota");
    }

    @Test
    @DisplayName("Test messaggio lungo con emoji")
    void testMessaggioLungoConEmoji() {
        // Arrange
        String testoLungo = "Ciao! 👋 Sono interessato al tuo annuncio. 📚 " +
                           "Il libro è ancora disponibile? 🤔 Possiamo incontrarci " +
                           "in università per vedere lo stato del libro? 📖 " +
                           "Grazie! 😊";

        Messaggio msg = new Messaggio();
        msg.setMittenteId(1);
        msg.setDestinatarioId(2);
        msg.setTesto(testoLungo);
        msg.setDataInvio(LocalDateTime.now());

        // Act
        boolean result = messaggioDAO.inviaMessaggio(msg);

        // Assert
        assertTrue(result);
        List<Messaggio> conversazione = messaggioDAO.getConversazione(1, 2);
        assertFalse(conversazione.isEmpty());
        assertEquals(testoLungo, conversazione.get(0).getTesto(), "Il messaggio lungo con emoji dovrebbe essere preservato");
    }

    @Test
    @DisplayName("Test recupero messaggi in ordine cronologico")
    void testOrdineCronologicoMessaggi() {
        // Arrange
        for (int i = 1; i <= 5; i++) {
            Messaggio msg = new Messaggio();
            msg.setMittenteId(i % 2 == 0 ? 2 : 1);
            msg.setDestinatarioId(i % 2 == 0 ? 1 : 2);
            msg.setTesto("Messaggio " + i);
            msg.setDataInvio(LocalDateTime.now().minusMinutes(5 - i));
            messaggioDAO.inviaMessaggio(msg);
        }

        // Act
        List<Messaggio> conversazione = messaggioDAO.getConversazione(1, 2);

        // Assert
        assertEquals(5, conversazione.size());
        assertEquals("Messaggio 1", conversazione.get(0).getTesto());
        assertEquals("Messaggio 5", conversazione.get(4).getTesto());
    }

    @AfterAll
    void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
        System.out.println("✅ Database test chiuso");
    }
}
