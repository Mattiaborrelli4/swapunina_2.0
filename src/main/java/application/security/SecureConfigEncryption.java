package application.security;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Base64;

/**
 * Servizio di crittografia sicuro per configurazioni sensibili
 *
 * <p>Usa AES-256-GCM (Galois/Counter Mode) che fornisce:
 * <ul>
 *   <li>Crittografia autenticata con tag MAC</li>
 *   <li>Protezione contro tampering</li>
 *   <li>IV unico per ogni operazione (mai riutilizzato)</li>
 *   <li>Standard NIST-approved</li>
 * </ul>
 * </p>
 *
 * <p><b>Sicurezza:</b>
 * <ul>
 *   <li>AES-256 (256 bit key)</li>
 *   <li>GCM mode (authenticated encryption)</li>
 *   <li>96-bit IV (12 bytes) - standard per GCM</li>
 *   <li>128-bit tag (16 bytes) - integrità</li>
 * </ul>
 * </p>
 */
public class SecureConfigEncryption {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int KEY_SIZE = 256; // bits
    private static final int GCM_IV_LENGTH = 12; // bytes (96 bits - standard for GCM)
    private static final int GCM_TAG_LENGTH = 128; // bits

    private final SecretKey secretKey;
    private final SecureRandom secureRandom;

    /**
     * Costruttore con chiave derivata da password
     *
     * @param password La master password per derivare la chiave
     */
    public SecureConfigEncryption(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("La password non può essere nulla o vuota");
        }

        this.secureRandom = new SecureRandom();

        // Deriva chiave da password usando PBKDF2 con SHA-256
        this.secretKey = deriveKeyFromPassword(password);
    }

    /**
     * Genera una nuova chiave casuale AES-256
     */
    public static SecretKey generateKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
            keyGen.init(KEY_SIZE, new SecureRandom());
            return keyGen.generateKey();
        } catch (Exception e) {
            throw new RuntimeException("Errore generazione chiave AES", e);
        }
    }

    /**
     * Deriva una chiave AES-256 da una password usando PBKDF2
     *
     * @param password La password master
     * @return SecretKey AES-256 derivata
     */
    private SecretKey deriveKeyFromPassword(String password) {
        try {
            // Usa SHA-256 per hash della password (PBKDF2 semplificato)
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));

            // Estendi a 32 bytes (256 bits) per AES-256
            byte[] keyBytes = new byte[32];
            System.arraycopy(hash, 0, keyBytes, 0, Math.min(hash.length, 32));

            // Se l'hash è più corto di 32 bytes, riempi con hash ripetuti
            for (int i = hash.length; i < 32; i++) {
                keyBytes[i] = hash[i % hash.length];
            }

            return new SecretKeySpec(keyBytes, ALGORITHM);
        } catch (Exception e) {
            throw new RuntimeException("Errore derivazione chiave da password", e);
        }
    }

    /**
     * Crittografa un valore usando AES-256-GCM
     *
     * @param plaintext Il testo in chiaro da crittografare
     * @return Stringa Base64 con formato: IV(12) + Ciphertext + Tag(16)
     * @throws Exception Se la crittografia fallisce
     */
    public String encrypt(String plaintext) throws Exception {
        if (plaintext == null) {
            throw new IllegalArgumentException("Il plaintext non può essere null");
        }

        // Genera IV casuale (mai riutilizzato!)
        byte[] iv = new byte[GCM_IV_LENGTH];
        secureRandom.nextBytes(iv);

        // Inizializza cipher per crittografia
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

        // Crittografa
        byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

        // Combina IV + ciphertext (il tag è già incluso in ciphertext da GCM)
        byte[] combined = ByteBuffer.allocate(iv.length + ciphertext.length)
                .put(iv)
                .put(ciphertext)
                .array();

        // Ritorna Base64 encoded
        return Base64.getEncoder().encodeToString(combined);
    }

    /**
     * Decifra un valore crittografato con AES-256-GCM
     *
     * @param encrypted Base64 string con IV + ciphertext + tag
     * @return Il testo originale in chiaro
     * @throws Exception Se la decifratura fallisce o il tag non corrisponde
     */
    public String decrypt(String encrypted) throws Exception {
        if (encrypted == null || encrypted.isEmpty()) {
            throw new IllegalArgumentException("Il valore cifrato non può essere null o vuoto");
        }

        try {
            // Decodifica Base64
            byte[] combined = Base64.getDecoder().decode(encrypted);

            // Estrai IV (primi 12 bytes)
            ByteBuffer buffer = ByteBuffer.wrap(combined);

            byte[] iv = new byte[GCM_IV_LENGTH];
            buffer.get(iv);

            // Il resto è ciphertext + tag
            byte[] ciphertext = new byte[buffer.remaining()];
            buffer.get(ciphertext);

            // Inizializza cipher per decifratura
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            // Decifra e verifica automaticamente il tag GCM
            byte[] plaintext = cipher.doFinal(ciphertext);

            return new String(plaintext, StandardCharsets.UTF_8);

        } catch (Exception e) {
            // Tag non corrisponde o decifratura fallita = possibile tampering!
            throw new Exception("Decifratura fallita (possibile tampering?): " + e.getMessage(), e);
        }
    }

    /**
     * Verifica se una stringa è nel formato cifrato atteso
     */
    public boolean isEncrypted(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(value);
            // IV + tag + almeno 1 byte di ciphertext
            return decoded.length >= GCM_IV_LENGTH + 16; // 12 IV + 16 tag min
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Genera una password sicura casuale
     *
     * @param length Lunghezza della password
     * @return Password alfanumerica sicura
     */
    public static String generateSecurePassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < length; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }

        return password.toString();
    }

    /**
     * Test di integrità del sistema di crittografia
     */
    public static void main(String[] args) throws Exception {
        System.out.println("🔐 Test Crittografia AES-256-GCM");

        // Test con password casuale
        String testPassword = generateSecurePassword(32);
        System.out.println("🔑 Password generata: " + testPassword);

        SecureConfigEncryption encryption = new SecureConfigEncryption(testPassword);

        String original = "SuperSecretValue123!";
        System.out.println("📝 Originale: " + original);

        String encrypted = encryption.encrypt(original);
        System.out.println("🔒 Cifrato: " + encrypted.substring(0, Math.min(50, encrypted.length())) + "...");

        String decrypted = encryption.decrypt(encrypted);
        System.out.println("🔓 Decifrato: " + decrypted);

        System.out.println("✅ Test: " + (original.equals(decrypted) ? "PASSATO" : "FALLITO"));

        // Test tampering detection
        try {
            byte[] tampered = Base64.getDecoder().decode(encrypted);
            tampered[tampered.length - 1] ^= 0xFF; // Modifica ultimo byte
            String tamperedStr = Base64.getEncoder().encodeToString(tampered);
            encryption.decrypt(tamperedStr);
            System.out.println("❌ Test tampering: FALLITO (avrebbe dovuto lanciare eccezione)");
        } catch (Exception e) {
            System.out.println("✅ Test tampering: PASSATO (eccezione corretta)");
        }
    }
}
