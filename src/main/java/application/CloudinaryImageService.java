package application;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servizio per la gestione del caricamento e eliminazione delle immagini su Cloudinary.
 *
 * <p>Questo servizio fornisce metodi per:</p>
 * <ul>
 *   <li>Caricare immagini di annunci, profili utenti e categorie</li>
 *   <li>Eliminare immagini da Cloudinary</li>
 *   <li>Generare URL ottimizzati per diverse dimensioni</li>
 *   <li>Gestire fallback locale quando Cloudinary non è disponibile</li>
 * </ul>
 *
 * @author SwapUnina Development Team
 * @version 2.0
 * @since 1.0
 */
public class CloudinaryImageService {

    private static final Logger LOGGER = Logger.getLogger(CloudinaryImageService.class.getName());

    private final Cloudinary cloudinary;
    private final boolean enabled;

    /**
     * Costruttore che inizializza il servizio Cloudinary.
     * Utilizza la configurazione globale dalla classe config.
     */
    public CloudinaryImageService() {
        this.cloudinary = CloudinaryConfig.isAvailable() ? CloudinaryConfig.getCloudinary() : null;
        this.enabled = this.cloudinary != null;

        if (enabled) {
            LOGGER.log(Level.INFO, "✅ CloudinaryImageService inizializzato correttamente");
        } else {
            LOGGER.log(Level.WARNING, "⚠️ CloudinaryImageService disabilitato - modalità fallback locale attiva");
        }
    }

    /**
     * Carica un'immagine per un annuncio da InputStream.
     *
     * @param is Stream di input dell'immagine
     * @param originalFilename Nome originale del file
     * @param annuncioId ID dell'annuncio associato
     * @return URL pubblico dell'immagine caricata
     * @throws Exception se il caricamento fallisce
     */
    public String uploadAnnuncioImage(InputStream is, String originalFilename, int annuncioId) throws Exception {
        String publicId = generateAnnuncioPublicId(annuncioId);
        return upload(is, originalFilename, "annunci", publicId);
    }

    /**
     * Carica un'immagine per un annuncio da File.
     *
     * @param file File dell'immagine da caricare
     * @param annuncioId ID dell'annuncio associato
     * @return URL pubblico dell'immagine caricata
     * @throws Exception se il caricamento fallisce
     */
    public String uploadAnnuncioImage(File file, int annuncioId) throws Exception {
        try (InputStream is = new FileInputStream(file)) {
            return uploadAnnuncioImage(is, file.getName(), annuncioId);
        }
    }

    /**
     * Carica un'immagine per un annuncio con suffisso personalizzato.
     *
     * @param file File dell'immagine da caricare
     * @param publicIdSuffix Suffisso per il public_id
     * @return URL pubblico dell'immagine caricata
     * @throws Exception se il caricamento fallisce
     */
    public String uploadAnnuncioImage(File file, String publicIdSuffix) throws Exception {
        try (InputStream is = new FileInputStream(file)) {
            return upload(is, file.getName(), "annunci", publicIdSuffix);
        }
    }

    /**
     * Carica un'immagine del profilo utente.
     *
     * @param file File dell'immagine del profilo
     * @param userId ID dell'utente
     * @return URL pubblico dell'immagine caricata
     * @throws Exception se il caricamento fallisce
     */
    public String uploadProfileImage(File file, int userId) throws Exception {
        try (InputStream is = new FileInputStream(file)) {
            String publicId = generateUserProfilePublicId(userId);
            return upload(is, file.getName(), "profiles", publicId);
        }
    }

    /**
     * Carica un'immagine per una categoria.
     *
     * @param file File dell'immagine della categoria
     * @param categoriaNome Nome della categoria
     * @return URL pubblico dell'immagine caricata
     * @throws Exception se il caricamento fallisce
     */
    public String uploadCategoryImage(File file, String categoriaNome) throws Exception {
        try (InputStream is = new FileInputStream(file)) {
            String publicId = "cat_" + categoriaNome.toLowerCase().replaceAll("[^a-z0-9]", "_");
            return upload(is, file.getName(), "categories", publicId);
        }
    }

    /**
     * Carica un file generico in una cartella specifica.
     *
     * @param file File da caricare
     * @param folder Cartella di destinazione su Cloudinary
     * @param publicIdSuffix Suffisso per il public_id
     * @return URL pubblico del file caricato
     * @throws Exception se il caricamento fallisce
     */
    public String uploadFile(File file, String folder, String publicIdSuffix) throws Exception {
        try (InputStream is = new FileInputStream(file)) {
            return upload(is, file.getName(), folder, publicIdSuffix);
        }
    }

    /**
     * Metodo principale di caricamento immagini su Cloudinary.
     *
     * @param is Stream di input dell'immagine
     * @param originalFilename Nome originale del file
     * @param folder Cartella di destinazione
     * @param publicIdSuffix Suffisso per il public_id
     * @return URL pubblico sicuro dell'immagine caricata
     * @throws Exception se il caricamento fallisce
     */
    public String upload(InputStream is, String originalFilename, String folder, String publicIdSuffix) throws Exception {
        if (!enabled) {
            throw new IllegalStateException("Cloudinary non è configurato. Imposta la variabile d'ambiente CLOUDINARY_URL");
        }

        // Estrai l'estensione del file per determinare il formato
        String extension = extractFileExtension(originalFilename);

        // Genera un public_id univoco
        String publicId = generatePublicId(publicIdSuffix);

        // Configura le opzioni di upload
        Map<String, Object> options = buildUploadOptions(folder, publicId);

        // Esegui l'upload
        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(is, options);
            String secureUrl = (String) uploadResult.get("secure_url");

            if (secureUrl != null && !secureUrl.isEmpty()) {
                LOGGER.log(Level.INFO, "✅ Immagine caricata: folder={0}, publicId={1}", new Object[]{folder, publicId});
                return secureUrl;
            } else {
                throw new RuntimeException("Upload completato ma nessun URL restituito");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "❌ Errore caricamento su Cloudinary: {0}", e.getMessage());
            throw e;
        }
    }

    /**
     * Elimina un'immagine da Cloudinary.
     *
     * @param imageUrl URL dell'immagine da eliminare
     * @return true se l'eliminazione è avvenuta con successo, false altrimenti
     */
    public boolean deleteImage(String imageUrl) {
        if (!enabled || imageUrl == null || imageUrl.isEmpty()) {
            LOGGER.log(Level.WARNING, "⚠️ Impossibile eliminare: servizio disabilitato o URL nullo");
            return false;
        }

        try {
            String publicId = extractPublicIdFromUrl(imageUrl);
            if (publicId == null) {
                LOGGER.log(Level.WARNING, "⚠️ Impossibile estrarre public_id dall'URL: {0}", imageUrl);
                return false;
            }

            Map<?, ?> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            String outcome = (String) result.get("result");

            if ("ok".equals(outcome)) {
                LOGGER.log(Level.INFO, "✅ Immagine eliminata: publicId={0}", publicId);
                return true;
            } else {
                LOGGER.log(Level.WARNING, "⚠️ Eliminazione fallita: result={0}", outcome);
                return false;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "❌ Errore eliminazione immagine: {0}", e.getMessage());
            return false;
        }
    }

    /**
     * Genera un URL ottimizzato per la visualizzazione in card prodotto.
     *
     * @param originalUrl URL originale dell'immagine
     * @return URL ottimizzato per card (280x200)
     */
    public String getOptimizedCardUrl(String originalUrl) {
        return getOptimizedUrl(originalUrl, 280, 200);
    }

    /**
     * Genera un URL ottimizzato per la visualizzazione grande.
     *
     * @param originalUrl URL originale dell'immagine
     * @return URL ottimizzato per large (600x400)
     */
    public String getOptimizedLargeUrl(String originalUrl) {
        return getOptimizedUrl(originalUrl, 600, 400);
    }

    /**
     * Genera un URL ottimizzato per thumbnail.
     *
     * @param originalUrl URL originale dell'immagine
     * @return URL ottimizzato per thumbnail (100x100)
     */
    public String getOptimizedThumbnailUrl(String originalUrl) {
        return getOptimizedUrl(originalUrl, 100, 100);
    }

    // ==================== METODI DI UTILITÀ ====================

    /**
     * Verifica se il servizio Cloudinary è disponibile e configurato.
     *
     * @return true se Cloudinary è abilitato, false altrimenti
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Verifica se un URL è un URL Cloudinary valido.
     *
     * @param imageUrl URL da verificare
     * @return true se l'URL proviene da Cloudinary, false altrimenti
     */
    public boolean isCloudinaryUrl(String imageUrl) {
        return imageUrl != null && imageUrl.contains("cloudinary.com");
    }

    /**
     * Normalizza un URL immagine rimuovendo spazi e valori null.
     *
     * @param imageUrl URL da normalizzare
     * @return URL normalizzato o stringa vuota se null
     */
    public String normalizeUrl(String imageUrl) {
        if (imageUrl == null || "null".equals(imageUrl)) {
            return "";
        }
        return imageUrl.trim();
    }

    // ==================== METODI PRIVATI ====================

    /**
     * Genera un public_id per un annuncio.
     */
    private String generateAnnuncioPublicId(int annuncioId) {
        return "annuncio_" + annuncioId + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Genera un public_id per il profilo utente.
     */
    private String generateUserProfilePublicId(int userId) {
        return "user_profile_" + userId;
    }

    /**
     * Genera un public_id univoco.
     */
    private String generatePublicId(String suffix) {
        return suffix + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Estrae l'estensione del file.
     */
    private String extractFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex >= 0 && dotIndex < filename.length() - 1) {
            return filename.substring(dotIndex + 1).toLowerCase();
        }
        return "jpg"; // Estensione di default
    }

    /**
     * Costruisce le opzioni di upload per Cloudinary.
     */
    private Map<String, Object> buildUploadOptions(String folder, String publicId) {
        return ObjectUtils.asMap(
            "folder", folder,
            "public_id", publicId,
            "resource_type", "image",
            "overwrite", true,
            "transformation", ObjectUtils.asMap(
                "quality", "auto",
                "fetch_format", "auto"
            )
        );
    }

    /**
     * Genera un URL ottimizzato con dimensioni specifiche.
     */
    private String getOptimizedUrl(String originalUrl, int width, int height) {
        if (!isCloudinaryUrl(originalUrl)) {
            return originalUrl;
        }
        return originalUrl.replace("/upload/", "/upload/w_" + width + ",h_" + height + ",c_fill/");
    }

    /**
     * Estrae il public_id dall'URL Cloudinary.
     */
    private String extractPublicIdFromUrl(String imageUrl) {
        try {
            // L'URL Cloudinary ha formato: https://res.cloudinary.com/cloudname/image/upload/v1234567/folder/public_id.ext
            String[] parts = imageUrl.split("/upload/");
            if (parts.length > 1) {
                String path = parts[1];
                // Rimuovi la versione se presente (v1234567/)
                if (path.startsWith("v")) {
                    path = path.substring(path.indexOf('/') + 1);
                }
                // Rimuovi l'estensione del file
                int dotIndex = path.lastIndexOf('.');
                if (dotIndex > 0) {
                    path = path.substring(0, dotIndex);
                }
                return path;
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Errore nell'estrazione del public_id: {0}", e.getMessage());
        }
        return null;
    }
}
