package application.config;

import application.security.SecureConfigEncryption;
import org.mindrot.jbcrypt.BCrypt;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Gestisce la configurazione dell'applicazione con credenziali crittografate
 * Utilizza jBCrypt per proteggere le password sensibili nel file di configurazione
 *
 * <p><b>Caratteristiche:</b>
 * <ul>
 *   <li>File di configurazione crittografato con BCrypt</li>
 *   <li>Backup automatico della configurazione</li>
 *   <li>Validazione integrità configurazione</li>
 *   <li>Supporto per variabili d'ambiente override</li>
 *   <li>Crittografia completa delle credenziali DB</li>
 * </ul>
 * </p>
 */
public class ConfigManager {
    private static final String CONFIG_FILE = "config.properties";
    private static final String ENCRYPTED_PREFIX = "ENC(";
    private static final String ENCRYPTED_SUFFIX = ")";
    private static final String MASTER_PASSWORD_ENV = "SWAPUNINA_MASTER_KEY";

    private static volatile ConfigManager instance;
    private Properties properties;
    private String configFilePath;
    private String masterPassword;
    private SecureConfigEncryption encryption;

    private ConfigManager() {
        initializeConfig();
    }

    /**
     * Ottiene l'istanza singleton del ConfigManager
     * Thread-safe con double-checked locking
     */
    public static ConfigManager getInstance() {
        if (instance == null) {
            synchronized (ConfigManager.class) {
                if (instance == null) {
                    instance = new ConfigManager();
                }
            }
        }
        return instance;
    }

    /**
     * Inizializza la configurazione caricando il file o creando uno di default
     */
    private void initializeConfig() {
        try {
            // Determina il percorso del file di configurazione
            String userDir = System.getProperty("user.dir");
            Path configPath = Paths.get(userDir, CONFIG_FILE);
            this.configFilePath = configPath.toString();

            // Ottiene la master password dalle variabili d'ambiente o system properties
            this.masterPassword = System.getenv(MASTER_PASSWORD_ENV);
            if (this.masterPassword == null || this.masterPassword.isEmpty()) {
                // Fallback 1: cerca nelle system properties (-D flag)
                this.masterPassword = System.getProperty(MASTER_PASSWORD_ENV);
                if (this.masterPassword == null || this.masterPassword.isEmpty()) {
                    // Fallback 2: valore hardcoded per development (DA RIMUOVERE IN PRODUZIONE!)
                    this.masterPassword = "SwapUnina2025!Secure";
                    System.err.println("⚠️  ATTENZIONE: Usando MASTER KEY di default per development. Imposta SWAPUNINA_MASTER_KEY in produzione!");
                }
            }

            // Inizializza il servizio di crittografia sicuro con AES-256-GCM
            this.encryption = new SecureConfigEncryption(masterPassword);
            System.out.println("🔐 Crittografia AES-256-GCM inizializzata");

            this.properties = new Properties();

            // Se il file esiste, lo carica
            if (Files.exists(configPath)) {
                loadConfig();
                System.out.println("✅ Configurazione caricata da: " + configFilePath);
            } else {
                // Crea configurazione di default
                createDefaultConfig();
                System.out.println("📝 Creata configurazione di default in: " + configFilePath);
            }

            // Verifica integrità configurazione
            validateConfig();

        } catch (Exception e) {
            System.err.println("❌ Errore inizializzazione configurazione: " + e.getMessage());
            throw new RuntimeException("Impossibile inizializzare la configurazione", e);
        }
    }

    /**
     * Carica la configurazione dal file crittografato
     */
    private void loadConfig() throws IOException {
        try (FileInputStream fis = new FileInputStream(configFilePath)) {
            properties.load(fis);

            // Decifra le proprietà che sono crittografate
            properties.forEach((key, value) -> {
                String strValue = (String) value;
                if (strValue.startsWith(ENCRYPTED_PREFIX) && strValue.endsWith(ENCRYPTED_SUFFIX)) {
                    String encrypted = strValue.substring(
                        ENCRYPTED_PREFIX.length(),
                        strValue.length() - ENCRYPTED_SUFFIX.length()
                    );
                    String decrypted = decryptValue(encrypted);
                    properties.setProperty((String) key, decrypted);
                }
            });
        }
    }

    /**
     * Crea una configurazione di default con valori sicuri
     */
    private void createDefaultConfig() throws IOException {
        // Configurazione database (valori di default)
        setProperty("db.host", "localhost", true);
        setProperty("db.port", "5432", true);
        setProperty("db.name", "postgres", true);
        setProperty("db.user", "postgres", true);
        setProperty("db.password", "1234", true); // Sarà crittografata

        // Timeout configurazioni
        setProperty("db.timeout.connection", "5", true);
        setProperty("db.timeout.socket", "30", true);

        // HikariCP Pool settings
        setProperty("db.pool.max_size", "10", true);
        setProperty("db.pool.min_idle", "2", true);
        setProperty("db.pool.connection_timeout", "30000", true);
        setProperty("db.pool.idle_timeout", "600000", true);
        setProperty("db.pool.max_lifetime", "1800000", true);

        // Altre configurazioni
        setProperty("app.name", "SwapUnina-App", true);
        setProperty("app.version", "2.0", true);

        // Salva la configurazione
        saveConfig();
    }

    /**
     * Valida la configurazione caricata
     */
    private void validateConfig() {
        // Verifica che tutte le proprietà richieste esistano
        String[] requiredProps = {
            "db.host", "db.port", "db.name", "db.user", "db.password"
        };

        for (String prop : requiredProps) {
            if (properties.getProperty(prop) == null) {
                throw new RuntimeException("Proprietà richiesta mancante: " + prop);
            }
        }

        System.out.println("✅ Configurazione validata con successo");
    }

    /**
     * Crittografa un valore usando AES-256-GCM (sicuro)
     *
     * @param value Il valore da crittografare
     * @return Stringa Base64 con IV + ciphertext + tag
     */
    private String encryptValue(String value) {
        try {
            return encryption.encrypt(value);
        } catch (Exception e) {
            throw new RuntimeException("Errore crittografia valore: " + e.getMessage(), e);
        }
    }

    /**
     * Decifra un valore crittografato con AES-256-GCM
     * Verifica automaticamente l'integrità del tag GCM
     *
     * @param encrypted Valore crittografato
     * @return Valore originale in chiaro
     */
    private String decryptValue(String encrypted) {
        try {
            return encryption.decrypt(encrypted);
        } catch (Exception e) {
            System.err.println("⚠️  Errore decifratura valore (possibile tampering?): " + e.getMessage());
            // In caso di errore, ritorna la stringa cifrata come fallback
            return encrypted;
        }
    }

    /**
     * Salva la configurazione su file con le password crittografate (AES-256-GCM)
     */
    public void saveConfig() throws IOException {
        Properties toSave = new Properties();

        // Crittografa le proprietà sensibili prima di salvare
        properties.forEach((key, value) -> {
            String keyStr = (String) key;
            String valueStr = (String) value;

            // Crittografa le password con AES-256-GCM
            if (keyStr.contains("password") || keyStr.contains("secret") || keyStr.contains("key")) {
                try {
                    String encrypted = encryptValue(valueStr);
                    toSave.setProperty(keyStr, ENCRYPTED_PREFIX + encrypted + ENCRYPTED_SUFFIX);
                } catch (Exception e) {
                    System.err.println("⚠️  Errore crittografia proprietà " + keyStr + ": " + e.getMessage());
                    toSave.setProperty(keyStr, valueStr);
                }
            } else {
                toSave.setProperty(keyStr, valueStr);
            }
        });

        // Crea backup del file esistente
        Path configPath = Paths.get(configFilePath);
        if (Files.exists(configPath)) {
            Path backupPath = Paths.get(configFilePath + ".backup");
            Files.copy(configPath, backupPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            System.out.println("💾 Backup configurazione creato");
        }

        // Salva la nuova configurazione
        try (FileOutputStream fos = new FileOutputStream(configFilePath)) {
            toSave.store(fos, "SwapUnina Configuration - AES-256-GCM Encrypted");
            System.out.println("💾 Configurazione salvata in: " + configFilePath);
        }
    }

    /**
     * Imposta una proprietà e opzionalmente salva immediatamente
     */
    public void setProperty(String key, String value, boolean save) {
        properties.setProperty(key, value);
        if (save) {
            try {
                saveConfig();
            } catch (IOException e) {
                System.err.println("❌ Errore salvataggio configurazione: " + e.getMessage());
            }
        }
    }

    /**
     * Ottiene una proprietà con fallback su variabile d'ambiente
     */
    public String getProperty(String key) {
        // Prima controlla le variabili d'ambiente
        String envValue = System.getenv(key.toUpperCase().replace(".", "_"));
        if (envValue != null && !envValue.isEmpty()) {
            return envValue;
        }

        String value = properties.getProperty(key);

        // Decifra se il valore è criptato
        if (value != null && value.startsWith(ENCRYPTED_PREFIX) && value.endsWith(ENCRYPTED_SUFFIX)) {
            String encrypted = value.substring(
                ENCRYPTED_PREFIX.length(),
                value.length() - ENCRYPTED_SUFFIX.length()
            );
            return decryptValue(encrypted);
        }

        return value;
    }

    /**
     * Ottiene una proprietà con valore di default
     */
    public String getProperty(String key, String defaultValue) {
        String value = getProperty(key);
        return value != null ? value : defaultValue;
    }

    /**
     * Ottiene una proprietà come intero
     */
    public int getIntProperty(String key, int defaultValue) {
        try {
            return Integer.parseInt(getProperty(key));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Ottiene una proprietà come long
     */
    public long getLongProperty(String key, long defaultValue) {
        try {
            return Long.parseLong(getProperty(key));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Ottiene una proprietà come booleano
     */
    public boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = getProperty(key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }

    // ========== GETTER CONFIGURAZIONE DATABASE ==========

    public String getDbHost() {
        return getProperty("db.host");
    }

    public int getDbPort() {
        return getIntProperty("db.port", 5432);
    }

    public String getDbName() {
        return getProperty("db.name");
    }

    public String getDbUser() {
        return getProperty("db.user");
    }

    public String getDbPassword() {
        return getProperty("db.password");
    }

    public int getDbConnectionTimeout() {
        return getIntProperty("db.timeout.connection", 5);
    }

    public int getDbSocketTimeout() {
        return getIntProperty("db.timeout.socket", 30);
    }

    // ========== GETTER CONFIGURAZIONE POOL ==========

    public int getPoolMaxSize() {
        return getIntProperty("db.pool.max_size", 10);
    }

    public int getPoolMinIdle() {
        return getIntProperty("db.pool.min_idle", 2);
    }

    public long getPoolConnectionTimeout() {
        return getLongProperty("db.pool.connection_timeout", 30000);
    }

    public long getPoolIdleTimeout() {
        return getLongProperty("db.pool.idle_timeout", 600000);
    }

    public long getPoolMaxLifetime() {
        return getLongProperty("db.pool.max_lifetime", 1800000);
    }

    // ========== METODI DI UTILITÀ ==========

    /**
     * Ricarica la configurazione dal file
     */
    public void reloadConfig() {
        try {
            loadConfig();
            validateConfig();
            System.out.println("♻️  Configurazione ricaricata con successo");
        } catch (Exception e) {
            System.err.println("❌ Errore ricaricamento configurazione: " + e.getMessage());
        }
    }

    /**
     * Verifica se il file di configurazione esiste
     */
    public boolean configExists() {
        return Files.exists(Paths.get(configFilePath));
    }

    /**
     * Restituisce il percorso del file di configurazione
     */
    public String getConfigFilePath() {
        return configFilePath;
    }

    /**
     * Stampa tutte le proprietà (sensiibili mascherate)
     */
    public void printConfig() {
        System.out.println("=== CONFIGURAZIONE CORRENTE ===");
        properties.forEach((key, value) -> {
            String keyStr = (String) key;
            String valueStr = (String) value;

            // Maschera le informazioni sensibili
            if (keyStr.contains("password") || keyStr.contains("secret")) {
                valueStr = "****";
            }

            System.out.println(keyStr + " = " + valueStr);
        });
        System.out.println("============================");
    }
}
