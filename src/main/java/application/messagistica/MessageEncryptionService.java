package application.messagistica;

import javax.crypto.KeyGenerator;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Servizio centralizzato per la crittografia e decrittografia dei messaggi
 * Utilizza AES-256 GCM per una crittografia sicura e autenticata
 *
 * <p><b>Caratteristiche:</b>
 * <ul>
 *   <li>Crittografia AES-256 GCM (autenticata)</li>
 *   <li>IV univoco per ogni messaggio (12 bytes)</li>
 *   <li>Chiave globale derivata automaticamente</li>
 *   <li>Supporto per chiavi per conversazione</li>
 *   <li>Gestione sicura delle chiavi</li>
 * </ul>
 * </p>
 *
 * <p><b>Flusso di crittografia:</b>
 * <pre>
 * Messaggio in chiaro
 *    ↓
 * Generazione IV random (12 bytes)
 *    ↓
 * Crittografia AES-GCM (chiave 256-bit)
 *    ↓
 * Messaggio cifrato + IV salvati nel DB
 * </pre>
 * </p>
 */
public class MessageEncryptionService {
    private static volatile MessageEncryptionService instance;
    private final byte[] globalKey;
    private final Map<Integer, byte[]> conversationKeys; // Chiavi per conversazione
    private final SecureRandom secureRandom;

    private static final int KEY_SIZE = 256; // AES-256
    private static final int IV_LENGTH = 12; // Per GCM
    private static final int GCM_TAG_LENGTH = 128; // Tag autenticazione

    private MessageEncryptionService() {
        this.secureRandom = new SecureRandom();
        this.conversationKeys = new HashMap<>();

        // Genera o carica la chiave globale
        this.globalKey = loadOrCreateGlobalKey();

        System.out.println("🔐 MessageEncryptionService inizializzato con AES-256 GCM");
    }

    /**
     * Ottiene l'istanza singleton del servizio
     */
    public static MessageEncryptionService getInstance() {
        if (instance == null) {
            synchronized (MessageEncryptionService.class) {
                if (instance == null) {
                    instance = new MessageEncryptionService();
                }
            }
        }
        return instance;
    }

    /**
     * Carica o crea la chiave globale per la crittografia
     */
    private byte[] loadOrCreateGlobalKey() {
        try {
            // Prima controlla se c'è una chiave salvata nelle proprietà di sistema
            String savedKey = System.getProperty("swapunina.encryption.key");
            if (savedKey != null && !savedKey.isEmpty()) {
                return Base64.getDecoder().decode(savedKey);
            }

            // Se non esiste, genera una nuova chiave
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(KEY_SIZE);
            byte[] key = keyGen.generateKey().getEncoded();

            // Salva la chiave nelle proprietà di sistema per questa sessione
            System.setProperty("swapunina.encryption.key", Base64.getEncoder().encodeToString(key));

            System.out.println("🔑 Nuova chiave di crittografia AES-256 generata");

            return key;
        } catch (Exception e) {
            System.err.println("❌ Errore generazione chiave: " + e.getMessage());
            throw new RuntimeException("Impossibile inizializzare la chiave di crittografia", e);
        }
    }

    /**
     * Crittografa un messaggio usando AES-GCM
     *
     * @param plaintext Il messaggio in chiaro
     * @return EncryptedMessageContainer con dati cifzati e IV
     * @throws Exception Se la crittografia fallisce
     */
    public EncryptedMessageContainer encryptMessage(String plaintext) throws Exception {
        if (plaintext == null || plaintext.isEmpty()) {
            throw new IllegalArgumentException("Il messaggio non può essere vuoto");
        }

        try {
            // Genera IV univoco per questo messaggio
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);

            // Usa MessageEncryptor per la crittografia
            MessageEncryptor.EncryptedMessage encrypted =
                MessageEncryptor.encryptMessage(plaintext, globalKey);

            // Crea il container con i dati crittografati
            return new EncryptedMessageContainer(
                encrypted.getEncryptedData(),
                iv,
                "AES/GCM/NoPadding",
                GCM_TAG_LENGTH
            );

        } catch (Exception e) {
            System.err.println("❌ Errore crittografia messaggio: " + e.getMessage());
            throw new Exception("Impossibile crittografare il messaggio", e);
        }
    }

    /**
     * Decrittografa un messaggio crittografato
     *
     * @param container Il container con i dati cifrati
     * @return Il messaggio in chiaro
     * @throws Exception Se la decifratura fallisce
     */
    public String decryptMessage(EncryptedMessageContainer container) throws Exception {
        if (container == null) {
            throw new IllegalArgumentException("Il container non può essere null");
        }

        try {
            // Ricostruisci l'oggetto EncryptedMessage
            MessageEncryptor.EncryptedMessage encryptedMessage =
                new MessageEncryptor.EncryptedMessage(
                    container.getEncryptedData(),
                    container.getIv()
                );

            // Decrittografa usando MessageEncryptor
            String plaintext = MessageEncryptor.decryptMessage(encryptedMessage, globalKey);

            return plaintext;

        } catch (Exception e) {
            System.err.println("❌ Errore decifratura messaggio: " + e.getMessage());
            throw new Exception("Impossibile decifrare il messaggio", e);
        }
    }

    /**
     * Crittografa un messaggio usando una chiave specifica per conversazione
     * Utile per crittografia end-to-end tra due utenti
     *
     * @param plaintext Il messaggio in chiaro
     * @param conversationId ID della conversazione
     * @return EncryptedMessageContainer con dati cifrati
     * @throws Exception Se la crittografia fallisce
     */
    public EncryptedMessageContainer encryptMessageForConversation(String plaintext, int conversationId) throws Exception {
        // Ottieni o crea la chiave per questa conversazione
        byte[] conversationKey = conversationKeys.computeIfAbsent(conversationId, k -> {
            try {
                KeyGenerator keyGen = KeyGenerator.getInstance("AES");
                keyGen.init(KEY_SIZE);
                return keyGen.generateKey().getEncoded();
            } catch (Exception e) {
                throw new RuntimeException("Errore generazione chiave conversazione", e);
            }
        });

        // Genera IV univoco
        byte[] iv = new byte[IV_LENGTH];
        secureRandom.nextBytes(iv);

        // Crittografa con la chiave della conversazione
        MessageEncryptor.EncryptedMessage encrypted =
            MessageEncryptor.encryptMessage(plaintext, conversationKey);

        return new EncryptedMessageContainer(
            encrypted.getEncryptedData(),
            iv,
            "AES/GCM/NoPadding",
            GCM_TAG_LENGTH
        );
    }

    /**
     * Decrittografa un messaggio di una conversazione specifica
     *
     * @param container Il container con i dati cifrati
     * @param conversationId ID della conversazione
     * @return Il messaggio in chiaro
     * @throws Exception Se la decifratura fallisce
     */
    public String decryptMessageFromConversation(EncryptedMessageContainer container, int conversationId) throws Exception {
        byte[] conversationKey = conversationKeys.get(conversationId);
        if (conversationKey == null) {
            throw new Exception("Chiave per conversazione " + conversationId + " non trovata");
        }

        MessageEncryptor.EncryptedMessage encryptedMessage =
            new MessageEncryptor.EncryptedMessage(
                container.getEncryptedData(),
                container.getIv()
            );

        return MessageEncryptor.decryptMessage(encryptedMessage, conversationKey);
    }

    /**
     * Genera una nuova chiave per una conversazione specifica
     *
     * @param conversationId ID della conversazione
     * @return La chiave generata in Base64 (da condividere con gli utenti)
     * @throws Exception Se la generazione fallisce
     */
    public String generateConversationKey(int conversationId) throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(KEY_SIZE);
        byte[] key = keyGen.generateKey().getEncoded();

        conversationKeys.put(conversationId, key);

        return Base64.getEncoder().encodeToString(key);
    }

    /**
     * Imposta una chiave per una conversazione (usata quando ricevuta da altro utente)
     *
     * @param conversationId ID della conversazione
     * @param keyBase64 La chiave in formato Base64
     */
    public void setConversationKey(int conversationId, String keyBase64) {
        byte[] key = Base64.getDecoder().decode(keyBase64);
        conversationKeys.put(conversationId, key);
    }

    /**
     * Verifica se una chiave per conversazione esiste
     *
     * @param conversationId ID della conversazione
     * @return true se la chiave esiste
     */
    public boolean hasConversationKey(int conversationId) {
        return conversationKeys.containsKey(conversationId);
    }

    /**
     * Rimuove una chiave di conversazione (quando la conversazione termina)
     *
     * @param conversationId ID della conversazione
     */
    public void removeConversationKey(int conversationId) {
        conversationKeys.remove(conversationId);
    }

    /**
     * Pulisce tutte le chiavi di conversazione
     */
    public void clearAllConversationKeys() {
        conversationKeys.clear();
        System.out.println("🗑️  Tutte le chiavi di conversazione rimosse");
    }

    /**
     * Container per i dati di un messaggio crittografato
     */
    public static class EncryptedMessageContainer {
        private final byte[] encryptedData;
        private final byte[] iv;
        private final String algorithm;
        private final int tagLength;

        public EncryptedMessageContainer(byte[] encryptedData, byte[] iv, String algorithm, int tagLength) {
            this.encryptedData = encryptedData;
            this.iv = iv;
            this.algorithm = algorithm;
            this.tagLength = tagLength;
        }

        public byte[] getEncryptedData() {
            return encryptedData;
        }

        public byte[] getIv() {
            return iv;
        }

        public String getAlgorithm() {
            return algorithm;
        }

        public int getTagLength() {
            return tagLength;
        }

        /**
         * Converte i dati crittografati in Base64 per storage
         */
        public String getEncryptedDataBase64() {
            return Base64.getEncoder().encodeToString(encryptedData);
        }

        /**
         * Converte l'IV in Base64 per storage
         */
        public String getIvBase64() {
            return Base64.getEncoder().encodeToString(iv);
        }

        /**
         * Crea un container da dati Base64
         */
        public static EncryptedMessageContainer fromBase64(String encryptedDataBase64, String ivBase64, String algorithm, int tagLength) {
            byte[] encryptedData = Base64.getDecoder().decode(encryptedDataBase64);
            byte[] iv = Base64.getDecoder().decode(ivBase64);

            return new EncryptedMessageContainer(encryptedData, iv, algorithm, tagLength);
        }
    }

    /**
     * Verifica l'integrità di un messaggio crittografato
     *
     * @param container Il container da verificare
     * @return true se il messaggio sembra valido
     */
    public boolean verifyMessageIntegrity(EncryptedMessageContainer container) {
        if (container == null) {
            return false;
        }

        // Verifica che i dati non siano vuoti
        if (container.getEncryptedData() == null || container.getEncryptedData().length == 0) {
            return false;
        }

        // Verifica che l'IV sia della lunghezza corretta
        if (container.getIv() == null || container.getIv().length != IV_LENGTH) {
            return false;
        }

        // Verifica l'algoritmo
        if (!"AES/GCM/NoPadding".equals(container.getAlgorithm())) {
            return false;
        }

        return true;
    }
}
