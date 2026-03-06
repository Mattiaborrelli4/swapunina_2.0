package application.DB;

import application.config.ConfigManager;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Gestisce la connessione al database PostgreSQL con HikariCP Connection Pool
 * Fornisce connessioni ottimizzate con pooling automatico e metriche dettagliate
 *
 * <p><b>Caratteristiche:</b>
 * <ul>
 *   <li>Connection pooling con HikariCP</li>
 *   <li>Configurazione centralizzata tramite ConfigManager</li>
 *   <li>Credenziali crittografate nel file di configurazione</li>
 *   <li>Monitoraggio utilizzo connessioni</li>
 *   <li>Timeout configurabili</li>
 *   <li>Validazione connessioni</li>
 *   <li>Metriche prestazioni avanzate</li>
 * </ul>
 * </p>
 *
 * <p><b>Vantaggi rispetto a DriverManager:</b>
 * <ul>
 *   <li>Pooling automatico delle connessioni</li>
 *   <li>Migliori prestazioni (connessioni riutilizzate)</li>
 *   <li>Gestione automatica connessioni stale</li>
 *   <li>Configurazione esternalizzata e crittografata</li>
 *   <li>Metriche dettagliate sul pool</li>
 * </ul>
 * </p>
 */
public class ConnessioneDB {
    // ========== POOL DI CONNESSIONI HIKARICP ==========

    private static HikariDataSource dataSource;
    private static volatile boolean poolInizializzato = false;

    // ========== METRICHE E STATISTICHE ==========

    private static final AtomicInteger CONTATORE_CONNESSIONI_TOTALI = new AtomicInteger(0);
    private static final AtomicInteger CONTATORE_ERRORI_CONNESSIONE = new AtomicInteger(0);

    // ========== INIZIALIZZAZIONE STATICA ==========

    static {
        inizializzaPool();
    }

    /**
     * Inizializza il pool di connessioni HikariCP con configurazione da ConfigManager
     * Eseguito una sola volta quando la classe viene caricata
     */
    private static void inizializzaPool() {
        try {
            Class.forName("org.postgresql.Driver");

            ConfigManager config = ConfigManager.getInstance();

            HikariConfig hikariConfig = new HikariConfig();

            // Configurazione base database
            hikariConfig.setJdbcUrl(String.format("jdbc:postgresql://%s:%d/%s",
                    config.getDbHost(),
                    config.getDbPort(),
                    config.getDbName()));

            hikariConfig.setUsername(config.getDbUser());
            hikariConfig.setPassword(config.getDbPassword());

            // Configurazione del driver
            hikariConfig.setDriverClassName("org.postgresql.Driver");

            // ========== CONFIGURAZIONE POOL ==========

            // Dimensione pool
            hikariConfig.setMaximumPoolSize(config.getPoolMaxSize());        // Max connessioni nel pool
            hikariConfig.setMinimumIdle(config.getPoolMinIdle());            // Min connessioni idle nel pool

            // Timeout (in millisecondi)
            hikariConfig.setConnectionTimeout(config.getPoolConnectionTimeout());  // Timeout attesa connessione
            hikariConfig.setIdleTimeout(config.getPoolIdleTimeout());              // Timeout connessioni idle
            hikariConfig.setMaxLifetime(config.getPoolMaxLifetime());              // Max vita connessione (30 min)

            // Comportamento pool
            hikariConfig.setConnectionTestQuery("SELECT 1");              // Query test validazione
            hikariConfig.setValidationTimeout(3000);                       // Timeout validazione (3s)

            // ========== OTTIMIZZAZIONI PRESTAZIONI ==========

            // PreparedStatement caching
            hikariConfig.addDataSourceProperty("preparedStatementCacheQueries", "256");
            hikariConfig.addDataSourceProperty("preparedStatementCacheSizeMiB", "5");

            // Performance tuning
            hikariConfig.addDataSourceProperty("tcpKeepAlive", "true");
            hikariConfig.addDataSourceProperty("ApplicationName", "SwapUnina-App-HikariCP");

            // Fetch size ottimizzato
            hikariConfig.addDataSourceProperty("defaultRowFetchSize", "100");

            // ========== LOGGING E MONITORAGGIO ==========

            hikariConfig.setPoolName("SwapUnina-Pool");

            // Imposta LogLevel (meno verboso)
            hikariConfig.setRegisterMbeans(true);  // Abilita JMX per monitoraggio

            // Crea il DataSource
            dataSource = new HikariDataSource(hikariConfig);
            poolInizializzato = true;

            System.out.println("✅ HikariCP Pool inizializzato con successo");
            System.out.println("   Pool: " + config.getPoolMinIdle() + "-" + config.getPoolMaxSize() + " connessioni");
            System.out.println("   URL: jdbc:postgresql://" + config.getDbHost() + ":" + config.getDbPort() + "/" + config.getDbName());

        } catch (ClassNotFoundException e) {
            poolInizializzato = false;
            String messaggioErrore = "❌ Driver PostgreSQL non trovato. Verifica che il driver sia nel classpath.";
            System.err.println(messaggioErrore);
            // NON lanciamo eccezione per permettere all'app di partire in modalità demo
            System.err.println("⚠️  Applicazione avviata in MODALITÀ DEMO senza database");
        } catch (Exception e) {
            poolInizializzato = false;
            String messaggioErrore = "❌ Errore durante l'inizializzazione del pool HikariCP.";
            System.err.println(messaggioErrore);
            System.err.println("   Dettaglio: " + e.getMessage());
            // NON lanciamo eccezione per permettere all'app di partire in modalità demo
            System.err.println("⚠️  Applicazione avviata in MODALITÀ DEMO senza database");
        }
    }

    // ========== METODI GESTIONE CONNESSIONE ==========

    /**
     * Ottiene una connessione dal pool HikariCP
     *
     * <p>La connessione viene presa dal pool se disponibile, altrimenti ne viene creata
     * una nuova automaticamente dal pool. Quando la connessione viene chiusa,
     * viene restituita al pool invece di essere realmente chiusa.</p>
     *
     * @return Connection oggetto connessione al database
     * @throws SQLException se la connessione fallisce o il pool non è inizializzato
     */
    public static Connection getConnessione() throws SQLException {
        if (!poolInizializzato || dataSource == null) {
            System.err.println("⚠️  Tentativo di connessione in modalità DEMO - database non disponibile");
            throw new SQLException("Database non disponibile - modalità demo attiva");
        }

        try {
            Connection connessione = dataSource.getConnection();

            // Aggiorna metriche
            CONTATORE_CONNESSIONI_TOTALI.incrementAndGet();

            return connessione;

        } catch (SQLException e) {
            // Aggiorna metriche errori
            CONTATORE_ERRORI_CONNESSIONE.incrementAndGet();

            System.err.println("❌ Errore durante la connessione al database: " + e.getMessage());
            throw new SQLException("Impossibile connettersi al database: " + e.getMessage(), e);
        }
    }

    /**
     * Chiude una connessione restituendola al pool
     *
     * <p><b>IMPORTANTE:</b> Questo metodo NON chiude realmente la connessione,
     * ma la restituisce al pool per essere riutilizzata. È fondamentale chiamare
     * questo metodo (o preferibilmente try-with-resources) dopo l'uso della connessione.</p>
     *
     * @param connessione La connessione da restituire al pool (può essere null)
     */
    public static void chiudiConnessione(Connection connessione) {
        if (connessione != null) {
            try {
                if (!connessione.isClosed()) {
                    connessione.close();
                    // Nota: Con HikariCP, close() restituisce la connessione al pool
                    // non decrementiamo il contatore perché le connessioni nel pool sono "attive"
                }
            } catch (SQLException e) {
                System.err.println("⚠️ Errore durante la chiusura della connessione: " + e.getMessage());
            }
        }
    }

    /**
     * Chiude il pool di connessioni e rilascia tutte le risorse
     * Da chiamare solo alla chiusura definitiva dell'applicazione
     */
    public static void chiudiConnessione() {
        System.out.println("🧹 Chiusura pool connessioni database...");

        if (dataSource != null && !dataSource.isClosed()) {
            // Stampa statistiche finali
            System.out.println("📊 Statistiche finali - Totali: " + CONTATORE_CONNESSIONI_TOTALI.get() +
                    ", Errori: " + CONTATORE_ERRORI_CONNESSIONE.get());
            System.out.println("📊 Pool HikariCP:");
            System.out.println("   Connessioni attive: " + dataSource.getHikariPoolMXBean().getActiveConnections());
            System.out.println("   Connessioni idle: " + dataSource.getHikariPoolMXBean().getIdleConnections());
            System.out.println("   Thread in attesa: " + dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection());
            System.out.println("   Totale connessioni create: " + dataSource.getHikariPoolMXBean().getTotalConnections());

            dataSource.close();
            poolInizializzato = false;
            System.out.println("✅ Pool chiuso con successo");
        }
    }

    // ========== METODI DI VERIFICA E VALIDAZIONE ==========

    /**
     * Verifica se è possibile stabilire una connessione con il database
     *
     * @return true se la connessione è stabilita e valida, false altrimenti
     */
    public static boolean verificaConnessione() {
        Connection connessione = null;
        try {
            connessione = getConnessione();
            return connessione != null && !connessione.isClosed() && connessione.isValid(2);
        } catch (SQLException e) {
            System.err.println("❌ Verifica connessione fallita: " + e.getMessage());
            return false;
        } finally {
            chiudiConnessione(connessione);
        }
    }

    /**
     * Verifica rapidamente la connessione con un timeout ridotto
     * Ideale per check veloci dello stato del database
     *
     * @return true se la connessione è disponibile e reattiva, false altrimenti
     */
    public static boolean verificaConnessioneRapida() {
        Connection connessione = null;
        try {
            connessione = getConnessione();
            return connessione.isValid(1); // Timeout di 1 secondo per validazione rapida
        } catch (SQLException e) {
            return false;
        } finally {
            chiudiConnessione(connessione);
        }
    }

    /**
     * Testa le prestazioni della connessione eseguendo una query semplice
     *
     * @return tempo di risposta in millisecondi, -1 se fallisce
     */
    public static long testPrestazioniConnessione() {
        long startTime = System.currentTimeMillis();
        Connection connessione = null;

        try {
            connessione = getConnessione();
            try (var statement = connessione.createStatement();
                 var resultSet = statement.executeQuery("SELECT 1")) {

                if (resultSet.next()) {
                    long endTime = System.currentTimeMillis();
                    return endTime - startTime;
                }
            }
            return -1;

        } catch (SQLException e) {
            return -1;
        } finally {
            chiudiConnessione(connessione);
        }
    }

    /**
     * Esegue un test completo della connessione al database
     * Include verifica connettività, prestazioni, stato del pool
     *
     * @return Report dettagliato del test
     */
    public static String testConnessioneCompleto() {
        StringBuilder report = new StringBuilder();
        report.append("=== TEST CONNESSIONE DATABASE (HikariCP) ===\n");

        // Test connettività base
        report.append("🔌 Connettività base: ");
        boolean connessioneOK = verificaConnessione();
        report.append(connessioneOK ? "✅ OK\n" : "❌ FALLITO\n");

        // Test prestazioni
        report.append("⚡ Test prestazioni: ");
        long tempoRisposta = testPrestazioniConnessione();
        if (tempoRisposta >= 0) {
            report.append("✅ ").append(tempoRisposta).append("ms\n");
        } else {
            report.append("❌ FALLITO\n");
        }

        // Test connessione rapida
        report.append("🚀 Connessione rapida: ");
        boolean rapidaOK = verificaConnessioneRapida();
        report.append(rapidaOK ? "✅ OK\n" : "❌ FALLITO\n");

        // Statistiche pool
        if (dataSource != null && !dataSource.isClosed()) {
            report.append("📊 Statistiche Pool HikariCP:\n");
            report.append("   Attive: ").append(dataSource.getHikariPoolMXBean().getActiveConnections()).append("\n");
            report.append("   Idle: ").append(dataSource.getHikariPoolMXBean().getIdleConnections()).append("\n");
            report.append("   In attesa: ").append(dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection()).append("\n");
            report.append("   Totali create: ").append(dataSource.getHikariPoolMXBean().getTotalConnections()).append("\n");
        }

        // Statistiche generali
        report.append("📊 Statistiche Generali: ");
        report.append("Totali=").append(CONTATORE_CONNESSIONI_TOTALI.get())
                .append(", Errori=").append(CONTATORE_ERRORI_CONNESSIONE.get())
                .append("\n");

        report.append("========================================\n");
        return report.toString();
    }

    // ========== METODI DI UTILITÀ E METRICHE ==========

    /**
     * Restituisce il numero di connessioni attualmente attive nel pool
     *
     * @return Numero di connessioni attive
     */
    public static int getConnessioniAttive() {
        if (dataSource != null && !dataSource.isClosed()) {
            return dataSource.getHikariPoolMXBean().getActiveConnections();
        }
        return 0;
    }

    /**
     * Restituisce il numero di connessioni idle nel pool
     *
     * @return Numero di connessioni idle
     */
    public static int getConnessioniIdle() {
        if (dataSource != null && !dataSource.isClosed()) {
            return dataSource.getHikariPoolMXBean().getIdleConnections();
        }
        return 0;
    }

    /**
     * Restituisce il numero totale di connessioni create dal pool
     *
     * @return Numero totale di connessioni
     */
    public static int getConnessioniTotali() {
        if (dataSource != null && !dataSource.isClosed()) {
            return dataSource.getHikariPoolMXBean().getTotalConnections();
        }
        return CONTATORE_CONNESSIONI_TOTALI.get();
    }

    /**
     * Restituisce il numero di thread in attesa di una connessione
     *
     * @return Numero di thread in attesa
     */
    public static int getThreadsInAttesa() {
        if (dataSource != null && !dataSource.isClosed()) {
            return dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection();
        }
        return 0;
    }

    /**
     * Restituisce il numero di errori di connessione
     *
     * @return Numero di errori
     */
    public static int getErroriConnessione() {
        return CONTATORE_ERRORI_CONNESSIONE.get();
    }

    /**
     * Verifica se il pool è stato inizializzato correttamente
     *
     * @return true se il pool è pronto, false altrimenti
     */
    public static boolean isInizializzato() {
        return poolInizializzato && dataSource != null && !dataSource.isClosed();
    }

    /**
     * Ripristina le metriche e i contatori
     * Utile per test o reset statistiche
     */
    public static void resettaMetriche() {
        CONTATORE_CONNESSIONI_TOTALI.set(0);
        CONTATORE_ERRORI_CONNESSIONE.set(0);
        System.out.println("📊 Metriche connessioni resettate");
    }

    /**
     * Ottiene un report delle statistiche correnti
     *
     * @return Stringa formattata con le statistiche
     */
    public static String getReportStatistiche() {
        StringBuilder sb = new StringBuilder();
        sb.append("📈 Statistiche Connessioni DB (HikariCP)\n");
        sb.append("   Pool Inizializzato: ").append(poolInizializzato ? "✅" : "❌").append("\n");

        if (dataSource != null && !dataSource.isClosed()) {
            sb.append("   Connessioni Attive: ").append(getConnessioniAttive()).append("\n");
            sb.append("   Connessioni Idle: ").append(getConnessioniIdle()).append("\n");
            sb.append("   Totali Create: ").append(getConnessioniTotali()).append("\n");
            sb.append("   Threads in Attesa: ").append(getThreadsInAttesa()).append("\n");
        }

        sb.append("   Errori Connessione: ").append(CONTATORE_ERRORI_CONNESSIONE.get()).append("\n");

        return sb.toString();
    }

    /**
     * Ricarica la configurazione del pool (utile per cambiamenti runtime)
     */
    public static void ricaricaConfigurazione() {
        System.out.println("♻️  Ricaricamento configurazione pool...");

        // Chiudi il pool esistente
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }

        // Riattiva l'inizializzazione
        poolInizializzato = false;

        // Re-inizializza con nuova configurazione
        inizializzaPool();

        System.out.println("✅ Pool ricaricato con nuova configurazione");
    }

    /**
     * Restituisce il DataSource HikariCP (per usi avanzati)
     *
     * @return HikariDataSource o null se non inizializzato
     */
    public static HikariDataSource getDataSource() {
        return dataSource;
    }
}
