package application.security;

/**
 * Utility per generare password criptata per il database
 */
public class GenerateDBPassword {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Uso: java GenerateDBPassword <password_in_chiaro>");
            System.err.println("Esempio: java GenerateDBPassword postgres123");
            System.exit(1);
        }

        try {
            String masterPassword = System.getenv("SWAPUNINA_MASTER_KEY");
            if (masterPassword == null || masterPassword.isEmpty()) {
                System.err.println("❌ ERRORE: Imposta la variabile d'ambiente SWAPUNINA_MASTER_KEY");
                System.err.println("export SWAPUNINA_MASTER_KEY=\"tua_password_sicura\"");
                System.exit(1);
            }

            SecureConfigEncryption encryption = new SecureConfigEncryption(masterPassword);
            String plainPassword = args[0];
            String encryptedPassword = encryption.encrypt(plainPassword);

            System.out.println("✅ Password criptata con successo!");
            System.out.println("Password in chiaro: " + plainPassword);
            System.out.println("Password criptata: ENC(" + encryptedPassword + ")");
            System.out.println("\nCopia questo valore nel config.properties:");

        } catch (Exception e) {
            System.err.println("❌ Errore: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
