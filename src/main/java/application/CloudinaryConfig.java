package application;

import com.cloudinary.Cloudinary;
import java.util.Map;
import java.util.HashMap;

public class CloudinaryConfig {
    private static Cloudinary cloudinary;
    private static boolean available = false;

    static {
        initializeCloudinary();
    }

    private static void initializeCloudinary() {
        try {
            String cloudinaryUrl = System.getenv("CLOUDINARY_URL");

            if (cloudinaryUrl == null || cloudinaryUrl.trim().isEmpty()) {
                System.err.println("⚠️  CLOUDINARY_URL non trovata. Uso configurazione di fallback (Cloudinary disattivato).");
                // configurazione “vuota” per evitare crash
                Map<String, String> dummyConfig = new HashMap<>();
                dummyConfig.put("cloud_name", "demo");
                dummyConfig.put("api_key", "000000000000000");
                dummyConfig.put("api_secret", "xxxxxxxxxxxxxxxxxxxxxxxxxxx");
                cloudinary = new Cloudinary(dummyConfig);
                available = false;
                return;
            }

            // configurazione reale
            cloudinary = new Cloudinary(cloudinaryUrl);
            available = true;
            System.out.println("✅ Cloudinary configurato correttamente");

        } catch (Exception e) {
            System.err.println("❌ Errore nella configurazione di Cloudinary: " + e.getMessage());
            e.printStackTrace();
            available = false;
        }
    }

    public static Cloudinary getCloudinary() {
        return cloudinary;
    }

    public static boolean isAvailable() {
        return available;
    }
}
